package org.omicon.initial;

import java.util.Date;

import org.json.JSONObject;
import org.omicon.helper.pure_helper;
import org.omicon.helper.upload_file;
import org.omicon.serversync.DownloadTask;
import org.omicon.serversync.UploadTask;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class SendDataReceiver extends IntentService {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public SendDataReceiver() {
        super("UploadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {

            if (pure_helper.get_running(getApplicationContext())) {

                pure_helper.set_running(getApplicationContext(), 1);
                SharedPreferences prefs = getApplicationContext()
                        .getSharedPreferences("MY_PREFS", 0);

                //upload data
                //upload_file.upload_stacktraces(getApplicationContext());

                UploadTask uploader = new UploadTask(getApplicationContext());
                uploader.tableUploadData();

                // Download Data

                DownloadTask dtask = new DownloadTask(getApplicationContext());
                String is_all = prefs.getString("IS_ALL_DOWNLOAD", "1");
                JSONObject result = dtask
                        .DownloadDataFromServer(Global.downloadLink
                                + prefs.getString("user_email", "")
                                + "&IS_ALL=" + is_all, false);
                if (result.getString("Success").matches("true")) {
                    // prefs.edit().putLong(Global.last_download_time,
                    // Calendar.getInstance().getTimeInMillis()).apply();
                    dtask.executeData(result.getString("Content"), false);
                }
                Log.e("msg", "Download Complete Service");
                pure_helper.set_running(getApplicationContext(), 0);
            }
        } catch (Exception ex) {
            pure_helper.set_running(getApplicationContext(), 0);
            new logger(this).appendLog("exception in send data receiver"
                    + ex.getMessage());
        }
    }
}
