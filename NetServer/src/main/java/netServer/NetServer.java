package netServer;

import authorization.NetAuthService;
import clients.NetHandler;
import constants.NetConstants;
import files.NetFile;
import files.NetTreeFiles;
import users.NetUser;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetServer {
	private ServerSocket serverSocket;
	private Socket socket;
	private Thread prThread;
	private NetAuthService netAuthService;

	private CopyOnWriteArrayList<NetHandler> netHandlers;

	public static void main(String[] args) {
		new NetServer();
	}

	private NetServer(){
		Scanner scanner = new Scanner(System.in);
		String command;
		netHandlers = new CopyOnWriteArrayList<>();
		netAuthService = new NetAuthService();

		do{

			command = scanner.nextLine();

			switch (command){
				case "start":
					serverStart();
					break;

				case "stop":
					serverStop();
					break;

				default:
					System.out.println("Invalid command");
					break;
			}
		} while(!command.equals("stop"));
	}

	private void serverStart(){
		startSocket();
		startThread();
		System.out.println("Server is starting on PORT: " + NetConstants.SERVER_PORT);
	}
	private void startSocket(){
		try {
			serverSocket = new ServerSocket(NetConstants.SERVER_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void startThread(){
		prThread = new Thread(()->{
			System.out.println("AcceptThread start");

			try {
				while (!prThread.isInterrupted() && !serverSocket.isClosed()){
					socket = serverSocket.accept();
					NetHandler handler = new NetHandler(this.socket, this, netAuthService);
					netHandlers.add(handler);
					System.out.println("client connected");
				}
			} catch (IOException e){
				//System.out.println("serverSocket closed");
				//e.printStackTrace();
			} finally {
				try {
					serverSocket.close();
					//System.out.println("serverSocket closed_finally");
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}

			System.out.println("AcceptThread stop");
		});
		prThread.start();
	}

	private void serverStop(){
		stopHandlers();
		stopSocket();
		stopThread();
		System.out.println("Server stop");
	}
	private void stopHandlers(){
		for (NetHandler netHandler : netHandlers) {
			netHandler.stopNetHandler();
		}
	}
	private void stopSocket(){
		if (serverSocket != null && !serverSocket.isClosed()){
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void stopThread(){
		if (prThread != null) prThread.interrupt();
	}

	public void disconnectClient(NetHandler handler, NetUser user){
		if (user != null) System.out.println("Client " + user.getLogin() + " is disconnected");
		else System.out.println("Unknown client disconnected");
		netHandlers.remove(handler);
	}
}
