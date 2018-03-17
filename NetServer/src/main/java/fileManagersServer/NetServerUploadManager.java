package fileManagersServer;

import clients.interfaces.NetHandlerListenable;
import constants.NetConstants;
import fileManagersServer.tasks.NetServerUploadTask;
import files.NetFile;
import fileManagersServer.interfaces.NetServerUploadManagerListenable;
import fileManagersServer.tasks.interfaces.NetServerUploadTaskListenable;
import messages.NetMessage;
import messages.enums.NetMessageType;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetServerUploadManager implements NetServerUploadManagerListenable {

	private ConcurrentHashMap<String, NetServerUploadTaskListenable> currentUploadFiles;
	private ConcurrentLinkedQueue<byte[]> allBuffer;

	private Thread prThread;

	private NetHandlerListenable handler;

    private byte[] tempBuffer;
    private byte[] header;
    private StringBuilder MD5 = new StringBuilder();
    private long numberOfPart;
    private long currentSendPart;
    private long sizeActualData;
    private byte[] actualData;
    private NetServerUploadTaskListenable currentTask;

	public NetServerUploadManager(NetHandlerListenable handler) {
		this.currentUploadFiles = new ConcurrentHashMap<>();
		allBuffer = new ConcurrentLinkedQueue<>();
		this.handler = handler;
	}

	@Override
	public void addNewUploadFile(NetFile file) {
		File newUploadFile = new File(NetConstants.SERVER_FOLDER_PATH + handler.getUser().getFolder() +"\\"+ file.getMD5());
		NetServerUploadTaskListenable newTask = new NetServerUploadTask(file, newUploadFile);
		currentUploadFiles.put(file.getMD5(), newTask);
	}

	@Override
	public void addToBuffer(byte[] buffer) {
		allBuffer.add(buffer);
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

					currentTask = currentUploadFiles.get(MD5.toString());

					if (currentTask.getParts() == -1) currentTask.setParts(numberOfPart);

					currentTask.addData(actualData);
					currentTask.setCurrentPart(currentSendPart);

					if (currentSendPart == numberOfPart){
						currentTask.closeStream();
						currentUploadFiles.remove(MD5.toString());
						NetMessage msg = new NetMessage(NetMessageType.UPLOAD_FILE_IS_OK);
						msg.setMsg(MD5.toString());
						handler.sendMessage(msg);
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
	    if(prThread!=null) prThread.interrupt();
        for (NetServerUploadTaskListenable listenable : currentUploadFiles.values()) {
            listenable.closeStream();
        }
        currentUploadFiles.clear();
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
