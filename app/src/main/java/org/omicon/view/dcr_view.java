package org.omicon.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.omicon.R;
import org.omicon.entry.EntryActivity;
import org.omicon.entry.dcr_master;
import org.omicon.initial.Global;
import org.omicon.initial.HomeActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class dcr_view extends Activity {
    private MyListAdapter adapter;
    private EditText etDatePicker;
    private ArrayList<String[]> data_out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dcr_view_main);


        etDatePicker = (EditText) findViewById(R.id.dcrDateValue);
        DatePickerDialog.OnDateSetListener dateListener = new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {

				/*if(!dateValidation(dayOfMonth, monthOfYear, year)){
                    etDatePicker.setText("");
					Toast.makeText(dcr_view.this,"Date is not valid",Toast.LENGTH_LONG).show();
					loadDataToList(etDatePicker.getText().toString());
					return ;
				}*/

                String date = String.valueOf(dayOfMonth);
                if (dayOfMonth < 10)
                    date = "0" + dayOfMonth;
                ++monthOfYear;
                if (monthOfYear < 10)
                    date += "/0" + monthOfYear;
                else
                    date += "/" + monthOfYear;
                date += "/" + year;

                etDatePicker.setText(date);
                loadDataToList(date);
            }
        };
        Calendar cal = Calendar.getInstance();
        final DatePickerDialog dialog = new DatePickerDialog(dcr_view.this, dateListener,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        etDatePicker.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.show();
            }
        });

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        etDatePicker.setText(formatter.format(new Date(Calendar.getInstance().getTimeInMillis())));
        loadDataToList(etDatePicker.getText().toString());
    }

    public boolean dateValidation(int day, int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, 11, 11);
        long time = cal.getTimeInMillis();
        cal.set(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH), 11, 11);
        if (time <= cal.getTimeInMillis())
            return true;
        else
            return false;
    }

    public void loadDataToList(String date_str) {
        SharedPreferences prefs = getSharedPreferences("MY_PREFS", 0);

        //data_out=Global.dbObject.queryFromTable("TRN_DCR", null,"ENTRY_STATE<>3 AND TRN_DCR_DATE='"+date_str+"' AND USER_NO="+prefs.getString("user_no",""));
        String query = "SELECT * FROM TRN_DCR WHERE ENTRY_STATE<>3 AND TRN_DCR_DATE='" + date_str + "'" +
                " AND USER_NO=" + prefs.getString("user_no", "") + " ORDER BY ACTION_OFFLINE_TIME DESC";
        data_out = Global.dbObject.rawqueryFromDatabase(query);

        ListView list = (ListView) findViewById(R.id.listView);
        TextView noRecord = (TextView) findViewById(R.id.noRecord);
        if (data_out.size() != 0) {
            list.setVisibility(View.VISIBLE);
            noRecord.setVisibility(View.INVISIBLE);
        } else {
            list.setVisibility(View.INVISIBLE);
            noRecord.setVisibility(View.VISIBLE);
        }

        adapter = new MyListAdapter(this, data_out);
        list.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
		
		/*Intent intent=new Intent(getApplicationContext(),dcr_view.class);
		startActivity(intent);
		this.finish();*/
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadDataToList(etDatePicker.getText().toString());
            }
        });

    }

    public void deleteDCRMaster(String table_name, String query) {
        ArrayList<String[]> master_data = Global.dbObject.queryFromTable(table_name, new String[]{"IS_UPLOADED", "IS_COMPLETE_NEW"}, query);
        if (master_data.get(0)[0].matches("1")) {
            ContentValues values = new ContentValues();
            values.put("ENTRY_STATE", "3");
            values.put("IS_UPLOADED", "0");
            values.put("IS_COMPLETE_NEW", "0");

            Global.dbObject.updateIntoTable(table_name, values, query);
        } else {
            if (master_data.get(0)[1].matches("1")) {
                Global.dbObject.deleteFromTable(table_name, query);
                Global.dbObject.deleteFromTable("TRN_DCR_DET", query);
            } else {
                ContentValues values = new ContentValues();
                values.put("ENTRY_STATE", "3");
                values.put("IS_UPLOADED", "0");
                values.put("IS_COMPLETE_NEW", "0");

                Global.dbObject.updateIntoTable(table_name, values, query);
            }
        }

        //Toast.makeText(context,"Deleted From DCR",Toast.LENGTH_LONG);
		/*((Activity)context).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				//adapter.notifyDataSetChanged();
				Intent intent=new Intent(context,EntryView.class);
				startActivity(intent);
				((Activity)context).finish();
			}
		});*/
	/*	Intent intent=new Intent(this,dcr_view.class);
		startActivity(intent);
		this.finish();*/
        loadDataToList(etDatePicker.getText().toString());
    }

    public void alertDialogYesorNo(final String[] item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Master Entry");
        builder.setMessage("Do you want to delete ?");
        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Log.d("msg", "Enter for Master Delete ... ");
                deleteDCRMaster("TRN_DCR", "OFFLINE_DCR_NO=" + item[0]);

                if (item[19].matches("0")) {
                    long time = Calendar.getInstance().getTimeInMillis();
                    try {
                        time = Long.parseLong(item[13]);
                    } catch (Exception e) {
                    }

                    if (Global.currentSessionStart < time) {
                        ContentValues values = new ContentValues();
                        values.put("LOCATION_NAME", item[8]);
                        values.put("TIME", item[12]);
                        values.put("LONGITUDE", item[7]);
                        values.put("LATITUDE", item[6]);

                        SharedPreferences prefs = getSharedPreferences("MY_PREFS", 0);
                        Global.dbObject.updateIntoTable("LOGIN_INFO", values, "USER_NO=" + prefs.getString("user_no", ""));
                    }
                }
            }
        });
        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        builder.create().show();
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
            Intent intent = new Intent(dcr_view.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyListAdapter extends ArrayAdapter<String[]> {
        private Context context;
        private ArrayList<String[]> data;
        private String[] workType = {"SD", "MP"
                , "TC", "LC", "OW", "CSR"};
        private Map<String, String> transType = new HashMap<String, String>();

        private boolean isFirstOnlineDCR = true;
        private String id_first_online;

        public MyListAdapter(Context con, ArrayList<String[]> d) {
            super(con, R.layout.dcr_view_list_item, d);
            context = con;
            Log.d("msg", "At Adapter " + String.valueOf(d.size()));
            data = d;

            isFirstOnlineDCR = true;
            ArrayList<String[]> transportDatas = Global.dbObject.queryFromTable("SET_TRANSPORT_TYPE", new String[]{"TRANS_TYPE_NO", "TRANS_TYPE_NAME"}, null);
            for (int i = 0; i < transportDatas.size(); i++)
                transType.put(transportDatas.get(i)[0], transportDatas.get(i)[1]);

            try {
                SharedPreferences prefs = getSharedPreferences("MY_PREFS", 0);
                ArrayList<String[]> tmp_output = Global.dbObject.rawqueryFromDatabase("SELECT OFFLINE_DCR_NO from TRN_DCR WHERE ACTION_OFFLINE_TIME=(SELECT max(ACTION_OFFLINE_TIME) from TRN_DCR WHERE ENTRY_STATE<>3 AND IS_MANUAL_ENTRY=0 AND USER_NO=" + prefs.getString("user_no", "") + ")");
                if (tmp_output.size() != 0)
                    id_first_online = tmp_output.get(0)[0];
                else
                    id_first_online = "0";

            } catch (Exception e) {
            }
        }

        public String timeValue(String value) {
            SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
            Date date = new Date(Long.parseLong(value));
            return formatter.format(date);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            //Log.d("msg","View For Item Position:"+String.valueOf(position));

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.dcr_view_list_item, parent, false);

            TextView tv = (TextView) view.findViewById(R.id.showViewType);
            tv.setText(workType[Integer.parseInt(data.get(position)[2]) - 1]);


            if (data.get(position)[19].matches("1")) {
                //tv.setText("Offline Entry");
                tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.red, 0);
            } else {
                //tv.setText("Online Entry");
                tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.green, 0);
            }
            tv = (TextView) view.findViewById(R.id.showUploaded);
            tv.setText(((data.get(position)[22].matches("1")) ? "Syncd" : ""));

            tv = (TextView) view.findViewById(R.id.showViewFrom);
            tv.setText(data.get(position)[8]);

            tv = (TextView) view.findViewById(R.id.showViewFromTime);
            tv.setText(timeValue(data.get(position)[12]));

            tv = (TextView) view.findViewById(R.id.showViewTo);
            tv.setText(data.get(position)[11]);

            tv = (TextView) view.findViewById(R.id.showViewToTime);
            tv.setText(timeValue(data.get(position)[13]));

            tv = (TextView) view.findViewById(R.id.showViewTransport);
            if (transType.containsKey(data.get(position)[16]))
                tv.setText(transType.get(data.get(position)[16]));
            else
                tv.setText("Transport");

            tv = (TextView) view.findViewById(R.id.showViewCost);
            tv.setText(data.get(position)[17] + " taka");

            ImageView viewManual = (ImageView) view.findViewById(R.id.showViewIsManual);
            if (data.get(position)[3].matches("1"))
                viewManual.setImageResource(R.drawable.onbehalf);
            else
                viewManual.setImageResource(R.drawable.self);

            ImageButton button = (ImageButton) view.findViewById(R.id.nextMaster1);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent(context, dcr_view_detail.class);
                    intent.putExtra("master", data.get(position));
                    startActivity(intent);
                    Log.d("msg", "Next Button Clicked");
                }
            });
            if (data.get(position)[2].matches("5"))
                button.setVisibility(View.INVISIBLE);

            String action_date = Global.getDateValue(Long.parseLong(data.get(position)[1]));
            button = (ImageButton) view.findViewById(R.id.addMaster1);
            if (!action_date.matches(Global.currentDate()))
                button.setVisibility(View.GONE);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, dcr_master.class);
                    intent.putExtra("master", data.get(position));
                    startActivityForResult(intent, 1);
                    Log.d("msg", "Add Button Clicked");
                }
            });

            button = (ImageButton) view.findViewById(R.id.deleteMaster1);
            if (!action_date.matches(Global.currentDate()))
                button.setVisibility(View.GONE);

            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialogYesorNo(data.get(position));
                    Log.d("msg", "Delete Button Clicked");
                }
            });
            /**
             * Only the latest one record dcr(online/offline) is valid for delete
             */
            if (data.get(position)[19].matches("0")) {
				/*if(!isFirstOnlineDCR)
					button.setVisibility(View.INVISIBLE);
				isFirstOnlineDCR=false;*/
                if (!id_first_online.matches(data.get(position)[0]))
                    button.setVisibility(View.INVISIBLE);
            }
            return view;
        }
    }

}
