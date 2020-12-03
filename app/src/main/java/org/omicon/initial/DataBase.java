package org.omicon.initial;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class DataBase {
    private String DATA_BASE_NAME;
    private int DATA_BASE_VERSION;
    private Context context;

    private static DataBase databaseHandler = null;
    private DataBaseHelper myDbHelper = null;
    private SQLiteDatabase myDatase = null;
    private Map<String, String> table_query_list;

    private DataBase() {
        table_query_list = new HashMap<String, String>();
    }

    public void init(Context con, String db_name, int db_version) {
        context = con;
        DATA_BASE_NAME = db_name;
        DATA_BASE_VERSION = db_version;
        myDbHelper = new DataBaseHelper();
        openMyDB();
    }

    public void openMyDB() {
        myDatase = myDbHelper.getWritableDatabase();
        Log.d("msg_db", "Successfully Databse Opened");
    }

    public void closeMyDB() {
        if (myDatase != null)
            myDatase.close();
        Log.d("msg_db", "DataBase Now Closed");
    }

    public long getLastRowID(String table_name, String column) {
        long last_id = 0;
        String sql = "Select max(" + column + ") from " + table_name;
        Cursor cursor = myDatase.rawQuery(sql, null);
        if (cursor.moveToFirst())
            last_id = cursor.getLong(0);

        if(cursor != null){
            cursor.close();
        }
        return last_id;
    }

    public boolean isTableExist(String tbl_name) {
        if (!myDatase.isOpen())
            openMyDB();
        Cursor cursor = myDatase.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tbl_name + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public void executeRawQuery(String query) {
        if (!myDatase.isOpen())
            openMyDB();
        try {
            myDatase.execSQL(query);
        } catch (Exception ex) {
            new logger(context).appendLog("NOT EXECUTED QUERY:" + query);
            Log.d("msg", "NOT EXECUTED QUERY:" + query);
        }

    }

    public void addTable(String table_name, String table_query) {
        if (myDatase.isOpen()) {
            table_query_list.put(table_name, table_query);
            myDatase.execSQL(table_query);
        } else
            Log.d("msg_db", "Databse Not Opened");

    }

    public int updateIntoTable(String table_name, ContentValues values, String query) {
        int num = 0;
        if (myDatase.isOpen()) {
            num = myDatase.update(table_name, values, query, null);
            if (num > 0)
                Log.d("msg_db", "Number of " + num + " Rows updated From " + table_name);
            else {
                Log.d("msg_db", "No Rows updated");
                new logger(context).appendLog("No Rows updated in updateIntoTable : " + table_name);
            }
        } else {
            new logger(context).appendLog("Databse Not Opened in updateIntoTable : " + table_name);
        }

        return num;
    }

    public void deleteTable(String table_name) {
        if (myDatase.isOpen()) {
            String query = "DROP TABLE IF EXISTS " + table_name;
            myDatase.execSQL(query);
            table_query_list.remove(table_name);
        } else
            Log.d("msg_db", "Databse Not Opened");
    }

    public long insertIntoTable(String table_name, ContentValues value) {
        long rowID = -1;
        if (myDatase.isOpen())
        {
            rowID = myDatase.insert(table_name, null, value);
            Log.d("roeid",String.valueOf(rowID));
        }

        else {
            Log.d("msg_db", "Databse Not Opened");
            new logger(context).appendLog("Databse Not Opened in insertIntoTable : " + table_name);
        }
        return rowID;
    }

    // Here query is after of where clause in sql statement
    public void deleteFromTable(String table_name, String query) {
        if (myDatase.isOpen()) {
            int num = myDatase.delete(table_name, query, null);
            if (num > 0)
                Log.d("msg_db delete ", "Number of " + num + " Rows deleted From " + table_name);
            else
                Log.d("msg_db delete ", "No Rows Deleted");
        }
    }

    public ArrayList<ArrayList<String>> queryFromTable(String query) {
        ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();


        if (myDatase.isOpen()) {
            if (query.toLowerCase().contains("select")) {
                Cursor cursor = myDatase.rawQuery(query, null);
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    ArrayList<String> row = new ArrayList<String>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        row.add(cursor.getString(i));
                    }
                    cursor.moveToNext();
                    ret.add(row);
                }
                cursor.close();
            } else
                myDatase.execSQL(query);
        }
        return ret;
    }
    /*public void updateIntoTable(String table_name,ContentValues values,String query)
    {
		if(myDatase.isOpen())
		{			
			int num=myDatase.update(table_name, values, query, null);
			if(num > 0)
				Log.d("msg_db", "Number of "+num+" Rows updated From "+table_name);
			else
				Log.d("msg_db", "No Rows Deleted");
		}
	}*/

    // this for query to one table
    public ArrayList<String[]> queryFromTable(String table_name, String[] columns, String whereClause) {
        ArrayList<String[]> allData = new ArrayList<String[]>();
        String[] dataItem;

        if (myDatase.isOpen()) {
            Cursor data = myDatase.query(table_name, columns, whereClause, null, null, null, null);
            data.moveToFirst();
            while (!data.isAfterLast()) {
                if (columns == null)
                    dataItem = new String[data.getColumnCount()];
                else
                    dataItem = new String[columns.length];

                for (int i = 0; i < dataItem.length; i++) {
                    if (columns == null)
                        dataItem[i] = data.getString(i);
                    else
                        dataItem[i] = data.getString(data.getColumnIndex(columns[i]));


                }

                data.moveToNext();
                allData.add(dataItem);

            }

            if(data != null){
                data.close();
            }

            Log.i("msg_db", "Successfully Read From DataBase");
        } else
            Log.i("msg_db", "Database Not Opened");

        return allData;
    }

    // this is raw_query to Read
    public ArrayList<String[]> rawqueryFromDatabase(String query) {
        ArrayList<String[]> allData = new ArrayList<String[]>();
        String[] dataItem;
        if (myDatase.isOpen()) {
            Cursor data = myDatase.rawQuery(query, null);
            data.moveToFirst();
            while (!data.isAfterLast()) {
                dataItem = new String[data.getColumnCount()];
                for (int i = 0; i < dataItem.length; i++)
                    dataItem[i] = data.getString(i);
                allData.add(dataItem);
                data.moveToNext();
            }
            Log.d("msg_db", "Successfully Read From DataBase");
        } else
            Log.d("msg_db", "Database Not Opened");
        return allData;
    }

    public int GetNextRowID(String tableName, String columnName) {
        String query = "SELECT MAX(" + columnName + ") AS maxid FROM " + tableName;
        int nextId = 0;
        Cursor queryCursor = myDatase.rawQuery(query, null);
        if (queryCursor.moveToFirst()) {
            nextId = queryCursor.getInt(0);
        }

        nextId++;

        if(queryCursor != null){
            queryCursor.close();
        }
        return nextId;
    }

    public static DataBase createDataBase(Context con, String db_name, int db_version) {
        if (databaseHandler != null)
            return databaseHandler;
        databaseHandler = new DataBase();
        databaseHandler.init(con, db_name, db_version);
        return databaseHandler;
    }

    private class DataBaseHelper extends SQLiteOpenHelper {

        //private String DB_PATH;
        private String query_string = "";

        public DataBaseHelper() {
            super(context, DATA_BASE_NAME, null, DATA_BASE_VERSION);
            /*if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1)
                DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
			else            
				DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";*/
        }

        private void copyDataBaseFromAsset() {
            try {
                query_string = "";
                InputStream is = context.getAssets().open("og_mobile_db.sql");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null)
                    query_string += line;
                Log.d("msg_db", "Successfully Created");
            } catch (Exception ex) {
                Log.d("msg_db", "Error in creating database from Asset Folder:" + ex.getMessage());
            }
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            //Fabric.with(this, new Crashlytics());
            if (table_query_list.size() != 0) {
                String[] tables = (String[]) table_query_list.keySet().toArray();
                for (int i = 0; i < tables.length; i++)
                    db.execSQL(table_query_list.get(tables[i]));
            }

            // call  copyDataBaseFromAsset to create Database
            copyDataBaseFromAsset();
            for (String each_query : query_string.split(";")) {
                Log.d("msg", each_query);
                db.execSQL(each_query);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(DataBaseHelper.class.getName(),
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data");
            String[] tables = (String[]) table_query_list.keySet().toArray();

            for (int i = 0; i < tables.length; i++)
                db.execSQL("DROP TABLE IF EXISTS " + tables[i]);
            onCreate(db);

        }

    }
}
