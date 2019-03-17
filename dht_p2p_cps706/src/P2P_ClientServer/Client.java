package P2P_ClientServer;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Menu;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class Client extends Application{
	
	TextArea logTextArea;
	Stage window;
	Button _B_quit;
	UDP_Messenger messenger;
	
	// IP of DHTs here!
	static final String dht1ip = "google.com";
	static final String dht2ip = "reddit.com";
	// DHT port
	private static final int dhtPort = 7080;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		/*
		 * NETWORKING STUFF HERE
		 */
		try {
			messenger = new UDP_Messenger(dhtPort);
		} catch (Exception e) {
			System.out.println("Could not create messenger");
			System.out.println(e.toString());
		}
		
		/*
		 * UI & Logic Stuff Here!
		 */
		window = primaryStage;
		window.setTitle("P2P Client");
		
		// Main layout
		BorderPane mainLayout = new BorderPane();
		
		// Menu
		MenuBar mainMenu = new MenuBar();
		Menu file = new Menu("File");
		MenuItem fileOpen = new MenuItem("Open");
		MenuItem fileQuit = new MenuItem("Quit");
		//------------------
		Menu edit = new Menu("Edit");
		Menu editSelDHT = new Menu("Select DHT (ip)");
		ToggleGroup selDHTGroup = new ToggleGroup();
		RadioMenuItem dht1 = new RadioMenuItem("192.168.bla.1");
		RadioMenuItem dht2 = new RadioMenuItem("192.168.bla.2");
		// Menu Logic
		fileQuit.setOnAction(e -> Platform.exit());
		dht1.setOnAction(event -> setDHT_ip(dht1ip));
		dht2.setOnAction(event -> setDHT_ip(dht2ip));
		// Assemble Menu
		mainMenu.getMenus().addAll(file, edit);
		file.getItems().addAll(fileOpen, fileQuit);
		edit.getItems().addAll(editSelDHT);
		editSelDHT.getItems().addAll(dht1, dht2);
		selDHTGroup.getToggles().addAll(dht1, dht2);
		
		// Log Area
		logTextArea = new TextArea("Client Started");
		logTextArea.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
		logTextArea.setEditable(false);
		logTextArea.setMinHeight(26);
		logTextArea.setPrefHeight(100);
		
		// Send Message Area
		VBox centerLayout = new VBox();
		HBox sendMessageArea = new HBox();
		Button sendButton = new Button("Send");
		Button clearButton = new Button("Clear");
		TextArea msgInput = new TextArea();
		msgInput.setPrefWidth(400);
		msgInput.setPrefHeight(400);
		// Send Message Logic
		sendButton.setOnAction(e -> dhtSend(msgInput.getText()));
		clearButton.setOnAction(e -> msgInput.setText(""));
		// Assemble Send Message Area
		sendMessageArea.getChildren().addAll(msgInput, sendButton, clearButton);
		centerLayout.getChildren().addAll(sendMessageArea);
		
		// Assemble main layout
		mainLayout.setTop(mainMenu);
		mainLayout.setBottom(logTextArea);
		mainLayout.setCenter(centerLayout);
		
		window.setScene(new Scene(mainLayout, 800, 600));
		window.show();
		
		pushLog("Messenger created on port: " + messenger.getPort());
		
	}
	
	void pushLog(String log) {
		logTextArea.appendText("\n" + log);
	}
	
	void setDHT_ip(String dhtip) {
		pushLog("Switching DHT IP");
		try {
			pushLog(messenger.setRecipientIP(InetAddress.getByName(dhtip)));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			pushLog("Could not switch");
		}
		
	}
	
	//This will block until there is a reply
	void dhtSend(String s) {
		try {
			String reply = messenger.sendMessage(s);
			//System.out.println(reply);
			pushLog(String.format("---==REPLY==---\n%s\n---===END===---", reply));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			pushLog("Could not send message!");
		}
		
	}
	
	@Override
	public void stop() {
		messenger.close();
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
