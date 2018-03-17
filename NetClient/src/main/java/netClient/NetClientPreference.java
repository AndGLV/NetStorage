package netClient;

import fileManagersClient.tasks.interfaces.NetDownloadTaskListenable;
import fileManagersClient.tasks.interfaces.NetUploadTaskListenable;
import files.NetTreeFiles;
import users.NetUser;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class NetClientPreference implements Serializable {
    private ConcurrentHashMap<String, NetDownloadTaskListenable> downloadingFiles;
    private ConcurrentHashMap<String, NetUploadTaskListenable> uploadFiles;
    private NetTreeFiles netTreeFiles;
    private NetUser user;

    NetClientPreference() {
        downloadingFiles = new ConcurrentHashMap<>();
        uploadFiles = new ConcurrentHashMap<>();
    }

    public void setDownloadingFiles(ConcurrentHashMap<String, NetDownloadTaskListenable> downloadingFiles) {
        this.downloadingFiles.clear();
        this.downloadingFiles = downloadingFiles;
    }

    public void setUploadFiles(ConcurrentHashMap<String, NetUploadTaskListenable> uploadFiles) {
        this.uploadFiles.clear();
        this.uploadFiles = uploadFiles;
    }

    public void setNetTreeFiles(NetTreeFiles netTreeFiles) {
        this.netTreeFiles = netTreeFiles;
    }

    public void setUser(NetUser user) {
        this.user = user;
    }
}
