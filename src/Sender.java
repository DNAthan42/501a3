import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Sender {

	public static void main(String[] args) {
		ObjectCreator oc = new ObjectCreator();
		Serializer serializer;
		Object[] objects = oc.create();
		System.out.println(objects.length);

		Document[] documents = new Document[objects.length];
		for (int i = 0; i < objects.length; i++){
			serializer = new Serializer();
			documents[i] = serializer.serialize(objects[i]);
		}

//		//Socket Code
//		Socket socket;
//		try {
//			socket = new Socket(args[0], Integer.parseInt(args[1]));
//		} catch (IOException e) {
//			System.out.printf("Could not connect to %s:%s", args[0], args[1]);
//			return;
//		}
//
//		PrintWriter out;
//		BufferedReader in;
//		try {
//			out = new PrintWriter(socket.getOutputStream());
//			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//		}catch (IOException e){
//			System.out.println("Could not establish reading and writing structs for socket");
//			return;
//		}
//
//		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
//		for (Document document: documents){
//
//		}

		//Socket code
		//pipe entire file to socket via outputter. Close and reopen socket to send diff files
		Socket socket;
		try {
			socket = new Socket(args[0], Integer.parseInt(args[1]));
			//send the number of documents
			System.out.println(documents.length);
			new DataOutputStream(socket.getOutputStream()).writeInt(documents.length);
			System.out.println("sent");
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		for (Document document: documents){
			try {
				socket = new Socket(args[0], Integer.parseInt(args[1]));
			} catch (IOException e) {
				System.out.printf("Could not connect to %s:%s", args[0], args[1]);
				return;
			}
			try {
				outputter.output(document, socket.getOutputStream());
			} catch (IOException e){
				System.out.println("Error while sending document");
				return;
			}
			try {
				socket.close();
			} catch (IOException e){
				return;
			}
		}
	}
}
