package org.omicon.initial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.omicon.helper.location_service;

public class LocationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent locationUpdater = new Intent(context, SendUserLocationService.class);
        //Intent locationUpdater = new Intent(context, location_service.class);
        context.startService(locationUpdater);

    }

}


