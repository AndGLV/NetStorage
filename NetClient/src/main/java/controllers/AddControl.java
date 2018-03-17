package controllers;

import controllers.interfaces.AddListenable;
import files.NetFile;
import files.NetTreeFiles;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;

public class AddControl implements AddListenable{
    @FXML private TextField textDirectory;
    @FXML private Button buttonOk;
    @FXML private Button buttonCancel;

    private NetTreeFiles.NetNode currentNode;
    @Override
    public void setCurrentNode(NetTreeFiles.NetNode currentNode) {
        this.currentNode = currentNode;
    }

    @FXML private void initialize(){
        buttonOk.setOnAction(this::btnOk);
        buttonCancel.setOnAction(this::btnCancel);
    }

    private void btnOk(ActionEvent event){
        if (!(textDirectory.getLength() == 0)){
            File newFile = new File(textDirectory.getText());
            NetFile netFile = new NetFile(newFile, currentNode.getValue().getPath());
            netFile.setDirectory(true);
            netFile.setSizeString("");
            currentNode.addChildren(netFile);
            textDirectory.clear();
            btnCancel(event);
        }
    }

    private void btnCancel(ActionEvent event){
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.hide();
    }

}
