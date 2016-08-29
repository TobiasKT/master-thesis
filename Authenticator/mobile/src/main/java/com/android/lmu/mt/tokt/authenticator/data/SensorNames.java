package com.android.lmu.mt.tokt.authenticator.data;

import android.util.SparseArray;

import com.android.lmu.mt.tokt.authenticator.shared.AppConstants;

/**
 * Created by tobiaskeinath on 28.08.16.
 */
public class SensorNames {

    public SparseArray<String> names;

    public SensorNames() {
        names = new SparseArray<String>();

        names.append(AppConstants.SENSOR_TYPE_DEBUG, AppConstants.SENSOR_NAME_DEBUG);
        names.append(AppConstants.SENSOR_TYPE_ACCELEROMETER, AppConstants.SENSOR_NAME_ACCELEROMETER);
        names.append(AppConstants.SENSOR_TYPE_STEP_COUNTER, AppConstants.SENSOR_NAME_STEP_COUNTER);
        names.append(AppConstants.SENSOR_TYPE_WRIST_TILT, AppConstants.SENSOR_NAME_WRIST_TILT);
        names.append(AppConstants.SENSOR_TYPE_GYRO, AppConstants.SENSOR_NAME_GYRO);
        names.append(AppConstants.SENSOR_TYPE_LIGHT, AppConstants.SENSOR_NAME_LIGHT);
        names.append(AppConstants.SENSOR_TYPE_HEART_RATE, AppConstants.SENSOR_NAME_HEART_RATE);
        names.append(AppConstants.SENSOR_TYPE_GAME_ROTATION, AppConstants.SENSOR_NAME_GAME_ROTATION);
        names.append(AppConstants.SENSOR_TYPE_WELLNESS_PASSIVE, AppConstants.SENSOR_NAME_WELLNESS_PASSIVE);
        names.append(AppConstants.SENSOR_TYPE_PPG, AppConstants.SENSOR_NAME_PPG);
        names.append(AppConstants.SENSOR_TYPE_SIGNIFICANT_MOTION, AppConstants.SENSOR_NAME_SIGNIFICANT_MOTION);
        names.append(AppConstants.SENSOR_TYPE_DETAILED_STEP_COUNTER, AppConstants.SENSOR_NAME_DETAILED_STEP_COUNTER);
        names.append(AppConstants.SENSOR_TYPE_USER_PROFILE, AppConstants.SENSOR_NAME_USER_PROFILE);
        names.append(AppConstants.SENSOR_TYPE_USER_STRIDE_FACTOR, AppConstants.SENSOR_NAME_USER_STRIDE_FACTOR);
        names.append(AppConstants.SENSOR_TYPE_GRAVITY, AppConstants.SENSOR_NAME_GRAVITY);
        names.append(AppConstants.SENSOR_TYPE_LINEAR_ACCELERATION, AppConstants.SENSOR_NAME_LINEAR_ACCELERATION);
    }


    public String getName(int sensorId) {
        String name = names.get(sensorId);

        if (name == null) {
            name = "Unknown";
        }

        return name;
    }

}
