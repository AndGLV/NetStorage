package fileManagersServer;

import clients.interfaces.NetHandlerListenable;
import constants.NetConstants;
import fileManagersServer.interfaces.NetServerDownloadManagerListenable;
import fileManagersServer.tasks.NetServerDownloadTask;
import fileManagersServer.tasks.interfaces.NetServerDownloadTaskListenable;
import files.NetFile;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetServerDownloadManager implements NetServerDownloadManagerListenable{

    private ConcurrentHashMap<String, NetServerDownloadTaskListenable> currentDownloadFiles;
    private ExecutorService threadWorks;
    private NetHandlerListenable handler;

    public NetServerDownloadManager(NetHandlerListenable handler) {
        this.handler = handler;
        this.currentDownloadFiles = new ConcurrentHashMap<>();
        threadWorks = Executors.newFixedThreadPool(NetConstants.MAX_FILES);
    }

    @Override
    public void addDownloadFile(NetFile netFile) {
        File serverFile = new File(NetConstants.SERVER_FOLDER_PATH + "\\" + handler.getUser().getFolder());
        File[] files = serverFile.listFiles();
        File file = (files != null && files.length > 0) ? searchFileToMd5(netFile, files) : null;
        if (file != null){
            NetServerDownloadTaskListenable newTask = new NetServerDownloadTask(file, netFile, handler);
            currentDownloadFiles.put(netFile.getMD5(), newTask);
            threadWorks.execute(newTask);
        }
    }

    @Override
    public void stop() {
        for (NetServerDownloadTaskListenable listenable : currentDownloadFiles.values()) {
            listenable.stop();
        }
        threadWorks.shutdown();
    }

    private File searchFileToMd5(NetFile src, File[] arr){
        for (File anArr : arr) {
            if (anArr.getName().equals(src.getMD5())) return anArr;
        }
        return null;
    }
}
