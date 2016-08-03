package com.lmu.tokt.mt;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController implements Initializable {

	@FXML
	private AnchorPane anchorPaneLogin;
	@FXML
	private Circle circProfile;
	@FXML
	private Label lblUserName;
	@FXML
	private Label lblDateTime;
	@FXML
	private PasswordField txtPassword;
	@FXML
	private Button btnLogin;
	@FXML
	private ImageView imgAddUser;
	@FXML
	private ImageView imgChangeUser;
	@FXML
	private ImageView imgSleep;
	@FXML
	private ImageView imgShutDown;
	@FXML
	private ImageView imgWatchConnection;
	@FXML
	private ImageView imgSettings;

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
				((Node) (event.getSource())).getScene().getWindow().hide();

				Parent parent = FXMLLoader.load(getClass().getResource("Main.fxml"));
				Stage stage = new Stage();
				Scene scene = new Scene(parent);
				stage.setScene(scene);
				stage.setTitle("Login");
				stage.show();

			} else {
				System.out.println("Username and Password is WRONG!");
				Shaker shaker = new Shaker(anchorPaneLogin);
				shaker.shake();

				txtPassword.selectAll();
			}
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
			sqle.printStackTrace();
		} catch (IOException ioe) {
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

	private class Shaker {
		private TranslateTransition translateTransition;

		public Shaker(Node node) {
			translateTransition = new TranslateTransition(Duration.millis(55), node);
			translateTransition.setFromX(0f);
			translateTransition.setByX(16f);
			translateTransition.setCycleCount(5);
			translateTransition.setAutoReverse(true);
		}

		public void shake() {
			translateTransition.playFromStart();
		}
	}
}
