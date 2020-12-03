package org.omicon.initial;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.acra.ACRA;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.omicon.R;
import org.omicon.entry.dcr_master;
import org.omicon.helper.Debug;
import org.omicon.serversync.NewVersionDownload;
import org.omicon.serversync.SyncServer;
import org.omicon.serversync.UploadTask;
import org.omicon.view.DerActivity;
import org.omicon.view.ReportDailyDCR;
import org.omicon.view.UserLocation;
import org.omicon.view.dcr_view;
import org.omicon.view.feedback;
import org.omicon.view.message_view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.Math.abs;

public class HomeActivity extends Activity {
    TextView messageCounter = null;
    ArrayList<String[]> data_user_info;
    GridView gridView = null;
    String New_version = null;
    String user_no;
    String Time_Out=null;
    Session session;
    TextView username=null;
    TextView header=null;
	/*
     * @Override public boolean onCreateOptionsMenu(Menu menu) {
	 * getMenuInflater().inflate(R.menu.menu, menu); MenuItem homeIconMenu =
	 * menu.findItem(R.id.homeIcon); SharedPreferences
	 * prefsUser=getSharedPreferences("MY_PREFS",0);
	 * 
	 * homeIconMenu.setTitle(prefsUser.getString("user_no","")); MenuItem
	 * backIconMenu = menu.findItem(R.id.backIcon);
	 * backIconMenu.setVisible(false); return true; }
	 * 
	 * @Override public boolean onOptionsItemSelected(MenuItem item) {
	 * 
	 * return super.onOptionsItemSelected(item); }
	 */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
     /*   username=(TextView)findViewById(R.id.UserName);
        SharedPreferences prefs = getSharedPreferences(
                "MY_PREFS", 0);
        String userName=prefs.getString("user_name","");
        if(userName!=null)
        {
            username.setText(userName);
        }
*/

        New_version = getIntent().getStringExtra("New_version");
        Time_Out=getIntent().getStringExtra("Time_Out");
        Integer t_o=60000;
        if(Time_Out!=null)
        {
            t_o=Integer.parseInt(Time_Out);
        }
        // Global.initialConfig(this)
        //Set primary sequence for id
        //////////////////////////////////////////////////////////////////////

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {

                Intent i = new Intent(HomeActivity.this, Login.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                return;
            }
        }, t_o);
        //////////////////////////////////////////////////////////////////////
        showDialogLocation();

        if(Global.dbObject != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("msg", "Primary Sequence Set");
                    Global.resetPrimarySeq();
                }
            }).start();
        }

        SharedPreferences prefsUser = getSharedPreferences("MY_PREFS", 0);
        user_no = prefsUser.getString("user_no", "");
        String tmpuser_email = prefsUser.getString("user_email", "");
        String user_name=prefsUser.getString("user_name", "");
        header=(TextView)findViewById(R.id.Header);
        header.setText("Welcome, "+user_name+"("+tmpuser_email+")");

        // socket old call
		/*
		 * new Thread(new Runnable() {
		 * 
		 * @Override public void run() {
		 * 
		 * //mWebSocketClient.send("app"); } }).start();
		 */

        // This Thread For Download Data
		/*
		 * new Thread(new Runnable() {
		 * 
		 * @Override public void run() { try{ SharedPreferences
		 * prefs=getSharedPreferences("MY_PREFS",0);
		 * 
		 * DownloadTask dtask=new DownloadTask(HomeActivity.this); JSONObject
		 * result
		 * =dtask.DownloadDataFromServer(Global.downloadLink+prefs.getString
		 * ("user_email", "")); if(result.getString("Success").matches("true"))
		 * dtask.executeData(result.getString("Content"));
		 * Log.d("msg","Download Complete At First Entering"); }catch(Exception
		 * ex){} } }).start();
		 */


       /* SharedPreferences prefs2 = ACRA.getACRASharedPreferences();
        prefs2.edit().putString(ACRA.PREF_USER_EMAIL_ADDRESS, tmpuser_email)
                .commit();
        TelephonyManager manager = (TelephonyManager) getSystemService(getApplicationContext().TELEPHONY_SERVICE);


        prefs2.edit()
                .putString(ACRA.PREF_ENABLE_DEVICE_ID, manager.getDeviceId())
                .commit();
        prefs2.edit()
                .putString(ACRA.PREF_LAST_VERSION_NR, manager.getLine1Number())
                .commit();*/
/*SharedPreferences prefsUser = getSharedPreferences("MY_PREFS", 0);
        user_no = prefsUser.getString("user_no", "");
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String TimeNow = formatter.format(new Date());
        String tmpuser_email = prefsUser.getString("user_email", "");
        String loginTime=prefsUser.getString("remind_time", "");
        Log.d("got_it",loginTime);
        Log.d("got_it",TimeNow);
        Date date1 = null;
        Date date2=null;
        try {
            date1 = formatter.parse(TimeNow);
            date2 = formatter.parse(loginTime);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date1.compareTo(date2)!=0)
        {

            SharedPreferences prefs = getSharedPreferences(
                    "MY_PREFS", 0);
            Editor editor = prefs.edit();
            editor.remove("log_in");
            editor.commit();

            Intent intent = new Intent(
                    HomeActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // To
            // clean
            // up
            // all
            // activities

            startActivity(intent);
            finish();
        }*/
        Log.d("msg", "in home oncreate ");

        if(Global.dbObject != null) {
            try {
                data_user_info = Global.dbObject.queryFromTable("LOGIN_INFO", null,
                        "USER_NO=" + user_no);
                Log.d("user_infa","done");
            }catch (Exception exp){
                Log.d("user_info_ex", exp.getMessage());
            }
            // startDialog();
        }


        gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(new MyGridAdapter(this));
/**
 * This is where we change all the activity from grid
 */
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long rowID) {
                Intent intent = null;
                if (position == 0) {

                    intent = new Intent(HomeActivity.this, dcr_master.class);
                    intent.putExtra("IS_OFFLINE", false);
                } else if (position == 1) {
                    intent = new Intent(HomeActivity.this, dcr_master.class);
                    intent.putExtra("IS_OFFLINE", true);
                } else if (position == 2) {
                    // Log.d("msg", "2 postion item clicked");
                    intent = new Intent(HomeActivity.this, dcr_view.class);
                } else if (position == 3) {
                    // Log.d("msg", "3 postion item clicked");
                    intent = new Intent(HomeActivity.this, DerActivity.class);
                    intent.putExtra("Option", "0");
                } else if (position == 4) {
                    // Log.d("msg", "4 postion item clicked");
                    intent = new Intent(HomeActivity.this, DerActivity.class);
                    intent.putExtra("Option", "1");
                } else if (position == 5) {
                    // Log.d("msg", "5 postion item clicked");
                    intent = new Intent(HomeActivity.this, DerActivity.class);
                    intent.putExtra("Option", "2");
                } else if (position == 6) {
                    // Log.d("msg", "6 postion item clicked");
                    intent = new Intent(HomeActivity.this, UserLocation.class);
                } else if (position == 7) {
                    // Log.d("msg", "7 postion item clicked");
                    intent = new Intent(HomeActivity.this, ReportDailyDCR.class);
                } else if (position == 8) {
                    // Log.d("msg", "8 postion item clicked");
                    // Toast.makeText(HomeActivity.this,"Sorry On Processing ",Toast.LENGTH_LONG).show();
                    intent = new Intent(HomeActivity.this, ChangePassword.class);
                } else if (position == 9) {
                    // Log.d("msg", "8 postion item clicked");
                    // Toast.makeText(HomeActivity.this,"Sorry On Processing ",Toast.LENGTH_LONG).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            HomeActivity.this);
                    builder.setTitle("Change User");
                    builder.setMessage("Are You Sure Want To Change User ?");
                    builder.setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0,
                                                    int arg1) {
                                    SharedPreferences prefs = getSharedPreferences(
                                            "MY_PREFS", 0);
                                    Editor editor = prefs.edit();
                                    editor.remove("log_in");
                                    editor.commit();

                                    Intent intent = new Intent(
                                            HomeActivity.this, Login.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // To
                                    // clean
                                    // up
                                    // all
                                    // activities

                                    startActivity(intent);
                                    finish();
                                }
                            });
                    builder.setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                }
                            });
                    builder.create().show();
                } else if (position == 10) {
                    // Log.d("msg", "10 postion item clicked");
                    intent = new Intent(HomeActivity.this, SyncServer.class);
                } else if (position == 11) {
                    // Log.d("msg", "11 postion item clicked");
                    intent = new Intent(HomeActivity.this, feedback.class);
                } else if (position == 12) {
                    // Log.d("msg", "12 postion item clicked");
                    intent = new Intent(HomeActivity.this, message_view.class);
                } else if (position == 13) {
                    // Log.d("msg", "13 postion item clicked");
                    // for finish activity
					/*
					 * AlertDialog.Builder builder=new
					 * AlertDialog.Builder(HomeActivity.this);
					 * builder.setTitle("Log Out Message");
					 * builder.setMessage("Are You Want to Log Out ?");
					 * builder.setPositiveButton("Log Out",new
					 * DialogInterface.OnClickListener() {
					 * 
					 * @Override public void onClick(DialogInterface dialog, int
					 * which) { LogOut(); } });
					 * builder.setNegativeButton("Cancel",new
					 * DialogInterface.OnClickListener() {
					 * 
					 * @Override public void onClick(DialogInterface dialog, int
					 * which) {
					 * 
					 * } }); builder.create().show();
					 */
                    // start upload
                    UploadTask uploader = new UploadTask(
                            getApplicationContext());
                    uploader.tableUploadData();

                    logoutDialog();
                } else if (position == 14) {
//The debug activity of the app
                    showDebug();
                }
                if (intent != null)
                    startActivity(intent);
                else
                    Log.d("msg", "Sorry No Item Is Selected:" + position);
            }

        });
		/*
		 * gridView.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { Intent intent=null;
		 * if(v.getId() == 0){ intent=new
		 * Intent(HomeActivity.this,MainActivity.class);
		 * intent.putExtra("IS_OFFLINE", false); } else if(v.getId() ==1){
		 * intent=new Intent(HomeActivity.this,MainActivity.class);
		 * intent.putExtra("IS_OFFLINE", true); } else if(v.getId()==4){
		 * intent=new Intent(HomeActivity.this,EntryView.class); }
		 * startActivity(intent); } });
		 */

    }

    public void showDebug() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Enter password");

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.debug_password, null);

        builder.setView(dialogView);
        builder.setPositiveButton("Enter",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        final EditText passText = (EditText) dialogView
                .findViewById(R.id.password_debug);
        Location location = Global.currentLocation();

        final AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface arg0) {
                // HomeActivity.this.finish();
                if (dialog.isShowing())
                    dialog.dismiss();
            }
        });

        // dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);
        if (!dialog.isShowing()) {
            dialog.show();
            Button dButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            dButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (passText.getText().toString().equals("123321")) {
                        Intent intent = new Intent(HomeActivity.this,
                                Debug.class);
                        startActivity(intent);
                    }
                    if (dialog.isShowing())
                        dialog.dismiss();

                }
            });
        }
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    public void startDialog() {
        long time_prev = 0;

        if (data_user_info.size() != 0)
            time_prev = Long.parseLong(data_user_info.get(0)[2]);

        if (time_prev != 0) {
            if (!Global.currentDate().matches(Global.getDateValue(time_prev))) {
                // here pop up window for location name

                showDialogLocation();
            } else {
                if (data_user_info.size() != 0) {
                    String[] user = data_user_info.get(0);
                    initialisation(user[1], Long.parseLong(user[2]), user[3],
                            user[4]);
                }

                Log.d("msg", "Time Value:" + String.valueOf(time_prev));
            }
        } else
            showDialogLocation();
    }

    public void showDialogLocation() {
        if (New_version == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Enter Your Location Name");

            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View dialogView = inflater.inflate(R.layout.dialog_location, null);

            builder.setView(dialogView);
            builder.setPositiveButton("Start Work",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            final AutoCompleteTextView locText = (AutoCompleteTextView) dialogView
                    .findViewById(R.id.locationNameDialog);
            Location location = Global.currentLocation();

            Log.d("msg", "Location At Dialog:" + location);
            locText.setThreshold(0);
            if (location != null)
                locText.setAdapter(new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, Global
                        .getNearestLocation(location, 1000)));

            final AlertDialog dialog = builder.create();
            dialog.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface arg0) {
                    // HomeActivity.this.finish();
                    System.exit(0);
                }
            });

            // dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            if (!dialog.isShowing()) {
                dialog.show();
                Button dButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                dButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (locText.getText().toString().matches("")) {
                            showToast("Location Name Empty");
                        } else if (locText.getText().toString().length() < 4) {
                            showToast("Location name too short");
                        } else {
                            String lon = "", lat = "";
                            Location location = Global.currentLocation();
                            if (location != null) {

                                lon = Double.toString(location.getLongitude());
                                lat = Double.toString(location.getLatitude());

                                // Global.insertNewLocation(location,locText.getText().toString());
                                ArrayList<String[]> data_location = Global.dbObject
                                        .queryFromTable("TRN_USER_LOCATION",
                                                null, "LOCATION_NAME='"
                                                        + locText.getText()
                                                        .toString()
                                                        + "'");
                                if (data_location.size() == 0) {
                                    ContentValues values = new ContentValues();
                                    values.put("OFFLINE_LOC_NO",
                                            Global.dbObject.getLastRowID(
                                                    "TRN_USER_LOCATION",
                                                    "OFFLINE_LOC_NO") + 1);
                                    values.put("ACTION_OFFLINE_TIME", Calendar
                                            .getInstance().getTimeInMillis());
                                    values.put("LAT_VAL",
                                            location.getLatitude());
                                    values.put("LON_VAL",
                                            location.getLongitude());
                                    values.put("LOCATION_NAME", Global
                                            .CapitalizeWords(locText.getText()
                                                    .toString()));

                                    SharedPreferences prefs = getSharedPreferences(
                                            "MY_PREFS", 0);
                                    String userNo=prefs.getString("user_no","");
                                    Log.d("u_no",userNo);
                                    values.put("USER_NO", userNo);
                                    Global.dbObject.insertIntoTable(
                                            "TRN_USER_LOCATION", values);
                                }
                            }

                            try {
                                insertLoginInfo(Global.CapitalizeWords(locText
                                        .getText().toString()));

                                initialisation(Global.CapitalizeWords(locText
                                        .getText().toString()), Calendar
                                        .getInstance().getTimeInMillis(), lon, lat);
                                dialog.dismiss();

                                Global.currentSessionStart = Calendar.getInstance()
                                        .getTimeInMillis();
                            }catch (Exception exp){
                                Log.e("exp_global", exp.getMessage());
                            }
                        }
                    }
                });
            }
        } else {
            new NewVersionDownload(HomeActivity.this, New_version).execute();
        }
    }

    public void insertLoginInfo(String name) {

        Log.d("global_data", Global.dbObject.toString());

        Location location = Global.currentLocation();

        ContentValues values = new ContentValues();

        long last_row_id = Global.dbObject.getLastRowID("TRN_LOG_INFO",
                "LOG_INFO_NO");
        values.put("LOG_INFO_NO", last_row_id + 1);
        values.put("LOG_IN_LOCATION_NAME", name);
        if (location != null) {
            values.put("LOG_IN_LAT", location.getLatitude());
            values.put("LOG_IN_LONG", location.getLongitude());
        }
        values.put("LOG_IN_TIME", Calendar.getInstance().getTimeInMillis());

        SharedPreferences prefs = getSharedPreferences("MY_PREFS", 0);
        values.put("USER_NO", prefs.getString("user_no", ""));

        Global.dbObject.insertIntoTable("TRN_LOG_INFO", values);
        Log.d("msg", "inseted into TRN_LOG_INFO");
    }

    public void initialisation(String locName, Long time, String longitude,
                               String latitude) {
        SharedPreferences prefs = getSharedPreferences("location_time", 1);
        Editor editor = prefs.edit();
        editor.putString("start_location", locName);
        editor.putLong("start_time", time);
        editor.putString("start_long", longitude);
        editor.putString("start_lat", latitude);
        editor.commit();

        ContentValues values = new ContentValues();
        values.put("LOCATION_NAME", locName);
        values.put("TIME", time);
        values.put("LONGITUDE", longitude);
        values.put("LATITUDE", latitude);
        values.put("USER_NO", user_no);

        Log.d("data_size:",String.valueOf(data_user_info.size()));
        if (data_user_info.size() == 0) {


            Global.dbObject.insertIntoTable("LOGIN_INFO", values);

        } else
            Global.dbObject.updateIntoTable("LOGIN_INFO", values, "USER_NO="
                    + user_no);

    }

    public void initialSet() {
        SharedPreferences prefs = getSharedPreferences("location_time", 1);
        Editor editor = prefs.edit();
		/*
		 * String prevName=prefs.getString("start_location", "");
		 * 
		 * if(prevName.matches("")) editor.putString("start_location",
		 * "karwan Bazar");
		 * 
		 * long prevTime=prefs.getLong("start_time",0); if(prevTime != 0)
		 * editor.putLong("start_time",
		 * Calendar.getInstance().getTimeInMillis());
		 */
        Location location = Global.currentLocation();

        if (location != null) {
            editor.putString("start_long",
                    Double.toString(location.getLongitude()));
            editor.putString("start_lat",
                    Double.toString(location.getLatitude()));
            Log.d("msg", "Location SET FROM:" + location.toString());
        } else
            Log.d("msg", "No Location SET FROM Because Location Null");

        editor.commit();
    }

    /*
     * @Override protected void onStart() { super.onStart();
     * if(messageCounter!=null){ ArrayList<String[]>
     * data=Global.dbObject.queryFromTable("TRN_MSG",null,"IS_READ=1");
     * if(data.size()==0){ messageCounter.setVisibility(View.GONE); }else{
     * messageCounter.setVisibility(View.VISIBLE);
     * messageCounter.setText(data.size()); } }else
     * Log.d("msg","Message Counter TextView NULL"); }
     */
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

		/*
		 * if(messageCounter!=null){ final ArrayList<String[]>
		 * data=Global.dbObject.queryFromTable("TRN_MSG",null,"IS_READ=0");
		 * Log.d("msg","Unread Message Total:"+data.size()); if(data.size()==0){
		 * messageCounter.setVisibility(View.GONE); }else{ runOnUiThread(new
		 * Runnable() {
		 * 
		 * @Override public void run() {
		 * messageCounter.setVisibility(View.VISIBLE);
		 * messageCounter.setText(data.size()); MyGridAdapter
		 * adapter=(MyGridAdapter)gridView.getAdapter();
		 * adapter.notifyDataSetChanged(); } }); } }else
		 * Log.d("msg","Message Counter TextView NULL");
		 */

        MyGridAdapter adapter = (MyGridAdapter) gridView.getAdapter();
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        // super.onBackPressed();
		/*
		 * AlertDialog.Builder builder=new
		 * AlertDialog.Builder(HomeActivity.this); builder.setTitle("Exit");
		 * builder.setMessage("Are You Sure Want to Exit?");
		 * builder.setNegativeButton("Yes", new
		 * DialogInterface.OnClickListener() {
		 * 
		 * @Override public void onClick(DialogInterface arg0, int arg1) {
		 * LogOut(); } }); builder.setPositiveButton("No", new
		 * DialogInterface.OnClickListener() {
		 * 
		 * @Override public void onClick(DialogInterface arg0, int arg1) { } });
		 * builder.create().show();
		 */
        logoutDialog();
    }

    public void logoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("Logout Warning");

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_logout, null);

        final AutoCompleteTextView logoutLocation = (AutoCompleteTextView) view
                .findViewById(R.id.logoutLocation);
        final TextView logoutComments = (TextView) view
                .findViewById(R.id.logoutComments);
        final Spinner logoutReason = (Spinner) view
                .findViewById(R.id.logoutReason);
        final CheckBox forGetMe = (CheckBox) view.findViewById(R.id.forgetMe);

        if (Global.currentLocation() != null) {
            logoutLocation.setThreshold(0);
            logoutLocation
                    .setAdapter(new ArrayAdapter<String>(this,
                            android.R.layout.simple_list_item_1, Global
                            .getNearestLocation(
                                    Global.currentLocation(), 1000)));
        }

        ArrayList<String[]> reasons = Global.dbObject.queryFromTable(
                "SET_LOGOUT_TYPE", null, null);
        ArrayList<String> reasonList = new ArrayList<String>();
        final Map<String, String> reasonMap = new HashMap<String, String>();

        reasonList.add("--Select Logout Reason--");

        reasonList.add("Complete");
        reasonMap.put("Complete", "1");

        for (String[] str : reasons) {
            reasonList.add(str[1]);
            reasonMap.put(str[1], str[0]);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                HomeActivity.this,
                android.R.layout.simple_spinner_dropdown_item, reasonList);
        logoutReason.setAdapter(adapter);

        builder.setView(view);
        builder.setPositiveButton("Logout",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.show();

        Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String locationName = logoutLocation.getText().toString(), comments = logoutComments
                        .getText().toString();
                int position = logoutReason.getSelectedItemPosition();

                if (position != 0 && !locationName.matches("")
                        && !comments.matches("")) {
                    {
                        Location location = Global.currentLocation();

                        ContentValues values = new ContentValues();
                        values.put("LOGOUT_TYPE_NO",
                                reasonMap.get(logoutReason.getSelectedItem()));
                        values.put("LOG_OUT_LOCATION_NAME", logoutLocation
                                .getText().toString());
                        if (location != null) {
                            values.put("LOG_OUT_LAT", location.getLatitude());
                            values.put("LOG_OUT_LONG", location.getLongitude());
                        }
                        values.put("LOG_OUT_TIME", Calendar.getInstance()
                                .getTimeInMillis());
                        values.put("LOG_OUT_MESSAGE", logoutComments.getText()
                                .toString());

                        long last_row_id = Global.dbObject.getLastRowID(
                                "TRN_LOG_INFO", "LOG_INFO_NO");
                        Global.dbObject.updateIntoTable("TRN_LOG_INFO", values,
                                "LOG_INFO_NO=" + last_row_id);

                        if (forGetMe.isChecked()) {
                            SharedPreferences prefs = getSharedPreferences(
                                    "MY_PREFS", 0);
                            Editor editor = prefs.edit();
                            editor.remove("log_in");
                            editor.commit();
                        }

                        LogOut();
                        dialog.dismiss();
                    }
                } else {
                    Toast toast = Toast
                            .makeText(
                                    getApplicationContext(),
                                    "Select Reason or Location Name or Comments \n Can not be Empty",
                                    Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

    }

    public void LogOut() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

                try {
                    HttpClient client = new DefaultHttpClient();

                    HttpResponse response = null;
                    SharedPreferences prefs = getApplicationContext()
                            .getSharedPreferences("MY_PREFS", 0);

                    HttpGet httpLogout = new HttpGet(Global.logoutLink
                            + prefs.getString("user_email", ""));
                    response = client.execute(httpLogout);
                    if (response == null) {
                        Log.d("msg", "server not respond");
                    } else {
                        Log.d("msg", "data sent");
                    }

                    ComponentName componentName = new ComponentName(
                            getApplicationContext(), LocationReceiver.class);
                    getPackageManager().setComponentEnabledSetting(
                            componentName,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);

                    ComponentName componentNameUpload = new ComponentName(
                            getApplicationContext(), UploadReceiver.class);
                    getPackageManager().setComponentEnabledSetting(
                            componentNameUpload,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);

                } catch (ClientProtocolException cpe) {

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                } catch (Exception ex) {

                }

                // stopService(new Intent(getApplicationContext(),
                // LocationReceiver.class));
                // Global.showToastText(getApplicationContext(),
                // "ended..activity", true);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        // System.exit(0);
                        finish();
                        // Process.killProcess(Process.myPid());

                        // finish();
                    }
                });
            }
        }).start();
    }

    class MyGridAdapter extends BaseAdapter {
        Context context;

        public MyGridAdapter(Context context) {
            this.context = context;
        }

        private String[] home_menus = {"Online DCR", "Offline DCR",
                "View DCR", "Online Expense", "Offline Expense",
                "View Expense", "Add Location", "View Report",
                "Password Change", "Change User", "Server Sync", "Feedback",
                "Message", "Logout", "Debug"};
        private int[] home_menus_images = {R.drawable.dcrentry,
                R.drawable.dcroffline, R.drawable.dcrview,
                R.drawable.deronline, R.drawable.deroffline,
                R.drawable.viewder, R.drawable.locationentry,
                R.drawable.report2, R.drawable.changepassword,
                R.drawable.changeuser, R.drawable.server_sink,
                R.drawable.feedback, R.drawable.msg, R.drawable.logout,
                R.drawable.debug};

        @Override
        public int getCount() {
            return home_menus.length;
        }

        @Override
        public Object getItem(int arg0) {
            return home_menus[arg0];
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.menu_icon, arg2, false);

            ImageView iv = (ImageView) view.findViewById(R.id.menuIcon);
            iv.setImageResource(home_menus_images[arg0]);

            TextView tv = (TextView) view.findViewById(R.id.menuTitle);
            tv.setText(home_menus[arg0]);

            if (arg0 == 12) {
                messageCounter = (TextView) view
                        .findViewById(R.id.MessageCounter);
                Log.d("msg", "Message Counter Initialised");
                try{
                    SharedPreferences prefsUser = getSharedPreferences("MY_PREFS",
                            0);
                    ArrayList<String[]> data = Global.dbObject.queryFromTable(
                            "TRN_MSG", null, "IS_READ=0 AND REC_USER_NO="
                                    + prefsUser.getString("user_no", ""));
                    Log.d("msg", "Unread Message Total:" + data.size());
                    messageCounter.setText(String.valueOf(data.size()));

                    if (data.size() == 0) {
                        messageCounter.setVisibility(View.GONE);
                    } else {
                        messageCounter.setVisibility(View.VISIBLE);
                    }
                }
                catch (Exception e)
                {
                    Log.d("Hommie",e.getMessage());
                }

            } /*
			 * else if (arg0 == 10) { if (Global.toUploadCount < 0) {
			 * messageCounter2 = (TextView)
			 * view.findViewById(R.id.MessageCounter);
			 * Global.initToUploadCount();
			 * messageCounter2.setText(String.valueOf(Global.toUploadCount)); }
			 * if (Global.toUploadCount <= 0) {
			 * messageCounter2.setVisibility(View.GONE); } else {
			 * messageCounter2.setVisibility(View.VISIBLE); } }
			 */ else
                Log.d("msg", "For " + arg0);

            return view;
        }

    }
}
