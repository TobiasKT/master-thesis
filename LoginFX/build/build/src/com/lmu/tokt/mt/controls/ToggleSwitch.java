package com.lmu.tokt.mt.controls;

import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ToggleSwitch extends Parent {

	private Color BLUE_COLOR_TRANS = Color.rgb(58, 83, 155, 0.4);
	private Color BLUE_COLOR = Color.rgb(58, 83, 155, 0.7);

	private BooleanProperty switchedOn = new SimpleBooleanProperty(false);

	private TranslateTransition translateAnimation = new TranslateTransition(Duration.seconds(0.25));
	private FillTransition fillAnimation = new FillTransition(Duration.seconds(0.25));

	private ParallelTransition animation = new ParallelTransition(translateAnimation, fillAnimation);

	public BooleanProperty switchedOnProperty() {
		return switchedOn;
	}

	private ToggleSwitchListener mToggleSwitchListener;

	public ToggleSwitch(double width, double height, ToggleSwitchListener listener) {
		mToggleSwitchListener = listener;
		Rectangle background = new Rectangle(width, height);
		background.setArcWidth(height);
		background.setArcHeight(height);
		background.setFill(Color.WHITE);
		background.setStroke(BLUE_COLOR);

		Circle trigger = new Circle(height / 2);
		trigger.setCenterX(height / 2);
		trigger.setCenterY(height / 2);
		trigger.setFill(Color.WHITE);
		trigger.setStroke(Color.LIGHTGRAY);

		DropShadow shadow = new DropShadow();
		shadow.setRadius(1);
		trigger.setEffect(shadow);

		translateAnimation.setNode(trigger);
		fillAnimation.setShape(background);

		getChildren().addAll(background, trigger);

		switchedOn.addListener((obs, oldState, newState) -> {
			boolean isOn = newState.booleanValue();
			translateAnimation.setToX(isOn ? width - height : 0);
			fillAnimation.setFromValue(isOn ? Color.WHITE : BLUE_COLOR_TRANS);
			fillAnimation.setToValue(isOn ? BLUE_COLOR_TRANS : Color.WHITE);

			animation.play();
		});

		setOnMouseClicked(event -> {
			switchedOn.set(!switchedOn.get());
			boolean switchOnToggle = switchedOn.getValue();
			if (switchOnToggle) {
				mToggleSwitchListener.onToggelSwitchListener(TOGGLE_STATE_ON);
			} else {
				mToggleSwitchListener.onToggelSwitchListener(TOGGLE_STATE_OFF);
			}

		});

		trigger.setOnMouseEntered(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				setCursor(Cursor.HAND);
			}
		});

	}

	private static final int TOGGLE_STATE_ON = 1;
	private static final int TOGGLE_STATE_OFF = 0;

	/* Toggle Listener */
	public interface ToggleSwitchListener {
		void onToggelSwitchListener(int state);
	}

}