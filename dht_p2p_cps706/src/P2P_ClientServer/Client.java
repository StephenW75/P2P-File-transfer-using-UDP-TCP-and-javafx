package P2P_ClientServer;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class Client extends Application{
	
	// DHT Server Information
	private static final String dhtIP = "localhost";
	private static final int dhtServerPort = 7080;

	private static final int clientPort = 7090;
	
	ExecutorService executor = Executors.newCachedThreadPool();
	
	UDP_Messenger udpMessenger;
	TCP_Messenger tcpMessenger;
	DatagramSocket udpSocket;
	ServerSocket tcpSocket;
	
	TextArea logTextArea;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		// UDP
		udpSocket = new DatagramSocket(clientPort);
		InetAddress dhtIpAddress = InetAddress.getByName(dhtIP);
		udpMessenger = new UDP_Messenger(udpSocket, dhtIpAddress, dhtServerPort);
		
		// TCP
		tcpSocket = new ServerSocket(clientPort);
		tcpMessenger = new TCP_Messenger(tcpSocket);
		TCP_Listener tcpWorker = new TCP_Listener(tcpSocket);
		
		// Multi-threading
		executor.submit(tcpWorker);
		
		
		Stage window = primaryStage;
		window.setTitle("P2P Client");
		window.setScene(new Scene(newGUI(), 800, 600));
		window.show();
		
		pushLog("TCP on port: " + tcpSocket.getLocalPort());
		pushLog("UDP on port: " + udpSocket.getLocalPort());
		
	}
	
	
	void search(String key) {
		String message = String.format("query\n%s", key);
		dhtSend(message);
	}
	
	void pushLog(String log) {
		logTextArea.appendText("\n" + log);
	}

	void dhtSend(String s) {
		udpMessenger.setMessage(s);
		Future<String> reply = executor.submit(udpMessenger);
		
		// Do something with reply
		try {
			pushLog(reply.get());
		} catch (ExecutionException e) {
		// Catches exceptions thrown by thread
			pushLog(e.getMessage());
		} catch (InterruptedException e) {
		// Something went wrong!
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop() {
		udpSocket.close();
		try {
			tcpSocket.close();
		} catch (IOException e) {
			System.out.print("Could not close TCP socket");
		}
		executor.shutdown();
	}
	
	@SuppressWarnings("static-access")
	private VBox newGUI() {
		// Top Menu
		Menu file = new Menu("File");
		MenuItem fileOpen = new MenuItem("Open");
		MenuItem fileQuit = new MenuItem("Quit");
		// Menu Logic
		fileQuit.setOnAction(e -> Platform.exit());
		// Assemble Menu
		MenuBar mainMenu = new MenuBar(file);
		file.getItems().addAll(fileOpen, fileQuit);
		
		// Main Area
		ListView<File> fileListView = new ListView<File>();
		TextArea searchTextArea = new TextArea();
		Button queryButton = new Button("Search");
		TextArea pathTextArea = new TextArea();
		Button downloadButton = new Button("Download");
		// Log Area
		logTextArea = new TextArea("Client Started");
		logTextArea.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
		logTextArea.setEditable(false);
		logTextArea.setMinHeight(80);
		// Main Area Logic
		queryButton.setOnAction(e -> search(searchTextArea.getText()));
		downloadButton.setOnAction(e -> {System.out.print("d");});
		// Assemble Main Area
		fileListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		pathTextArea.setMinHeight(25);
		pathTextArea.setPrefHeight(25);
		searchTextArea.setMinHeight(25);
		searchTextArea.setPrefHeight(25);
		HBox searchArea = new HBox(searchTextArea, queryButton);
		searchArea.setHgrow(searchTextArea, Priority.ALWAYS);
		HBox downloadArea = new HBox(pathTextArea, downloadButton);
		downloadArea.setHgrow(pathTextArea, Priority.ALWAYS);
		VBox centerLayout = new VBox(searchArea, fileListView, downloadArea, logTextArea);
		fileListView.setPrefHeight(Integer.MAX_VALUE);
		centerLayout.setVgrow(fileListView, Priority.ALWAYS);
		
		// Assemble main layout
		VBox mainLayout = new VBox(mainMenu, centerLayout);
		return mainLayout;
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
