package com.lmu.tokt.mt.util;

public class AppConstants {

	public static final String ACCURACY = "accuracy";
	public static final String TIMESTAMP = "timestamp";
	public static final String VALUES = "values";
	public static final String FILTER = "filter";

	public static final String COMMAND_CONNECT = "CONNECT";
	public static final String COMMAND_DISCONNECT = "DISCONNECT";
	public static final String COMMAND_CONFIRM = "CONFIRM";
	public static final String COMMAND_GET_CUES = "GET_CUES";
	public static final String COMMAND_NOT_AUTHENTICATED = "NOT_AUTHENTICATED";

	public static final String SENSORDATA = "SENSORDATA";

	public static final int STATE_CONNECTED = -1000;
	public static final int STATE_DISCONNECTED = -1001;
	public static final int STATE_CONFIRM = -2000;

	public static final int ERROR = -9999;
	public static final int STATE_SERVER_RUNNING = 1000;
	public static final int STATE_SERVER_STOPPED = 1001;

	public static final int STATE_HEART_BEAT_DETECTED = -3000;
	public static final int STATE_HEART_BEATING = -3001;
	public static final int STATE_HEART_STOPPED = -3002;

	public static final String STRING_TYPE_HEART_RATE = "heart_rate";

}
