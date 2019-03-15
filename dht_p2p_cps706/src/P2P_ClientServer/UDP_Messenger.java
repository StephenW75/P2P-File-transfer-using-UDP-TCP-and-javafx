package P2P_ClientServer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDP_Messenger {

	private DatagramSocket socket;
	private InetAddress DHT_IP;
	
	private byte[] buf;
	
	UDP_Messenger() throws Exception {
		socket = new DatagramSocket();
		DHT_IP = InetAddress.getByName("localhost"); //Need IP of a DHT
	}
	
	void sendMessage(String command, String data) throws Exception {
		buf = (command + "\n" + data).getBytes();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, DHT_IP,6789);
		socket.send(packet);
	}
	
	String setDHT_IP(InetAddress newDHTip) {
		DHT_IP = newDHTip;
		return newDHTip.toString();
	}
	
	void close() {
		socket.close();
	}

}
