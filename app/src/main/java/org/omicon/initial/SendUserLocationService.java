package org.omicon.initial;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;
import org.omicon.helper.notificationBuilder;
import org.omicon.serversync.UploadTask;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.IntentService;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;

import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class SendUserLocationService extends IntentService {

    //private ArrayList<String> locationLists=null ;
    String[] splitedValues;
    String longitude, latitude;
    private JSONObject user_info = null;
    String user_no;
    SharedPreferences prefs;

    SimpleDateFormat sdfOfflineDate = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat sdfOfflineTime = new SimpleDateFormat("hh:mm:ss a");
    Dialog dialog;

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

    /*@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);
        return START_REDELIVER_INTENT;
        //

    }*/

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        //locationLists = new ArrayList<String>() ;
        prefs = getApplicationContext().getSharedPreferences("MY_PREFS", 0);
        user_no = prefs.getString("user_no", "");
        //mainfunc();
    }

    public SendUserLocationService() {
        super("LocationService");

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

    public void mainfunc() {
        if (check_gps()) {
            Location currentLocation = Global.currentLocation();

            Log.e("ServiceLocation", "At Service:" + currentLocation);
            if (currentLocation != null) {
                Global.locationList.add(Double.toString(currentLocation.getLatitude()) + "#" + Double.toString(currentLocation.getLongitude()) + "#" + Long.toString(Calendar.getInstance().getTimeInMillis()) + "#" + String.valueOf(getBatteryLevel()));

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

                Log.e("ServiceLocation", "Added:" + Global.locationList.size());
            } else
                Log.e("ServiceLocation", "No Add");


            if (Global.locationList.size() > 5) {
                Global.locationList.clear();
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
                            Log.e("msg", "Location Movement upload error");
                        }
                    }
                }).start();

		/*	user_info =  new JSONObject();
            SharedPreferences prefs= getApplicationContext().getSharedPreferences("MY_PREFS",0);

			JSONArray location_infos = new JSONArray();
			for (String strItems : Global.locationList) {
				splitedValues = strItems.split("#");
				latitude = splitedValues[0];
				longitude = splitedValues[1];
				try{
					JSONObject location_info = new JSONObject();
					location_info .put("MOVE_DATE", sdfOfflineDate.format(Long.parseLong(splitedValues[2])));
					location_info.put("MOVE_TIME", sdfOfflineTime.format(Long.parseLong(splitedValues[2])));
					location_info.put("LON_VAL", longitude);
					location_info.put("LAT_VAL", latitude);

					location_info.put("BATT_PCT",splitedValues[3]);

					location_info.put("USER_NO", prefs.getString("user_no", ""));

					location_infos.put(location_info);
				}catch(Exception ex){
					Log.d("ServiceLocation","error in creating json object");

				}
			}

			Global.locationList.clear();

			//SharedPreferences prefs= getApplicationContext().getSharedPreferences("MY_PREFS",0);
			try {
				JSONObject jsonUser = new JSONObject();
				jsonUser.put("USER_NAME",prefs.getString("user_email", ""));
				user_info.put( "USER_INFO", jsonUser);
				user_info.put("TRN_USER_MOVEMENTS_UP", location_infos);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}



			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					String dta = "";
					HttpClient client=new DefaultHttpClient();

					HttpResponse response=null;

					HttpPost post=new HttpPost(Global.uploadMovement);
					try {

						Log.d("ServiceLocation", user_info.toString());
						StringEntity en=new StringEntity(user_info.toString());

						en.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
						post.setEntity(en);

						response = client.execute(post);
						if(response == null)
						{
							Log.d("ServiceLocation", "server not respond");
						}
						else
						{
							Log.d("ServiceLocation","data sent");
							InputStream input=response.getEntity().getContent();
							BufferedReader reader=new BufferedReader(new InputStreamReader(input));
							String line;
							while((line=reader.readLine())!=null)
								dta=dta+line;
							Log.d("ServiceLocation","data:"+dta);
						}
					}catch(ClientProtocolException cpe){

					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch(Exception ex){}
				}
			}).start();
			Log.d("ServiceLocation", "Enter In Location Service Sent");
			*/
            } else {
                Log.d("ServiceLocation", "Size less than 5");
            }
        } else
            Log.e("GPS off", "No Add");


    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub
        mainfunc();
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
