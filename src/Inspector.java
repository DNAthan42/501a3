import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Inspector Class
 * Introspectively inspects classes, their hierarchies and fields
 * @author Nathan Douglas
 */
public class Inspector {
	//todo UNIT TESTING

	public static final String[] ARRAY_TYPE_CODES = {"B", "C", "D", "F", "I", "J", "S", "Z"};
	public static final String[] ARRAY_TYPE_NAMES = {"byte", "char", "double", "float", "int", "long", "short", "boolean"};


	/**
	 * Required override method, forwards to more flexible 3 arg method
	 * @param obj The object to inspect
	 * @param recursive If true, will recursively inspect all non primitive fields
	 */
	public void inspect(Object obj, boolean recursive){
		inspect(obj, recursive, obj.getClass());
	}

	/**
	 * Main driver method for inspections
	 * @param obj the object to inspect
	 * @param recursive if true, will recursively Inspect all non primitive fields
	 * @param scope the Class in obj's hierarchy in which to inspect from. Returns immediately if scope is not in obj's hierarchy.
	 */
    public void inspect(Object obj, boolean recursive, Class scope){

    	Method[] methods;
    	Constructor[] constructors;
    	Field[] fields;

    	if (!scope.isInstance(obj) && !scope.isInterface()) {
    		System.out.println("FATAL: OBJECT SCOPE MISMATCH");
    		return;
		}
    	Class thisClass = scope;

		//If inspecting an array type, inspect each non primitive index
		if (thisClass.isArray()) {
			Matcher namePieces = getArrayMatcher(thisClass.getName());
			int arrayType = getArrayType(namePieces.group(2));
			if (arrayType < ARRAY_TYPE_CODES.length) return; // nothing to inspect in a primitive array.
			if (arrayType < 0) return; // couldn't parse array type. something went very wrong.
			Object[] array = (Object[]) obj; // otherwise, we have an array of objects, which we can inspect
			for (int i = 0; i < array.length; i++){
				System.out.printf("%s[%d]\n", namePieces.group(3), i);
				if (array[i] == null) System.out.println("null");
				else inspect(array[i], recursive);
			}
			return;
		}

		//Not an array, print header information for the object
        declaringClass(thisClass);
        superClass(thisClass);
        interfaces(thisClass);

        System.out.println();

        System.out.println("Method Summary");
        methods = thisClass.getDeclaredMethods();
        methods(methods);

        System.out.println();

        System.out.println("Constructor Summary");
        constructors = thisClass.getDeclaredConstructors();
        constructors(constructors);

        System.out.println();

        System.out.println("Field Summary");
        fields = thisClass.getDeclaredFields();
        fields(fields, obj, recursive);

        //Don't recur into interfaces if there are none
		if (thisClass.getInterfaces().length != 0){
			System.out.println("\n---------------Start Interface Recursion--------------");
			for (Class iClass: thisClass.getInterfaces()){
				System.out.println("\n-----------------Recurring Interface------------------");
				inspect(obj, recursive, iClass);
			}
			System.out.println("\n--------------End Interface Recursion-----------------");
		}
        // Don't try and recur into Object's parent class
        //if (!thisClass.equals(Object.class)
		if (thisClass.getSuperclass() != null) {

			System.out.println("\n-------------------Recurring Upwards------------------");
        	inspect(obj, recursive, thisClass.getSuperclass());
			System.out.println("\n-----------------End Upward Recursion-----------------");


		}
		//else System.out.println("\n-----------------End Upward Recursion-----------------");
    }

	/**
	 * Prints the formatted class name for the given class
	 * @param thisClass the class whose name should be printed
	 */
	private void declaringClass(Class thisClass){
    	System.out.print("class ");
    	if (thisClass.isArray()) System.out.println(arrayCodeToFormattedString(thisClass.getName()));
    	else System.out.println(thisClass.getName());
    }

	/**
	 * Prints the formatted superclass name for the given class
	 * @param thisClass the class whose super should be printed
	 */
	private void superClass(Class thisClass){ System.out.println("extends " + thisClass.getSuperclass()); }

	/**
	 * Prints the formatted interface name for the given class
	 * @param thisClass
	 */
    private void interfaces(Class thisClass){
    	boolean first = true; //string formatting is a pita
    	Class[] interfaces = thisClass.getInterfaces();
    	if (interfaces.length == 0) return; // no interfaces, thus nothing to print
    	System.out.print("implements ");
    	for (Class i: interfaces){
    		System.out.printf("%s%s", (first)?"":", ", i);
    		if (first) first = false;
		}
		System.out.println();
	}

	/**
	 * Returns the signature for a given methdo or constructor using reflection
	 * @param exec the method or constructor to get
	 * @return a Formatted string in the form of [Modifiers] [Name]([parameters])
	 */
	private String getSignature(Executable exec){
    	Parameter[] parameters = exec.getParameters();
    	String ret = "";

    	//get modifiers
    	ret += Modifier.toString(exec.getModifiers()) + " ";

    	//get name
		ret += exec.getName();

		//get params
		ret += "(";
		boolean first = true;
		String type;
		for (Parameter parameter: parameters){
			//check for arrays
			type = parameter.getType().getName();
			if (parameter.getType().isArray()){
				type = arrayCodeToFormattedString(type);
			}
			//don't precede the first param with a comma
			if (first) {
				ret += "";
				first = false;
			}
			else ret += ", ";
			//throw in the type and name
			ret += type + " " + parameter.getName();
		}
		ret += ")"; //close the method sig

		return ret;
	}

	/**
	 * Checks if the code is a string describing an array type in Java
	 * @param code the String to decode
	 * @return	Null if the string is not an array type
	 * 			a Matcher with three groups otherwise,
	 * 				Group 1: The open square brackets detailing the dimensions
	 * 				Group 2: The type code of the array
	 * 				Group 3: The fully qualified type name
	 */
	private Matcher getArrayMatcher(String code){
		if (code == null) return null;
		//checks 'code' against the regex. regex looks for array types of n>0 dimensions
		//array types occasionally have a semi colon at the end and IDK why
		//hence the ending being things that aren't colons followed by maybe some colons
		Matcher matcher = Pattern.compile("([\\[]+)([BCDFIJLSZ])([^;]*);*").matcher(code);

		if (!matcher.matches()) return null; //not a match then return null. probably shouldn't happen ever
		return matcher;
	}

	/**
	 * Given a string in ARRAY_TYPE_CODES, returns the index of the corresponding type in ARRAY_TYPE_NAMES
	 * @param type the string to check
	 * @return the index
	 */
	protected int getArrayType(String type){
		for (int i = 0; i < ARRAY_TYPE_CODES.length; i++){
			if (type.equals(ARRAY_TYPE_CODES[i])){
				return i;
			}
		}
		if (type.equals("L")) return ARRAY_TYPE_CODES.length;
		else return -1;
	}

	/**
	 * Rearranges java's internal array codes to java's developer facing notation
	 * @param code the array code to decrypt
	 * @return a more visually pleasing string
	 */
	protected String arrayCodeToFormattedString(String code){
    	String ret = "";
    	String type = "";
		Matcher matcher = getArrayMatcher(code);
		if (matcher == null) return "";//not a match then return the empty string. probably shouldn't happen ever

		type = matcher.group(2); //isolate just the type character
		int typeNum = getArrayType(type);
		if(typeNum < ARRAY_TYPE_CODES.length) ret = ret.concat(ARRAY_TYPE_NAMES[typeNum]);
		else if (typeNum == ARRAY_TYPE_CODES.length) ret = ret.concat(matcher.group(3));
		else return "";
		for (int i = 0; i < matcher.group(1).length(); i++){ //add in a pair of square brackets for each open square bracket found
			ret = ret.concat("[]");
		}
		return ret;
	}

	/**
	 * prints The signature, exceptions, and return types of all given methods
	 * @param methods the methods to parse
	 */
	private void methods(Method[] methods){
    	for (Method method: methods){

			System.out.print(getSignature(method));

			//print exceptions
			boolean first = true;
			if (method.getExceptionTypes().length != 0) System.out.print(" throws "); //if the method doesn't throw anything, don't write throws
			for (Class exception: method.getExceptionTypes()){ //print a list of all the exceptions
				System.out.printf("%s%s", (first)?"":", ", exception.getName());
				if (first) first = false;
			}

			//end the line
			System.out.println();

			//print the returns on a new line
			System.out.print("	returns ");
			if (method.getReturnType().isArray()) System.out.println(arrayCodeToFormattedString(method.getReturnType().getName()));
			else System.out.println(method.getReturnType().getName());
		}
	}

	/**
	 * Prints the signature, exceptions, and return types of all given methods
	 * @param constructors the constructors to parse
	 */
	private void constructors(Constructor[] constructors){
		for (Constructor constructor: constructors){
			System.out.println(getSignature(constructor));
		}
	}

	/**
	 * Prints the fields names and values, recurring in if recursive is true and the field is not primitive
	 * @param fields the fields to parse
	 * @param obj the object to get the fields of
	 * @param recursive if true, will inspect each non-primitive object
	 */
	private void fields(Field[] fields, Object obj, boolean recursive){
		String modifiers;
		HashSet<Field> toInspect = new HashSet<Field>(fields.length); //instantiating with the largest amount of fields we're likely to find.
		for (Field field: fields){

			modifiers = Modifier.toString(field.getModifiers());
			System.out.print((modifiers.equals(""))?"":modifiers + " ");

			if (field.getType().isArray()) System.out.print(arrayCodeToFormattedString(field.getType().getName()));
			else System.out.print(field.getType().getName());

			System.out.print(" ");
			System.out.print(field.getName());

			System.out.print(" = ");
			field.setAccessible(true);

			//check for primitive
			if (field.getType().isPrimitive()){
				try {
					System.out.print(field.get(obj));
				} catch (IllegalAccessException e) {
					//I literally just setAccessible(true). This shouldn't happen
					e.printStackTrace();
					System.exit(-Integer.parseInt("How", 36));
				}
			}

			else if (field.getType().isArray()){
				int arrayType = getArrayType(getArrayMatcher(field.getType().getName()).group(2));
				if (arrayType < ARRAY_TYPE_CODES.length){
					try {
						//I'm very not okay with how get does not wrap array types of primitives.
						if (arrayType == 0) System.out.print(Arrays.toString((byte[]) field.get(obj)));
						else if (arrayType == 1) System.out.print(Arrays.toString((char[]) field.get(obj)));
						else if (arrayType == 2) System.out.print(Arrays.toString((double[]) field.get(obj)));
						else if (arrayType == 3) System.out.print(Arrays.toString((float[]) field.get(obj)));
						else if (arrayType == 4) System.out.print(Arrays.toString((int[]) field.get(obj)));
						else if (arrayType == 5) System.out.print(Arrays.toString((long[]) field.get(obj)));
						else if (arrayType == 6) System.out.print(Arrays.toString((short[]) field.get(obj)));
						else System.out.print(Arrays.toString((boolean[]) field.get(obj)));
					}
					catch (IllegalAccessException e){
						System.out.println("couldn't access field " + field.getType().getName());
					}
				}
				else if (arrayType == ARRAY_TYPE_CODES.length){ //array of reference types/ object[]
					Object[] array = null;
					try {
						array = (Object[]) field.get(obj);
					} catch (IllegalAccessException e){
						System.out.println("couldn't access field " + field.getType().getName());
					}

					if (array == null) {
						System.out.println("null");
						continue;
					}

					//print each value inside square brackets because that's how Arrays.toString does it.
					System.out.print("[");
					boolean first = true;
					for (Object object: array){
						if (object != null) System.out.printf("%s%s:%s", (first)?"":", ", object.getClass().getName(), object.hashCode());
						else System.out.printf("%snull", (first)?"":", ");
						if (first) first = false;
					}
					System.out.print("]");

					toInspect.add(field); //mark field as a reference type to be inspected
				}
				else {
					System.out.println("Couldn't get type of array: " + field.getType().getName());
					System.exit(-1);
				}
			}

			else { //field must be a reference type
				Object fieldObj = null;
				try {
					fieldObj = field.get(obj);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				if (fieldObj != null) System.out.printf("%s:%s", fieldObj.getClass().getName(), fieldObj.hashCode());
				else System.out.print("null");
				toInspect.add(field); //mark field as a reference type to be inspected
			}

			System.out.println();
		}

		//inspect all of the non-primitive fields if recursive is set
		if (recursive && toInspect.size() != 0) {
			System.out.println("\n------------------Recurring Laterally-----------------");
			for (Field field: toInspect){
				System.out.println("-----start " + field.getName());
				field.setAccessible(true);
				try {
					Object fieldObj = field.get(obj);
					if (fieldObj == null) System.out.println("null");
					else inspect(field.get(obj), recursive);
					System.out.println("-----end " + field.getName());
				} catch (IllegalAccessException e) {
					//Shouldn't be reached
				}
			}
			System.out.println("\n-----------------End Lateral Recursion----------------");

		}
	}
}
