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
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			
			DataOutputStream outToClient = new DataOutputStream(connect.getOutputStream());
			itemName = inFromClient.readLine(); 
			itemName = itemName.toLowerCase(); 
			targetIp = database.get(itemName); 
			outToClient.writeBytes(targetIp); 
			
		}
		
	}
	 public static Hashtable<String, String> init() {
		 Hashtable<String, String> result = new Hashtable<String, String>();
		 
		 
		 
		 
		 
		 
		 return result; 
		 
	 }
	
	
}
