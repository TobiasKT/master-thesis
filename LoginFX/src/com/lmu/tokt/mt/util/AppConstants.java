package com.lmu.tokt.mt.util;

public class AppConstants {

	/*------------------------ SERVER VARS ------------------------*/

	public static final int DEFAULT_SERVER_PORT = 8888;

	/*------------------------ SENSOR NAMES ------------------------*/
	public static final String SENSOR_NAME_HEART_RATE = "HEART_RATE";
	public static final String SENSOR_NAME_STEP_COUNTER = "STEP_COUNTER";
	public final static String SENSOR_NAME_BEACON = "BEACON";

	/*------------------------ SENSOR DATA ------------------------*/
	public static final String SENSORDATA = "SENSORDATA";

	public static final String ACCURACY = "accuracy";
	public static final String TIMESTAMP = "timestamp";
	public static final String VALUES = "values";
	public static final String FILTER = "filter";

	/*------------------------  PROXIMITY VALUES ------------------------*/
	public static final String PROXIMITY_IMMEDIATE = "immediate";
	public static final String PROXIMITY_NEAR = "near";
	public static final String PROXIMITY_FAR = "far";

	/*------------------------ COMMANDS ------------------------*/

	public static final String COMMAND_PHONE_WATCH_CONNECT = "CONNECT";
	public static final String COMMAND_PHONE_WATCH_DISCONNECT = "DISCONNECT";
	public static final String COMMAND_PHONE_WATCH_CONNECTION_CONFIRM = "CONFIRM";
	public static final String COMMAND_START_SENDING_SENSORDATA = "START_SENDING_SENSORDATA";
	public static final String COMMAND_USER_AUTHENTICATED = "AUTHENTICATED";
	public static final String COMMAND_USER_NOT_AUTHENTICATED = "NOT_AUTHENTICATED";
	public static final String COMMAND_LISTEN_TO_SOUND = "LISTEN_TO_SOUND";
	public static final String COMMAND_UNLOCKED = "UNLOCKED";
	public static final String COMMAND_LOCKED = "LOCKED";
	public static final String COMMAND_USERNAME = "USERNAME";
	public static final String COMMAND_START_TYPING_SENSORS = "START_TYPING_SENSORS";
	public static final String COMMAND_STOP_TYPING_SENSORS = "STOP_TYPING_SENSORS";
	public static final String COMMAND_SAVE_DIALOG_EVENT_LOCK = "SAVE_DIALOG_EVENT_LOCK";
	public static final String COMMAND_SAVE_DIALOG_EVENT_UNLOCK = "SAVE_DIALOG_EVENT_UNLOCK";
	public static final String COMMAND_SAVE_DIALOG_EVENT_NOT_AUTHENTICATED = "SAVE_DIALOG_EVENT_NOT_AUTHENTICATED";

	/*------------------------ PHONE/WATCH CONNECTION STATES ------------------------*/

	public static final int STATE_PHONE_WATCH_CONNECTED = -1000;
	public static final int STATE_PHONE_WATCH_DISCONNECTED = -1001;
	public static final int STATE_PHONE_WATCH_CONNECTION_CONFIRMED = -2000;

	/*------------------------ SERVER STATES ------------------------*/

	public static final int STATE_NETWORK_ERROR = -9999;
	public static final int STATE_SERVER_RUNNING = 1000;
	public static final int STATE_SERVER_STOPPED = 1001;

	/*------------------------ USER STATES ------------------------*/

	public static final int STATE_HEART_BEATING = -3001;
	public static final int STATE_HEART_STOPPED = -3002;
	public static final int STATE_PROXIMITY_DETECTED = -4001;
	public static final int STATE_PROXIMITY_NOT_AVAILABLE = -4002;
	public static final int STATE_USER_WALKING = -6001;
	public static final int STATE_USER_STILL = -6002;

	public static final int STATE_USER_AUTHENTICATED = -7001;
	public static final int STATE_USER_NOT_AUTHENTICATED = -7002;

	/*------------------------ APP STATES ------------------------*/
	public static final int STATE_APP_LOCKED = -5001;
	public static final int STATE_APP_UNLOCKED = -5002;

	public static final int STATE_SOUND_SIGNAL_SENDING = -8001;
	public static final int STATE_SOUND_SENDING_NONE = -8002;

	/*------------------- DIALOG EVENT TYPES ----------------*/

	public static final int DIALOG_EVENT_TYPE_LOCK = 1000001;
	public static final int DIALOG_EVENT_TYPE_UNLOCK = 2000001;
	public static final int DIALOG_EVENT_TYPE_NOT_AUTHENTICATED = 3000001;

	/*------------------------ DATABASE ------------------------*/
	public static final int IMAGE_TYPE_BACKGROUND = 1111;
	public static final int IMAGE_TYPE_AVATAR = 2222;

	public static final String DB_FIELD_USERNAME = "username";
	public static final String DB_FIELD_PASSWORD = "password";
	public static final String DB_FIELD_BACKGROUND = "background";
	public static final String DB_FIELD_AVATAR = "avatar";

	public static final String DB_FIELD_LAST_USER = "lastUser";

}
