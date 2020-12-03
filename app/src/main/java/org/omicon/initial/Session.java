package org.omicon.initial;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class Session {
    SharedPreferences prefs;
    Editor editor;
    Context context;
    int PRIVATE_MODE = 0;
    String KEY_EMAIL = "user_email", KEY_PASS = "user_pass", KEY_SESSIONID = "session_id", KEY_ALLUSERS = "users_list", IS_LOGGED_IN = "log_in";
    String KEY_USER_NO = "user_no";
    String KEY_REMIND_ME_TIME="remind_time";
    String KEY_USER_NAME="user_name";
    public Session(Context con) {
        context = con;
        prefs = con.getSharedPreferences("MY_PREFS", PRIVATE_MODE);
        editor = prefs.edit();
    }

    public void login(String email, String pass, String sessionID, String userNo,String userName) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String remindTime = formatter.format(new Date());
        editor.putString(KEY_REMIND_ME_TIME,remindTime);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASS, pass);
        editor.putString(KEY_SESSIONID, sessionID);
        editor.putString(KEY_USER_NO, userNo);

        editor.putString(KEY_USER_NAME, userName);
        editor.commit();
        Log.d("while_login",remindTime);
    }

    public void remindUser(String email, String pass) {
        String users = prefs.getString(KEY_ALLUSERS, null);

        if (users == null)
            users = email;
        else {
            String[] userList = users.split(";");
            boolean flag = false;
            for (String u : userList)
                if (u.matches(email)) {
                    flag = true;
                    break;
                }
            if (!flag) {
                users += (";" + email);
            }
        }
        /*
         SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");


                        String str1 =dateFrom.getText().toString();
                        Date date1 = formatter.parse(str1);

                        String str2 = dateTo.getText().toString();
                        Date date2 = formatter.parse(str2);

                        if (date1.compareTo(date2)>0)
         */
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String remindTime = formatter.format(new Date());
        editor.putString(KEY_REMIND_ME_TIME,remindTime);
        editor.putString(IS_LOGGED_IN, "true");
        editor.putString(email, pass);
        editor.putString(KEY_ALLUSERS, users);

        editor.commit();
    }

    public ArrayList<User> getAllRemindUsers() {
        ArrayList<User> ulist = new ArrayList<User>();
        /*String users=prefs.getString(KEY_ALLUSERS, null);
		if(users != null)
		{
			String []userList=users.split(";");
			for(String u:userList)
				ulist.add(new User(u, prefs.getString(u,"")));
		}*/
        return ulist;
    }

    public void removeSession() {
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_PASS);
        editor.remove(IS_LOGGED_IN);
        editor.remove(KEY_SESSIONID);
        editor.remove(KEY_USER_NO);
        editor.remove(KEY_REMIND_ME_TIME);
        editor.remove(KEY_USER_NAME);
        editor.commit();
    }

    public boolean loggedStatus() {
        String b = prefs.getString(IS_LOGGED_IN, "false");

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String TimeNow = formatter.format(new Date());
        String remind_time=prefs.getString("remind_time","");

        Date date1 = null;
        Date date2=null;
        try {
            date1 = formatter.parse(TimeNow);
            date2 = formatter.parse(remind_time);
            Log.d("remind_time",remind_time);
            Log.d("nowtime",TimeNow);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(date1==null || date2==null)
        {
            return false;
        }
        else if ((date1.compareTo(date2)==0 && b.matches("true")))
        {

                return true;
        }
        else
        {
            return false;
        }


    }

    public void removeCookies() {
        editor.clear();
        editor.commit();
    }
}
