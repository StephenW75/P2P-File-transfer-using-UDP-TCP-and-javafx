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
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Menu;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class Client extends Application{
	
	TextArea logTextArea;
	Stage window;
	Button _B_quit;
	UDP_Messenger messenger;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		/*
		 * NETWORKING STUFF HERE
		 */
		try {
			messenger = new UDP_Messenger();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//IP of DHTs here!
		String dht1ip = "google.com";
		String dht2ip = "reddit.com";
		
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
		RadioMenuItem dht1 = new RadioMenuItem("192.168.bla.1");
		RadioMenuItem dht2 = new RadioMenuItem("192.168.bla.2");
		ToggleGroup selDHTGroup = new ToggleGroup();
		selDHTGroup.getToggles().addAll(dht1, dht2);
		// Menu Logic
		fileQuit.setOnAction(e -> Platform.exit());
		dht1.setOnAction(event -> {
			pushLog("Switching to DHT 1");
			try {
				pushLog(messenger.setDHT_IP(InetAddress.getByName(dht1ip)));
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				pushLog("Could not switch");
			}
		});
		dht2.setOnAction(event -> {
			pushLog("Switching to DHT 2");
			try {
				pushLog(messenger.setDHT_IP(InetAddress.getByName(dht2ip)));
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				pushLog("Could not switch");
			}
		});
		// Putting Menu together
		mainMenu.getMenus().addAll(file, edit);
		file.getItems().addAll(fileOpen, fileQuit);
		edit.getItems().addAll(editSelDHT);
		editSelDHT.getItems().addAll(dht1, dht2);
		
		// Log Area
		logTextArea = new TextArea("Client Started");
		logTextArea.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
		logTextArea.setEditable(false);
		logTextArea.setMinHeight(26);
		logTextArea.setPrefHeight(100);
		
		mainLayout.setTop(mainMenu);
		mainLayout.setBottom(logTextArea);
		
		Scene mainWindow = new Scene(mainLayout, 800, 600);

		window.setScene(mainWindow);
		window.show();
		
	}
	
	
	void pushLog(String log) {
		logTextArea.appendText("\n" + log);
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
