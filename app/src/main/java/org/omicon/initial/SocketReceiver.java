package org.omicon.initial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SocketReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent SocketUpdater = new Intent(context, WebSocketService.class);
        context.startService(SocketUpdater);

    }
}
