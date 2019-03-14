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
		while(true) {
			try {
				Socket connectionSocket = socket.accept();
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			
				message = inFromClient.readLine(); 
			}
			catch(IOException e) {
				System.out.println(e.getMessage()); 
			}
			command = message.substring(0, message.indexOf('\n'));
			messageData = message.substring(message.indexOf('\n'), message.length()); 
			
			//Enter in actions taken based on message received
			//
			//
			//
			
		}
		
		
		
	}
	public void start() {
		System.out.println("Starting " +  threadName );
	      if (t == null) {
	         t = new Thread (this, threadName);
	         t.start ();
	      }	
		
	}
}
	

