package org.omicon.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.omicon.R;
import org.omicon.initial.Global;
import org.omicon.initial.HomeActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class LocationEntry extends Activity {

    private static final int GPS_TIME_INTERVAL = 0; // get gps location every 1 min
    private static final int GPS_DISTANCE = 0; // set the distance value in meter
    private SimpleDateFormat sdfOfflineTime = null;
    private LocationManager locMan = null;
    private Button btnShowLocation, btnCancel;
    private EditText etUserLocation;
    private TextView tvShowLocationInfo;
    Location gpslocation = null;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    private TableLayout tblShowLocations = null;
    private static String hiddenID = "", edit_entry_is_uploaded = "";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem homeIconMenu = menu.findItem(R.id.homeIcon);
        SharedPreferences prefsUser = getSharedPreferences("MY_PREFS", 0);
        homeIconMenu.setTitle(prefsUser.getString("user_name", ""));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.backIcon)
            finish();
        else if (item.getItemId() == R.id.homeIcon) {
            Intent intent = new Intent(LocationEntry.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_activity);

        sdfOfflineTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
        sdfOfflineTime.setTimeZone(TimeZone.getTimeZone("GMT+06:00"));

        tblShowLocations = (TableLayout) findViewById(R.id.tblShowLocations);
        tvShowLocationInfo = (TextView) findViewById(R.id.tvShowLocationInfo);
        etUserLocation = (EditText) findViewById(R.id.etUserLocation);

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                etUserLocation.setText("");
                btnShowLocation.setText("Save Location");
                hiddenID = "";
            }
        });

        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);

        btnShowLocation.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                //obtainLocation();
                if (gpslocation != null) {
                    String userLocation = etUserLocation.getText().toString();
                    if (userLocation.length() > 2) {
                        if (((Button) arg0).getText().toString().equalsIgnoreCase("update")) {
                            saveLocationToDb(userLocation, hiddenID);
                        } else {
                            saveLocationToDb(userLocation, "");
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Please Enter at least 3 characters", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Could not find GPS!", Toast.LENGTH_SHORT).show();
                }
            }

        });

        locMan = (LocationManager) getSystemService(LOCATION_SERVICE);

        gps_enabled = locMan.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = locMan.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gps_enabled && !network_enabled) {
            Toast.makeText(getApplicationContext(), "No Network OR GPS found!", Toast.LENGTH_SHORT).show();
        }

        MyLocationList listener = new MyLocationList();

        if (network_enabled) {
            //Toast.makeText(getApplicationContext(), "location from Network", Toast.LENGTH_LONG).show();

            locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GPS_TIME_INTERVAL, GPS_DISTANCE, listener);
            gpslocation = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (gps_enabled) {
            //Toast.makeText(getApplicationContext(), "location from GPS", Toast.LENGTH_LONG).show();
            locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_TIME_INTERVAL, GPS_DISTANCE, listener);
            gpslocation = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }


        ShowCurrentLocation();
        ShowAllLocations();
    }


    private void ShowCurrentLocation() {
        // TODO Auto-generated method stub
        if (gpslocation != null) {
            ArrayList<String[]> dbLocationSet = Global.dbObject.queryFromTable("TRN_USER_LOCATION", new String[]{"OFFLINE_LOC_NO", "LOCATION_NAME", "IS_UPLOADED", "ENTRY_STATE"}, " ENTRY_STATE <> 3 AND LAT_VAL = " + gpslocation.getLatitude() + " AND LON_VAL = " + gpslocation.getLongitude());
            Log.d("edit_first", String.valueOf(dbLocationSet.size()));
            if (dbLocationSet.size() > 0) {
                hiddenID = dbLocationSet.get(0)[0];
                if (dbLocationSet.get(0)[2].equalsIgnoreCase("0") && dbLocationSet.get(0)[3].equalsIgnoreCase("1")) {
                    edit_entry_is_uploaded = "1";
                } else {
                    edit_entry_is_uploaded = "2";
                }

                etUserLocation.setText(dbLocationSet.get(0)[1]);
                btnShowLocation.setText("Update");
            }
        } else {
            Toast.makeText(getApplicationContext(), "GPS NULL", Toast.LENGTH_LONG).show();
        }

    }


    private void saveLocationToDb(String userLocation, String hiddenID) {
        // TODO Auto-generated method stub
        ContentValues TRN_user_location = new ContentValues();
        TRN_user_location.put("ACTION_OFFLINE_TIME", Calendar.getInstance().getTimeInMillis());

        TRN_user_location.put("LOCATION_NAME", userLocation);

        if (hiddenID == "") {
            TRN_user_location.put("LAT_VAL", gpslocation.getLatitude());
            TRN_user_location.put("LON_VAL", gpslocation.getLongitude());
            TRN_user_location.put("OFFLINE_LOC_NO", Global.dbObject.GetNextRowID("TRN_USER_LOCATION", "OFFLINE_LOC_NO"));
            Log.d("TRN", TRN_user_location.toString());
            long id = Global.dbObject.insertIntoTable("TRN_USER_LOCATION", TRN_user_location);

            if (id > 0) {
                Toast.makeText(getApplicationContext(), "Added!", Toast.LENGTH_SHORT).show();
            }
        } else {

            TRN_user_location.put("ENTRY_STATE", edit_entry_is_uploaded);


            TRN_user_location.put("IS_UPLOADED", "0");
            int rowid = Global.dbObject.updateIntoTable("TRN_USER_LOCATION", TRN_user_location, " OFFLINE_LOC_NO = " + hiddenID);
            if (rowid > 0) {
                Toast.makeText(getApplicationContext(), "Updated!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Could not Update!", Toast.LENGTH_SHORT).show();
            }

            btnShowLocation.setText("Save Location");
            hiddenID = "";
        }

        etUserLocation.setText("");
        ShowAllLocations();
    }

    private void ShowAllLocations() {
        tblShowLocations.removeAllViews();

        //Add the header columns

        TableRow trExpItem = new TableRow(getApplicationContext());
        TextView tvIds = new TextView(getApplicationContext());
        tvIds.setText("ID");
        tvIds.setLayoutParams(new TableRow.LayoutParams(0));
        tvIds.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Large);

        TextView tvLoc = new TextView(getApplicationContext());
        tvLoc.setText("Location");
        tvLoc.setLayoutParams(new TableRow.LayoutParams(1));
        tvLoc.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Large);

        TextView tvEdit = new TextView(getApplicationContext());
        tvEdit.setText("Edit");
        tvEdit.setLayoutParams(new TableRow.LayoutParams(2));
        tvEdit.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Large);

        TextView tvDel = new TextView(getApplicationContext());
        tvDel.setText("Delete");
        tvDel.setLayoutParams(new TableRow.LayoutParams(3));
        tvDel.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Large);

        trExpItem.addView(tvIds);
        trExpItem.addView(tvLoc);
        trExpItem.addView(tvEdit);
        trExpItem.addView(tvDel);
        trExpItem.setBackgroundColor(Color.BLACK);
        tblShowLocations.addView(trExpItem, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // Header additon ended

        //int ids = 1 ;
        ArrayList<String[]> dbLocationSet = Global.dbObject.queryFromTable("TRN_USER_LOCATION", new String[]{"OFFLINE_LOC_NO", "LOCATION_NAME"}, " ENTRY_STATE <> 3 ");
        for (String[] strings : dbLocationSet) {
            trExpItem = new TableRow(getApplicationContext());

            //trExpItem.setId(ids++);
            TextView tvExp = new TextView(getApplicationContext());
            tvExp.setText(strings[0]);
            TextView tvAmt = new TextView(getApplicationContext());
            tvAmt.setText(strings[1]);
            Button btnEditLocation = new Button(getApplicationContext());
            btnEditLocation.setText("Edit");
            btnEditLocation.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    // TODO Auto-generated method stub
                    ViewGroup locationRow = (ViewGroup) view.getParent();
                    String locText = ((TextView) locationRow.getChildAt(1)).getText().toString();
                    hiddenID = ((TextView) locationRow.getChildAt(0)).getText().toString();
                    ArrayList<String[]> dbLocationById = Global.dbObject.queryFromTable("TRN_USER_LOCATION", new String[]{"IS_UPLOADED", "ENTRY_STATE"}, " OFFLINE_LOC_NO= " + hiddenID);
                    Log.d("db_ens", dbLocationById.get(0)[1]);
                    Log.d("db_up", dbLocationById.get(0)[0]);
                    if (dbLocationById.get(0)[0].equalsIgnoreCase("0") && dbLocationById.get(0)[1].equalsIgnoreCase("1")) {
                        edit_entry_is_uploaded = "1";
                    } else {
                        edit_entry_is_uploaded = "2";
                    }

                    etUserLocation.setText(locText);
                    btnShowLocation.setText("Update");
                }
            });

            Button btnDeleteLocation = new Button(getApplicationContext());
            btnDeleteLocation.setText("Delete");
            btnDeleteLocation.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    // TODO Auto-generated method stub
                    ViewGroup locationRow = (ViewGroup) view.getParent();
                    String rowId = ((TextView) locationRow.getChildAt(0)).getText().toString();

                    ContentValues values = new ContentValues();
                    values.put("ENTRY_STATE", "3");
                    values.put("IS_UPLOADED", "0");
                    int rows = Global.dbObject.updateIntoTable("TRN_USER_LOCATION", values, " OFFLINE_LOC_NO = " + rowId);
                    Log.d("db_del", String.valueOf(rows));
                    if (rows > 0) {
                        btnShowLocation.setText("Save Location");
                        hiddenID = "";
                        etUserLocation.setText("");
                        Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT).show();
                        tblShowLocations.removeView(locationRow);
                    } else {
                        Toast.makeText(getApplicationContext(), "Can't be Deleted", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            trExpItem.addView(tvExp);
            trExpItem.addView(tvAmt);
            trExpItem.addView(btnEditLocation);
            trExpItem.addView(btnDeleteLocation);

            tblShowLocations.addView(trExpItem, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }

    }

    private void sendLocationsToServer() {
        if (gpslocation != null) {
            /*ArrayList<String[]> locationsList = dbObject.queryFromTable("TRN_USER_LOCATION", null, "IS_UPLOADED = 0");
			Log.d("locations", locationsList.toString());
			for (String[] locationItemRow : locationsList) {				
				
				Log.d("OFFLINE_LOC_NO", locationItemRow[0]);	
				Log.d("entry_state", locationItemRow[5]);	
				Log.d("ACTION_OFFLINE_TIME", sdfOfflineTime.format(Long.parseLong(locationItemRow[1])));
			}*/

            new SendUserLocationDataTask().execute();
        } else {
            Toast.makeText(getApplicationContext(), "GPS not Set ..", Toast.LENGTH_LONG).show();
        }
    }

    public class MyLocationList implements LocationListener {

        public void onLocationChanged(Location locGps) {

            //Toast.makeText(getApplicationContext(), Double.toString(locGps.getLatitude()), Toast.LENGTH_LONG).show();
            gpslocation = locGps;
            tvShowLocationInfo.setText("Lat: " + Double.toString(gpslocation.getLatitude()) + "   Long: " + Double.toString(gpslocation.getLongitude()));
        }

        public void onProviderDisabled(String provider) {
            // Toast.makeText(getApplicationContext(), "GPS Disable ",
            //         Toast.LENGTH_LONG).show();
        }

        public void onProviderEnabled(String provider) {
            // Toast.makeText(getApplicationContext(), "GPS enabled",
            //       Toast.LENGTH_LONG).show();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

    }

    // Send data to server as a list of JSONs

    private class SendUserLocationDataTask extends AsyncTask<Void, Void, String> {
        private String send_url = "http://172.16.24.131:6060/WS_UploadApi/Upload_UserLocation";

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String dta = "";
            JSONObject locatoin_object = new JSONObject();
            JSONArray location_infos = new JSONArray();
            JSONObject location_info;

            ArrayList<String[]> locationsList = Global.dbObject.queryFromTable("TRN_USER_LOCATION", null, "IS_UPLOADED = 0");
            try {

                for (String[] locationItemRow : locationsList) {

                    location_info = new JSONObject();

                    location_info.put("OFFLINE_LOC_NO", locationItemRow[0]);
                    location_info.put("ACTION_OFFLINE_TIME", sdfOfflineTime.format(Long.parseLong(locationItemRow[1])));
                    location_info.put("LAT_VAL", locationItemRow[2]);
                    location_info.put("LON_VAL", locationItemRow[3]);
                    location_info.put("LOCATION_NAME", locationItemRow[4]);
                    location_info.put("ENTRY_STATE", locationItemRow[5]);

                    location_infos.put(location_info);

                }

            } catch (JSONException ex) {
                Log.d("msg", "error in creating json object");

            }

            if (location_infos.length() < 1) {
                return dta;
            }

            HttpClient client = new DefaultHttpClient();

            HttpResponse response = null;

            HttpPost post = new HttpPost(send_url);
            try {

                locatoin_object.put("TRN_USER_LOCATION_UP", location_infos);
                Log.d("json", locatoin_object.toString());
                StringEntity en = new StringEntity(locatoin_object.toString());

                en.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                post.setEntity(en);

                response = client.execute(post);
                if (response == null) {
                    Log.d("msg", "server not respond");
                } else {
                    Log.d("msg", "data sent");
                    InputStream input = response.getEntity().getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    String line;
                    while ((line = reader.readLine()) != null)
                        dta = dta + line;
                    Log.d("msg", "data:" + dta);

                    if (dta.toLowerCase(Locale.US).contains("true")) {
                        ContentValues TRN_user_location = new ContentValues();
                        TRN_user_location.put("IS_UPLOADED", "1");

                        for (String[] locationItemRow : locationsList) {

                            if (locationItemRow[5].equalsIgnoreCase("3")) {
                                Global.dbObject.deleteFromTable("TRN_USER_LOCATION", " OFFLINE_LOC_NO = " + locationItemRow[0]);
                                Log.d("db_del", "deleted");
                            } else {
                                int rowid = Global.dbObject.updateIntoTable("TRN_USER_LOCATION", TRN_user_location, " OFFLINE_LOC_NO = " + locationItemRow[0]);

                                Log.d("db_send", String.valueOf(rowid));
                            }

                        }
                    }
                }

            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.d("msg", "error to encode json array");
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.d("msg", "error to send data due protocol exception ,Type:" + e.getMessage());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.d("msg", "error to send data due to IO exception ,Type:" + e.getMessage());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return dta;
        }

        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

        }
    }

}


