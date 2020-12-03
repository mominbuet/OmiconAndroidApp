package org.omicon.initial;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.omicon.R;
import org.omicon.helper.getNewAppLink;
import org.omicon.helper.location_service;
import org.omicon.helper.pure_helper;
import org.omicon.serversync.NewVersionDownload;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

import static java.lang.Math.abs;

public class Login extends Activity {

    AutoCompleteTextView username;
    EditText password;
    CheckBox remind;
    Context context;
    Session session;
    ArrayList<User> uinfos;
    AlertDialog settingDialog;
    boolean prevSettingDialog = false;
    AlertDialog dialog;
    boolean langCheck = false, gpsCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_login);

        Global.initialConfig(this);
        // new logger(this).appendLog(" App Started");
        ((TextView) findViewById(R.id.versionNo))
                .setText("DCR Automation System  \n version "
                        + Global.APP_VERSION+".2" );

        showDialog();

        session = new Session(getApplicationContext());
        context = this;
        username = (AutoCompleteTextView) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        remind = (CheckBox) findViewById(R.id.remind);

        uinfos = session.getAllRemindUsers();
        ArrayList<String> unames = new ArrayList<String>();
        for (int i = 0; i < uinfos.size(); i++)
            unames.add(uinfos.get(i).username);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1, unames);
        username.setAdapter(adapter);
        username.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                String unm = username.getText().toString().trim();
                for (int i = 0; i < uinfos.size(); i++)
                    if (uinfos.get(i).username.compareTo(unm) == 0)
                        password.setText(uinfos.get(i).password);
            }
        });

        // Log.d("msg","IS LOGGED IN_MAIN:"+Boolean.toString(session.loggedStatus()));
        // Log.d("msg",session.prefs.getString(session.KEY_EMAIL, "default"));
        if (session.loggedStatus()) {
            /*
			 * Here Check Previous Session for Whether it is Active or not
			 * and start the service for upload
			 */

            // new LoginTask().execute();

            // ************ Start Service for Location Send ************//
			/*
			 * Log.d("msg", "starting service"); final Intent locationIntent =
			 * new Intent(getApplicationContext(), LocationReceiver.class);
			 * PendingIntent pendingIntent =
			 * PendingIntent.getBroadcast(getApplicationContext(), 0,
			 * locationIntent, PendingIntent.FLAG_CANCEL_CURRENT); AlarmManager
			 * alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
			 * alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
			 * Calendar.getInstance().getTimeInMillis(),10*60*1000,
			 * pendingIntent); Log.d("msg", "alarm manager called");
			 *
			 * ComponentName componentName = new
			 * ComponentName(getApplicationContext(), LocationReceiver.class);
			 * getPackageManager().setComponentEnabledSetting(componentName,
			 * PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
			 * PackageManager.DONT_KILL_APP);
			 */
            // *********************************************************//
            if (!Global.isMyServiceRunning(UploadReceiver.class, this)) {
                SharedPreferences prefs = getApplicationContext()
                        .getSharedPreferences("MY_PREFS", 0);
                pure_helper.set_running(context, 0);

            }
            //check language for bangla
            if (!langCheck) {
                try {
                    // ServiceStart(LocationReceiver.class,10*60*1000);
                    Intent locationUpdater = new Intent(context,
                            location_service.class);
                    context.startService(locationUpdater);
                    ServiceStart(UploadReceiver.class, 30 * 60 * 1000);

                    if (Global.ApplciationControllSocketServiceEnable == 1)
                        ServiceStart(SocketReceiver.class, 60 * 60 * 1000);
                } catch (Exception ex) {
                    Log.e("error in service start ", ex.getMessage());
                    new logger(context).appendLog("error in service start "
                            + ex.getMessage());
                }
                // Send Version Of App to Server
                // rememberMe();
                long mills = 0;
                try {
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df1 = new SimpleDateFormat("hh:mm:ss aa");
                    Date Date1 = df1.parse("11:59:59 pm");
                    String formattedDate1 = df1.format(c.getTime());
                    Date Date2=df1.parse(formattedDate1);

                    mills = abs(Date1.getTime() - Date2.getTime());

                }
                catch(Exception e){ e.printStackTrace();
                    Log.d("Error:","did it");
                }

                Intent intent = new Intent(context, HomeActivity.class);

                String res = remembermeNew();
                //if (!res.matches("")) {
                intent.putExtra("New_version", res);
                Log.d("Millls",String.valueOf(mills));
                intent.putExtra("Time_Out",String.valueOf(mills));
                //}

                startActivity(intent);

                this.finish();

            }
        }

        Button b = (Button) findViewById(R.id.submit);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                String user = username.getText().toString(), pass = password
                        .getText().toString();
                if (user.matches("") && pass.matches(""))
                    showToast("Both UserName & Password fileds are Empty");
                else if (user.matches(""))
                    showToast("UserName field Empty");
                else if (pass.matches(""))
                    showToast("Password field Empty");
                else {
					/*
					 * if(remind.isChecked()){
					 *
					 * Log.d("msg", "starting service"); final Intent
					 * locationIntent = new Intent(getApplicationContext(),
					 * LocationReceiver.class); PendingIntent pendingIntent =
					 * PendingIntent.getBroadcast(getApplicationContext(), 0,
					 * locationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
					 * AlarmManager alarmManager =
					 * (AlarmManager)getSystemService(ALARM_SERVICE);
					 * alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
					 * Calendar.getInstance().getTimeInMillis(),5*60*1000,
					 * pendingIntent); Log.d("msg", "alarm manager called");
					 *
					 *
					 * Intent intent=new Intent(context, HomeActivity.class);
					 * startActivity(intent); finish(); } else {
					 */
                    // Here Async Task
                    try {
//						if (!Global.isMyServiceRunning(UploadReceiver.class, Log)) {
//							SharedPreferences prefs = getApplicationContext()
//									.getSharedPreferences("MY_PREFS", 0);
//							pure_helper.set_running(context, 0);
//
//						}
						/*
						 * Intent notificationIntent=new Intent(context,
						 * Login.class);
						 * notificationBuilder.generateNotification
						 * (context," Omicon ", "Started the app",
						 * notificationIntent);
						 */
                        // new
                        // notificationBuilder(context).createNotification("Omicon Started",
                        // "hello there", this.getClass());

                        new LoginTask().execute();
                        // remembermeNew();

                    } catch (Exception ex) {
                        showToast("Network Error Occured");
                    }
                    // }

					/*
					 * if(user.matches("test") && pass.matches("test")) {
					 * if(!session.loggedStatus()){
					 * session.login(username.getText
					 * ().toString(),password.getText().toString(),"12345678");
					 * if(remind.isChecked())
					 * session.remindUser(username.getText
					 * ().toString(),password.getText().toString()); }
					 * 
					 * Intent intent=new Intent(context, HomeActivity.class);
					 * startActivity(intent); finish(); } else
					 * Toast.makeText(context
					 * ,"Sorry Username or Password Not Correct"
					 * ,Toast.LENGTH_LONG).show();
					 */
                }
            }
        });

    }

    /*
     * @Override protected void onResume() { // TODO Auto-generated method stub
     * super.onResume(); showDialog(); }
     */
    private void serv_start(Context context) {
        Intent locationUpdater = new Intent(context, location_service.class);
        context.startService(locationUpdater);
        ServiceStart(UploadReceiver.class, 60 * 60 * 1000);
    }

    public void ServiceStart(Class<?> cls, long interval) {
        final Intent updateIntent = new Intent(getApplicationContext(), cls);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), 0, updateIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar
                .getInstance().getTimeInMillis(), interval, pendingIntent);

        ComponentName componentName = new ComponentName(
                getApplicationContext(), cls);
        getPackageManager().setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void showToast(String message) {
        Toast toast = Toast.makeText(Login.this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
        builder.setTitle("Activate");
        String message = "";
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netState = manager.getActiveNetworkInfo();
        if (netState != null) {
            if (!netState.isConnected())
                message = "No Internet Available";
        } else
            message = "No Internet Available";
        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            message += "\n" + "GPS Unavailable";
            gpsCheck = true;
        }
        if (!Locale.getDefault().getLanguage().equals("en")) {
            message += "\n" + "Please change your language to English";
            langCheck = true;
            gpsCheck = false;
        }
        builder.setMessage(message);
        builder.setNegativeButton("Settings",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent myIntent;
                        if (langCheck)
                            myIntent = new Intent(
                                    Settings.ACTION_LOCALE_SETTINGS);
                        else if (gpsCheck)
                            myIntent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        else
                            myIntent = new Intent(
                                    android.provider.Settings.ACTION_SETTINGS);
                        startActivity(myIntent);
                        finish();
                    }
                });
        builder.setPositiveButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        System.exit(0);
                    }
                });
        if (!message.matches("")) {
            dialog = builder.create();
            dialog.setCancelable(false);
            if (!dialog.isShowing())
                dialog.show();
        }
    }

    public String getNumber() {
        TelephonyManager manager = (TelephonyManager) getSystemService(context.TELEPHONY_SERVICE);
		/*
		 * String simNumber=manager.getLine1Number();
		 * if(simNumber.compareTo("")==0)
		 * simNumber=manager.getSimSerialNumber(); return simNumber;
		 */
        return manager.getDeviceId();
    }

    private class LoginTask extends AsyncTask<Void, Void, String> {
        private String login_url = Global.loginLink;
        private ProgressDialog dialog = null;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setTitle("Please Wait...");
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String dta = "";
            String name = (session.loggedStatus()) ? session.prefs.getString(
                    session.KEY_EMAIL, null) : username.getText().toString();
            String pass = (session.loggedStatus()) ? session.prefs.getString(
                    name, null) : password.getText().toString();

            JSONObject user_info = new JSONObject();
            try {
                user_info.put("USER_NAME", name);
                user_info.put("USER_PWD", pass);
                user_info.put("USER_MOBILE", getNumber());
                user_info.put("APP_VERSION", Global.APP_VERSION);
                // Global.appendLog("user_info "+user_info.toString());
                // new logger(context).appendLog("user_info " +
                // user_info.toString());
                Log.d("user_info", user_info.toString());
            } catch (Exception ex) {
                Log.d("sending userinfo", "error in creating json object");
                return dta;
            }

            HttpClient client = new DefaultHttpClient();
            // HttpConnectionParams.setConnectionTimeout(client.getParams(),
            // 10000); //Timeout Limit
            HttpResponse response = null;

            // Send Data Using Post Method
            try {
                HttpPost post = new HttpPost(login_url);
                StringEntity en = new StringEntity(user_info.toString());
                en.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
                        "application/json"));
                post.setEntity(en);

                response = client.execute(post);
                if (response == null) {
                    Log.e("error in response", "server not respond");
                } else {
                    Log.d("msg", "send data ");
                    InputStream input = response.getEntity().getContent();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(input));
                    // new logger(context).appendLog(reader.readLine());
                    String line;
                    while ((line = reader.readLine()) != null)
                        dta = dta + line;
                    Log.i("info about sent data", "data:" + dta);
                    // new logger(context).appendLog("dta  "+reader.readLine());
                }

            } catch (UnsupportedEncodingException e) {
                // e.printStackTrace();
                Log.d("msg", "error to encode json array");
            } catch (ClientProtocolException e) {
                // e.printStackTrace();
                Log.d("msg", "error to send data due protocol exception");
            } catch (IOException e) {
                // e.printStackTrace();
                Log.d("msg", "error to send data due to IO exception ");
            } catch (Exception ex) {
                Log.e("error exception", ex.getMessage());
            }

            return dta;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            if (result != null && !result.matches("")) {
                try {
                    JSONObject user_session = new JSONObject(result);
                    if (user_session.getString("is_success") != null
                            && user_session.getString("is_success").matches(
                            "true")) {

						/*
						 * if(session.loggedStatus() &&
						 * session.prefs.getString(session.KEY_SESSIONID,
						 * "").compareTo
						 * (user_session.getString("SESSION_ID"))!=0) return;
						 */

                        if (!session.loggedStatus()) {
                            String temp_uf_name="";
                            if(!user_session.getString("uf_name").isEmpty())
                            {
                                temp_uf_name=user_session.getString("uf_name");
                            }

                            session.login(username.getText().toString(),
                                    password.getText().toString(),
                                    user_session.getString("SESSION_ID"),
                                    user_session.getString("USER_NO"),
                                    temp_uf_name
                                    );
                            if (remind.isChecked())
                                session.remindUser(username.getText()
                                        .toString(), password.getText()
                                        .toString());
                        }

                        // ************ Start Service for Location Send
                        // ************//
						/*
						 * Log.d("msg", "starting service"); Intent
						 * locationIntent = new Intent(getApplicationContext(),
						 * LocationReceiver.class); PendingIntent pendingIntent
						 * = PendingIntent.getBroadcast(getApplicationContext(),
						 * 0, locationIntent,
						 * PendingIntent.FLAG_CANCEL_CURRENT); AlarmManager
						 * alarmManager =
						 * (AlarmManager)getSystemService(ALARM_SERVICE);
						 * alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
						 * Calendar.getInstance().getTimeInMillis(),10*60*1000,
						 * pendingIntent); Log.d("msg", "alarm manager called");
						 * 
						 * ComponentName componentName = new
						 * ComponentName(getApplicationContext(),
						 * LocationReceiver.class);
						 * getPackageManager().setComponentEnabledSetting
						 * (componentName,
						 * PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
						 * PackageManager.DONT_KILL_APP);
						 */
                        // *********************************************************//
                        // Global.appendLog("Starting app......");
                        new logger(context).appendLog("Starting app......");

                        try {
                            if (Global.ApplciationControllSocketServiceEnable == 1)
                                ServiceStart(SocketReceiver.class,
                                        75 * 60 * 1000);
                            // ServiceStart(LocationReceiver.class, 10 * 60 *
                            // 1000);
                            Intent locationUpdater = new Intent(context,
                                    location_service.class);
                            context.startService(locationUpdater);

                            ServiceStart(UploadReceiver.class, 60 * 60 * 1000);

                            // pk val update
                            long last_pk_val = user_session
                                    .getLong("LAST_PK_VAL");
                            if ((last_pk_val + 1) > Global.getPrimaryKeyID())
                                Global.setPrimaryKeyID(last_pk_val + 1);

                        } catch (Exception ex) {
                            new logger(context)
                                    .appendLog("exception in service  "
                                            + ex.getMessage());
                        }
                        if (dialog != null)
                            dialog.cancel();
                        Intent intent = new Intent(context, HomeActivity.class);
                        long mills = 0;
                        try {
                            Calendar c = Calendar.getInstance();
                            SimpleDateFormat df1 = new SimpleDateFormat("hh:mm:ss aa");
                            Date Date1 = df1.parse("11:59:59 pm");
                            String formattedDate1 = df1.format(c.getTime());
                            Date Date2=df1.parse(formattedDate1);

                            mills = abs(Date1.getTime() - Date2.getTime());

                        }
                        catch(Exception e){ e.printStackTrace();
                            Log.d("Error:","did it");
                        }
                        intent.putExtra("Time_Out",String.valueOf(mills));
                        startActivity(intent);

                        finish();
                    } else {
                        String login_msg_type = user_session
                                .getString("login_msg_type");

                        if (login_msg_type.matches("1"))
                            showToast(user_session.getString("msg"));
                        else if (login_msg_type.matches("2")) {
                            String down_link = user_session
                                    .getString("down_link");
                            if (down_link != null && !down_link.matches("")) {
                                try {
                                    Log.d("msg", "DOWNLINK");
                                    // Session Remove
                                    session.removeSession();

                                    new DownloadApp(Login.this, down_link)
                                            .execute();
                                } catch (Exception e) {
                                    Log.d("msg", "Error in Updating app");
                                }
                            } else
                                Log.d("msg", "Downlink Not Found");
                        } else
                            showToast(user_session.getString("msg"));
                    }

                } catch (Exception ex) {
                    Log.d("msg",
                            "ERROR IN PARSE GET JSON OBJECT: "
                                    + ex.getMessage());
                    showToast("Error Occured");
                }

            } else
                showToast("Error In Connection To Server");
        }

    }

    public String remembermeNew() {
        String url_str = Global.link + "WS_SEC_USERS/CheckVersion?APP_VERSION="
                + Global.APP_VERSION;
        try {
            return new getNewAppLink().execute(url_str).get();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void rememberMe() {
        // activity.runOnUiThread(new Runnable() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url_str = Global.link
                            + "WS_SEC_USERS/CheckVersion?APP_VERSION="
                            + Global.APP_VERSION;

                    HttpClient client = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(url_str);
                    HttpResponse response = client.execute(httpGet);

                    InputStream in = response.getEntity().getContent();

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(in));
                    String line, content = "";
                    long count = 0;
                    while ((line = reader.readLine()) != null) {
                        content += line;
                        count += line.getBytes().length;
                    }
                    Log.d("msg", "Remember Me Server Request:" + content);
                    JSONObject object = new JSONObject(content);
                    if (!object.getString("is_success").matches("true")) {
                        line = object.getString("down_link");
                        Log.e("msg", "Down Link:" + line);
                        DownloadApp appd = new DownloadApp(
                                getApplicationContext(), line);
                        if (appd.downnAndsave()) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(new File(
                                            Environment.getExternalStorageDirectory()
                                                    + "/download/" + "Omicon.apk")),
                                    "application/vnd.android.package-archive");
                            context.startActivity(intent);
                        }
                    } else {
                        Log.e("msg", "No Download After Version Mismatch");
                    }
                } catch (Exception ex) {
                    Log.e("msg", "Error at Remember Me Server Request");
                }
            }
        }).start();
    }

}
