package fileManagersClient.tasks;

import constants.NetConstants;
import controllers.interfaces.MainForManagers;
import fileManagersClient.tasks.interfaces.NetUploadTaskListenable;
import files.NetFile;
import messages.NetMessage;
import messages.enums.NetMessageType;
import stateApp.interfaces.NetStateListenable;

import java.io.*;

public class NetUploadTask implements NetUploadTaskListenable {
	private NetStateListenable stateManager;
	private MainForManagers mainControl;
	private File file;
	private NetFile netFile;

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

	public NetUploadTask(NetStateListenable stateManager, MainForManagers mainControl, File file, NetFile netFile) {
		this.stateManager = stateManager;
		this.mainControl = mainControl;
		this.file = file;
		this.netFile = netFile;
		this.isActive = false;
	}

	@Override
	public void run() {
        System.out.println("START UPLOAD " + netFile.getName());
        size = netFile.getSize();
		sizeLastPart =  size % NetConstants.SIZE_OF_PART_FILE;
		numberOfParts = size / NetConstants.SIZE_OF_PART_FILE;
		if (sizeLastPart > 0) numberOfParts++;

		NetMessage message = new NetMessage(NetMessageType.UPLOADING_FILE, netFile);
		stateManager.sendMessage(message);

		mainControl.addNewProgressBar(netFile.getPath(), netFile.getMD5(), "UPLOADING____");
		isActive = true;

		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis, NetConstants.SIZE_OF_PART_FILE);

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
						stateManager.sendMessage(msg);
					}
					else System.out.println("error read file");

				} else {

					if (sizeLastPart != 0){
						header = getHeader(netFile.getMD5(), numberOfParts, currentSendPart, sizeLastPart);
						buffer = new byte[(int) sizeLastPart];
						readByte = bis.read(buffer, 0 , buffer.length);

						if (readByte == buffer.length){
							msg = getMsg(header, buffer);
							stateManager.sendMessage(msg);
						}
						else System.out.println("error read file");
					} else {
						header = getHeader(netFile.getMD5(), numberOfParts, currentSendPart, NetConstants.SIZE_OF_PART_FILE);
						buffer = new byte[NetConstants.SIZE_OF_PART_FILE];
						readByte = bis.read(buffer, 0 , buffer.length);

						if (readByte == buffer.length){
							msg = getMsg(header, buffer);
							stateManager.sendMessage(msg);
						}
						else System.out.println("error read file");
					}
				}

				currentSendPart++;
				mainControl.updateProgressBar(netFile.getMD5(), (double)currentSendPart/(double)numberOfParts);
			}


			bis.close();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//isActive = false;
        System.out.println("STOP UPLOAD " + netFile.getName());
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
	public NetFile getNetFile() {
        return netFile;
    }

    @Override
    public void setActive(boolean active) {
        this.isActive = active;
    }
}
