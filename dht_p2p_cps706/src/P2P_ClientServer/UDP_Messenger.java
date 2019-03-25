package P2P_ClientServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

// Blocking
public class UDP_Messenger{
	
	//Maximum message length
	private final int MAX_BUFFER = 1024;
	
	private DatagramSocket serverSocket;
	private InetAddress recipientIP;
	private int recipientPort;
	
	UDP_Messenger(DatagramSocket serverSocket, InetAddress recipientIP, int recipientPort) throws IOException {
		this.serverSocket = serverSocket;
		this.recipientIP = recipientIP;
		this.recipientPort = recipientPort;
	}
	
	// Once message is sent, wait for a reply
	public String sendMessage(String message) {
		byte[] buffer = message.getBytes();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, recipientIP, recipientPort);
		
		// Try to send message to DHT server
		try {
			serverSocket.send(packet);
		} catch (IOException e) {
			System.out.println("Could not send packet");
		}
		
		
		// Set max receive buffer size to MAX_BUFFER
		buffer = new byte[MAX_BUFFER];
		packet = new DatagramPacket(buffer, buffer.length);
		
		// Wait up to 5 seconds to get response for DHT server
		try {
			serverSocket.setSoTimeout(5000);
			serverSocket.receive(packet);
		} catch (SocketTimeoutException e) { 
			System.out.println("Could not receive packet, timed out after 5s");
		} catch (Exception e) {
			System.out.println("UDP ServerSocket: " + e.getMessage());
		}
		String reply = new String(packet.getData());
		String shortReply = reply.substring(0, packet.getLength());
		return shortReply;
	}
}
