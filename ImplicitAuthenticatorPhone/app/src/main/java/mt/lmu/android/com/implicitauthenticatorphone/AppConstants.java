package mt.lmu.android.com.implicitauthenticatorphone;

/**
 * Created by tobiaskeinath on 17.08.16.
 */
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


}
