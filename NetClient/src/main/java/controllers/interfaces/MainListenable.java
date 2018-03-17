package controllers.interfaces;

import messages.NetMessage;

public interface MainListenable {
	void mainFormAuthorizationIsOk();
    void uploadFileIsOk(NetMessage msg);
    void addToBuffer(byte[] buffer);
    void logOut(boolean serverDisconnect);
}
