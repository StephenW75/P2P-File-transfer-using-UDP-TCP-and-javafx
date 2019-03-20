package DHT_Server;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Hashtable;

public class PeerListener implements Runnable {
	
	private final int MAX_BUFFER = 1024;
	
	private Thread t; 
	private String threadName;
	
	// public socket allows us to socket.close() while run() is blocked
	public DatagramSocket socket;
	private DatagramPacket receivePacket;
	private byte[] receiveData;
	private byte[] sendData;
	
	// Constructor
	PeerListener(int port) {
		try {
			socket = new DatagramSocket(port); 
		}
		catch(SocketException e) {
			e.printStackTrace();
		}
		receiveData = new byte[MAX_BUFFER];
		receivePacket = new DatagramPacket(receiveData, receiveData.length);
	}
	
	
	
	public void run() {
		
		String itemName; 
		String targetIp;
		Hashtable<String, String> database = new Hashtable<String, String>();
		
		//Filling database with fake data for now
		database.put("a", "apple");
		database.put("b", "banana");
		database.put("c", "corn");
		
		// Loop until exception!
		while (true) {
			
			// Wait for packet to be received (socket.close() can be called from main)
			try {
				socket.receive(receivePacket);
			// socket.close() from main will throw here
			} catch (Exception e) {
				System.out.println(e.getMessage());
				break;
			}
			
			//Read data and interpret retrieved from socket
			String message = new String(receivePacket.getData());
			String command = message.substring(0,message.indexOf("\n")); 
			//receivePacket.getLength() instead of message.length() because message is too long (1024).
			String messageData = message.substring(message.indexOf("\n") + 1, receivePacket.getLength()); 
			
			//Receive client info
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			
			//Determine action based on data from retrieved from socket
			if (command.toLowerCase().equals("query")) {
				//Message header is query request, so rest of message is the name of component requested
				itemName = messageData; 
				//Get IP address of peer with the component from database
				targetIp = database.get(itemName); 
			
				//Response to client
				sendData = ("queryresponse\n"+targetIp).getBytes(); 
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				try {
					socket.send(sendPacket);
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			} else if (command.toLowerCase().equals("init")) {
				try {
				
					String dhtAddresses = InetAddress.getLocalHost().getHostAddress()+"\n";
				}
				catch (java.net.UnknownHostException e) {
				      System.out.println("Unknown Host");
				}
				
				
				//... 
			
			}
			
			else if(command.toLowerCase().equals("inform_update")) {
			
			
			}
			else {
				//Testing for Response
				String resp = String.format("UNKNOWN HEADER: %s\n%s", command, messageData);
				sendData = resp.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				System.out.println("Length of Packet S: " + sendPacket.getLength());
				try {
					socket.send(sendPacket);
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}//end of while true
		socket.close();
	}// end of run
	
	
	public void start() {
		System.out.println("Starting " +  threadName );
	      if (t == null) {
	         t = new Thread (this, threadName);
	         t.start();
	      }	
	}
}
