import com.sun.org.apache.xpath.internal.WhitespaceStrippingElementMatcher;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;

public class Deserializer {
	//private LinkedList<IndexedObject> references;
	public IdentityHashMap<Integer, Object> deserialized;
	private Element root;

	public Deserializer(){
		deserialized = new IdentityHashMap<>();
	}

	public Object deserialize(Document document){
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
			Element value = findElementByAttr(element, "name", field.getName());
			//check if the new value of the field is a primitive or not.
			if (value.getName().equals("value")){
				Class fType = field.getType();
				try {
					if (fType == double.class) field.setDouble(eleObj, new Double(value.getText()));
					else if (fType == float.class) field.setFloat(eleObj, new Float(value.getText()));
					else if (fType == long.class) field.setLong(eleObj, new Long(value.getText()));
					else if (fType == int.class) field.setInt(eleObj, new Integer(value.getText()));
					else if (fType == short.class) field.setShort(eleObj, new Short(value.getText()));
					else if (fType == char.class) field.setChar(eleObj, value.getText().charAt(0));
					else if (fType == byte.class) field.setByte(eleObj, new Byte(value.getText()));
					else if (fType == boolean.class) field.setBoolean(eleObj, new Boolean(value.getText()));
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
			else if (value.getName().equals("reference")){
				Class fType = field.getType();
				int reference = Integer.parseInt(value.getText());
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
				System.out.println("Unexpected tag" + outputter.outputString(value));
				return null;
			}
		}

		return eleObj;

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
}
