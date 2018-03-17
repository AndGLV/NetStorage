package fileManagersClient.tasks.interfaces;

import files.NetFile;

public interface NetUploadTaskListenable extends Runnable {
    @Override
    void run();

    NetFile getNetFile();

    void setActive(boolean active);
}
