package DHT_Server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
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

		NextDhtConnection(String nextip) {
			nextIP = nextip;
		}

		@Override
		public void run() {

			try {
				// Connects to: remoteAddress, remotePort <-> localAddress, localPort
				nextDhtSocket.setSoTimeout(2000);
				nextDhtSocket = new Socket(nextIP, TcpInPort, InetAddress.getByName("localhost"), TcpOutPort);
				DataOutputStream outToNextDHT = new DataOutputStream(nextDhtSocket.getOutputStream());
				BufferedReader inFromNextDHT = new BufferedReader(new InputStreamReader(nextDhtSocket.getInputStream()));

				// On connect, immediately send ID for simple authentication
				outToNextDHT.writeBytes(String.format("%d\n", ID));

				// If connection is authenticated by server, return handle to socket
				if (inFromNextDHT.readLine().contains("CONNECTION OK")) {
					
					/* (probably will never need to get commands from nextDHT)
					 * =======================================
					 * RECEIVING COMMANDS FROM NEXT DHT SERVER
					 * =======================================
					 */
					
					while (!isStopped()) {
						
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						// Get Command, Blocks here until line availible in buffer or connection is closed
						String command = inFromNextDHT.readLine();

						if (command.toLowerCase().equals("hello")) {
							outToNextDHT.writeBytes("world");
						}
					}
				}

			} catch (UnknownHostException e) {
				System.out.println(e.getMessage());
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}

		}
	}
	
	// Thread that deals with PrevDhtServer
	private class PrevDhtConnection implements Runnable {

		ServerSocket serverSocket;
		int ringSize;
		
		PrevDhtConnection(int rSize) {
			ringSize = rSize;
		}

		@Override
		public void run() {
			// Attempt to start a TPC socket to listen to other DHT servers
			try {
				serverSocket = new ServerSocket(TcpInPort);
			} catch (IOException e) {
				// On exception shut everything down (rest of code useless wihtout a tcp
				// serversocket)
				System.out.println(e.getMessage() + " : " + TcpInPort);
				return;
			}

			// Wait for connection
			while (!isStopped()) {
				try {
					// Wait for incomming connection from prevDHTServer
					Socket previousDHTSocket = serverSocket.accept();
					System.out.println("Connected to " + previousDHTSocket);
					BufferedReader inFromPrevDHT = new BufferedReader(new InputStreamReader(previousDHTSocket.getInputStream()));
					DataOutputStream outToPrevDHT = new DataOutputStream(previousDHTSocket.getOutputStream());

					// Get first message from prevDHTServer (should reply with "ID\n" first)
					String message = inFromPrevDHT.readLine();
					int previDhtID = Integer.parseInt(message);

					// If ID is not valid (Java's modulo doesnt behave like other modulos)
					// ((ID - 1) % ringSize + ringSize) % ringSize is a circular number
					// with maxInt == ServerRingSize
					if (previDhtID != ((ID - 1) % ringSize + ringSize) % ringSize) {
						System.out.println("prevDHT has invalid ID, ignoring...");
						continue;
					} else {

						/*
						 * ===========================================
						 * RECEIVING COMMANDS FROM PREVIOUS DHT SERVER
						 * ===========================================
						 */

						System.out.println("prevDHT Accepted, listening to commands");

						// If ID is valid, start parsing for commands
						while (!isStopped()) {

							// Get Command, Blocks here until line availible in buffer or until connection is closed
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
					} // end if message is from previous DHT
				} // end try
				catch (IOException e) {
					System.out.println(e.getMessage());
				} // end catch

			} // listen for messages from DHT server
		}
	}
}
