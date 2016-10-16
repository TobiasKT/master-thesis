package com.lmu.tokt.mt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.lmu.tokt.mt.util.AppConstants;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import javafx.stage.Stage;

public class SettingsController implements Initializable {

	private static final String TAG = SettingsController.class.getSimpleName();

	final FileChooser fileChooser = new FileChooser();

	@FXML
	private ImageView imgEditBackground, imgEditAvatar, imgDisconnect, imgReloadServerIp, imgEditServerPort,
			imgPlayTutorial;

	@FXML
	private Label lblServerPort, lblServerIp;

	@FXML
	private TextField editServerPort;

	private LoginModel mLoginModel;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		mLoginModel = new LoginModel();

		setServerPort();
		setPublicIPAdress();
	}

	@FXML
	private void onEditBackgroundClicked(MouseEvent event) {

		String title = "Choose Background Image";
		String dir = "user.home";
		setUpFileChooser(title, dir);

		File file = fileChooser.showOpenDialog(LoginApp.getInstance().getStage());
		if (file != null) {
			setBackgroundImage(file);
			saveImageinDB(file, AppConstants.IMAGE_TYPE_BACKGROUND);
		}
	}

	@FXML
	private void onEditAvatarClicked(MouseEvent event) {

		String title = "Choose Avatar Image";
		String dir = "user.home";
		setUpFileChooser(title, dir);

		File file = fileChooser.showOpenDialog(LoginApp.getInstance().getStage());
		if (file != null) {
			setAvatarImage(file);
			saveImageinDB(file, AppConstants.IMAGE_TYPE_AVATAR);
		}
	}

	@FXML
	private void onDisconnectClicked(MouseEvent event) {

		LoginController.getInstance().getTCPServer().sendMessage(AppConstants.COMMAND_PHONE_WATCH_DISCONNECT);

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
			imgEditServerPort.setImage(new Image("drawable/icons/settings/save.png"));
		} else {
			updateServerPort();
		}

	}

	@FXML
	private void onEditServerPortKeyPressed(KeyEvent event) {

		if (event.getCode().equals(KeyCode.ENTER)) {
			if (isServerPortEditable) {
				updateServerPort();
			}
		}
	}

	private void updateServerPort() {

		isServerPortEditable = false;
		editServerPort.setDisable(true);
		imgEditServerPort.setImage(new Image("drawable/icons/settings/edit_blue.png"));

		int port = Integer.parseInt(editServerPort.getText());
		try {
			LoginController.getInstance().getServerPort().setText("" + port);
			mLoginModel.updateServerPort(port);
		} catch (SQLException e) {
			System.out.println(TAG + ": SQL ERROR updating server port in DB. Exception: " + e.toString());
		}
	}

	@FXML
	private void onPlayTutorialClicked(MouseEvent event) {

		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/lmu/tokt/mt/Intro.fxml"));
			Parent root1 = (Parent) fxmlLoader.load();
			Stage stage = new Stage();
			stage.setScene(new Scene(root1));
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void setBackgroundImage(File file) {

		InputStream is;
		try {
			is = new FileInputStream(file);
			BackgroundImage myBI = new BackgroundImage(new Image(is), BackgroundRepeat.REPEAT,
					BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
			LoginController.getInstance().getRoot().setBackground(new Background(myBI));
		} catch (FileNotFoundException e) {
			System.out.println(TAG + ": FILE NOT FOUND ERROR updating background image. Exception: " + e.toString());
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
			System.out.println(TAG + ": FILE NOT FOUND ERROR updating avater image. Exception: " + e.toString());
		}

	}

	private void saveImageinDB(File file, int dbField) {

		try {
			mLoginModel.updateImageInDBSucceeded("Tobias Keinath", file, dbField);
		} catch (SQLException e) {
			System.out.println(TAG + ": SQL ERROR updating image in DB (" + dbField + "). Exception: " + e.toString());
		}

	}

	private void setPublicIPAdress() {
		InetAddress ip;
		try {

			ip = InetAddress.getLocalHost();
			lblServerIp.setText(ip.getHostAddress());
			System.out.println(TAG + ": Current ip address : " + ip.getHostAddress());
		} catch (UnknownHostException e) {
			System.out.println(TAG + ": ERROR getting public ip. Exception: " + e.toString());

		}
	}

	private void setServerPort() {
		try {

			int port = mLoginModel.getServerPort();
			editServerPort.setText("" + port);
		} catch (SQLException e) {
			System.out.println(TAG + ": ERROR getting server port from DB. Exception: " + e.toString());
		}
	}

	private void setUpFileChooser(String title, String dir) {

		fileChooser.setTitle(title);
		fileChooser.setInitialDirectory(new File(System.getProperty(dir)));
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Images", "*.*"),
				new FileChooser.ExtensionFilter("JPG", "*.jpg"), new FileChooser.ExtensionFilter("GIF", "*.gif"),
				new FileChooser.ExtensionFilter("BMP", "*.bmp"), new FileChooser.ExtensionFilter("PNG", "*.png"));
	}

}
