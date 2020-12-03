package org.omicon.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.omicon.R;
import org.omicon.initial.Global;
import org.omicon.initial.HomeActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class message_view extends Activity {
    private ListView listView;
    private TextView textView;
    private MessageListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_view);

        textView = (TextView) findViewById(R.id.noMessagetext);
        listView = (ListView) findViewById(R.id.listView);
        loadDatatoList();
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
            Intent intent = new Intent(message_view.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadDatatoList() {
        SharedPreferences prefsUser = getSharedPreferences("MY_PREFS", 0);
        String query = "SELECT MSG_NO,USER_FULL_NAME,USER_MOBILE,MSG_SUBJECT,MSG_BODY,IS_READ,INSERT_TIME" +
                " FROM TRN_MSG WHERE REC_USER_NO=" + prefsUser.getString("user_no", "") + " ORDER BY INSERT_TIME DESC;";
        ArrayList<String[]> data = Global.dbObject.rawqueryFromDatabase(query);
        ArrayList<MessageItem> data_items = new ArrayList<message_view.MessageItem>();
        for (String[] str : data)
            data_items.add(new MessageItem(str[0], str[1], str[2], str[3], str[4], str[5], str[6]));

        adapter = new MessageListAdapter(data_items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(adapter);

        if (data.size() == 0) {
            textView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    class MessageItem {
        String msg_no;
        String from_user_name, from_user_mobile;
        String subject, body;
        String is_read, action_time;

        public MessageItem(String mno, String fn, String fm, String sub, String by, String ir, String at) {
            msg_no = mno;
            from_user_name = fn;
            from_user_mobile = fm;
            subject = sub;
            body = by;
            is_read = ir;
            action_time = at;
            if (at != null) {
                try {
                    action_time = timeValue(at.trim());
                } catch (Exception e) {
                    action_time = "";
                }
            }
        }

        public String timeValue(String value) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
            formatter.setTimeZone(TimeZone.getTimeZone("Asia/Dhaka"));
            Date date = new Date(Long.parseLong(value));
            return formatter.format(date);
        }
    }

    class MessageListAdapter extends ArrayAdapter<MessageItem> implements OnItemClickListener {
        private ArrayList<MessageItem> data;

        public MessageListAdapter(ArrayList<MessageItem> d) {
            super(message_view.this, R.layout.message_view_item, d);
            data = d;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MessageItem item = data.get(position);

            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.message_view_item, parent, false);

            TextView tv1 = (TextView) view.findViewById(R.id.messageFrom);
            tv1.setText(item.from_user_name + "\n" + item.from_user_mobile);

            TextView tv2 = (TextView) view.findViewById(R.id.messageSubject);
            tv2.setText(item.subject);

            TextView tv3 = (TextView) view.findViewById(R.id.messageDate);
            tv3.setText(item.action_time);

            if (item.is_read.matches("0")) {
                tv1.setTypeface(null, Typeface.BOLD);
                tv2.setTypeface(null, Typeface.BOLD);
                tv3.setTypeface(null, Typeface.BOLD);

                Log.d("msg", "Message Not Readed");
            } else
                Log.d("msg", "Message Already Readed");
            if (position % 2 == 0) {
                tv1.setBackgroundResource(R.drawable.message_item2_shape);
                tv2.setBackgroundResource(R.drawable.message_item2_shape);
                tv3.setBackgroundResource(R.drawable.message_item2_shape);
            }
            return view;
        }

        @Override
        public void onItemClick(AdapterView<?> arg0, View vw, int position,
                                long arg3) {
            MessageItem message = data.get(position);

            if (message.is_read.matches("0")) {
                ContentValues values = new ContentValues();
                values.put("IS_READ", "1");
                Global.dbObject.updateIntoTable("TRN_MSG", values, "MSG_NO=" + message.msg_no);

                TextView tv = (TextView) vw.findViewById(R.id.messageFrom);
                tv.setTypeface(null, Typeface.NORMAL);
                tv = (TextView) vw.findViewById(R.id.messageDate);
                tv.setTypeface(null, Typeface.NORMAL);
                tv = (TextView) vw.findViewById(R.id.messageSubject);
                tv.setTypeface(null, Typeface.NORMAL);

                message.is_read = "1";
                adapter.notifyDataSetChanged();
            }

            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.message_dialog, null);

            TextView tv = (TextView) view.findViewById(R.id.fromUserMobile);
            tv.setText(message.from_user_name + "<" + message.from_user_mobile + ">");
            tv = (TextView) view.findViewById(R.id.fromSentTime);
            tv.setText(message.action_time);
            tv = (TextView) view.findViewById(R.id.subject);
            tv.setText(message.subject);
            tv = (TextView) view.findViewById(R.id.message);
            tv.setText(message.body);

            AlertDialog.Builder builder = new AlertDialog.Builder(message_view.this);
            builder.setTitle("Message");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                }
            });
            builder.setView(view);
            builder.create().show();
        }

    }
}
