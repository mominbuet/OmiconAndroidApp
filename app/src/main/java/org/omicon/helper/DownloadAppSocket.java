package org.omicon.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.omicon.initial.logger;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class DownloadAppSocket extends AsyncTask<Void, Void, Boolean> {
    private Context context;
    private String update_link;
    String filename = "";

    public DownloadAppSocket(Context con, String link, String filename) {
        context = con;
        update_link = link;
        this.filename = filename;
    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();
        Log.d("msg", "Window Prompt");
    }

    public Boolean downnAndsave() {
        Boolean isSuccess = false;

        String PATH = Environment.getExternalStorageDirectory() + "/download/";
        File filePath = null;
        boolean isExist = true;
        try {
            filePath = new File(PATH);
            if (!filePath.exists()) {
                isExist = filePath.mkdir();
            }
            if (isExist) {
                URL url = new URL(update_link);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                //c.setDoOutput(true);
                c.connect();
                //new logger(context).appendLog("Response code "+c.getResponseCode()+c.getResponseMessage());
                //new  logger(this.context).appendLog("File size of the new download "+c.getContentLength());
                File outputFile = new File(filePath, filename);
                FileOutputStream fos = new FileOutputStream(outputFile);

                InputStream is = c.getInputStream();

                byte[] buffer = new byte[4096];
                int len1 = 0, total = 0;
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                    total += len1;


                }
                fos.close();
                is.close();

                isSuccess = true;

            } else
                Log.d("msg", "Directory Can not be created");
        } catch (Exception ex) {
            new logger(this.context).appendLog("error occured in downloading app " + ex.getLocalizedMessage());
            Log.e("error download ", "Error Occured in uploading app" + ex.getMessage());
            ex.printStackTrace();
        }
        return isSuccess;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return downnAndsave();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/" + filename)), "application/vnd.android.package-archive");
            context.startActivity(intent);
        }
    }
}
