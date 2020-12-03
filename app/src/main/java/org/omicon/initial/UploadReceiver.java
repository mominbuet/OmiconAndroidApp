package org.omicon.initial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UploadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent locationUpdater = new Intent(context, SendDataReceiver.class);
        context.startService(locationUpdater);
    }
}