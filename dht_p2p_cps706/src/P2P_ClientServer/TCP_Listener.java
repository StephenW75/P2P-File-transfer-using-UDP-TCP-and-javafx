package P2P_ClientServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Multiple-threaded, non-blocking
public class TCP_Listener implements Runnable {

	ServerSocket serverSocket;
	Socket clientSocket;
	
	TCP_Listener(ServerSocket serverSocket) throws IOException {
		this.serverSocket = serverSocket;
	}

	@Override
	public void run() {
		
		ExecutorService executor = Executors.newCachedThreadPool();
		
		// TODO Auto-generated method stub
		while (true) {
			clientSocket = null;
			try {
				// Block here while waiting...
				clientSocket = serverSocket.accept();
			} catch (SocketException e){
				System.out.println(e.getMessage());
				return;
			} catch (IOException e) {
				System.out.println("Error while listening for TCP");
				return;
			}
			executor.submit(new TCP_ListenerWorker(clientSocket));
		}
	}
}

class TCP_ListenerWorker implements Runnable {
	
	Socket clientSocket;
	
	TCP_ListenerWorker(Socket clientSocket){
		this.clientSocket = clientSocket;
	}
	
	@Override
	public void run() {
		try {
			OutputStream out = clientSocket.getOutputStream();
			
			// Do stuff with input/output
			System.out.println(out.toString());
			
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
