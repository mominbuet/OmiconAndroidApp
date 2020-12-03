package org.omicon.helper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class getNewAppLink extends AsyncTask<String, Void, String> {
    Context context;

    //String link = "";
//	public getNewAppLink(Context context, String link){
//		this.context = context;
//		this.link = link;
//	}
    @Override
    protected String doInBackground(String... params) {
        String res = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(params[0]);
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
            in.close();
            if (!object.getString("is_success").matches("true")) {
                return object.getString("down_link");
            }
        } catch (Exception ex) {
            Log.e("error in getting link", ex.getMessage());
        }
        //Log.e("msg", "Down Link:" + line);
        return res;
    }
}
