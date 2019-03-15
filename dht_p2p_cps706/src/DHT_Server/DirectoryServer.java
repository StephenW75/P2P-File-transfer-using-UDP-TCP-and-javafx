package DHT_Server;
import java.io.*;
import java.util.*;
import java.net.*;


public class DirectoryServer{
	public static void main(String[] args) throws Exception{
		//DHTListener socket1 = new DHTListener();
		PeerListener socket2 = new PeerListener();
		//Thread t1 = new Thread(socket1);
		Thread t2 = new Thread(socket2);
		//t1.start();
		t2.start(); 
		
		
		
		
	}// end of main
	

	//Initialize database contents
	 public static Hashtable<String, String> init() {
		 Hashtable<String, String> result = new Hashtable<String, String>();
		 
		 
		 
		 
		 
		 
		 return result; 
		 
	 }//end of init
	
	
}//end of class


