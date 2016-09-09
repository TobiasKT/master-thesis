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

	private Stage mStage;

	private static LoginApp instance;

	public LoginApp() {
		instance = this;
	}

	public static LoginApp getInstance() {
		return instance;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		System.out.println("Start Application...");
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

					Alert alert = new Alert(AlertType.NONE);
					alert.setTitle("Confirmation - Close Application");
					alert.setContentText("Do you want do close the \nAuthenticator application?");

					ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
					ButtonType buttonTypeOk = new ButtonType("OK", ButtonData.APPLY);

					alert.getButtonTypes().setAll(buttonTypeOk, buttonTypeCancel);

					Optional<ButtonType> result = alert.showAndWait();
					if (result.get() == buttonTypeOk) {

						// TDOD Last loggedInUser
						LoginModel model = new LoginModel();
						try {
							model.updateLastLoggedInUser(LoginUtil.getInstance().getUsername());
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						System.exit(1);
					} else if (result.get() == buttonTypeCancel) {
						event.consume();
					}

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Stage getStage() {
		return mStage;
	}

	@Override
	public void stop() throws Exception {
		super.stop();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
