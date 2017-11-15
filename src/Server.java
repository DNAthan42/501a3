import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Server {

	public static void main(String[] args) {
		ObjectCreator oc = new ObjectCreator();
		Serializer serializer;
		Object[] objects = oc.create();

		Document[] documents = new Document[objects.length];
		for (int i = 0; i < objects.length; i++){
			serializer = new Serializer();
			documents[i] = serializer.serialize(objects[i]);
		}

		//Socket Code
		Socket socket;
		try {
			socket = new Socket(args[0], Integer.parseInt(args[1]));
		} catch (IOException e) {
			System.out.printf("Could not connect to %s:%s", args[0], args[1]);
			return;
		}

		PrintWriter out;
		BufferedReader in;
		try {
			out = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}catch (IOException e){
			System.out.println("Could not establish reading and writing structs for socket");
			return;
		}

		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		for (Document document: documents){
		}

	}
}
