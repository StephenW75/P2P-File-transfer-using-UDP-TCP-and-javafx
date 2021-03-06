package P2P_ClientServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

// Blocking
public class UDP_Messenger {

	private InetAddress recipientIP = InetAddress.getByName("localhost");
	private int recipientPort = 20041;

	// Maximum message length
	private final int MAX_BUFFER = 1024;

	private DatagramSocket serverSocket;

	UDP_Messenger(int serverPort) throws IOException {
		serverSocket = new DatagramSocket(serverPort);
	}

	int getLocalPort() {
		return serverSocket.getLocalPort();
	}

	String getCurrentDHT() {
		return recipientIP + ":" + recipientPort;
	}

	void cleanUp() {
		serverSocket.close();
	}

	// Update UDP info
	boolean changeIp(String ip) {
		try {
			recipientIP = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	// Once message is sent, wait for a reply
	public String sendMessage(String message) {
		// Check if recipientIP is set yet
		if (recipientIP == null) {
			return null;
		}
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
			serverSocket.setSoTimeout(10000);
			serverSocket.receive(packet);
		} catch (SocketTimeoutException e) {
			System.out.println("Could not receive packet, timed out after 10s");
			return null;
		} catch (Exception e) {
			System.out.println("UDP ServerSocket: " + e.getMessage());
			return null;
		}
		String reply = new String(packet.getData());
		String shortReply = reply.substring(0, packet.getLength());
		return shortReply;
	}
}
