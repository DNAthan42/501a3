import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.Scanner;

public class ObjectCreator {

	//Collects the created objects
	private LinkedList<Object> objList;

	public static void main(String[] args){
		new ObjectCreator().create();
	}

	public ObjectCreator(){
		objList = new LinkedList<>();
	}

	public Object[] create(){
		String classInput;
		String fieldVal;
		Class classType;
		Constructor[] constructors;
		Object thisObj;
		boolean moreObjects = true;
		Scanner in = new Scanner(System.in);

		System.out.println("Object Creator\n");
		while (moreObjects){
			//prompt for object type
			System.out.println("Availble Classes:");
			System.out.println("ClassA, ClassB, ClassC, ClassD");
			System.out.print("Desired Class: ");
			classInput = in.next();

			//try to get the class reflectively
			try {
				classType = Class.forName(classInput);
			} catch (ClassNotFoundException e){
				if (classInput.toUpperCase().equals("EXIT")){
					break;
				}
				else {
					System.out.println("Class not found.");
					System.out.println("Class must be one of: ClassA, ClassB, ClassC, ClassD");
					System.out.println("Enter exit to exit");
					continue;
				}
			}

			// get the constructors so we can find the arguments needed.
			constructors = classType.getConstructors();
			for (Constructor c: constructors){

			}
		}


		return objList.toArray();
	}


}
