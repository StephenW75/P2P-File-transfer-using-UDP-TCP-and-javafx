package DHT_Server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Handles DHT server ring
public class DHT_Manager {

	private final int TcpInPort = 20041;

	private boolean STOP_SIGNAL = false;

	private NextDhtConnection nextDhtCon;
	private PrevDhtConnection prevDhtCon;
	private ExecutorService ex = Executors.newCachedThreadPool();

	private int ID;
	private int ringSize;
	Hashtable<String, String> database;

	// id: ID of this server, nextIP: IP of next server, dhtRingSize: number of dht
	// servers
	public DHT_Manager(Hashtable<String, String> referenceToDHT, int id, String nextIp, int dhtSize) {
		database = referenceToDHT;
		ID = id;
		ringSize = dhtSize;
		nextDhtCon = new NextDhtConnection(nextIp);
		prevDhtCon = new PrevDhtConnection(dhtSize);
		ex.submit(nextDhtCon);
		ex.submit(prevDhtCon);
	}

	// Ugly but it works...
	String[] getAllIPs() {
		// Starting ID keeps track so that servers won't keep asking for nextIPs
		int startingID = ID;
		String[] allIPs = new String[ringSize];

		String restOfIps = nextDhtCon.sendCommand("GET ALL IPS", Integer.toString(startingID));

		// restOfIps Format: "ip2|id2,ip3|id3,ip4|id4, ... ip1|id1END\r\n"
		String[] idip = restOfIps.split(",");
		for (int i = 0; i < ringSize; ++i) {
			String ip = idip[i].substring(0, idip[i].indexOf('|'));
			String id = idip[i].substring(idip[i].indexOf('|') + 1, idip[i].length());
			allIPs[Integer.parseInt(id)] = ip;
		}

		return allIPs;
	}

	// Safely stops thread
	public void stop() {
		STOP_SIGNAL = true;
		ex.shutdown();
	}

	private boolean isStopped() {
		return STOP_SIGNAL;
	}

	/*
	 * =============================================================================
	 * ==== CLASSES/RUNNABLES For NextDhtConnection and PrevDhtConnection are below
	 * =============================================================================
	 * ====
	 */

	// Thread that deals with NextDhtServer
	private class NextDhtConnection implements Runnable {

		private String nextIP;
		private Socket nextDhtSocket;

		DataOutputStream outToNextDHT;
		BufferedReader inFromNextDHT;

		NextDhtConnection(String nextip) {
			nextIP = nextip;
		}

		String sendCommand(String command, String instructions) {
			if (nextDhtSocket == null) {
				return null;
			} else {
				try {
					String message = String.format("%s\n%s\r\n", command, instructions);
					System.out.println("Sending to next: " + message);
					outToNextDHT.writeBytes(message);
					String reply = inFromNextDHT.readLine();
					return reply;
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
					 * ======================================= RECEIVING COMMANDS FROM NEXT DHT
					 * SERVER =======================================
					 */

					while (!isStopped()) {
						try {
							Thread.sleep(500);
							// Get Command, Blocks here until line availible in buffer or connection is
							// closed

							/*
							 * String command = inFromNextDHT.readLine();
							 * 
							 * if (command.toLowerCase().equals("ping")) { outToNextDHT.writeBytes("pong");
							 * }
							 * 
							 * 
							 * 
							 * } catch (IOException e) { System.out.println("NextListener: " + e);
							 */
						} catch (InterruptedException e) {
							System.out.println(e);
						}
					}
				} // End of authenticatedByID()
			}
		}// End of run()

		private void connect() {
			System.out.println("Attempting to connect to " + nextIP + ":" + TcpInPort);
			boolean connected = false;

			while (!isStopped() && !connected) {

				try {
					Thread.sleep(2500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

				try {
					// Connects to: remoteAddress, remotePort <-> localAddress, localPort
					nextDhtSocket = new Socket(nextIP, TcpInPort);
					connected = true;
					outToNextDHT = new DataOutputStream(nextDhtSocket.getOutputStream());
					inFromNextDHT = new BufferedReader(new InputStreamReader(nextDhtSocket.getInputStream()));
				} catch (SocketException e) {
					System.out.println(e.getMessage() + ". Retrying " + nextIP + ":" + TcpInPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private boolean authenticatedByID(int id) {
			// On connect, immediately send ID for simple authentication
			try {
				outToNextDHT.writeBytes(String.format("%d\n", id));

				if (inFromNextDHT.readLine().contains("CONNECTION OK")) {
					System.out.println(nextDhtSocket + " Authenticated Me");
					return true;
				} else {
					System.out.println(nextDhtSocket + " Rejected Me");
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

		@Override
		public void run() {

			// Wait for connection
			while (!isStopped()) {
				connect();
				try {

					if (authenticateID()) {
						/*
						 * =============================================================================
						 * ============= RECEIVING COMMANDS FROM PREVIOUS DHT SERVER
						 * =============================================================================
						 */

						System.out.println("prevDHT Accepted, listening to commands..");

						// If ID is valid, start parsing for commands
						while (!isStopped()) {
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							// Get Command, Blocks here until line availible in buffer or until connection
							// is closed
							if (inFromPrevDHT.ready()) {
								String command = inFromPrevDHT.readLine();

								// Init; recursively calls next server to get it's IP
								if (command.toLowerCase().equals("get all ips")) {

									// ID of server which initially called "GET ALL IPS"
									String startingID = inFromPrevDHT.readLine();

									// Get localIP of server
									String iplocal = getAddress("192").toString();
									// Add ID to reply
									iplocal = iplocal.substring(iplocal.indexOf("/") + 1, iplocal.length());
									iplocal += "|" + ID;

									// If this command has looped back to initial caller, return nothing (already
									// stored)
									if (Integer.parseInt(startingID) == ID) {
										outToPrevDHT.writeBytes(iplocal + "\r\n");
									} else {
										String nextIps = iplocal + ","
												+ nextDhtCon.sendCommand("GET ALL IPS", startingID);
										outToPrevDHT.writeBytes(nextIps + '\n');
									}
								} // End of Init

								// Query
								else if (command.toLowerCase().equals("query")) {
									System.out.println("query runs here");
									/*
									 * TODO: If DHT contains query item return IP of client with said item to
									 * prev.DHTserver [] Else ask next.DHTserver to query item []
									 * 
									 */
								} // End of Query

								// Exit
								else if (command.toLowerCase().equals("exit")) {
									System.out.println("exit runs here");
									/*
									 * TODO: Remove all items with of specified IP in this.DHT [] Remove all items
									 * with of specified IP in next.DHT []
									 * 
									 */
								} // Edn of Exit
							}
						}
					} else {
						System.out.println("PrevDHT rejected");
						continue;
					}
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
		}// End of run()

		InetAddress getAddress(String seq) {
			Enumeration<NetworkInterface> allNetInterface;
			try {
				allNetInterface = NetworkInterface.getNetworkInterfaces();
				while (allNetInterface.hasMoreElements()) {
					NetworkInterface nInterface = allNetInterface.nextElement();
					Enumeration<InetAddress> addressOfInterface = nInterface.getInetAddresses();
					while (addressOfInterface.hasMoreElements()) {
						InetAddress address = addressOfInterface.nextElement();
						if (address.getHostAddress().contains(seq)) {
							return address;
						}
					}
				}
			} catch (SocketException e) {
				e.printStackTrace();
			}
			return null;
		}

		private void connect() {
			
			boolean connected = false;
			
			while (!isStopped() && !connected) {
				// Wait for incomming connection from prevDHTServer
				try {
					serverSocket = new ServerSocket(TcpInPort);
					System.out.println("Waiting for a prevDhtConnection on socket: " + serverSocket);
					prevDHTSocket = serverSocket.accept();
					connected = true;
					inFromPrevDHT = new BufferedReader(new InputStreamReader(prevDHTSocket.getInputStream()));
					outToPrevDHT = new DataOutputStream(prevDHTSocket.getOutputStream());
				} catch (IOException e) {
					System.out.println("PrevListener|port=" + TcpInPort + ": " + e);
				}
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
					outToPrevDHT.writeBytes("CONNECTION REJECT\r\n");
					serverSocket.close();
					return false;
				} else {
					outToPrevDHT.writeBytes("CONNECTION OK\r\n");
					return true;
				}

			} catch (IOException e) {
				System.out.println("PrevListener: " + e);
				return false;
			}
		}// End of authenticateID()
	} // End of class PrevDhtConnection
}// End of class DHT_Manager