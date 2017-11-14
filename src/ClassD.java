public class ClassD implements XMLSerializable {
	public ClassA[] arr;

	public ClassD(ClassA[] arr){
		this.arr = arr;
	}

	public ClassD(){
		arr = new ClassA[10];
		for (int i = 0; i < arr.length; i++){
			arr[i] = new ClassA();
		}
	}
}
