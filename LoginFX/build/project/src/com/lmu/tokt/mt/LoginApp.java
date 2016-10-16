package com.lmu.tokt.mt;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class LoginApp extends Application {

	private static final String TAG = LoginApp.class.getSimpleName();

	private Stage mMainAppStage;
	private Stage mIntroStage;
	private Stage mStage;

	private boolean userComesFromIntro = false;

	private static LoginApp instance;

	private LoginModel mLoginModel;

	public LoginApp() {
		instance = this;
	}

	public static LoginApp getInstance() {
		return instance;
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		System.out.println(TAG + ": start application");
		mLoginModel = new LoginModel();
		mStage = primaryStage;
		try {

			if (mLoginModel.getDidIntroState() == 0) {
				openIntro(primaryStage);
			} else {
				openMainApp(primaryStage, false);
			}
		} catch (Exception e) {
			System.out.println(TAG + ": ERROR loading primary stage. Exception: " + e.toString());
		}

	}

	public void openIntro(Stage primaryStage) throws IOException {
		VBox root = FXMLLoader.load(LoginApp.class.getResource("/com/lmu/tokt/mt/Intro.fxml"));
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("intro.css").toExternalForm());
		mIntroStage = primaryStage;
		mIntroStage.setScene(scene);
		mIntroStage.show();
	}

	public void openMainApp(Stage primaryStage, boolean fromIntro) throws IOException {

		userComesFromIntro = fromIntro;

		BorderPane root = FXMLLoader.load(LoginApp.class.getResource("/com/lmu/tokt/mt/Login.fxml"));
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		mMainAppStage = primaryStage;
		mMainAppStage.setScene(scene);
		mMainAppStage.show();

		mMainAppStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				System.out.println(TAG + ": handling close Event");
				Alert alert = new Alert(AlertType.NONE);
				alert.setTitle("Confirmation - Close Application");
				alert.setContentText("Do you want do close the \nAuthenticator application?");

				ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
				ButtonType buttonTypeOk = new ButtonType("OK", ButtonData.APPLY);

				alert.getButtonTypes().setAll(buttonTypeOk, buttonTypeCancel);

				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == buttonTypeOk) {
					try {
						mLoginModel.updateLastLoggedInUser(LoginUtil.getInstance().getUsername());
					} catch (SQLException e) {
						System.out.println(
								TAG + ": ERROR updatating" + " last logged in user. Exception: " + e.toString());
					}
					// stop server
					LoginController.getInstance().getTCPServer().interrupt();
					System.exit(1);
				} else if (result.get() == buttonTypeCancel) {
					event.consume();
				}

			}
		});
	}

	public void closeIntro() {
		mIntroStage.close();
	}

	public void closeMainApp() {
		mMainAppStage.close();
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		System.out.println(TAG + ": stop application");
	}

	public Stage getStage() {
		return mStage;
	}

	public boolean isUserFromIntro() {
		return userComesFromIntro;
	}

}
