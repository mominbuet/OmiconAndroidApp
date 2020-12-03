package org.omicon.initial;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by user on 9/3/14.
 */
public class logger {
    Context context;

    public logger(Context context) {
        this.context = context;
    }

    public boolean create_dir() {
        File fl = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "omicon");
        return fl.mkdirs();
    }

    public void appendLog(String text) {
        /*
		 * if(!text.isEmpty()) return; else{
		 */
        // try{
        // FileOutputStream fos= context.openFileOutput("omicon.dat",
        // Context.MODE_APPEND);
        File logFile = (Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
        boolean success = true;
        File last = null;
        // Log.e("Folder creation1111", (success ? "yes" :
        // "no")+logFile.getPath());

		/*
		 * try{if (!logFile.exists()) { try { success=logFile.mkdirs();
		 * Log.e("Folder creation", (success ? "yes" : "no")+logFile.getPath());
		 * if(success){ logFile = new File(logFile,"logg.dat");
		 * Log.e("folder Created",logFile.getPath());
		 * if(logFile.createNewFile())
		 * Log.e("folder not create",Environment.getExternalStorageDirectory
		 * ().toString()); } } catch (IOException e) {
		 * Log.e("error in catch1",logFile.getPath()); e.printStackTrace(); } }
		 * else{ success=true;
		 */
        logFile.mkdirs();
        Calendar c = Calendar.getInstance();
        boolean check = true;
        SimpleDateFormat sdf = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss a");
        String strDate = sdf.format(c.getTime());
        try {
            last = new File(logFile, "omicon_log.dat");
            // last.mkdirs();
            // Log.e("folder Created",logFile.getPath());
            if (!last.exists()) {
                if (!last.createNewFile())
                    Log.e("file not create", logFile.getPath().toString());
            }
        } catch (IOException e) {
            Log.e("error in catch2", logFile.getPath());
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {

            if (success) {
                // BufferedWriter for performance, true to set append to file
                // flag
                long diffDays = (System.currentTimeMillis() - new Date(
                        last.lastModified()).getTime())
                        / (24 * 60 * 60 * 1000);
                BufferedWriter buf = new BufferedWriter(new FileWriter(last,
                        ((diffDays > 0) ? false : true)));

                buf.append(strDate + ": " + text);
                buf.newLine();
                buf.close();
            } else {

                String towrite = strDate + ": " + text;
				/*
				 * fos.write(towrite.getBytes()); fos.close();
				 */
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		/*
		 * } catch (FileNotFoundException ex){
		 * 
		 * }
		 */

    }

    public void appendBiggerLog(String text) {
        // try{
        // FileOutputStream fos= context.openFileOutput("omicon.dat",
        // Context.MODE_APPEND);
		/*
		 * if(!text.isEmpty()) return; else{
		 */
        File logFile = (Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
        boolean success = true;
        File last = null;
        // Log.e("Folder creation1111", (success ? "yes" :
        // "no")+logFile.getPath());

		/*
		 * try{if (!logFile.exists()) { try { success=logFile.mkdirs();
		 * Log.e("Folder creation", (success ? "yes" : "no")+logFile.getPath());
		 * if(success){ logFile = new File(logFile,"logg.dat");
		 * Log.e("folder Created",logFile.getPath());
		 * if(logFile.createNewFile())
		 * Log.e("folder not create",Environment.getExternalStorageDirectory
		 * ().toString()); } } catch (IOException e) {
		 * Log.e("error in catch1",logFile.getPath()); e.printStackTrace(); } }
		 * else{ success=true;
		 */
        logFile.mkdirs();

        try {
            last = new File(logFile, "omicon_dcr_log.dat");
            // last.mkdirs();
            // Log.e("folder Created",logFile.getPath());
            if (!last.exists())
                if (!last.createNewFile())
                    Log.e("file not create", logFile.getPath().toString());
        } catch (IOException e) {
            Log.e("error in catch2", logFile.getPath());
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss a");
            String strDate = sdf.format(c.getTime());
            if (success) {
                // BufferedWriter for performance, true to set append to file
                // flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(last,
                        true));
                buf.append(strDate + ": " + text);
                buf.newLine();
                buf.close();
            } else {

                String towrite = strDate + ": " + text;
				/*
				 * fos.write(towrite.getBytes()); fos.close();
				 */
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		/*
		 * } catch (FileNotFoundException ex){
		 * 
		 * }
		 */
        // }
    }
}
