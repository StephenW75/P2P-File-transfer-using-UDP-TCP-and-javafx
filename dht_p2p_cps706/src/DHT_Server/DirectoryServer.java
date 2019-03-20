package DHT_Server;
import java.util.*;


public class DirectoryServer{
	
	private static final int port = 7080;
	
	public static void main(String[] args) throws Exception{
		//DHTListener socket1 = new DHTListener(port);
		PeerListener pListener = new PeerListener(port);
		//Thread t1 = new Thread(socket1);
		Thread pThread = new Thread(pListener);
		//t1.start();
		pThread.start();
		
		System.out.println("Press any key to close server");
		System.in.read();
		
		//attempt to close sockets
		//socket1.socket.close();
		pListener.socket.close();
	}// end of main
	

	//Initialize database contents
	 public static Hashtable<String, String> init() {
		 Hashtable<String, String> result = new Hashtable<String, String>();
		 
		 
		 
		 
		 
		 
		 return result; 
		 
	 }//end of init
	
	
}//end of class


