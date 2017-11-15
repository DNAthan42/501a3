public class ClassB implements XMLSerializable {
	public ClassA prims;
	public ClassC primArr;

	public ClassB(ClassA prims, ClassC primArr){
		this.prims = prims;
		this.primArr = primArr;
	}

	public ClassB(ClassA prims){
		this(prims, new ClassC());
	}

	public ClassB(ClassC primArr){
		this(new ClassA(), primArr);
	}

	public ClassB(){
		this(new ClassA(), new ClassC());
	}

	@Override
	public boolean equals(Object obj) {
		ClassB other;
		if (obj.getClass().equals(this.getClass())) other = (ClassB) obj;
		else return false;
		return this.primArr.equals(other.primArr) && this.prims.equals(other.prims);
	}
}
