package controllers;

import constants.NetConstants;
import controllers.interfaces.AddListenable;
import controllers.interfaces.MainForManagers;
import controllers.interfaces.MainListenable;
import fileManagersClient.NetDownloadManager;
import fileManagersClient.NetUploadManager;
import fileManagersClient.interfaces.NetUploadManagerListenable;
import fileManagersClient.tasks.interfaces.NetDownloadTaskListenable;
import fileManagersClient.tasks.interfaces.NetUploadTaskListenable;
import files.NetFile;
import files.NetTreeFiles;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import messages.NetMessage;
import messages.enums.NetMessageType;
import stateApp.enums.NetState;
import stateApp.interfaces.NetStateListenable;
import users.NetUser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MainControl implements MainListenable, MainForManagers {

	@FXML private TableView<NetFile> tableViewTreeFiles;
	@FXML private TableColumn<NetFile, String> columName;
	@FXML private TableColumn<NetFile, String> columPath;
	@FXML private TableColumn<NetFile, String> columSize;

	@FXML private TextArea textFileInfo;

	@FXML private Button buttonDownload;
	@FXML private Button buttonUpload;
	@FXML private Button buttonAddDirectory;
	@FXML private Button buttonLogOut;
	@FXML private Button buttonDelete;

	@FXML private Label labelcurrentPath;

	@FXML private BorderPane mainBorderPane;

	private NetStateListenable stateManager;
	private NetUser currentUser;
	private NetTreeFiles treeFiles;
	private NetTreeFiles.NetNode currentNode;

	private Stage addFolderStage;
	private AddListenable addControl;

	private ObservableList<NetFile> currentFileList = FXCollections.observableArrayList();

	private FileChooser fileChooser;
	private NetUploadManagerListenable uploadManager;
	private NetDownloadManager downloadManager;

	private ConcurrentHashMap<String, VBox> currentProgressBar;

	@FXML
	private void initialize(){
		columName.setCellValueFactory(new PropertyValueFactory<NetFile, String>("name"));
		columPath.setCellValueFactory(new PropertyValueFactory<NetFile, String>("path"));
		columSize.setCellValueFactory(new PropertyValueFactory<NetFile, String>("sizeString"));
		tableViewTreeFiles.setOnMouseClicked(this::onClickMouseList);
		buttonUpload.setOnAction(event -> uploadFile());
		buttonDownload.setOnAction(event -> downloadFile());
		buttonAddDirectory.setOnAction(event -> addDirectory());
		buttonLogOut.setOnAction(event -> logOut(false));
		buttonDelete.setOnAction(event -> btnDelete());
	}

	public MainControl(NetStateListenable stateManager) {
		this.stateManager = stateManager;
		this.fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(NetConstants.CLIENT_FOLDER_PATH));
		this.currentProgressBar = new ConcurrentHashMap<>();
		initAddDialog();
	}

	private void btnDelete(){
        NetFile downFile = tableViewTreeFiles.getSelectionModel().getSelectedItem();
        if (downFile != null){
            currentNode.deleteChildren(downFile);
            update(currentNode);
        }
    }

	@Override
	public void logOut(boolean serverDisc){
	    if (serverDisc) {
            stopManagers();
            savePreference();
            Platform.runLater(()->{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Server disconnecting");
                alert.showAndWait();
            });
        } else if (!uploadManager.isActiveUploading() || !downloadManager.isActiveDownloading()){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm logout");
            alert.setContentText("Active downloads/uploads available! \nContinue?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && (result.get() == ButtonType.OK)){
                sendDisconnectMessage();
                stopManagers();
                savePreference();
                showAuthForm();
            }
        } else {
            sendDisconnectMessage();
            stopManagers();
            showAuthForm();
        }
    }
    private void stopManagers(){
        downloadManager.stop();
        uploadManager.stop();
    }
    private void savePreference(){
        ConcurrentHashMap<String, NetDownloadTaskListenable> downloadingFiles = downloadManager.getDownloadingFiles();
        ConcurrentHashMap<String, NetUploadTaskListenable> uploadFiles = uploadManager.getUploadingFiles();
        stateManager.getPreference().setDownloadingFiles(downloadingFiles);
        stateManager.getPreference().setUploadFiles(uploadFiles);
        stateManager.getPreference().setNetTreeFiles(treeFiles);
        stateManager.getPreference().setUser(currentUser);
    }
    private void showAuthForm(){
        stateManager.getMainStage().setMaximized(false);
        stateManager.getMainStage().setResizable(false);
        stateManager.setState(NetState.AUTH_FORM_CONNECTING);
    }
    private void sendDisconnectMessage(){
        NetMessage msg = new NetMessage(NetMessageType.USER_LOGOUT, currentUser);
        stateManager.sendMessage(msg);
    }

	private void initAddDialog(){
	    addFolderStage = new Stage();
	    addFolderStage.setTitle("Add directory");
	    addFolderStage.setResizable(false);
	    addFolderStage.initModality(Modality.WINDOW_MODAL);
	    addFolderStage.initOwner(stateManager.getMainStage());

        addControl = new AddControl();
        FXMLLoader addLoader = new FXMLLoader();
        addLoader.setLocation(getClass().getResource("/views/addDirectory.fxml"));
        addLoader.setController(addControl);

        Scene addScene;

        try {
            Parent parent = addLoader.load();
            addScene = new Scene(parent);
            addFolderStage.setScene(addScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addDirectory(){
	    addControl.setCurrentNode(currentNode);
	    addFolderStage.showAndWait();
	    update(currentNode);
    }

	private void uploadFile(){
	    List<File> files = fileChooser.showOpenMultipleDialog(stateManager.getMainStage());
	    if (files!=null && !files.isEmpty()) uploadManager.uploadFiles(files, currentNode.getValue().getPath(), currentNode.getChildrens());
    }

    private void downloadFile(){
	    NetFile downFile = tableViewTreeFiles.getSelectionModel().getSelectedItem();
	    if (downFile != null && downFile.isFileRet()) downloadManager.downloadFile(downFile);
    }

	private void onClickMouseList(MouseEvent event){
		switch (event.getClickCount()){
			case 1:
				showFileInfo();
				break;

			case 2:
				choiceNetFile();
				break;
		}
	}

	private void showFileInfo(){
		NetFile tempFile = tableViewTreeFiles.getSelectionModel().getSelectedItem();
		if (tempFile != null){
			textFileInfo.clear();
			textFileInfo.appendText(tempFile.toString());
		}
	}

	private void choiceNetFile(){
		NetFile choiceFile = tableViewTreeFiles.getSelectionModel().getSelectedItem();
		if (choiceFile != null && choiceFile.isDirectory()) update(treeFiles.getNodeFromValue(choiceFile));
	}

    private void update(NetTreeFiles.NetNode parent){
        currentFileList.clear();

        List<NetTreeFiles.NetNode> currentChildren = parent.getChildrens();

        if (parent.getParent() != null){
            currentFileList.add(parent.getParent().getValue());
        }

        for (NetTreeFiles.NetNode currentChild : currentChildren) {
            currentFileList.add(currentChild.getValue());
        }




        tableViewTreeFiles.setItems(currentFileList);


        /*tableViewTreeFiles.setRowFactory(param -> new TableRow<NetFile>(){
            @Override
            protected void updateItem(NetFile item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("-fx-background-color: LIGHTCORAL;");
            }
        });*/


        currentNode = parent;
        labelcurrentPath.setText(currentNode.getValue().getPath());
    }

    @Override
    public void removeProgressBar(String md5) {
        Platform.runLater(()->{
            VBox vBoxBottom = (VBox) mainBorderPane.getBottom();
            vBoxBottom.getChildren().remove(currentProgressBar.remove(md5));
        });
    }

	@Override
	public void mainFormAuthorizationIsOk() {
        uploadManager = new NetUploadManager(this.stateManager, this);
        downloadManager = new NetDownloadManager(this.stateManager, this);
        downloadManager.start();
	    this.treeFiles = new NetTreeFiles("root");
	    update(treeFiles.getRoot());
		/*this.currentUser = stateManager.getUser();
		this.treeFiles = currentUser.getTreeFiles();
		//testingData();
		update(treeFiles.getRoot());*/

	}

	@Override
	synchronized public void addNewProgressBar(String path, String md5, String type){
		Platform.runLater(()->{
			VBox vBox = new VBox();
			Label newProgressLabel = new Label();
			ProgressBar newProgress = new ProgressBar(0.0d);

			newProgressLabel.setText(type + path);
			newProgress.setPrefWidth(mainBorderPane.getWidth());
			newProgress.setPadding(new Insets(0,0,10,0));

			vBox.getChildren().add(newProgressLabel);
			vBox.getChildren().add(newProgress);

			currentProgressBar.put(md5, vBox);

			VBox vBoxBottom = (VBox) mainBorderPane.getBottom();
			vBoxBottom.getChildren().add(vBox);
		});
	}

	@Override
	synchronized public void updateProgressBar(String md5, double progress) {
		Platform.runLater(()->{
			VBox vBox = currentProgressBar.get(md5);
			if (vBox!=null){
                ProgressBar pb = (ProgressBar) vBox.getChildren().get(1);
                pb.setProgress(progress);
            }
		});
	}

    @Override
    public void uploadFileIsOk(NetMessage msg) {
        String MD5 = msg.getMsg();
        if (MD5 != null){
            removeProgressBar(MD5);

            NetFile nf = uploadManager.removeAndReturnUploadFile(MD5);
            NetTreeFiles.NetNode folNode = treeFiles.getNodeFromPath(nf.getFolder());

            folNode.addChildren(nf);

            update(currentNode);

            System.gc();
        }
    }

    @Override
    public void addToBuffer(byte[] buffer) {
        downloadManager.addToBuffer(buffer);
    }
}
