package org.omicon.helper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.omicon.initial.Global;
import org.omicon.initial.WebSocketService;
import org.omicon.initial.logger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class pure_helper {
    public static void start_socket(Context context) {
        Intent SocketUpdater = new Intent(context, WebSocketService.class);
        context.startService(SocketUpdater);
    }

    public static void send_down_no(String val, Context context, String user) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netState = manager.getActiveNetworkInfo();
        String dta = "";
        if (netState != null && netState.isConnected()) {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(Global.DownloadAck + val
                        + "&user=" + user);
                HttpResponse response = client.execute(httpGet);

                if (response != null) {
                    InputStream in = response.getEntity().getContent();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(in));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        dta += line;
                    }
                    Log.e("msg from server", "message: " + dta);
                    if (dta.contains("false"))
                        new logger(context)
                                .appendLog("Download Log not Not updated on server request.");
                }

            } catch (Exception ex) {
                new logger(context).appendLog("Download Log not Not updated");
            }
        }
    }

    public static boolean get_running(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MY_PREFS", 0);

        Long isProcessing = prefs.getLong("isProcessing", 0);

        // Log.e("download status ", " processing " + isProcessing + " mins "+
        // (System.currentTimeMillis() - last_mod));
        return (isProcessing == 0);
    }

    public static double get_timeDiff(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MY_PREFS", 0);
        Long last_mod = prefs.getLong("UpDownTime", 0);
        return ((System.currentTimeMillis() - last_mod) / (60 * 1000));
    }

    public static void set_running(Context context, int process) {
        SharedPreferences prefs = context.getSharedPreferences("MY_PREFS", 0);
        // if()
        prefs.edit()
                .putLong("UpDownTime",
                        (process == 1) ? System.currentTimeMillis() : 0)
                .apply();
        prefs.edit().putLong("isProcessing", process).apply();

    }
}
