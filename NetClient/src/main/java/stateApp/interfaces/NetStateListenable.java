package stateApp.interfaces;

import javafx.stage.Stage;
import messages.NetMessage;
import netClient.NetClientPreference;
import stateApp.enums.NetState;
import users.NetUser;

import java.io.File;

public interface NetStateListenable {
	void connecting(String host, int port);
	void disconnecting();
	void uploadFileIsOk(NetMessage msg);
    void addToBuffer(byte[] buffer);

	void setState(NetState state);


	void setUser(NetUser user);
	NetUser getUser();

	Stage getMainStage();
	NetClientPreference getPreference();

	void sendMessage(Object msg);

}
