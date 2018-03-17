package stateApp;

import connect.NetConnection;
import connect.interfaces.NetConnectionListenable;
import controllers.AuthControl;
import controllers.MainControl;
import controllers.RegisterControl;
import controllers.interfaces.AuthListenable;
import controllers.interfaces.MainListenable;
import controllers.interfaces.RegisterListenable;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import messages.NetMessage;
import netClient.NetClientPreference;
import stateApp.enums.NetState;
import stateApp.enums.NetTypeScene;
import stateApp.interfaces.NetStateListenable;
import users.NetUser;

import java.io.IOException;

public class NetStateApp implements NetStateListenable {
	private AuthListenable authControl;
	private MainListenable mainControl;
	private RegisterListenable regControl;

	private NetConnectionListenable connection;

	private NetState state;
	private NetState currentState;

	private Stage mainStage;

	private Scene authScene;
	private Scene mainScene;
	private Scene regScene;
	private Scene currentScene;

	private NetUser user;

	private Thread prThread;

	private NetClientPreference preference;

    @Override
    synchronized public void setState(NetState state) {
        this.state = state;
    }
    @Override
    synchronized public void setUser(NetUser user) {
        this.user = user;
    }
    @Override
    public NetUser getUser() {
        return this.user;
    }
    @Override
    public Stage getMainStage() {
        return mainStage;
    }

    public NetStateApp(NetState state, Stage mainStage, NetClientPreference preference) {
        this.preference = preference;
		this.state = state;
		this.mainStage = mainStage;
		init(state);
		mainStage.setScene(authScene);
		currentScene = authScene;
		mainStage.centerOnScreen();
		mainStage.setResizable(false);
		mainStage.setTitle("Network storage");

		mainStage.setOnCloseRequest(event -> {
		    if (currentScene == mainScene) mainControl.logOut(false);
			connection.disconnect();
			stopThread();
			Platform.exit();
		});

		mainStage.show();
	}
	private void init(NetState state){
		currentState = state;

		authControl = new AuthControl(this);
		mainControl = new MainControl(this);
		regControl = new RegisterControl(this);

		connection = new NetConnection(this);

		initScene();

		prThread = new Thread(()->{
			System.out.println("State Thread start");

			while (!prThread.isInterrupted()){
				if (this.state != currentState){
					currentState = this.state;
					stateProcessor(currentState);
				}
			}

			System.out.println("State Thread stop");
		});
		prThread.start();
	}
	private void initScene(){
		FXMLLoader authLoader = new FXMLLoader();
		FXMLLoader mainLoader = new FXMLLoader();
		FXMLLoader regLoader = new FXMLLoader();

		authLoader.setLocation(getClass().getResource("/views/AuthView.fxml"));
		mainLoader.setLocation(getClass().getResource("/views/MainViews.fxml"));
		regLoader.setLocation(getClass().getResource("/views/RegisterView.fxml"));

		authLoader.setController(authControl);
		mainLoader.setController(mainControl);
		regLoader.setController(regControl);

		try {
			Parent authWindow = authLoader.load();
			Parent mainWindow = mainLoader.load();
			Parent regWindow = regLoader.load();

			authScene = new Scene(authWindow);
			mainScene = new Scene(mainWindow);
			regScene = new Scene(regWindow);
		} catch (IOException e) {
			System.out.println("Не удалось загрузить fxml");
			e.printStackTrace();
		}
	}
	private void stopThread(){
		if(this.prThread != null) prThread.interrupt();
	}

	private void stateProcessor(NetState state){
		switch (state){

            case AUTH_FORM_TRY_CONNECTING:
                authFormTryConnecting();
                break;

			case AUTH_FORM_CONNECTING:
				authFormConnecting();
				break;

			case AUTH_FORM_FAULT_CONNECTING:
				authFormFaultConnecting();
				break;

			case AUTH_FORM_DISCONNECTING:
				authFormDisconnecting();
				break;

            case FAULT_CONNECT_SERVER:
                faultConnectServer();
                break;

            case MAIN_FORM_AUTHORIZATION_IS_OK:
                mainFormAuthrizationIsOk();
                break;

            case AUTH_FORM_AUTHORIZATION_IS_NOT_OK:
                authFormAuthIsNotOk();
                break;

            case AUTH_FORM_AUTHORIZATION_IS_UNKNOW:
                authFormAuthIsUnknow();
                break;

            case REG_FORM_REGISTERING:
                regFormRegistering();
                break;

            case REG_FORM_BACK_TO_AUTH:
                regFormBackToAuth();
                break;

			case REG_FORM_LOGIN_IS_BUSY:
				regFormLoginIsBusy();
				break;

			case REG_FORM_REG_IS_NOT_OK:
				regFormRegIsNotOk();
				break;
		}
	}

	synchronized private void regFormRegIsNotOk(){
		setScene(NetTypeScene.REGISTRATION);
		regControl.regFormRegIsNotOk();
	}

	synchronized private void regFormLoginIsBusy(){
		setScene(NetTypeScene.REGISTRATION);
		regControl.regFormLoginIsBusy();
	}

	synchronized private void regFormBackToAuth(){
	    if (connection.isConnected()) setState(NetState.AUTH_FORM_CONNECTING);
	    else setState(NetState.AUTH_FORM_DISCONNECTING);
    }

	synchronized private void regFormRegistering(){
	    setScene(NetTypeScene.REGISTRATION);
    }

	synchronized private void authFormAuthIsUnknow(){
		setScene(NetTypeScene.AUTHORIZATION);
	    authControl.authFormAuthIsUnknow();
    }

	synchronized private void authFormAuthIsNotOk(){
		setScene(NetTypeScene.AUTHORIZATION);
        authControl.authFormAuthIsNotOk();
    }

	synchronized private void authFormConnecting(){
		setScene(NetTypeScene.AUTHORIZATION);
		authControl.authFormConnecting();
	}

	synchronized private void authFormFaultConnecting(){
		setScene(NetTypeScene.AUTHORIZATION);
		authControl.authFormDisconnecting();
		authControl.authFormFaultConnecting();
	}

	synchronized private void authFormDisconnecting(){
		setScene(NetTypeScene.AUTHORIZATION);
		authControl.authFormDisconnecting();
	}

	synchronized private void authFormTryConnecting(){
	    setScene(NetTypeScene.AUTHORIZATION);
	    authControl.authFormTryConnecting();
    }

    synchronized private void faultConnectServer(){
	    if (currentScene == authScene){
	        setState(NetState.AUTH_FORM_DISCONNECTING);
	        connection.serverDisconnect();
        } else if (currentScene == regScene){

        } else if (currentScene == mainScene){
            mainControl.logOut(true);
            setState(NetState.AUTH_FORM_DISCONNECTING);
            connection.serverDisconnect();
        }
    }

    synchronized private void mainFormAuthrizationIsOk(){
        setScene(NetTypeScene.MAIN);
        Platform.runLater(()->{
        	if (user != null) this.mainStage.setTitle("Hello " + this.user.getName());
            this.mainStage.setResizable(true);
            this.mainStage.setMaximized(true);
            this.mainStage.centerOnScreen();
        });
        mainControl.mainFormAuthorizationIsOk();
    }

    synchronized private void setScene(NetTypeScene type){
        Platform.runLater(()->{
            switch (type){
                case AUTHORIZATION:
                    if (currentScene != authScene){
                        mainStage.setScene(authScene);
                        currentScene = authScene;
                    }
                    break;

                case REGISTRATION:
                    if (currentScene != regScene){
                        mainStage.setScene(regScene);
                        currentScene = regScene;
                    }
                    break;

                case MAIN:
                    if (currentScene != mainScene){
                        mainStage.setScene(mainScene);
                        currentScene = mainScene;
                    }
                    break;
            }
        });
    }

    @Override
    public void sendMessage(Object msg) {
        connection.sendMessage(msg);
    }

	@Override
	public void connecting(String host, int port) {
		connection.connect(host, port);
	}

    @Override
    public void disconnecting() {
        connection.disconnect();
    }

    @Override
    public void uploadFileIsOk(NetMessage msg) {
        mainControl.uploadFileIsOk(msg);
    }

    @Override
    public void addToBuffer(byte[] buffer) {
        mainControl.addToBuffer(buffer);
    }

    @Override
    public NetClientPreference getPreference() {
        return this.preference;
    }
}
