package fileManagersServer.tasks.interfaces;

public interface NetServerDownloadTaskListenable extends Runnable {
    @Override
    void run();

    void stop();
}
