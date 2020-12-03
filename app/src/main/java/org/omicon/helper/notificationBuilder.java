package org.omicon.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import org.omicon.R;


/**
 * Created by user on 9/23/14.
 */
public class notificationBuilder {
    Context context;
    int mId;

    public int getmId() {
        return mId;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }

    public notificationBuilder(Context context) {
        this.context = context;
    }

    public static void generateNotification(Context context, String titles, String message, Intent notificationIntent, boolean nonRemovable) {
        int icon = R.drawable.logo_sm;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        /*Notification notification = new Notification(icon, message, when);
        String title = titles;
        if (nonRemovable)
            notification.flags |= Notification.FLAG_NO_CLEAR;
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
        */

        //int notifyID = 1;
        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(context).setContentTitle(titles).setContentText(message);

// Start of a loop that processes data and then notifies the user

        mNotifyBuilder.setContentText(null).setNumber(1);
        // Because the ID remains unchanged, the existing notification is
        // updated.
        notificationManager.notify( 1, mNotifyBuilder.build());
    }

    public void createNotification(String title, String text, Class resIntent) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(title)
                        .setContentText(text);
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, resIntent.getClass());

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(resIntent.getClass());
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());

    }
}
