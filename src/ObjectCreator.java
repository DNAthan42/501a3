import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

public class ObjectCreator {

	//Collects the created objects
	private LinkedList<Object> objList;

	public static void main(String[] args){
		System.out.println(Arrays.toString(new ObjectCreator().create()));
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
			System.out.println("Available Classes:");
			System.out.println("ClassA, ClassB, ClassC, ClassD, ClassE");
			System.out.print("Desired Class: ");
			classInput = in.next();
			System.out.println();

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

			try{
				objList.add(classType.newInstance());
				System.out.println("added a " + classType.toString());
				System.out.println("Done.");
			} catch (IllegalAccessException e){
				System.out.println("Could not access the constructor. No object created");

			} catch (InstantiationException e){
				System.out.println("Could not create the object.");
			}

			System.out.println("Create another object?");
			boolean validResponse = false;
			while (!validResponse) {
				System.out.print("(y/n): ");
				String response = in.next().toLowerCase();
				System.out.println();
				if (response.equals("y") || response.equals("ye") || response.equals("yes")) {
					moreObjects = true;
					validResponse = true;
				} else if (response.equals("n") || response.equals("no")) {
					moreObjects = false;
					validResponse = true;
				}
				else {
					System.out.println("Please, either \'yes\' or \'no\'");
				}
			}
		}


		return objList.toArray();
	}


}
