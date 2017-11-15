import org.jdom2.Document;
import org.jdom2.Element;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;

public class Deserializer {
	//private LinkedList<IndexedObject> references;
	public IdentityHashMap<Integer, Object> deserialized;

	public Deserializer(){
		//references = new LinkedList<>();
		deserialized = new IdentityHashMap<>();
	}

	public Object deserialize(Document document){
		Element root = document.getRootElement();
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
			//TODO array logic
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
			field.setAccessible(true);
			//get new value from doc
			element.get()
			//check if the new value of the field is a primitive or not.

		}


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
