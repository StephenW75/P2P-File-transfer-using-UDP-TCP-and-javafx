package TEST;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class DhtServerTester {
	
	private final static int TcpOutPort = 20040;
	private final static int TcpInPort = 20041;
	
	public static void main(String[] args){
		
		try {
			// connect remoteAddress, remotePort <-> localAddress, localPort
			Socket dhtSocket = new Socket("localhost", TcpInPort, InetAddress.getByName("localhost"), TcpOutPort);
			DataOutputStream outToNextDHTServer = new DataOutputStream(dhtSocket.getOutputStream());
			BufferedReader inFromNextDHTServer = new BufferedReader(new InputStreamReader(dhtSocket.getInputStream()));
			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				String message = br.readLine();
				if (message.contains("quit")) break;
				System.out.println("MESSAGE: " + message);
				outToNextDHTServer.writeBytes(message + '\n');
			}
			dhtSocket.close();
			
			
			
			
			
			
			
			
			
			
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
