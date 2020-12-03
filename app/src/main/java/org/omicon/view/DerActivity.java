package org.omicon.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.SimpleFormatter;

import org.omicon.R;
import org.omicon.entry.EntryActivity;
import org.omicon.initial.Global;
import org.omicon.initial.HomeActivity;

import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class DerActivity extends Activity {

    private EditText etDatePicker;
    private Map<String, String> expenMap = new HashMap<String, String>();
    private Map<String, String> expenIDtoName = new HashMap<String, String>();
    private ArrayList<String> expenNames = new ArrayList<String>();
    private String optionValue;

    private boolean check_gps() {
        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Activate");
            builder.setMessage("Please turn on your Gps");
            builder.setNegativeButton("Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    finish();
                }
            });
            builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    System.exit(0);
                }
            });

            Dialog dialog = builder.create();
            dialog.setCancelable(false);
            if (!dialog.isShowing())
                dialog.show();

            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.der_main);

        optionValue = getIntent().getStringExtra("Option");

        etDatePicker = (EditText) findViewById(R.id.dateValue);
        etDatePicker.setText(Global.currentDate());
        loadDataToList(etDatePicker.getText().toString());

        expenNames.add("--Select An Expenditure--");
        ArrayList<String[]> expenDatas = Global.dbObject.queryFromTable("SET_EXP_TYPE", new String[]{"EXP_TYPE_NO", "EXP_TYPE_NAME"}, null);
        for (int i = 0; i < expenDatas.size(); i++)
            if (!expenMap.containsKey(expenDatas.get(i)[1])) {
                expenMap.put(expenDatas.get(i)[1], expenDatas.get(i)[0]);
                expenNames.add(expenDatas.get(i)[1]);
                expenIDtoName.put(expenDatas.get(i)[0], expenDatas.get(i)[1]);
            }

        DatePickerDialog.OnDateSetListener dateListener = new OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {

                if (!optionValue.matches("2") && !dateValidation(dayOfMonth, monthOfYear, year)) {
                    etDatePicker.setText("");
                    //etDatePicker.setError("Future Date Fixed");
                    Toast toast = Toast.makeText(DerActivity.this, "Invalid Selected Date", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                    return;
                }

                //	etDatePicker.setError(null);

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
        final DatePickerDialog dialog = new DatePickerDialog(DerActivity.this, dateListener,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        etDatePicker.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!optionValue.matches("0"))
                    dialog.show();
            }
        });

        Button addExpenditure = (Button) findViewById(R.id.newExpenditure);
        addExpenditure.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (etDatePicker.getText().toString().matches("")) {
                    Toast.makeText(DerActivity.this, "Please Choose A Date", Toast.LENGTH_LONG).show();
                } else
                    expenditureDialog(null);
            }
        });

        if (optionValue.matches("2"))
            addExpenditure.setVisibility(View.GONE);
        boolean val = check_gps();
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
            Intent intent = new Intent(DerActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadDataToList(String date_str) {
        SharedPreferences prefs = getSharedPreferences("MY_PREFS", 0);
        //from data base
        String query = "SELECT TRN_EXPENSE.OFFLINE_EXP_NO,OFFLINE_EXP_DET_NO,TRN_EXPENSE_DET.ACTION_OFFLINE_TIME,EXP_TYPE_NO,EXP_AMT,VENDOR,COMMENTS,TRN_EXPENSE_DET.is_uploaded FROM TRN_EXPENSE,TRN_EXPENSE_DET" +
                " WHERE TRN_EXPENSE.OFFLINE_EXP_NO=TRN_EXPENSE_DET.OFFLINE_EXP_NO AND TRN_EXPENSE_DET.ENTRY_STATE<>3 AND TRN_EXP_DATE='" + date_str + "' AND USER_NO=" + prefs.getString("user_no", "");

        Log.d("msg", query);
        ArrayList<String[]> data_s = Global.dbObject.rawqueryFromDatabase(query);
        Log.d("msg", "Query Size:" + String.valueOf(data_s.size()));
        ListView list_expen = (ListView) findViewById(R.id.expenditureList);
        TextView noRecord = (TextView) findViewById(R.id.noRecord);

        if (data_s.size() == 0) {
            list_expen.setVisibility(View.INVISIBLE);
            noRecord.setVisibility(View.VISIBLE);
        } else {
            noRecord.setVisibility(View.INVISIBLE);
            list_expen.setVisibility(View.VISIBLE);
        }

        data_expen = new ArrayList<DerActivity.ExpenData>();
        for (int i = 0; i < data_s.size(); i++) {
            String[] tmp = data_s.get(i);
            data_expen.add(new ExpenData(tmp[1], tmp[0], tmp[3], tmp[4], tmp[2], tmp[5], tmp[6], tmp[7]));
        }

        adapter = new MyListAdapter(data_expen);
        list_expen.setAdapter(adapter);
    }

    public void saveToDatabase(String date_str, String type, String amount, String vender, String comments) {
        SharedPreferences prefs = getSharedPreferences("MY_PREFS", 0);
        ArrayList<String[]> data_id = Global.dbObject.queryFromTable("TRN_EXPENSE",
                new String[]{"OFFLINE_EXP_NO"}, "TRN_EXP_DATE='" + date_str + "' AND USER_NO=" + prefs.getString("user_no", ""));
        long last_rowID;
        ContentValues values;
        if (data_id.size() == 0) {
            //last_rowID=Global.dbObject.getLastRowID("TRN_EXPENSE", "OFFLINE_EXP_NO")+1;
            last_rowID = Global.getPrimaryKeyID();
            Global.setPrimaryKeyID(last_rowID + 1);

            values = new ContentValues();
            values.put("OFFLINE_EXP_NO", last_rowID);
            values.put("ACTION_OFFLINE_TIME", Calendar.getInstance().getTimeInMillis());
            values.put("TRN_EXP_DATE", date_str);
            values.put("IS_MANUAL_ENTRY", optionValue);

            values.put("USER_NO", prefs.getString("user_no", ""));

            Global.dbObject.insertIntoTable("TRN_EXPENSE", values);
        } else {
            last_rowID = Long.parseLong(data_id.get(0)[0]);
            ContentValues masterValue = new ContentValues();
            masterValue.put("IS_UPLOADED", "0");
            Global.dbObject.updateIntoTable("TRN_EXPENSE", masterValue, "OFFLINE_EXP_NO=" + last_rowID);
        }
        long last_rowDetail = Global.dbObject.getLastRowID("TRN_EXPENSE_DET", "OFFLINE_EXP_DET_NO") + 1;
        values = new ContentValues();
        values.put("OFFLINE_EXP_DET_NO", last_rowDetail);
        values.put("OFFLINE_EXP_NO", last_rowID);
        values.put("ACTION_OFFLINE_TIME", Calendar.getInstance().getTimeInMillis());
        values.put("EXP_TYPE_NO", type);
        values.put("EXP_AMT", amount);
        // Here to Insert In the Database Must Update In to the upload Task
        values.put("VENDOR", vender);
        values.put("COMMENTS", comments);

        if (optionValue.matches("0")) {
            Location location = Global.currentLocation();
            if (location != null) {
                values.put("LAT_VAL", location.getLatitude());
                values.put("LON_VAL", location.getLongitude());
            }
        }

        Global.dbObject.insertIntoTable("TRN_EXPENSE_DET", values);

        if (data_expen != null) {
            /*final ExpenData nwDta=new ExpenData(String.valueOf(last_rowDetail),String.valueOf(last_rowID),type,
					amount,String.valueOf(Calendar.getInstance().getTimeInMillis()));*/
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
					/*data_expen.add(nwDta);
					adapter.notifyDataSetChanged();*/
                    loadDataToList(etDatePicker.getText().toString());
                }
            });
        }
        showToast("Added");
    }

    public void deleteFromDatabase(ExpenData data) {
        ArrayList<String[]> data_s = Global.dbObject.queryFromTable("TRN_EXPENSE_DET",
                new String[]{"OFFLINE_EXP_DET_NO", "IS_UPLOADED", "OFFLINE_EXP_NO"}, "OFFLINE_EXP_DET_NO=" + data.id);
        if (data_s.size() != 0) {
            long rowID = Long.parseLong(data_s.get(0)[0]);
            if (data_s.get(0)[1].matches("1")) {
                ContentValues values = new ContentValues();
                values.put("ENTRY_STATE", "3");
                values.put("IS_UPLOADED", "0");
                values.put("IS_COMPLETE_NEW", "0");
                Global.dbObject.updateIntoTable("TRN_EXPENSE_DET", values, "OFFLINE_EXP_DET_NO=" + rowID);

                values = new ContentValues();
                values.put("IS_UPLOADED", "0");
                values.put("IS_COMPLETE_NEW", "0");
                Global.dbObject.updateIntoTable("TRN_EXPENSE", values, "OFFLINE_EXP_NO=" + data_s.get(0)[2]);
            } else
                Global.dbObject.deleteFromTable("TRN_EXPENSE_DET", "OFFLINE_EXP_DET_NO=" + rowID);
        } else
            Log.d("msg_db", "No Row Found");
    }

    public void updateToExpenditure(final ExpenData data) {
        ArrayList<String[]> data_s = Global.dbObject.queryFromTable("TRN_EXPENSE_DET",
                new String[]{"OFFLINE_EXP_DET_NO", "IS_UPLOADED", "OFFLINE_EXP_NO"}, "OFFLINE_EXP_DET_NO=" + data.id);
        if (data_s.size() != 0) {
            long rowID = Long.parseLong(data_s.get(0)[0]);
            ContentValues values = new ContentValues();
            values.put("EXP_TYPE_NO", data.type);
            values.put("EXP_AMT", data.amount);
            values.put("VENDOR", data.vendor);
            values.put("COMMENTS", data.comments);

            if (data_s.get(0)[1].matches("1")) {
                values.put("ENTRY_STATE", "2");
                values.put("IS_UPLOADED", "0");
                values.put("IS_COMPLETE_NEW", "0");
            }
            Global.dbObject.updateIntoTable("TRN_EXPENSE_DET", values, "OFFLINE_EXP_DET_NO=" + rowID);

            values = new ContentValues();
            values.put("IS_UPLOADED", "0");
            values.put("ENTRY_STATE", "2");
            values.put("IS_COMPLETE_NEW", "0");
            Global.dbObject.updateIntoTable("TRN_EXPENSE", values, "OFFLINE_EXP_NO=" + data_s.get(0)[2]);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
					/*data_expen.add(data);
					adapter.notifyDataSetChanged();*/
                    loadDataToList(etDatePicker.getText().toString());
                }
            });
            Log.d("msg", "Update One Row:" + data.id);
        } else
            Log.d("msg", "Data Not Successfully Updated");
    }

    public void showToast(String message) {
        Toast toast = Toast.makeText(DerActivity.this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void expenditureDialog(final ExpenData data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DerActivity.this);
        String tle = "Add";
        if (data == null)
            builder.setTitle("New Expenditure");
        else {
            tle = "Edit";
            builder.setTitle("Edit Expenditure");
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.der_entry, null);

        final Spinner type = (Spinner) view.findViewById(R.id.shwExpenditure);
        if (expenMap.size() > 0) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(DerActivity.this, android.R.layout.simple_spinner_dropdown_item, expenNames);
            type.setAdapter(adapter);
        }

        final EditText et = (EditText) view.findViewById(R.id.shwExpenditureCost);
        final EditText vender = (EditText) view.findViewById(R.id.shwExpenditureVender);
        final EditText comments = (EditText) view.findViewById(R.id.shwExpenditureComments);

        if (data != null) {

            if (expenIDtoName.containsKey(data.type)) {
                int i;
                String eName = expenIDtoName.get(data.type);
                for (i = 0; i < expenNames.size(); i++)
                    if (eName.matches(expenNames.get(i)))
                        break;
                if (i < expenNames.size())
                    type.setSelection(i);
            }
            et.setText(data.amount);

            // for edit data here set vendor comments
            vender.setText(data.vendor);
            comments.setText(data.comments);
        }


        builder.setPositiveButton(tle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub
            }
        });

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();

        Button save = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if (type.getSelectedItemPosition() == 0)
                    showToast("Expenditure Not Select");
                else if (et.getText().toString().matches("")) {
                    showToast("Amount(tk) Field Empty");
                } else {
                    if (data == null) {
                        Log.d("msg", "New Expenditure:" + type.getSelectedItem() + "," + et.getText().toString());

                        String exp_type_no = "0";
                        if (expenMap.containsKey(type.getSelectedItem()))
                            exp_type_no = expenMap.get(type.getSelectedItem());

                        saveToDatabase(etDatePicker.getText().toString(), exp_type_no, et.getText().toString(),
                                vender.getText().toString(), comments.getText().toString());
                        dialog.dismiss();
                    } else {
                        Log.d("msg", "Edit Expenditure:" + type.getSelectedItem() + "," + et.getText().toString());

                        String exp_type_no = "0";
                        if (expenMap.containsKey(type.getSelectedItem()))
                            exp_type_no = expenMap.get(type.getSelectedItem());

                        data.type = exp_type_no;
                        data.amount = et.getText().toString();
                        data.vendor = vender.getText().toString();
                        data.comments = comments.getText().toString();

                        updateToExpenditure(data);
                        dialog.dismiss();
                    }
                }
            }
        });

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


    class ExpenData {
        String id, pid;
        String type, amount, time;
        String insertDate = "";
        String vendor, comments, is_upload;

        public ExpenData(String i, String pd, String t, String a, String tt, String vend, String comm, String is_upload) {
            id = i;
            pid = pd;
            type = t;
            amount = a;
            time = formatted(tt);
            insertDate = Global.getDateValue(Long.parseLong(tt));
            this.is_upload = is_upload;
            vendor = vend;
            comments = comm;
        }

        public String formatted(String t) {
            SimpleDateFormat fmt = new SimpleDateFormat("hh:mm a");
            return fmt.format(new Date(Long.parseLong(t)));
        }
    }

    private class MyListAdapter extends ArrayAdapter<ExpenData> {
        private ArrayList<ExpenData> data;
        private Map<String, String> mapExpen = new HashMap<String, String>();

        public MyListAdapter(ArrayList<ExpenData> d) {
            super(DerActivity.this, R.layout.der_entry_item, d);
            data = d;
            ArrayList<String[]> expenDatas = Global.dbObject.queryFromTable("SET_EXP_TYPE", new String[]{"EXP_TYPE_NO", "EXP_TYPE_NAME"}, null);
            for (int i = 0; i < expenDatas.size(); i++)
                mapExpen.put(expenDatas.get(i)[0], expenDatas.get(i)[1]);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.der_entry_item, parent, false);

            TextView tv = (TextView) rowView.findViewById(R.id.expenType);
            tv.setText(mapExpen.get(data.get(position).type));

            tv = (TextView) rowView.findViewById(R.id.expenTime);
            tv.setText(data.get(position).time);

            tv = (TextView) rowView.findViewById(R.id.expenAmount);
            double amount = Double.parseDouble(data.get(position).amount);
            tv.setText(String.format("%.2f", amount));

            tv = (TextView) rowView.findViewById(R.id.expenVendor);
            tv.setText(data.get(position).vendor);

            tv = (TextView) rowView.findViewById(R.id.showSync);
            tv.setText((data.get(position).is_upload.matches("1") ? "Syncd" : ""));
            String tmp = data.get(position).is_upload;

            ImageView expenEdit = (ImageView) rowView.findViewById(R.id.editExpen);
            expenEdit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Log.d("msg", "Edit Button Clicked");
                    expenditureDialog(data.get(position));
					/*runOnUiThread(new Runnable() {
						@Override
						public void run() {
							data_expen.remove(position);
						}
					});*/

                }
            });

            ImageView expenRemove = (ImageView) rowView.findViewById(R.id.removeExpen);
            expenRemove.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DerActivity.this);
                    builder.setTitle("Delete DCR Detail Entry");
                    builder.setMessage("Are you sure want to delete DCR Detail Entry?");
                    builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            deleteFromDatabase(data.get(position));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
									/*data_expen.remove(position);
									adapter.notifyDataSetChanged();*/
                                    loadDataToList(etDatePicker.getText().toString());
                                }
                            });
                        }
                    });
                    builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.create().show();
                }
            });

            if (!Global.currentDate().matches(data.get(position).insertDate)) {
                expenEdit.setVisibility(View.GONE);
                expenRemove.setVisibility(View.GONE);
            }
            return rowView;
        }
    }

    private ArrayList<ExpenData> data_expen = new ArrayList<DerActivity.ExpenData>();
    private MyListAdapter adapter;
}
