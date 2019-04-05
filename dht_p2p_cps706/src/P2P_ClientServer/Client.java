package P2P_ClientServer;

import java.io.File;

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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class Client extends Application{
	
	private static final int clientPort = 7070;
	
	private UDP_Messenger udpMessenger;
	private TCP_Manager tcpManager;
	
	TextArea logTextArea;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		// Start TCP & UDP Background processes
		udpMessenger = new UDP_Messenger(clientPort);
		tcpManager = new TCP_Manager(clientPort);
		
		// Appliation GUI start
		primaryStage.setTitle("P2P Client");
		primaryStage.setScene(new Scene(newGUI(primaryStage), 800, 600));
		primaryStage.show();
		
		// Some info for client-log
		pushLog("TCP Server started on port: " + tcpManager.getLocalPort());
		pushLog("UDP Server started on port: " + udpMessenger.getLocalPort());
	}
	
	// Formats message to query DHT
	String query(String key) {
		String message = String.format("query\n%s\r\n", key);
		return udpMessenger.sendMessage(message);
	}
	
	// Formats message to imform and update DHT
	String informUpdate(File file) {
		String fileName = file.getName();
		String path = file.getPath().substring(0, file.getPath().indexOf(fileName));
		// switch to UNIX style paths
		path = path.replace("\\","/");
		String message = String.format("inform&update\nFileName=%s\nPath=%s\r\n", fileName, path);
		return udpMessenger.sendMessage(message);
	}
	
	// sends message to another p2p-client
	void p2pSend(String s) {
		
	}
	
	// writes to client-log
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
	
	// GUI
	@SuppressWarnings("static-access")
	private VBox newGUI(Stage pStage){
		
		FileChooser fChooser = new FileChooser();
		
		// Top Menu
		Menu file = new Menu("File");
		Menu edit = new Menu("Edit");
		MenuItem fileOpen = new MenuItem("Upload");
		MenuItem fileQuit = new MenuItem("Quit");
		MenuItem changeDHTIP = new MenuItem("Change DHT IP");
		// Menu Logic
		fileOpen.setOnAction(e -> {
			File f = fChooser.showOpenDialog(pStage);
			if (f != null) informUpdate(f);
		});
		fileQuit.setOnAction(e -> Platform.exit());
		changeDHTIP.setOnAction(e -> {
			showDhtWindow();
		});
		// Assemble Menu
		MenuBar mainMenu = new MenuBar(file, edit);
		file.getItems().addAll(fileOpen, fileQuit);
		edit.getItems().addAll(changeDHTIP);
		
		
		// Main Area
		ListView<File> fileListView = new ListView<File>(); //TODO: Removed "File" class cause using java.io.File
		TextField searchTextArea = new TextField();
		Button queryButton = new Button("Query");
		Button tcpSendButton = new Button("TCP Send");
		TextField pathTextArea = new TextField("/path/to/download/filename.jpg");
		Button downloadButton = new Button("Download");
		// Log Area
		logTextArea = new TextArea("Client Started");
		logTextArea.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
		logTextArea.setEditable(false);
		logTextArea.setMinHeight(80);
		// Main Area Logic
		queryButton.setOnAction(e -> pushLog(query(searchTextArea.getText())));
		tcpSendButton.setOnAction(e -> p2pSend(searchTextArea.getText()));
		downloadButton.setOnAction(e -> {System.out.print("d");});
		// Assemble Main Area
		fileListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		HBox queryArea = new HBox(searchTextArea, queryButton, tcpSendButton);
		HBox downloadArea = new HBox(pathTextArea, downloadButton);
		VBox centerLayout = new VBox(queryArea, fileListView, downloadArea, logTextArea);
		//fileListView.setPrefHeight(Integer.MAX_VALUE);
		centerLayout.setVgrow(fileListView, Priority.ALWAYS);
		queryArea.setHgrow(searchTextArea, Priority.ALWAYS);
		downloadArea.setHgrow(pathTextArea, Priority.ALWAYS);
		pathTextArea.setDisable(true);
		
		// Assemble main layout
		VBox mainLayout = new VBox(mainMenu, centerLayout);
		return mainLayout;
	}
	
	// Window to change DHT IP and Port
	void showDhtWindow() {
		Stage changeDHTwindow = new Stage();
		changeDHTwindow.setTitle("Change IP:Port of DHT");
		Text curDHTtext = new Text("Current DHT = " + udpMessenger.getCurrentDHT());
		TextField portField = new TextField();
		TextField ipField_1 = new TextField();
		TextField ipField_2 = new TextField();
		TextField ipField_3 = new TextField();
		TextField ipField_4 = new TextField();
		Button updateButton = new Button("Update");
		Button cancelButton = new Button("Cancel");
		// Logic
		updateButton.setOnAction(ev -> {
			udpMessenger.updateDHTinfo(String.format("%s.%s.%s.%s", ipField_1.getText(), ipField_2.getText(), ipField_3.getText(), ipField_4.getText()), Integer.valueOf(portField.getText()));
			curDHTtext.setText("Current DHT = " + udpMessenger.getCurrentDHT());
			// Clear textfields after updating
			portField.setText("");
			ipField_1.setText("");
			ipField_2.setText("");
			ipField_3.setText("");
			ipField_4.setText("");
		});
		cancelButton.setOnAction(ev -> {
			changeDHTwindow.close();
		});
		// Assemble changeDHT window
		HBox ipField = new HBox(ipField_1, new Text("."), ipField_2, new Text("."), ipField_3, new Text("."), ipField_4);
		HBox updateCancelButtons = new HBox(updateButton, cancelButton);
		VBox layout = new VBox(curDHTtext, ipField, portField, updateCancelButtons);
		changeDHTwindow.setScene(new Scene(layout, 300, 90));
		changeDHTwindow.initModality(Modality.APPLICATION_MODAL);
		changeDHTwindow.showAndWait();
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
