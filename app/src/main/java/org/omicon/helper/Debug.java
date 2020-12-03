package org.omicon.helper;


import org.omicon.*;
import org.omicon.initial.Global;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.omicon.R;
import org.omicon.initial.HomeActivity;
import org.omicon.initial.logger;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;


public class Debug extends Activity {
    Context context;
    String user_no = "";
    private static final int FILE_SELECT_CODE = 0;
    private static final int FILE_EXECUTE_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_debug);
        //showDialogLocation();
        context = this;

        SharedPreferences prefsUser = getSharedPreferences("MY_PREFS", 0);
        user_no = prefsUser.getString("user_no", "");
        //Global.exceptionhandler(context);
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
            Intent intent = new Intent(Debug.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void executequery(View view) {

        EditText query = (EditText) findViewById(R.id.txtquery);
        ArrayList<ArrayList<String>> data = Global.dbObject.queryFromTable(query.getText().toString());
        TextView queryOutput = (TextView) findViewById(R.id.queryOutput);
        queryOutput.setText("");
        if (data.isEmpty())
            queryOutput.setText("No data" + "\n");
        else {
            for (int i = 0; i < data.size(); i++) {
                for (int j = 0; j < data.get(i).size(); j++)
                    queryOutput.setText(queryOutput.getText() + data.get(i).get(j) + ((j + 1 == data.get(i).size()) ? "" : "\t"));
                queryOutput.setText(queryOutput.getText() + "\n");
            }
        }
    }

    public void backupdb(View view) {
        dbBackup.exportDatabase(Global.dbName, context);
        dbBackup.exportPrefs(context);
    }

    public void runscript(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        //intent.addCategory(Intent.CATEGORY_OPENABLE);
        File folderPath = Environment.getExternalStorageDirectory();
        Uri myUri = Uri.parse(folderPath.getPath());
        intent.setDataAndType(myUri, "file/*");
        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a script to execute"),
                    FILE_EXECUTE_CODE);

        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void restoreDB(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //intent.setType("*/*");
        //intent.addCategory(Intent.CATEGORY_OPENABLE);
        File folderPath = Environment.getExternalStorageDirectory();
        Uri myUri = Uri.parse(folderPath.getPath());
        intent.setDataAndType(myUri, "file/*");
        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a db to restore"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public String getPath(Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri uri = data.getData();
        try {
            String path = getPath(uri);
            Log.d("file omicon", "File Path: " + path);
            switch (requestCode) {
                case FILE_SELECT_CODE:
                    if (resultCode == RESULT_OK) {
                        dbBackup.importDB(this, path);
                    }
                    break;
                case FILE_EXECUTE_CODE:
                    if (resultCode == RESULT_OK) {
                        dbBackup.runscript(this, path);
                    }
                    break;
            }
        } catch (Exception ex) {
            new logger(context).appendLog("exception in getting file " + ex.getMessage());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
