package org.omicon.entry;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.omicon.*;
import org.omicon.helper.CustomExceptionHandler;
import org.omicon.initial.Global;
import org.omicon.initial.HomeActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class dcr_master extends Activity {
    Context context;
    LinearLayout numberLayout, insnameLayout, timeLayout, zmInfo;
    RadioGroup hrGroup;
    EditText cost, stime, etime, fromLocation, comments;
    AutoCompleteTextView locName;
    EditText etDatePicker;
    AutoCompleteTextView insCode, zmMobile;
    Spinner transport, workType;
    TextView insName, zmName, zmNum;
    Button save;
    ArrayList<String> insCodeData = new ArrayList<String>();
    ArrayList<String> zmMobileData = new ArrayList<String>();

    Map<String, String> insCodePrimary = new HashMap<String, String>();
    Map<String, String> insCodePrimaryToCode = new HashMap<String, String>();

    Map<String, String> zmMobilePrimary = new HashMap<String, String>();
    Map<String, String> transportIDS = new HashMap<String, String>();
    Map<String, String> transportIDtoName = new HashMap<String, String>();
    ArrayList<String> transPortName = new ArrayList<String>();

    private SharedPreferences prefs;
    private long primary_id;
    private int is_ref_zm = 0;
    private long stime_value, etime_value;
    private int for_update;
    private boolean is_offline;
    private String[] master_data;
    private String fromLocLat = "", fromLocLon = "", toLocLat = "", toLocLon = "";
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

    private String user_no;

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
                    moveTaskToBack(true);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
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
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        context = this;

        SharedPreferences prefsUser = getSharedPreferences("MY_PREFS", 0);
        user_no = prefsUser.getString("user_no", "");

        transPortName.add("--Select A Transport--");
        ArrayList<String[]> transportDatas = Global.dbObject.queryFromTable("SET_TRANSPORT_TYPE", new String[]{"TRANS_TYPE_NO", "TRANS_TYPE_NAME"}, null);

            for (int i = 0; i < transportDatas.size(); i++)
                if (!transportIDS.containsKey(transportDatas.get(i)[1])) {
                    transportIDS.put(transportDatas.get(i)[1], transportDatas.get(i)[0]);
                    transPortName.add(transportDatas.get(i)[1]);
                    transportIDtoName.put(transportDatas.get(i)[0], transportDatas.get(i)[1]);
                }



        prefs = context.getSharedPreferences("MY_PREFS_CONFIG", 0);
        //primary_id=prefs.getInt("DCR_PRIMARY",1);

        is_offline = getIntent().getBooleanExtra("IS_OFFLINE", true);

        initInstitutionCodes();
        initZmMobiles();
        //below two method initialization and all event fire
        bindListener();
        eventFiered();
        eventForDate();

        forOnline();
        checkforUpdate();
        boolean checkgps = check_gps();
        //Global.exceptionhandler(context);

    }

    public void forOnline() {
        if (!is_offline) {
            SimpleDateFormat formater = new SimpleDateFormat("hh:mm a");

			/*SharedPreferences timeprefs=context.getSharedPreferences("location_time",1);
            String startLoc=timeprefs.getString("start_location", "");
			long st_time=timeprefs.getLong("start_time", 0);*/
            SharedPreferences prefs = getSharedPreferences(
                    "MY_PREFS", 0);
            user_no=prefs.getString("user_no","");



            String[] user_data = Global.dbObject.queryFromTable("LOGIN_INFO", null, "USER_NO=" + user_no).get(0);

            long st_time = Long.parseLong(user_data[2]);
            String startLoc = user_data[1];

            String startTime = "";
            if (st_time != 0)
                startTime = formater.format(new Date(st_time));

            fromLocation.setText(startLoc);
            stime.setText(startTime);
            etime.setText(formater.format(new Date(Calendar.getInstance().getTimeInMillis())));

            fromLocation.setFocusable(false);
            stime.setFocusable(false);
            etime.setFocusable(false);

            fromLocLon = user_data[3];
            fromLocLat = user_data[4];

            stime.setTextColor(Color.MAGENTA);
            etime.setTextColor(Color.MAGENTA);
            fromLocation.setTextColor(Color.MAGENTA);
            etDatePicker.setTextColor(Color.MAGENTA);

            //Location currentLocation=getCurrentLocation();
            Location currentLocation = Global.currentLocation();
            if (currentLocation != null) {
                toLocLon = Double.toString(currentLocation.getLongitude());
                toLocLat = Double.toString(currentLocation.getLatitude());
            }
        }
    }

	/*public Location getCurrentLocation(){
        Location location=null;
		LocationManager locManager=(LocationManager) getSystemService(LOCATION_SERVICE);
		
		if(locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			location=locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		else if(locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			location=locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			Log.d("msg", "Location SET FROM:"+location);
		}
		else
			Log.d("msg", "No Location SET FROM");
		return location;
	}*/

    public void bindListener() {
        numberLayout = (LinearLayout) findViewById(R.id.numberlayout);
        numberLayout.setVisibility(LinearLayout.GONE);

        //timeLayout=(LinearLayout) findViewById(R.id.timeLayout);

        zmInfo = (LinearLayout) findViewById(R.id.otherZM);
        zmInfo.setVisibility(LinearLayout.GONE);

        zmName = (TextView) findViewById(R.id.zmName);
        zmNum = (TextView) findViewById(R.id.zmNum);
        // Here Check IS whether offline mode or server mode

        insnameLayout = (LinearLayout) findViewById(R.id.insnameLayout);
        insnameLayout.setVisibility(LinearLayout.GONE);

        stime = (EditText) findViewById(R.id.stime);
        etime = (EditText) findViewById(R.id.etime);
        insName = (TextView) findViewById(R.id.insname);

        transport = (Spinner) findViewById(R.id.transport);
        if (transportIDS.size() > 0) {
            ArrayAdapter<String> tAdp = new ArrayAdapter<String>(dcr_master.this, android.R.layout.simple_spinner_dropdown_item, transPortName);
            transport.setAdapter(tAdp);
        }

        cost = (EditText) findViewById(R.id.cost);
        fromLocation = (EditText) findViewById(R.id.fromlocation);
        locName = (AutoCompleteTextView) findViewById(R.id.location);
        comments = (EditText) findViewById(R.id.comments);
        zmMobile = (AutoCompleteTextView) findViewById(R.id.zmmobile);
        insCode = (AutoCompleteTextView) findViewById(R.id.institution);

        workType = (Spinner) findViewById(R.id.workType);
        hrGroup = (RadioGroup) findViewById(R.id.hrtype);

    }

    public void eventFiered() {
        Location location = Global.currentLocation();
        Log.d("msg", "Location At Dialog:" + location);
        if (location != null)
            locName.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Global.getNearestLocation(location, 1000)));
        locName.setThreshold(0);

        SuggestionAdapter sadapter = new SuggestionAdapter(context, R.layout.suggestion_label, insCodeData);
        insCode.setAdapter(sadapter);
        insCode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(android.widget.AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String str = (String) arg0.getItemAtPosition(arg2);
                if (!insCode.getText().toString().matches("")) {
                    insnameLayout.setVisibility(LinearLayout.VISIBLE);
                    insCode.setText(str.substring(str.lastIndexOf("(") + 1, str.lastIndexOf(")")));
                    insName.setText(str.substring(0, str.lastIndexOf("(")));

                    insCode.setSelection(insCode.getText().length());
                } else
                    insnameLayout.setVisibility(LinearLayout.GONE);
            }

            ;
        });
        insCode.setThreshold(1);

        sadapter = new SuggestionAdapter(context, R.layout.suggestion_label, zmMobileData);
        zmMobile.setAdapter(sadapter);
        zmMobile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(android.widget.AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String str = (String) arg0.getItemAtPosition(arg2);
                if (!zmMobile.getText().toString().matches("")) {
                    zmInfo.setVisibility(LinearLayout.VISIBLE);
                    zmMobile.setText(str.substring(str.indexOf("(") + 1, str.indexOf(")")));
                    zmName.setText(str.substring(0, str.indexOf("(")));
                    zmNum.setText(str.substring(str.indexOf("(") + 1, str.indexOf(")")));

                    zmMobile.setSelection(zmMobile.getText().length());
                } else
                    zmInfo.setVisibility(LinearLayout.GONE);

            }

            ;
        });

        insCode.addTextChangedListener(inscodeWatcher);
        zmMobile.addTextChangedListener(zmMobileWatcher);

        workType.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                if (master_data == null) {
                    if (arg2 == 5) {
                        //save.setBackgroundResource(R.drawable.save);
                        save.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.save, 0, 0);
                        save.setText("Save");
                    } else {
                        save.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.next, 0, 0);
                        save.setText("Continue");
                    }
                } else {
                    save.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.save, 0, 0);
                    save.setText("Update");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        hrGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                if (arg0.getCheckedRadioButtonId() == R.id.other) {
                    is_ref_zm = 1;
                    numberLayout.setVisibility(LinearLayout.VISIBLE);

                } else {
                    is_ref_zm = 0;
                    numberLayout.setVisibility(LinearLayout.GONE);
                    zmInfo.setVisibility(LinearLayout.GONE);
                    zmMobile.setText("");
                    zmName.setText("");
                    zmNum.setText("");
                }

            }
        });
		/*insCode.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View arg0, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(!hasFocus && !insCode.getText().toString().matches(""))
				{
					insnameLayout.setVisibility(LinearLayout.VISIBLE);
					if(insCode.getText().toString().compareTo("test")==0)
						insName.setText("RIGHT");
					else
						insName.setText("WRONG");
				}
				else
					insnameLayout.setVisibility(LinearLayout.GONE);
			}
		});*/
        timePicker(stime, "Select Start Time");
        timePicker(etime, "Select End Time");

        save = (Button) findViewById(R.id.button1);
        save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (checkValidation()) {
                    if (master_data == null) {
                        insertDCRMaster();

                        insCode.setError(null);

                        if (!is_offline) {
							/*SharedPreferences timeprefs=context.getSharedPreferences("location_time",1);
							Editor editor=timeprefs.edit();
							editor.putString("start_location", locName.getText().toString());
							editor.putLong("start_time",Calendar.getInstance().getTimeInMillis());*/

                            Location location = Global.currentLocation();

                            if (location != null) {
                                fromLocLat = Double.toString(location.getLatitude());
                                fromLocLon = Double.toString(location.getLongitude());
								/*editor.putString("start_long",Double.toString(location.getLongitude()));
								editor.putString("start_lat",Double.toString(location.getLatitude()));*/

                                ArrayList<String[]> data_location = Global.dbObject.queryFromTable("TRN_USER_LOCATION", null, "LOCATION_NAME='" + locName.getText().toString() + "'");
                                if (data_location.size() == 0) {
                                    ContentValues values = new ContentValues();
                                    values.put("OFFLINE_LOC_NO", Global.dbObject.getLastRowID("TRN_USER_LOCATION", "OFFLINE_LOC_NO") + 1);
                                    values.put("ACTION_OFFLINE_TIME", Calendar.getInstance().getTimeInMillis());
                                    values.put("LAT_VAL", location.getLatitude());
                                    values.put("LON_VAL", location.getLongitude());
                                    values.put("LOCATION_NAME", Global.CapitalizeWords(locName.getText().toString()));
                                    values.put("USER_NO", user_no);

                                    Global.dbObject.insertIntoTable("TRN_USER_LOCATION", values);
                                }
                            } else {
                                fromLocLat = fromLocLon = "";
								/*editor.putString("start_long","");
								editor.putString("start_lat","");*/
                            }
							/*editor.commit();*/

                            ContentValues values = new ContentValues();
                            values.put("LOCATION_NAME", Global.CapitalizeWords(locName.getText().toString()));
                            values.put("TIME", Global.timeValueFromString(etDatePicker.getText().toString(), etime.getText().toString()));
                            values.put("LONGITUDE", fromLocLon);
                            values.put("LATITUDE", fromLocLat);
                            Global.dbObject.updateIntoTable("LOGIN_INFO", values, "USER_NO=" + user_no);
                        }

                        if (workType.getSelectedItemPosition() != 5) {
                            // start another intent
                            Intent intent = new Intent(context, EntryActivity.class);
                            intent.putExtra("MASTER_ID", primary_id);

                            if (workType.getSelectedItemPosition() != 6)
                                intent.putExtra("workType", (String) workType.getSelectedItem());
                            else
                                intent.putExtra("workType", (String) workType.getItemAtPosition(1));

                            startActivity(intent);
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "Added Successfully", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                        clearAllField();
                    } else {
                        updateDCRMaster();
                        //onBackPressed();
                        finish();
                    }
                }
            }
        });
    }

    public void eventForDate() {
        etDatePicker = (EditText) findViewById(R.id.prevDateValue);
        etDatePicker.setText(Global.currentDate());
        DatePickerDialog.OnDateSetListener dateListener = new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {

                if (!Global.dateValidation(dayOfMonth, monthOfYear, year)) {
                    etDatePicker.setText("");
                    //etDatePicker.setError("Future Date Fixed");
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
            }
        };
        Calendar cal = Calendar.getInstance();
        final DatePickerDialog dialog = new DatePickerDialog(dcr_master.this, dateListener,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        if (is_offline) {
            etDatePicker.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {

                    dialog.show();


                }
            });
        }
    }

    public boolean checkValidation() {
        boolean isValid = true;
		/*String loc_name=locName.getText().toString(),
				ins_code=insCode.getText().toString(),
				tra_cost=cost.getText().toString(),
				date=etDatePicker.getText().toString(),
				s_time=stime.getText().toString(),
				e_time=etime.getText().toString();
		
		if(loc_name.matches("")||tra_cost.matches("")||s_time.matches("")||e_time.matches("")||date.matches(""))
		{
			isValid=false;
			Toast.makeText(context, "LocationName|Transport|Date|Time input field empty", Toast.LENGTH_LONG).show();
		}
		else if(ins_code.matches("") && workType.getSelectedItemPosition()==0)
		{
			isValid=false;
			Toast.makeText(context, "institution code field value empty", Toast.LENGTH_LONG).show();			
		}
		else
		{
			if(hrGroup.getCheckedRadioButtonId()==R.id.other && zmMobile.getText().toString().matches(""))
			{
				Toast.makeText(context, "other zm mobile field empty", Toast.LENGTH_LONG).show();
				isValid=false;
			}
		}*/
		/*if(workType.getSelectedItemPosition()==0){
			isValid=false;
			workType.requestFocus();
		}*/
        int type = workType.getSelectedItemPosition();
        String message = "";
        if (transport.getSelectedItemPosition() == 0) {
            isValid = false;
            message = "Transport Not Selected";
        }

        if (etDatePicker.getText().toString().matches("")) {
            isValid = false;
            message = "No Date Value Set";
        }

        if (type == 0) {
            isValid = false;
            message = "Work Type Not Selected";
        }
        if (!message.matches("")) {
            Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
        if (insCode.getText().toString().matches("") && (type == 1 || type == 3 || type == 6)) {
            isValid = false;
            insCode.requestFocus();
            insCode.setError("Institution Code Value Empty");

        } else if (!insCode.getText().toString().matches("")) {
            String value = insCode.getText().toString();
            if (!insCodePrimary.containsKey(value)) {
                isValid = false;
                insCode.setError("No Instituation Found");
                insCode.requestFocus();
            }
        }

        if (cost.getText().toString().matches("")) {
            isValid = false;
            cost.setError("Cost Value Empty");
            cost.requestFocus();
        }
        if (is_offline && !etime.getText().toString().matches("") && !stime.getText().toString().matches("")) {
            String msg = "";
            if (!Global.compareTimeValue(etime.getText().toString(), stime.getText().toString())) {
                isValid = false;
                etime.requestFocus();
                msg = "End Time must be greater than start time";
                etime.setError(msg);

            }
            etime_value = Global.timeValueFromString(etDatePicker.getText().toString(), etime.getText().toString());
            if (etime_value > System.currentTimeMillis()) {
                isValid = false;
                msg = "End time is greater than current time";
                etime.requestFocus();
                etime.setError(msg);

            }
            if (!isValid) {
                Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }

        if (etime.getText().toString().matches("")) {
            isValid = false;
            etime.setError("End Time Not Set");
        }
        if (stime.getText().toString().matches("")) {
            isValid = false;
            stime.setError("Start Time Not Set");
        }

        if (locName.getText().toString().matches("")) {
            isValid = false;
            locName.setError("No Location Set");
            locName.requestFocus();
        }
        if (fromLocation.getText().toString().matches("")) {
            isValid = false;
            fromLocation.setError("No Location Set");
            fromLocation.requestFocus();
        }
        if (hrGroup.getCheckedRadioButtonId() == R.id.other) {
            String value = zmMobile.getText().toString();
            if (value.matches("")) {
                isValid = false;
                zmMobile.setError("Empty");
                zmMobile.requestFocus();
            } else if (!zmMobile.getText().toString().matches("^(\\+88){0,1}01[156789][0-9]{8}$")) {
                isValid = false;
                zmMobile.setError("Mobile Number Not Valid");
                zmMobile.requestFocus();
            } else if (!zmMobilePrimary.containsKey(value)) {
                isValid = false;
                zmMobile.setError("No ZM Found With This Mobile");
                zmMobile.requestFocus();
            }
        }

        return isValid;
    }

    public void timePicker(final EditText eReminderTime, final String txt_str) {
		/*eReminderTime.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(View v) {
	            // TODO Auto-generated method stub
	            Calendar mcurrentTime = Calendar.getInstance();
	            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
	            int minute = mcurrentTime.get(Calendar.MINUTE);
	            TimePickerDialog mTimePicker;
	            mTimePicker = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
	                @Override
	                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
	                    eReminderTime.setText( selectedHour + ":" + selectedMinute);
	                }
	            }, hour, minute, true);//Yes 24 hour time
	            mTimePicker.setTitle(txt_str);
	            mTimePicker.show();

	        }
	    });		*/
        master_data = getIntent().getStringArrayExtra("master");
        if (master_data != null)
            is_offline = master_data[19].matches("1");

        eReminderTime.setFocusable(false);
        if (is_offline) {
            eReminderTime.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    int hour, minute;
                    final Calendar mcurrentTime = Calendar.getInstance();
                    if (eReminderTime.getText().toString().matches("")) {
                        hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                        minute = mcurrentTime.get(Calendar.MINUTE);
                    } else {
                        long value_time = Global.timeValueFromString(etDatePicker.getText().toString(), eReminderTime.getText().toString());
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(value_time);
                        hour = cal.get(Calendar.HOUR_OF_DAY);
                        minute = cal.get(Calendar.MINUTE);
                    }

                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            mcurrentTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                            mcurrentTime.set(Calendar.MINUTE, selectedMinute);

                            SimpleDateFormat ff = new SimpleDateFormat("hh:mm a");
                            eReminderTime.setText(ff.format(new Date(mcurrentTime.getTimeInMillis())));

                            eReminderTime.setError(null);
                        }
                    }, hour, minute, false);//Yes 24 hour time

                    mTimePicker.setTitle(txt_str);
                    mTimePicker.show();
                }

            });
        }
    }

    public void checkforUpdate() {
        master_data = getIntent().getStringArrayExtra("master");
        if (master_data != null) {
            SimpleDateFormat formater = new SimpleDateFormat("hh:mm a");
            fromLocation.setText(master_data[8]);
            locName.setText(master_data[11]);

            etDatePicker.setText(master_data[18]);

            workType.setFocusable(false);
            workType.setClickable(false);

            stime_value = Long.parseLong(master_data[12]);
            stime.setText(formater.format(new Date(Long.parseLong(master_data[12]))));

            etime_value = Long.parseLong(master_data[13]);
            etime.setText(formater.format(new Date(Long.parseLong(master_data[13]))));

            is_ref_zm = Integer.parseInt(master_data[3]);

            if (is_ref_zm == 1) {
                ((RadioButton) (findViewById(R.id.other))).setChecked(true);
                zmMobile.setText(master_data[5]);
                ArrayList<String[]> data_s = Global.dbObject.queryFromTable("SEC_USERS", new String[]{"USER_FULL_NAME"}, "USER_NO=" + master_data[4]);
                if (data_s.size() != 0) {
                    zmName.setText(data_s.get(0)[0]);
                    zmNum.setText(master_data[5]);
                }
            }

            cost.setText(master_data[17]);

            if (insCodePrimaryToCode.containsKey(master_data[15]))
                insCode.setText(insCodePrimaryToCode.get(master_data[15]));

            comments.setText(master_data[20]);

            if (transportIDtoName.containsKey(master_data[16])) {
                int i;
                String tName = transportIDtoName.get(master_data[16]);
                for (i = 0; i < transPortName.size(); i++)
                    if (transPortName.get(i).matches(tName))
                        break;
                if (i < transPortName.size())
                    transport.setSelection(i);
                else
                    Log.d("msg", "No Transport Set");
            }

            workType.setSelection(Integer.parseInt(master_data[2]));

            if (!master_data[3].matches("1")) {
                zmInfo.setVisibility(LinearLayout.GONE);
            }

            save.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.save, 0, 0);
            save.setText("Update");

            String line = "";
            for (int i = 0; i < master_data.length; i++)
                line += master_data[i];
            Log.d("msg", "data " + line);
        } else
            Log.d("msg", "Sorry not Set");

    }

    public void clearAllField() {
        workType.setSelection(0);
        transport.setSelection(0);

        stime.setText("");
        etime.setText("");

        fromLocation.setText("");
        locName.setText("");

        hrGroup.check(R.id.self);
        insCode.setText("");
        comments.setText("");

        cost.setText("");
        zmInfo.setVisibility(LinearLayout.GONE);
        insnameLayout.setVisibility(LinearLayout.GONE);

        forOnline();
    }

    public long getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        return cal.getTimeInMillis();
    }

    public long getLongValue(int hours, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), hours, minutes);
        return cal.getTimeInMillis();
    }

    public String getCurrentTimeInString() {
        SimpleDateFormat mformatter = new SimpleDateFormat("hh:mm");
        return mformatter.format(new Date(Calendar.getInstance().getTimeInMillis()));
    }

    public void insertDCRMaster() {
        stime_value = Global.timeValueFromString(etDatePicker.getText().toString(), stime.getText().toString());
        etime_value = Global.timeValueFromString(etDatePicker.getText().toString(), etime.getText().toString());

        ContentValues dcr_master_value = new ContentValues();
        //primary_id= Global.dbObject.getLastRowID("TRN_DCR","OFFLINE_DCR_NO")+1;
        primary_id = Global.getPrimaryKeyID();
        Global.setPrimaryKeyID(primary_id + 1);

        dcr_master_value.put("OFFLINE_DCR_NO", primary_id);
        dcr_master_value.put("ACTION_OFFLINE_TIME", getCurrentTime());
        dcr_master_value.put("DCR_TYPE_NO", workType.getSelectedItemPosition());
        dcr_master_value.put("IS_REF_ZM", is_ref_zm);

        String key;
        if (is_ref_zm == 1) {
            key = zmMobile.getText().toString();
            if (!key.matches("") && zmMobilePrimary.containsKey(key)) {
                dcr_master_value.put("REF_ZM_USER_NO", zmMobilePrimary.get(key));
                dcr_master_value.put("REF_ZM_MOBILE", zmMobile.getText().toString());
            }
        }

        dcr_master_value.put("WORK_AREA_FROM_LAT", fromLocLat);
        dcr_master_value.put("WORK_AREA_FROM_LON", fromLocLon);
        dcr_master_value.put("WORK_AREA_FROM_NAME", Global.CapitalizeWords(fromLocation.getText().toString()));

        dcr_master_value.put("WORK_AREA_TO_LAT", toLocLat);
        dcr_master_value.put("WORK_AREA_TO_LON", toLocLon);
        dcr_master_value.put("WORK_AREA_TO_NAME", Global.CapitalizeWords(locName.getText().toString()));

        dcr_master_value.put("TIME_FROM", stime_value);
        dcr_master_value.put("TIME_TO", etime_value);
        dcr_master_value.put("TIME_GAP", etime_value - stime_value);

        SharedPreferences prefs = getSharedPreferences("MY_PREFS", 0);
        dcr_master_value.put("USER_NO", prefs.getString("user_no", ""));

        key = insCode.getText().toString();
        if (!key.matches("") && insCodePrimary.containsKey(key))
            dcr_master_value.put("INSTITUTE_NO", insCodePrimary.get(key));

        String tID = "1";
        if (transportIDS.containsKey(transport.getSelectedItem()))
            tID = transportIDS.get(transport.getSelectedItem());

        dcr_master_value.put("TRANS_TYPE_NO", tID);

        dcr_master_value.put("FARE_AMT", cost.getText().toString());
        dcr_master_value.put("TRN_DCR_DATE", etDatePicker.getText().toString());
        if (is_offline)
            dcr_master_value.put("IS_MANUAL_ENTRY", 1);
        dcr_master_value.put("COMMENTS", comments.getText().toString());

        Editor editor = prefs.edit();
        editor.putLong("DCR_PRIMARY", primary_id);
        editor.commit();

        Global.dbObject.insertIntoTable("TRN_DCR", dcr_master_value);
    }

    public void updateDCRMaster() {
        stime_value = Global.timeValueFromString(etDatePicker.getText().toString(), stime.getText().toString());
        etime_value = Global.timeValueFromString(etDatePicker.getText().toString(), etime.getText().toString());

        ContentValues dcr_master_value = new ContentValues();
        //dcr_master_value.put("OFFLINE_DCR_NO", primary_id++);

        //dcr_master_value.put("ACTION_OFFLINE_TIME", getCurrentTime());
        dcr_master_value.put("DCR_TYPE_NO", workType.getSelectedItemPosition());
        dcr_master_value.put("IS_REF_ZM", is_ref_zm);


        String key;
        if (is_ref_zm == 1) {
            key = zmMobile.getText().toString();
            if (!key.matches("") && zmMobilePrimary.containsKey(key)) {
                dcr_master_value.put("REF_ZM_USER_NO", zmMobilePrimary.get(key));
                dcr_master_value.put("REF_ZM_MOBILE", key);
            }
        } else {
            dcr_master_value.put("REF_ZM_USER_NO", "");
            dcr_master_value.put("REF_ZM_MOBILE", "");
        }

        dcr_master_value.put("WORK_AREA_FROM_NAME", Global.CapitalizeWords(fromLocation.getText().toString()));
        dcr_master_value.put("WORK_AREA_TO_NAME", Global.CapitalizeWords(locName.getText().toString()));
        dcr_master_value.put("TIME_FROM", stime_value);
        dcr_master_value.put("TIME_TO", etime_value);
        dcr_master_value.put("TIME_GAP", etime_value - stime_value);

        key = insCode.getText().toString();
        if (!key.matches("") && insCodePrimary.containsKey(key))
            dcr_master_value.put("INSTITUTE_NO", insCodePrimary.get(key));
        else
            dcr_master_value.put("INSTITUTE_NO", "");

        String tID = "1";
        if (transportIDS.containsKey(transport.getSelectedItem()))
            tID = transportIDS.get(transport.getSelectedItem());

        dcr_master_value.put("TRANS_TYPE_NO", tID);

        dcr_master_value.put("FARE_AMT", Double.parseDouble(cost.getText().toString()));
        dcr_master_value.put("COMMENTS", comments.getText().toString());

        if (master_data[22].matches("1")) {
            dcr_master_value.put("IS_UPLOADED", "0");
            dcr_master_value.put("ENTRY_STATE", "2");
            dcr_master_value.put("IS_COMPLETE_NEW", "0");
        }


        Global.dbObject.updateIntoTable("TRN_DCR", dcr_master_value, "OFFLINE_DCR_NO=" + master_data[0]);
        Log.d("msg_db", "Master Detail is updated");
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        //clearAllField();

        Location location = Global.currentLocation();
        if (location != null) {
            toLocLat = Double.toString(location.getLatitude());
            toLocLon = Double.toString(location.getLongitude());
        } else
            toLocLon = toLocLat = "";

        if (master_data == null && !is_offline) {
            SimpleDateFormat formater = new SimpleDateFormat("hh:mm a");
            etime.setText(formater.format(Calendar.getInstance().getTimeInMillis()));
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
            Intent intent = new Intent(dcr_master.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void initInstitutionCodes() {
		/*insCodeData.add(new String("Notre Dame College(1001)"));
		insCodeData.add(new String("Dhaka College(1002)"));
		insCodeData.add(new String("Dhaka City College(1003)"));
		insCodeData.add(new String("Rifles Public College(1004)"));
		insCodeData.add(new String("Rajuk Uttara Model College(1005)"));*/
        //ArrayList<String []> specData=Global.dbObject.queryFromTable("SET_INSTITUTE", new String[]{"F_INSTITUTION_DB_ID","INSTITUTE_NAME","INSTITUTE_NO"},null);
        ArrayList<String[]> specData = Global.dbObject.queryFromTable("SET_INSTITUTE", new String[]{"INSTITUTE_NO", "INSTITUTE_NAME", "INSTITUTE_NO"}, null);
        for (int i = 0; i < specData.size(); i++) {
            insCodeData.add(new String(specData.get(i)[1] + "(" + specData.get(i)[0] + ")"));
            insCodePrimary.put(specData.get(i)[0], specData.get(i)[2]);
            insCodePrimaryToCode.put(specData.get(i)[2], specData.get(i)[0]);
        }

    }

    public void initZmMobiles() {
		/*zmMobileData.add(new String("Abdul(01456978901)"));
		zmMobileData.add(new String("Malek(01957928945)"));
		zmMobileData.add(new String("Afridi(0180978901)"));
		zmMobileData.add(new String("Majed(01854516218)"));
		zmMobileData.add(new String("Robin(01756778901)"));*/
        ArrayList<String[]> data_s = Global.dbObject.queryFromTable("SEC_USERS", new String[]{"USER_MOBILE", "USER_FULL_NAME", "USER_NO"}, null);
        for (int i = 0; i < data_s.size(); i++) {
            zmMobileData.add(new String(data_s.get(i)[1] + "(" + data_s.get(i)[0] + ")"));
            zmMobilePrimary.put(data_s.get(i)[0], data_s.get(i)[2]);
        }
    }

    private TextWatcher inscodeWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
            // TODO Auto-generated method stub
        }

        @Override
        public void afterTextChanged(Editable arg0) {
            if (insCodePrimary.containsKey(arg0.toString())) {
                insnameLayout.setVisibility(View.VISIBLE);
                ArrayList<String[]> data_s = Global.dbObject.queryFromTable("SET_INSTITUTE", new String[]{"INSTITUTE_NAME"}, "INSTITUTE_NO=" + arg0.toString());
                if (data_s.size() != 0) {
                    insName.setText(data_s.get(0)[0]);
                }
            } else {
                insnameLayout.setVisibility(LinearLayout.GONE);
            }
        }
    };
    private TextWatcher zmMobileWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
            // TODO Auto-generated method stub
        }

        @Override
        public void afterTextChanged(Editable arg0) {
            if (zmMobilePrimary.containsKey(arg0.toString())) {
                zmInfo.setVisibility(View.VISIBLE);
                ArrayList<String[]> data_s = Global.dbObject.queryFromTable("SEC_USERS", new String[]{"USER_FULL_NAME"}, "USER_NO=" + zmMobilePrimary.get(arg0.toString()));
                if (data_s.size() != 0) {
                    zmName.setText(data_s.get(0)[0]);
                    zmNum.setText(arg0.toString());
                } else {
                    zmInfo.setVisibility(LinearLayout.GONE);
                }
            } else {
                zmInfo.setVisibility(LinearLayout.GONE);
            }
        }
    };
}
