package P2P_ClientServer;

import java.io.File;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Client extends Application {

	private static final int clientPort = 20049;

	private UDP_Messenger udpMessenger;
	private TCP_Manager tcpManager;

	Stage pStage;
	TextArea logTextArea;
	ListView<String> fileListView;
	
	String fileName;
	String PeerIP;
	File defDir = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "tosend");
	
	String[] knownIPs = {"localhost"};

	@Override
	public void start(Stage primaryStage) throws Exception {

		// Start TCP & UDP Background processes
		udpMessenger = new UDP_Messenger(clientPort);
		tcpManager = new TCP_Manager(clientPort, defDir);

		// Appliation GUI start
		pStage = primaryStage;
		pStage.setTitle("P2P Client -> " + udpMessenger.getCurrentDHT());
		pStage.setScene(new Scene(newGUI(pStage), 800, 600));
		pStage.setResizable(false);
		pStage.show();

		// Some info for client-log
		pushLog("TCP Server started on port: " + tcpManager.getLocalPort());
		pushLog("UDP Server started on port: " + udpMessenger.getLocalPort());
	}

	// Formats message to query DHT
	void query(String fileName) {
		int key = hash(fileName);
		String message = String.format("query\n%d\r\n", key);
		String reply = udpMessenger.sendMessage(message);
		
		populateList(fileName, reply);
	}

	// Formats message to imform and update DHT
	String informUpdate(File file) {
		String fileName = file.getName();
		int hashedFileName = hash(fileName);
		String message = String.format("inform&update\nKey=%d\r\n", hashedFileName);
		return udpMessenger.sendMessage(message);
	}
	
	// Hash(x) -> summation of (int)chars in x
	int hash(String key) {
		int hashedKey = 0;
		char[] keyCharArr = key.toCharArray();
		for (int i = 0; i < keyCharArr.length; ++i) {
			hashedKey += Character.getNumericValue(keyCharArr[i]);
		}
		return hashedKey;
	}
	
	// Init
	String[] DHTinit() {
		String message = "init\nkthxbye\r\n";
		String reply = udpMessenger.sendMessage(message);
		if (reply == null) {
			return null;
		}
		System.out.println(reply);
		String[] newIPs = reply.split(",");
		return newIPs;
	}
	
	void exit() {
		/*
		 * TODO: On Plateform.exit(), run the exit command on dht servers
		 */
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

		
		
		
		// Top Menu
		Menu fileMenu = new Menu("File");
		MenuItem fileOpenMItem = new MenuItem("Upload");
		MenuItem fileQuitMItem = new MenuItem("Quit");
		Menu serverMenu = new Menu("Select Server");
		ToggleGroup dhtPickerTGroup = new ToggleGroup();
		MenuItem serverGetAll = new MenuItem("Get More...");
		FileChooser fChooser = new FileChooser();
		// Menu Logic
		fileOpenMItem.setOnAction(e -> {
			File f = fChooser.showOpenDialog(pStage);
			if (f != null)
				pushLog(informUpdate(f));
		});
		serverGetAll.setOnAction(e ->{
			String[] newKnownIPs = DHTinit();
			if (newKnownIPs == null) {
				pushLog("Could not get/init() server IPs");
			} else {
				knownIPs = newKnownIPs;
				refreshServerRadioItems(serverMenu, dhtPickerTGroup, serverGetAll);
			}
		});
		fileQuitMItem.setOnAction(e -> {
			// TODO: exit() here
			Platform.exit();
		});
		// Assemble Menu
		MenuBar mainMenu = new MenuBar(fileMenu, serverMenu);
		fileMenu.getItems().addAll(fileOpenMItem, fileQuitMItem);
		defDir.mkdirs(); // Make default directory
		fChooser.setInitialDirectory(defDir);
		refreshServerRadioItems(serverMenu, dhtPickerTGroup, serverGetAll);

		// Main Area
		fileListView = new ListView<String>();
		TextField searchTextArea = new TextField();
		Button queryButton = new Button("Search");
		TextField pathTextArea = new TextField(defDir.getAbsolutePath());
		Button downloadButton = new Button("Download");
		downloadButton.setDisable(true);
		// Log Area
		logTextArea = new TextArea();
		logTextArea.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
		logTextArea.setEditable(false);
		logTextArea.setMinHeight(80);
		// Main Area Logic
		queryButton.setOnAction(e -> {
			query(searchTextArea.getText());
			setDownloadFileName(searchTextArea.getText());
			downloadButton.setDisable(true);
		});
		fileListView.getSelectionModel().selectedItemProperty().addListener( (v, oldval, newval) -> {
			//TODO: On File Select, change path to reflect where file will download to.
			pathTextArea.setText(defDir.getAbsolutePath() + System.getProperty("file.separator") + this.fileName );
			setDownloadPeerIP(newval);
			if (!newval.contains("null")) downloadButton.setDisable(false);
		});
		downloadButton.setOnAction(e -> {
			TCP_Worker newPeer = tcpManager.initHandShake(PeerIP, clientPort);
			newPeer.get(fileName);
		});
		// Assemble Main Area
		
		HBox queryArea = new HBox(searchTextArea, queryButton);
		HBox downloadArea = new HBox(pathTextArea, downloadButton);
		VBox centerLayout = new VBox(queryArea, fileListView, downloadArea, logTextArea);
		// fileListView.setPrefHeight(Integer.MAX_VALUE);
		fileListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        fileListView.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        
        
		
		centerLayout.setVgrow(fileListView, Priority.ALWAYS);
		queryArea.setHgrow(searchTextArea, Priority.ALWAYS);
		downloadArea.setHgrow(pathTextArea, Priority.ALWAYS);
		pathTextArea.setDisable(true);

		// Assemble main layout
		VBox mainLayout = new VBox(mainMenu, centerLayout);
		return mainLayout;
	}
	
	void setDownloadFileName (String fileName) {
		this.fileName = fileName;
	}
	
	void setDownloadPeerIP (String ip) {
		this.PeerIP = ip;
	}
	
	//refreshServerRadioItems(Menu menuToResfresh, ToggleGroup toggleGroup, MenuItem lastItem)
	void refreshServerRadioItems(Menu menuToResfresh, ToggleGroup toggleGroup, MenuItem lastItem) {
		menuToResfresh.getItems().clear();
		
		for (int i=0; i < knownIPs.length; ++i) {
			RadioMenuItem newServer = new RadioMenuItem("ID: " + (i + 1) + "   IP: " + knownIPs[i]);
			newServer.setToggleGroup(toggleGroup);
			final String ip = knownIPs[i];
			newServer.setOnAction(e -> {
				udpMessenger.changeIp(ip);
				pStage.setTitle("P2P Client -> " + udpMessenger.getCurrentDHT());
			});;
			menuToResfresh.getItems().add(newServer);
		}
		
		menuToResfresh.getItems().add(lastItem);
		System.out.println("Reseting name");
		pStage.setTitle("P2P Client -> " + udpMessenger.getCurrentDHT());
	}
	
	void populateList(String fileName, String reply) {
		
		ObservableList<String> ips = FXCollections.observableArrayList();
		fileListView.setItems(ips);;
		// replySplit[1] = address
		String[] replySplit = reply.split("\n");
		
		for (int i = 1; i < replySplit.length; ++i) {
			
			String ip = replySplit[i].substring(replySplit[i].indexOf("/") + 1);
			ip =ip.replaceAll("\n", "");
			ip =ip.replaceAll("\r", "");
			ips.add(ip);
		}
		
	}
	

	public static void main(String[] args) {
		launch(args);
	}
}
