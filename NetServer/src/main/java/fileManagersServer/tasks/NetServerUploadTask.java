package fileManagersServer.tasks;

import constants.NetConstants;
import files.NetFile;
import fileManagersServer.tasks.interfaces.NetServerUploadTaskListenable;

import java.io.*;

public class NetServerUploadTask implements NetServerUploadTaskListenable {
	private NetFile netFile;
	private File file;

	private FileOutputStream fos;
	private BufferedOutputStream bos;

	private long size;
	private long parts;
	private long currentPart;

	public NetServerUploadTask(NetFile netFile, File file) {
		this.netFile = netFile;
		this.file = file;
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
	public void addData(byte[] actualData) {
		try {
			bos.write(actualData);
			bos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setCurrentPart(long currentPart) {
		this.currentPart = currentPart;
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
	public void closeStream() {
		try {
			bos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
