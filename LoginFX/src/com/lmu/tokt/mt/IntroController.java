package com.lmu.tokt.mt;

import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class IntroController implements Initializable {

	private static final String TAG = IntroController.class.getSimpleName();

	private static final String SURVEY_URL = "https://survey.medien.ifi.lmu.de/index.php/251657/lang-en";
	private static final String ANDROID_WEAR_APP_URL = "https://play.google.com/store/apps/details?id=com.google.android.wearable.app";
	private static final String AUTHENTICATOR_APP_URL = "https://www.dropbox.com/sh/ytyjwm207lv0uh4/AABiHU_fjU9-1GyJxIzzxSI0a?dl=0";

	@FXML
	private Label lblIntroTitle, lblIntroSubTitle, lblBack, lblSkip;

	@FXML
	private AnchorPane anchorPaneIntro1, anchorPaneIntro2, anchorPaneIntro3, anchorPaneIntro4, anchorPaneIntro5,
			anchorPaneIntro6;

	@FXML
	private VBox vBoxSkip, vBoxBack;

	@FXML
	private Circle circleIndex1, circleIndex2, circleIndex3, circleIndex4, circleIndex5, circleIndex6;

	@FXML
	private Hyperlink hyperlinkSurvey, hyperlinkAndroidWear, hyperlinkAuthenticator;

	@FXML
	private ImageView imgSurvey;

	private int mIndex = 0;

	private LoginModel mLoginModel;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		mLoginModel = new LoginModel();

	}

	@FXML
	private void onSurveyAction(ActionEvent event) {
		openLinkInBrowser(SURVEY_URL);
	}

	@FXML
	private void onSurveyImgClicked(MouseEvent event) {
		openLinkInBrowser(SURVEY_URL);
	}

	@FXML
	private void onAndroidWearAction(ActionEvent event) {
		openLinkInBrowser(ANDROID_WEAR_APP_URL);
	}

	@FXML
	private void onAuthenticatorAction(ActionEvent event) {
		openLinkInBrowser(AUTHENTICATOR_APP_URL);
	}

	private void openLinkInBrowser(String link) {

		try {
			Desktop.getDesktop().browse(new URL(link).toURI());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	private void onBackClicked(MouseEvent event) {
		if (mIndex > 0) {
			mIndex--;
		}

		switch (mIndex) {
		case 0:
			circleIndex1.setRadius(8);
			circleIndex2.setRadius(3);

			anchorPaneIntro1.setVisible(true);
			anchorPaneIntro2.setVisible(false);
			setTitlesIntroScreen1();
			break;
		case 1:
			circleIndex2.setRadius(8);
			circleIndex3.setRadius(3);

			anchorPaneIntro2.setVisible(true);
			anchorPaneIntro3.setVisible(false);
			setTitlesIntroScreen2();
			break;
		case 2:
			circleIndex3.setRadius(8);
			circleIndex4.setRadius(3);

			anchorPaneIntro3.setVisible(true);
			anchorPaneIntro4.setVisible(false);
			setTitlesIntroScreen3();
			break;
		case 3:
			circleIndex4.setRadius(8);
			circleIndex5.setRadius(3);

			anchorPaneIntro4.setVisible(true);
			anchorPaneIntro5.setVisible(false);
			setTitlesIntroScreen4();
			break;
		case 4:
			circleIndex5.setRadius(8);
			circleIndex6.setRadius(3);

			anchorPaneIntro5.setVisible(true);
			anchorPaneIntro6.setVisible(false);
			setTitlesIntroScreen5();
			lblSkip.setText("SKIP");
			break;

		default:
			break;
		}
	}

	@FXML
	private void onSkipClicked(MouseEvent event) {

		if (mIndex < 5) {
			mIndex++;
		} else if (mIndex == 5) {

			// reopen
			int userDidIntro = 0;
			try {
				userDidIntro = mLoginModel.getDidIntroState();
			} catch (SQLException e) {
			}

			if (userDidIntro == 1) {
				Node source = (Node) event.getSource();
				Stage stage = (Stage) source.getScene().getWindow();
				stage.close();
			} else {
				closeIntro();
			}
		}

		switch (mIndex) {
		case 1:
			circleIndex1.setRadius(3);
			circleIndex2.setRadius(8);

			anchorPaneIntro1.setVisible(false);
			anchorPaneIntro2.setVisible(true);

			setTitlesIntroScreen2();
			break;
		case 2:

			circleIndex2.setRadius(3);
			circleIndex3.setRadius(8);

			anchorPaneIntro2.setVisible(false);
			anchorPaneIntro3.setVisible(true);
			setTitlesIntroScreen3();
			break;
		case 3:
			circleIndex3.setRadius(3);
			circleIndex4.setRadius(8);

			anchorPaneIntro3.setVisible(false);
			anchorPaneIntro4.setVisible(true);
			setTitlesIntroScreen4();
			break;
		case 4:
			circleIndex4.setRadius(3);
			circleIndex5.setRadius(8);

			anchorPaneIntro4.setVisible(false);
			anchorPaneIntro5.setVisible(true);
			setTitlesIntroScreen5();

			break;
		case 5:
			circleIndex5.setRadius(3);
			circleIndex6.setRadius(8);

			anchorPaneIntro5.setVisible(false);
			anchorPaneIntro6.setVisible(true);
			setTitlesIntroScreen6();
			lblSkip.setText("DONE");
			break;

		default:
			break;
		}
	}

	private void setTitlesIntroScreen1() {
		lblIntroTitle.setText("Welcome");
		lblIntroSubTitle.setText("Survey");
	}

	private void setTitlesIntroScreen2() {
		lblIntroTitle.setText("Installation");
		lblIntroSubTitle.setText("Android");
	}

	private void setTitlesIntroScreen3() {
		lblIntroTitle.setText("Permissions");
		lblIntroSubTitle.setText("Android");

	}

	private void setTitlesIntroScreen4() {
		lblIntroTitle.setText("Internet Connection");
		lblIntroSubTitle.setText("Setup");
	}

	private void setTitlesIntroScreen5() {
		lblIntroTitle.setText("Beacon Settings");
		lblIntroSubTitle.setText("Android");
	}

	private void setTitlesIntroScreen6() {
		lblIntroTitle.setText("Connection");
		lblIntroSubTitle.setText("Desktop - Android");
	}

	private void closeIntro() {
		try {
			mLoginModel.updateDidIntroState(1);
			LoginApp.getInstance().closeIntro();
			LoginApp.getInstance().openMainApp(LoginApp.getInstance().getStage(), true);
		} catch (SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
