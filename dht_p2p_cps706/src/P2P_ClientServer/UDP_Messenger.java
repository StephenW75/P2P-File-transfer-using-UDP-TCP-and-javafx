package P2P_ClientServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

// Blocking
public class UDP_Messenger{
	
	private String recipientIP = "localhost";
	private int recipientPort = 7070;
	
	//Maximum message length
	private final int MAX_BUFFER = 1024;
	
	private DatagramSocket serverSocket;
	
	UDP_Messenger(int serverPort) throws IOException {
		serverSocket = new DatagramSocket(serverPort);
	}
	
	int getLocalPort() {
		return serverSocket.getLocalPort();
	}
	
	String getCurrentDHTLoc() {
		return recipientIP + ":" + recipientPort;
	}
	
	void cleanUp(){
		serverSocket.close();
	}
	
	void updateDHTinfo(String ip, int port) {
		recipientIP = ip;
		recipientPort = port;
	}
	
	// Once message is sent, wait for a reply
	public String sendMessage(String message) throws UnknownHostException {
		byte[] buffer = message.getBytes();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(recipientIP), recipientPort);
		
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
