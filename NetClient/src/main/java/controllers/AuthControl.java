package controllers;

import controllers.interfaces.AuthListenable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import messages.NetMessage;
import messages.enums.NetMessageType;
import stateApp.enums.NetState;
import stateApp.interfaces.NetStateListenable;
import users.NetUser;

public  class  AuthControl implements AuthListenable {
	@FXML private Button buttonForgot;
	@FXML private Button buttonConnect;
	@FXML private Button buttonLogIn;
	@FXML private Button buttonDisconnect;
	@FXML private Button buttonSignIn;

	@FXML private ChoiceBox choiseUserName;

	@FXML private PasswordField textPassword;
	@FXML private TextField textPort;
	@FXML private TextField textHost;
	@FXML private TextField textUserName;

	@FXML private Label labelConnectStatus;
	@FXML private CheckBox checkRememberMe;

	private String host;
	private int port;

	private NetStateListenable stateManager;


	public AuthControl(NetStateListenable stateManager) {
		this.stateManager = stateManager;
	}

	@FXML
	private void initialize(){
		buttonConnect.setOnAction(event -> connectToServer());
		buttonDisconnect.setOnAction(event -> disconnect());
		buttonLogIn.setOnAction(event -> logIn());
		buttonSignIn.setOnAction(event -> register());
	}

	private void connectToServer(){
	    stateManager.setState(NetState.AUTH_FORM_TRY_CONNECTING);
		host = textHost.getText();
		try {
			port = Integer.parseInt(textPort.getText());
			stateManager.connecting(host, port);
		} catch (NumberFormatException e) {
			labelConnectStatus.setText("Invalid PORT!");
		}
	}

	@FXML
	private void logIn() {
        String login = textUserName.getText();
        String password = textPassword.getText();

        NetUser user = new NetUser();
        user.setLogin(login);
        user.setPassword(password);

        NetMessage msgOutput = new NetMessage(NetMessageType.REQUEST_AUTHORIZATION, user);
        stateManager.sendMessage(msgOutput);
	}

	@FXML
	private void register(){
        stateManager.setState(NetState.REG_FORM_REGISTERING);
	}

	@FXML
	private void disconnect(){
		stateManager.disconnecting();
	}

	@Override
	synchronized public void authFormConnecting() {
		buttonForgot.setDisable(false);
		buttonConnect.setDisable(true);
		buttonLogIn.setDisable(false);
		buttonDisconnect.setDisable(false);
		buttonSignIn.setDisable(false);

		textPassword.setDisable(false);
		textUserName.setDisable(false);
		textHost.setDisable(true);
		textPort.setDisable(true);

		checkRememberMe.setDisable(false);

		Platform.runLater(()->{
			labelConnectStatus.setText("Connected to host " + host + ", port " + port);
		});
	}

    @Override
    synchronized public void authFormDisconnecting() {
        buttonForgot.setDisable(true);
        buttonConnect.setDisable(false);
        buttonLogIn.setDisable(true);
        buttonDisconnect.setDisable(true);
        buttonSignIn.setDisable(true);

        textPassword.setDisable(true);
        textPassword.clear();
        textUserName.setDisable(true);
        textUserName.clear();
        textHost.setDisable(false);
        textPort.setDisable(false);

        checkRememberMe.setDisable(true);

        Platform.runLater(()->{
            labelConnectStatus.setText("No connected");
        });
    }

    @Override
    synchronized public void authFormFaultConnecting() {
        Platform.runLater(()->{
            labelConnectStatus.setText("Connected failed");
        });
    }

    @Override
    public void authFormTryConnecting() {
        Platform.runLater(()->{
            labelConnectStatus.setText("Try connecting to " + host + "/" + port);
        });
    }

    @Override
    public void authFormAuthIsUnknow() {
        Platform.runLater(()->{
            labelConnectStatus.setText("Authorization is unknown");
        });
    }

    @Override
    public void authFormAuthIsNotOk() {
        Platform.runLater(()->{
            labelConnectStatus.setText("Wrong login or password");
        });
    }
}
