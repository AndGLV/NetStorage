package fileManagersServer.interfaces;

import files.NetFile;

public interface NetServerUploadManagerListenable {
	void addNewUploadFile(NetFile file);
	void addToBuffer(byte[] buffer);
	void start();
	void stop();
}
