package P2P_ClientServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Multiple-threaded, non-blocking
 * Handles multiple incoming TCP connections
 */
public class TCP_Listener implements Runnable {

	ServerSocket serverSocket;
	Socket clientSocket;
	
	TCP_Listener(ServerSocket serverSocket) throws IOException {
		this.serverSocket = serverSocket;
	}

	@Override
	public void run() {
		
		ExecutorService executor = Executors.newCachedThreadPool();
		
		while (true) {
			clientSocket = null;
			try {
				// Block here, wait for TCP connection / hand-shake
				clientSocket = serverSocket.accept();
				//hand-shake complete
			} catch (SocketException e){
				System.out.println("TCP ServerSocket: " + e.getMessage());
				TCP_ListenerWorker.shutdownALL();
				executor.shutdown();
				return;
			} catch (Exception e) {
				System.out.println("Error while listening for TCP");
				TCP_ListenerWorker.shutdownALL();
				executor.shutdown();
				return;
			}
			// Send the TCP connection to a new thread to handle messages / commands
			executor.submit(new TCP_ListenerWorker(clientSocket));
		}
	}
}

/*
 * Handles TCP messages / commands from a TCP connection
 */
class TCP_ListenerWorker implements Runnable {
	
	Socket clientSocket;
	String threadName = Thread.currentThread().getName();
	static boolean threadPoolKillSwitch = false;
	
	TCP_ListenerWorker(Socket clientSocket){
		this.clientSocket = clientSocket;
	}
	
	boolean isShutdown() {
		return threadPoolKillSwitch;
	}
	
	static void shutdownALL() {
		threadPoolKillSwitch = true;
	}
	@Override
	public void run() {
		System.out.println(String.format("Starting TCP connection on Thread: '%s' with: '%s:%s'", threadName, clientSocket.getInetAddress(), clientSocket.getPort()));
		try {
			BufferedReader in = new BufferedReader (new InputStreamReader(clientSocket.getInputStream()));
			
			// Loop until connection closed
			while (!isShutdown()) {
				//Sleep, so computer doesnt use 100% cpu core
				Thread.sleep(50);
				if (in.ready()) {
					System.out.println(in.readLine());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(String.format("Thread: %s closed", threadName));
		try {
			this.clientSocket.close();
		} catch (Exception e) {
			System.out.println("Error closing socket for thread: " + threadName);
		}
		
	}
}
