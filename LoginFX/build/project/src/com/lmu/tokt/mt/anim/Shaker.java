package com.lmu.tokt.mt.anim;

import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class Shaker {

	private TranslateTransition mTranslateTransition;

	public Shaker(Node node) {
		mTranslateTransition = new TranslateTransition(Duration.millis(55), node);
		mTranslateTransition.setFromX(0f);
		mTranslateTransition.setByX(16f);
		mTranslateTransition.setCycleCount(5);
		mTranslateTransition.setAutoReverse(true);
	}

	public void shake() {
		mTranslateTransition.playFromStart();
	}
}
