package fileManagersServer.interfaces;

import files.NetFile;

public interface NetServerDownloadManagerListenable {
    void addDownloadFile(NetFile file);
    void stop();
}
