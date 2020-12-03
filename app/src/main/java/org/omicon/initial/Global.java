package org.omicon.initial;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class Global {

    public static DataBase dbObject = null;
    public static int dbVersion = 1;
    public static String dbName = "og_mobile.db";
    public static String GLOBAL_USER_NO = "";
    public static String APP_VERSION = "3.7";
    public static int detailChunk = 30;
    public static int extra_perc = 20;
    public static int run_freq_location = 1000 * 60 * 10;
    public static ArrayList<String> locationList = new ArrayList<String>();

    //public static String wslink = "http://172.16.24.229:8080/omicon_ws/omicon_endpoint";
    public static String wslink = "ws://ogrpcsrv.arobil.com:8080/omicon_ws-1.0/omicon_endpoint";
    public static String link = "http://ogdcr1.arobil.com:6060/";
    //public static String link = "http://192.168.101.104:6060/";
    public static String loginLink = link + "WS_SEC_USERS/Login";
    public static String DownloadAck = link + "WS_DownloadApi/set_downloaded?DOWN_NO=";
    public static String logoutLink = link + "WS_SEC_USERS/Logout?user_name=";
    public static String chgPasswdlink = link + "WS_SEC_USERS/ChangePassword";
    public static String downloadLink = link
            + "WS_DownloadApi/DownloadData_4_0?DEVICE_ID=";
    public static String uploadLink = link + "WS_UploadApi/";
    public static String uploadMovement = link + "WS_UploadApi/Upload_Movement";
    public static String last_download_time = "last_download_time";
    public final static String crashreport = link
            + "WS_FileUploads/UploadCrashReports2";
    public static String dbupload = link + "WS_FileUploads/UploadDB";
    private static SimpleDateFormat sdfDateFormatter = new SimpleDateFormat(
            "dd/MM/yyyy");
    private static SimpleDateFormat sdfTimeFormatter = new SimpleDateFormat(
            "hh:mm a");
    private static SimpleDateFormat sdfOfflineDateTime = new SimpleDateFormat(
            "yyyy-MM-dd hh:mm:ss a");
    public static int ApplciationControllSocketServiceEnable = 1;
    public static int AppCrashReportServiceEnable = 1;
    private static SharedPreferences primaryPrefs;
    private static Editor primaryEditor;
    private static String primaryKey = "PRIMARY_SEQUENCE";
    public static long currentSessionStart;
    public static int toUploadCount = -1;
    public static double timeDuration_sync = 10.0;
    public static Location last_location = null;
    public static int is_changing_arraylist = 0;

    // public static void exceptionhandler(Context context) {
    // File sd = new File(
    // Environment
    // .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
    // "omicon");
    // if (!sd.exists())
    // sd.mkdirs();
    //
    // if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof
    // CustomExceptionHandler)) {
    // Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
    // sd.getAbsolutePath(), context));
    // }
    // }
    public static boolean isMyServiceRunning(Class<?> serviceClass, Activity act) {
        ActivityManager manager = (ActivityManager) act.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void initToUploadCount() {

        // Upload DCR Data
        // Check entry state
        ArrayList<String[]> masterDataList_dcr = Global.dbObject
                .queryFromTable("TRN_DCR", null, "IS_UPLOADED = 0  ");
        ArrayList<String[]> masterDataList_expense = Global.dbObject
                .queryFromTable("TRN_EXPENSE", null, "IS_UPLOADED = 0  ");
        ArrayList<String[]> masterDataList_det = Global.dbObject
                .queryFromTable("TRN_DCR_DET", null, "IS_UPLOADED = 0  ");

        ArrayList<String[]> masterDataList_det2 = Global.dbObject
                .queryFromTable("TRN_EXPENSE_DET", null, "IS_UPLOADED = 0  ");
        toUploadCount = (masterDataList_dcr.size()
                + masterDataList_expense.size() + masterDataList_det.size())
                + masterDataList_det2.size();
    }

    public static void initialConfig(Context con) {
        // here Data Base Object Is created
        Fabric.with(con, new Crashlytics());
        dbObject = DataBase.createDataBase(con, dbName, dbVersion);

        // Here Register for Android Location Service And listener set
        CurrentLocation.ActivateLocationService(con);

        // set User NO
        SharedPreferences prefs = con.getSharedPreferences("MY_PREFS", 0);
        GLOBAL_USER_NO = prefs.getString("user_no", "");

        // More Table Added
        addNewTable();
        runchangeDates(prefs);
        // Config Shared Preferences for Primary Key Sequence
        primaryPrefs = con.getSharedPreferences("PRIMARY_SEQ_PREFS", 0);
        primaryEditor = primaryPrefs.edit();

        // Time Zone
        /*
		 * sdfDateFormatter.setTimeZone(TimeZone.getTimeZone("Asia/Dhaka"));
		 * sdfTimeFormatter.setTimeZone(TimeZone.getTimeZone("Asia/Dhaka"));
		 * sdfOfflineDateTime.setTimeZone(TimeZone.getTimeZone("Asia/Dhaka"));
		 */
    }

    public static String currentDate() {
        Calendar cal = Calendar.getInstance();
        return sdfDateFormatter.format(new Date(cal.getTimeInMillis()));
    }

    public static String currentTime() {
        Calendar cal = Calendar.getInstance();
        return sdfTimeFormatter.format(new Date(cal.getTimeInMillis()));
    }

    public static String getDateValue(long timeInMills) {
        return sdfDateFormatter.format(new Date(timeInMills));
    }

    public static String getTimeValue(long timeInMills) {
        return sdfTimeFormatter.format(new Date(timeInMills));
    }

    public static String getDateWithTime(String time_value) {
        try {
            return sdfOfflineDateTime.format(Long.parseLong(time_value));
        } catch (Exception ex) {
            return "";
        }
    }

    public static Location currentLocation() {
        //Global.last_location = CurrentLocation.location;
        return CurrentLocation.location;
    }

    public static boolean dateValidation(int day, int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, 11, 11);
        long time = cal.getTimeInMillis();
        cal.set(Calendar.getInstance().get(Calendar.YEAR), Calendar
                        .getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH), 11, 11);
        if (time <= cal.getTimeInMillis())
            return true;
        else
            return false;
    }

    public static long timeValueFromString(String date, String time) {
        // date will be in dd/mm/yyyy
        // time must be in hh:mm a format

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        Date dd = null;
        try {
            dd = formatter.parse(date + " " + time);
        } catch (Exception e) {
            Log.d("msg", "Time Calculation Error");
        }
        if (dd != null)
            return dd.getTime();
        else
            return Calendar.getInstance().getTimeInMillis();
    }

    public static long getPrimaryKeyID() {
        return primaryPrefs.getLong(primaryKey, -1);
    }

    public static void setPrimaryKeyID(long id) {
        primaryEditor.putLong(primaryKey, id);
        primaryEditor.commit();
    }

    public static void resetPrimarySeq() {
        String table_list[] = {"TRN_DCR", "TRN_EXPENSE", "TRN_USER_LOCATION"};
        String table_column[] = {"OFFLINE_DCR_NO", "OFFLINE_EXP_NO",
                "OFFLINE_LOC_NO"};
        long maxID = -1, tmp;
        for (int i = 0; i < table_list.length; i++) {
            tmp = Global.dbObject.GetNextRowID(table_list[i], table_column[i]);
            if (maxID < tmp)
                maxID = tmp;
        }
        if (maxID > getPrimaryKeyID())
            setPrimaryKeyID(maxID);
    }

    public static String CapitalizeWords(String text) {
        String resultStr = "";
        try {
            String[] list_str = text.split(" ");
            for (String str : list_str) {
                str = str.trim();
                if (!str.matches("")) {
                    str = str.toLowerCase();
                    str = Character.toUpperCase(str.charAt(0))
                            + str.substring(1);
                    resultStr += str + " ";
                }
            }
            return resultStr;
        } catch (Exception ex) {
            return text;
        }
    }

    public static boolean compareTimeValue(String t1, String t2) {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
        try {
            Date d1 = format.parse(t1), d2 = format.parse(t2);
            return d1.getTime() >= d2.getTime();
        } catch (Exception e) {
            Log.d("msg", "Time Comparision Error");
            return true;
        }
    }

    public static ArrayList<String> getNearestLocation(Location location,
                                                       int distance) {
        ArrayList<String> list = new ArrayList<String>();
        ArrayList<String[]> allLocation = Global.dbObject.queryFromTable(
                "TRN_USER_LOCATION", null, null);
        for (int i = 0; i < allLocation.size(); i++) {
            Location locB = new Location("B");
            locB.setLongitude(Double.parseDouble(allLocation.get(i)[3]));
            locB.setLatitude(Double.parseDouble(allLocation.get(i)[2]));
            if (location.distanceTo(locB) < distance)
                list.add(allLocation.get(i)[4]);
        }
        return list;
    }

    public static void addNewTable() {
        String query;
        if (dbObject != null) {
            // Add TABLE SET_LOGOUT_TYPE IN v2.1
            if (!dbObject.isTableExist("SET_LOGOUT_TYPE")) {
                query = "CREATE TABLE SET_LOGOUT_TYPE("
                        + "LOGOUT_TYPE_NO INTEGER NOT NULL ,"
                        + "LOGOUT_TYPE_NAME TEXT(200),"
                        + "PRIMARY KEY(LOGOUT_TYPE_NO)" + ");";
                dbObject.executeRawQuery(query);
            }
            // Add TABLE TRN_LOG_INFORMATION IN v2.1
            if (!dbObject.isTableExist("TRN_LOG_INFO")) {
                query = "CREATE TABLE TRN_LOG_INFO(" + "LOG_INFO_NO INTEGER,"
                        + "LOGOUT_TYPE_NO INTEGER," + "USER_NO TEXT(100),"
                        + "LOG_IN_LOCATION_NAME TEXT(500),"
                        + "LOG_IN_LAT REAL," + "LOG_IN_LONG REAL,"
                        + "LOG_IN_TIME TEXT(200),"
                        + "LOG_OUT_LOCATION_NAME TEXT(500),"
                        + "LOG_OUT_LAT REAL," + "LOG_OUT_LONG REAL,"
                        + "LOG_OUT_TIME TEXT(200),"
                        + "LOG_OUT_MESSAGE TEXT(1000),"
                        + "PRIMARY KEY(LOG_INFO_NO)" + ");";
                dbObject.executeRawQuery(query);
            }

            // Add TABLE TRN_MOVEMENT IN v3.3
            if (!dbObject.isTableExist("TRN_MOVEMENT")) {
                query = "CREATE TABLE TRN_MOVEMENT("
                        + "MOVEMENT_ID INTEGER PRIMARY KEY NOT NULL,"
                        + "MOVE_DATE TEXT(50)," + "MOVE_TIME TEXT(50),"
                        + "LON_VAL TEXT(50)," + "LAT_VAL TEXT(50),"
                        + "BATT_PCT TEXT(20)," + "USER_NO TEXT(50)" + ");";
                dbObject.executeRawQuery(query);
            }

            // Add INSERT_TIME COLUMN IN THE TRN_MSG IN v2.1
            /*dbObject.executeRawQuery("ALTER TABLE TRN_MSG ADD COLUMN INSERT_TIME TEXT(100);");
            dbObject.executeRawQuery("ALTER TABLE TRN_DCR_DET ADD COLUMN BEHALF_NICK TEXT(100);");
            dbObject.executeRawQuery("ALTER TABLE TRN_DCR_DET ADD COLUMN TEACHER_NICK TEXT(100);");*/

        }
    }

    public static void runchangeDates(SharedPreferences prefs) {
        int change_dates = prefs.getInt("change_dates", 1);
        if (change_dates == 1) {
            ArrayList<String[]> dcrMasterDataList = Global.dbObject
                    .rawqueryFromDatabase("SELECT * FROM TRN_DCR WHERE IS_UPLOADED=0 ORDER BY OFFLINE_DCR_NO;");
            if (dcrMasterDataList.size() != 0) {
                for (String[] dcrMasterItemRow : dcrMasterDataList) {
                    String[] date_spilit = dcrMasterItemRow[18].split("/");
                    if (date_spilit.length == 2) {
                        try {

//							str =date_spilit[0].charAt(0)+""+
//									+ date_spilit[0].charAt(1) + "/"
//									+ date_spilit[0].charAt(2)
//									+ date_spilit[0].charAt(3)+ "/" + date_spilit[1];
                            String str = date_spilit[0].substring(0, 2)
                                    + "/"
                                    + date_spilit[0].substring(2, date_spilit[0].length()) +
                                    "/" + date_spilit[1];
                            //String tmp2= date_spilit[0].charAt(0)+"-"+date_spilit[0].charAt(1);

                            Log.e("replaced date ", str);
                            dbObject.executeRawQuery("update trn_dcr set TRN_DCR_DATE ='"
                                    + str.toString()
                                    + "' where offline_dcr_no = "
                                    + dcrMasterItemRow[0]);
                        } catch (Exception ex) {
                            Log.e("updating error", dcrMasterItemRow[0]);
                        }
                    }
                }
                dcrMasterDataList = Global.dbObject
                        .rawqueryFromDatabase("select * from TRN_EXPENSE where is_uploaded =0 order by offline_exp_no;");
                if (dcrMasterDataList.size() != 0) {
                    for (String[] expMasterItem : dcrMasterDataList) {
                        String[] date_spilit = expMasterItem[2].split("/");
                        if (date_spilit.length == 2) {
                            try {
                                String str = date_spilit[0].substring(0, 2)
                                        + "/"
                                        + date_spilit[0].substring(2, date_spilit[0].length()) +
                                        "/" + date_spilit[1];
                                Log.e("replaced date ", str);
                                dbObject.executeRawQuery("update trn_dcr set TRN_DCR_DATE ='"
                                        + str.toString()
                                        + "' where offline_dcr_no = "
                                        + expMasterItem[0]);
                            } catch (Exception ex) {
                                Log.e("updating error", expMasterItem[0]);
                            }
                        }
                    }
                }
            }
            prefs.edit().putInt("change_dates", 0).apply();
        }
    }
	/*
	 * public static void insertNewLocation(Location location,String
	 * location_name){ ContentValues values=new ContentValues();
	 * values.put("OFFLINE_LOC_NO"
	 * ,Global.dbObject.getLastRowID("TRN_USER_LOCATION", "OFFLINE_LOC_NO")+1);
	 * values
	 * .put("ACTION_OFFLINE_TIME",Calendar.getInstance().getTimeInMillis());
	 * values.put("LAT_VAL",location.getLatitude());
	 * values.put("LON_VAL",location.getLongitude());
	 * values.put("LOCATION_NAME",location_name);
	 * values.put("USER_NO",GLOBAL_USER_NO);
	 * 
	 * Global.dbObject.insertIntoTable("TRN_USER_LOCATION", values); }
	 */
}
