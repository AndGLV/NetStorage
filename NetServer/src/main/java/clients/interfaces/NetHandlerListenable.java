package clients.interfaces;

import messages.NetMessage;
import users.NetUser;

public interface NetHandlerListenable {
	NetUser getUser();
	void sendMessage(Object msg);
}
