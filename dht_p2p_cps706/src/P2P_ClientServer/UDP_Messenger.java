package P2P_ClientServer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDP_Messenger {

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
		socket.send(packet);
		packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		return new String(packet.getData(), 0, packet.getLength());
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
