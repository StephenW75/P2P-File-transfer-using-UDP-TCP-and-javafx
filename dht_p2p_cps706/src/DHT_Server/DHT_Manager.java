package DHT_Server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Handles DHT server ring
public class DHT_Manager {

	private final int TcpOutPort = 20040;
	private final int TcpInPort = 20041;

	private boolean STOP_SIGNAL = false;

	private NextDhtConnection nextDhtCon;
	private PrevDhtConnection prevDhtCon;
	private ExecutorService ex = Executors.newCachedThreadPool();

	private int ID;
	Hashtable<String, String> database;

	// id: ID of this server, nextIP: IP of next server, dhtRingSize: number of dht
	// servers
	public DHT_Manager(Hashtable<String, String> referenceToDHT, int id, String nextIp, int dhtSize) {
		database = referenceToDHT;
		ID = id;

		nextDhtCon = new NextDhtConnection(nextIp);
		prevDhtCon = new PrevDhtConnection(dhtSize);
		ex.submit(nextDhtCon);
		ex.submit(prevDhtCon);
	}

	// Safely stops thread
	public void stop() {
		STOP_SIGNAL = true;
		ex.shutdown();
	}

	private boolean isStopped() {
		return STOP_SIGNAL;
	}

	// Thread that deals with NextDhtServer
	private class NextDhtConnection implements Runnable {

		private String nextIP;
		private Socket nextDhtSocket;

		DataOutputStream outToNextDHT;
		BufferedReader inFromNextDHT;

		NextDhtConnection(String nextip) {
			nextIP = nextip;
		}

		String sendMessage(String s) {
			if (nextDhtSocket == null) {
				return null;
			} else {
				try {
					outToNextDHT.writeBytes(String.format("%s\r\n", s));
					return inFromNextDHT.readLine();
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}
		}

		@Override
		public void run() {

			while (!isStopped()) {
				// Connect to nextDht
				connect();
				// If connection is authenticated by server, return handle to socket
				if (authenticatedByID(ID)) {

					/*
					 * (probably will never need to get commands from nextDHT)
					 * =======================================
					 * RECEIVING COMMANDS FROM NEXT DHT SERVER
					 * =======================================
					 */

					while (!isStopped()) {
						try {
							Thread.sleep(500);
							// Get Command, Blocks here until line availible in buffer or connection is
							// closed
							String command = inFromNextDHT.readLine();

							if (command.toLowerCase().equals("hello")) {
								outToNextDHT.writeBytes("world");
							}
						} catch (IOException e) {
							System.out.println("NextListener: " + e);
						} catch (InterruptedException e) {
							System.out.println(e);
						}
					}
				}// End of authenticatedByID()
			}
		}// End of run()
		
		private void connect() {
			// Connects to: remoteAddress, remotePort <-> localAddress, localPort
			try {
				nextDhtSocket.setSoTimeout(2000);
				nextDhtSocket = new Socket(nextIP, TcpInPort, InetAddress.getByName("localhost"), TcpOutPort);
				outToNextDHT = new DataOutputStream(nextDhtSocket.getOutputStream());
				inFromNextDHT = new BufferedReader(new InputStreamReader(nextDhtSocket.getInputStream()));
			} catch (SocketException e) {
				System.out.println(e.getMessage());
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}

		private boolean authenticatedByID(int id) {
			// On connect, immediately send ID for simple authentication
			try {
				outToNextDHT.writeBytes(String.format("%d\n", id));

				if (inFromNextDHT.readLine().contains("CONNECTION OK")) {
					return true;
				} else {
					return false;
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
				return false;
			}
		}
	}// End of class NextDhtConnection

	// Thread that deals with PrevDhtServer
	private class PrevDhtConnection implements Runnable {

		ServerSocket serverSocket;
		Socket prevDHTSocket;
		int ringSize;

		BufferedReader inFromPrevDHT;
		DataOutputStream outToPrevDHT;

		PrevDhtConnection(int rSize) {
			ringSize = rSize;
		}

		String sendMessage(String s) {
			if (prevDHTSocket == null) {
				return null;
			} else {
				try {
					outToPrevDHT.writeBytes(String.format("%s\r\n", s));
					return inFromPrevDHT.readLine();
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}
		}

		@Override
		public void run() {

			// Wait for connection
			while (!isStopped()) {
				connect();
				try {

					if (authenticateID()) {
						/*
						 * ===========================================
						 * RECEIVING COMMANDS FROM PREVIOUS DHT SERVER
						 * ===========================================
						 */

						System.out.println("prevDHT Accepted, listening to commands..");

						// If ID is valid, start parsing for commands
						while (!isStopped()) {

							// Get Command, Blocks here until line availible in buffer or until connection
							// is closed
							String command = inFromPrevDHT.readLine();

							if (command.toLowerCase().equals("getallip")) {
								System.out.println("DHTinit runs here");
								// outToNextDHTServer.writeBytes("DHTinit\n"+messageData+InetAddress.getLocalHost().getHostAddress()+"\n");
							} // if DHTinit request

							else if (command.toLowerCase().equals("query")) {
								System.out.println("query runs here");
								/*
								 * TODO: If DHT contains query item return IP of client with said item to
								 * prev.DHTserver [] Else ask next.DHTserver to query item []
								 * 
								 */
							} else if (command.toLowerCase().equals("exit")) {
								System.out.println("exit runs here");
								/*
								 * TODO: Remove all items with of specified IP in this.DHT [] Remove all items
								 * with of specified IP in next.DHT []
								 * 
								 */
							}
						}
					} else {
						System.out.println("PrevDHT rejected");
						continue;
					}
				}
				catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
		}// End of run()
		
		private void connect() {

			// Wait for incomming connection from prevDHTServer
			try {
				serverSocket = new ServerSocket(TcpInPort);
				prevDHTSocket = serverSocket.accept();
				System.out.println("Connected to " + prevDHTSocket);
				inFromPrevDHT = new BufferedReader(new InputStreamReader(prevDHTSocket.getInputStream()));
				outToPrevDHT = new DataOutputStream(prevDHTSocket.getOutputStream());
			} catch (IOException e) {
				System.out.println("PrevListener|port=" + TcpInPort + ": " + e);
			}

		} // End of connect()

		private boolean authenticateID() {
			try {
				// Get first message from prevDHTServer (should reply with "ID\n" first)
				String message;
				message = inFromPrevDHT.readLine();
				int previDhtID = Integer.parseInt(message);

				// If ID is not valid (Java's modulo doesnt behave like other modulos)
				// ((ID - 1) % ringSize + ringSize) % ringSize is a circular number
				// with maxInt == ServerRingSize

				if (previDhtID != ((ID - 1) % ringSize + ringSize) % ringSize) {
					return false;
				} else {
					return true;
				}

			} catch (IOException e) {
				System.out.println("PrevListener: " + e);
				return false;
			}
		}// End of authenticateID()
	} // End of class PrevDhtConnection 
}// End of class DHT_Manager
