package fileManagersClient.interfaces;

import fileManagersClient.tasks.interfaces.NetDownloadTaskListenable;
import files.NetFile;

import java.util.concurrent.ConcurrentHashMap;

public interface NetDownloadManagerListenable {
    void downloadFile(NetFile file);
    void start();
    void stop();
    void addToBuffer(byte[] buffer);
    boolean isActiveDownloading();
    ConcurrentHashMap<String, NetDownloadTaskListenable> getDownloadingFiles();
}
