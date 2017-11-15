import java.util.LinkedList;

public class ClassE extends ClassA{
	public LinkedList<ClassA> list;

	public ClassE(){
		list.add(new ClassA());
		list.add(new ClassA());
	}
}
