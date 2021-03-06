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
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

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
	private ImageView imgSettings;

	// root bottom fields
	@FXML
	private HBox hboxBottomButtons;
	@FXML
	private ImageView imgAddUser, imgChangeUser, imgSleep, imgShutDown;
	@FXML
	private Label lblLog;

	// login fields
	@FXML
	private Circle circProfile;
	@FXML
	private Label lblUserName, lblConnectToWatch, lblPassword, lblTyping, lblLockState, lblLoginState;
	@FXML
	private ImageView imgLockState, imgLoginStateState, imgWatchConnected, imgPassword, imgTyping,
			imgConnectedToWatchSuccess, imgStartingKeyDetectorSuccess, imgCancelStartKeyDetection,
			imgCancelConnectToWatch, imgPasswordCorrect;
	@FXML
	private Button btnConnectToWatch, btnLogin, btnStartTypingDetector;
	@FXML
	private PasswordField txtPassword;
	@FXML
	private ProgressIndicator progressConnectToWatch, progressTyping;

	// cues fields
	@FXML
	private Label lblHeartbeat, lblHeartbeatValue, lblProximity, lblProximityValue, lblUserState, lblUserStateValue,
			lblInformation;
	@FXML
	private ImageView imgHeartBeat, imgProximity, imgUserState;
	@FXML
	private ProgressIndicator progressHeartrateDetection, progressProximity;
	@FXML
	private HBox hboxInfobox;

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

	private FadeTransition animation = new FadeTransition(Duration.millis(1000));

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

		// initAnimation
		animation.setFromValue(1.0);
		animation.setToValue(0.3);
		animation.setCycleCount(Timeline.INDEFINITE);
		animation.setAutoReverse(true);
		animation.setNode(lblInformation);

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
		} catch (UnknownHostException e) {
			System.out.println(TAG + ": ERROR getting public ip. Exception: " + e.toString());

		}
	}

	private void setServerPort() {
		try {

			int port = mLoginModel.getServerPort();
			lblServerPort.setText("" + port);
		} catch (SQLException e) {
			System.out.println(TAG + ": ERROR getting server port from DB.Exception: " + e.toString());
		}
	}

	/*---------------- PROCESSING SERVER MESSAGES ----------------*/

	// implements MessageCallback
	private TCPServer.MessageCallback mMessageCallback = new MessageCallback() {

		@Override
		public void callbackMessageReceiver(String message) {
			// System.out.println(TAG + ":callbackMessageReceiver() - Message
			// from client: " + message);
		}

		@Override
		public void callbackMessageReceiver(int state, String message) {
			switch (state) {

			case AppConstants.STATE_SERVER_RUNNING:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						lblInformation.setText(message);
					}
				});
				break;
			case AppConstants.STATE_PHONE_WATCH_DISCONNECTED:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						resetAllFields();
						setAppFullscreen(true);
						lblInformation.setText(message);

						Checksum.getInstance().resetChecksum();
					}
				});
				// System.out.println(TAG + ": ERROR Failing to connect to
				// Smartwatch/Phone");
				break;
			case AppConstants.STATE_PHONE_WATCH_CONNECTED:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						// Successful connection with watch
						setConnectToWatchFields(mBlackWatchImg, false, "Connected", true);
						enableTypingDetectorFields(true);
						lblInformation.setText(message);
					}
				});
				break;
			case AppConstants.STATE_HEART_BEATING:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						setHeartBeatFields(mWhiteHeartImg, false, "Heartbeat", message);
					}
				});
				break;
			case AppConstants.STATE_HEART_STOPPED:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						// reset Heartbeat fields
						setHeartBeatFields(mGreyHeartImg, false, "Heartbeat", "0.0");
					}
				});
				break;
			case AppConstants.STATE_PROXIMITY_DETECTED:

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						setProximityFields(mWhiteSignalImg, false, "Proximity", message.toLowerCase());
					}
				});
				break;
			case AppConstants.STATE_APP_LOCKED:

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						setLockStateFields(true);
						setAppFullscreen(true);
					}
				});
				break;

			case AppConstants.STATE_APP_UNLOCKED:

				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						setLockStateFields(false);
						setAppFullscreen(false);

						// TODO: createMiniView
					}
				});
				break;
			case AppConstants.STATE_USER_STILL:

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						imgUserState.setImage(mWhiteStandingImg);
						lblUserStateValue.setText(message);
					}
				});

				break;
			case AppConstants.STATE_USER_WALKING:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						imgUserState.setImage(mWhiteWalkingImg);
						lblUserStateValue.setText(message);
					}
				});
				break;

			case AppConstants.STATE_USER_NOT_AUTHENTICATED:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						setAppFullscreen(true);
						setLockStateFields(true);
						setLoginStateFields(false);

						setPasswordFields(mGreyKeyImg, true, true, false, true);
						setHeartBeatFields(mGreyHeartImg, false, "Heartbeat", "0.0");
						setProximityFields(mGreySignalImg, false, "Proximity", "-");
						resetUserStateFields();

						setTypingDetectorFields(mGreyKeyboardImg, false, "Typing-Detector off", false, false);
						enableTypingDetectorFields(true);

						lblInformation.setText(message);
						animation.play();

					}
				});
				break;
			case AppConstants.STATE_USER_AUTHENTICATED:
				// System.out.println(TAG + ":" + message);
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
						keypressDetectorReady();
					}
				});
				break;
			case AppConstants.STATE_USER_WAS_TYPING:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if (!onStoppingTypingSensorClicked) {
							setTypingDetectorFields(mBlackKeyboardImg, false, "Typing detected", true, false);
							login();
						}
						onStoppingTypingSensorClicked = false;
					}
				});
				break;
			case AppConstants.STATE_USER_WAS_NOT_TYPING:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if (!onStoppingTypingSensorClicked) {
							setPasswordFields(mGreyKeyImg, true, true, false, true);
							setTypingDetectorFields(mGreyKeyboardImg, false, "No typing detected", false, false);
							enableTypingDetectorFields(true);
							lblInformation.setText("Are you wearing the watch?");
							animation.play();
						}
						onStoppingTypingSensorClicked = false;
					}
				});
				break;
			default:
				break;
			}
		}
	};

	/*---------------- SET UI Fields----------------*/

	private Image mLockedImg = new Image("drawable/icons/lock_state/locked.png");
	private Image mUnlockedImg = new Image("drawable/icons/lock_state/unlocked.png");

	private Image mLoginImg = new Image("drawable/icons/login_state/login.png");
	private Image mLogoutImg = new Image("drawable/icons/login_state/logout.png");

	private Image mGreyWatchImg = new Image("drawable/icons/watch/watch_grey_1.png");
	private Image mBlackWatchImg = new Image("drawable/icons/watch/wristwatch_black.png");

	private Image mGreyKeyboardImg = new Image("drawable/icons/keyboard/keyboard_grey_1.png");
	private Image mBlackKeyboardImg = new Image("drawable/icons/keyboard/keyboard_black.png");

	private Image mGreyKeyImg = new Image("drawable/icons/key/key_grey_1.png");
	private Image mBlackKeyImg = new Image("drawable/icons/key/key_black.png");

	private Image mGreyHeartImg = new Image("drawable/icons/heart/heart_grey_1.png");
	private Image mWhiteHeartImg = new Image("drawable/icons/heart/heart_white_1.png");

	private Image mGreySignalImg = new Image("drawable/icons/signal/signal_grey_1.png");
	private Image mWhiteSignalImg = new Image("drawable/icons/signal/signal_white_1.png");

	private Image mGreyStandingImg = new Image("drawable/icons/user_state/standing_grey.png");
	private Image mWhiteStandingImg = new Image("drawable/icons/user_state/standing_white.png");
	private Image mWhiteWalkingImg = new Image("drawable/icons/user_state/walking_white.png");

	public void setAppFullscreen(boolean fullscreen) {

		Stage stage = (Stage) root.getScene().getWindow();

		if (fullscreen) {
			stage.setIconified(false);
			stage.setFullScreen(true);
			stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
		} else {
			stage.setFullScreen(false);
		}
	}

	public void resetAllFields() {

		animation.stop();
		
		onStoppingTypingSensorClicked = false;

		setLockStateFields(true);
		setLoginStateFields(false);

		setConnectToWatchFields(mGreyWatchImg, false, "Not Connected", false);

		setTypingDetectorFields(mGreyKeyboardImg, false, "Typing-Detector off", false, false);
		enableTypingDetectorFields(false);

		setPasswordFields(mGreyKeyImg, true, true, false, true);

		setHeartBeatFields(mGreyHeartImg, false, "Heartbeat", "0.0");
		setProximityFields(mGreySignalImg, false, "Proximity", "-");
		resetUserStateFields();
	}

	private void setLockStateFields(boolean isLocked) {
		if (isLocked) {
			lblLockState.setText("locked");
			imgLockState.setImage(mLockedImg);
		} else {
			lblLockState.setText("unlocked");
			imgLockState.setImage(mUnlockedImg);
		}
	}

	private void setLoginStateFields(boolean isLoggedIn) {
		if (isLoggedIn) {
			lblLoginState.setText("logged In");
			imgLoginStateState.setImage(mLoginImg);
		} else {
			lblLoginState.setText("logged out");
			imgLoginStateState.setImage(mLogoutImg);
		}
	}

	private void setConnectToWatchFields(Image img, boolean isInProgress, String connectionState,
			boolean connectedSuccess) {

		imgWatchConnected.setImage(img);
		lblConnectToWatch.setText(connectionState);

		if (isInProgress) {
			progressConnectToWatch.setVisible(true);
			imgWatchConnected.setVisible(false);
			btnConnectToWatch.setVisible(false);
			imgCancelConnectToWatch.setVisible(true);
		} else {
			progressConnectToWatch.setVisible(false);
			imgWatchConnected.setVisible(true);
			imgCancelConnectToWatch.setVisible(false);

			if (connectedSuccess) {
				btnConnectToWatch.setVisible(false);
				imgConnectedToWatchSuccess.setVisible(true);
			} else {
				btnConnectToWatch.setVisible(true);
				imgConnectedToWatchSuccess.setVisible(false);
			}
		}

	}

	private void setTypingDetectorFields(Image img, boolean isInProgress, String detectorState, boolean userWasTyping,
			boolean isCancable) {

		imgTyping.setImage(img);
		lblTyping.setText(detectorState);

		if (isInProgress) {
			progressTyping.setVisible(true);
			imgTyping.setVisible(false);
			btnStartTypingDetector.setVisible(false);
			imgCancelStartKeyDetection.setVisible(true);
		} else {
			progressTyping.setVisible(false);
			imgTyping.setVisible(true);

			if (userWasTyping) {
				btnStartTypingDetector.setVisible(false);
				imgStartingKeyDetectorSuccess.setVisible(true);
				imgCancelStartKeyDetection.setVisible(false);
			} else {

				if (isCancable) {
					btnStartTypingDetector.setVisible(false);
					imgStartingKeyDetectorSuccess.setVisible(false);
					imgCancelStartKeyDetection.setVisible(true);
				} else {

					btnStartTypingDetector.setVisible(true);
					imgStartingKeyDetectorSuccess.setVisible(false);
					imgCancelStartKeyDetection.setVisible(false);
				}

			}

		}
	}

	private void enableTypingDetectorFields(boolean enable) {
		lblTyping.setDisable(!enable);
		btnStartTypingDetector.setDisable(!enable);
	}

	private void setPasswordFields(Image keyImage, boolean showPWInput, boolean disablePWInput, boolean pwCorrect,
			boolean clear) {

		imgPassword.setImage(keyImage);
		if (showPWInput) {
			txtPassword.setVisible(true);
			lblPassword.setVisible(false);
		} else {
			txtPassword.setVisible(false);
			lblPassword.setVisible(true);
		}

		if (disablePWInput) {
			txtPassword.requestFocus();
			btnLogin.setDisable(true);
			txtPassword.setDisable(true);
		} else {
			btnLogin.setDisable(true);
			txtPassword.setDisable(false);
		}

		if (pwCorrect) {
			btnLogin.setVisible(false);
			imgPasswordCorrect.setVisible(true);
		} else {
			btnLogin.setVisible(true);
			imgPasswordCorrect.setVisible(false);
		}

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

	private void setProximityFields(Image img, boolean isInProgress, String proximity, String value) {
		if (isInProgress) {
			progressProximity.setVisible(true);
			imgProximity.setVisible(false);
		} else {
			progressProximity.setVisible(false);
			imgProximity.setVisible(true);
		}

		imgProximity.setImage(img);
		lblProximity.setText(proximity);
		lblProximityValue.setText(value);
	}

	private void resetUserStateFields() {
		imgUserState.setImage(mGreyStandingImg);
		lblUserState.setText("User State");
		lblUserStateValue.setText("still");
	}

	/*----------------LOGIN----------------*/

	@FXML
	private void btnLoginAction(ActionEvent event) {
		mKeyDetectorstarted = false;
		mTCPServer.sendMessage(AppConstants.COMMAND_STOP_TYPING_SENSORS);
	}

	@FXML
	private void onConnectToWatchAction(ActionEvent event) {

		int checksum = mChecksum.calculateChecksum();

		// System.out.println(TAG + ": connecting to watch/phone (checksum:" +
		// checksum + ")");

		setConnectToWatchFields(mGreyWatchImg, true, "Connecting (" + checksum + ") ...", false);
	}

	private boolean mKeyDetectorstarted = false;

	@FXML
	private void onStartTypingDetectorAction(ActionEvent event) {
		startKeyPressDetection();
		animation.stop();
	}

	private boolean onStoppingTypingSensorClicked = false;

	@FXML
	private void onCancelStartKeyDetectionClicked(MouseEvent event) {

		onStoppingTypingSensorClicked = true;
		setTypingDetectorFields(mGreyKeyboardImg, false, "Typing-Detector off", false, false);
		enableTypingDetectorFields(true);
		setPasswordFields(mGreyKeyImg, true, true, false, true);

		mTCPServer.sendMessage(AppConstants.COMMAND_STOP_TYPING_SENSORS);
	}

	@FXML
	private void onPasswordFieldKeyPressed(KeyEvent event) {

		if (event.getCode() == KeyCode.ENTER) {
			mKeyDetectorstarted = false;
			mTCPServer.sendMessage(AppConstants.COMMAND_STOP_TYPING_SENSORS);
		}

	}

	@FXML
	private void onPasswordFieldKeyReleased(KeyEvent event) {

		if (txtPassword.getLength() > 0 && !mKeyDetectorstarted && event.getCode() != KeyCode.ENTER) {
			mKeyDetectorstarted = true;
		}

		if (txtPassword.getLength() == 0) {
			btnLogin.setDisable(true);
		} else {
			btnLogin.setDisable(false);
		}

	}

	private void login() {

		try {
			if (mLoginModel.isValidCredentials(lblUserName.getText(), txtPassword.getText())) {

				// System.out.println(TAG + ": username and password CORRECT");
				lblLog.setText("");

				setPasswordFields(mBlackKeyImg, false, true, true, true);

				setHeartBeatFields(mWhiteHeartImg, true, "Detect...", "0.0");
				setProximityFields(mWhiteSignalImg, true, "Detect", " - ");
				imgUserState.setImage(mWhiteStandingImg);

				lblPassword.setText("Password correct");
				lblInformation.setText("Checking...");

				setLoginStateFields(true);
				mTCPServer.sendMessage(AppConstants.COMMAND_USERNAME + "::" + LoginUtil.getInstance().getUsername());
				mTCPServer.sendMessage(AppConstants.COMMAND_START_SENDING_SENSORDATA);

			} else {
				System.out.println(TAG + ": username and/or password INCORRECT");
				Shaker shaker = new Shaker(anchorPaneLogin);
				shaker.shake();
				txtPassword.selectAll();
				startKeyPressDetection();
			}
		} catch (SQLException sqle) {
			// System.out.println(TAG + ": SQL ERROR login user. Exception: " +
			// sqle.toString());
		}
	}

	@FXML
	private void onCancelConnectToWatchClicked(MouseEvent event) {
		setConnectToWatchFields(mGreyWatchImg, false, "Not connected", false);
		Checksum.getInstance().resetChecksum();
	}

	private void startKeyPressDetection() {
		setTypingDetectorFields(mGreyKeyboardImg, true, "Starting...", false, true);
		mTCPServer.sendMessage(AppConstants.COMMAND_START_TYPING_SENSORS);
	}

	private void keypressDetectorReady() {
		txtPassword.setDisable(false);
		txtPassword.setFocusTraversable(true);
		txtPassword.requestFocus();

		setTypingDetectorFields(mBlackKeyboardImg, false, "Typing-Detector active", false, true);

	}

	@FXML
	private void onAuthenticatorBoxMouseEntered(MouseEvent event) {
		//animation.setToValue(1);
		//animation.stop();
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
				lblCancel.setText("Go to Main Screen");

				lblUserName.setText(txtUsername.getText());
			} else {

				Shaker shaker = new Shaker(anchorPaneRegister);
				shaker.shake();
				lblRegistrationFailed.setVisible(true);
				txtUsername.selectAll();
			}
		} catch (SQLException e) {
			// System.out.println(TAG + ": SQL ERROR resgister new user.
			// Exception: " + e.toString());
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
			// System.out.println(TAG + ": SQL ERROR changing user. Exception: "
			// + e.toString());
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
			// System.out.println(shutdownCommand);
		} else if ("Windows".equals(operatingSystem)) {
			shutdownCommand = new String[] { "shutdown -s -t 60" };
		} else {
			throw new RuntimeException("Unsupported operating system.");
		}
		try {
			Runtime.getRuntime().exec(shutdownCommand);
			// System.out.println(TAG + ": Shutdown PC ");
		} catch (IOException e) {
			// System.out.println(TAG + ": ERROR executing SHUTDOWN command.
			// Exception: " + e.toString());
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
			// System.out.println(TAG + ": Send PC to sleep");
		} catch (IOException e) {
			// System.out.println(TAG + ": ERROR executing SLEEP command.
			// Exception: " + e.toString());
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
			// System.out.println(TAG + ": ERROR opening settings. Exception: "
			// + e.toString());
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

	public Label getInformationLbl() {
		return lblInformation;
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
