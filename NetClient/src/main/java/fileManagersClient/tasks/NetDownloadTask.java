package fileManagersClient.tasks;

import constants.NetConstants;
import fileManagersClient.tasks.interfaces.NetDownloadTaskListenable;
import files.NetFile;

import java.io.*;

public class NetDownloadTask implements NetDownloadTaskListenable{
    private File file;
    private NetFile netFile;

    private FileOutputStream fos;
    private BufferedOutputStream bos;

    private long size;
    private long parts;
    private long currentPart;

    public NetDownloadTask(File file, NetFile netFile) {
        this.file = file;
        this.netFile = netFile;
        this.size = netFile.getSize();
        this.currentPart = -1;
        this.parts = -1;
        if (createFile()) startStream();
    }

    private boolean createFile(){
        if (!file.exists()){
            try {
                return file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    private void startStream(){
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos, NetConstants.SIZE_OF_PART_FILE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getParts() {
        return this.parts;
    }

    @Override
    public void setParts(long parts) {
        this.parts = parts;
    }

    @Override
    public void setCurrentPart(long currentPart) {
        this.currentPart = currentPart;
    }

    @Override
    public void addData(byte[] actualData) {
        try {
            bos.write(actualData);
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeStream() {
        try {
            bos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
