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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Client extends Application {

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
		String message = String.format("inform&update\nFileName=%s\r\n", fileName);
		return udpMessenger.sendMessage(message);
	}

	// sends message to another p2p-client
	void p2pSend(String s) {

	}

	// writes to client-log
	void pushLog(String log) {
		logTextArea.appendText(log + '\n');
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
	private VBox newGUI(Stage pStage) {

		FileChooser fChooser = new FileChooser();

		// Top Menu
		Menu file = new Menu("File");
		MenuItem fileOpen = new MenuItem("Upload");
		MenuItem fileQuit = new MenuItem("Quit");
		// Menu Logic
		fileOpen.setOnAction(e -> {
			File f = fChooser.showOpenDialog(pStage);
			if (f != null)
				informUpdate(f);
		});
		fileQuit.setOnAction(e -> Platform.exit());
		// Assemble Menu
		MenuBar mainMenu = new MenuBar(file);
		file.getItems().addAll(fileOpen, fileQuit);

		// Main Area
		ListView<File> fileListView = new ListView<File>(); // TODO: Removed "File" class cause using java.io.File
		TextField searchTextArea = new TextField();
		Button queryButton = new Button("Search");
		TextField pathTextArea = new TextField("/path/to/download/filename.jpg");
		Button downloadButton = new Button("Download");
		// Log Area
		logTextArea = new TextArea();
		logTextArea.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
		logTextArea.setEditable(false);
		logTextArea.setMinHeight(80);
		// Main Area Logic
		queryButton.setOnAction(e -> pushLog(query(searchTextArea.getText())));
		downloadButton.setOnAction(e -> {
			System.out.print("d");
		});
		// Assemble Main Area
		fileListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		HBox queryArea = new HBox(searchTextArea, queryButton);
		HBox downloadArea = new HBox(pathTextArea, downloadButton);
		VBox centerLayout = new VBox(queryArea, fileListView, downloadArea, logTextArea);
		// fileListView.setPrefHeight(Integer.MAX_VALUE);
		centerLayout.setVgrow(fileListView, Priority.ALWAYS);
		queryArea.setHgrow(searchTextArea, Priority.ALWAYS);
		downloadArea.setHgrow(pathTextArea, Priority.ALWAYS);
		pathTextArea.setDisable(true);

		// Assemble main layout
		VBox mainLayout = new VBox(mainMenu, centerLayout);
		return mainLayout;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
