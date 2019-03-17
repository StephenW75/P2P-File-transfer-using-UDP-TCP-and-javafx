package P2P_ClientServer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDP_Messenger {
	
	private final int MAX_BUFFER = 1024;
	
	private DatagramSocket socket;
	private InetAddress recipientIP;
	private int recipientPort;
	
	private byte[] buf;
	
	UDP_Messenger(int recipientPort) throws Exception {
		socket = new DatagramSocket(9023);
		recipientIP = InetAddress.getByName("localhost"); //Need IP of a DHT
		this.recipientPort = recipientPort;
	}
	
	// Once message is sent, wait for a reply
	String sendMessage(String msg) throws Exception {
		buf = msg.getBytes();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, recipientIP, recipientPort);
		System.out.println("Length of Packet Sent: " + packet.getLength());
		socket.send(packet);
		// Wait for response
		buf = new byte[MAX_BUFFER];
		packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		String reply = new String(packet.getData());
		System.out.println("Length of Packet Recv: " + packet.getLength());
		return reply;
	}
	
	String setRecipientIP(InetAddress newIP) {
		recipientIP = newIP;
		return newIP.toString();
	}
	
	int getPort() {
		return socket.getLocalPort();
	}
	
	void close() {
		socket.close();
	}

}
