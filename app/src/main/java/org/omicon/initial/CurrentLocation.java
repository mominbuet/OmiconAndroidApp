package org.omicon.initial;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

public class CurrentLocation {
    public static Location location = null;

    public static void ActivateLocationService(Context context) {
        LocationManager locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        LocationListener listener = new LocationListener() {
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProviderEnabled(String provider) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onLocationChanged(Location loc) {
                location = loc;
            }
        };
        /*if(locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
			location=locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}
		if(locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
			location=locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}*/
        try {
            LocationProvider provider = locManager.getProvider(LocationManager.GPS_PROVIDER);
            if (provider != null) {
                Log.d("msg", "GPS Listener Activated");
                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
                location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } else
                Log.d("msg", "GPS Listener NOT Activated");
            provider = locManager.getProvider(LocationManager.NETWORK_PROVIDER);
            if (provider != null) {
                Log.d("msg", "NETWORK Listener Activated");
                locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
                location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } else
                Log.d("msg", "NETWORK Listener Not Activated");
        } catch (Exception ex) {
            Log.d("msg", "error occur at provider enable operation");
        }
    }
}
