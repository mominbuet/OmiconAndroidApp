package org.omicon.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.omicon.R;
import org.omicon.entry.EntryActivity;
import org.omicon.initial.Global;
import org.omicon.initial.HomeActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class feedback extends Activity {
    private ListView listView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        final EditText et = (EditText) findViewById(R.id.feedbacks);
        final Spinner type = (Spinner) findViewById(R.id.feedbackType);

        ArrayList<String[]> feedBacksTypes = Global.dbObject.queryFromTable("SET_FEEDBACK_TYPE", new String[]{"FEEDBACK_TYPE_NO", "FEEDBACK_NAME"}, "IS_ACTIVE=1");
        final Map<String, String> nameToPrimary = new HashMap<String, String>();
        ArrayList<String> typeNames = new ArrayList<String>();

        typeNames.add("--Select Feedback--");
        for (String[] strings : feedBacksTypes) {
            nameToPrimary.put(strings[1], strings[0]);
            typeNames.add(strings[1]);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(feedback.this, android.R.layout.simple_spinner_dropdown_item, typeNames);
        type.setAdapter(adapter);

        textView = (TextView) findViewById(R.id.feedBackNoRecordMessage);
        listView = (ListView) findViewById(R.id.listFeedbacks);
        loadDataTolist();

        Button b = (Button) findViewById(R.id.feedbackButton);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String message = "";
                if (type.getSelectedItemPosition() == 0)
                    message = "Feedback type not select";
                else if (!et.getText().toString().matches("")) {
                    ContentValues values = new ContentValues();
                    values.put("OFFLINE_FEEDBACK_NO", Global.dbObject.getLastRowID("TRN_USER_FEED_BACK", "OFFLINE_FEEDBACK_NO") + 1);
                    values.put("ACTION_OFFLINE_TIME", Calendar.getInstance().getTimeInMillis());

                    SharedPreferences prefs = getSharedPreferences("MY_PREFS", 0);
                    values.put("USER_NO", prefs.getString("user_no", ""));

                    values.put("FEEDBACK_TYPE_NO", nameToPrimary.get(type.getSelectedItem()));
                    values.put("MESSAGE", et.getText().toString());

                    Global.dbObject.insertIntoTable("TRN_USER_FEED_BACK", values);

                    message = "Added Successfully";
                    et.setText("");
                    loadDataTolist();
                } else
                    message = "Can not be empty";

                Toast toast = Toast.makeText(feedback.this, message, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }

    public void loadDataTolist() {
        SharedPreferences prefs = getSharedPreferences("MY_PREFS", 0);

        String query = "SELECT FEEDBACK_NAME,MESSAGE,ACTION_OFFLINE_TIME,IS_UPLOADED" +
                " FROM SET_FEEDBACK_TYPE,TRN_USER_FEED_BACK" +
                " WHERE SET_FEEDBACK_TYPE.FEEDBACK_TYPE_NO=TRN_USER_FEED_BACK.FEEDBACK_TYPE_NO AND TRN_USER_FEED_BACK.ENTRY_STATE<>3  AND TRN_USER_FEED_BACK.USER_NO=" + prefs.getString("user_no", "") +
                " ORDER BY TRN_USER_FEED_BACK.ACTION_OFFLINE_TIME DESC";
        ArrayList<String[]> results = Global.dbObject.rawqueryFromDatabase(query);
        FeedBackAdapter adapter = new FeedBackAdapter(this, results);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(adapter);

        if (results.size() == 0) {
            listView.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.INVISIBLE);
        }
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
            Intent intent = new Intent(feedback.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    class FeedBackAdapter extends ArrayAdapter<String[]> implements OnItemClickListener {
        private ArrayList<String[]> data;
        private SimpleDateFormat formatter;

        public FeedBackAdapter(Context con, ArrayList<String[]> d) {
            super(con, R.layout.activity_feedback_item, d);
            data = d;
            formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        }

        public String formatedDate(String s) {
            String time;
            try {
                time = formatter.format(new Date(Long.parseLong(s)));
            } catch (Exception ex) {
                time = formatter.format(new Date());
            }
            return time;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.activity_feedback_item, parent, false);

            TextView tv = (TextView) rowView.findViewById(R.id.feedBackTypeName);
            tv.setText(data.get(position)[0]);

            tv = (TextView) rowView.findViewById(R.id.feedBackMessage);
            tv.setText(data.get(position)[1]);

            tv = (TextView) rowView.findViewById(R.id.feedBackTime);
            tv.setText(formatedDate(data.get(position)[2]));

            ImageView iv = (ImageView) rowView.findViewById(R.id.feedBackUpload);
            if (data.get(position)[3].matches("1"))
                iv.setImageResource(R.drawable.upload_okay);
            else
                iv.setImageResource(R.drawable.upload_fail);

            if (position % 2 == 0)
                rowView.setBackgroundColor(Color.parseColor("#FFEBCD"));
            else
                rowView.setBackgroundColor(Color.parseColor("#E0FFFF"));

            return rowView;
        }

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                long arg3) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.feedback_full, null);

            TextView tv = (TextView) view.findViewById(R.id.feedBackTypeName);
            tv.setText(data.get(position)[0]);

            tv = (TextView) view.findViewById(R.id.feedBackMessage);
            tv.setText(data.get(position)[1]);

            tv = (TextView) view.findViewById(R.id.feedBackTime);
            tv.setText(formatedDate(data.get(position)[2]));

            AlertDialog.Builder builder = new AlertDialog.Builder(feedback.this);
            builder.setTitle("Feed Back");
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
