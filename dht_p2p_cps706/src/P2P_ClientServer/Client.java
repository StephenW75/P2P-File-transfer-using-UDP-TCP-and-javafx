package P2P_ClientServer;

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
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class Client extends Application{
	
	private static final int clientPort = 7070;
	
	private UDP_Messenger udpMessenger;
	private TCP_Manager tcpManager;
	
	TCP_Worker w;
	
	TextArea logTextArea;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		// UDP
		udpMessenger = new UDP_Messenger(clientPort);
		// TCP
		tcpManager = new TCP_Manager(clientPort);
		
		Stage window = primaryStage;
		window.setTitle("P2P Client");
		window.setScene(new Scene(newGUI(), 800, 600));
		window.show();
		
		pushLog("TCP Server started on port: " + tcpManager.getLocalPort());
		pushLog("UDP Server started on port: " + udpMessenger.getLocalPort());
		
	}
	
	void dhtSend(String s) {
		//String reply = udpMessenger.sendMessage(s);
		//pushLog(reply);
	}
	
	void search(String key) {
		String message = String.format("query\n%s", key);
		dhtSend(message);
	}
	
	void p2pSend(String s) {
		w.sendRawMessage(s + "\n");
	}
	
	void pushLog(String log) {
		logTextArea.appendText("\n" + log);
	}
	
	
	/*
	 * Cleanup on exit/stop client
	 */
	@Override
	public void stop() {
		System.out.println("Exiting Client..");
		udpMessenger.cleanUp();
		tcpManager.cleanUp();
		System.out.println("Exit Complete");
	}
	
	@SuppressWarnings("static-access")
	private VBox newGUI(){
		
		
		// Top Menu
		Menu file = new Menu("File");
		Menu edit = new Menu("Edit");
		MenuItem fileOpen = new MenuItem("Upload");
		MenuItem fileQuit = new MenuItem("Quit");
		MenuItem changeDHTIP = new MenuItem("Change DHT IP");
		//changeDHT window
		Stage changeDHT = new Stage();
		changeDHT.setTitle("Change IP and Port of DHT");
		Text currentDHTLoc = new Text("Current DHT = ");
		TextField DHTVBox_port = new TextField();
		TextField DHTVBox_ip1 = new TextField();
		TextField DHTVBox_ip2 = new TextField();
		TextField DHTVBox_ip3 = new TextField();
		TextField DHTVBox_ip4 = new TextField();
		Button DHTVBox_Update = new Button("Update");
		Button DHTVBox_Cancel = new Button("Cancel");
		// Assemble changeDHT window
		HBox DHTVBox_ip = new HBox(DHTVBox_ip1, new Text("."), DHTVBox_ip2, new Text("."), DHTVBox_ip3, new Text("."), DHTVBox_ip4);
		HBox DHTVBox_updatecancel = new HBox(DHTVBox_Update, DHTVBox_Cancel);
		VBox DHTVBox = new VBox(currentDHTLoc, DHTVBox_ip, DHTVBox_port, DHTVBox_updatecancel);
		changeDHT.setScene(new Scene(DHTVBox, 300, 90));
		// Menu Logic
		fileQuit.setOnAction(e -> Platform.exit());
		changeDHTIP.setOnAction(e -> {
			currentDHTLoc.setText("Current DHT = " + udpMessenger.getCurrentDHTLoc());
			changeDHT.show();
		});
		// DHT menu logic
		DHTVBox_Update.setOnAction(e -> {
			udpMessenger.updateDHTinfo(String.format("%s.%s.%s.%s", DHTVBox_ip1.getText(), DHTVBox_ip2.getText(), DHTVBox_ip3.getText(), DHTVBox_ip4.getText()), Integer.valueOf(DHTVBox_port.getText()));
			currentDHTLoc.setText("Current DHT = " + udpMessenger.getCurrentDHTLoc());
			DHTVBox_port.setText("");
			DHTVBox_ip1.setText("");
			DHTVBox_ip2.setText("");
			DHTVBox_ip3.setText("");
			DHTVBox_ip4.setText("");
		});
		DHTVBox_Cancel.setOnAction(e -> {changeDHT.close();});
		// Assemble Menu
		MenuBar mainMenu = new MenuBar(file, edit);
		file.getItems().addAll(fileOpen, fileQuit);
		edit.getItems().addAll(changeDHTIP);
		
		
		// Main Area
		ListView<File> fileListView = new ListView<File>();
		TextArea searchTextArea = new TextArea();
		Button queryButton = new Button("Search");
		Button tcpSendButton = new Button("TCP Send");
		TextArea pathTextArea = new TextArea();
		Button downloadButton = new Button("Download");
		// Log Area
		logTextArea = new TextArea("Client Started");
		logTextArea.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
		logTextArea.setEditable(false);
		logTextArea.setMinHeight(80);
		// Main Area Logic
		queryButton.setOnAction(e -> search(searchTextArea.getText()));
		tcpSendButton.setOnAction(e -> p2pSend(searchTextArea.getText()));
		downloadButton.setOnAction(e -> {System.out.print("d");});
		// Assemble Main Area
		fileListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		pathTextArea.setMinHeight(25);
		pathTextArea.setPrefHeight(25);
		searchTextArea.setMinHeight(25);
		searchTextArea.setPrefHeight(25);
		HBox searchArea = new HBox(searchTextArea, queryButton, tcpSendButton);
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
