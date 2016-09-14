package com.lmu.tokt.mt.util;

public class AppConstants {

	public static final int DEFAULT_SERVER_PORT = 8888;

	public static final String ACCURACY = "accuracy";
	public static final String TIMESTAMP = "timestamp";
	public static final String VALUES = "values";
	public static final String FILTER = "filter";

	public static final String COMMAND_CONNECT = "CONNECT";
	public static final String COMMAND_DISCONNECT = "DISCONNECT";
	public static final String COMMAND_CONFIRM = "CONFIRM";
	public static final String COMMAND_GET_CUES = "GET_CUES";
	public static final String COMMAND_NOT_AUTHENTICATED = "NOT_AUTHENTICATED";
	public static final String COMMAND_LISTEN_TO_SOUND = "LISTEN_TO_SOUND";

	public static final String SENSORDATA = "SENSORDATA";
	public static final String BEACONDATA = "BEACONDATA";

	public static final int STATE_CONNECTED = -1000;
	public static final int STATE_DISCONNECTED = -1001;
	public static final int STATE_CONFIRM = -2000;

	public static final int ERROR = -9999;
	public static final int STATE_SERVER_RUNNING = 1000;
	public static final int STATE_SERVER_STOPPED = 1001;

	public static final int STATE_HEART_BEAT_DETECTED = -3000;
	public static final int STATE_HEART_BEATING = -3001;
	public static final int STATE_HEART_STOPPED = -3002;

	public static final int STATE_PROXIMITY = -4000;

	public static final int STATE_LOCKED = -5000;
	public static final int STATE_UNLOCKED = -5001;

	public static final String SENSOR_NAME_HEART_RATE = "HEART_RATE";
	public static final String SENSOR_NAME_STEP_COUNTER = "STEP_COUNTER";

	public static final String PROXIMITY_IMMEDIATE = "immediate";
	public static final String PROXIMITY_NEAR = "near";
	public static final String PROXIMITY_FAR = "far";

	// DB
	public static final int IMAGE_TYPE_BACKGROUND = 1111;
	public static final int IMAGE_TYPE_AVATAR = 2222;

	public static final String DB_FIELD_USERNAME = "username";
	public static final String DB_FIELD_PASSWORD = "password";
	public static final String DB_FIELD_BACKGROUND = "background";
	public static final String DB_FIELD_AVATAR = "avatar";

	public static final String DB_FIELD_LAST_USER = "lastUser";

	public static final int UPDATE_STEP_COUNT = -6000;
	public static final int STATE_WALKING = -6001;
	public static final int STATE_STILL = -6002;
}
