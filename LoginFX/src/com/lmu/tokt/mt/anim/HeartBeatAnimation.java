package com.lmu.tokt.mt.anim;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class HeartBeatAnimation {

	private ScaleTransition mScaleTransition;

	private ImageView mHeartImage;

	public HeartBeatAnimation(Node node) {

		mHeartImage = (ImageView) node;
		mScaleTransition = new ScaleTransition(Duration.millis(1500), node);
		mScaleTransition.setByX(-0.25f);
		mScaleTransition.setByY(-0.25f);
		mScaleTransition.setCycleCount(TranslateTransition.INDEFINITE);
		mScaleTransition.setAutoReverse(true);
	}

	public void startAnimation() {
		mScaleTransition.play();
	}

	public void stopAnimation() {
		resetWidthAndHeight();
		mScaleTransition.stop();
	}

	private void resetWidthAndHeight() {
		mHeartImage.setFitHeight(24.0);
		mHeartImage.setFitWidth(24.0);
	}

}
