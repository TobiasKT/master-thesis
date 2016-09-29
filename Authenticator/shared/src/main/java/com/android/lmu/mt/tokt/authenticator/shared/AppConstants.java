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

    //Beacon
    public final static String DEFAULT_BEACON_UUID = "E2C56DB5-DFFB-48D2-B060-D0F5A71096E0";


    //Shared Preferences
    public final static String SHARED_PREF_APP_KEY = "com.android.lmu.mt.tokt.authenticator.PREFERENCE_FILE_KEY";
    public final static String SHARED_PREF_SEVER_IP = "server_ip";
    public final static String SHARED_PREF_SEVER_PORT = "server_port";
    public static final String SHARED_PREF_BEACON_UUID = "beacon_uuid";


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

    //BeaconType
    public final static int SENSOR_TYPE_BEACON = 99988;

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

    //BeaconName
    public final static String SENSOR_NAME_BEACON = "BEACON";


    //DataMapKeys
    public static final String DATA_MAP_KEY_ACCURACY = "accuracy";
    public static final String DATA_MAP_KEY_TIMESTAMP = "timestamp";
    public static final String DATA_MAP_KEY_VALUES = "values";
    public static final String DATA_MAP_KEY_FILTER = "filter";

    public static final String DATA_MAP_KEY_BEACON_PROXIMITY = "proximity";

    //ClientPaths
    public static final String CLIENT_PATH_START_MEASUREMENT = "/start";
    public static final String CLIENT_PATH_STOP_MEASUREMENT = "/stop";
    public static final String CLIENT_PATH_LISTEN_TO_SOUND = "/listen";
    public static final String CLIENT_PATH_USER_AUTHENTICATED = "/authenticated";
    public static final String CLIENT_PATH_USER_NOT_AUTHENTICATED = "/not_authenticated";
    public static final String CLIENT_PATH_LOCKED = "/locked";
    public static final String CLIENT_PATH_UNLOCKED = "/unlocked";
    public static final String CLIENT_PATH_BEACON_UUID = "/beacon_uuid";
    public static final String CLIENT_PATH_START_KEYPRESS_DETECTOR = "/start_key_detector";
    public static final String CLIENT_PATH_STOP_KEYPRESS_DETECTOR = "/stop_key_detector";

    //ServerPaths
    public static final String SERVER_PATH_SENSOR_DATA = "/sensors/";
    public static final String SERVER_PATH_BEACON_DATA = "/beacon/";


    //Commands
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
    //
    public static final int START_LISTEN_TO_SOUND = -5000;

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
    public static final int STATE_LOCKED = -5001;
    public static final int STATE_UNLOCKED = -5002;

    public static final int STATE_SEND_TYPING_VALUES = -7001;
    public static final int STATE_STOP_SENDING_TYPING_VALUES = -7002;

    public static final int SET_USERNAME = -6001;

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
    public static final String BEACON_IDENTIFIER_RESULT = "com.android.lmu.tokt.authenticator.BeaconService.ID_REQUEST_PROCESSED";
    public static final String BEACON_MESSAGE = "com.android.lmu.tokt.authenticator.BeaconService.BEACON_MESSAGE";
    public static final String BEACON_IDENTIFIER_MESSAGE = "com.android.lmu.tokt.authenticator.BeaconService.ID_BEACON_MESSAGE";
    public static final String BEACON_IDENTIFIER_STRING = "B0702880-A295-A8AB-F734-031A98A512DE";


    //Sensor Service Constants
    public static final String SENSOR_HEART_RATE_RESULT = "com.android.lmu.tokt.authenticator.SensorService.HEART_RATE_REQUEST_PROCESSED";
    public static final String SENSOR_HEART_RATE_MESSAGE = "com.android.lmu.tokt.authenticator.SensorService.HEART_REATE_MESSAGE";
    public static final String SENSOR_STEP_COUNT_RESULT = "com.android.lmu.tokt.authenticator.SensorService.STEP_COUNT_REQUEST_PROCESSED";
    public static final String SENSOR_STEP_COUNT_MESSAGE = "com.android.lmu.tokt.authenticator.SensorService.BEACON_MESSAGE";


    //Sound Listening Constants
    public static final String SOUND_LISTENING_RESULT = "com.android.lmu.tokt.authenticator.SensorService.SOUND_LISTENING_REQUEST_PROCESSED";
    public static final String SOUND_LISTENING_MESSAGE = "com.android.lmu.tokt.authenticator.SensorService.SOUND_LISTENING_MESSAGE";
    public static final String SOUND_PASSWORD = "welcome back";

    //MessageReceiverService
    public static final String MESSAGE_RECEIVER_RESULT = "com.android.lmu.tokt.authenticator.MessageReceiverService.REQUEST_PROCESSED";
    public static final String MESSAGE_RECEIVER_MESSAGE = "com.android.lmu.tokt.authenticator.MessageReceiverService.MESSAGE_RECEIVER_MESSAGE";

    //MessageReceiverServeice locked
    public static final String MESSAGE_RECEIVER_LOCK_RESULT = "com.android.lmu.tokt.authenticator.MessageReceiverService.REQUEST_LOCK_PROCESSED";
    public static final String MESSAGE_RECEIVER_LOCK_MESSAGE = "com.android.lmu.tokt.authenticator.MessageReceiverService.MESSAGE_RECEIVER_LOCK_MESSAGE";


    //LogKeys
    public static final String LOG_KEY_ANDROID_DEVICE = "android_device";
    public static final String LOG_KEY_USERNAME = "username";
    public static final String LOG_KEY_TIMESTAMP = "timestamp";
    public static final String LOG_KEY_DATE = "date";
    public static final String LOG_KEY_X = "x";
    public static final String LOG_KEY_Y = "y";
    public static final String LOG_KEY_Z = "z";
    public static final String LOG_KEY_ACCURACY = "accuracy";
    public static final String LOG_KEY_DATASOURCE = "datasource";
    public static final String LOG_KEY_SENSORNAME = "sensorname";
    public static final String LOG_KEY_DATATYPE = "datatype";

    public static final String LOG_KEY_TAG_EVENT = "tag_event";
    public static final String LOG_KEY_APP_EVENT = "app_event";
    public static final String LOG_KEY_STATE = "state";
    public static final String LOG_KEY_USERNOTE = "usernote";


}
