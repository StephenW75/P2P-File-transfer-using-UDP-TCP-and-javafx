import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.UnknownHostException;
import java.util.Hashtable;

public class PeerListener implements Runnable {
	private Thread t; 
	private String threadName;
	private int id;
	
	public void run() {
		String itemName; 
		String targetIp;
		Hashtable<String, String> database = new Hashtable<String, String>();
		DatagramSocket socket = null;
		String nextDHTServerIp = "";
		Socket nextDHTSocket = null;
		DataOutputStream outToNextDHTServer = null;
		BufferedReader inFromNextDHTServer = null; 
		String dhtAddresses = ""; 
		String message = "";
		String command = "";
		String messageData = "";
		
		try {
			socket = new DatagramSocket(7024); 
		}
		catch(SocketException e) {
			e.printStackTrace();
		}
		finally {
			socket.close(); 
		}
		
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];

		
		while(true) {
			
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			
			try {
				socket.receive(receivePacket);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
			
			//Read data and interpret retrieved from socket
			message = new String(receivePacket.getData());
			command = message.substring(0,message.indexOf("\n")); 
			messageData = message.substring(message.indexOf("\n"), message.length()); 
			
			//Receive client info
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			
			//Determine action based on data from retrieved from socket
			if(command.toLowerCase().equals("query")) {
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
			}
			
			else if(command.toLowerCase().equals("init")) {
				do {
					try {
				
				
					dhtAddresses = InetAddress.getLocalHost().getHostAddress()+"\n";
					nextDHTSocket = new Socket(nextDHTServerIp, 6789);
					outToNextDHTServer =new DataOutputStream(nextDHTSocket.getOutputStream());
					inFromNextDHTServer = new BufferedReader(new InputStreamReader(nextDHTSocket.getInputStream()));
					
						
				}
				catch (java.net.UnknownHostException e) {
				      System.out.println("Unknown Host");
				}
				catch(IOException e) {
					continue;
				}
				
				}while(false); //loop until a connection is initiated 
				
				
				try {
					outToNextDHTServer.writeBytes(id+"\nDHTinit\n"+dhtAddresses);//send init request to next server
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			
				
				//Need to handle receiving of complete list of IPs
				
				
			
			}
			
			else if(command.toLowerCase().equals("inform_update")) {
			
			
			}
			else {
			
			}
			
		}//end of while true
	
	}// end of run
	public void start() {
		System.out.println("Starting " +  threadName );
	      if (t == null) {
	         t = new Thread (this, threadName);
	         t.start();
	      }	
	}
	
	
	
	
	
	
	
}
