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
			
			String message = new String(receivePacket.getData());
			
			String command = message.substring(??,??); 
			
			InetAddress IPAddress = receivePacket.getAddress();

			int port = receivePacket.getPort();
			
			if(command.toLowerCase().equals("query")) {
	
				//Get peer IP address from database
				targetIp = database.get(itemName); 
			
				//Response to client
				endData = targetIp.getBytes(); 
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				socket.send(sendPacket); 
			}
			else if(command.toLowerCase().equals("init")) {
			
			
			
			}
			else if(command.toLowerCase().equals("inform_update")) {
			
			
			}
			else {
			
			}
			
			//read data retrieved from socket
			
		}
		
	}
	
	//1. initialize database contents
	 public static Hashtable<String, String> init() {
		 Hashtable<String, String> result = new Hashtable<String, String>();
		 
		 
		 
		 
		 
		 
		 return result; 
		 
	 }
	//end 1. 
	
}
