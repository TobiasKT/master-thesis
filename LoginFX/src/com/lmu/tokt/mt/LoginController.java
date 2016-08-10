package com.lmu.tokt.mt;

import java.net.URL;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

import com.lmu.tokt.mt.anim.Shaker;
import com.lmu.tokt.mt.server.Server;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class LoginController implements Initializable {

	@FXML
	private AnchorPane anchorPaneContainer, anchorPaneRegister, anchorPaneLogin, anchorPaneChangeUser;
	@FXML
	private HBox hboxBottomButtons;
	@FXML
	private Label lblUserName, lblDateTime, lblRegistrationSuccess, lblRegistrationFailed, lblCancel,
			lblCancelChangeUser, lblChangeUserTitle, lblAuthenticated;
	@FXML
	private PasswordField txtPassword, txtRegisterPassword, txtRegisterPasswordRepeat, txtChangeUserPassword;
	@FXML
	private TextField txtUsername, txtChangeUsername;
	@FXML
	private Button btnLogin, btnRegister, btnChange;
	@FXML
	private ImageView imgAddUser, imgChangeUser, imgSleep, imgShutDown;
	@FXML
	private ImageView imgWatchConnection, imgSettings;
	@FXML
	private ImageView imgCancelRegister, imgBackRegister, imgBackChange,imgAuthenticatedSucceeded;
	@FXML
	private ImageView imgUsernameChecked, imgPasswordChecked, imgRepeatPasswordhecked;
	@FXML
	private Circle circProfile;

	public LoginModel mLoginModel = new LoginModel();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Image image = new Image("drawable/avatar/leaf.png");
		circProfile.setFill(new ImagePattern(image));
		setDateTime();

		if (mLoginModel.isDBConnected()) {
			System.out.println("DB is connected!");
		} else {
			System.out.println("DB is NOT connected!");
		}

	}

	private void setDateTime() {
		Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				DateFormat dateFormat = new SimpleDateFormat("EEE. HH:mm");
				Calendar cal = Calendar.getInstance();
				lblDateTime.setText(dateFormat.format(cal.getTime()));
			}
		}), new KeyFrame(Duration.seconds(1)));
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}

	/*----------------LOGIN----------------*/

	@FXML
	private void btnLoginAction(ActionEvent event) {
		login(event);
	}

	@FXML
	private void onPasswordFieldKeyPressed(KeyEvent event) {

		if (event.getCode() == KeyCode.ENTER) {
			login(event);
		}

	}

	@FXML
	private void onPasswordFieldKeyReleased(KeyEvent event) {

		if (txtPassword.getLength() == 0) {
			btnLogin.setVisible(false);
		} else {
			btnLogin.setVisible(true);
		}

	}

	private void login(Event event) {
		try {
			if (mLoginModel.isValidCredentials(lblUserName.getText(), txtPassword.getText())) {

				System.out.println("Username and Password is correct!");
				/*
				 * ((Node) (event.getSource())).getScene().getWindow().hide();
				 * 
				 * Parent parent =
				 * FXMLLoader.load(getClass().getResource("Main.fxml")); Stage
				 * stage = new Stage(); Scene scene = new Scene(parent);
				 * stage.setScene(scene); stage.setTitle("Login"); stage.show();
				 */

				Server server = new Server(this);
				server.start();

			} else {
				System.out.println("Username and Password is WRONG!");
				Shaker shaker = new Shaker(anchorPaneLogin);
				shaker.shake();
				txtPassword.selectAll();
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

	/*----------------ADD USER----------------*/

	@FXML
	private void onAddUserClick(MouseEvent event) {

		hboxBottomButtons.setVisible(false);

		anchorPaneLogin.setDisable(true);
		anchorPaneLogin.setVisible(false);

		anchorPaneRegister.setDisable(false);
		anchorPaneRegister.setVisible(true);
	}

	@FXML
	private void onRegUsernameKeyReleased(KeyEvent event) {

		if (txtUsername.getLength() == 0) {
			imgUsernameChecked.setVisible(false);
			btnRegister.setVisible(false);
		} else {
			imgUsernameChecked.setVisible(true);
		}

	}

	@FXML
	private void onRegPasswordKeyReleased(KeyEvent event) {
		checkIsPasswordEqual();
		if (txtRegisterPassword.getLength() == 0) {
			imgPasswordChecked.setVisible(false);
			btnRegister.setVisible(false);
		} else {
			imgPasswordChecked.setVisible(true);
		}
	}

	@FXML
	private void onRegRepeatPasswordKeyReleased(KeyEvent event) {
		checkIsPasswordEqual();
	}

	@FXML
	private void onAddUserKeyPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			if (btnRegister.isVisible()) {
				registerNewUser();
			}
		}
	}

	@FXML
	private void onRegisterAction(ActionEvent event) {
		registerNewUser();
	}

	@FXML
	private void onCancelRegisterClicked(MouseEvent event) {

		hboxBottomButtons.setVisible(true);

		anchorPaneLogin.setDisable(false);
		anchorPaneLogin.setVisible(true);

		anchorPaneRegister.setDisable(true);
		anchorPaneRegister.setVisible(false);

		lblRegistrationFailed.setVisible(false);

		txtUsername.clear();
		txtRegisterPassword.clear();
		txtRegisterPasswordRepeat.clear();
		txtUsername.setDisable(false);
		txtRegisterPassword.setDisable(false);
		txtRegisterPasswordRepeat.setDisable(false);
		imgCancelRegister.setVisible(true);
		imgBackRegister.setVisible(false);
		imgUsernameChecked.setVisible(false);
		imgPasswordChecked.setVisible(false);
		imgRepeatPasswordhecked.setVisible(false);
		btnRegister.setVisible(false);
	}

	private void checkIsPasswordEqual() {
		if (txtRegisterPasswordRepeat.getLength() > 0 && txtRegisterPassword.getLength() > 0
				&& txtRegisterPassword.getText().equals(txtRegisterPasswordRepeat.getText())) {
			imgRepeatPasswordhecked.setVisible(true);
			btnRegister.setVisible(true);
		} else {
			imgRepeatPasswordhecked.setVisible(false);
			btnRegister.setVisible(false);
		}
	}

	private void registerNewUser() {
		try {
			if (mLoginModel.registerUserInDBSucceeded(txtUsername.getText(), txtRegisterPassword.getText())) {
				btnRegister.setVisible(false);
				lblRegistrationSuccess.setVisible(true);
				lblRegistrationFailed.setVisible(false);

				txtUsername.setDisable(true);
				txtRegisterPassword.setDisable(true);
				txtRegisterPasswordRepeat.setDisable(true);

				imgCancelRegister.setVisible(false);
				imgBackRegister.setVisible(true);
				lblCancel.setText("Back");

				lblUserName.setText(txtUsername.getText());
			} else {

				Shaker shaker = new Shaker(anchorPaneRegister);
				shaker.shake();
				lblRegistrationFailed.setVisible(true);
				txtUsername.selectAll();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*----------------CHANGE USER----------------*/

	@FXML
	private void onChangeUserClicked(MouseEvent event) {

		hboxBottomButtons.setVisible(false);

		anchorPaneLogin.setDisable(true);
		anchorPaneLogin.setVisible(false);

		anchorPaneChangeUser.setDisable(false);
		anchorPaneChangeUser.setVisible(true);

	}

	@FXML
	private void onChangeUsernameKeyReleased(KeyEvent event) {
		setBtnChangeInvisibleOrVisible();
	}

	@FXML
	private void onChangePasswordKeyReleased(KeyEvent event) {
		setBtnChangeInvisibleOrVisible();
	}

	@FXML
	private void onChangeAction(ActionEvent event) {
		changeUser();
	}

	@FXML
	private void onChangeUserKeyPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			if (txtChangeUsername.getLength() > 0 && txtChangeUserPassword.getLength() > 0) {
				changeUser();
			}
		}

	}

	@FXML
	private void onCancelChangeUserClicked(MouseEvent event) {
		cancelChangeUser();
	}

	private void cancelChangeUser() {

		hboxBottomButtons.setVisible(true);

		anchorPaneLogin.setDisable(false);
		anchorPaneLogin.setVisible(true);

		anchorPaneChangeUser.setVisible(false);
		anchorPaneChangeUser.setDisable(true);

		txtChangeUsername.clear();
		txtChangeUserPassword.clear();
		lblChangeUserTitle.setText("Change User");
		btnChange.setVisible(false);
	}

	private void setBtnChangeInvisibleOrVisible() {
		if (txtChangeUsername.getLength() > 0 && txtChangeUserPassword.getLength() > 0) {
			btnChange.setVisible(true);
		} else {
			btnChange.setVisible(false);
		}
	}

	private void changeUser() {
		try {
			if (mLoginModel.isValidCredentials(txtChangeUsername.getText(), txtChangeUserPassword.getText())) {
				lblUserName.setText(txtChangeUsername.getText());
				cancelChangeUser();
			} else {

				Shaker shaker = new Shaker(anchorPaneChangeUser);
				shaker.shake();

				lblChangeUserTitle.setText("Incorrect Username or Password!");
				txtChangeUsername.selectAll();
				txtChangeUserPassword.selectAll();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*--------------------------------------*/
	public Label getLblAuthenticated(){
		return lblAuthenticated;
	}
	
	public ImageView getImgAuthenticatedSucceeded(){
		return imgAuthenticatedSucceeded;
	}
	
	public PasswordField getTxtPassword(){
		return txtPassword;
	}
}
