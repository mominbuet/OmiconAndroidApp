package org.omicon.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.omicon.initial.Global;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by user on 9/29/14.
 */
public class dbBackup {
    // importing database.
    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static String exportLog(Context context) throws IOException {
        File logFile2 = (Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
        File logFile = new File(logFile2, "omicon_log.dat").getAbsoluteFile();

        SharedPreferences prefs = context.getSharedPreferences("MY_PREFS", 0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_HH_mm");
        String strDate = sdf.format(Calendar.getInstance().getTime());
        String backupLogPath = prefs.getString("user_email", "")
                + "_backup_" + strDate + ".dat";
        File outFile = new File(logFile2, backupLogPath);
        dbBackup.copy(logFile, outFile);
        return outFile.getAbsolutePath();
    }

    public static void runscript(Context cont, String restorePath) {
        try {
            File currentScript = new File(restorePath);
            StringBuilder text = new StringBuilder();

            try {
                BufferedReader br = new BufferedReader(new FileReader(
                        currentScript));
                String line;

                while ((line = br.readLine()) != null) {
                    Global.dbObject.executeRawQuery(line);
                }
            } catch (IOException e) {
                Toast.makeText(cont, e.toString(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(cont, ex.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public static void importDB(Context cont, String restorePath) {
        // TODO Auto-generated method stub

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + "org.omicon"
                        + "//databases//" + Global.dbName;
                String backupDBPath = restorePath;
                File backupDB = new File(data, currentDBPath);
                File currentDB = new File(restorePath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(cont, backupDB.toString(), Toast.LENGTH_LONG)
                        .show();

            }
        } catch (Exception e) {

            Toast.makeText(cont, e.toString(), Toast.LENGTH_LONG).show();

        }
    }

    public static String exportDatabase(String databaseName, Context cont) {

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_HH_mm");
            String strDate = sdf.format(c.getTime());
            if (sd.canWrite()) {
                String currentDBPath = "data//" + "org.omicon"
                        + "//databases//" + databaseName + "";
                SharedPreferences prefs = cont.getSharedPreferences("MY_PREFS",
                        0);
                String backupDBPath = prefs.getString("user_email", "")
                        + "_backup_" + strDate + ".db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB)
                            .getChannel();
                    FileChannel dst = new FileOutputStream(backupDB)
                            .getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    return backupDB.getAbsolutePath();
                }

            }
        } catch (Exception e) {
            Log.e("error in backing up", e.getMessage());
        }
        return "";
    }

    public static String exportPrefs(Context cont) {

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_HH_mm");
            String strDate = sdf.format(c.getTime());
            if (sd.canWrite()) {
                SharedPreferences prefs = cont.getSharedPreferences("MY_PREFS",
                        0);
                String currentDBPath = "data//org.omicon//shared_prefs//MY_PREFS.xml";

                String backupDBPath = prefs.getString("user_email", "")
                        + "_backup_" + strDate + ".xml";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB)
                            .getChannel();
                    FileChannel dst = new FileOutputStream(backupDB)
                            .getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    return backupDB.getAbsolutePath();
                }

            }
        } catch (Exception e) {
            Log.e("error in backing up", e.getMessage());
        }
        return "";
    }
}
