package DHT_Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;

public class DirectoryServer{
	
	public static int id; 
	public String ip;
	public static String nextIp;
	
	// Main begin
	public static void main(String[] args){
		
		/*
		// Launch Arguments
		for(int i = 0; i<args.length; i++) {
			if(args[i].equals("-id")) {
				id = Integer.parseInt(args[i+1]); 
			}
			else if(args[i].contentEquals("-nextip")) {
				nextIp = args[i+1]; 
			}
		}
		*/
		
		// Start threads
		DHTListener dhtListener = new DHTListener(0, null);
		PeerListener pListener = new PeerListener(initDHTable(), dhtListener);
		
		Thread dhtListenerThread = new Thread(dhtListener);
		Thread pListenerThread = new Thread(pListener);
		
		dhtListenerThread.start();
		pListenerThread.start();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		while (true) {
			System.out.println("Enter \"quit\" to quit, or enter a command.");
			String message;
			try {
				message = br.readLine();
				if (message.contains("quit")) break;
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		
		// Safely close all Connections
		dhtListener.stop();

		
	}// Main end
	

	//Initialize database contents
	 public static Hashtable<String, String> initDHTable() {
		 Hashtable<String, String> result = new Hashtable<String, String>();
		 
		 // Testing
		 result.put("spiderman", "elsa");
		 
		 
		 
		 return result; 
		 
	 }//end of init
	
	
}//end of class


