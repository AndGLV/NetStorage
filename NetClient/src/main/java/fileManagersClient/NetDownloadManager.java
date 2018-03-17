package fileManagersClient;

import constants.NetConstants;
import controllers.interfaces.MainForManagers;
import fileManagersClient.interfaces.NetDownloadManagerListenable;
import fileManagersClient.tasks.NetDownloadTask;
import fileManagersClient.tasks.interfaces.NetDownloadTaskListenable;
import files.NetFile;
import javafx.stage.FileChooser;
import messages.NetMessage;
import messages.enums.NetMessageType;
import stateApp.interfaces.NetStateListenable;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetDownloadManager implements NetDownloadManagerListenable {
    private NetStateListenable stateManager;
    private MainForManagers mainControl;
    private FileChooser fileChooser;
    private ConcurrentHashMap<String, NetDownloadTaskListenable> currentDownloadFiles;
    private ConcurrentLinkedQueue<byte[]> allBuffer;
    private Thread prThread;

    private byte[] tempBuffer;
    private byte[] header;
    private StringBuilder MD5 = new StringBuilder();
    private long numberOfPart;
    private long currentSendPart;
    private long sizeActualData;
    private byte[] actualData;
    private NetDownloadTaskListenable currentTask;

    public NetDownloadManager(NetStateListenable stateManager, MainForManagers mainControl) {
        this.stateManager = stateManager;
        this.mainControl = mainControl;
        this.currentDownloadFiles = new ConcurrentHashMap<>();
        this.allBuffer = new ConcurrentLinkedQueue<>();
        this.fileChooser = new FileChooser();
        fileChooser.setTitle("Download directory");
        fileChooser.setInitialDirectory(new File(NetConstants.CLIENT_FOLDER_PATH + "\\DOWNLOADING"));
    }

    @Override
    public void downloadFile(NetFile netFile) {
        fileChooser.setInitialFileName(netFile.getName());
        File saveFile = fileChooser.showSaveDialog(stateManager.getMainStage());
        if (saveFile != null){
            NetDownloadTaskListenable newTask = new NetDownloadTask(saveFile, netFile);
            currentDownloadFiles.put(netFile.getMD5(), newTask);
            mainControl.addNewProgressBar(netFile.getPath(), netFile.getMD5(), "DOWNLOADING___");
            NetMessage msg = new NetMessage(NetMessageType.DOWNLOADING_FILE, netFile);
            stateManager.sendMessage(msg);
        }
    }

    @Override
    public void start() {
        prThread = new Thread(()->{
            System.out.println("FileManagerStart");

            while (!prThread.isInterrupted()){


                if (!allBuffer.isEmpty()){

                    tempBuffer = allBuffer.poll();

                    header = getHeader(tempBuffer);
                    MD5.setLength(0);
                    MD5.append(getMD5(header));
                    numberOfPart = getLongData(header, "numberOfPart");
                    currentSendPart = getLongData(header, "currentSendPart");
                    sizeActualData = getLongData(header, "actualData");

                    actualData = new byte[(int) sizeActualData];
                    System.arraycopy(tempBuffer, (header.length+1), actualData, 0, actualData.length);

                    currentTask = currentDownloadFiles.get(MD5.toString());

                    if (currentTask.getParts() == -1) currentTask.setParts(numberOfPart);

                    currentTask.addData(actualData);
                    currentTask.setCurrentPart(currentSendPart);

                    mainControl.updateProgressBar(MD5.toString(), (double) currentSendPart / (double) numberOfPart);

                    if (currentSendPart == numberOfPart){
                        currentTask.closeStream();
                        currentDownloadFiles.remove(MD5.toString());
                        mainControl.removeProgressBar(MD5.toString());
                        System.gc();
                    }
                }
            }
            System.out.println("FileManagerStop");
        });
        prThread.start();
    }

    @Override
    public void stop() {
        if (prThread!=null) prThread.interrupt();
        for (String s : currentDownloadFiles.keySet()) {
            mainControl.removeProgressBar(s);
        }
        for (NetDownloadTaskListenable listenable : currentDownloadFiles.values()) {
            listenable.closeStream();
        }
    }

    @Override
    public void addToBuffer(byte[] buffer) {
        allBuffer.add(buffer);
    }

    @Override
    public boolean isActiveDownloading() {
        return currentDownloadFiles.isEmpty();
    }

    @Override
    public ConcurrentHashMap<String, NetDownloadTaskListenable> getDownloadingFiles() {
        return currentDownloadFiles;
    }

    private byte[] getHeader(byte[] buffer){
        byte sizeHeader = buffer[0];
        byte[] header = new byte[sizeHeader-1];
        System.arraycopy(buffer, 1, header, 0, header.length);
        return header;
    }
    private String getMD5(byte[] header){
        byte sizeMD5 = header[0];
        byte[] md5 = new byte[sizeMD5];
        System.arraycopy(header, 1, md5, 0, md5.length);
        return new String(md5);
    }
    private long getLongData(byte[] header, String kind){
        byte sizeMD5 = header[0];

        byte sizeNumberOfParts = header[sizeMD5 + 1];
        int positionNumberOfParts = sizeMD5 + 2;

        byte sizeCurrentSendPart = header[positionNumberOfParts + sizeNumberOfParts];
        int positionCurrentSendPart = positionNumberOfParts + sizeNumberOfParts + 1;

        byte sizeActualData = header[positionCurrentSendPart + sizeCurrentSendPart];
        int positionActualData = positionCurrentSendPart + sizeCurrentSendPart + 1;

        switch (kind){
            case "numberOfPart":
                byte[] numberOfPart = new byte[sizeNumberOfParts];
                System.arraycopy(header, positionNumberOfParts, numberOfPart, 0, numberOfPart.length);
                return Long.parseLong(new String(numberOfPart));

            case "currentSendPart":
                byte[] currentSendPart = new byte[sizeCurrentSendPart];
                System.arraycopy(header, positionCurrentSendPart, currentSendPart, 0, currentSendPart.length);
                return Long.parseLong(new String(currentSendPart));

            case "actualData":
                byte[] actualData = new byte[sizeActualData];
                System.arraycopy(header, positionActualData, actualData, 0, actualData.length);
                return Long.parseLong(new String(actualData));

            default:
                return -1;
        }
    }
}
