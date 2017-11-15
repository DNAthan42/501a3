import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.output.XMLOutputter;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedList;

public class Serializer {

	private LinkedList<IndexedObject> references;
	public IdentityHashMap<Object, Integer> serialized;
	private int counter;

	public Serializer(){
		references = new LinkedList<>();
		serialized = new IdentityHashMap<>();
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
			IndexedObject next = references.remove();
			if (!serialized.containsKey(next.obj))
				root.addContent(objectToElement(next));
		}

		return new Document(root);
	}

	public int getReference(Object obj){
		//If the object has already been serialized, return it's reference number
		if (serialized.containsKey(obj)){
			return serialized.get(obj);
		}
		//otherwise, mark this object for serialization, assign it a reference number, and return that.
		int ret = counter++;
		references.add(new IndexedObject(obj, ret));
		return ret;
	}

	private Element objectToElement(IndexedObject io){
		if (io.obj.getClass().isArray()) return arrayToElement(io);
		int id = io.reference;
		Object obj = io.obj;

		//mark this object as serialized
		serialized.put(obj, id);

		//create the top of the object tag
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
					int refNumber = getReference(f.get(obj)); //get the value of the field, and mark the value for serialization if it hasn't been seen yet
					reference.addContent(Integer.toString(refNumber));
				} catch (IllegalAccessException e) {
					System.out.printf("field %s could not be accessed", f.getName());
					reference.addContent(new Text("Error"));
				}
				field.addContent(reference);
			}
			object.addContent(field);
		}
		// return the now populated element.
		return object;
	}

	private Element arrayToElement(IndexedObject io){
		int id = io.reference;
		Object obj = io.obj;

		//mark this object as serialized
		serialized.put(obj, id);

		//create the top of the object tag
		Element array = new Element("object");
		Class arrClass = obj.getClass();
		array.setAttribute("class", arrClass.toString());
		array.setAttribute("id", Integer.toString(id));
		array.setAttribute("length", Integer.toString(Array.getLength(obj)));

		//iterate over everything in the array, so they can be added
		for (int i = 0; i < Array.getLength(obj); i++){
			Object thisObj = Array.get(obj, i);
			//if primitive, include the child element
			if (thisObj.getClass().isPrimitive() || isWrapper(thisObj.getClass())){
				Element value = new Element("value");
				value.addContent(new Text(thisObj.toString()));
				array.addContent(value);
			}
			//otherwise, include the reference child and add the object to the queue
			else {
				Element reference = new Element("reference");
				int refNumber = getReference(thisObj); //get the value of the field, and mark the value for serialization if it hasn't been seen yet
				reference.addContent(Integer.toString(refNumber));
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

	private boolean isWrapper(Class c){
		return (c == Double.class	|| c == Float.class 	|| c == Long.class ||
				c == Integer.class	|| c == Short.class 	|| c == Character.class ||
				c == Byte.class		|| c == Boolean.class);
	}
}
