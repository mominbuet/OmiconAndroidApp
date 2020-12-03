package org.omicon.helper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.omicon.initial.Global;
import org.omicon.initial.logger;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

public class upload_file {
    public static void upload_stacktraces(Context context) {
        File fl = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "omicon");
        boolean res = false;
        if (!fl.exists()) {
            res = new logger(context).create_dir();
        } else {
            File[] matchingFiles = fl.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File pathname, String name) {
                    return name.endsWith("stacktrace");
                }
            });
            for (File tmp : matchingFiles)
                if (Global.crashreport != null) {
                    if (sendToServer(tmp, "text/plain", Global.crashreport)) {
                        tmp.delete();
                    }
                }
        }

    }

    public static void Upload_database(Context context, String filepath) {
        File fl = new File(filepath);

        try {

            if (Global.dbupload != null) {
                if (sendDBServer(fl, "application/octet-stream",
                        Global.dbupload)) {
                    if (!filepath.contains(".dat"))
                        fl.delete();
                }
            }

        } catch (Exception e) {
            Log.e("file match err ", e.getMessage());
        }
    }

    private static boolean sendToServer(File fl, String mimetype, String url) {
        String dta = "";
        boolean ret = false;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = null;

            HttpPost post = new HttpPost(url);

            // StringEntity en = new StringEntity(json.toString());

            // File file = fl;
            // FileEntity fileentity = new FileEntity(file, "UTF-8");

			/*
             * List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			 * 
			 * // nvps.add(new BasicNameValuePair("stacktrace", stacktrace));
			 * InputStreamEntity reqEntity = new InputStreamEntity( new
			 * FileInputStream(fl), -1); reqEntity.setContentType("text/plain");
			 * reqEntity.setChunked(true); post.setEntity(reqEntity);
			 */

            post.setEntity(new FileEntity(fl, mimetype));
            // en.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
            // "application/json"));
            // nvps.add(new BasicNameValuePair("file",fl.));
            // nvps.add(new File("file", fl.getName()));

            // post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

            response = client.execute(post);
            Log.e("error report", response.toString());
            if (response != null) {
                InputStream in = response.getEntity().getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    dta += line;
                }
                Log.e("msg from server", "message: " + dta);
                if (dta.contains("was here"))
                    ret = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static byte[] convertTextFileToByteArray(File file) {
        FileInputStream fileInputStream = null;
        byte[] bFile = new byte[(int) file.length()];
        try {
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                fileInputStream = null;
            }
        }
        return bFile;
    }

    private static boolean sendDBServer(File fl, String mimetype, String url) {
        String dta = "";
        boolean ret = false;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = null;

            HttpPost post = new HttpPost(url);
			/*
			 * post.addHeader("Connection", "Keep-Alive");
			 * post.addHeader("Content-Type",
			 * "application/x-www-form-urlencoded"); String fileAsBase64 =
			 * Base64.encodeToString( upload_file.convertTextFileToByteArray(fl)
			 * , Base64.DEFAULT);
			 */
            MultipartEntity reqEntity = new MultipartEntity(
                    HttpMultipartMode.BROWSER_COMPATIBLE);
            FileBody cbFile = new FileBody(fl, "application/x-sqlite3");
            reqEntity.addPart("file", cbFile);

			/*
			 * List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			 * nvps.add(new BasicNameValuePair("file", fileAsBase64));
			 * post.setEntity(new UrlEncodedFormEntity(nvps));
			 */
            post.setEntity(reqEntity);
            response = client.execute(post);
            Log.e("error report", response.toString());
            if (response != null) {
                InputStream in = response.getEntity().getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    dta += line;
                }
                Log.e("msg from server", "message: " + dta);
                if (dta.contains("was here"))
                    ret = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
