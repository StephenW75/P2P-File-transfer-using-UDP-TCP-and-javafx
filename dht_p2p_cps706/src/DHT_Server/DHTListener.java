import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.UnknownHostException;
import java.util.Hashtable;
import java.io.*;

public class DHTListener implements Runnable{
	private Thread t; 
	private String threadName;
	private ServerSocket socket;
	private int id;
	private String nextDHTServerIP; 
	
	public DHTListener(int id, String nextIp) {
		this.id = id; 
		this.nextDHTServerIP = nextIp; 
	}
	public DHTListener() {
		this.id = 1; 
	}
	
	public void run() {
		try {
			 socket = new ServerSocket(6789);
		}
		catch(IOException e) {
			System.out.println(e.getMessage());
		}
		String message = null; 
		String messageData; 
		String command; 
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		Socket nextDHTSocket = null;
		DataOutputStream outToNextDHTServer = null;
		BufferedReader inFromNextDHTServer = null; 
		String previousDHTID = null;
		
		
		
			do {
				try {
					nextDHTSocket = new Socket(nextDHTServerIP, 7024);
					outToNextDHTServer =new DataOutputStream(nextDHTSocket.getOutputStream());
					
					inFromNextDHTServer = new BufferedReader(new InputStreamReader(nextDHTSocket.getInputStream()));
					
				}
				catch(IOException e) {
					continue;
				}
			}while(false);//loop until a connection is initiated 

			
			
		
		while(true) {
			try {
				Socket previousDHTSocket = socket.accept();
				BufferedReader inFromPreviousDHT = new BufferedReader(new InputStreamReader(previousDHTSocket.getInputStream()));
				DataOutputStream outToPreviousDHT = new DataOutputStream(previousDHTSocket.getOutputStream());
				
				
				
				
				
				message = inFromPreviousDHT.readLine(); 
				previousDHTID = message.substring(0, message.indexOf('\n'));
				
				if(Integer.parseInt(previousDHTID)!=(id+1)%4) {
					continue;
				}
				else {
			
					message = message.substring(message.indexOf('\n')+1, message.length());
					command = message.substring(0, message.indexOf('\n')); 
					messageData = message.substring(message.indexOf('\n')+1, message.length());
				
					if(command.contentEquals("DHTinit")&&id!=1) {
						outToNextDHTServer.writeBytes("DHTinit\n"+messageData+InetAddress.getLocalHost().getHostAddress()+"\n"); 
					}//if DHTinit request
					else if(command.contentEquals("query")) {
						
						
						
						
					}
					else if(command.contentEquals("inform&update")) {
						
						
						
					}
					else if(command.contentEquals("exit")) {
						
						
						
						
					}
					
					
					
					
					
					
					
					
					
				}//end if message is from previous DHT
				outToNextDHTServer =new DataOutputStream(nextDHTSocket.getOutputStream());
				
				inFromNextDHTServer = new BufferedReader(new InputStreamReader(nextDHTSocket.getInputStream()));
			}// end try
			catch(IOException e) {
				System.out.println(e.getMessage()); 
			}// end catch
		
			
		}//listen for messages from DHT server
		
		
		
	}
	public void start() {
		System.out.println("Starting " +  threadName );
	      if (t == null) {
	         t = new Thread (this, threadName);
	         t.start ();
	      }	
		
	}
}
	

