package com.android.lmu.mt.tokt.authenticator.shared;

import android.hardware.Sensor;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.Sensor.TYPE_GAME_ROTATION_VECTOR;
import static android.hardware.Sensor.TYPE_GRAVITY;
import static android.hardware.Sensor.TYPE_GYROSCOPE;
import static android.hardware.Sensor.TYPE_LIGHT;
import static android.hardware.Sensor.TYPE_LINEAR_ACCELERATION;
import static android.hardware.Sensor.TYPE_SIGNIFICANT_MOTION;
import static android.hardware.Sensor.TYPE_STEP_COUNTER;

/**
 * Created by tobiaskeinath on 28.08.16.
 */
public class AppConstants {

    //Server
    public final static String DEFAULT_SERVER_IP = "192.168.43.108";
    public final static int DEFAULT_SERVER_PORT = 8080;


    //Shared Preferences
    public final static String SHARED_PREF_APP_KEY = "com.android.lmu.mt.tokt.authenticator.PREFERENCE_FILE_KEY";
    public final static String SHARED_PREF_SEVER_IP = "server_ip";
    public final static String SHARED_PREF_SEVER_PORT = "server_port";


    //SensorTypes
    public final static int SENSOR_TYPE_DEBUG = 0;
    public final static int SENSOR_TYPE_ACCELEROMETER = TYPE_ACCELEROMETER; //1
    public final static int SENSOR_TYPE_STEP_COUNTER = TYPE_STEP_COUNTER; //19
    public final static int SENSOR_TYPE_WRIST_TILT = 26;
    public final static int SENSOR_TYPE_GYRO = TYPE_GYROSCOPE; //4
    public final static int SENSOR_TYPE_LIGHT = TYPE_LIGHT; //5
    public final static int SENSOR_TYPE_HEART_RATE = Sensor.TYPE_HEART_RATE; //21
    public final static int SENSOR_TYPE_GAME_ROTATION = TYPE_GAME_ROTATION_VECTOR; //15
    public final static int SENSOR_TYPE_WELLNESS_PASSIVE = 65538; //
    public final static int SENSOR_TYPE_PPG = 65545;
    public final static int SENSOR_TYPE_SIGNIFICANT_MOTION = TYPE_SIGNIFICANT_MOTION; //17
    public final static int SENSOR_TYPE_DETAILED_STEP_COUNTER = 65537;
    public final static int SENSOR_TYPE_USER_PROFILE = 65539;
    public final static int SENSOR_TYPE_USER_STRIDE_FACTOR = 65546;
    public final static int SENSOR_TYPE_GRAVITY = TYPE_GRAVITY; //9
    public final static int SENSOR_TYPE_LINEAR_ACCELERATION = TYPE_LINEAR_ACCELERATION; //10

    //SensorNames
    public final static String SENSOR_NAME_DEBUG = "DEBUG";
    public final static String SENSOR_NAME_ACCELEROMETER = "ACCELEROMETER";
    public final static String SENSOR_NAME_STEP_COUNTER = "STEP_COUNTER";
    public final static String SENSOR_NAME_WRIST_TILT = "WRIST_TILT";
    public final static String SENSOR_NAME_GYRO = "GYROSCOPE";
    public final static String SENSOR_NAME_LIGHT = "LIGHT";
    public final static String SENSOR_NAME_HEART_RATE = "HEART_RATE";
    public final static String SENSOR_NAME_GAME_ROTATION = "GAME_ROTATION_VECTOR";
    public final static String SENSOR_NAME_WELLNESS_PASSIVE = "WELLNESS_PASSIV";
    public final static String SENSOR_NAME_PPG = "PPG";
    public final static String SENSOR_NAME_SIGNIFICANT_MOTION = "SIGNIFICANT_MOTION";
    public final static String SENSOR_NAME_DETAILED_STEP_COUNTER = "DETAILED_STEP_COUNTER";
    public final static String SENSOR_NAME_USER_PROFILE = "USER_PROFILE";
    public final static String SENSOR_NAME_USER_STRIDE_FACTOR = "USER_STRIDE_FACTOR";
    public final static String SENSOR_NAME_GRAVITY = "GRAVITY";
    public final static String SENSOR_NAME_LINEAR_ACCELERATION = "LINEAR_ACCELERATION";


    //DataMapKeys
    public static final String DATA_MAP_KEY_ACCURACY = "accuracy";
    public static final String DATA_MAP_KEY_TIMESTAMP = "timestamp";
    public static final String DATA_MAP_KEY_VALUES = "values";
    public static final String DATA_MAP_KEY_FILTER = "filter";

    public static final String DATA_MAP_KEY_BEACON_PROXIMITY = "proximity";

    //ClientPaths
    public static final String CLIENT_PATH_START_MEASUREMENT = "/start";
    public static final String CLIENT_PATH_STOP_MEASUREMENT = "/stop";

    //ServerPaths
    public static final String SERVER_PATH_SENSOR_DATA = "/sensors/";
    public static final String SERVER_PATH_BEACON_DATA = "/beacon/";


    //Commands
    public static final String COMMAND_CONNECT = "CONNECT";
    public static final String COMMAND_DISCONNECT = "DISCONNECT";
    public static final String COMMAND_CONFIRM = "CONFIRM";
    public static final String COMMAND_GET_CUES = "GET_CUES";
    public static final String COMMAND_NOT_AUTHENTICATED = "NOT_AUTHENTICATED";

    //States
    public static final int STATE_SERVER_RUNNING = 1000;
    public static final int STATE_SERVER_STOPPED = 1001;
    public static final int STATE_CONNECTED = -1000;
    public static final int STATE_DISCONNECTED = -1001;
    public static final int STATE_CONFIRM = -2000;
    public static final int STATE_HEART_BEATING = -3000;
    public static final int STATE_HEART_STOPPED = -3001;
    public static final int STATE_AUTHENTICATED = -4000;
    public static final int STATE_NOT_AUTHENTICATED = -4001;

    public static final int ERROR = -9999;

    //Message Prefix
    public static final String SENSORDATA_PREFIX = "SENSORDATA";
    public static final String BEACONDATA_PREFIX = "BEACONDATA";


    //Proximity States
    public static final String PROXIMITY_IMMEDIATE = "immediate";
    public static final String PROXIMITY_NEAR = "near";
    public static final String PROXIMITY_FAR = "far";


    //Beacon Service Constants
    public static final String BEACON_RESULT = "com.android.lmu.tokt.authenticator.BeaconService.REQUEST_PROCESSED";
    public static final String BEACON_MESSAGE = "com.android.lmu.tokt.authenticator.BeaconService.BEACON_MESSAGE";


    //Sensor Service Constants
    public static final String SENSOR_HEART_RATE_RESULT = "com.android.lmu.tokt.authenticator.SensorService.HEART_RATE_REQUEST_PROCESSED";
    public static final String SENSOR_HEART_RATE_MESSAGE = "com.android.lmu.tokt.authenticator.SensorService.HEART_REATE_MESSAGE";
    public static final String SENSOR_STEP_COUNT_RESULT = "com.android.lmu.tokt.authenticator.SensorService.STEP_COUNT_REQUEST_PROCESSED";
    public static final String SENSOR_STEP_COUNT_MESSAGE = "com.android.lmu.tokt.authenticator.SensorService.BEACON_MESSAGE";

    //MessageReceiverService
    public static final String MESSAGE_RECEIVER_RESULT = "com.android.lmu.tokt.authenticator.MessageReceiverService.REQUEST_PROCESSED";
    public static final String MESSAGE_RECEIVER_MESSAGE = "com.android.lmu.tokt.authenticator.MessageReceiverService.MESSAGE_RECEIVER_MESSAGE";

}