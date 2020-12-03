package org.omicon.initial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.omicon.R;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.Activity;
import android.content.SharedPreferences;

public class ChangePassword extends Activity {

    private Button btnChangePass;
    private EditText etCurpass, etNewpass, etNewpassagin;
    private String strOldPass, strNewPass, strNewPassagin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etCurpass = (EditText) findViewById(R.id.etCurrentPassword);
        etNewpass = (EditText) findViewById(R.id.etNewPassword);
        etNewpassagin = (EditText) findViewById(R.id.etNewPasswordAgain);


        btnChangePass = (Button) findViewById(R.id.btnChangePassword);
        btnChangePass.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                strOldPass = etCurpass.getText().toString();
                strNewPass = etNewpass.getText().toString();
                strNewPassagin = etNewpassagin.getText().toString();

                String strMsgToShow = "";

                if (strOldPass.isEmpty() || strNewPass.isEmpty() || strNewPassagin.isEmpty()) {
                    strMsgToShow = "Fields Cant be Empty!";
                    Toast tsWarning = Toast.makeText(getApplicationContext(), strMsgToShow, Toast.LENGTH_SHORT);
                    tsWarning.setGravity(Gravity.CENTER, 0, 0);
                    tsWarning.show();

                } else if (!strNewPass.equals(strNewPassagin)) {
                    strMsgToShow = "New Password Doesn't match!";
                    Toast tsWarning = Toast.makeText(getApplicationContext(), strMsgToShow, Toast.LENGTH_SHORT);
                    tsWarning.setGravity(Gravity.CENTER, 0, 0);
                    tsWarning.show();
                } else {
                    changePassword();
                }

            }
        });
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
            Intent intent = new Intent(ChangePassword.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void changePassword() {

        new Thread(new Runnable() {

            @Override
            public void run() {

                String dta = "";
                JSONObject user_info = new JSONObject();

                Log.d("thread", "started");
                try {

                    SharedPreferences prefs = getApplicationContext().getSharedPreferences("MY_PREFS", 0);

                    user_info.put("USER_NAME", prefs.getString("user_email", ""));
                    user_info.put("OLD_PASSWORD", strOldPass);
                    user_info.put("NEW_PASSWORD", strNewPass);
                } catch (JSONException ex) {
                    Log.d("msg", "error in creating json object");

                }

                HttpClient client = new DefaultHttpClient();

                HttpResponse response = null;
                try {
                    HttpPost post = new HttpPost(Global.chgPasswdlink);
                    Log.d("json", user_info.toString());
                    StringEntity en = new StringEntity(user_info.toString());

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

                        JSONObject jsonResponse = new JSONObject(dta);
                        final String responseMsg = jsonResponse.getString("msg");

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                Toast tsWarning = Toast.makeText(getApplicationContext(), responseMsg, Toast.LENGTH_SHORT);
                                tsWarning.setGravity(Gravity.CENTER, 0, 0);
                                tsWarning.show();
                            }
                        });
                    }

                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block

                    Log.d("msg", "error to encode json array");
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    //e.printStackTrace();
                    Log.d("msg", "error to send data due protocol exception ,Type:" + e.getMessage());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    //e.printStackTrace();
                    Log.d("msg", "error to send data due to IO exception ,Type:" + e.getMessage());
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    //e.printStackTrace();
                } catch (Exception ex) {
                }
            }
        }).start();

    }


}
