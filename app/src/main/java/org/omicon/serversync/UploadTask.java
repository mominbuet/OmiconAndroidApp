package org.omicon.serversync;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.omicon.initial.Global;
import org.omicon.initial.logger;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import android.util.JsonToken;
import android.util.Log;
import android.widget.Toast;

public class UploadTask extends AsyncTask<String, Void, String> {
    private Context context;
    private SimpleDateFormat sdfOfflineDateTime = null;
    // , sdfOfflineDate = null, sdfOfflineTime = null;
    //ProgressDialog dialog = null;
    logger loger;
    private static int chunk_size = 1;
    private ArrayList<String> detList;

    public UploadTask(Context con) {
        context = con;
        loger = new logger(context);
        sdfOfflineDateTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        detList = new ArrayList<String>();
        // sdfOfflineDateTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a",
        // Locale.US);
        // sdfOfflineDateTime.setTimeZone(TimeZone.getTimeZone("GMT+06:00"));

        // sdfOfflineDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        // sdfOfflineTime = new SimpleDateFormat("hh:mm:ss", Locale.US);
    }

    public JSONObject dcrDetailsJson(String detid) {
        try {
            // ArrayList<String[]> dcrMasterDataList =
            // Global.dbObject.queryFromTable("TRN_DCR", null,
            // "IS_UPLOADED = 0 ");
            // ArrayList<String[]> tmp =
            // Global.dbObject.rawqueryFromDatabase("SELECT OFFLINE_DCR_NO,count(OFFLINE_DCR_DET_NO) as cnt FROM TRN_DCR_DET WHERE IS_UPLOADED=0 GROUP BY OFFLINE_DCR_NO order by cnt desc LIMIT 0,1");
            // Log.e("******","SELECT * FROM TRN_DCR WHERE OFFLINE_DCR_NO="+detid+" LIMIT 0,1");
            ArrayList<String[]> dcrMasterDataList = Global.dbObject
                    .rawqueryFromDatabase("SELECT * FROM TRN_DCR WHERE OFFLINE_DCR_NO="
                            + detid + " LIMIT 0,1");
            if (dcrMasterDataList.size() != 0) {

                JSONObject dcr_info_bulk = new JSONObject();
                JSONArray dcrMasterArray = new JSONArray();

                for (String[] dcrMasterItemRow : dcrMasterDataList) {
                    JSONObject dcr_master_info = new JSONObject();

                    dcr_master_info.put("OFFLINE_DCR_NO", dcrMasterItemRow[0]);
                    // dcr_master_info.put("ACTION_OFFLINE_TIME",
                    // sdfOfflineDateTime.format(Long.parseLong(dcrMasterItemRow[1])));
                    dcr_master_info.put("ACTION_OFFLINE_TIME",
                            Global.getDateWithTime(dcrMasterItemRow[1]));
                    dcr_master_info.put("DCR_TYPE_NO", dcrMasterItemRow[2]);
                    dcr_master_info.put("IS_REF_ZM", dcrMasterItemRow[3]);
                    dcr_master_info.put("REF_ZM_USER_NO", dcrMasterItemRow[4]);
                    dcr_master_info.put("REF_ZM_MOBILE", dcrMasterItemRow[5]);
                    dcr_master_info.put("WORK_AREA_FROM_LAT",
                            dcrMasterItemRow[6]);
                    dcr_master_info.put("WORK_AREA_FROM_LON",
                            dcrMasterItemRow[7]);
                    dcr_master_info.put("WORK_AREA_FROM_NAME",
                            dcrMasterItemRow[8]);
                    dcr_master_info
                            .put("WORK_AREA_TO_LAT", dcrMasterItemRow[9]);
                    dcr_master_info.put("WORK_AREA_TO_LON",
                            dcrMasterItemRow[10]);
                    dcr_master_info.put("WORK_AREA_TO_NAME",
                            dcrMasterItemRow[11]);

                    // dcr_master_info.put("TIME_FROM",
                    // sdfOfflineDateTime.format(Long.parseLong(dcrMasterItemRow[12])));
                    dcr_master_info.put("TIME_FROM",
                            Global.getDateWithTime(dcrMasterItemRow[12]));
                    // dcr_master_info.put("TIME_TO",
                    // sdfOfflineDateTime.format(Long.parseLong(dcrMasterItemRow[13])));
                    dcr_master_info.put("TIME_TO",
                            Global.getDateWithTime(dcrMasterItemRow[13]));

                    dcr_master_info.put("TIME_GAP", dcrMasterItemRow[14]);
                    dcr_master_info.put("INSTITUTE_NO", dcrMasterItemRow[15]);
                    dcr_master_info.put("TRANS_TYPE_NO", dcrMasterItemRow[16]);
                    dcr_master_info.put("FARE_AMT", dcrMasterItemRow[17]);

                    String[] date_spilit = dcrMasterItemRow[18].split("/");

                    dcr_master_info.put("TRN_DCR_DATE", date_spilit[2] + "-"
                            + date_spilit[1] + "-" + date_spilit[0]);

                    dcr_master_info
                            .put("IS_MANUAL_ENTRY", dcrMasterItemRow[19]);
                    dcr_master_info.put("COMMENTS", dcrMasterItemRow[20]);
                    dcr_master_info.put("ENTRY_STATE", dcrMasterItemRow[21]);
                    dcr_master_info.put("USER_NO", dcrMasterItemRow[24]);

                    JSONArray dcrJsonArray = new JSONArray();

                    ArrayList<String[]> dcrDetails = Global.dbObject
                            .queryFromTable("TRN_DCR_DET", null,
                                    "IS_UPLOADED = 0 AND OFFLINE_DCR_NO = "
                                            + dcrMasterItemRow[0] + " limit 0,"
                                            + Global.detailChunk);
                    for (String[] dcrDetItem : dcrDetails) {

                        JSONObject dcrDetailData = new JSONObject();
                        detList.add(dcrDetItem[0]);
                        dcrDetailData.put("OFFLINE_DCR_DET_NO", dcrDetItem[0]);
                        dcrDetailData.put("OFFLINE_DCR_NO", dcrDetItem[1]);
                        // dcrDetailData.put("ACTION_OFFLINE_TIME",
                        // sdfOfflineDateTime.format(Long.parseLong(dcrDetItem[2])));
                        dcrDetailData.put("ACTION_OFFLINE_TIME",
                                Global.getDateWithTime(dcrDetItem[2]));

                        dcrDetailData.put("IS_FOR_TEACHER", dcrDetItem[3]);
                        dcrDetailData.put("TEACHER_NO", dcrDetItem[4]);
                        dcrDetailData.put("TEACHER_MOBILE", dcrDetItem[5]);
                        dcrDetailData.put("SPECIMEN_NO", dcrDetItem[6]);
                        dcrDetailData.put("SPECIMEN_QTY", dcrDetItem[7]);
                        dcrDetailData.put("IS_FOR_CLIENT", dcrDetItem[8]);
                        dcrDetailData.put("CLIENT_NO", dcrDetItem[9]);
                        dcrDetailData.put("CLIENT_MOBILE", dcrDetItem[10]);
                        dcrDetailData.put("PROMO_ITEM_NO", dcrDetItem[11]);
                        dcrDetailData.put("PROMO_ITEM_QTY", dcrDetItem[12]);
                        dcrDetailData.put("ENTRY_STATE", dcrDetItem[13]);
                        dcrDetailData.put("IS_ON_BEHALF", dcrDetItem[16]);
                        dcrDetailData.put("BEHALF_MOBILE", dcrDetItem[17]);

                        dcrDetailData.put("BEHALF_NICK", dcrDetItem[18]);
                        dcrDetailData.put("TEACHER_NICK", dcrDetItem[19]);
                        dcrJsonArray.put(dcrDetailData);
                    }

                    dcr_master_info.put("TRN_DCR_DET_UP", dcrJsonArray);

                    dcrMasterArray.put(dcr_master_info);

                }

                dcr_info_bulk.put("TRN_DCR_UP", dcrMasterArray);

                JSONObject user_info = new JSONObject();
                SharedPreferences prefs = context.getSharedPreferences(
                        "MY_PREFS", 0);

                user_info.put("USER_NAME", prefs.getString("user_email", ""));
                dcr_info_bulk.put("USER_INFO", user_info);

                Log.d("msg", dcr_info_bulk.toString());
                // loger.appendBiggerLog("dcr  result: "+dcr_info_bulk.toString(1));
                return dcr_info_bulk;
            } else
                return new JSONObject();

        } catch (Exception ex) {
            Log.e("DCR2 error querying", "error in creating DCR json object:"
                    + ex.getMessage());
            loger.appendLog("Error in querying dcr :: " + ex.getMessage());
            return null;
        }
    }

    public JSONObject derDetailsJson(String expid) {
        try {
            // ArrayList<String[]> expenditureMasters =
            // Global.dbObject.queryFromTable("TRN_EXPENSE", null,
            // "IS_UPLOADED = 0");
            ArrayList<String[]> expenditureMasters = Global.dbObject
                    .rawqueryFromDatabase("SELECT * FROM TRN_EXPENSE WHERE OFFLINE_EXP_NO ="
                            + expid + " LIMIT 0,1");

            if (expenditureMasters.size() != 0) {
                JSONObject expn_info_bulk = new JSONObject();
                JSONArray expMasterArray = new JSONArray();
                for (String[] expMasterItem : expenditureMasters) {

                    JSONObject expn_info = new JSONObject();

                    expn_info.put("OFFLINE_EXP_NO", expMasterItem[0]);
                    // expn_info.put("ACTION_OFFLINE_TIME",
                    // sdfOfflineDateTime.format(Long.parseLong(expMasterItem[1])));
                    expn_info.put("ACTION_OFFLINE_TIME",
                            Global.getDateWithTime(expMasterItem[1]));

                    String[] date_spilit = expMasterItem[2].split("/");

                    expn_info.put("TRN_EXP_DATE", date_spilit[2] + "-"
                            + date_spilit[1] + "-" + date_spilit[0]);
                    expn_info.put("IS_MANUAL_ENTRY", expMasterItem[3]);
                    expn_info.put("ENTRY_STATE", expMasterItem[4]);
                    expn_info.put("USER_NO", expMasterItem[7]);

                    JSONArray expJsonArray = new JSONArray();

                    ArrayList<String[]> expenditureDetails = Global.dbObject
                            .queryFromTable("TRN_EXPENSE_DET", null,
                                    "IS_UPLOADED = 0 AND OFFLINE_EXP_NO = "
                                            + expMasterItem[0]);
                    for (String[] expenditureDetItem : expenditureDetails) {

                        JSONObject expDetailData = new JSONObject();
                        expDetailData.put("OFFLINE_EXP_DET_NO",
                                expenditureDetItem[0]);
                        expDetailData.put("OFFLINE_EXP_NO",
                                expenditureDetItem[1]);
                        // expDetailData.put("ACTION_OFFLINE_TIME",
                        // sdfOfflineDateTime.format(Long.parseLong(expenditureDetItem[2])));
                        expDetailData.put("ACTION_OFFLINE_TIME",
                                Global.getDateWithTime(expenditureDetItem[2]));
                        expDetailData.put("EXP_TYPE_NO", expenditureDetItem[3]);
                        expDetailData.put("EXP_AMT", expenditureDetItem[4]);
                        expDetailData.put("ENTRY_STATE", expenditureDetItem[9]);
                        expDetailData.put("VENDOR", expenditureDetItem[7]);
                        expDetailData.put("COMMENTS", expenditureDetItem[8]);
                        expDetailData.put("LAT_VAL", expenditureDetItem[5]);
                        expDetailData.put("LON_VAL", expenditureDetItem[6]);

                        expJsonArray.put(expDetailData);

                    }

                    // if(expenditureDetails.size() > 1){
                    expn_info.put("TRN_EXPENSE_DET_UP", expJsonArray);
                    // }

                    expMasterArray.put(expn_info);
                }
                expn_info_bulk.put("TRN_EXPENSE_UP", expMasterArray);

                JSONObject user_info = new JSONObject();
                SharedPreferences prefs = context.getSharedPreferences(
                        "MY_PREFS", 0);
                user_info.put("USER_NAME", prefs.getString("user_email", ""));

                expn_info_bulk.put("USER_INFO", user_info);

                Log.d("msg", "Expenditure:" + expn_info_bulk.toString());

                return expn_info_bulk;
            } else
                return new JSONObject();
        } catch (Exception ex) {
            loger.appendLog("Error in querying Expense :: " + ex.getMessage());
            Log.d("msg",
                    "error in creating Expenditure json object:"
                            + ex.getMessage());
            return null;
        }
    }

    public JSONObject dcrJson() {
        try {
            // ArrayList<String[]> dcrMasterDataList =
            // Global.dbObject.queryFromTable("TRN_DCR", null,
            // "IS_UPLOADED = 0 ");
            ArrayList<String[]> dcrMasterDataList = Global.dbObject
                    .rawqueryFromDatabase("SELECT * FROM TRN_DCR WHERE IS_UPLOADED=0 ORDER BY OFFLINE_DCR_NO LIMIT 0,"
                            + chunk_size);

            if (dcrMasterDataList.size() != 0) {

                JSONObject dcr_info_bulk = new JSONObject();
                JSONArray dcrMasterArray = new JSONArray();

                for (String[] dcrMasterItemRow : dcrMasterDataList) {
                    JSONObject dcr_master_info = new JSONObject();

                    dcr_master_info.put("OFFLINE_DCR_NO", dcrMasterItemRow[0]);
                    // dcr_master_info.put("ACTION_OFFLINE_TIME",
                    // sdfOfflineDateTime.format(Long.parseLong(dcrMasterItemRow[1])));
                    dcr_master_info.put("ACTION_OFFLINE_TIME",
                            Global.getDateWithTime(dcrMasterItemRow[1]));
                    dcr_master_info.put("DCR_TYPE_NO", dcrMasterItemRow[2]);
                    dcr_master_info.put("IS_REF_ZM", dcrMasterItemRow[3]);
                    dcr_master_info.put("REF_ZM_USER_NO", dcrMasterItemRow[4]);
                    dcr_master_info.put("REF_ZM_MOBILE", dcrMasterItemRow[5]);
                    dcr_master_info.put("WORK_AREA_FROM_LAT",
                            dcrMasterItemRow[6]);
                    dcr_master_info.put("WORK_AREA_FROM_LON",
                            dcrMasterItemRow[7]);
                    dcr_master_info.put("WORK_AREA_FROM_NAME",
                            dcrMasterItemRow[8]);
                    dcr_master_info
                            .put("WORK_AREA_TO_LAT", dcrMasterItemRow[9]);
                    dcr_master_info.put("WORK_AREA_TO_LON",
                            dcrMasterItemRow[10]);
                    dcr_master_info.put("WORK_AREA_TO_NAME",
                            dcrMasterItemRow[11]);

                    // dcr_master_info.put("TIME_FROM",
                    // sdfOfflineDateTime.format(Long.parseLong(dcrMasterItemRow[12])));
                    dcr_master_info.put("TIME_FROM",
                            Global.getDateWithTime(dcrMasterItemRow[12]));
                    // dcr_master_info.put("TIME_TO",
                    // sdfOfflineDateTime.format(Long.parseLong(dcrMasterItemRow[13])));
                    dcr_master_info.put("TIME_TO",
                            Global.getDateWithTime(dcrMasterItemRow[13]));

                    dcr_master_info.put("TIME_GAP", dcrMasterItemRow[14]);
                    dcr_master_info.put("INSTITUTE_NO", dcrMasterItemRow[15]);
                    dcr_master_info.put("TRANS_TYPE_NO", dcrMasterItemRow[16]);
                    dcr_master_info.put("FARE_AMT", dcrMasterItemRow[17]);

                    String[] date_spilit = dcrMasterItemRow[18].split("/");

                    dcr_master_info.put("TRN_DCR_DATE", date_spilit[2] + "-"
                            + date_spilit[1] + "-" + date_spilit[0]);

                    dcr_master_info
                            .put("IS_MANUAL_ENTRY", dcrMasterItemRow[19]);
                    dcr_master_info.put("COMMENTS", dcrMasterItemRow[20]);
                    dcr_master_info.put("ENTRY_STATE", dcrMasterItemRow[21]);
                    dcr_master_info.put("USER_NO", dcrMasterItemRow[24]);

                    JSONArray dcrJsonArray = new JSONArray();

                    ArrayList<String[]> dcrDetails = Global.dbObject
                            .queryFromTable("TRN_DCR_DET", null,
                                    "IS_UPLOADED = 0 AND OFFLINE_DCR_NO = "
                                            + dcrMasterItemRow[0] + " limit 0,"
                                            + Global.detailChunk);
                    for (String[] dcrDetItem : dcrDetails) {

                        JSONObject dcrDetailData = new JSONObject();
                        detList.add(dcrDetItem[0]);
                        dcrDetailData.put("OFFLINE_DCR_DET_NO", dcrDetItem[0]);
                        dcrDetailData.put("OFFLINE_DCR_NO", dcrDetItem[1]);
                        // dcrDetailData.put("ACTION_OFFLINE_TIME",
                        // sdfOfflineDateTime.format(Long.parseLong(dcrDetItem[2])));
                        dcrDetailData.put("ACTION_OFFLINE_TIME",
                                Global.getDateWithTime(dcrDetItem[2]));

                        dcrDetailData.put("IS_FOR_TEACHER", dcrDetItem[3]);
                        dcrDetailData.put("TEACHER_NO", dcrDetItem[4]);
                        dcrDetailData.put("TEACHER_MOBILE", dcrDetItem[5]);
                        dcrDetailData.put("SPECIMEN_NO", dcrDetItem[6]);
                        dcrDetailData.put("SPECIMEN_QTY", dcrDetItem[7]);
                        dcrDetailData.put("IS_FOR_CLIENT", dcrDetItem[8]);
                        dcrDetailData.put("CLIENT_NO", dcrDetItem[9]);
                        dcrDetailData.put("CLIENT_MOBILE", dcrDetItem[10]);
                        dcrDetailData.put("PROMO_ITEM_NO", dcrDetItem[11]);
                        dcrDetailData.put("PROMO_ITEM_QTY", dcrDetItem[12]);
                        dcrDetailData.put("ENTRY_STATE", dcrDetItem[13]);
                        dcrDetailData.put("IS_ON_BEHALF", dcrDetItem[16]);
                        dcrDetailData.put("BEHALF_MOBILE", dcrDetItem[17]);

                        dcrDetailData.put("BEHALF_NICK", dcrDetItem[18]);
                        dcrDetailData.put("TEACHER_NICK", dcrDetItem[19]);

                        dcrJsonArray.put(dcrDetailData);
                    }

                    dcr_master_info.put("TRN_DCR_DET_UP", dcrJsonArray);

                    dcrMasterArray.put(dcr_master_info);

                }

                dcr_info_bulk.put("TRN_DCR_UP", dcrMasterArray);

                JSONObject user_info = new JSONObject();
                SharedPreferences prefs = context.getSharedPreferences(
                        "MY_PREFS", 0);

                user_info.put("USER_NAME", prefs.getString("user_email", ""));
                dcr_info_bulk.put("USER_INFO", user_info);

                Log.d("msg", dcr_info_bulk.toString());
                // loger.appendBiggerLog("dcr  result: "+dcr_info_bulk.toString(1));
                return dcr_info_bulk;
            } else
                return new JSONObject();

        } catch (Exception ex) {
            Log.d("DCR error querying", "error in creating DCR json object:"
                    + ex.getMessage());
            loger.appendLog("Error in querying dcr :: " + ex.getMessage());
            return null;
        }
    }

    public JSONObject derJson() {
        try {
            // ArrayList<String[]> expenditureMasters =
            // Global.dbObject.queryFromTable("TRN_EXPENSE", null,
            // "IS_UPLOADED = 0");
            ArrayList<String[]> expenditureMasters = Global.dbObject
                    .rawqueryFromDatabase("SELECT * FROM TRN_EXPENSE WHERE IS_UPLOADED=0  ORDER BY OFFLINE_EXP_NO LIMIT 0,"
                            + chunk_size);

            if (expenditureMasters.size() != 0) {
                JSONObject expn_info_bulk = new JSONObject();
                JSONArray expMasterArray = new JSONArray();
                for (String[] expMasterItem : expenditureMasters) {

                    JSONObject expn_info = new JSONObject();

                    expn_info.put("OFFLINE_EXP_NO", expMasterItem[0]);
                    // expn_info.put("ACTION_OFFLINE_TIME",
                    // sdfOfflineDateTime.format(Long.parseLong(expMasterItem[1])));
                    expn_info.put("ACTION_OFFLINE_TIME",
                            Global.getDateWithTime(expMasterItem[1]));

                    String[] date_spilit = expMasterItem[2].split("/");

                    expn_info.put("TRN_EXP_DATE", date_spilit[2] + "-"
                            + date_spilit[1] + "-" + date_spilit[0]);
                    expn_info.put("IS_MANUAL_ENTRY", expMasterItem[3]);
                    expn_info.put("ENTRY_STATE", expMasterItem[4]);
                    expn_info.put("USER_NO", expMasterItem[7]);

                    JSONArray expJsonArray = new JSONArray();

                    ArrayList<String[]> expenditureDetails = Global.dbObject
                            .queryFromTable("TRN_EXPENSE_DET", null,
                                    "IS_UPLOADED = 0 AND OFFLINE_EXP_NO = "
                                            + expMasterItem[0]);
                    for (String[] expenditureDetItem : expenditureDetails) {

                        JSONObject expDetailData = new JSONObject();
                        expDetailData.put("OFFLINE_EXP_DET_NO",
                                expenditureDetItem[0]);
                        expDetailData.put("OFFLINE_EXP_NO",
                                expenditureDetItem[1]);
                        // expDetailData.put("ACTION_OFFLINE_TIME",
                        // sdfOfflineDateTime.format(Long.parseLong(expenditureDetItem[2])));
                        expDetailData.put("ACTION_OFFLINE_TIME",
                                Global.getDateWithTime(expenditureDetItem[2]));
                        expDetailData.put("EXP_TYPE_NO", expenditureDetItem[3]);
                        expDetailData.put("EXP_AMT", expenditureDetItem[4]);
                        expDetailData.put("ENTRY_STATE", expenditureDetItem[9]);
                        expDetailData.put("VENDOR", expenditureDetItem[7]);
                        expDetailData.put("COMMENTS", expenditureDetItem[8]);
                        expDetailData.put("LAT_VAL", expenditureDetItem[5]);
                        expDetailData.put("LON_VAL", expenditureDetItem[6]);

                        expJsonArray.put(expDetailData);

                    }

                    // if(expenditureDetails.size() > 1){
                    expn_info.put("TRN_EXPENSE_DET_UP", expJsonArray);
                    // }

                    expMasterArray.put(expn_info);
                }
                expn_info_bulk.put("TRN_EXPENSE_UP", expMasterArray);

                JSONObject user_info = new JSONObject();
                SharedPreferences prefs = context.getSharedPreferences(
                        "MY_PREFS", 0);
                user_info.put("USER_NAME", prefs.getString("user_email", ""));

                expn_info_bulk.put("USER_INFO", user_info);

                Log.d("msg", "Expenditure:" + expn_info_bulk.toString());

                return expn_info_bulk;
            } else
                return new JSONObject();
        } catch (Exception ex) {
            loger.appendLog("Error in querying Expense :: " + ex.getMessage());
            Log.d("msg",
                    "error in creating Expenditure json object:"
                            + ex.getMessage());
            return null;
        }
    }

    public ArrayList<JSON_Range> getLocationJson() {
        ArrayList<JSON_Range> result_list = new ArrayList<JSON_Range>();
        try {
            ArrayList<String[]> locationsList = Global.dbObject
                    .rawqueryFromDatabase("SELECT * FROM TRN_USER_LOCATION WHERE IS_UPLOADED=0 ORDER BY OFFLINE_LOC_NO");
            String low = "";
            if (locationsList.size() != 0)
                low = locationsList.get(0)[0];

            JSONObject user_info = new JSONObject();
            SharedPreferences prefs = context.getSharedPreferences("MY_PREFS",
                    0);
            user_info.put("USER_NAME", prefs.getString("user_email", ""));

            JSONObject locatoin_object = new JSONObject();
            JSONArray location_infos = new JSONArray();
            JSONObject location_info;

            for (int i = 0; i < locationsList.size(); i++) {
                location_info = new JSONObject();

                location_info.put("OFFLINE_LOC_NO", locationsList.get(i)[0]);
                // location_info.put("ACTION_OFFLINE_TIME",sdfOfflineDateTime.format(Long.parseLong(locationItemRow[1])));
                location_info.put("ACTION_OFFLINE_TIME",
                        Global.getDateWithTime(locationsList.get(i)[1]));
                location_info.put("LAT_VAL", locationsList.get(i)[2]);
                location_info.put("LON_VAL", locationsList.get(i)[3]);
                location_info.put("LOCATION_NAME", locationsList.get(i)[4]);
                location_info.put("ENTRY_STATE", locationsList.get(i)[5]);
                location_info.put("USER_NO", locationsList.get(i)[8]);

                location_infos.put(location_info);

                if ((i + 1) % 8 == 0) {
                    locatoin_object.put("TRN_USER_LOCATION_UP", location_infos);
                    locatoin_object.put("USER_INFO", user_info);
                    result_list.add(new JSON_Range(locatoin_object, low,
                            locationsList.get(i)[0]));
                    low = locationsList.get(i)[0];

                    location_infos = new JSONArray();
                    locatoin_object = new JSONObject();
                }
            }
            if (locationsList.size() % 8 != 0) {
                locatoin_object.put("TRN_USER_LOCATION_UP", location_infos);
                locatoin_object.put("USER_INFO", user_info);
                result_list.add(new JSON_Range(locatoin_object, low,
                        locationsList.get(locationsList.size() - 1)[0]));
            }

        } catch (Exception ex) {
            Log.d("msg", "Error Creating Location Json Array");
        }
        return result_list;
    }

    public JSONObject locationJson() {
        ArrayList<String[]> locationsList = Global.dbObject.queryFromTable(
                "TRN_USER_LOCATION", null, "IS_UPLOADED = 0");
        try {
            if (locationsList.size() != 0) {
                JSONObject locatoin_object = new JSONObject();
                JSONArray location_infos = new JSONArray();
                JSONObject location_info;

                for (String[] locationItemRow : locationsList) {
                    location_info = new JSONObject();

                    location_info.put("OFFLINE_LOC_NO", locationItemRow[0]);
                    // location_info.put("ACTION_OFFLINE_TIME",sdfOfflineDateTime.format(Long.parseLong(locationItemRow[1])));
                    location_info.put("ACTION_OFFLINE_TIME",
                            Global.getDateWithTime(locationItemRow[1]));
                    location_info.put("LAT_VAL", locationItemRow[2]);
                    location_info.put("LON_VAL", locationItemRow[3]);
                    location_info.put("LOCATION_NAME", locationItemRow[4]);
                    location_info.put("ENTRY_STATE", locationItemRow[5]);
                    location_info.put("USER_NO", locationItemRow[8]);

                    location_infos.put(location_info);

                }

                locatoin_object.put("TRN_USER_LOCATION_UP", location_infos);

                JSONObject user_info = new JSONObject();
                SharedPreferences prefs = context.getSharedPreferences(
                        "MY_PREFS", 0);
                user_info.put("USER_NAME", prefs.getString("user_email", ""));

                locatoin_object.put("USER_INFO", user_info);

                Log.d("msg", locatoin_object.toString());

                return locatoin_object;
            } else
                return new JSONObject();

        } catch (JSONException ex) {
            Log.d("msg", "error in creating json object");
            return null;
        }
    }

    public JSONObject feedbackJson() {
        ArrayList<String[]> feedbacksList = Global.dbObject.queryFromTable(
                "TRN_USER_FEED_BACK", null, "IS_UPLOADED = 0");
        try {
            if (feedbacksList.size() != 0) {
                JSONObject feedback_object = new JSONObject();
                JSONArray feedback_infos = new JSONArray();
                JSONObject feedback_info;
                for (String[] feedbackItemRow : feedbacksList) {

                    feedback_info = new JSONObject();

                    feedback_info
                            .put("OFFLINE_FEEDBACK_NO", feedbackItemRow[0]);
                    // feedback_info.put("ACTION_OFFLINE_TIME",sdfOfflineDateTime.format(Long.parseLong(feedbackItemRow[1])));
                    feedback_info.put("ACTION_OFFLINE_TIME",
                            Global.getDateWithTime(feedbackItemRow[1]));
                    feedback_info.put("USER_NO", feedbackItemRow[2]);
                    feedback_info.put("FEEDBACK_TYPE_NO", feedbackItemRow[3]);
                    feedback_info.put("MESSAGE", feedbackItemRow[4]);
                    feedback_info.put("ENTRY_STATE", feedbackItemRow[5]);

                    feedback_infos.put(feedback_info);
                }

                feedback_object.put("TRN_USER_FEED_BACK_UP", feedback_infos);
                Log.d("msg", feedback_object.toString());

                return feedback_object;
            } else
                return new JSONObject();
        } catch (JSONException ex) {
            Log.d("msg", "error in creating json object");
            return null;
        }
    }

    public JSONObject loginfoJson() {
        ArrayList<String[]> data = Global.dbObject.queryFromTable(
                "TRN_LOG_INFO", null, null);
        try {
            Log.d("msg", "TRN_LOG_INFO Size:" + data.size());
            if (data.size() != 0) {
                JSONArray logJsonArray = new JSONArray();
                for (String[] str : data) {
                    if (!str[2].isEmpty()) {
                        JSONObject logJson = new JSONObject();
                        logJson.put("LOG_INFO_NO", str[0]);
                        logJson.put("LOGOUT_TYPE_NO", str[1]);
                        logJson.put("USER_NO", str[2]);
                        logJson.put("LOG_IN_LOCATION_NAME", str[3]);
                        logJson.put("LOG_IN_LAT", str[4]);
                        logJson.put("LOG_IN_LONG", str[5]);
                        logJson.put("LOG_IN_TIME",
                                Global.getDateWithTime(str[6]));
                        logJson.put("LOG_OUT_LOCATION_NAME", str[7]);
                        logJson.put("LOG_OUT_LAT", str[8]);
                        logJson.put("LOG_OUT_LONG", str[9]);
                        logJson.put("LOG_OUT_TIME",
                                Global.getDateWithTime(str[10]));
                        logJson.put("LOG_OUT_MESSAGE", str[11]);
                        logJsonArray.put(logJson);
                    }
                }
                JSONObject jsonR = new JSONObject();
                jsonR.put("TRN_LOG_INFO_BACK_UP", logJsonArray);

                Log.d("msg", jsonR.toString());
                return jsonR;
            } else
                return new JSONObject();
        } catch (Exception ex) {

            Log.d("login query error", "Error to create json of log info data:"
                    + ex.getMessage());
            loger.appendLog("login query error: " + ex.getMessage());
            // loger.appendLog("login result: "+jsonR.toString(2));
            return null;
        }
    }

    public String uploadToServer(String uploadURL, String content) {
        InputStream in = null;
        String dta = "";
        try {
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = null;

            HttpPost post = new HttpPost(uploadURL);
            StringEntity en = new StringEntity(content);
            en.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json"));
            post.setEntity(en);

            response = client.execute(post);

            if (response != null) {
                in = response.getEntity().getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    dta += line;
                }
                Log.d("msg", "message: " + dta);
            } else {
                Log.d("e", "Error In uploading Data");
            }
        } catch (Exception ex) {
            return "";
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (Exception e) {
            }
        }
        return dta;
    }

    public int GetUpto(String table_name, String column_name) {
        ArrayList<String[]> datas = Global.dbObject
                .rawqueryFromDatabase("SELECT max(" + column_name
                        + ") FROM (SELECT * from " + table_name
                        + " WHERE IS_UPLOADED=0 ORDER BY " + column_name
                        + " LIMIT 0," + chunk_size + ")");
        try {
            return Integer.parseInt(datas.get(0)[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    public String tableUploadData() {
        char[] success = {'0', '0', '0', '0'};
        ArrayList<String[]> masterDataList_dcr = new ArrayList<String[]>();
        ArrayList<String[]> masterDataList_expense = new ArrayList<String[]>();

        try {
            String dta;
            JSONObject jsonObject_all;
            JSONObject jsonObject_feedback = feedbackJson();
            JSONObject jsonObject_login = loginfoJson();
            // Upload DCR Data
            // Check entry state
            masterDataList_dcr = Global.dbObject.queryFromTable("TRN_DCR",
                    null, "IS_UPLOADED = 0 ");
            loger.appendLog("Total dcr size before entry  "
                    + masterDataList_dcr.size());
            masterDataList_expense = Global.dbObject.queryFromTable(
                    "TRN_EXPENSE", null, "IS_UPLOADED = 0");
            loger.appendLog("Total expense size before entry  "
                    + masterDataList_dcr.size());
            ArrayList<JSON_Range> jsonList_location = getLocationJson();

            int total_data_count = masterDataList_dcr.size()
                    + masterDataList_expense.size() + jsonList_location.size()
                    + jsonObject_feedback.length() + jsonObject_login.length()
                    + Global.extra_perc;
            int repeat = (int) Math.ceil(masterDataList_dcr.size()
                    / (double) chunk_size);

            if (repeat == 0)
                success[0] = '1';
            int uid_global = 0;
            for (int i = 0; i < repeat; i++) {
                Log.i("msg", "DCR REPEAT:" + i);
                try {
                    detList = new ArrayList<String>();
                    jsonObject_all = dcrJson();
                    if (jsonObject_all != null) {
                        if (jsonObject_all.length() == 0)
                            success[0] = '1';
                        else {
                            dta = uploadToServer(Global.uploadLink
                                    + "Upload_DCR", jsonObject_all.toString());
                            // new logger(context).appendLog(dta);
                            if (dta.toLowerCase(Locale.US).contains("true")) {
                                ContentValues TRN_user_dcr = new ContentValues();
                                TRN_user_dcr.put("IS_UPLOADED", "1");

                                int uid = GetUpto("TRN_DCR", "OFFLINE_DCR_NO");
                                Log.i("dcr upload", "UPTO DCR UPLOAD " + uid);
                                uid_global = uid;
                                Global.dbObject.deleteFromTable("TRN_DCR",
                                        " ENTRY_STATE = 3 AND OFFLINE_DCR_NO <= "
                                                + uid);
                                Global.dbObject.deleteFromTable("TRN_DCR_DET",
                                        " ENTRY_STATE = 3 AND OFFLINE_DCR_NO <= "
                                                + uid);

                                long rowid = Global.dbObject.updateIntoTable(
                                        "TRN_DCR", TRN_user_dcr,
                                        " IS_UPLOADED = 0 AND OFFLINE_DCR_NO <= "
                                                + uid);
                                Log.i("msg",
                                        "db_update_dcr "
                                                + String.valueOf(rowid));
                                int total_updated = 0;
                                for (String det_id : detList) {
                                    rowid = Global.dbObject.updateIntoTable(
                                            "TRN_DCR_DET", TRN_user_dcr,
                                            " IS_UPLOADED = 0 AND OFFLINE_DCR_DET_NO = "
                                                    + det_id);
                                    total_updated++;
                                }
                                Log.d("updated dcr det1", "db_update_dcr "
                                        + String.valueOf(total_updated));
                                success[0] = '1';
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("exception in dcr det1", e.getMessage());
                    loger.appendLog("ex in sending OFFLINE_DCR_NO "
                            + uid_global + " error " + e.getMessage());
                }
//				if (dialog != null)
//					dialog.setProgress((int) Math.ceil((i * 100)
//							/ total_data_count));
                // Log.e("progres here", String.valueOf((int) ((i * 100) /
                // total_data_count)));
            }
            // dialog.setProgress((int) (masterDataList_dcr.size() * 100 /
            // total_data_count));

            // Upload DER Data

            repeat = (int) Math.ceil(masterDataList_expense.size()
                    / (double) chunk_size);
            Log.d("msg", "TOTAL REPEAT NO:" + repeat);

            if (repeat == 0)
                success[1] = '1';
            for (int i = 0; i < repeat; i++) {
                Log.i("der repeat", "DER REPEAT NO:" + i);
                try {
                    jsonObject_all = derJson();
                    if (jsonObject_all != null) {
                        if (jsonObject_all.length() == 0)
                            success[1] = '1';
                        else {
                            dta = uploadToServer(Global.uploadLink
                                            + "Upload_Expense",
                                    jsonObject_all.toString());
                            if (dta.toLowerCase(Locale.US).contains("true")) {
                                ContentValues TRN_user_dcr = new ContentValues();
                                TRN_user_dcr.put("IS_UPLOADED", "1");

                                int uid = GetUpto("TRN_EXPENSE",
                                        "OFFLINE_EXP_NO");
                                Log.d("msg", "UPTO DER UPLOAD " + uid);
                                uid_global = uid;
                                Global.dbObject.deleteFromTable("TRN_EXPENSE",
                                        " ENTRY_STATE = 3 AND OFFLINE_EXP_NO <="
                                                + uid);
                                Global.dbObject.deleteFromTable(
                                        "TRN_EXPENSE_DET",
                                        " ENTRY_STATE = 3 AND OFFLINE_EXP_NO <="
                                                + uid);

                                long rowid = Global.dbObject.updateIntoTable(
                                        "TRN_EXPENSE", TRN_user_dcr,
                                        " IS_UPLOADED = 0 AND OFFLINE_EXP_NO <="
                                                + uid);
                                Log.d("msg",
                                        "db_update_der "
                                                + String.valueOf(rowid));
                                rowid = Global.dbObject.updateIntoTable(
                                        "TRN_EXPENSE_DET", TRN_user_dcr,
                                        " IS_UPLOADED = 0 AND OFFLINE_EXP_NO <="
                                                + uid);
                                Log.d("updated exp det1 ", "db_update_der "
                                        + String.valueOf(rowid));

                                success[1] = '1';
                            }
                        }
                    }
//					if (dialog != null)
//						dialog.setProgress((int) ((repeat + masterDataList_dcr
//								.size() * 100) / total_data_count));
                } catch (Exception e) {
                    loger.appendLog("ex in sending TRN_EXPENSE " + uid_global
                            + " error " + e.getMessage());
                }
            }
            // dialog.setProgress((int)((masterDataList_dcr.size()+masterDataList_expense.size())*100/total_data_count));
            // Upload Location Data
            /*
			 * jsonObject=locationJson(); if(jsonObject!=null){
			 * if(jsonObject.length()==0) success[2]='1'; else{
			 * dta=uploadToServer(Global.uploadLink+"Upload_UserLocation",
			 * jsonObject.toString());
			 * if(dta.toLowerCase(Locale.US).contains("true")){ ContentValues
			 * TRN_user_location = new ContentValues();
			 * TRN_user_location.put("IS_UPLOADED", "1");
			 * 
			 * Global.dbObject.deleteFromTable("TRN_USER_LOCATION",
			 * " ENTRY_STATE = 3"); Log.d("msg","deleted"); int rowid =
			 * Global.dbObject.updateIntoTable("TRN_USER_LOCATION",
			 * TRN_user_location ," IS_UPLOADED = 0"); Log.d("msg",
			 * String.valueOf(rowid));
			 * 
			 * success[2]='1'; } } }
			 */
            try {

                for (JSON_Range json_Range : jsonList_location) {
                    Log.d("msg", "User Location JSON:" + json_Range.value);
                    dta = uploadToServer(Global.uploadLink
                                    + "Upload_UserLocation",
                            json_Range.value.toString());
                    if (dta.toLowerCase(Locale.US).contains("true")) {
                        ContentValues TRN_user_location = new ContentValues();
                        TRN_user_location.put("IS_UPLOADED", "1");

                        Global.dbObject.deleteFromTable("TRN_USER_LOCATION",
                                " ENTRY_STATE = 3 AND OFFLINE_LOC_NO>="
                                        + json_Range.low
                                        + " AND OFFLINE_LOC_NO<="
                                        + json_Range.high);
                        Log.d("msg", "deleted");

                        int rowid = Global.dbObject.updateIntoTable(
                                "TRN_USER_LOCATION", TRN_user_location,
                                " IS_UPLOADED = 0 AND OFFLINE_LOC_NO>="
                                        + json_Range.low
                                        + " AND OFFLINE_LOC_NO<="
                                        + json_Range.high);
                        Log.d("msg", String.valueOf(rowid));

                        success[2] = '1';
                    }
//					if (dialog != null)
//						dialog.setProgress((int) ((masterDataList_dcr.size()
//								+ masterDataList_expense.size() + jsonList_location
//								.size()) * 100 / total_data_count));
                }
            } catch (Exception e) {
                loger.appendLog("error in location upload " + e.getMessage());
                Log.e("error upload_location", "Error Uploading Location data" + e.getMessage());
            }

            // Upload feedback Data
            if (jsonObject_feedback != null) {
                if (jsonObject_feedback.length() == 0)
                    success[3] = '1';
                else {
                    dta = uploadToServer(Global.uploadLink + "Upload_Feedback",
                            jsonObject_feedback.toString());
                    if (dta.toLowerCase(Locale.US).contains("true")) {
                        ContentValues TRN_user_feedback = new ContentValues();
                        TRN_user_feedback.put("IS_UPLOADED", "1");

                        Global.dbObject.deleteFromTable("TRN_USER_FEED_BACK",
                                " ENTRY_STATE = 3");
                        Log.d("msg", "deleted");
                        int rowid = Global.dbObject.updateIntoTable(
                                "TRN_USER_FEED_BACK", TRN_user_feedback,
                                " IS_UPLOADED = 0");
                        Log.d("msg", String.valueOf(rowid));

                        success[3] = '1';
                    }
                }
//				if (dialog != null)
//					dialog.setProgress((int) ((masterDataList_dcr.size()
//							+ masterDataList_expense.size()
//							+ jsonList_location.size() + jsonObject_feedback
//							.length()) * 100 / total_data_count));
            }

            jsonObject_all = jsonObject_login;
            if (jsonObject_all != null) {
                dta = uploadToServer(Global.uploadLink + "Upload_Login",
                        jsonObject_all.toString());
                if (dta.toLowerCase(Locale.US).contains("true")) {
                    long last_row_id = Global.dbObject.getLastRowID(
                            "TRN_LOG_INFO", "LOG_INFO_NO");
                    Global.dbObject.deleteFromTable("TRN_LOG_INFO",
                            "LOG_INFO_NO<>" + last_row_id);
                }
            } else {
                Log.d("msg", "log info json null");
            }

            ArrayList<String[]> tmp = Global.dbObject
                    .rawqueryFromDatabase("SELECT OFFLINE_DCR_NO,count(OFFLINE_DCR_DET_NO) as cnt FROM TRN_DCR_DET WHERE IS_UPLOADED=0 GROUP BY OFFLINE_DCR_NO order by cnt desc");
            // ArrayList<String[]> DetailDataList_dcr =
            // Global.dbObject.queryFromTable("TRN_DCR_DET", null,
            // "IS_UPLOADED = 0 ");
            repeat = (int) Math.ceil(tmp.size() / (double) chunk_size);

            if (repeat == 0)
                success[0] = '1';
            uid_global = 0;
            for (String[] detids : tmp) {
                Log.d("for det2 id", detids[0]);
                try {
                    detList = new ArrayList<String>();
                    jsonObject_all = dcrDetailsJson(detids[0]);
                    if (jsonObject_all != null) {
                        if (jsonObject_all.length() == 0)
                            success[0] = '1';
                        else {
                            dta = uploadToServer(Global.uploadLink
                                            + "Upload_DCR_DET",
                                    jsonObject_all.toString());
                            if (dta.toLowerCase(Locale.US).contains("true")) {
                                ContentValues TRN_user_dcr = new ContentValues();
                                TRN_user_dcr.put("IS_UPLOADED", "1");

                                int uid = GetUpto("TRN_DCR", "OFFLINE_DCR_NO");
                                Log.i("dcr upload", "UPTO DCR UPLOAD " + uid);
                                uid_global = uid;
                                Global.dbObject.deleteFromTable("TRN_DCR",
                                        " ENTRY_STATE = 3 AND OFFLINE_DCR_NO <= "
                                                + uid);
                                Global.dbObject.deleteFromTable("TRN_DCR_DET",
                                        " ENTRY_STATE = 3 AND OFFLINE_DCR_NO <= "
                                                + uid);

                                long rowid = Global.dbObject.updateIntoTable(
                                        "TRN_DCR", TRN_user_dcr,
                                        " IS_UPLOADED = 0 AND OFFLINE_DCR_NO <= "
                                                + uid);
                                Log.i("msg",
                                        "db_update_dcr "
                                                + String.valueOf(rowid));

                                int total_updated = 0;
                                for (String det_id : detList) {
                                    rowid = Global.dbObject.updateIntoTable(
                                            "TRN_DCR_DET", TRN_user_dcr,
                                            " IS_UPLOADED = 0 AND OFFLINE_DCR_DET_NO = "
                                                    + det_id);
                                    total_updated++;
                                }
                                Log.w("updated dcr det2", "db_update_dcr "
                                        + String.valueOf(total_updated));
                                success[0] = '1';
                            }
//							if (dialog != null)
//								dialog.setProgress((int) ((masterDataList_dcr
//										.size()
//										+ masterDataList_expense.size()
//										+ jsonList_location.size()
//										+ jsonObject_feedback.length()
//										+ jsonObject_login.length() + (Global.extra_perc / repeat--)) * 100 / total_data_count));
                        }
                    }
                } catch (Exception e) {
                    Log.e("exception in dcr det2", e.getMessage());
                    loger.appendLog("ex in sending OFFLINE_DCR_NO "
                            + uid_global + " error " + e.getMessage());
                }
                // dialog.setProgress((int) (repeat * 100 / total_data_count));
            }
            ArrayList<String[]> tmpExp = Global.dbObject
                    .rawqueryFromDatabase("SELECT OFFLINE_DCR_NO,count(OFFLINE_DCR_DET_NO) as cnt FROM TRN_DCR_DET WHERE IS_UPLOADED=0 GROUP BY OFFLINE_DCR_NO order by cnt desc");
            // ArrayList<String[]> DetailDataList_dcr =
            // Global.dbObject.queryFromTable("TRN_DCR_DET", null,
            // "IS_UPLOADED = 0 ");
            repeat = (int) Math.ceil(tmp.size() / (double) chunk_size);

            if (repeat == 0)
                success[0] = '1';
            uid_global = 0;
            for (String[] expids : tmpExp) {
                Log.w("for det2 id", expids[0]);
                try {
                    detList = new ArrayList<String>();
                    jsonObject_all = dcrDetailsJson(expids[0]);
                    if (jsonObject_all != null) {
                        if (jsonObject_all.length() == 0)
                            success[0] = '1';
                        else {
                            dta = uploadToServer(Global.uploadLink
                                            + "Upload_DCR_DET",
                                    jsonObject_all.toString());
                            if (dta.toLowerCase(Locale.US).contains("true")) {

                                ContentValues TRN_user_dcr = new ContentValues();
                                TRN_user_dcr.put("IS_UPLOADED", "1");

                                int uid = GetUpto("TRN_EXPENSE",
                                        "OFFLINE_EXP_NO");
                                Log.d("msg", "UPTO DER UPLOAD " + uid);
                                uid_global = uid;
                                Global.dbObject.deleteFromTable("TRN_EXPENSE",
                                        " ENTRY_STATE = 3 AND OFFLINE_EXP_NO <="
                                                + uid);
                                Global.dbObject.deleteFromTable(
                                        "TRN_EXPENSE_DET",
                                        " ENTRY_STATE = 3 AND OFFLINE_EXP_NO <="
                                                + uid);

                                long rowid = Global.dbObject.updateIntoTable(
                                        "TRN_EXPENSE", TRN_user_dcr,
                                        " IS_UPLOADED = 0 AND OFFLINE_EXP_NO <="
                                                + uid);
                                Log.d("msg",
                                        "db_update_der "
                                                + String.valueOf(rowid));
                                int total_updated = 0;
                                for (String det_id : detList) {
                                    rowid = Global.dbObject.updateIntoTable(
                                            "TRN_EXPENSE_DET", TRN_user_dcr,
                                            " IS_UPLOADED = 0 AND OFFLINE_EXP_NO <="
                                                    + uid);
                                    total_updated++;
                                }
                                Log.w("updated exp det2 ", "db_update_der "
                                        + String.valueOf(rowid));
                                success[1] = '1';
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("exception in dcr det2", e.getMessage());
                    loger.appendLog("ex in sending OFFLINE_EXP_NO "
                            + uid_global + " error " + e.getMessage());
                }
                // dialog.setProgress((int) (repeat * 100 / total_data_count));
            }
//			if (dialog != null)
//				dialog.setProgress((int) ((masterDataList_dcr.size()
//						+ masterDataList_expense.size()
//						+ jsonList_location.size()
//						+ jsonObject_feedback.length()
//						+ jsonObject_login.length() + Global.extra_perc) * 100 / total_data_count));
            Log.d("Upload success", "Success Message:" + new String(success));
            if (total_data_count == 0)
                Toast.makeText(context, "Uploaded Successfully",
                        Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
			/*
			 * if (dialog.isShowing()) { dialog.dismiss(); }
			 */
            if (masterDataList_expense.size() != 0
                    && masterDataList_expense.size() != 0)
                new logger(context).appendLog("uploading error: "
                        + ex.getMessage());
            Log.e("Upload error UP_task","uploading error:" + ex.getMessage());
        } /*
		 * finally { if (masterDataList_expense.size() == 0 &&
		 * masterDataList_expense.size() == 0) { if (dialog.isShowing()) {
		 * dialog.dismiss(); } Toast.makeText(context, "Uploaded partially",
		 * Toast.LENGTH_LONG); } }
		 */

        Global.toUploadCount = -1;
        return new String(success);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
//		if (dialog.isShowing()) {
//			dialog.dismiss();
//		}
        Toast.makeText(context, "Cancelled uploading", Toast.LENGTH_LONG);
    }

    @Override
    protected void onProgressUpdate(Void... progress) {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
//		dialog.setIndeterminate(false);
//		dialog.setMax(100);
//		dialog.setProgress(Integer.valueOf(progress[0].toString()));
    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();

//		if (dialog == null) {
//			dialog = new ProgressDialog(context);
//			dialog.setTitle("Uploading...");
//			dialog.setIndeterminate(false);
//			dialog.setMax(100);
//			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//			dialog.setCancelable(true);
//			dialog.show();
//		} else {
        Toast.makeText(context,
                "Upload running in background",
                Toast.LENGTH_LONG).show();
        //}
    }

    @Override
    protected String doInBackground(String... arg0) {
        return tableUploadData();
    }

    @Override
    protected void onPostExecute(String result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
//		if (dialog.isShowing() || dialog!=null) {
//			dialog.dismiss();
//			dialog=null;
//		}
        if (result.matches("1111") || result.matches("1101"))
            Toast.makeText(context, "Uploaded Successfully", Toast.LENGTH_LONG)
                    .show();
        else {
            loger.appendLog("upload Result " + result);
            char[] result_arr = result.toCharArray();
            if (result_arr[0] == '1' && result_arr[1] == '1')
                Toast.makeText(context, "Dcr & Expense Successfully uploaded.",
                        Toast.LENGTH_LONG);
            else if (result_arr[0] == '1' && result_arr[1] == '0')
                Toast.makeText(
                        context,
                        "Dcr Successfully uploaded, Expense will be uploaded later",
                        Toast.LENGTH_LONG).show();
            else if (result_arr[0] == '0' && result_arr[1] == '1')
                Toast.makeText(
                        context,
                        "Expense Successfully uploaded, DCR will be uploaded later",
                        Toast.LENGTH_LONG).show();
        }
        Log.d("msg", "Upload Complete");
    }
}

class JSON_Range {
    JSONObject value;
    String low, high;

    JSON_Range(JSONObject v, String l, String h) {
        value = v;
        low = l;
        high = h;
    }
}