package fileManagersServer.tasks.interfaces;

public interface NetServerUploadTaskListenable {
	void addData(byte[] actualData);
	void setCurrentPart(long currentPart);
	long getParts();
	void setParts(long parts);
	void closeStream();
}
