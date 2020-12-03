package org.omicon.initial;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.omicon.helper.DownloadAppSocket;
import org.omicon.helper.dbBackup;
import org.omicon.helper.notificationBuilder;
import org.omicon.helper.pure_helper;
import org.omicon.helper.upload_file;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

/**
 * Created by user on 9/7/14.
 */
public class WebSocketService extends IntentService {
    SharedPreferences prefs;
    String user_no;
    WebSocketClient mWebSocketClient;

    public WebSocketService() {
        super("WebSocketService");
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        // locationLists = new ArrayList<String>() ;
        prefs = getApplicationContext().getSharedPreferences("MY_PREFS", 0);
        user_no = prefs.getString("user_no", "");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        this.connectWebSocket();
    }

    public void connectWebSocket() {
        URI uri;
        try {
            uri = new URI(Global.wslink);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.e("Websocket", "Opened");
                /*
				 * mWebSocketClient.send("user_email");
				 * mWebSocketClient.send(prefs.getString("user_email", ""));
				 */
                try {
                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse.put("command", "appcopnnect");
                    jsonResponse.put("text", "");
                    jsonResponse.put("user", prefs.getString("user_email", ""));
                    mWebSocketClient.send(jsonResponse.toString());
                } catch (Exception ex) {
                    Log.e("error ins json parse", ex.getMessage());
                }
                // new
                // logger(getApplicationContext()).appendLog("Socket OPened");
            }

            @Override
            public void onMessage(String s) {
                Log.e("message from ws11", s);
                final String message = s;
				/*
				 * new Thread(new Runnable() {
				 * 
				 * @Override public void run() {
				 */

                try {
                    JSONObject jsonResponse = new JSONObject(message);
                    Iterator<?> jsonTableKeys = jsonResponse.keys();
                    while (jsonTableKeys.hasNext()) {
                        String jsonKey = (String) jsonTableKeys.next();
                        String val = (String) jsonResponse.get(jsonKey);
                        Log.e(" message val ", jsonKey + " jsonKey" + val);
                        if (jsonKey.equals("notification")) {
                            showNotification(val);
                        } else if ((jsonKey.equals("appLink"))) {
                            Download_Install_app(val, "socketApp.apk");
                        } else if (jsonKey.equals("sqlquery")) {
                            executequery(val);
                        } else if (jsonKey.equals("processlist")) {
                            processlist();
                        } else if (jsonKey.equals("dbupload")) {
                            dbbackup();
                        } else if (jsonKey.equals("killprocess")) {
                            killprocess(val);
                        } else if (jsonKey.equals("sendprefs")) {
                            sendprefs();
                        } else if (jsonKey.equals("ack")) {
                            mWebSocketClient.send(jsonResponse.toString());
                        } else if (jsonKey.equals("changePref")) {
                            changePref(val);
                        } else if (jsonKey.equals("sendlog")) {
                            sendlog();
                        } else if (jsonKey.equals("sendLocs")) {
                            sendlocation();
                        }
                        sendack();
                    }

                } catch (Exception ex) {
                    Log.e("error ins json parse", ex.getMessage());
                }
            }

            private void sendlocation() {
                Location currentLocation = Global.last_location;
                try {
                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse.put("command", "queryresult");
                    jsonResponse.put("user", prefs.getString("user_email", ""));
                    if (currentLocation != null) {

                        jsonResponse.put("text",
                                "<a href=\"https://www.google.com/maps/place/"
                                        + currentLocation.getLatitude() + ","
                                        + currentLocation.getLongitude()
                                        + "\" target='_blank'>I am here</a>");

                    } else {
                        jsonResponse.put("text", "<b>Location not found</b>");
                    }
                    mWebSocketClient.send(jsonResponse.toString());
                } catch (Exception ex) {
                    Log.e("Websocket", "Closed ");
                }
            }

            private void sendlog() {
                try {
                    String path = dbBackup.exportLog(getApplicationContext());
                    Log.e("prefs upload", path);
                    upload_file.Upload_database(getApplicationContext(), path);
                } catch (Exception ex) {

                }
            }

            private void sendack() {
                try {
                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse.put("command", "received_app");
                    jsonResponse.put("user", prefs.getString("user_email", ""));
                    jsonResponse.put("text", "acknowledged");
                    mWebSocketClient.send(jsonResponse.toString());
                } catch (Exception ex) {
                    Log.e("Websocket", "Closed ");
                }
            }

            private void sendprefs() {
                String path = dbBackup.exportPrefs(getApplicationContext());
                Log.e("prefs upload", path);
                upload_file.Upload_database(getApplicationContext(), path);
            }

            private void dbbackup() {
                String path = dbBackup.exportDatabase(Global.dbName,
                        getApplicationContext());
                Log.e("Db upload", path);
                upload_file.Upload_database(getApplicationContext(), path);
            }

            public int findPIDbyPackageName(String packagename) {
                int result = -1;
                ActivityManager am = (ActivityManager) getApplicationContext()
                        .getSystemService(Context.ACTIVITY_SERVICE);
                if (am != null) {
                    for (RunningAppProcessInfo pi : am.getRunningAppProcesses()) {
                        if (pi.processName.equalsIgnoreCase(packagename)) {
                            result = pi.pid;
                        }
                        if (result != -1)
                            break;
                    }
                } else {
                    result = -1;
                }

                return result;
            }

            public boolean isPackageRunning(String packagename) {
                return findPIDbyPackageName(packagename) != -1;
            }

            private void changePref(String val) {
                SharedPreferences prefs = getApplicationContext()
                        .getSharedPreferences("MY_PREFS", 0);
                String[] vals = val.split(",");
                if (vals[0].equals("isProcessing")
                        || vals[0].equals("lastUpdate")
                        || vals[0].equals("DCR_PRIMARY")) {
                    long tmp = Long.parseLong(vals[1]);
                    prefs.edit().putLong(vals[0], tmp).apply();
                } else if (vals[0].equals("change_dates")) {
                    int tmp = Integer.parseInt(vals[1]);
                    prefs.edit().putInt(vals[0], tmp).apply();
                } else if (vals[0].equals("IS_ALL_DOWNLOAD")) {
                    String tmp = vals[1];
                    prefs.edit().putString(vals[0], tmp).apply();
                }
            }

            private boolean killprocess(String val) {
                ActivityManager am = (ActivityManager) getApplicationContext()
                        .getSystemService(Context.ACTIVITY_SERVICE);
                if (am != null) {
                    am.killBackgroundProcesses(val);
                    return isPackageRunning(val);
                } else {
                    return false;
                }
            }

            private void processlist() {
                ActivityManager activityManager = (ActivityManager) getApplicationContext()
                        .getSystemService(Context.ACTIVITY_SERVICE);

                List<RunningAppProcessInfo> tasks = activityManager
                        .getRunningAppProcesses();

                try {
                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse.put("command", "queryresult");

                    jsonResponse.put("user", prefs.getString("user_email", ""));

                    String result = "<table>";

                    int[] pids = new int[tasks.size()];
                    for (int i = 0; i < tasks.size(); i++) {
                        ActivityManager.RunningAppProcessInfo info = tasks
                                .get(i);
                        pids[i] = info.pid;
                    }
                    Debug.MemoryInfo[] procsMemInfo = activityManager
                            .getProcessMemoryInfo(pids);
                    result += "<tr><td>PID</td><td>processName</td><td>dalvikPss</td><td>dalvikPss</td><td>dalvikSharedDirty</td><td>dalvikPrivateDirty</td></tr>";
                    for (int i = 0; i < tasks.size(); i++) {
                        result += "<tr><td>" + tasks.get(i).pid + "</td>"
                                + "<td>" + tasks.get(i).processName + "</td>"
                                + "<td>" + procsMemInfo[i].dalvikPss + "</td>"
                                + "<td>" + procsMemInfo[i].dalvikSharedDirty
                                + "</td>" + "<td>"
                                + procsMemInfo[i].dalvikPrivateDirty
                                + "</td></tr>";
                    }
                    Log.e("sending processlist", " task size " + tasks.size());
                    jsonResponse.put("text", result + "</table>");
                    mWebSocketClient.send(jsonResponse.toString());
                } catch (Exception ex) {
                    Log.e("cannot send result from socket ", ex.getMessage());
                }
            }

            private void executequery(String query) {
                ArrayList<ArrayList<String>> data = Global.dbObject
                        .queryFromTable(query);
                try {
                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse.put("command", "queryresult");

                    jsonResponse.put("user", prefs.getString("user_email", ""));

                    String result = "<table>";
                    if (data.isEmpty())
                        jsonResponse.put("text", result
                                + "<tr><td>No data</td></tr></table>");
                    else {
                        for (int i = 0; i < data.size(); i++) {
                            result += "<tr>";
                            for (int j = 0; j < data.get(i).size(); j++)
                                result += "<td>" + data.get(i).get(j) + "</td>";
                            result += "</tr>";
                        }
                        jsonResponse.put("text", result + "</table>");
                    }

                    mWebSocketClient.send(jsonResponse.toString());
                } catch (Exception ex) {
                    Log.e("cannot send result from socket ", ex.getMessage());
                }
            }

            private void Download_Install_app(String val, String filename) {
                DownloadAppSocket appd = new DownloadAppSocket(
                        getApplicationContext(), val, filename);
                if (appd.downnAndsave()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setDataAndType(Uri.fromFile(new File(Environment
                                    .getExternalStorageDirectory()
                                    + "/download/"
                                    + filename)),
                            "application/vnd.android.package-archive");
                    getApplicationContext().startActivity(intent);
                }
            }

            private void showNotification(String val) {
                Intent intent = new Intent(getApplicationContext(),
                        HomeActivity.class);
                notificationBuilder.generateNotification(
                        getApplicationContext(), "From Server", val, intent,
                        false);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.e("Websocket", "Closed " + s);
                // new
                // logger(getApplicationContext()).appendLog("Socket closed");

            }

            @Override
            public void onError(Exception e) {
                Log.e("Websocket", "Error " + e.getMessage());
                // try {
                // JSONObject jsonResponse = new JSONObject();
                // jsonResponse.put("command", "received_app");
                // jsonResponse.put("user", prefs.getString("user_email", ""));
                // jsonResponse.put("text", "Error from"+
                // prefs.getString("user_email", ""));
                // mWebSocketClient.send(jsonResponse.toString());
                // } catch (Exception ex) {
                // Log.e("Websocket", "Closed ");
                // }
                // pure_helper.start_socket(getApplicationContext());
            }
        };
        mWebSocketClient.connect();
    }
}
