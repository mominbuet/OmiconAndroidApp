package org.omicon.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.omicon.R;
import org.omicon.entry.EntryActivity;
import org.omicon.initial.Global;
import org.omicon.initial.HomeActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class UserLocation extends Activity {
    LocationListAdapter adapter;
    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        list = (ListView) findViewById(R.id.listLocation);

        final EditText et = (EditText) findViewById(R.id.locationName);
        Button b = (Button) findViewById(R.id.saveLocation);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Location location = Global.currentLocation();
                if (location == null) {
                    showToastMessage("GPS Value Not Found");
                    Log.d("msg", "GPS Value Not Found");
                } else if (et.getText().toString().matches(""))
                    showToastMessage("Location Name Empty");
                else {
                    Log.d("msg", "Save Location:" + location);
                    ContentValues values = new ContentValues();

                    long id = Global.getPrimaryKeyID();
                    Global.setPrimaryKeyID(id + 1);

                    values.put("OFFLINE_LOC_NO", id);
                    values.put("ACTION_OFFLINE_TIME", Calendar.getInstance().getTimeInMillis());
                    values.put("LAT_VAL", location.getLatitude());
                    values.put("LON_VAL", location.getLongitude());
                    values.put("LOCATION_NAME", et.getText().toString());

                    SharedPreferences prefs = getSharedPreferences("MY_PREFS", 0);
                    values.put("USER_NO", prefs.getString("user_no", ""));

                    Global.dbObject.insertIntoTable("TRN_USER_LOCATION", values);

                    //Global.insertNewLocation(location,et.getText().toString());

                    loadDataToList();
                    et.setText("");
                    showToastMessage("Added");
                }
            }
        });

        loadDataToList();
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
            Intent intent = new Intent(UserLocation.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void showToastMessage(String message) {
        Toast toast = Toast.makeText(UserLocation.this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void loadDataToList() {
        SharedPreferences prefs = getSharedPreferences("MY_PREFS", 0);

        ArrayList<String[]> data_s = Global.dbObject.queryFromTable("TRN_USER_LOCATION", null, "ENTRY_STATE<>3 AND USER_NO=" + prefs.getString("user_no", ""));
        adapter = new LocationListAdapter(this, data_s);
        list.setAdapter(adapter);
    }

    public void editLocationDialog(final String[] data_item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserLocation.this);
        builder.setMessage("Edit Location Name");

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.location_edit, null);
        final EditText et = (EditText) view.findViewById(R.id.locationName);
        et.setText(data_item[4]);

        builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (et.getText().toString().matches("")) {
                } else {
                    ContentValues value = new ContentValues();
                    value.put("LOCATION_NAME", et.getText().toString());
                    //value.put("ACTION_OFFLINE_TIME",Calendar.getInstance().getTimeInMillis());
                    if (data_item[6].matches("1")) {
                        value.put("ENTRY_STATE", "2");
                        value.put("IS_UPLOADED", "0");
                        value.put("IS_COMPLETE_NEW", "0");
                    }
                    Global.dbObject.updateIntoTable("TRN_USER_LOCATION", value, "OFFLINE_LOC_NO=" + data_item[0]);

                    loadDataToList();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setView(view);
        builder.create().show();

    }

    class LocationListAdapter extends ArrayAdapter<String[]> {
        private Context context;
        private ArrayList<String[]> data;

        public LocationListAdapter(Context con, ArrayList<String[]> d) {
            super(con, R.layout.location_item, d);
            data = d;
            context = con;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.location_item, parent, false);

            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy\nhh:mm a");

            TextView name = (TextView) rowView.findViewById(R.id.locationEntryName);
            TextView time = (TextView) rowView.findViewById(R.id.locationEntryTime);
            name.setText(data.get(position)[4]);
            time.setText(formatter.format(Long.parseLong(data.get(position)[1])));

            ImageButton edit = (ImageButton) rowView.findViewById(R.id.editLocation);
            edit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    editLocationDialog(data.get(position));
                }
            });
            ImageButton remove = (ImageButton) rowView.findViewById(R.id.removeLocation);
            remove.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete Message");
                    builder.setMessage("Are you sure want to delete?");
                    builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            Log.d("msg", "Item Position:" + position);

                            String[] data_item = data.get(position);
                            if (data_item[6].matches("1")) {
                                ContentValues value = new ContentValues();
                                value.put("ENTRY_STATE", "3");
                                value.put("IS_UPLOADED", "0");
                                value.put("IS_COMPLETE_NEW", "0");
                                Global.dbObject.updateIntoTable("TRN_USER_LOCATION", value, "OFFLINE_LOC_NO=" + data_item[0]);
                            } else {
                                if (data_item[7].matches("1"))
                                    Global.dbObject.deleteFromTable("TRN_USER_LOCATION", "OFFLINE_LOC_NO=" + data_item[0]);
                                else {
                                    ContentValues value = new ContentValues();
                                    value.put("ENTRY_STATE", "3");
                                    value.put("IS_UPLOADED", "0");
                                    value.put("IS_COMPLETE_NEW", "0");
                                    Global.dbObject.updateIntoTable("TRN_USER_LOCATION", value, "OFFLINE_LOC_NO=" + data_item[0]);
                                }
                            }
                            loadDataToList();
                        }
                    });
                    builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                        }
                    });
                    builder.create().show();
                }
            });


            return rowView;
        }
    }
}
