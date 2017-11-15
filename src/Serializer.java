import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;

public class Serializer {

	LinkedList<Element> references;
	int counter;

	public Serializer(){
		references = new LinkedList<>();
		counter = 0;
	}

	public Document serialize(Object obj){
		//Top level element
		Element root = new Element("serialized");

		//serialize the object passed, collecting references into references
		root.addContent(objectToElement(obj));

		//while there are referenced objects to serialize, serialize them
		//These objects may also reference objects, which will be added to the end of the list.
		while (!references.isEmpty()){
			root.addContent(objectToElement(references.remove()));
		}

		return new Document(root);
	}

	private Element objectToElement(Object obj){
		Element object = new Element("object");
		Class objClass = object.getClass();
		object.setAttribute("class", objClass.toString());
		object.setAttribute("id", Integer.toString(counter++));

		//get all the fields of the object, so they can be added
		for (Field f: getAllFields(objClass)){
			Element field = new Element("field");
			field.setAttribute("name", f.getName());
			field.setAttribute("declaringclass", f.getDeclaringClass().toString());

			//get the value of the field
			//if primitive, include the child element
			if (f.getType().isPrimitive()){
				Element value = new Element("value");
				f.setAccessible(true);
				try {
					value.addContent(new Text(f.get(obj).toString()));
				} catch (IllegalAccessException e){
					System.out.printf("field %s could not be accessed", f.getName());
					value.addContent(new Text("Error"));
				}
				field.addContent(value);
			}
			//otherwise, include the reference child and add the object to the queue
			else {
				Element reference = new Element("reference");
				f.setAccessible(true);
				try{
					reference.addContent
				}
			}
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
