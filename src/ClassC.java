import java.util.Arrays;

public class ClassC implements XMLSerializable {
	public int[] arr;

	public ClassC(int[] arr){
		this.arr = arr;
	}

	public ClassC(){
		arr = new int[] {0,1,2,3,4,5,6,7,8,9};
	}

	@Override
	public boolean equals(Object obj) {
		ClassC other;
		if (obj.getClass().equals(this.getClass())) other = (ClassC) obj;
		else return false;
		return Arrays.equals(this.arr, other.arr);
	}
}
