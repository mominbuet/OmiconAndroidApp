package org.omicon.helper;

import android.app.Dialog;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.omicon.initial.Global;
import org.omicon.initial.logger;
import org.omicon.serversync.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


import android.app.Service;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;


public class location_service extends Service {
    String key_time = "lastUpdate";
    //private ArrayList<String> locationLists=null ;
    private JSONObject user_info = null;
    String user_no;
    SharedPreferences prefs;
    volatile int flg = 0;
    SimpleDateFormat sdfOfflineDate = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat sdfOfflineTime = new SimpleDateFormat("hh:mm:ss a");
    Dialog dialog;
    Context context;
    public static ArrayList<String> locationList = new ArrayList<String>();
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };
    Thread thread = new Thread() {
        @Override
        public void run() {
            try {
                while (true) {
                    long diff = Calendar.getInstance().getTimeInMillis() - prefs.getLong(key_time, 0);

                    if (prefs.getLong(key_time, 0) == 0 || ((int) diff / (1000 * 60)) >= 1) {
                        mainfunc();
                        //handler.postDelayed(this, 1000 * 30); // 1000 - Milliseconds
                        flg = 1;
                        prefs.edit().putLong(key_time, Calendar.getInstance().getTimeInMillis()).apply();
                        Log.e("location_thred", "Location Service running");
                        //new logger(getApplicationContext()).appendLog("Created location" + Calendar.getInstance().getTime());
                        sleep(1000 * 30);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            long diff = Calendar.getInstance().getTimeInMillis() - prefs.getLong(key_time, 0);

            if (prefs.getLong(key_time, 0) == 0 || diff >= Global.run_freq_location) {
                mainfunc();

                flg = 1;
                prefs.edit().putLong(key_time, Calendar.getInstance().getTimeInMillis()).apply();

                //new logger(getApplicationContext()).appendLog("Created location" + Calendar.getInstance().getTime());
                //Thread.sleep(1000 * 30);
            }
            Log.e("location_runnable", "Location Service running");
            handler.postDelayed(this, Global.run_freq_location); // 1000 - Milliseconds


        }
    };

    private boolean check_gps() {
        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.e("location here", "GPS turn on");
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            notificationBuilder.generateNotification(getApplicationContext(), "GPS OFF", "Please turn on your GPS", intent, true);
           /* Toast toast = Toast.makeText(getApplicationContext(), "Please turn on your gps", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();*/
            return false;
        }
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //super.onStartCommand(intent,flags,startId);
        //mainfunc();

        return START_STICKY;
        //

    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        Fabric.with(this, new Crashlytics());
        //locationLists = new ArrayList<String>() ;
        prefs = getApplicationContext().getSharedPreferences("MY_PREFS", 0);
        user_no = prefs.getString("user_no", "");

        //handler.postDelayed(runnable,1000);
//            if (!runnable.)
        /*if(!thread.isAlive())
            thread.run();*/
        handler.postDelayed(runnable, 0);
        // mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        /*Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);

        String provider = mLocationManager.getBestProvider(criteria, true);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                //makeUseOfNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        mLocationManager.requestLocationUpdates(provider, 10*60*1000, 100,locationListener);/*/

        //mainfunc();
        //new logger(getApplicationContext()).appendLog("Created location"+ Calendar.getInstance().getTime());
    }

    public location_service() {
        currentLocation = Global.currentLocation();
        //super("LocationService");

    }

    public float getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if (level == -1 || scale == -1) {
            return -1.0f;
        }

        return ((float) level / (float) scale) * 100.0f;
    }

    public ArrayList<JSON_Range> getLocationMovementJson() {
        ArrayList<JSON_Range> result_list = new ArrayList<JSON_Range>();
        try {
            ArrayList<String[]> locationsList = Global.dbObject.rawqueryFromDatabase("SELECT * FROM TRN_MOVEMENT ORDER BY MOVEMENT_ID");
            String low = "";
            if (locationsList.size() != 0)
                low = locationsList.get(0)[0];

            JSONObject user_info = new JSONObject();
            user_info.put("USER_NAME", prefs.getString("user_email", ""));

            JSONObject locatoin_object = new JSONObject();
            JSONArray location_infos = new JSONArray();
            JSONObject location_info;


            for (int i = 0; i < locationsList.size(); i++) {
                location_info = new JSONObject();
                /*
                 location_info .put("MOVE_DATE", sdfOfflineDate.format(Long.parseLong(splitedValues[2])));
					location_info.put("MOVE_TIME", sdfOfflineTime.format(Long.parseLong(splitedValues[2])));
					location_info.put("LON_VAL", longitude);
					location_info.put("LAT_VAL", latitude);

					location_info.put("BATT_PCT",splitedValues[3]);

					location_info.put("USER_NO", prefs.getString("user_no", ""));
				 * */


                location_info.put("MOVE_DATE", locationsList.get(i)[1]);
                location_info.put("MOVE_TIME", locationsList.get(i)[2]);
                location_info.put("LON_VAL", locationsList.get(i)[3]);
                location_info.put("LAT_VAL", locationsList.get(i)[4]);
                location_info.put("BATT_PCT", locationsList.get(i)[5]);
                location_info.put("USER_NO", locationsList.get(i)[6]);

                location_infos.put(location_info);

                if ((i + 1) % 8 == 0) {

					/*
                     user_info.put( "USER_INFO", jsonUser);
				user_info.put("TRN_USER_MOVEMENTS_UP", location_infos);
					 * */

                    locatoin_object.put("TRN_USER_MOVEMENTS_UP", location_infos);
                    locatoin_object.put("USER_INFO", user_info);
                    result_list.add(new JSON_Range(locatoin_object, low, locationsList.get(i)[0]));
                    low = locationsList.get(i)[0];

                    location_infos = new JSONArray();
                    locatoin_object = new JSONObject();
                }
            }
            if (locationsList.size() % 8 != 0) {
                locatoin_object.put("TRN_USER_MOVEMENTS_UP", location_infos);
                locatoin_object.put("USER_INFO", user_info);
                result_list.add(new JSON_Range(locatoin_object, low, locationsList.get(locationsList.size() - 1)[0]));
            }

        } catch (Exception ex) {
            Log.e("msg", "Error Creating Location Json Array");
        }
        return result_list;
    }

    Location currentLocation = null;

    public void mainfunc() {
        if (check_gps()) {
            currentLocation = Global.currentLocation();

            if (currentLocation == null) {
                LocationManager locManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

                LocationListener listener = new LocationListener() {
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }

                    @Override
                    public void onLocationChanged(Location loc) {
                        currentLocation = loc;
                    }
                };
                //LocationManager locManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                //locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
                try {
                    LocationProvider provider = locManager.getProvider(LocationManager.GPS_PROVIDER);
                    if (provider != null) {
                        //Log.e("msg", "GPS Listener Activated");
                        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
                        currentLocation = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    } else
                        Log.d("msg", "GPS Listener NOT Activated");
                    provider = locManager.getProvider(LocationManager.NETWORK_PROVIDER);
                    if (provider != null) {
                        Log.e("msg", "NETWORK Listener Activated");
                        locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
                        currentLocation = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    } else
                        Log.e("msg", "NETWORK Listener Not Activated");
                } catch (Exception ex) {
                    Log.d("msg", "error occur at provider enable operation");
                }
            }

            Log.e("ServiceLocation", "At Service:" + currentLocation);
            if (currentLocation != null) {
                Global.last_location = currentLocation;
                locationList.add(Double.toString(currentLocation.getLatitude()) + "#" + Double.toString(currentLocation.getLongitude()) + "#" + Long.toString(Calendar.getInstance().getTimeInMillis()) + "#" + String.valueOf(getBatteryLevel()));

                // insert Into Table
                try {
                    if ((currentLocation.getLatitude()) != 0) {
                        long pid = Global.dbObject.getLastRowID("TRN_MOVEMENT", "MOVEMENT_ID");
                        ContentValues values = new ContentValues();
                        values.put("MOVEMENT_ID", pid + 1);
                        values.put("MOVE_DATE", sdfOfflineDate.format(Calendar.getInstance().getTimeInMillis()));
                        values.put("MOVE_TIME", sdfOfflineTime.format(Calendar.getInstance().getTimeInMillis()));
                        values.put("LON_VAL", Double.toString(currentLocation.getLongitude()));
                        values.put("LAT_VAL", Double.toString(currentLocation.getLatitude()));
                        values.put("BATT_PCT", String.valueOf(getBatteryLevel()));
                        values.put("USER_NO", user_no);

                        Global.dbObject.insertIntoTable("TRN_MOVEMENT", values);
                    } else {
                        Log.e("Error in getting lat", "latitude");
                    }
                } catch (Exception e) {
                    Log.d("msg", "Error Inserted in Location Movement Table");
                }

                Log.d("ServiceLocation", "Added:" + locationList.size());
            }

            if (locationList.size() > 5) {
                locationList.clear();
                final ArrayList<JSON_Range> data_list = getLocationMovementJson();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            UploadTask uploader = new UploadTask(getApplicationContext());
                            for (JSON_Range json_Range : data_list) {
                                Log.d("msg", "Movement Json:" + json_Range.value.toString());
                                String dta = uploader.uploadToServer(Global.uploadMovement, json_Range.value.toString());
                                Log.d("msg", "Location Movement Upload Message" + dta);
                                if (dta.toLowerCase(Locale.US).contains("true")) {
                                    Global.dbObject.deleteFromTable("TRN_MOVEMENT", "MOVEMENT_ID>=" + json_Range.low + " AND MOVEMENT_ID<=" + json_Range.high);
                                }
                            }
                        } catch (Exception e) {
                            Log.d("msg", "Location Movement upload error");
                        }
                    }
                }).start();

            } else {
                Log.d("ServiceLocation", "Size less than 5");
            }
        } else
            Log.d("GPS off", "No Add");
        //new logger(getApplicationContext()).appendLog("Current size is " + locationList.size());
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        mainfunc();
        return null;
    }
}

class JSON_Range {
    JSONObject value;
    String low, high;

    JSON_Range(JSONObject v, String l, String h) {
        value = v;
        low = l;
        high = h;
    }
}
