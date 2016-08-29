package com.android.lmu.mt.tokt.authenticator.events;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.squareup.otto.Bus;

/**
 * Created by tobiaskeinath on 28.08.16.
 */
public final class BusProvider {

    private static final String TAG = BusProvider.class.getSimpleName();

    private static final Bus BUS = new Bus();

    private BusProvider() {

    }

    public static Bus getInstance() {
        return BUS;
    }

    public static void postOnMainThread(final Object event) {
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                BUS.post(event);
            }
        });
    }

    public static void updateTextViewOnMainThread(final TextView textView, final String text) {
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                textView.setText(text);
            }
        });
    }
}
