package org.omicon.helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Application;
import android.content.SharedPreferences;

import com.crashlytics.android.Crashlytics;

import org.acra.*;
import org.acra.annotation.*;
import org.omicon.R;
import org.omicon.initial.Global;

import io.fabric.sdk.android.Fabric;

/*@ReportsCrashes(formKey = "", customReportContent = { 
 ReportField.USER_EMAIL, ReportField.STACK_TRACE, ReportField.LOGCAT,ReportField.TOTAL_MEM_SIZE },
 mode = ReportingInteractionMode.TOAST,
 resToastText = R.string.crashtext,
 formUri = "http://172.16.24.229:6060/WS_FileUploads/UplaodCrashAcra")*/
@ReportsCrashes(formKey = "", mode = ReportingInteractionMode.TOAST, resToastText = R.string.crashtext,

// formUri = "http://172.16.24.229:6060/WS_FileUploads/UplaodCrashAcra"
        formUri = "http://ogcrashsrv.arobil.com:3000/logs/Omicon3_6")
public class OmiconApplication extends Application {
    @Override
    public void onCreate() {

        super.onCreate();
        Fabric.with(this, new Crashlytics());
        // SharedPreferences prefs = getSharedPreferences("MY_PREFS", 0);

        try {
            if (Global.AppCrashReportServiceEnable == 1) {

                /*ACRA.init(this);
                SimpleDateFormat sdf = new SimpleDateFormat(
                        "dd-MM-yyyy HH:mm:ss");
                String strDate = sdf.format(Calendar.getInstance().getTime());
                ACRA.getErrorReporter().putCustomData("User_date", strDate);
                SharedPreferences prefsUser = getSharedPreferences("MY_PREFS",
                        0);
                ACRA.getErrorReporter().putCustomData("User_no",
                        prefsUser.getString("user_no", ""));*/
            }
        } catch (Exception ex) {

        }
    }
}
