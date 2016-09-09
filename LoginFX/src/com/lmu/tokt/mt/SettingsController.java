package com.lmu.tokt.mt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.lmu.tokt.mt.util.AppConstants;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.paint.ImagePattern;
import javafx.stage.FileChooser;

public class SettingsController implements Initializable {

	final FileChooser fileChooser = new FileChooser();

	@FXML
	private ImageView imgEditBackground, imgEditAvatar, imgRestartServer, imgDisconnect, imgReloadServerIp,
			imgEditServerPort, imgPlayTutorial;

	@FXML
	private Label lblServerPort, lblServerIp;

	@FXML
	private TextField editServerPort;

	private LoginModel mLoginModel;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		mLoginModel = new LoginModel();

		try {

			int port = mLoginModel.getServerPort();
			editServerPort.setText("" + port);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setPublicIPAdress();
	}

	@FXML
	private void onEditBackgroundClicked(MouseEvent event) {

		fileChooser.setTitle("Choose Backgorund Image");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Images", "*.*"),
				new FileChooser.ExtensionFilter("JPG", "*.jpg"), new FileChooser.ExtensionFilter("GIF", "*.gif"),
				new FileChooser.ExtensionFilter("BMP", "*.bmp"), new FileChooser.ExtensionFilter("PNG", "*.png"));

		File file = fileChooser.showOpenDialog(LoginApp.getInstance().getStage());
		if (file != null) {
			setBackgroundImage(file);
			saveImageinDB(file, AppConstants.IMAGE_TYPE_BACKGROUND);
		}
	}

	@FXML
	private void onEditAvatarClicked(MouseEvent event) {

		fileChooser.setTitle("Choose Avatar Image");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Images", "*.*"),
				new FileChooser.ExtensionFilter("JPG", "*.jpg"), new FileChooser.ExtensionFilter("GIF", "*.gif"),
				new FileChooser.ExtensionFilter("BMP", "*.bmp"), new FileChooser.ExtensionFilter("PNG", "*.png"));

		File file = fileChooser.showOpenDialog(LoginApp.getInstance().getStage());
		if (file != null) {
			setAvatarImage(file);
			saveImageinDB(file, AppConstants.IMAGE_TYPE_AVATAR);
		}

	}

	@FXML
	private void onRestartServerClicked(MouseEvent event) {
		LoginController.getInstance().getTCPServer().stopRunning();
	}

	@FXML
	private void onDisconnectClicked(MouseEvent event) {

	}

	@FXML
	private void onReloadServerIpClicked(MouseEvent event) {

	}

	private boolean isServerPortEditable = false;

	@FXML
	private void onEditServerPortClicked(MouseEvent event) {

		if (!isServerPortEditable) {
			isServerPortEditable = true;
			editServerPort.setDisable(false);
			imgEditServerPort.setImage(new Image("drawable/icons/save.png"));
		} else {
			setServerPort();
		}

	}

	@FXML
	private void onEditServerPortKeyPressed(KeyEvent event) {

		if (event.getCode().equals(KeyCode.ENTER)) {
			if (isServerPortEditable) {
				setServerPort();
			}
		}
	}

	private void setServerPort() {

		isServerPortEditable = false;
		editServerPort.setDisable(true);
		imgEditServerPort.setImage(new Image("drawable/icons/edit_blue.png"));

		int port = Integer.parseInt(editServerPort.getText());
		try {
			mLoginModel.updateServerPort(port);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	private void onPlayTutorialClicked(MouseEvent event) {

	}

	private void setBackgroundImage(File file) {

		InputStream is;
		try {
			is = new FileInputStream(file);
			BackgroundImage myBI = new BackgroundImage(new Image(is), BackgroundRepeat.REPEAT,
					BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
			LoginController.getInstance().getRoot().setBackground(new Background(myBI));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// LoginController.getInstance().getRoot()
		// .setStyle("-fx-background-image: url('file://" +
		// file.getAbsolutePath() + "');");
	}

	private void setAvatarImage(File file) {

		InputStream is;
		try {
			is = new FileInputStream(file);
			Image img = new Image(is);
			LoginController.getInstance().getAvatarCircle().setFill(new ImagePattern(img));

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void saveImageinDB(File file, int dbField) {

		try {
			mLoginModel.updateImageInDBSucceeded("Tobias Keinath", file, dbField);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void setPublicIPAdress() {
		InetAddress ip;
		try {

			ip = InetAddress.getLocalHost();
			System.out.println("Current IP address : " + ip.getHostAddress());
			lblServerIp.setText(ip.getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();

		}

	}

}
