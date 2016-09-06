package com.lmu.tokt.mt;

import java.net.URL;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

import com.lmu.tokt.mt.anim.HeartBeatAnimation;
import com.lmu.tokt.mt.anim.Shaker;
import com.lmu.tokt.mt.server.TCPServer;
import com.lmu.tokt.mt.server.TCPServer.MessageCallback;
import com.lmu.tokt.mt.util.AppConstants;
import com.lmu.tokt.mt.util.Checksum;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController implements Initializable {

	@FXML
	private BorderPane root;
	@FXML
	private AnchorPane anchorPaneContainer, anchorPaneRegister, anchorPaneLogin, anchorPaneChangeUser;
	@FXML
	private HBox hboxBottomButtons;
	@FXML
	private Label lblUserName, lblDateTime, lblRegistrationSuccess, lblRegistrationFailed, lblCancel,
			lblCancelChangeUser, lblChangeUserTitle, lblConnectToWatch, lblServerStatus, lblPassword, lblHeartbeat,
			lblHeartbeatValue, lblProximity;
	@FXML
	private PasswordField txtPassword, txtRegisterPassword, txtRegisterPasswordRepeat, txtChangeUserPassword;
	@FXML
	private TextField txtUsername, txtChangeUsername;
	@FXML
	private Button btnLogin, btnRegister, btnChange, btnConnectToWatch;
	@FXML
	private ImageView imgAddUser, imgChangeUser, imgSleep, imgShutDown, imgWatchConnected, imgCancelConnectToWatch,
			imgPassword, imgConnectedToWatchSuccess, imgPasswordCorrect, imgHeartBeat, imgHeartBeatDetected,
			imgProximity, imgLockState;
	@FXML
	private ImageView imgSettings;
	@FXML
	private ImageView imgCancelRegister, imgBackRegister, imgBackChange;
	@FXML
	private ImageView imgUsernameChecked, imgPasswordChecked, imgRepeatPasswordhecked;
	@FXML
	private Circle circProfile;
	@FXML
	private ProgressIndicator progressConnectToWatch, progressHeartrateDetection;

	public LoginModel mLoginModel = new LoginModel();

	private TCPServer mTCPServer;

	private static Checksum mChecksum;

	private HeartBeatAnimation mHeartBeatAnimation;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		Image image = new Image("drawable/avatar/leaf.png");
		circProfile.setFill(new ImagePattern(image));
		setDateTime();

		imgHeartBeat.setFitHeight(24.0);
		imgHeartBeat.setFitWidth(24.0);
		//mHeartBeatAnimation = new HeartBeatAnimation(imgHeartBeat);

		if (mLoginModel.isDBConnected()) {
			System.out.println("DB is connected!");
		} else {
			System.out.println("DB is NOT connected!");
		}

		mChecksum = Checksum.getInstance();
		// start TCP Server
		mTCPServer = new TCPServer(mMessageCallback);
		mTCPServer.start();
	}

	// set current date and time
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

	// implements MessageCallback
	private TCPServer.MessageCallback mMessageCallback = new MessageCallback() {

		@Override
		public void callbackMessageReceiver(String message) {
			System.out.println("Message from Client: " + message);
		}

		@Override
		public void callbackMessageReceiver(int state, String message) {
			switch (state) {

			case AppConstants.STATE_SERVER_RUNNING:

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						lblServerStatus.setText("Server running (" + message + ")");
					}
				});

				break;

			case AppConstants.STATE_SERVER_STOPPED:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						// Connection to watch stopped
						lblServerStatus.setText("Server running (" + message + ")");
						btnConnectToWatch.setVisible(true);
						txtPassword.setDisable(false);
						imgWatchConnected.setImage(new Image("drawable/icons/no_watch.png"));
						imgWatchConnected.setVisible(true);
						imgConnectedToWatchSuccess.setVisible(false);
						imgCancelConnectToWatch.setVisible(false);
						progressConnectToWatch.setVisible(false);

					}
				});

				break;
			case AppConstants.STATE_CONNECTED:
				System.out.println("Successfully connected to SmartWatch!");

				// Update UI
				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						// Successful connection with watch
						lblConnectToWatch.setText("Connected");
						imgConnectedToWatchSuccess.setVisible(true);
						progressConnectToWatch.setVisible(false);
						imgWatchConnected.setImage(new Image("drawable/icons/watch.png"));
						imgWatchConnected.setVisible(true);
						imgCancelConnectToWatch.setVisible(false);
						txtPassword.setDisable(false);
						txtPassword.setFocusTraversable(true);
						btnConnectToWatch.setVisible(false);

						txtPassword.requestFocus();
					}
				});

				break;
			case AppConstants.ERROR:

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						// retry to connect with watch
						lblConnectToWatch.setText("Retry connecting...");
						imgConnectedToWatchSuccess.setVisible(false);
						progressConnectToWatch.setVisible(true);
						imgWatchConnected.setVisible(false);
						imgCancelConnectToWatch.setVisible(true);
						txtPassword.setDisable(true);
						btnConnectToWatch.setVisible(false);

						// reset PW Input
						imgPassword.setImage(new Image("drawable/icons/no_key.png"));
						txtPassword.setDisable(true);
						txtPassword.setVisible(true);
						btnLogin.setVisible(true);
						lblPassword.setVisible(false);
						imgPasswordCorrect.setVisible(false);
						txtPassword.clear();

						// reset HeartBeat
						//mHeartBeatAnimation.stopAnimation();
						imgHeartBeat.setImage(new Image("drawable/icons/no_heartbeat.png"));
						imgHeartBeatDetected.setVisible(false);
						progressHeartrateDetection.setVisible(false);
						lblHeartbeat.setText("No Heartbeat");
						
						//reset Poximity
						lblProximity.setText("-");
						imgProximity.setImage(new Image("drawable/icons/no_proximity.png"));
						
						imgLockState.setImage(new Image("drawable/icons/locked.png"));
					}
				});
				System.out.println("Failing to connect to SmartWatch!");
				break;
			case AppConstants.STATE_CONFIRM:

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						lblConnectToWatch.setText("Connecting... (" + message + ")");
					}
				});
				break;
			case AppConstants.STATE_HEART_BEAT_DETECTED:

				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						progressHeartrateDetection.setVisible(false);
						imgHeartBeat.setVisible(true);
						imgHeartBeat.setImage(new Image("drawable/icons/heartbeat.png"));
						//mHeartBeatAnimation.startAnimation();
						imgHeartBeatDetected.setVisible(true);
						// display value
						lblHeartbeat.setText("Heartbeat detected");
					}
				});

				break;

			case AppConstants.STATE_HEART_BEATING:

				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						lblHeartbeatValue.setText(message);
					}
				});
				break;
			case AppConstants.STATE_HEART_STOPPED:

				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						imgLockState.setImage(new Image("drawable/icons/locked.png"));
						
						// reset HeartBeat
						//mHeartBeatAnimation.stopAnimation();
						imgHeartBeat.setImage(new Image("drawable/icons/no_heartbeat.png"));
						progressHeartrateDetection.setVisible(false);
						imgHeartBeatDetected.setVisible(false);
						lblHeartbeat.setText("No Heartbeat");
						lblHeartbeatValue.setText("(0.0)");

						// reset PW Input
						imgPassword.setImage(new Image("drawable/icons/no_key.png"));
						txtPassword.setVisible(true);
						txtPassword.requestFocus();
						txtPassword.setDisable(false);
						btnLogin.setVisible(true);
						lblPassword.setVisible(false);
						imgPasswordCorrect.setVisible(false);
						txtPassword.clear();
						
						//reset Poximity
						lblProximity.setText("-");
						imgProximity.setImage(new Image("drawable/icons/no_proximity.png"));
					}
				});

				break;
			case AppConstants.STATE_PROXIMITY:

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						lblProximity.setText(message.toUpperCase());
						imgProximity.setImage(new Image("drawable/icons/proximity.png"));
					}
				});
				break;
			case AppConstants.STATE_LOCKED:

				// TODO: MAXIMIZE APP

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						imgLockState.setImage(new Image("drawable/icons/locked.png"));

						Stage stage = (Stage) root.getScene().getWindow();
						stage.setIconified(false);
						stage.setFullScreen(true);
					}
				});
				break;

			case AppConstants.STATE_UNLOCKED:

				// TODO: MINIMIZE APP

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						imgLockState.setImage(new Image("drawable/icons/unlocked.png"));

						Stage stage = (Stage) root.getScene().getWindow();
						stage.setFullScreen(false);
						//stage.toBack();

						// TODO: createMiniView
						// stage.setIconified(true);
					}
				});
				break;
			default:
				break;
			}
		}
	};

	/*----------------LOGIN----------------*/

	@FXML
	private void btnLoginAction(ActionEvent event) {
		login(event);

	}

	@FXML
	private void onConnectToWatchAction(ActionEvent event) {

		lblConnectToWatch.setText("Connecting... (" + mChecksum.calculateChecksum() + ")");
		btnConnectToWatch.setVisible(false);
		progressConnectToWatch.setVisible(true);
		progressConnectToWatch.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
		imgCancelConnectToWatch.setVisible(true);
		imgWatchConnected.setVisible(false);
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
			btnLogin.setDisable(true);
		} else {
			btnLogin.setDisable(false);
		}

	}

	private void login(Event event) {
		try {
			if (mLoginModel.isValidCredentials(lblUserName.getText(), txtPassword.getText())) {

				System.out.println("Username and Password is correct!");
				imgPassword.setImage(new Image("drawable/icons/key.png"));
				txtPassword.setDisable(true);
				txtPassword.setVisible(false);
				btnLogin.setVisible(false);
				lblPassword.setVisible(true);
				imgPasswordCorrect.setVisible(true);

				progressHeartrateDetection.setVisible(true);
				imgHeartBeat.setVisible(false);
				lblHeartbeat.setText("Detect heartrate ...");

				// starteService (get Cues: Heartbeat, Proximity, usercontext)
				mTCPServer.sendMessage(AppConstants.COMMAND_GET_CUES);

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

	@FXML
	private void onCancelConnectToWatchClicked(MouseEvent event) {
		// Stop server
		// TODO RESET Method
		lblConnectToWatch.setText("Not connected");
		progressConnectToWatch.setVisible(false);
		imgWatchConnected.setImage(new Image("drawable/icons/no_watch.png"));
		imgWatchConnected.setVisible(true);
		imgCancelConnectToWatch.setVisible(false);
		btnConnectToWatch.setVisible(true);

		// TODO killserver
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

}
