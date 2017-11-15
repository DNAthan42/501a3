import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;

public class Serializer {

	LinkedList<IndexedObject> references;
	int counter;

	public Serializer(){
		references = new LinkedList<>();
		counter = 0;
	}

	public Document serialize(Object obj){
		//Top level element
		Element root = new Element("serialized");

		//serialize the object passed, collecting references into references
		references.add(new IndexedObject(obj, counter++));

		//while there are referenced objects to serialize, serialize them
		//These objects may also reference objects, which will be added to the end of the list.
		while (!references.isEmpty()){
			root.addContent(objectToElement(references.remove()));
		}

		return new Document(root);
	}

	private Element objectToElement(IndexedObject io){
		if (io.obj.getClass().isArray()) return arrayToElement(io);
		int id = io.reference;
		Object obj = io.obj;
		Element object = new Element("object");
		Class objClass = obj.getClass();
		object.setAttribute("class", objClass.toString());
		object.setAttribute("id", Integer.toString(id));

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
				try {
					int newId = counter++;
					references.add(new IndexedObject(f.get(obj), newId));
					reference.addContent(Integer.toString(id));
				} catch (IllegalAccessException e) {
					System.out.printf("field %s could not be accessed", f.getName());
					reference.addContent(new Text("Error"));
				}
				field.addContent(reference);
			}
		}
		// return the now populated element.
		return object;
	}

	private Element arrayToElement(IndexedObject io){
		int id = io.reference;
		Object[] obj = (Object[]) io.obj;
		Element array = new Element("object");
		Class arrClass = obj.getClass();
		array.setAttribute("class", arrClass.toString());
		array.setAttribute("id", Integer.toString(id));
		array.setAttribute("length", Integer.toString(Array.getLength(obj.length)));

		//iterate over everything in the array, so they can be added
		for (int i = 0; i < Array.getLength(obj); i++){
			Object thisObj = Array.get(obj, i);
			//if primitive, include the child element
			if (thisObj.getClass().isPrimitive()){
				Element value = new Element("value");
				value.addContent(new Text(thisObj.toString()));
				array.addContent(value);
			}
			//otherwise, include the reference child and add the object to the queue
			else {
				Element reference = new Element("reference");
				int newId = counter++;
				references.add(new IndexedObject(thisObj, newId));
				reference.addContent(Integer.toString(id));
				array.addContent(reference);
			}
		}
		//return the now populated element.
		return array;
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
