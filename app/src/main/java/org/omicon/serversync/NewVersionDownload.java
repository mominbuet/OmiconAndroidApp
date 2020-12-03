package org.omicon.serversync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.omicon.initial.logger;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class NewVersionDownload extends AsyncTask<Void, Void, Boolean> {
    Context context;
    String link = "";
    String filename = "Omicon.apk";
    ProgressDialog dialog = null;

    public NewVersionDownload(Context con, String link) {
        this.context = con;
        this.link = link;

    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();
        dialog = new ProgressDialog(context);
        dialog.setTitle("New Version is Downloading, Please Wait...");
        dialog.setIndeterminate(false);
        dialog.setMax(100);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Boolean isSuccess = false;
        String PATH = Environment.getExternalStorageDirectory() + "/download/";
        File filePath = null;
        boolean isExist = true;
        try {
            filePath = new File(PATH);
            if (!filePath.exists()) {
                isExist = filePath.mkdir();
            }

            URL url = new URL(link);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            // c.
            // c.setDoOutput(true);
            c.connect();
            int length = c.getContentLength();
            // new
            // logger(context).appendLog("Response code "+c.getResponseCode()+c.getResponseMessage());
            // new
            // logger(this.context).appendLog("File size of the new download "+c.getContentLength());
            File outputFile = new File(filePath, filename);
            if (outputFile.exists())
                outputFile.delete();
            FileOutputStream fos = new FileOutputStream(outputFile);

            InputStream is = c.getInputStream();

            byte[] buffer = new byte[4096];
            int len1 = 0, total = 0;
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);
                total += len1;
                if (dialog != null)
                    dialog.setProgress((int) (total * 100 / c
                            .getContentLength()));
            }
            fos.close();
            is.close();
            isSuccess = true;

        } catch (Exception ex) {
            new logger(this.context)
                    .appendLog("error occured in downloading app "
                            + ex.getLocalizedMessage());
            Log.e("error download ",
                    "Error Occured in uploading app" + ex.getMessage());
            ex.printStackTrace();
        }
        return isSuccess;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (dialog != null) {
            if (dialog.isShowing())
                dialog.dismiss();
            dialog.cancel();
        }
        if (result) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(Environment
                            .getExternalStorageDirectory() + "/download/" + filename)),
                    "application/vnd.android.package-archive");
            context.startActivity(intent);
        }
    }

}
