public class ClassC implements XMLSerializable {
	public int[] arr;

	public ClassC(int[] arr){
		this.arr = arr;
	}

	public ClassC(){
		arr = new int[] {0,1,2,3,4,5,6,7,8,9};
	}
}
