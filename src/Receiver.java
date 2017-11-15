import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Receiver {

	public static LinkedList<Object> objects = new LinkedList<>();

	public static void main(String[] args) {
		ServerSocket socket;
		int amount;
		try{
			socket = new ServerSocket(Integer.parseInt(args[0]));
			//initial connection
			Socket init = socket.accept();
			amount = init.getInputStream().read(new byte[4]); //4 being the max size of an int

		} catch (IOException e){
			System.out.println("Could not reserve socket on port " + args[0]);
			return;
		}

		//deserialize in threads
		Thread[] threads = new Thread[amount];
		for (int i = 0; i < amount; i++){
			Socket client;
			try{
				client = socket.accept();
				Thread t = new HandleClient(client);
				threads[i] = t;
				t.start();
			} catch (IOException e){
				System.out.println("Error while accepting socket");
				return;
			}
		}
		//let all threads finish before moving to visualization
		for (Thread t: threads){
			try{
				t.join();
			} catch (InterruptedException e){
				System.out.println(t.getName() + " interrupted");
				//continue.
			}
		}

		//visualize the objects one at a time.
		Inspector inspector = new Inspector();
		for (Object o: objects){
			inspector.inspect(o, false);
		}

	}
}

class HandleClient extends Thread {

	Socket client;
	Deserializer deserializer;

	public HandleClient(Socket client){
		this.client = client;
		deserializer = new Deserializer();
	}

	public void run(){
		SAXBuilder builder = new SAXBuilder();
		Document document;
		try{
			document = builder.build(client.getInputStream());
		} catch (IOException e){
			e.printStackTrace();
			return;
		} catch (JDOMException e) {
			e.printStackTrace();
			return;
		}

		//deserialize and queue the object for viewing
		Receiver.objects.add(deserializer.deserialize(document));

	}
}
