package fileManagersClient;

import constants.NetConstants;
import controllers.interfaces.MainForManagers;
import fileManagersClient.interfaces.NetUploadManagerListenable;
import fileManagersClient.tasks.NetUploadTask;
import fileManagersClient.tasks.interfaces.NetUploadTaskListenable;
import files.NetFile;
import files.NetTreeFiles;
import javafx.scene.control.Alert;
import stateApp.interfaces.NetStateListenable;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class NetUploadManager implements NetUploadManagerListenable{
	private NetStateListenable stateManager;
	private ConcurrentHashMap<String, NetUploadTaskListenable> uploadingFiles;
	private ExecutorService threadWorks;
	private MainForManagers mainControl;
	private Alert alert;

	public NetUploadManager(NetStateListenable stateManager, MainForManagers mainControl) {
		this.stateManager = stateManager;
		this.mainControl = mainControl;
		uploadingFiles = new ConcurrentHashMap<>();
		threadWorks = Executors.newFixedThreadPool(NetConstants.MAX_FILES);

	}

    private void addFilesToQueue(List<File> files, String currentDir, List<NetTreeFiles.NetNode> currentNodes){
        for (File file : files) {
            NetFile newNetFile = new NetFile(file, currentDir);
            alert = new Alert(Alert.AlertType.WARNING);
            for (NetTreeFiles.NetNode currentNode : currentNodes) {
                if (currentNode.getValue().getMD5().equals(newNetFile.getMD5())){
                    alert.setTitle("Wrong file name");
                    alert.setContentText(newNetFile.getName() + " already exists!");
                    alert.showAndWait();
                    return;
                }
            }
            for (String s : uploadingFiles.keySet()) {
                if (s.equals(newNetFile.getMD5())){
                    alert.setTitle("Wrong file name");
                    alert.setContentText(newNetFile.getName() + " already downloading!");
                    alert.showAndWait();
                    return;
                }
            }
            NetUploadTaskListenable newTask = new NetUploadTask(stateManager, mainControl, file, newNetFile);
            uploadingFiles.put(newNetFile.getMD5(), newTask);
            threadWorks.execute(newTask);
        }
    }

    @Override
    public void uploadFiles(List<File> files, String currentDir, List<NetTreeFiles.NetNode> currentNodes) {
		addFilesToQueue(files, currentDir, currentNodes);
    }

    @Override
    public NetFile removeAndReturnUploadFile(String md5) {
        return uploadingFiles.remove(md5).getNetFile();
    }

    @Override
    public boolean isActiveUploading() {
        return uploadingFiles.isEmpty();
    }

    @Override
    public void stop() {
        for (NetUploadTaskListenable listenable : uploadingFiles.values()) {
            listenable.setActive(false);
        }
        for (String s : uploadingFiles.keySet()) {
            mainControl.removeProgressBar(s);
        }
        threadWorks.shutdown();
    }

    @Override
    public ConcurrentHashMap<String, NetUploadTaskListenable> getUploadingFiles() {
        return uploadingFiles;
    }
}