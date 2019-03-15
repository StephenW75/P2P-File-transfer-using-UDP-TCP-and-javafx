package DHT_Server;
import java.io.IOException;
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
	
	
	public void run() {
		String itemName; 
		String targetIp;
		Hashtable<String, String> database = new Hashtable<String, String>();
		DatagramSocket socket = null;
		
		try {
			socket = new DatagramSocket(7024); 
		}
		catch(SocketException e) {
			e.printStackTrace();
		}
		finally {
			//this is always called
			//socket.close(); 
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
			String message = new String(receivePacket.getData());
			String command = message.substring(0,message.indexOf("\n")); 
			String messageData = message.substring(message.indexOf("\n"), message.length()); 
			
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
				sendData = ("messageData: " + messageData + "... was received").getBytes(); 
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				try {
					socket.send(sendPacket);
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			
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
