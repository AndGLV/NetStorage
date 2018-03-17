package controllers.interfaces;


public interface AuthListenable {
    void authFormTryConnecting();
	void authFormConnecting();
	void authFormFaultConnecting();
	void authFormDisconnecting();
    void authFormAuthIsUnknow();
    void authFormAuthIsNotOk();
}
