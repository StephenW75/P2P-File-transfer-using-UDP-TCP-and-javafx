package DHT_Server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

// Handles DHT server ring
public class DHTListener implements Runnable{
	
	private ServerSocket serverSocket;
	private final int TcpOutPort = 20040;
	private final int TcpInPort = 20041;
	private int ID;
	private String nextDHTServerIP;
	
	private NextDhtFinder dhtFinder;
	private Socket nextDHTSocket;
	private DataOutputStream outToNextDHTServer;
	private BufferedReader inFromNextDHTServer;
	
	private boolean STOP_SIGNAL = false;
	
	// Constructor
	public DHTListener(int id, String nextIp) {
		ID = id; 
		nextDHTServerIP = nextIp; 
	}
	
	void setNextDHTServerIP(String newIP) {
		nextDHTServerIP = newIP; 
	}
	
	String getNextDHTServerIP() {
		return nextDHTServerIP;
	}
	
	// Safely stops thread
	public void stop() {
		dhtFinder.safeStop();
		STOP_SIGNAL = true;
		try {
			serverSocket.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private boolean isStopped() {
		return STOP_SIGNAL;
	}
	
	public void run() {
		
		// Attempt to start a TPC socket to listen to other DHT servers
		try {
			serverSocket = new ServerSocket(TcpInPort);
		}
		catch(IOException e) {
			// On exception shut everything down (rest of code useless wihtout a tcp serversocket)
			System.out.println(e.getMessage() + " : " + TcpInPort);
			return;
		}
		
		// Start a new thread that attempts to connect to nextDHTServer
		dhtFinder = new NextDhtFinder();
		Thread dhtFinderThread = new Thread(dhtFinder);
		dhtFinderThread.start();
		
		// Wait for connection
		while(!isStopped()) {
			try {
				// Wait for incomming connection
				Socket previousDHTSocket = serverSocket.accept();
				System.out.println("Connected to " + previousDHTSocket);
				BufferedReader inFromPreviousDHT = new BufferedReader(new InputStreamReader(previousDHTSocket.getInputStream()));
				DataOutputStream outToPreviousDHT = new DataOutputStream(previousDHTSocket.getOutputStream());
				
				// Get first message from previosDHT (should reply with "ID\n" first)
				String message = inFromPreviousDHT.readLine(); 
				int previousDHTID = Integer.parseInt(message);
				
				// If ID is not valid, Java's modulo doesnt behave like other modulos.
				// ((ID-1)% 4+4)%4 gives you a proper circular number
				if(previousDHTID != ((ID-1)% 4+4)%4) {
					System.out.println("prevDHT has invalid ID, ignoring...");
					continue;
				} else {
					
					
					
				System.out.println("prevDHT Accepted, listening to commands");

				// If ID is valid, start parsing for commands
					while (!isStopped()) {
						
						// Get Command, Blocks here until line availible in buffer
						String command = inFromPreviousDHT.readLine();
						
						
						if(command.toLowerCase().equals("getallip")) {
							System.out.println("DHTinit runs here");
							//outToNextDHTServer.writeBytes("DHTinit\n"+messageData+InetAddress.getLocalHost().getHostAddress()+"\n");
						}//if DHTinit request
						
						
						
						else if(command.toLowerCase().equals("query")) {
							System.out.println("query runs here");
							/*
							 * TODO:
							 * If DHT contains query item return IP of client with said item to prev.DHTserver []
							 * Else ask next.DHTserver to query item []
							 * 
							 */
						}
						else if(command.toLowerCase().equals("exit")) {
							System.out.println("exit runs here");
							/*
							 * TODO:
							 * Remove all items with of specified IP in this.DHT []
							 * Remove all items with of specified IP in next.DHT []
							 * 
							 */
						}
					}

					
				}//end if message is from previous DHT
				outToNextDHTServer =new DataOutputStream(nextDHTSocket.getOutputStream());
				inFromNextDHTServer = new BufferedReader(new InputStreamReader(nextDHTSocket.getInputStream()));
			}// end try
			catch(IOException e) {
				System.out.println(e.getMessage()); 
			}// end catch
		
			
		}//listen for messages from DHT server
	}
	
	
	// Creates an outgoing connection to next dht server, as a thread
	private class NextDhtFinder implements Runnable {
		
		private boolean STOP_SIGNAL = false;
		
		public void safeStop() {
			STOP_SIGNAL = true;
			try {
				if (nextDHTSocket != null) {
					nextDHTSocket.close();
					System.out.println("Closed nextDHTSocket");
				}
			} catch (IOException e) {
				System.out.println("Failed to close nextDHTSocket: " + e.getMessage());
			}
		}
		
		private boolean isStopped () {
			return STOP_SIGNAL;
		}

		@Override
		public void run() {
			
			while (!isStopped()) {
				
				// Only attempts to connect to nextDHTServer ever 1 second (saves on CPU time)
				try {
					Thread.sleep(50);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				// If nextDHTServerIP is provided ... attempt to connect to it
				if (getNextDHTServerIP() != null) {
					try {
						// Connects to: remoteAddress, remotePort <-> localAddress, localPort
						nextDHTSocket.setSoTimeout(2000);
						nextDHTSocket = new Socket(nextDHTServerIP, TcpInPort, InetAddress.getByName("localhost"), TcpOutPort);
						outToNextDHTServer = new DataOutputStream(nextDHTSocket.getOutputStream());
						inFromNextDHTServer = new BufferedReader(new InputStreamReader(nextDHTSocket.getInputStream()));
						
						// On connect, immediately send ID for simple authentication
						outToNextDHTServer.writeBytes(String.format("%d\n", ID));
						
					} catch (UnknownHostException e) {
						System.out.println(e.getMessage());
					} catch (IOException e) {
						System.out.println(e.getMessage());
					}
				}
			}
		}
	}
}
	

