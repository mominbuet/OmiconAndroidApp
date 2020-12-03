package org.omicon.serversync;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.omicon.R;
import org.omicon.helper.notificationBuilder;
import org.omicon.helper.pure_helper;
import org.omicon.initial.Global;
import org.omicon.initial.logger;
import org.omicon.view.message_view;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
//import android.content.SharedPreferences;

public class DownloadTask extends AsyncTask<String, Void, JSONObject> {
    private Context context;
    // private ProgressDialog dialog = null;
    public Map<String, String> idTableDictionary = new HashMap<String, String>();
    logger loger;
    SharedPreferences prefs;

    /*View rootView;
    Button btnUpdate ;*/
    public DownloadTask(Context con) {

       // Fabric.with(this, new Crashlytics());
        loger = new logger(context);
        context = con;
        prefs = context.getSharedPreferences("MY_PREFS", 0);

        initiateTableIdColumns();
    }

    @Override
    protected void onProgressUpdate(Void... progress) {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false

        // dialog.setIndeterminate(false);
        // dialog.setMax(100);
        // dialog.setProgress(Integer.valueOf(progress[0].toString()));
    }

    public JSONObject DownloadDataFromServer(String url_str, boolean showdialog) {

        JSONObject json = new JSONObject();
        Log.d("msg", "Start Downloading from " + url_str);
        String exception_message = "", content = "", success = "false";
        InputStream in = null;
        // StringBuilder sb=new StringBuilder();
        try {
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netState = manager.getActiveNetworkInfo();
            if (netState != null && netState.isConnected()) {
                try {
                    // URL url=new URL(url_str);

                    HttpClient client = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(url_str);
                    HttpResponse response = client.execute(httpGet);

                    // in=conn.getInputStream();
                    loger.appendLog("Content-Length before download:"
                            + response.getEntity().getContentLength());
                    Log.d("msg", "Content-Length:"
                            + response.getEntity().getContentLength());
                    in = response.getEntity().getContent();
                    Log.d("in data", in.toString());
                    // InputStreamReader istream = new InputStreamReader(in);
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(in));
                    String line;
                    long count = 0;
                    long total = response.getEntity().getContentLength();
                    while ((line = reader.readLine()) != null) {
                        content += line;
                        count += line.getBytes().length;
                        // if (showdialog)
                        // if (dialog != null)
                        // dialog.setProgress((int) ((count * 100) / total));
                    }
                    /*
					 * int r; byte bb[] = new byte[1000]; String tmp = new
					 * String(); while ((r = in.read(bb)) != -1) { tmp = new
					 * String(bb,"UTF-8"); content +=tmp; count =
					 * content.toCharArray().length; if (dialog != null)
					 * dialog.setProgress((int) ((count * 100) / total)); }
					 */
                    // content = content.substring(0,(int)total-1);
                    // Toast.makeText(context, "Got data of " + ((int) count /
                    // 1000) + " KB", Toast.LENGTH_LONG);
                    loger.appendLog("Content Length After Download:" + count);
//					loger.appendBiggerLog(content);
                    Log.e("msg", "Content Length After Download:" + count);
                    success = "true";
					/*
					 * if(showdialog)
					 * Toast.makeText(context,"Download Successfull",
					 * Toast.LENGTH_LONG).show();
					 */
                } catch (Exception ex) {
                    loger.appendLog("Exception in getting data "
                            + ex.getMessage());
                    exception_message = ex.getMessage();
                    success = "false";
                }
            } else {
                exception_message = "Check your network Connection,It's not Active";
            }
        } catch (Exception ex) {
            loger.appendLog("Exception in getting data " + ex.getMessage());
            success = "false";
            exception_message = ex.getMessage();
        } finally {
            try {
                if (in != null)
                    in.close();
                json.put("Success", success);
                json.put("Content", content);
                json.put("Exception", exception_message);

            } catch (Exception e) {
                loger.appendLog("Exception in finally " + e.getMessage());
                Log.d("msg", "Sorry Exception Occured:" + e.getMessage());
            }
        }
        return json;

    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();
		/*rootView = ((Activity)context).getWindow().getDecorView().findViewById(R.id.sync_id);
		btnUpdate = (Button) rootView.findViewById(R.id.downloadButton);
		btnUpdate.setEnabled(false);*/
        Toast.makeText(context, "Download running in background",
                Toast.LENGTH_SHORT).show();
        // dialog = new ProgressDialog(context);
        // dialog.setTitle("Downloading...");
        // dialog.setIndeterminate(false);
        // dialog.setMax(100);
        // dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // dialog.setCancelable(true);
        // dialog.show();
    }

    @Override
    protected JSONObject doInBackground(String... arg0) {
		/*
		 * long last_download_try = prefs.getLong(Global.last_download_time, 0);
		 * long diff = Calendar.getInstance().getTimeInMillis() -
		 * prefs.getLong(Global.last_download_time, 0); if (((int) diff / (1000
		 * * 60 * 2)) >= 1) {
		 */
        // prefs.edit().putLong(Global.last_download_time,
        // Calendar.getInstance().getTimeInMillis()).apply();

        JSONObject result = DownloadDataFromServer(arg0[0], true);
        try {
            if (result.getString("Success").matches("true")) {
                // prefs.edit().putLong(Global.last_download_time,
                // Calendar.getInstance().getTimeInMillis()).apply();
                executeData(result.getString("Content"), false);
            }
        } catch (Exception ex) {
            Log.e("exception in download ", ex.getMessage());
        }
        // executeData(result, false);
        //btnUpdate.setEnabled(true);
        return result;
		/*
		 * } else { if (dialog.isShowing()) dialog.dismiss();
		 * //Toast.makeText(context, "Already running in background",
		 * Toast.LENGTH_LONG);
		 * 
		 * return new JSONObject(); }
		 */
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        // TODO Auto-generated method stub

        loger.appendLog("Download Complete, bnow on post execute");
        // super.onPostExecute(result);

        Log.d("msg", "Download Complete");
        try {

            Log.d("msg", "Success:" + result.getString("Success"));
            Log.d("msg", "Exception:" + result.getString("Exception"));
            // Log.d("msg","Content:"+result.getString("Content"));
            Log.d("msg", "Complete");
            // if (dialog.isShowing()) {
            // dialog.dismiss();
            // }
            if (result.getString("Success").matches("true")) {
                loger.appendLog("going to database to insert");
                executeData(result.getString("Content"), true);
                loger.appendLog("ended insertion on database");
            }
            if (result.getString("Success").matches("true"))
                Toast.makeText(context, "Download Complete", Toast.LENGTH_LONG)
                        .show();
            else
                Toast.makeText(context,
                        "Download is not complete, please try later",
                        Toast.LENGTH_LONG).show();

        } catch (JSONException e) {
            Log.d("msg", e.getMessage());

            loger.appendLog("exception in download result because fo running");
            e.printStackTrace();
        }

    }

    public void executeData(String content, boolean showDialog) {
        JSONObject jsonResponse;
        JSONArray jsonTableNodes;
        String idCoulmn, msg = "";
        String down_no = "";
        ArrayList<String[]> getRowByidList = null;
        // if (dialog != null) {
        // dialog.setTitle("Inserting data");
        // dialog.setProgress(0);
        // }
        try {
            loger.appendLog("inserting into database");
            jsonResponse = new JSONObject(content);
            Iterator<?> jsonTableKeys = jsonResponse.keys();

            int count = 0;
            while (jsonTableKeys.hasNext()) {
                String jsonKey = (String) jsonTableKeys.next();
                Log.d("jkey_table", jsonKey);


                if (jsonKey.equals("download_info")) {
                    JSONObject tmp = (JSONObject) jsonResponse.getJSONObject(jsonKey);
                    down_no = tmp.getString("DOWN_NO");

                } else {
                    idCoulmn = idTableDictionary.get(jsonKey);
                    int store = 0;
                    Log.d("id_column", idCoulmn);
                    jsonTableNodes = jsonResponse.getJSONArray(jsonKey);
                    for (int i = 0; i < jsonTableNodes.length(); i++) {
                        JSONObject jsonTableRow = jsonTableNodes
                                .getJSONObject(i);

                        if (jsonTableRow.has(idCoulmn)) {
                            try {

                                String id_column_value = jsonTableRow
                                        .getString(idCoulmn);
                                getRowByidList = Global.dbObject
                                        .queryFromTable(jsonKey, null, idCoulmn
                                                + "=" + id_column_value);
                                ContentValues cvColumns = new ContentValues();
                                Iterator<?> jsonTableRowKeys = jsonTableRow
                                        .keys();
                                while (jsonTableRowKeys.hasNext()) {
                                    String table_column = (String) jsonTableRowKeys
                                            .next();
                                    // Log.d("table_column", table_column);

                                    cvColumns.put(table_column, jsonTableRow
                                            .getString(table_column));
                                }
                                if (getRowByidList.size() > 0) {
                                    // UPDATE THE ROW JUST FOUND
                                    cvColumns.remove(idCoulmn);
                                    Global.dbObject.updateIntoTable(jsonKey,
                                            cvColumns, idCoulmn + "="
                                                    + id_column_value);
                                    // Log.d("row_num",
                                    // "row found...so update data");
                                } else {
                                    // INSERT THE ROW
                                    //Log.e("row name", jsonKey);
                                    // if (dialog != null)
                                    // dialog.setTitle("Inserting " + jsonKey);
                                    if (jsonKey.equals("TRN_MSG") && store == 0) {
                                        store = 1;

                                        msg = jsonTableRow
                                                .getString("MSG_SUBJECT");
                                        if (store == 1) {
                                            Intent intent = new Intent(context,
                                                    message_view.class);
                                            notificationBuilder
                                                    .generateNotification(
                                                            context,
                                                            "New message", msg,
                                                            intent, false);
                                            store = 2;
                                        }
                                    }

                                    Global.dbObject.insertIntoTable(jsonKey,
                                            cvColumns);
                                }

                            } catch (Exception ex) {
                                Log.d("msg_exception",
                                        "SQL Exception:" + ex.getMessage());
                                loger.appendLog("SQL Exception:"
                                        + ex.getMessage());
                                down_no = "";
                            }
                            // Log.d("content_values", cvColumns.toString());
                        } else {
                            Log.d("idcoumn", "id column not found");
                            // down_no = "";
                            // loger.appendLog("id column not found for " +
                            // idCoulmn);
                        }

                        count += jsonTableNodes.length();
                        // if (showDialog)
                        // if (dialog != null)
                        // dialog.setProgress((int) ((count * 100) /
                        // jsonResponse
                        // .length()));
                    }
                }
            }

			/*
			 * if (!msg.isEmpty()) { Intent intent = new Intent(context,
			 * message_view.class);
			 * notificationBuilder.generateNotification(context,
			 * "New message received", msg, intent, false); }
			 */

            SharedPreferences prefs = context.getSharedPreferences("MY_PREFS",
                    0);
            if (!down_no.equals(""))
                pure_helper.send_down_no(
                        down_no,
                        context,
                        prefs.getString("user_email", "0") + '-'
                                + prefs.getString("user_no", "0"));
            Editor editor = prefs.edit();
            editor.putString("IS_ALL_DOWNLOAD", "0");
            editor.commit();

        } catch (Exception ex) {
            Log.d("msg",
                    "error encountered during executing content:"
                            + ex.getMessage());
            Log.d("msg", "content of data Download:" + content);
            loger.appendLog("error encountered during executing content:"
                    + ex.getMessage());
            // if (showDialog)
            // if (dialog.isShowing())
            // dialog.dismiss();
        }
    }

    private void initiateTableIdColumns() {
        // TODO Auto-generated method stub
        idTableDictionary.put("SEC_USERS", "USER_NO");
        idTableDictionary.put("SEC_USER_THANA", "USER_THANA_NO");
        idTableDictionary.put("SET_CLASS", "CLASS_NO");
        idTableDictionary.put("SET_CLIENT_INFO", "CLIENT_NO");
        idTableDictionary.put("SET_DIVISION", "DIVISION_NO");
        idTableDictionary.put("SET_EXP_TYPE", "EXP_TYPE_NO");
        idTableDictionary.put("SET_FEEDBACK_TYPE", "FEEDBACK_TYPE_NO");
        idTableDictionary.put("SET_INSTITUTE", "INSTITUTE_NO");
        idTableDictionary.put("SET_INST_TYPE", "INST_TYPE_NO");
        idTableDictionary.put("SET_PROMO_ITEM", "PROMO_ITEM_NO");
        idTableDictionary.put("SET_SPECIMEN", "SPECIMEN_NO");
        idTableDictionary.put("SET_TEACHER_INFO", "TEACHER_NO");
        idTableDictionary.put("SET_TEACHER_DESIG", "TEACH_DESIG_NO");
        idTableDictionary.put("SET_TEACHER_DESIG", "TEACH_DESIG_NO");
        idTableDictionary.put("SET_THANA", "THANA_NO");
        idTableDictionary.put("SET_SUBJECT", "SUBJECT_NO");
        idTableDictionary.put("SET_TRANSPORT_TYPE", "TRANS_TYPE_NO");
        idTableDictionary.put("SET_WORK_PURPOSE", "WORK_PUR_NO");
        idTableDictionary.put("SET_ZILLA", "ZILLA_NO");
        idTableDictionary.put("SET_ZONE", "ZONE_NO");
        idTableDictionary.put("SET_ORG_TYPE", "ORG_TYPE_NO");
        idTableDictionary.put("TRN_USER_SPECIMEN", "USER_SPECIMEN_NO");
        idTableDictionary.put("TRN_USER_SPECIMEN_DET", "SPECIMEN_DET_NO");
        idTableDictionary.put("TRN_MSG", "MSG_NO");
        idTableDictionary.put("TRN_USER_PROMO_ITEM", "USER_PROMO_NO");
        idTableDictionary.put("TRN_USER_PROMO_DET", "USER_PROMO_DET_NO");
        idTableDictionary.put("SET_LOGOUT_TYPE", "LOGOUT_TYPE_NO");
    }

}
