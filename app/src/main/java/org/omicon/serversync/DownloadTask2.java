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
import org.omicon.helper.notificationBuilder;
import org.omicon.initial.Global;
import org.omicon.view.message_view;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
//import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class DownloadTask2 extends AsyncTask<String, Void, JSONObject> {
    private Context context;
    private ProgressDialog dialog = null;
    public Map<String, String> idTableDictionary = new HashMap<String, String>();

    public DownloadTask2(Context con) {
        context = con;
        initiateTableIdColumns();
    }

    public JSONObject DownloadDataFromServer(String url_str) {
        Log.d("msg", "Start Downloading from " + url_str);

        JSONObject json = new JSONObject();
        String exception_message = "", content = "", success = "false";
        InputStream in = null;
        //StringBuilder sb=new StringBuilder();
        try {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netState = manager.getActiveNetworkInfo();
            if (netState != null && netState.isConnected()) {
                try {
                    //URL url=new URL(url_str);

                    HttpClient client = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(url_str);
                    HttpResponse response = client.execute(httpGet);

                    //in=conn.getInputStream();
                    Log.d("msg", "Content-Length:" + response.getEntity().getContentLength());
                    in = response.getEntity().getContent();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    long count = 0;
                    while ((line = reader.readLine()) != null) {
                        content += line;
                        count += line.getBytes().length;
                    }
                    Log.d("msg", "Content Length After Download:" + count);
                    success = "true";
                } catch (Exception ex) {
                    exception_message = ex.getMessage();
                }
            } else {
                exception_message = "Check your network Connection,It's not Active";
            }
        } catch (Exception ex) {
            exception_message = ex.getMessage();
        } finally {
            try {
                if (in != null)
                    in.close();
                json.put("Success", success);
                json.put("Content", content);
                json.put("Exception", exception_message);

            } catch (Exception e) {
                Log.d("msg", "Sorry Exception Occured:" + e.getMessage());
            }
        }
        return json;
    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();
        dialog = new ProgressDialog(context);
        dialog.setTitle("Downloading...");
        dialog.show();
    }

    @Override
    protected JSONObject doInBackground(String... arg0) {
        return DownloadDataFromServer(arg0[0]);
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        Log.d("msg", "Download Complete");
        try {
            Log.d("msg", "Success:" + result.getString("Success"));
            Log.d("msg", "Exception:" + result.getString("Exception"));
            //Log.d("msg","Content:"+result.getString("Content"));
            Log.d("msg", "Complete");

            if (result.getString("Success").matches("true"))
                executeData(result.getString("Content"));
            dialog.dismiss();

        } catch (JSONException e) {
            Log.d("msg", e.getMessage());
            e.printStackTrace();
        }

        Toast.makeText(context, "Download Complete", Toast.LENGTH_LONG).show();
    }

    public void executeData(String content) {
        JSONObject jsonResponse;
        JSONArray jsonTableNodes;
        String idCoulmn, msg = "";
        ;
        ArrayList<String[]> getRowByidList = null;
        try {

            /*********** Creates a new JSONObject with name/value mappings from the JSON string. ********/

            jsonResponse = new JSONObject(content);
            Iterator<?> jsonTableKeys = jsonResponse.keys();


            while (jsonTableKeys.hasNext()) {
                String jsonKey = (String) jsonTableKeys.next();
                Log.d("jkey_table", jsonKey);
                jsonTableNodes = jsonResponse.getJSONArray(jsonKey);
                int store = 0;
                idCoulmn = idTableDictionary.get(jsonKey);

                Log.d("id_column", idCoulmn);
                for (int i = 0; i < jsonTableNodes.length(); i++) {
                    JSONObject jsonTableRow = jsonTableNodes.getJSONObject(i);

                    if (jsonTableRow.has(idCoulmn)) {
                        try {
                            String id_column_value = jsonTableRow.getString(idCoulmn);
                            getRowByidList = Global.dbObject.queryFromTable(jsonKey, null, idCoulmn + "=" + id_column_value);
                            ContentValues cvColumns = new ContentValues();
                            Iterator<?> jsonTableRowKeys = jsonTableRow.keys();
                            while (jsonTableRowKeys.hasNext()) {
                                String table_column = (String) jsonTableRowKeys.next();
                                // Log.d("table_column", table_column);
                                cvColumns.put(table_column, jsonTableRow.getString(table_column));
                            }
                            if (getRowByidList.size() > 0) {
                                // UPDATE THE ROW JUST FOUND
                                cvColumns.remove(idCoulmn);
                                Global.dbObject.updateIntoTable(jsonKey, cvColumns, idCoulmn + "=" + id_column_value);
                                Log.d("row_num", "row found...so update data");
                            } else {
                                // INSERT THE ROW
                                if (jsonKey.equals("TRN_MSG") && store == 0) {
                                    store = 1;

                                    msg = jsonTableRow.getString("MSG_SUBJECT");

                                }
                                Log.d("row_num", "row not found...so inserting data");
                                Global.dbObject.insertIntoTable(jsonKey, cvColumns);
                            }
                        } catch (Exception ex) {
                            Log.d("msg_exception", "SQL Exception:" + ex.getMessage());
                        }
                        //Log.d("content_values", cvColumns.toString());
                    } else {
                        Log.d("idcoumn", "id column not found");
                    }
                }
            }
            if (!msg.isEmpty()) {
                Intent intent = new Intent(context, message_view.class);
                notificationBuilder.generateNotification(context, "New message received", msg, intent, false);
            }
            SharedPreferences prefs = context.getSharedPreferences("MY_PREFS", 0);
            Editor editor = prefs.edit();
            editor.putString("IS_ALL_DOWNLOAD", "0");
            editor.commit();

        } catch (Exception ex) {
            Log.d("msg", "error encountered during executing content:" + ex.getMessage());
            Log.d("msg", "content of data Download:" + content);
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
