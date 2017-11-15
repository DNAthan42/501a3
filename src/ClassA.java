public class ClassA implements XMLSerializable {
	public int integer;
	public double dub;
	public boolean maybe;

	public ClassA(int integer, double dub, boolean maybe){
		this.integer = integer;
		this.dub = dub;
		this.maybe=  maybe;
	}

	public ClassA(){
		integer = 0;
		dub = 0.0;
		maybe = false;
	}

	@Override
	public boolean equals(Object obj) {
		ClassA other;
		if (obj.getClass().equals(this.getClass())){
			other = (ClassA) obj;
		}
		else return false;

		return integer == other.integer && dub == other.dub && maybe == other.maybe;
	}
}
