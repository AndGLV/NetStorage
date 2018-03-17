package fileManagersClient.interfaces;


import fileManagersClient.tasks.interfaces.NetUploadTaskListenable;
import files.NetFile;
import files.NetTreeFiles;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public interface NetUploadManagerListenable {
    void uploadFiles(List<File> files, String currentDir, List<NetTreeFiles.NetNode> currentNodes);
    NetFile removeAndReturnUploadFile(String md5);
    boolean isActiveUploading();
    void stop();
    ConcurrentHashMap<String, NetUploadTaskListenable> getUploadingFiles();
}
