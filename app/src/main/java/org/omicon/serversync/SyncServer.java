package org.omicon.serversync;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.omicon.R;
import org.omicon.helper.DownloadAppSocket;
import org.omicon.helper.pure_helper;
import org.omicon.initial.Global;
import org.omicon.initial.HomeActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SyncServer extends Activity implements OnClickListener {

	/*
     * @Override public boolean onDoubleTap(MotionEvent event) {
	 * Toast.makeText(SyncServer.this, "Double tap", Toast.LENGTH_LONG).show();
	 * return true; }
	 * 
	 * @Override public boolean onDoubleTapEvent(MotionEvent event) {
	 * Toast.makeText(SyncServer.this, "Double tap confirmed",
	 * Toast.LENGTH_LONG).show(); return true; }
	 */

	/*
	 * @Override public boolean onTouchEvent(MotionEvent event) {
	 * 
	 * int action = event.getActionMasked();
	 * 
	 * switch (action) { case (MotionEvent.ACTION_DOWN):
	 * Toast.makeText(SyncServer.this, "Down action", Toast.LENGTH_LONG).show();
	 * return true; case (MotionEvent.ACTION_UP):
	 * Toast.makeText(SyncServer.this, "Up action", Toast.LENGTH_LONG).show();
	 * return true;
	 * 
	 * default: return super.onTouchEvent(event); } }
	 */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.syn_activity);
        Button btnUpdate = (Button) findViewById(R.id.uploadButton);
        Button btnDownload = (Button) findViewById(R.id.downloadButton);
        // if (pure_helper.get_running(getApplicationContext())) {
        // btnUpdate.setEnabled(false);
        // btnDownload.setEnabled(false);
        // btnVersion.setEnabled(false);
        // }
        // else{
        // btnUpdate.setEnabled(true);
        // btnDownload.setEnabled(true);
        // btnVersion.setEnabled(true);
        // }
        // Global.initToUploadCount();
        // ((Button)(findViewById(R.id.uploadButton))).setText("Upload "+
        // ((Global.toUploadCount!=0)?("("+Global.toUploadCount+") item"+((Global.toUploadCount==1)?"":"s")):""));
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netState = manager.getActiveNetworkInfo();
        if (netState != null) {
            btnUpdate.setOnClickListener(this);
            btnDownload.setOnClickListener(this);

        } else {
            Toast.makeText(getApplicationContext(), "No network available",
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == R.id.uploadButton) {

                //Button btnUpdate = (Button) findViewById(R.id.uploadButton);

                if (pure_helper.get_running(getApplicationContext())) {
                    //btnUpdate.setEnabled(false);
                    pure_helper.set_running(getApplicationContext(), 1);

                    new UploadTask(SyncServer.this).execute(
                            Global.uploadLink);
					/*if (!val.isEmpty() && val != null)
						btnUpdate.setEnabled(true);*/
                    pure_helper.set_running(getApplicationContext(), 0);
                } else
                    Toast.makeText(getApplicationContext(),
                            "Upload already running in background",
                            Toast.LENGTH_LONG).show();

				/*
				 * Global.initToUploadCount();
				 * ((Button)(findViewById(R.id.uploadButton
				 * ))).setText("Upload "+ ((Global
				 * .toUploadCount!=0)?("("+Global
				 * .toUploadCount+") item"+((Global
				 * .toUploadCount==1)?"":"s")):""));
				 */
            }
            // else if (v.getId() == R.id.UpdateVersion) {
            // pure_helper.set_running(getApplicationContext(), 1);
            // runOnUiThread(new Runnable(){
            // //new Thread(new Runnable() {
            // @Override
            // public void run() {
            // Button btnUpdate = (Button) findViewById(R.id.UpdateVersion);
            // btnUpdate.setEnabled(false);
            // try {
            // String url_str = Global.link
            // + "WS_SEC_USERS/CheckVersion?APP_VERSION="
            // + Global.APP_VERSION;
            // HttpClient client = new DefaultHttpClient();
            // HttpGet httpGet = new HttpGet(url_str);
            // HttpResponse response = client.execute(httpGet);
            //
            // InputStream in = response.getEntity().getContent();
            //
            // BufferedReader reader = new BufferedReader(
            // new InputStreamReader(in));
            // String line, content = "";
            // long count = 0;
            // while ((line = reader.readLine()) != null) {
            // content += line;
            // count += line.getBytes().length;
            // }
            //
            // Log.d("msg", "Remember Me Server Request:" + content);
            //
            // JSONObject object = new JSONObject(content);
            // String filename = "Omicon.apk";
            // line = object.getString("down_link");
            // if (!object.getString("is_success").matches("true")) {
            // Toast.makeText(getApplicationContext(),
            // "Version is updating...",
            // Toast.LENGTH_SHORT).show();
            // DownloadAppSocket appd = new DownloadAppSocket(
            // getApplicationContext(), line, filename);
            // if (appd.downnAndsave()) {
            // Intent intent = new Intent(Intent.ACTION_VIEW);
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // intent.setDataAndType(Uri.fromFile(new File(
            // Environment
            // .getExternalStorageDirectory()
            // + "/download/" + filename)),
            // "application/vnd.android.package-archive");
            // getApplicationContext().startActivity(intent);
            // }
            // } else
            // Toast.makeText(getApplicationContext(),
            // "Version is already up to date",
            // Toast.LENGTH_SHORT).show();
            //
            // } catch (Exception ex) {
            // // Log.e("exception in json parse", ex.getMessage());
            // }
            // btnUpdate.setEnabled(true);
            // pure_helper.set_running(getApplicationContext(), 0);
            // }
            // });
            // }
            else if (v.getId() == R.id.downloadButton) {
                if (pure_helper.get_running(getApplicationContext())) {
					/*Button btnUpdate = (Button) findViewById(R.id.downloadButton);
					btnUpdate.setEnabled(false);*/
                    pure_helper.set_running(getApplicationContext(), 1);
                    SharedPreferences prefs = getSharedPreferences("MY_PREFS",
                            0);

                    String is_all = prefs.getString("IS_ALL_DOWNLOAD", "1");

                    new DownloadTask(SyncServer.this)
                            .execute(
                                    Global.downloadLink
                                            + prefs.getString("user_email", "")
                                            + "&IS_ALL=" + is_all);
                    //if (result.getString("Success")!=null || result.getString("Success")!="" )
                    //btnUpdate.setEnabled(true);
                    pure_helper.set_running(getApplicationContext(), 0);
                } else
                    // if(Toast.)
                    Toast.makeText(getApplicationContext(),
                            "Download Already running in background",
                            Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

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
            Intent intent = new Intent(SyncServer.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
