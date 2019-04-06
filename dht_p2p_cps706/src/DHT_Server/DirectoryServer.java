package DHT_Server;

import java.util.Hashtable;

public class DirectoryServer{
	
	private static int id;
	private static String nextIp;
	private static int size;
	
	// Main begin
	public static void main(String[] args){
		
		// Launch Arguments
		for(int i = 0; i<args.length; i++) {
			if(args[i].equals("-id")) {
				id = Integer.parseInt(args[i+1]); 
			}
			else if(args[i].contentEquals("-nextip")) {
				nextIp = args[i+1]; 
			}
			else if(args[i].contentEquals("-size")) {
				size = Integer.valueOf(args[i+1]); 
			}
		}
		
		System.out.println(String.format("Launching with ID:%d, nextIP:%s, RingSize:%d", id, nextIp, size));
		
		Hashtable<String, String> db =  initDHTable();
		//DHT_Manager dhtManager = new DHT_Manager(db, id, nextIp, size);
		DHT_Manager dhtManager = new DHT_Manager(db, id, nextIp, size);
		PeerListener pListener = new PeerListener(db, dhtManager);

		Thread pListenerThread = new Thread(pListener);
		pListenerThread.start();

	}// Main end
	

	//Initialize database contents
	 public static Hashtable<String, String> initDHTable() {
		 Hashtable<String, String> result = new Hashtable<String, String>();
		 
		 // Testing
		 result.put("spiderman", "elsa");
		 
		 
		 
		 return result; 
		 
	 }//end of init
	
	
}//end of class


