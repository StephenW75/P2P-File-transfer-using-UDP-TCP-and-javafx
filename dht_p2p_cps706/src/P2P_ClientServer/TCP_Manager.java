package P2P_ClientServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Manages multiple TCP connections using multi-threading
 */

public class TCP_Manager{
	
	private ServerSocket serverSocket;
	private ExecutorService tPool;

	// Constructor
	TCP_Manager(int tcpPort) throws IOException {
		serverSocket = new ServerSocket(tcpPort);
		// Create a thread pool for multi-threaded TCP handling
		tPool = Executors.newCachedThreadPool();
		// Start greeter (we only need 1)
		tPool.submit(new TCP_Greeter(serverSocket));
	}
	
	// Try to start a TCP connection with rIP:rPORT, returns a handle to new TCP_Worker.
	TCP_Worker initHandShake(String rIP, int rPORT) {
		try {
			Socket clientSocket = new Socket(rIP, rPORT);
			System.out.println("TCPHandShake: Connecting to " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
			DataOutputStream outStream = new DataOutputStream(clientSocket.getOutputStream());
			TCP_Worker worker = new TCP_Worker(clientSocket, outStream);
			tPool.submit(worker);
			return worker;
		} catch (IOException e) {
			System.out.println("TCPHandShake: " + e.getMessage());
			return null;
		}
	}

	int getLocalPort() {
		return serverSocket.getLocalPort();
	}
	
	// Cleans up for a safe shutdown
	void cleanUp() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			System.out.println("Error cleaning up TCP: " + e.getMessage());
		}
		TCP_Worker.signalKillAll();
		tPool.shutdown();
		System.out.println("Successfully cleaned up TCP");
	}
	
	
	/*
	 * Non-blocking
	 * Handles incoming TCP hand-shakes
	 */
	class TCP_Greeter implements Runnable {

		private ServerSocket serverSocket;
		private Socket clientSocket;
		private DataOutputStream outStream;
		private volatile boolean STOP_SIGNAL;
		
		TCP_Greeter(ServerSocket sSocket) {
			STOP_SIGNAL = false;
			serverSocket = sSocket;
		}
		
		boolean isStopped() {
			return STOP_SIGNAL;
		}

		@Override
		public void run() {
			System.out.println("TCP_G: Started listening");
			// Run forever*
			while (!isStopped()) {
				try {
					// Block here, wait for TCP connection / hand-shake
					System.out.println("TCP_G: Waiting for connection");
					clientSocket = serverSocket.accept();
					outStream = new DataOutputStream(clientSocket.getOutputStream());
					System.out.println("TCP_G: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
				} catch (Exception e) {
					System.out.println("TCP_G: " + e.getMessage());
					return;
				}
				// Send the TCP connection to a new thread to handle messages / commands
				tPool.submit(new TCP_Worker(clientSocket, outStream));
			}
		}
	}
}

/*
 * Thread which handles TCP messages / commands from a(one) TCP connection
 */
class TCP_Worker implements Runnable {
	
	// Initially set kill signals to false
	private volatile static boolean STOP_ALL_SIGNAL = false;
	private volatile boolean STOP_SIGNAL = false;
	
	//private ExecutorService txPool;
	private Socket clientSocket;
	private String threadID = Long.toString(Thread.currentThread().getId());
	private BufferedReader in;
	private DataOutputStream out;
	
	// Constructor
	TCP_Worker(Socket cSocket, DataOutputStream outStream){
		out = outStream;
		clientSocket = cSocket;
		//txPool = Executors.newSingleThreadExecutor();
		
		try {
			in = new BufferedReader (new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println("T" + threadID + " Could not read from socket: " + e.getMessage());
		}
	}
	
	@Override
	public void run() {
		System.out.println(String.format("T%s: Starting TCP with: '%s:%s'", threadID, clientSocket.getInetAddress(), clientSocket.getPort()));
		try {
			// If either signals are on, break loop
			while (!isStopped()) {
				// Thread.sleep() allows pc to chillout, was hogging too many resources
				Thread.sleep(50);
				if(in.ready()) {
					
					
					
					
					
					// TODO: Process Data here
					String message = in.readLine();
					System.out.println("T"+threadID + " Receiving: " + message);
					
					
					
					
					
					
				}
			}
			System.out.println("T"+threadID + ": signaled to stop");
			kill();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (InterruptedException e) {
			System.out.println("T"+threadID + ": sleep() interrupted");
		}
	}
	
	void sendRawMessage(String message) {
		//txPool.submit(new TCP_WorkerTX());\
		System.out.println("T"+threadID + " Sending: " + message);
		try {
			out.writeBytes(message);
		} catch (IOException e) {
			System.out.println("T"+threadID + ": " + e.getMessage());
		}
	}
	
	// Kills all TCP_Worker Threads
	static void signalKillAll () {
		STOP_ALL_SIGNAL = true;
	}
	// Kill just this instance
	void signalKill() {
		STOP_SIGNAL = true;
	}
	void kill(){
		try {
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("T" + threadID + " client socket closed");
	}
	boolean isStopped() {
		return STOP_ALL_SIGNAL || STOP_SIGNAL;
	}	
}	