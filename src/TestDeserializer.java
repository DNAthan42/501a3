import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestDeserializer {

	Serializer serializer;
	Deserializer deserializer;

	@Before
	public void init(){
		serializer = new Serializer();
		deserializer = new Deserializer();
	}

	@Test
	public void TestOnlyPrimitiveClass(){
		ClassA obj = new ClassA();
		assertEquals(deserializer.deserialize(serializer.serialize(obj)), obj);
	}
}
