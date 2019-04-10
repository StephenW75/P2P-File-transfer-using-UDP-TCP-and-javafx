package P2P_ClientServer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Manages multiple TCP connections using multi-threading
 */

public class TCP_Manager {

	private ServerSocket serverSocket;
	private ExecutorService tPool;
	File defaultDir;

	// Constructor
	TCP_Manager(int tcpPort, File defDir) throws IOException {
		defaultDir = defDir;
		serverSocket = new ServerSocket(tcpPort);
		// Create a thread pool for multi-threaded TCP handling
		tPool = Executors.newCachedThreadPool();
		// Start greeter (we only need 1)
		tPool.submit(new TCP_Greeter(serverSocket));
	}

	// Try to start a TCP connection with rIP:rPORT, returns a handle to new
	// TCP_Worker.
	TCP_Worker initHandShake(String rIP, int rPORT) {
		try {
			Socket clientSocket = new Socket(rIP, rPORT);
			System.out.println(
					"TCPHandShake: Connecting to " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
			TCP_Worker worker = new TCP_Worker(clientSocket, defaultDir);
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
	 * Non-blocking Handles incoming TCP hand-shakes
	 */
	class TCP_Greeter implements Runnable {

		private ServerSocket serverSocket;
		private Socket clientSocket;
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
					System.out.println("TCP_G: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
				} catch (Exception e) {
					System.out.println("TCP_G: " + e.getMessage());
					return;
				}
				// Send the TCP connection to a new thread to handle messages / commands
				tPool.submit(new TCP_Worker(clientSocket, defaultDir));
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

	// private ExecutorService txPool;
	private Socket clientSocket;
	private String threadID = Long.toString(Thread.currentThread().getId());
	private BufferedReader in;
	private DataOutputStream out;
	private File defDir;

	// Constructor
	TCP_Worker(Socket cSocket, File defDir) {
		this.defDir = defDir;
		clientSocket = cSocket;

		try {
			out = new DataOutputStream(cSocket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println("Erorr creating input/output streams " + e.getMessage());
			signalKill();
		}
	}

	@SuppressWarnings("resource")
	@Override
	public void run() {
		System.out.println(String.format("T%s: Starting TCP with: '%s:%s'", threadID, clientSocket.getInetAddress(),
				clientSocket.getPort()));
		try {
			// If either signals are on, break loop
			while (!isStopped()) {

				Thread.sleep(50);

				// Wait for some input
				if (in.ready()) {

					// read the command
					String message = in.readLine();
					System.out.println("T" + threadID + " Receiving: " + message);

					/*
					 * ============================================================
					 * ================== PROCESSESS COMMANDS HERE ================
					 * ============================================================
					 */

					if (message.toLowerCase().contains("get")) {

						// messageSplit[0] = get, messageSplit[1] = fileName, messageSplit[2] =
						// httpversion
						String[] messageSplit = message.split(" ");
						String fileName = messageSplit[1];

						out.writeBytes("INCFILE " + fileName + " HTTP/1.1\n");

						File toSend = new File(
								defDir.getAbsolutePath() + System.getProperty("file.separator") + fileName);

						byte[] mybytearray = new byte[(int) toSend.length()];
						FileInputStream fis = new FileInputStream(toSend);
						BufferedInputStream bis = new BufferedInputStream(fis);
						bis.read(mybytearray, 0, mybytearray.length);
						OutputStream os = clientSocket.getOutputStream();
						os.flush();
						System.out.println("Sending " + fileName + "(" + mybytearray.length + " bytes)");
						os.write(mybytearray, 0, mybytearray.length);
						os.flush();
						System.out.println("Done.");

						// File transfer done. Close the socket connection!
						bis.close();
						os.close();
						clientSocket.close();

					} else if (message.toLowerCase().contains("incfile")) {
						int bytesRead;
						int current = 0;
						FileOutputStream fos = null;
						BufferedOutputStream bos = null;

						// messageSplit[0] = incfile, messageSplit[1] = fileName, messageSplit[2] =
						// httpversion
						String[] messageSplit = message.split(" ");
						String fileName = messageSplit[1];

						// Initialize the FileOutputStream to the output file's full path.
						File toGet = new File(
								defDir.getAbsolutePath() + System.getProperty("file.separator") + fileName);
						fos = new FileOutputStream(toGet);
						
						
						
						// receive file
						byte[] mybytearray = new byte[100000000];
						InputStream is = clientSocket.getInputStream();
						bos = new BufferedOutputStream(fos);
						bos.flush();
						bytesRead = is.read(mybytearray, 0, mybytearray.length);
						current = bytesRead;

						do {
							bytesRead = is.read(mybytearray, current, (mybytearray.length - current));
							if (bytesRead >= 0)
								current += bytesRead;
						} while (bytesRead > -1);

						bos.write(mybytearray, 0, current);
						bos.flush();
						System.out.println("File " + fileName + " downloaded (" + current + " bytes read)");

						// Disconnect
						fos.close();
						bos.close();
						clientSocket.close();

					} else if (message.toLowerCase().contains("400 bad request")) {
						System.out.println("HTTP/1.1 400 Bad Request");
					} else {
						out.writeBytes("HTTP/1.1 400 Bad Request\n");
					}

				}
			}
			System.out.println("T" + threadID + ": signaled to stop");
			kill();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (InterruptedException e) {
			System.out.println("T" + threadID + ": sleep() interrupted");
		}
	}

	void get(String fileName) {
		sendRawMessage(String.format("GET %s HTTP/1.1\n", fileName));
	}

	void sendRawMessage(String message) {
		// txPool.submit(new TCP_WorkerTX());\
		System.out.println("T" + threadID + " Sending: " + message);
		try {
			out.writeBytes(message);
		} catch (IOException e) {
			System.out.println("T" + threadID + ": " + e.getMessage());
		}
	}

	// Kills all TCP_Worker Threads
	static void signalKillAll() {
		STOP_ALL_SIGNAL = true;
	}

	// Kill just this instance
	void signalKill() {
		STOP_SIGNAL = true;
	}

	void kill() {
		try {
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("T" + threadID + " client socket closed");
	}

	boolean isStopped() {
		return STOP_ALL_SIGNAL || STOP_SIGNAL;
	}
}