package DHT_Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Hashtable;

// Listens for a connection on UDP
public class PeerListener implements Runnable {
	
	private DatagramSocket socket;
	private final int UDPInPort = 20041;
	private final int MAX_BUFFER = 1024;
	private Hashtable<String, String> database;
	private DHT_Manager dhtManager;

	// Constructor
	PeerListener(Hashtable<String, String> referenceToDHT, DHT_Manager referenceToDHT_Manager) {
		database = referenceToDHT;
		dhtManager = referenceToDHT_Manager;
	}
	
	/*
	 * =====================================================================
	 *                        COMMAND PROCESSING HERE
	 * =====================================================================
	 */
	
	// Query
	byte[] query(String itemName) {
		// Get IP address of peer with the component from database
		String targetIp = database.get(itemName); 
		// Response to client
		byte[] response = (String.format("queryresponse\n%s\r\n", targetIp)).getBytes(); 
		return response;
		
	}
	
	// Inform&Update //message = String.format("inform&update\nFileName=%s\r\n", fileName)
	void informUpdate(String messageData, InetAddress ip) {
		// TODO: 
		String fileName = messageData.substring(messageData.indexOf("FileName=") + "FileName=".length(), messageData.length());
		String value = ip.toString();
		database.put(fileName, value);
	}
	
	
	public void run() {
		System.out.println("PeerListener Started.");
		
		String message;
		String command;
		String messageData;
		
		// Now receiving packets from UDPInPort
		try {
			socket = new DatagramSocket(UDPInPort); 
		}
		// Exit Peerlistener on exception
		catch(SocketException e) {
			e.printStackTrace();
			return;
		}
		
		// Keep handling incoming packets
		while(true) {
			
			byte[] receiveData = new byte[MAX_BUFFER];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			
			// Get packet
			try {
				System.out.println("Waiting for incoming packet...");
				socket.receive(receivePacket);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
			
			// Read data and interpret retrieved from socket
			message = new String(receivePacket.getData());
			command = message.substring(0,message.indexOf("\n")); 
			messageData = message.substring(message.indexOf("\n") + 1, message.indexOf("\r\n")); 
			
			// Receive client info
			InetAddress clientIP = receivePacket.getAddress();
			int clientPort = receivePacket.getPort();
			
			System.out.println("=== NEW MESSAGE ===");
			System.out.println(String.format("Command: %s\nMessage: %s\nFrom: %s:%s\n", command, messageData, clientIP, clientPort));
			
			// Query command
			if(command.toLowerCase().equals("query")) {
				byte[] response = query(messageData);
				try {
					socket.send(new DatagramPacket(response, response.length, clientIP, clientPort));
				} catch (IOException e) {
					System.out.println("p2pQuery: " + e.getMessage());
				}
			}
			// Init command  //message = String.format("query\n%s\r\n", key);
			else if(command.toLowerCase().equals("init")) {
				//
			}
			// Inform and Update command
			else if(command.toLowerCase().equals("inform&update")) {
				informUpdate(messageData, receivePacket.getAddress());
				try {
					byte[] response = String.format("db<-[%s:%s]\r\n", messageData, receivePacket.getAddress()).getBytes();
					socket.send(new DatagramPacket(response, response.length, clientIP, clientPort));
				} catch (IOException e) {
					System.out.println("p2pInformUpdate: " + e.getMessage());
				}
			}
			
			
		}//end of while true
	}// end of run
}
