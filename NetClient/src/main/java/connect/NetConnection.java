package connect;

import connect.interfaces.NetConnectionListenable;
import messages.NetMessage;
import messages.enums.NetMessageType;
import stateApp.enums.NetState;
import stateApp.interfaces.NetStateListenable;
import users.NetUser;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetConnection implements NetConnectionListenable {
	private Socket socket;
	private boolean isConnected = false;
	private Thread prThread;

	private ObjectInputStream objectInput;
	private ObjectOutputStream objectOutput;


	private NetMessage msgInput;
	private byte[] arrIn;


	private NetStateListenable stateManager;

	public NetConnection(NetStateListenable stateManager) {
		this.stateManager = stateManager;
	}

	@Override
	synchronized public void connect(String host, int port) {
		startSocket(host, port);
		if (socket != null && socket.isConnected() && !socket.isClosed()){
			this.isConnected = true;
			stateManager.setState(NetState.AUTH_FORM_CONNECTING);
			startStream();
			startThread();
		}
	}
	private void startSocket(String host, int port){
		try {
			this.socket = new Socket(host, port);
		} catch (IOException e) {
			stateManager.setState(NetState.AUTH_FORM_FAULT_CONNECTING);
		}
	}
	private void startStream(){
		try {
			objectInput = new ObjectInputStream(socket.getInputStream());
			objectOutput = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void startThread(){
		prThread = new Thread(() -> {
			System.out.println("runMessageStream");

			try {
				while (!prThread.isInterrupted() && !this.socket.isClosed()){

				    Object in = objectInput.readObject();
				    if (in instanceof NetMessage){
				        messageProcessor((NetMessage) in);
                    } else if (in instanceof byte[]) {
				        stateManager.addToBuffer((byte[]) in);
                    }

				}
			} catch (IOException e) {
				stateManager.setState(NetState.FAULT_CONNECT_SERVER);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			System.out.println("stopMessageStream");
		});
		prThread.start();
	}

	@Override
	synchronized public void disconnect() {
		stopStream();
		stopThread();
		stopSocket();
		this.isConnected = false;
		stateManager.setState(NetState.AUTH_FORM_DISCONNECTING);
	}
	@Override
	synchronized public void serverDisconnect() {
		stopSocket();
		stopStream();
		stopThread();
		stateManager.setState(NetState.AUTH_FORM_DISCONNECTING);
		this.isConnected = false;
	}
	private void stopStream(){
		if(objectInput != null && objectOutput != null  && !socket.isClosed()){
			try {
				NetMessage msg = new NetMessage(NetMessageType.DISCONNECT_CLIENT, stateManager.getUser());
				sendMessage(msg);
				objectOutput.close();
				objectInput.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void stopThread(){
		if (prThread != null) prThread.interrupt();
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

	@Override
	synchronized public void sendMessage(Object msg) {
		if (!socket.isClosed()){
			try {
				objectOutput.writeObject(msg);
				objectOutput.flush();
				objectOutput.reset();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean isConnected() {
		return isConnected;
	}

	private void messageProcessor(NetMessage msgInput){
		NetMessageType messageType = msgInput.getMessageType();

		if (messageType != null){
			switch (messageType){
				case RESPONSE_AUTHORIZATION:
					responseAuthorization(msgInput);
					break;

				case RESPONSE_REGISTRATION:
					responseRegistration(msgInput);
					break;

				case DISCONNECT_SERVER:
					serverDisconnect();
					break;

                case UPLOAD_FILE_IS_OK:
                    stateManager.uploadFileIsOk(msgInput);
                    break;
			}
		}
	}

	synchronized private void responseAuthorization(NetMessage msg){
		String msgIn = msg.getMsg();
		switch (msgIn) {
			case "isOk":
				NetUser user = (NetUser) msg.getObjMsg();
				if (user != null) {
					stateManager.setUser(user);
					stateManager.setState(NetState.MAIN_FORM_AUTHORIZATION_IS_OK);
				} else stateManager.setState(NetState.AUTH_FORM_AUTHORIZATION_IS_UNKNOW);
				break;

			case "isNotOk":
				stateManager.setState(NetState.AUTH_FORM_AUTHORIZATION_IS_NOT_OK);
				break;

			default:
				stateManager.setState(NetState.AUTH_FORM_AUTHORIZATION_IS_UNKNOW);
				break;
		}
	}

	synchronized private void responseRegistration(NetMessage msg){
		String msgIn = msg.getMsg();

		switch (msgIn){

			case "isOk":
				NetUser user = (NetUser) msg.getObjMsg();
				stateManager.setUser(user);
				stateManager.setState(NetState.MAIN_FORM_AUTHORIZATION_IS_OK);
				break;

			case "isNotOk":
				stateManager.setState(NetState.REG_FORM_REG_IS_NOT_OK);
				break;

			case "isBusy":
				stateManager.setState(NetState.REG_FORM_LOGIN_IS_BUSY);
				break;
		}
	}


}
