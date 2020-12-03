package org.omicon.helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.omicon.initial.Global;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class CustomExceptionHandler {

}
//public class CustomExceptionHandler implements UncaughtExceptionHandler {
//
//    private UncaughtExceptionHandler defaultUEH;
//
//    private String localPath;
//
//    Context context;
//    /* 
//     * if any of the parameters is null, the respective functionality 
//     * will not be used 
//     */
//    public CustomExceptionHandler(String localPath,Context cont) {
//        this.localPath = localPath;
//        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
//        context =cont;
//    }
//
//    public void uncaughtException(Thread t, Throwable e) {
//    	SimpleDateFormat s = new SimpleDateFormat("ddMMyyyy_hhmmss");
//    	String timestamp = s.format(new Date());
//        //String timestamp = TimestampFormatter.getInstance().getTimestamp();
//        final Writer result = new StringWriter();
//        final PrintWriter printWriter = new PrintWriter(result);
//        e.printStackTrace(printWriter);
//        
//        String stacktrace = "\t\t"+t.getClass().getName()+"\n";
//        stacktrace+="\t Line no "+t.getStackTrace()[4].getLineNumber()+" File name "+t.getStackTrace()[4].getFileName()+"\n";
//        stacktrace+="\t Line no "+t.getStackTrace()[3].getLineNumber()+" File name "+t.getStackTrace()[3].getFileName()+"\n";
//        stacktrace+="\t Line no "+t.getStackTrace()[5].getLineNumber()+" File name "+t.getStackTrace()[5].getFileName()+"\n";
//        stacktrace+="Message:: "+e.getMessage()+"\n";
//        stacktrace+= result.toString()+"\n";
//        		
//        
//        printWriter.close();
//        String filename = timestamp + ".stacktrace";
//
//        if (localPath != null) {
//            writeToFile(stacktrace, filename);
//        }
//        /*if (Global.crashreport != null) {
//            sendToServer( stacktrace);
//        }*/
//
//        defaultUEH.uncaughtException(t, e);
//    }
//
//    private void writeToFile(String stacktrace, String filename) {
//        try {
//            BufferedWriter bos = new BufferedWriter(new FileWriter(
//                    localPath + "/" + filename));
//            SharedPreferences prefs = context.getSharedPreferences("MY_PREFS", 0);
//            String user_name ="\t\t"+ prefs.getString("user_email", "")+"\n";
//            bos.write(user_name+stacktrace);
//            bos.flush();
//            bos.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void sendToServer( String filename) {
//        try {
//        	HttpClient client = new DefaultHttpClient();
//            HttpResponse response = null;
//
//            HttpPost post = new HttpPost(Global.crashreport);
//            
//
//            //StringEntity en = new StringEntity(json.toString());
//            
//            
//            File file = new File(localPath,filename);
//            FileEntity fileentity = new FileEntity(file, "UTF-8");
//
//            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
//            nvps.add(new BasicNameValuePair("filename", filename));
//            //nvps.add(new BasicNameValuePair("stacktrace", stacktrace));
//            
//            //en.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//            post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
//            
//            response =client.execute(post);
//            Log.e("error report", response.toString());
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}