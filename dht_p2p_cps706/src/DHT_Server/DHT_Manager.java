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

	// Not private case PeerListener uses it
	int ID;
	int ringSize;

	Hashtable<Integer, String> database;

	// id: ID of this server, nextIP: IP of next server, dhtRingSize: number of dht
	// servers
	public DHT_Manager(Hashtable<Integer, String> referenceToDHT, int id, String nextIp, int dhtSize) {
		database = referenceToDHT;
		ID = id;
		ringSize = dhtSize;
		nextDhtCon = new NextDhtConnection(nextIp);
		prevDhtCon = new PrevDhtConnection(dhtSize);
		ex.submit(prevDhtCon);
		ex.submit(nextDhtCon);
	}

	// This server requests all IPs of other servers recursively.
	String[] getAllIPs() {
		// Starting ID keeps track so that servers won't keep asking for nextIPs
		int startingID = ID;
		String[] allIPs = new String[ringSize + 1];

		String restOfIps = nextDhtCon.sendCommand("GET ALL IPS", Integer.toString(startingID));
		System.out.println(restOfIps);

		// restOfIps Format: "ip2|id2,ip3|id3,ip4|id4, ... ip1|id1END\r\n"
		String[] idip = restOfIps.split(",");
		// Store IPs in a String Array, index being the ID.
		for (int i = 0; i < ringSize; ++i) {
			String ip = idip[i].substring(0, idip[i].indexOf('|'));
			String id = idip[i].substring(idip[i].indexOf('|') + 1, idip[i].length());
			allIPs[Integer.parseInt(id)] = ip;
		}

		return allIPs;
	}

	
	
	
	// Inserts hashedKey into "closest" server
	String informUpdate(int serverID, int hashedKey, String ip) {

		// Aske next server to store
		return nextDhtCon.sendCommand("INFORM&UPDATE", String.format("%d,%d,%s", serverID, hashedKey, ip));
	}
	
	/*
	 * 
	 * QUERY
	 * 
	 */

	String query(int serverID, int hashedKey) {

		// Asks Next Server if it is has item
		return nextDhtCon.sendCommand("QUERY", String.format("%d,%d", serverID, hashedKey));
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
	 * ============= NextDhtConnection and PrevDhtConnection are below =============
	 * =============================================================================
	 */

	// Probes for Next Connect
	private class NextDhtConnection implements Runnable {

		private String nextIP;
		private Socket nextDhtSocket;

		private DataOutputStream outToNextDHT;
		private BufferedReader inFromNextDHT;

		NextDhtConnection(String nextip) {
			nextIP = nextip;
		}

		String sendCommand(String command, String instructions) {
			if (nextDhtSocket == null) {
				return null;
			} else {
				try {
					String message = String.format("%s\n%s\r\n", command, instructions);
					// System.out.println("Sending to next:\n" + message);
					outToNextDHT.writeBytes(message);
					
					System.out.print("waiting for reply .");
					while (!inFromNextDHT.ready()) {
						System.out.print(" .");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
					}
					String reply = inFromNextDHT.readLine();
					System.out.println(" Reply reveived!");
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

				// Try to connect every X milliseconds
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

				// If connection is authenticated by server, return handle to socket
				if (authenticatedByID(connect())) {

					/*
					 * (probably will never need to get commands from nextDHT) DO NOT USE
					 * =============================================================================
					 * ================== RECEIVING COMMANDS FROM NEXT DHTSERVER ===================
					 * =============================================================================
					 */

					while (!isStopped()) {
						try {
							Thread.sleep(50);
							// Get Command, Blocks here until line availible in buffer or connection is
							// closed
							/*
							 * String command = inFromNextDHT.readLine();
							 * 
							 * if (command.toLowerCase().contains("ping")) {
							 * outToNextDHT.writeBytes("pong\r\n"); }
							 * 
							 * } catch (IOException e) { System.out.println("NextListener: " +
							 * e.getMessage()); break;
							 */
						} catch (InterruptedException e) {
							System.out.println(e.getMessage());
						}
					}
				}
			}
		}// End of run()

		private String connect() {
			System.out.print("Attempting to connect to Next Server [" + nextIP + ":" + TcpInPort + "] ... ");
			try {
				nextDhtSocket = new Socket(nextIP, TcpInPort);
				outToNextDHT = new DataOutputStream(nextDhtSocket.getOutputStream());
				inFromNextDHT = new BufferedReader(new InputStreamReader(nextDhtSocket.getInputStream()));

				if (nextDhtSocket.isConnected() && !nextDhtSocket.isClosed()) {
					outToNextDHT.writeBytes(String.format("%d\n", ID));
					String reply = inFromNextDHT.readLine();
					return reply;
				}
			} catch (SocketException e) {
				if (e.getMessage().contains("refused")) {
					System.out.println("Connection refused, retrying.");
				} else {
					System.out.println(e.getMessage());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		private boolean authenticatedByID(String reply) {

			if (reply == null) {
				return false;
			}

			if (reply.contains("CONNECTION OK")) {
				System.out.println("Connected!");
				return true;
			} else {
				System.out.println("Rejected by Next Server, retrying.");
				return false;
			}
		}
	}// End of class NextDhtConnection

	// Listens for a Previous Connection
	private class PrevDhtConnection implements Runnable {

		int ringSize;

		ServerSocket serverSocket;

		BufferedReader inFromPrevDHT;
		DataOutputStream outToPrevDHT;

		// Constructor
		PrevDhtConnection(int rSize) {
			ringSize = rSize;
		}

		@Override
		public void run() {

			// Wait for connection
			while (!isStopped()) {
				try {

					// Try to connect every X milliseconds
					try {
						Thread.sleep(250);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}

					// Connect and authenticate
					if (authenticateID(connect())) {

						/*
						 * =============================================================================
						 * ================ RECEIVING COMMANDS FROM PREVIOUS DHT SERVER ================
						 * =============================================================================
						 */

						System.out.println("===== Connected to Prev Server! =====");

						// If ID is valid, start parsing for commands
						while (!isStopped()) {

							// Wait for incomming COMMAND\n of message: "COMMAND\nINSTRUCTIONS\r\n"
							String command = inFromPrevDHT.readLine();

							// Init: recursively calls next server to get it's IP
							if (command.toLowerCase().equals("get all ips")) {

								// ID of server which initially called "GET ALL IPS"
								String startingID = inFromPrevDHT.readLine();

								// Get localIP of server
								String iplocal = getAddress("192").toString();
								// Add ID to reply
								iplocal = iplocal.substring(iplocal.indexOf("/") + 1, iplocal.length());
								iplocal += "|" + ID;

								System.out.print("Prev is asking for all ips ...");

								// If this command has looped back to initial caller, return nothing (already
								// stored)
								if (Integer.parseInt(startingID) == ID) {
									System.out.println("I'm the last one, send back my IP");
									outToPrevDHT.writeBytes(iplocal + "\n");
								} else {
									System.out.println("I'm not last, add my ip to next!");
									String nextIps = iplocal + "," + nextDhtCon.sendCommand("GET ALL IPS", startingID);
									outToPrevDHT.writeBytes(nextIps + '\n');
								}
							} // End of Init
								// Inform and Update
							else if (command.toLowerCase().equals("inform&update")) {

								// "%d,%d,%s\r\n" , serverID, hashedKey, ip;
								// info[0] = serverID, info[1] = hashedKey, info[2] = ip
								String[] info = inFromPrevDHT.readLine().split(",");

								// information from instructions
								int destinationID = Integer.parseInt(info[0]);
								int hashedKey = Integer.parseInt(info[1]);
								String ip = info[2];

								System.out.print("Prev is asking if i can store information ... ");

								// Can i store this information?
								if (destinationID == ID) {
									// Store information
									System.out.println("I am storing the information: " + hashedKey + ":" + ip);
									database.put(hashedKey, ip);
									outToPrevDHT.writeBytes(ID + "\n");
								} else {
									// Ask Next Server to store information
									System.out.println("I'm not in responsible for this, send to next");
									outToPrevDHT.writeBytes(informUpdate(destinationID, hashedKey, ip)+ '\n');
								}
							}
							// Query
							else if (command.toLowerCase().equals("query")) {

								// "%d%d", serverID, hashedKey
								// info[0] = serverID, info[1] = hashedKey
								String[] info = inFromPrevDHT.readLine().split(",");

								// information from instructions
								int destinationID = Integer.parseInt(info[0]);
								int hashedKey = Integer.parseInt(info[1]);

								// Can i get this information?
								if (destinationID == ID) {
									// Store information
									String ip = database.get(hashedKey);
									outToPrevDHT.writeBytes(ip + "\n");
								} else {
									// Ask Next Server to store information
									outToPrevDHT.writeBytes(query(destinationID, hashedKey) + "\n");
								}
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
					} else {
						System.out.println("Prev Server rejected");
						continue;
					}
				} catch (IOException e) {
					System.out.println("Error while listening for Prev Server commands: " + e.getMessage());
					try {
						serverSocket.close();
					} catch (IOException e1) {
						System.out.println("Error closing Prev Server Socket: " + e.getMessage());
					}
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

		private String connect() {
			try {
				serverSocket = new ServerSocket(TcpInPort);

				// Wait for incomming connection from prevDHTServer
				try {
					System.out.println("Listening for Prev Server on port: " + serverSocket.getLocalPort());
					// Blocks here while waiting for a connection request
					Socket prevDHTSocket = serverSocket.accept();

					// Connection request accepted, set input/output streams
					inFromPrevDHT = new BufferedReader(new InputStreamReader(prevDHTSocket.getInputStream()));
					outToPrevDHT = new DataOutputStream(prevDHTSocket.getOutputStream());

					// On connect get reply, and return it for authentications
					return inFromPrevDHT.readLine();
				} catch (IOException e) {
					System.out.println("While listening for previous server: " + e.getMessage());
					serverSocket.close();
					return null;
				}
			} catch (IOException e) {
				System.out.println("Could not create server socket to listen for previous server: " + e.getMessage());
				return null;
			}
		} // End of connect()

		private boolean authenticateID(String token) {

			if (token == null) {
				return false;
			}

			try {
				// Get first message from prevDHTServer (should reply with "ID\n" first)
				int previDhtID = Integer.parseInt(token);

				// If ID is not valid (Java's modulo doesnt behave like other modulos)
				// ((ID - 1) % ringSize + ringSize) % ringSize is a circular number
				// with maxInt == ServerRingSize

				// Returns a single-line (for readLine()) to inform if authenticated
				if (previDhtID != new CircularNum(ringSize).getNumber(ID - 1)) {
					outToPrevDHT.writeBytes("CONNECTION REJECT\r\n");
					return false;
				} else {
					outToPrevDHT.writeBytes("CONNECTION OK\r\n");
					return true;
				}

			} catch (NumberFormatException e) {
				System.out.println("Could not determine ID to authenticate previous connection");
				return false;
			} catch (IOException e) {
				System.out.println(e.getMessage());
				return false;
			}
		}// End of authenticateID()
	} // End of class PrevDhtConnection
}// End of class DHT_Manager