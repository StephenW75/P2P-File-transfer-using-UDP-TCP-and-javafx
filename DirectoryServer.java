import java.io.*;
import java.util.*;
import java.net.*;


public class DirectoryServer {
	public static void main(String[] args) throws Exception{
		String itemName; 
		String targetIp;
		Hashtable<String, String> database = new Hashtable<String, String>();
		ServerSocket socket = new ServerSocket(6789); 
		while(true) {
			
			Socket connect = socket.accept(); 
			
			//Datastream from client via socket
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			
			//Datastream to client via socket
			DataOutputStream outToClient = new DataOutputStream(connect.getOutputStream());
			
			//read data retrieved from socket
			itemName = inFromClient.readLine(); 
			itemName = itemName.toLowerCase(); 
			
			//Get peer IP address from database
			targetIp = database.get(itemName); 
			
			//Response to client
			outToClient.writeBytes(targetIp); 
			
		}
		
	}
	
	//1. initialize database contents
	 public static Hashtable<String, String> init() {
		 Hashtable<String, String> result = new Hashtable<String, String>();
		 
		 
		 
		 
		 
		 
		 return result; 
		 
	 }
	//end 1. 
	
}
