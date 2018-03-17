package netClient;

import javafx.application.Application;
import javafx.stage.Stage;
import stateApp.NetStateApp;
import stateApp.enums.NetState;


public class NetClient extends Application{
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage){
	    NetClientPreference preference = new NetClientPreference();
		new NetStateApp(NetState.START_APPLICATION, primaryStage, preference);
	}
}
