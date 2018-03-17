package fileManagersClient.tasks.interfaces;

public interface NetDownloadTaskListenable {
    long getParts();
    void setParts(long parts);
    void setCurrentPart(long currentPart);
    void addData(byte[] actualData);
    void closeStream();
}
