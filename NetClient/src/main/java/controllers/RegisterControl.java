package controllers;

import controllers.interfaces.RegisterListenable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import messages.NetMessage;
import messages.enums.NetMessageType;
import stateApp.enums.NetState;
import stateApp.interfaces.NetStateListenable;
import users.NetUser;

public class RegisterControl implements RegisterListenable {
	@FXML private TextField textCheckWord;
	@FXML private TextField textName;
	@FXML private TextField textLogin;
	@FXML private TextField textSpace;
	@FXML private PasswordField textPassword;
	@FXML private PasswordField textRepeatPass;

	@FXML private Button buttonReg;
	@FXML private Button buttonCancel;

	@FXML private Label labelStatus;

	private NetStateListenable stateManager;

	@FXML
	private void initialize(){
		buttonReg.setOnAction(event -> sendRegisterData());
		buttonCancel.setOnAction(event -> backToAuthorization());
	}

	public RegisterControl(NetStateListenable stateManager) {
		this.stateManager = stateManager;
	}

	private void sendRegisterData(){
		if (checkFieldForNull()){
			String checkWord = textCheckWord.getText();
			String name = textName.getText();
			String login = textLogin.getText();
			String password = textPassword.getText();
			String repeatPass = textRepeatPass.getText();

			boolean checkPass = password.equals(repeatPass);
			int space = 0;

			try {
				space = Integer.parseInt(textSpace.getText());
			} catch (NumberFormatException e) {
				labelStatus.setText("Field \"Space\" should be numeric\n   and not longer " + Integer.MAX_VALUE);
				return;
			}

			if (space > 0){
				if (checkPass){
					NetUser newUser = new NetUser(name, login, checkWord, password, space);
					NetMessage msg = new NetMessage(NetMessageType.REQUEST_REGISTRATION, newUser);
					stateManager.sendMessage(msg);
				} else{
					labelStatus.setText("Fields \"Password\" and \"Repeat password\"\nshould be identically");
				}
			} else {
				labelStatus.setText("Field \"Space\" should be greater 0");
			}
		} else {
			labelStatus.setText("All fields should be fill!");
		}
	}
	private boolean checkFieldForNull(){
		return !(textCheckWord.getLength() == 0 ||
				textName.getLength() == 0 ||
				textLogin.getLength() == 0 ||
				textSpace.getLength() == 0 ||
				textPassword.getLength() == 0 ||
				textRepeatPass.getLength() == 0);

	}

	private void backToAuthorization(){
        stateManager.setState(NetState.REG_FORM_BACK_TO_AUTH);
	}

	@Override
	public void regFormLoginIsBusy() {
		Platform.runLater(()->{
			labelStatus.setText("This login is busy");
		});
	}

	@Override
	public void regFormRegIsNotOk() {
		Platform.runLater(()->{
			labelStatus.setText("Registration failed");
		});
	}
}
