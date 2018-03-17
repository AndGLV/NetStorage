package clients;

import authorization.NetAuthService;
import clients.interfaces.NetHandlerListenable;
import fileManagersServer.NetServerDownloadManager;
import fileManagersServer.interfaces.NetServerDownloadManagerListenable;
import files.NetFile;
import fileManagersServer.NetServerUploadManager;
import fileManagersServer.interfaces.NetServerUploadManagerListenable;
import messages.NetMessage;
import messages.enums.NetMessageType;
import netServer.NetServer;
import users.NetUser;

import java.io.*;
import java.net.Socket;

public class NetHandler implements NetHandlerListenable{

	private Socket socket;
	private NetServer netServer;
	private NetAuthService netAuthService;

	private Thread prThread;

	private ObjectInputStream objectInput;
	private ObjectOutputStream objectOutput;

	private NetMessage msgInput;
	private byte[] byteInput;
    private Object in;

	private NetUser netUser;

	private NetServerUploadManagerListenable uploadManager;
	private NetServerDownloadManagerListenable dowloadManager;

	public NetHandler(Socket socket, NetServer netServer, NetAuthService netAuthService) {
		this.socket = socket;
		this.netServer = netServer;
		this.netAuthService = netAuthService;
		this.uploadManager = new NetServerUploadManager(this);
		this.dowloadManager = new NetServerDownloadManager(this);
        uploadManager.start();

		startStream();
		startThread();
	}
	private void startStream(){
		try {
			objectOutput = new ObjectOutputStream(this.socket.getOutputStream());
			objectInput = new ObjectInputStream(this.socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void startThread(){
		prThread = new Thread(()->{
			System.out.println("StartHandler " + prThread.getName());

			try {

				while (!prThread.isInterrupted() && !socket.isClosed()){
					in = objectInput.readObject();
					if (in instanceof NetMessage){
						msgInput = (NetMessage) in;
						messageProcessor(msgInput);
					} else if (in instanceof byte[]){
					    byteInput = (byte[]) in;
                        uploadManager.addToBuffer(byteInput);
					}
				}

			} catch (IOException e) {
				netServer.disconnectClient(this, this.netUser);
				stopServerNetHandler();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}


			System.out.println("StopHandler " + prThread.getName());
		});
		prThread.start();
	}

	public void stopNetHandler(){
		NetMessage msg = new NetMessage(NetMessageType.DISCONNECT_SERVER);
		sendMessage(msg);
        uploadManager.stop();
        dowloadManager.stop();
		stopThread();
		stopStream();
		stopSocket();
	}
	private void stopStream(){
		if (objectInput != null && objectOutput != null && !socket.isClosed()){

			try {
				objectInput.close();
				objectOutput.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void stopSocket(){
		if (socket != null){
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void stopThread(){
		if (prThread != null) prThread.interrupt();
	}
	private void stopServerNetHandler(){
        uploadManager.stop();
        dowloadManager.stop();
		stopStream();
		stopSocket();
		stopThread();
	}

	@Override
	public NetUser getUser() {
		return netUser;
	}

	synchronized private void disconnectClient(NetMessage msg){
		if ( (msg.getObjMsg()) != null ){
			this.netUser = (NetUser) msg.getObjMsg();
			netServer.disconnectClient(this, this.netUser);
			stopServerNetHandler();
		} else {
			netServer.disconnectClient(this,null);
			stopServerNetHandler();
		}
	}

	@Override
	synchronized public void sendMessage(Object msg){
		try {
			objectOutput.writeObject(msg);
			objectOutput.flush();
			objectOutput.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void messageProcessor(NetMessage msgInput){
		NetMessageType messageType = msgInput.getMessageType();

		if (messageType != null){
			switch (messageType){
				case REQUEST_REGISTRATION:
					requestRegistration(msgInput);
					break;

				case REQUEST_AUTHORIZATION:
					requestAuthorization(msgInput);
					break;

				case DISCONNECT_CLIENT:
					disconnectClient(msgInput);
					break;

				case UPLOADING_FILE:
					uploadingFile(msgInput);
					break;

                case DOWNLOADING_FILE:
                    downloadingFile(msgInput);
                    break;

                case USER_LOGOUT:
                    userLogOut(msgInput);
                    break;

				default:
					break;
			}
		}
	}

	synchronized private void userLogOut(NetMessage msg){
	    uploadManager.stop();
	    dowloadManager.stop();
	    NetUser user = (NetUser) msg.getObjMsg();
	    netAuthService.saveTreeFiles(user.getTreeFiles(), user.getLogin());
    }

	synchronized private void downloadingFile(NetMessage msg){
	    NetFile downFile = (NetFile) msg.getObjMsg();
	    if (downFile != null)dowloadManager.addDownloadFile(downFile);
    }

    synchronized private void uploadingFile(NetMessage msg){
        NetFile newUploadFile = (NetFile) msg.getObjMsg();
        if (newUploadFile != null) uploadManager.addNewUploadFile(newUploadFile);
    }

	synchronized private void requestRegistration(NetMessage msg){
		NetUser newUser = (NetUser) msg.getObjMsg();
		NetMessage msgOutput = new NetMessage(NetMessageType.RESPONSE_REGISTRATION);

		if (newUser != null){
			if (netAuthService.requestReg(newUser)){
				this.netUser = netAuthService.getUser(newUser.getLogin());

				if(this.netUser != null){
					msgOutput.setMsg("isOk");
					msgOutput.setObjMsg(this.netUser);
				} else {
					msgOutput.setMsg("isNotOk");
				}
			} else {
				msgOutput.setMsg("isBusy");
			}

			sendMessage(msgOutput);
		}
	}

	synchronized private void requestAuthorization(NetMessage msg){
		netUser = (NetUser) msg.getObjMsg();
		NetMessage msgOutput = new NetMessage(NetMessageType.RESPONSE_AUTHORIZATION);
		if (netUser != null){

			if ( netAuthService.requestAuth(netUser.getLogin(), netUser.getPassword()) ){
				netUser = netAuthService.getUser(netUser.getLogin());
				if (netUser != null){
					msgOutput.setMsg("isOk");
					msgOutput.setObjMsg(netUser);
				} else msgOutput.setMsg("isNotOk");
			} else {
				msgOutput.setMsg("isNotOk");
			}

			sendMessage(msgOutput);
		}
	}
}
