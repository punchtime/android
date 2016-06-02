package io.punchtime.punchtime.logic.loaders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.Loader;

/**
 * Created by elias on 28/04/16.
 * for project: Punchtime
 */
public class TimerLoader extends Loader<Void> {
    private BroadcastReceiver mBroadcastReceiver;

    public TimerLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0)
                    deliverResult(null);
            }
        };

        getContext().registerReceiver(mBroadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }
    @Override
    protected void onStopLoading() {
        if (mBroadcastReceiver != null)
            getContext().unregisterReceiver(mBroadcastReceiver);
    }
    @Override
    protected void onForceLoad() {
        deliverResult(null);
    }

    @Override
    protected void onReset() {
    }
}
