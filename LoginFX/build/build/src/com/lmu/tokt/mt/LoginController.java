package com.lmu.tokt.mt;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

import com.lmu.tokt.mt.anim.Shaker;
import com.lmu.tokt.mt.server.TCPServer;
import com.lmu.tokt.mt.server.TCPServer.MessageCallback;
import com.lmu.tokt.mt.util.AppConstants;
import com.lmu.tokt.mt.util.Checksum;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.util.Pair;

public class LoginController implements Initializable {

	private static final String TAG = LoginController.class.getSimpleName();

	/*---------------- FXML FIELDS ----------------*/
	@FXML
	private BorderPane root;
	@FXML
	private AnchorPane anchorPaneContainer, anchorPaneRegister, anchorPaneLogin, anchorPaneChangeUser;

	// root top fields
	@FXML
	private Label lblPublicIP, lblServerPort, lblServerStatus, lblDateTime;
	@FXML
	private ImageView imgSettings, imgLockState;

	// root bottom fields
	@FXML
	private HBox hboxBottomButtons;
	@FXML
	private ImageView imgAddUser, imgChangeUser, imgSleep, imgShutDown;

	// login fields
	@FXML
	private Circle circProfile;
	@FXML
	private Label lblUserName, lblConnectToWatch, lblPassword, lblTyping;
	@FXML
	private ImageView imgWatchConnected, imgPassword, imgTyping, imgConnectedToWatchSuccess, imgCancelConnectToWatch,
			imgPasswordCorrect;
	@FXML
	private Button btnConnectToWatch, btnLogin;
	@FXML
	private PasswordField txtPassword;
	@FXML
	private ProgressIndicator progressConnectToWatch, progressTyping;

	// cues fields
	@FXML
	private Label lblHeartbeat, lblHeartbeatValue, lblProximity, lblProximityValue, lblUserState, lblUserStateValue;
	@FXML
	private ImageView imgHeartBeat, imgProximity, imgUserState;
	@FXML
	private ProgressIndicator progressHeartrateDetection;

	// add user fields
	@FXML
	private Label lblRegistrationFailed, lblRegistrationSuccess, lblCancel;
	@FXML
	private TextField txtUsername;
	@FXML
	private PasswordField txtRegisterPassword, txtRegisterPasswordRepeat;
	@FXML
	private ImageView imgUsernameChecked, imgPasswordChecked, imgRepeatPasswordhecked, imgCancelRegister,
			imgBackRegister;
	@FXML
	private Button btnRegister;

	// Change user fields
	@FXML
	private Label lblCancelChangeUser, lblChangeUserTitle;
	@FXML
	private PasswordField txtChangeUserPassword;
	@FXML
	private TextField txtChangeUsername;
	@FXML
	private Button btnChange;
	@FXML
	private ImageView imgBackChange;

	// Notification
	@FXML
	private AnchorPane anchorNotification;
	@FXML
	private RadioButton radioAgree, radioDisagree, radioUnsure;
	@FXML
	private TextField textFieldComment;
	@FXML
	private Button btnSendFeedback;
	@FXML
	private Label lblNotificationTitle;

	/*---------------- MEMBER FIELDS ----------------*/

	public LoginModel mLoginModel = new LoginModel();

	private TCPServer mTCPServer;

	private static Checksum mChecksum;

	private static LoginController instance;

	public static LoginController getInstance() {
		return instance;
	}

	/*---------------- INIT ----------------*/
	@Override
	public void initialize(URL location, ResourceBundle resources) {

		if (instance == null) {
			instance = this;
		}

		setUpUserSettings();
		setDateTime();
		setPublicIPAdress();
		setServerPort();

		if (mLoginModel.isDBConnected()) {
			System.out.println(TAG + ": DB is connected!");
		} else {
			System.out.println(TAG + ": DB is NOT connected!");
		}

		mChecksum = Checksum.getInstance();

		// start TCP Server
		mTCPServer = new TCPServer(mMessageCallback);
		mTCPServer.start();

		if (LoginApp.getInstance().isUserFromIntro()) {
			showAddUserPane(true);
		}

	}

	// load user settings
	private void setUpUserSettings() {

		// username
		lblUserName.setText(LoginUtil.getInstance().getUsername());

		// Backgorund Image
		BufferedImage bufferedBG = LoginUtil.getInstance().getBackground();
		Image image = null;
		/*
		 * if (bufferedBG != null) {
		 * 
		 * // TODO: NOT WORKING image = SwingFXUtils.toFXImage(bufferedBG,
		 * null); BackgroundImage myBI = new BackgroundImage(image,
		 * BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
		 * BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
		 * root.setBackground(new Background(myBI));
		 * 
		 * } else { // Error root.
		 * setStyle("-fx-background-image: url('file://../../../../drawable/wallpaper/background_3.jpg');"
		 * ); }
		 */

		// Avatar Image
		bufferedBG = LoginUtil.getInstance().getAvatar();
		if (bufferedBG != null) {
			image = SwingFXUtils.toFXImage(bufferedBG, null);
			circProfile.setFill(new ImagePattern(image));
		} else {
			image = new Image("drawable/avatar/leaf.png");
			circProfile.setFill(new ImagePattern(image));
		}

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

	private void setPublicIPAdress() {
		InetAddress ip;
		try {

			ip = InetAddress.getLocalHost();
			lblPublicIP.setText(ip.getHostAddress());
			System.out.println(TAG + ": Current ip address : " + ip.getHostAddress());
		} catch (UnknownHostException e) {
			System.out.println(TAG + ": ERROR getting public ip. Exception: " + e.toString());

		}
	}

	private void setServerPort() {
		try {

			int port = mLoginModel.getServerPort();
			lblServerPort.setText("" + port);
		} catch (SQLException e) {
			System.out.println(TAG + ": ERROR getting server port from DB. Exception: " + e.toString());
		}
	}

	/*---------------- PROCESSING SERVER MESSAGES ----------------*/

	// implements MessageCallback
	private TCPServer.MessageCallback mMessageCallback = new MessageCallback() {

		@Override
		public void callbackMessageReceiver(String message) {
			System.out.println(TAG + ":callbackMessageReceiver() - Message from client: " + message);
		}

		@Override
		public void callbackMessageReceiver(int state, String message) {
			switch (state) {

			case AppConstants.STATE_SERVER_RUNNING:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						lblServerStatus.setText(message);
					}
				});
				break;

			case AppConstants.STATE_SERVER_STOPPED:

				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						// Connection to watch stopped
						lblServerStatus.setText(message);
						resetAllFields();
					}
				});
				break;
			case AppConstants.STATE_PHONE_WATCH_CONNECTED:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						// Successful connection with watch
						setConnectToWatchFields(new Image("drawable/icons/watch/watch_white_1.png"), true, false,
								"Connected", true, false, false);
						startKeyPressDetection();
					}
				});

				break;
			case AppConstants.STATE_NETWORK_ERROR:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						// TODO: reset all fields
						resetAllFields();

						setConnectToWatchFields(new Image("drawable/icons/watch/watch_grey_1.png"), false, true,
								"Connecting...", false, true, false);
					}
				});
				System.out.println(TAG + ": ERROR Failing to connect to Smartwatch/Phone");
				break;
			case AppConstants.STATE_PHONE_WATCH_CONNECTION_CONFIRMED:
				System.out.println(TAG + ": STATE CONFIRM callback");

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						lblConnectToWatch.setText("Connecting... (" + message + ")");
					}
				});
				break;
			case AppConstants.STATE_HEART_BEATING:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						setHeartBeatFields(new Image("drawable/icons/heart/heart_white_1.png"), false, "Heartbeat",
								message);
					}
				});
				break;
			case AppConstants.STATE_HEART_STOPPED:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						// reset Heartbeat fields
						setHeartBeatFields(new Image("drawable/icons/heart/heart_grey_1.png"), false, "Heartbeat",
								"0.0");
					}
				});
				break;
			case AppConstants.STATE_PROXIMITY_DETECTED:

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						lblProximityValue.setText(message.toLowerCase());
						imgProximity.setImage(new Image("drawable/icons/signal/signal_white_1.png"));
					}
				});
				break;
			case AppConstants.STATE_APP_LOCKED:

				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						setLockStateImage(true);
						setAppFullscreen(true);
					}
				});
				break;

			case AppConstants.STATE_APP_UNLOCKED:

				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						setLockStateImage(false);
						setAppFullscreen(false);

						// TODO: createMiniView
					}
				});
				break;
			case AppConstants.STATE_USER_STILL:

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						imgUserState.setImage(new Image("drawable/icons/user_state/standing_white.png"));
						lblUserStateValue.setText(message);
					}
				});

				break;
			case AppConstants.STATE_USER_WALKING:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						imgUserState.setImage(new Image("drawable/icons/user_state/walking_white.png"));
						lblUserStateValue.setText(message);
					}
				});
				break;

			case AppConstants.STATE_USER_NOT_AUTHENTICATED:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						setPasswordFields(new Image("drawable/icons/keyboard/keyboard_grey_1.png"),
								new Image("drawable/icons/key/key_grey_1.png"), true, false, true, false, false, true);

						setHeartBeatFields(new Image("drawable/icons/heart/heart_grey_1.png"), false, "Heartbeat",
								"0.0");
						resetProximityFields();
						resetUserStateFields();

						// stopMeasurement
					}
				});
				break;
			case AppConstants.STATE_USER_AUTHENTICATED:
				System.out.println(TAG + ":" + message);
				break;
			case AppConstants.DIALOG_EVENT_TYPE_LOCK:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if (!anchorNotification.isVisible()) {
							showDialog(message, AppConstants.DIALOG_EVENT_TYPE_LOCK);
						}
					}
				});
				break;
			case AppConstants.DIALOG_EVENT_TYPE_UNLOCK:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if (!anchorNotification.isVisible()) {
							showDialog(message, AppConstants.DIALOG_EVENT_TYPE_UNLOCK);
						}
					}
				});
				break;
			case AppConstants.DIALOG_EVENT_TYPE_NOT_AUTHENTICATED:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if (!anchorNotification.isVisible()) {
							showDialog(message, AppConstants.DIALOG_EVENT_TYPE_NOT_AUTHENTICATED);
						}
					}
				});
				break;

			case AppConstants.STATE_TYPING_SENSORS_STARTED:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						lblTyping.setText("Typing sensors started");
						keypressDetectorReady();
					}
				});
				break;
			case AppConstants.STATE_USER_WAS_TYPING:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						lblTyping.setText("Typing detected");
						login();
					}
				});
				break;
			case AppConstants.STATE_USER_WAS_NOT_TYPING:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						lblTyping.setText("NO typing detected!");
						startKeyPressDetection();
					}
				});
				break;
			default:
				break;
			}
		}
	};

	private void setAppFullscreen(boolean fullscreen) {

		Stage stage = (Stage) root.getScene().getWindow();

		if (fullscreen) {
			stage.setIconified(false);
			stage.setFullScreen(true);
		} else {
			stage.setFullScreen(false);
			stage.toBack();
		}
	}

	private void resetAllFields() {
		setLockStateImage(true);

		setConnectToWatchFields(new Image("drawable/icons/watch/watch_grey_1.png"), false, false, "Not Connected",
				false, false, true);

		setPasswordFields(new Image("drawable/icons/keyboard/keyboard_grey_1.png"),
				new Image("drawable/icons/key/key_grey_1.png"), true, true, true, false, false, true);

		setHeartBeatFields(new Image("drawable/icons/heart/heart_grey_1.png"), false, "Heartbeat", "0.0");
		resetProximityFields();
		resetUserStateFields();
	}

	private void setLockStateImage(boolean isLocked) {
		if (isLocked) {
			imgLockState.setImage(new Image("drawable/icons/lock_state/locked.png"));
		} else {
			imgLockState.setImage(new Image("drawable/icons/lock_state/unlocked.png"));
		}
	}

	private void setPasswordFields(Image keyboardImage, Image keyImage, boolean showPWInput, boolean disablePWInput,
			boolean showLoginBtn, boolean showLblPW, boolean pwCorrect, boolean clear) {

		imgTyping.setImage(keyboardImage);
		imgPassword.setImage(keyImage);
		txtPassword.setVisible(showPWInput);

		if (disablePWInput) {
			txtPassword.requestFocus();
			btnLogin.setDisable(true);
			txtPassword.setDisable(true);
		} else {
			btnLogin.setDisable(true);
			txtPassword.setDisable(false);
		}

		btnLogin.setVisible(showLoginBtn);
		lblPassword.setVisible(showLblPW);
		imgPasswordCorrect.setVisible(pwCorrect);

		if (clear) {
			txtPassword.clear();
		}

	}

	private void setHeartBeatFields(Image img, boolean isInProgress, String heartbeat, String heartbeatValue) {

		if (isInProgress) {
			progressHeartrateDetection.setVisible(true);
			imgHeartBeat.setVisible(false);
		} else {
			progressHeartrateDetection.setVisible(false);
			imgHeartBeat.setVisible(true);
		}

		imgHeartBeat.setImage(img);
		lblHeartbeat.setText(heartbeat);
		lblHeartbeatValue.setText(heartbeatValue);
	}

	private void setConnectToWatchFields(Image img, boolean connectedToWatch, boolean isInProgress,
			String connectionState, boolean connectedSuccess, boolean cancel, boolean showButton) {

		if (isInProgress) {
			progressConnectToWatch.setVisible(true);
			imgWatchConnected.setVisible(false);
		} else {
			progressConnectToWatch.setVisible(false);
			imgWatchConnected.setVisible(true);
		}

		imgWatchConnected.setImage(img);
		imgWatchConnected.setVisible(connectedToWatch);
		lblConnectToWatch.setText(connectionState);
		imgConnectedToWatchSuccess.setVisible(connectedSuccess);
		imgCancelConnectToWatch.setVisible(cancel);
		btnConnectToWatch.setVisible(showButton);
	}

	private void resetProximityFields() {
		imgProximity.setImage(new Image("drawable/icons/signal/signal_grey_1.png"));
		lblProximity.setText("Proximity");
		lblProximityValue.setText("-");

	}

	private void resetUserStateFields() {
		imgUserState.setImage(new Image("drawable/icons/user_state/standing_grey.png"));
		lblUserState.setText("User State");
		lblUserStateValue.setText("-");
	}

	/*----------------LOGIN----------------*/

	@FXML
	private void btnLoginAction(ActionEvent event) {
		login();

	}

	@FXML
	private void onConnectToWatchAction(ActionEvent event) {

		int checksum = mChecksum.calculateChecksum();

		System.out.println(TAG + ": connecting to watch/phone (checksum:" + checksum + ")");

		setConnectToWatchFields(new Image("drawable/icons/watch/watch_grey_1.png"), false, true,
				"Connecting (" + checksum + ") ...", false, true, false);
	}

	private boolean mKeyDetectorstarted = false;

	@FXML
	private void onPasswordFieldKeyPressed(KeyEvent event) {

		if (event.getCode() == KeyCode.ENTER) {
			mKeyDetectorstarted = false;
			mTCPServer.sendMessage(AppConstants.COMMAND_STOP_TYPING_SENSORS);

			// TODO: call after keypressed detector erfolgreich;
			// login();
		}

	}

	@FXML
	private void onPasswordFieldKeyReleased(KeyEvent event) {

		if (txtPassword.getLength() > 0 && !mKeyDetectorstarted && event.getCode() != KeyCode.ENTER) {
			mKeyDetectorstarted = true;
			// TODO sending TIMESTAMP of type starting
			// mTCPServer.sendMessage(AppConstants.COMMAND_START_TYPING_SENSORS);
			// TODO progressindicator & textfield disablen
		}

		if (txtPassword.getLength() == 0) {
			btnLogin.setDisable(true);
		} else {
			btnLogin.setDisable(false);
		}

	}

	private void login() {

		// TODO stop typing sensors

		try {
			if (mLoginModel.isValidCredentials(lblUserName.getText(), txtPassword.getText())) {

				System.out.println(TAG + ": username and password CORRECT");

				setPasswordFields(new Image("drawable/icons/keyboard/keyboard_white_1.png"),
						new Image("drawable/icons/key/key_white_1.png"), false, true, false, true, true, false);
				lblPassword.setText("Password correct");
				setHeartBeatFields(new Image("drawable/icons/heart/heart_white_1.png"), true, "Detect...", "0.0");

				// mTCPServer.setAuthenticated(true);
				// starteService (get Cues: Heartbeat, Proximity, usercontext)
				mTCPServer.sendMessage(AppConstants.COMMAND_USERNAME + "::" + LoginUtil.getInstance().getUsername());
				mTCPServer.sendMessage(AppConstants.COMMAND_UNLOCKED);
				mTCPServer.sendMessage(AppConstants.COMMAND_START_SENDING_SENSORDATA);

			} else {
				System.out.println(TAG + ": username and/or password INCORRECT");
				Shaker shaker = new Shaker(anchorPaneLogin);
				shaker.shake();
				txtPassword.selectAll();
				startKeyPressDetection();
			}
		} catch (SQLException sqle) {
			System.out.println(TAG + ": SQL ERROR login user. Exception: " + sqle.toString());
		}
	}

	@FXML
	private void onCancelConnectToWatchClicked(MouseEvent event) {

		setConnectToWatchFields(new Image("drawable/icons/watch/watch_grey_1.png"), true, false, "Not conneceted",
				false, false, true);

		// TODO killserver, stop server
	}

	private void startKeyPressDetection() {
		progressTyping.setVisible(true);
		imgTyping.setVisible(false);
		txtPassword.setVisible(false);
		lblPassword.setVisible(true);
		lblPassword.setText("Starting keypress detection...");
		mTCPServer.sendMessage(AppConstants.COMMAND_START_TYPING_SENSORS);
	}

	private void keypressDetectorReady() {
		txtPassword.setVisible(true);
		txtPassword.setDisable(false);
		txtPassword.setFocusTraversable(true);
		txtPassword.requestFocus();

		progressTyping.setVisible(false);
		imgTyping.setVisible(true);
		lblPassword.setVisible(false);
		lblPassword.setText("");
	}

	/*----------------ADD USER----------------*/

	@FXML
	private void onAddUserClick(MouseEvent event) {
		showAddUserPane(true);

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

		showAddUserPane(false);

		// reset add user fields
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

	private void showAddUserPane(boolean show) {
		hboxBottomButtons.setVisible(!show);

		anchorPaneLogin.setDisable(show);
		anchorPaneLogin.setVisible(!show);

		anchorPaneRegister.setDisable(!show);
		anchorPaneRegister.setVisible(show);
	}

	private void checkIsPasswordEqual() {
		if (txtRegisterPasswordRepeat.getLength() > 0 && txtRegisterPassword.getLength() > 0
				&& txtRegisterPassword.getText().equals(txtRegisterPasswordRepeat.getText())) {
			imgRepeatPasswordhecked.setVisible(true);
			txtRegisterPassword.setEditable(false);
			txtRegisterPasswordRepeat.setEditable(false);
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
			System.out.println(TAG + ": SQL ERROR resgister new user. Exception: " + e.toString());
		}
	}

	/*----------------CHANGE USER----------------*/

	@FXML
	private void onChangeUserClicked(MouseEvent event) {
		showChangeUserPane(true);
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

	private void showChangeUserPane(boolean show) {

		hboxBottomButtons.setVisible(!show);

		anchorPaneLogin.setDisable(show);
		anchorPaneLogin.setVisible(!show);

		anchorPaneChangeUser.setDisable(!show);
		anchorPaneChangeUser.setVisible(show);
	}

	private void cancelChangeUser() {

		showChangeUserPane(false);

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
				LoginUtil.getInstance().setUsernamne(txtChangeUsername.getText());
				cancelChangeUser();
			} else {

				Shaker shaker = new Shaker(anchorPaneChangeUser);
				shaker.shake();

				lblChangeUserTitle.setText("Incorrect Username or Password!");
				txtChangeUsername.selectAll();
				txtChangeUserPassword.selectAll();
			}
		} catch (SQLException e) {
			System.out.println(TAG + ": SQL ERROR changing user. Exception: " + e.toString());
		}
	}

	/*---------------- SHUTDOWN ----------------*/
	@FXML
	private void onShutDownClicked(MouseEvent event) {

		String[] shutdownCommand = null;
		String operatingSystem = System.getProperty("os.name");

		if ("Mac OS X".equals(operatingSystem)) {
			shutdownCommand = new String[] { "osascript", "-e",
					"tell application \"loginwindow\" to «event aevtrsdn»" };
			System.out.println(shutdownCommand);
		} else if ("Windows".equals(operatingSystem)) {
			shutdownCommand = new String[] { "shutdown -s -t 60" };
		} else {
			throw new RuntimeException("Unsupported operating system.");
		}
		try {
			Runtime.getRuntime().exec(shutdownCommand);
			System.out.println(TAG + ": Shutdown PC ");
		} catch (IOException e) {
			System.out.println(TAG + ": ERROR executing SHUTDOWN command. Exception: " + e.toString());
		}
	}

	/*---------------- SLEEP ----------------*/
	@FXML
	private void onSleepClicked(MouseEvent event) {

		String sleepCommand;
		String operatingSystem = System.getProperty("os.name");

		if ("Linux".equals(operatingSystem) || "Mac OS X".equals(operatingSystem)) {
			sleepCommand = "pmset sleepnow";
		} else if ("Windows".equals(operatingSystem)) {
			sleepCommand = "rundll32.exe powrprof.dll,SetSuspendState 0,1,0";
		} else {
			throw new RuntimeException("Unsupported operating system.");
		}
		try {
			Runtime.getRuntime().exec(sleepCommand);
			System.out.println(TAG + ": Send PC to sleep");
		} catch (IOException e) {
			System.out.println(TAG + ": ERROR executing SLEEP command. Exception: " + e.toString());
		}

	}

	/*---------------- SETTINGS ----------------*/
	@FXML
	private void onSettingsClicked(MouseEvent event) {
		try {
			VBox root = FXMLLoader.load(getClass().getResource("Settings.fxml"));
			PopOver popOver = new PopOver(root);
			popOver.setArrowLocation(ArrowLocation.TOP_RIGHT);
			popOver.setDetached(false);
			popOver.setDetachable(false);
			popOver.show(imgSettings);

		} catch (IOException e) {
			System.out.println(TAG + ": ERROR opening settings. Exception: " + e.toString());
		}

	}

	/*---------------- GETTERS  & SETTERS----------------*/
	public BorderPane getRoot() {
		return root;
	}

	public Circle getAvatarCircle() {
		return circProfile;
	}

	public TCPServer getTCPServer() {
		return mTCPServer;
	}

	public void setAndStartTCPServer() {
		// start TCP Server
		mTCPServer = new TCPServer(mMessageCallback);
		mTCPServer.start();
	}

	public Label getPublicIP() {
		return lblPublicIP;
	}

	public Label getServerPort() {
		return lblServerPort;
	}

	private void showDialog(String headerText, int dialogEventType) {

		anchorNotification.setVisible(true);
		lblNotificationTitle.setText(headerText);

		btnSendFeedback.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				String comment = textFieldComment.getText();
				if (comment.isEmpty()) {
					comment = "none";
				}
				int radioBtnValue = 0;
				if (radioAgree.isSelected()) {
					radioBtnValue = 1;
				}
				if (radioDisagree.isSelected()) {
					radioBtnValue = 2;
				}
				if (radioUnsure.isSelected()) {
					radioBtnValue = 3;
				}

				switch (dialogEventType) {
				case AppConstants.DIALOG_EVENT_TYPE_LOCK:
					mTCPServer.sendMessage(
							AppConstants.COMMAND_SAVE_DIALOG_EVENT_LOCK + "::" + radioBtnValue + "::" + comment);
					break;
				case AppConstants.DIALOG_EVENT_TYPE_UNLOCK:
					mTCPServer.sendMessage(
							AppConstants.COMMAND_SAVE_DIALOG_EVENT_UNLOCK + "::" + radioBtnValue + "::" + comment);
					break;
				case AppConstants.DIALOG_EVENT_TYPE_NOT_AUTHENTICATED:
					mTCPServer.sendMessage(AppConstants.COMMAND_SAVE_DIALOG_EVENT_NOT_AUTHENTICATED + "::"
							+ radioBtnValue + "::" + comment);
					break;

				default:
					break;
				}

				anchorNotification.setVisible(false);

			}
		});

	}

}
