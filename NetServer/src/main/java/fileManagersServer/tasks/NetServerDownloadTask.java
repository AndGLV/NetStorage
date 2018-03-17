package fileManagersServer.tasks;

import clients.interfaces.NetHandlerListenable;
import constants.NetConstants;
import fileManagersServer.tasks.interfaces.NetServerDownloadTaskListenable;
import files.NetFile;

import java.io.*;

public class NetServerDownloadTask implements NetServerDownloadTaskListenable {
    private File file;
    private NetFile netFile;
    private NetHandlerListenable handler;

    private boolean isActive;
    private long size;
    private long numberOfParts;
    private long sizeLastPart;

    private long currentSendPart;
    private byte[] arrMD5;
    private byte[] arrNumberOfParts;
    private byte[] arrCurrentSendPart;
    private byte[] arrDataSize;
    private byte[] header;
    private byte[] arr;

    public NetServerDownloadTask(File file, NetFile netFile, NetHandlerListenable handler) {
        this.handler = handler;
        this.file = file;
        this.netFile = netFile;
        this.isActive = false;
        this.currentSendPart = -1;
    }

    @Override
    public void run() {
        System.out.println("START DOWNLOAD " + netFile.getName());
        size = file.length();
        sizeLastPart =  size % NetConstants.SIZE_OF_PART_FILE;
        numberOfParts = size / NetConstants.SIZE_OF_PART_FILE;
        if (sizeLastPart > 0) numberOfParts++;
        isActive = true;

        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            currentSendPart = 1;
            byte[] buffer;
            byte[] header;
            byte[] msg;
            int readByte;

            while (currentSendPart <= numberOfParts && isActive){

                if (currentSendPart != numberOfParts){
                    header = getHeader(netFile.getMD5(), numberOfParts, currentSendPart, NetConstants.SIZE_OF_PART_FILE);
                    buffer = new byte[NetConstants.SIZE_OF_PART_FILE];
                    readByte = bis.read(buffer, 0 , buffer.length);

                    if (readByte == buffer.length){
                        msg = getMsg(header, buffer);
                        handler.sendMessage(msg);
                    }
                    else System.out.println("error read file");

                } else {

                    if (sizeLastPart != 0){
                        header = getHeader(netFile.getMD5(), numberOfParts, currentSendPart, sizeLastPart);
                        buffer = new byte[(int) sizeLastPart];
                        readByte = bis.read(buffer, 0 , buffer.length);

                        if (readByte == buffer.length){
                            msg = getMsg(header, buffer);
                            handler.sendMessage(msg);
                        }
                        else System.out.println("error read file");
                    } else {
                        header = getHeader(netFile.getMD5(), numberOfParts, currentSendPart, NetConstants.SIZE_OF_PART_FILE);
                        buffer = new byte[NetConstants.SIZE_OF_PART_FILE];
                        readByte = bis.read(buffer, 0 , buffer.length);

                        if (readByte == buffer.length){
                            msg = getMsg(header, buffer);
                            handler.sendMessage(msg);
                        }
                        else System.out.println("error read file");
                    }
                }

                currentSendPart++;
            }

            bis.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //isActive = false;
        System.out.println("STOP DOWNLOAD " + netFile.getName());
    }

    synchronized private byte[] getHeader(String md5, long numberOfParts, long currentSendPart, long dataSize){

        arrMD5 = md5.getBytes();
        arrNumberOfParts = Long.toString(numberOfParts).getBytes();
        arrCurrentSendPart = Long.toString(currentSendPart).getBytes();
        arrDataSize = Long.toString(dataSize).getBytes();

        byte fullSize = (byte) (arrMD5.length+
                arrNumberOfParts.length+
                arrCurrentSendPart.length+
                arrDataSize.length + 5);

        header = new byte[fullSize];

        header[0] = fullSize;
        header[1] = (byte) arrMD5.length;

        int numberOfPartPosition = arrMD5.length + 2;
        header[numberOfPartPosition] = (byte) arrNumberOfParts.length;

        int currentSendPartPosition = numberOfPartPosition + arrNumberOfParts.length + 1;
        header[currentSendPartPosition] = (byte) arrCurrentSendPart.length;

        int dataSizePosition = currentSendPartPosition + arrCurrentSendPart.length + 1;
        header[dataSizePosition] = (byte) arrDataSize.length;

        System.arraycopy(arrMD5, 0, header, 2, arrMD5.length);
        System.arraycopy(arrNumberOfParts, 0, header, (numberOfPartPosition+1), arrNumberOfParts.length);
        System.arraycopy(arrCurrentSendPart, 0, header, (currentSendPartPosition+1), arrCurrentSendPart.length);
        System.arraycopy(arrDataSize, 0, header, (dataSizePosition+1), arrDataSize.length);

        return header;
    }

    synchronized private byte[] getMsg(byte[] h, byte[] b){
        int hLength = h.length;
        int bLength = b.length;
        arr = new byte[hLength + bLength];
        System.arraycopy(h, 0, arr, 0, hLength);
        System.arraycopy(b, 0, arr, hLength, bLength);
        return arr;
    }

    @Override
    public void stop() {
        isActive = false;
    }
}
