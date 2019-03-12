import java.io.*;
import java.util.*;
import java.net.*;


public class DirectoryServer {
	public static void main(String[] args) throws Exception{
		String itemName; 
		String targetIp;
		Hashtable<String, String> database = new Hashtable<String, String>();
		DatagramSocket socket = new DatagramSocket(6789); 
		
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		
		
		while(true) {
			
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			
			socket.receive(receivePacket);
			
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
				socket.send(sendPacket); 
			}
			
			else if(command.toLowerCase().equals("init")) {
				String dhtAddresses = InetAddress.getLocalHost().getHostAddress()+"\n";
				//... 
			
			}
			
			else if(command.toLowerCase().equals("inform_update")) {
			
			
			}
			else {
			
			}
			
		}//end of while true
		
	}// end of main
	
	
	//Initialize database contents
	 public static Hashtable<String, String> init() {
		 Hashtable<String, String> result = new Hashtable<String, String>();
		 
		 
		 
		 
		 
		 
		 return result; 
		 
	 }//end of init
	
	
}//end of class
