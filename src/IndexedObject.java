import org.jdom2.Element;

public class IndexedObject {

	public Object obj;
	public int reference;

	public IndexedObject(Object obj, int reference){
		this.obj = obj;
		this.reference = reference;
	}
}
