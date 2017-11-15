import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;

import static org.junit.Assert.*;

import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;

public class TestSerializer {

	Serializer serializer;

	@Before
	public void init(){
		serializer = new Serializer();
	}

	@Test
	public void TestPrimitivesClass(){
		XMLOutputter outputter = new XMLOutputter();
		Document doc = serializer.serialize(new ClassA());
		Element root = doc.getRootElement();
		List<Element> objects = root.getChildren();
		for (Element element: objects){
			System.out.println(outputter.outputString(element));
		}
		//check there is only one object, ClassA
		assertEquals(1, objects.size());
		List<Element> values = objects.get(0).getChildren();
		for (int i = 0; i < values.size(); i++){
			Element field = values.get(i);
			Attribute fName = field.getAttribute("name");
			Attribute fClass = field.getAttribute("declaringclass");
			assertNotNull(fName);
			assertNotNull(fClass);
			Element value = field.getChild("value");
			assertNotNull(value);
			if (fName.getValue().equals("integer")) assertEquals(0, Integer.parseInt(value.getValue()));
			else if (fName.getValue().equals("dub")) assertEquals(0.0, Double.parseDouble(value.getValue()), 0.1);
			else if (fName.getValue().equals("maybe")) assertEquals(false, Boolean.parseBoolean(value.getValue()));
			else {
				System.out.println(fName);
				fail();
			}
		}
	}

	@Test
	public void TestReferencesClass(){
		Document doc = serializer.serialize(new ClassB());
		Element root = doc.getRootElement();
		List<Element> objects = root.getChildren();
		assertEquals(4, objects.size());
	}

	@Test
	public void TestPrimitiveArrayClass(){
		Document doc = serializer.serialize(new ClassC());
		Element root = doc.getRootElement();
		List<Element> children = root.getChildren();
		if (!(children.size() == 2)) fail();
	}

	@Test
	public void identityHashMap(){

		ClassA obj = new ClassA();
		int num = serializer.getReference(obj);
		serializer.serialized.put(obj, num);
		int num2 = serializer.getReference(obj);
		assertEquals(num, num2);
	}

	@Test
	public void TestUniqueReferenceArray(){
		XMLOutputter outputter = new XMLOutputter();
		//create an array of 10 ClassA objects with unique references and equal values.
		Document doc = serializer.serialize(new ClassD());
		Element root = doc.getRootElement();
		List<Element> children = root.getChildren();
		for (Element child: children){
			System.out.println(outputter.outputString(child));
		}

		//should be 12 objects, the classD passed in, the array, and each classA
		assertEquals(12, children.size());
	}

	@Test
	public void TestOneReferenceArray(){
		XMLOutputter outputter = new XMLOutputter();

		//create an array containing one classA object 10 times
		ClassA obj = new ClassA();
		ClassA[] arr = new ClassA[10];
		for (int i = 0; i < arr.length; i++){
			arr[i] = obj;
		}
		ClassD test = new ClassD(arr);
		Document doc = serializer.serialize(test);
		List<Element> children = doc.getRootElement().getChildren();

		for (Element child: children){
			System.out.println(outputter.outputString(child));
		}

		//shold be 3 objects, the classD, the array, and the classA
		assertEquals(3, children.size());
	}
}
