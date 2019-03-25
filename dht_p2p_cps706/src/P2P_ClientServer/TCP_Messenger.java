package P2P_ClientServer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/*
 * TCP handshaking and message sending
 * #########Not sure if this needs be multithreaded yet.
 */
public class TCP_Messenger{
	
	public Socket clientSocket;
	DataOutputStream out;

	// Creates a new TCP connection during construction
	TCP_Messenger(InetAddress p2pIpAddress, int clientPort) {
		try {
			// Client creates a socket & hand-shakes with p2p server
			this.clientSocket = new Socket(p2pIpAddress, clientPort);
			out = new DataOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			System.out.println("TCP_Messenger: Could not create output stream");
		}
	}
	
	// Pushes a string (as bytes) to the TCP connections output stream
	void sendMessage(String message) {		
		try {
			out.writeBytes(message+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	void sendFile() {
		
	}
}
