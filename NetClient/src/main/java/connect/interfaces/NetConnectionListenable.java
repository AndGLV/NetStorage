package connect.interfaces;

import java.io.File;

public interface NetConnectionListenable {
	void connect(String host, int port);
	void disconnect();
	void serverDisconnect();
	boolean isConnected();
	void sendMessage(Object msg);





}
