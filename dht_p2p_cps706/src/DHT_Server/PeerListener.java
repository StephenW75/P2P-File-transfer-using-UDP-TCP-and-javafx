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
	private Hashtable<Integer, String> database;
	private DHT_Manager dhtManager;

	// Constructor
	PeerListener(Hashtable<Integer, String> referenceToDHT, DHT_Manager referenceToDHT_Manager) {
		database = referenceToDHT;
		dhtManager = referenceToDHT_Manager;
	}
	
	void replyToClient (byte[] message, InetAddress cIP, int cPort) {
		try {
			socket.send(new DatagramPacket(message, message.length, cIP, cPort));
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	/*
	 * =====================================================================
	 *                     COMMAND PROCESSING START HERE
	 * =====================================================================
	 */

	// Query
	byte[] query(int hashedKey) {
		
		/*
		 * TODO: Query ALL DHTs, currently only queries local DHT
		 */
		// Get IP address of peer with the component from database
		String targetIp = database.get(hashedKey);
		// Response to client
		byte[] response = (String.format("queryresponse\n%s\r\n", targetIp)).getBytes();
		return response;

	}
	
	// Init
	byte[] init() {
		byte[] response;
		String[] dhtIPs = dhtManager.getAllIPs();
		String ips = "";
		
		for (int i = 0; i < dhtIPs.length; ++i) {
			ips += dhtIPs[i] + ",";
		}
		response = ips.getBytes();
		return response;
	}

	// Inform&Update
	byte[] informUpdate(int hashedKey, InetAddress ip) {
		
		int destinationID = hashedKey%dhtManager.ringSize;
		String value = ip.toString();
		
		// If the hashedKey to appropriate database
		if (dhtManager.ID == destinationID) {
			database.put(hashedKey, value);
		} else {
			dhtManager.informUpdate(destinationID, hashedKey, value);
		}

		return String.format("DHT[%d]<-{%d:%s}\r\n",destinationID , hashedKey, ip).getBytes();
	}
	
	
	/*
	 * =====================================================================
	 *                     COMMAND PROCESSING END HERE
	 * =====================================================================
	 */

	public void run() {

		String message;
		String command;
		String messageData;

		// Now receiving packets from UDPInPort
		try {
			socket = new DatagramSocket(UDPInPort);
		}
		// Exit Peerlistener on exception
		catch (SocketException e) {
			e.printStackTrace();
			return;
		}

		// Keep handling incoming packets
		while (true) {

			byte[] receiveData = new byte[MAX_BUFFER];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

			// Get packet
			try {
				socket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Read data and interpret retrieved from socket
			message = new String(receivePacket.getData());
			command = message.substring(0, message.indexOf("\n"));
			messageData = message.substring(message.indexOf("\n") + 1, message.indexOf("\r\n"));

			// Receive client info
			InetAddress clientIP = receivePacket.getAddress();
			int clientPort = receivePacket.getPort();

			System.out.println("=== NEW MESSAGE ===");
			System.out.println(String.format("Command: %s\nMessage: %s\nFrom: %s:%s\n", command, messageData, clientIP,
					clientPort));
			
			byte[] response = null;;
			
			// Query command
			if (command.toLowerCase().equals("query")) {
				response = query(Integer.parseInt(messageData));
			}
			// Init command //message = String.format("query\n%s\r\n", key);
			else if (command.toLowerCase().equals("init")) {
				response = init();
			}
			// Inform and Update command
			else if (command.toLowerCase().equals("inform&update")) {
				int key = Integer.parseInt(messageData.substring(messageData.indexOf("Key=") + "Key=".length(),messageData.length()));
				response = informUpdate(key, receivePacket.getAddress());
			} else {
				response = "Command not recognized\r\n".getBytes();
			}
			
			replyToClient(response, clientIP, clientPort);

		} // end of while true
	}// end of run
}
