package com.lmu.tokt.mt;

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
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class LoginApp extends Application {

	private static final String TAG = LoginApp.class.getSimpleName();

	private Stage mStage;

	private static LoginApp instance;

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
		try {
			BorderPane root = FXMLLoader.load(getClass().getResource("Login.fxml"));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			mStage = primaryStage;
			primaryStage.setScene(scene);
			primaryStage.show();

			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

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
						LoginModel model = new LoginModel();
						try {
							model.updateLastLoggedInUser(LoginUtil.getInstance().getUsername());
						} catch (SQLException e) {
							System.out.println(
									TAG + ": ERROR updatating" + " last logged in user. Exception: " + e.toString());
						}

						System.exit(1);
					} else if (result.get() == buttonTypeCancel) {
						event.consume();
					}

				}
			});
		} catch (Exception e) {
			System.out.println(TAG + ": ERROR loading primary stage. Exception: " + e.toString());
		}
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		System.out.println(TAG + ": stop application");
	}

	public Stage getStage() {
		return mStage;
	}

}
