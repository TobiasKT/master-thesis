package com.android.lmu.mt.tokt.authenticator.util;

import java.util.ArrayList;

/**
 * Created by tobiaskeinath on 28.09.16.
 */
public class Statistics {


    ArrayList<Float> data;
    int size;

    public Statistics(ArrayList<Float> data) {
        this.data = data;
        size = data.size();
    }

    public double getMean() {
        double sum = 0.0;
        for (double a : data)
            sum += a;
        return sum / size;
    }

    public double getVariance() {
        double mean = getMean();
        double temp = 0;
        for (double a : data)
            temp += (a - mean) * (a - mean);
        return temp / size;
    }

    public double getStdDev() {
        return Math.sqrt(getVariance());
    }
}
