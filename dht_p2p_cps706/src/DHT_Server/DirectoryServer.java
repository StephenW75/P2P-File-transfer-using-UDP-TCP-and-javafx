import java.io.*;
import java.util.*;
import java.net.*;


public class DirectoryServer{
	public static int id; 
	public String ip;
	public static String nextIp;
	public static void main(String[] args) throws Exception{
		for(int i = 0; i<args.length; i++) {
			if(args[i].equals("-id")) {
				id = Integer.parseInt(args[i+1]); 
			}
			else if(args[i].contentEquals("-nextip")) {
				nextIp = args[i+1]; 
			}
		}
		DHTListener socket1 = new DHTListener(id, nextIp);
		PeerListener socket2 = new PeerListener();
		Thread t1 = new Thread(socket1);
		Thread t2 = new Thread(socket2);
		t1.start();
		t2.start(); 
		
		
		
		
	}// end of main
	

	//Initialize database contents
	 public static Hashtable<String, String> init() {
		 Hashtable<String, String> result = new Hashtable<String, String>();
		 
		 
		 
		 
		 
		 
		 return result; 
		 
	 }//end of init
	
	
}//end of class


