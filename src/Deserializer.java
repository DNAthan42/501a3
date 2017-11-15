import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

public class Deserializer {
	public IdentityHashMap<Integer, Object> deserialized;
	private Element root;

	public Deserializer(){
		deserialized = new IdentityHashMap<>();
	}

	public Object deserialize(Document document){
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		System.out.println(outputter.outputString(document));
		root = document.getRootElement();
		Element firstObject = findFirstElement(root);
		if (firstObject == null) return null;
		return elementToObject(firstObject);
	}

	private Element findFirstElement(Element parent){
		Element ret = null;
		for (Element element: parent.getChildren()){
			if (!(element.getName().equals("object"))) continue;
			if (ret == null) ret = element;
			else if (Integer.parseInt(element.getAttributeValue("id"))
					< Integer.parseInt(ret.getAttributeValue("id")))
				ret = element;

		}
		return ret;
	}

	private Object elementToObject(Element element){
		//get the reference number for this object
		int id = Integer.parseInt(element.getAttributeValue("id"));
		//if the object has already been deserialized, return the object.
		if (deserialized.containsKey(id)) return deserialized.get(id);

		if (element.getAttribute("length") != null) { //Need to handle arrays separately
			return elementToArray(element);
		}

		//Get the element's type from the class attr
		Class eleClass;
		try{
			eleClass = Class.forName(element.getAttributeValue("class"));
		} catch (ClassNotFoundException e){
			System.out.println("Could not find class " + element.getAttributeValue("class"));
			return null;
		}

		//try to instantiate with the no arg constructor
		Object eleObj;
		try {
			eleObj = eleClass.newInstance();
		} catch (InstantiationException e) {
			System.out.println("Could not find nullary constructor for class" + eleClass.toString());
			return null;
		} catch (IllegalAccessException e) {
			System.out.println("Could not access nullary constructor for class" + eleClass.toString());
			return null;
		}

		//mark Object as now instantiated,
		deserialized.put(id, eleObj);

		//match fields of this object to values provided
		LinkedList<Field> fields = getAllFields(eleClass);
		for (Field field: fields){
			//can't update static final fields, so just skip them
			if (Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) continue;

			field.setAccessible(true);
			//get new value from the element
			Element eleField = findElementByAttr(element, "name", field.getName());
			//check if the new value of the field is a primitive or not.
			if (eleField.equals("value")){
				Class fType = field.getType();
				try {
					if (fType == double.class) field.setDouble(eleObj, Double.parseDouble(eleField.getText()));
					else if (fType == float.class) field.setFloat(eleObj, Float.parseFloat(eleField.getText()));
					else if (fType == long.class) field.setLong(eleObj, Long.parseLong(eleField.getText()));
					else if (fType == int.class) field.setInt(eleObj, Integer.parseInt(eleField.getText()));
					else if (fType == short.class) field.setShort(eleObj, Short.parseShort(eleField.getText()));
					else if (fType == char.class) field.setChar(eleObj, eleField.getText().charAt(0));
					else if (fType == byte.class) field.setByte(eleObj, Byte.parseByte(eleField.getText()));
					else if (fType == boolean.class) field.setBoolean(eleObj, Boolean.parseBoolean(eleField.getText()));
					else {
						System.out.println("Cannot assign primitive to non primitive field.");
						return null;
					}
				}catch (IllegalAccessException e){
					System.out.printf("Could not modify field %s.%s %s", eleClass, field.getType(), field.getName());
					return null;
				}
			}
			// if not, recursively deserialize the next element and set the field to that when finished.
			else if (eleField.getName().equals("reference")){
				int reference = Integer.parseInt(eleField.getText());
				try {
					if (deserialized.containsKey(reference)) {
						field.set(eleObj, deserialized.get(reference));
					} else {
						field.set(eleObj, elementToObject(findElementByAttr(root, "id", Integer.toString(reference))));
					}
				} catch (IllegalAccessException e){
					System.out.println("Cannot assign primitive to non primitive field.");
					return null;
				}
			}
			else { //malformatted input
				XMLOutputter outputter = new XMLOutputter();
				System.out.println("Unexpected tag" + outputter.outputString(eleField));
				return null;
			}
		}

		return eleObj;

	}

	private Object elementToArray(Element element){
		String className = element.getAttributeValue("class");
		Matcher m = Inspector.getArrayMatcher(className);
		Object[] array = null;

		if (m.group(2).equals("L")) { //reference type array
			List<Element> children = element.getChildren();
			Class arrClass;
			//try to create the array.
			try {
				arrClass = Class.forName(className);
				array = (Object[]) arrClass.newInstance();
			} catch (ClassNotFoundException e) {
				System.out.println("Could not resolve array of type " + className);
				return null;
			} catch (IllegalAccessException e) {
				System.out.println("Not allowed to create array of type " + className);
				return null;
			} catch (InstantiationException e) {
				System.out.println("Error while creating array of type " + className);
				return null;
			}
			//iterate over each reference tag in the array object
			for (int i = 0; i < children.size(); i++){
				Element index = children.get(i);
				if (deserialized.containsKey(Integer.parseInt(index.getText()))){
					array[i] = deserialized.get(Integer.parseInt(index.getText()));
				}
				else {
					array [i] = elementToObject(findElementByAttr(root, "is", index.getText()));
				}
			}
			return array;
		}
		else {
			String type = m.group(2);
			if (type.equals("B")) return elementToByteArr(element);
			else if (type.equals("C")) return elementToCharArr(element);
			else if (type.equals("D")) return elementToDoubleArr(element);
			else if (type.equals("F")) return elementToFloatArr(element);
			else if (type.equals("I")) return elementToIntArr(element);
			else if (type.equals("J")) return elementToLongArr(element);
			else if (type.equals("S")) return elementToShortArr(element);
			else if (type.equals("Z")) return elementToBooleanArr(element);
			else {
				System.out.println("Invalid Array type code");
				return null;
			}
		}
	}

	private Element findElementByAttr(Element parent, String attr, String val){
		for (Element element: parent.getChildren()){
			if (element.getAttributeValue(attr).equals(val)){
				return element;
			}
		}
		return null;
	}

	private LinkedList<Field> getAllFields(Class c){
		//end of recursion, return Object's fields.
		if (c.equals(Object.class)){
			return new LinkedList<>(Arrays.asList(c.getDeclaredFields()));
		}
		//otherwise, append c's fields to all the fields of all superclasses
		else {
			LinkedList<Field> superFields = getAllFields(c.getSuperclass());
			superFields.addAll(Arrays.asList(c.getDeclaredFields()));
			return superFields;
		}
	}

	private double[] elementToDoubleArr(Element element){
		List<Element> children = element.getChildren();
		double[] ret = new double[Integer.parseInt(element.getAttributeValue("length"))];

		for (int i = 0; i < children.size(); i++){
			Element index = children.get(i);
			ret[i] = Double.parseDouble(index.getText());
		}

		return ret;
	}

	private float[] elementToFloatArr(Element element) {
		List<Element> children = element.getChildren();
		float[] ret = new float[Integer.parseInt(element.getAttributeValue("length"))];

		for (int i = 0; i < children.size(); i++) {
			Element index = children.get(i);
			ret[i] = Float.parseFloat(index.getText());
		}

		return ret;
	}

	private long[] elementToLongArr(Element element) {
		List<Element> children = element.getChildren();
		long[] ret = new long[Integer.parseInt(element.getAttributeValue("length"))];

		for (int i = 0; i < children.size(); i++) {
			Element index = children.get(i);
			ret[i] = Long.parseLong(index.getText());
		}

		return ret;
	}

	private int[] elementToIntArr(Element element) {
		List<Element> children = element.getChildren();
		int[] ret = new int[Integer.parseInt(element.getAttributeValue("length"))];

		for (int i = 0; i < children.size(); i++) {
			Element index = children.get(i);
			ret[i] = Integer.parseInt(index.getText());
		}

		return ret;
	}

	private short[] elementToShortArr(Element element) {
		List<Element> children = element.getChildren();
		short[] ret = new short[Integer.parseInt(element.getAttributeValue("length"))];

		for (int i = 0; i < children.size(); i++) {
			Element index = children.get(i);
			ret[i] = Short.parseShort(index.getText());
		}

		return ret;
	}

	private byte[] elementToByteArr(Element element) {
		List<Element> children = element.getChildren();
		byte[] ret = new byte[Integer.parseInt(element.getAttributeValue("length"))];

		for (int i = 0; i < children.size(); i++) {
			Element index = children.get(i);
			ret[i] = Byte.parseByte(index.getText());
		}

		return ret;
	}

	private boolean[] elementToBooleanArr(Element element) {
		List<Element> children = element.getChildren();
		boolean[] ret = new boolean[Integer.parseInt(element.getAttributeValue("length"))];

		for (int i = 0; i < children.size(); i++) {
			Element index = children.get(i);
			ret[i] = Boolean.parseBoolean(index.getText());
		}

		return ret;
	}

	private char[] elementToCharArr(Element element) {
		List<Element> children = element.getChildren();
		char[] ret = new char[Integer.parseInt(element.getAttributeValue("length"))];

		for (int i = 0; i < children.size(); i++){
			Element index = children.get(i);
			ret[i] = index.getText().charAt(0);
		}
		return ret;
	}
}
