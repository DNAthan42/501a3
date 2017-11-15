import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;
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

//	@Test
//	public void TestPrimitivesClass(){
//		Document doc =
//	}

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
