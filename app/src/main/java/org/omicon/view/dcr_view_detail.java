package org.omicon.view;

import java.util.ArrayList;

import org.omicon.R;
import org.omicon.entry.EntryActivity;
import org.omicon.initial.Global;
import org.omicon.initial.HomeActivity;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class dcr_view_detail extends Activity {
    private String[] master_data;
    private String type = "";
    private String[] workType = {"Specimen Distribution",
            "Marketing Promotion", "Teacher Contact", "Library Contact",
            "Official Work", "CSR Specimen"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dcr_view_detail);

        master_data = getIntent().getStringArrayExtra("master");

        final String workTypeCode[] = getResources().getStringArray(
                R.array.worktype_arrays);

        if (master_data == null)
            Log.d("msg", "master data null");
        else
            Log.d("msg", "Size of mater data:" + master_data.length);

        TextView tv = (TextView) findViewById(R.id.detailViewType);
        tv.setText(workType[Integer.parseInt(master_data[2]) - 1]);
        tv = (TextView) findViewById(R.id.detailViewLocation);
        tv.setText(master_data[11]);

        // new detail entry
        Button newDcrDetail = (Button) findViewById(R.id.addNewDCRDetail);
        newDcrDetail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (master_data != null && !master_data[2].matches("5")) {
                    Intent intent = new Intent(dcr_view_detail.this,
                            EntryActivity.class);
                    intent.putExtra("MASTER_ID", Long.parseLong(master_data[0]));
                    if (!master_data[2].matches("6"))
                        intent.putExtra("workType",
                                workTypeCode[Integer.parseInt(master_data[2])]);
                    else
                        intent.putExtra("workType", workTypeCode[1]);

                    startActivityForResult(intent, 0);
                }
            }
        });

        if (!master_data[2].matches("1") && !master_data[2].matches("2")
                && !master_data[2].matches("6")) {
            ((LinearLayout) findViewById(R.id.itemInfoHeader1))
                    .setVisibility(LinearLayout.GONE);
            ((LinearLayout) findViewById(R.id.quantityInfoHeader1))
                    .setVisibility(LinearLayout.GONE);
        }

        ListView listView = (ListView) findViewById(R.id.detailListView);
        String tmp = master_data[0];
        ArrayList<String[]> data = new ArrayList<String[]>();
        // if(!master_data[2].matches("2"))
        data = Global.dbObject.queryFromTable("TRN_DCR_DET", null,
                "ENTRY_STATE<>3 AND OFFLINE_DCR_NO=" + master_data[0]);
        /*
		 * else data=Global.dbObject.queryFromTable("TRN_DCR_DET", null,
		 * "ENTRY_STATE<>3 AND OFFLINE_DCR_NO="+master_data[0]);
		 */
        TextView noRecord = (TextView) findViewById(R.id.noRecord);
        if (data.size() != 0) {
            noRecord.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
        } else {
            noRecord.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
        }

        listView.setAdapter(new MyDetailListAdapter(this, data));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        Intent intent = new Intent(this, dcr_view_detail.class);
        intent.putExtra("master", master_data);
        startActivity(intent);
        this.finish();
    }

    public void childAlertDialogYesorNo(final String[] item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Detail Entry");
        builder.setMessage("Do you want to delete ?");
        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Log.d("msg", "Enter for Detail Entry Delete ... ");
                deleteDCRMaster("TRN_DCR_DET", "OFFLINE_DCR_DET_NO=" + item[0]);

                ContentValues entry_values = new ContentValues();
                entry_values.put("IS_UPLOADED", "0");
                Global.dbObject.updateIntoTable("TRN_DCR", entry_values,
                        "OFFLINE_DCR_NO=" + item[1]);
            }
        });
        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        builder.create().show();
    }

    public void deleteDCRMaster(String table_name, String query) {
        ArrayList<String[]> master_data1 = Global.dbObject.queryFromTable(
                table_name, new String[]{"IS_UPLOADED", "IS_COMPLETE_NEW"},
                query);
        if (master_data1.get(0)[0].matches("1")) {
            ContentValues values = new ContentValues();
            values.put("ENTRY_STATE", "3");
            values.put("IS_UPLOADED", "0");
            values.put("IS_COMPLETE_NEW", "0");

            Global.dbObject.updateIntoTable(table_name, values, query);
        } else {
            if (master_data1.get(0)[1].matches("1"))
                Global.dbObject.deleteFromTable(table_name, query);
            else {
                ContentValues values = new ContentValues();
                values.put("ENTRY_STATE", "3");
                values.put("IS_UPLOADED", "0");
                values.put("IS_COMPLETE_NEW", "0");

                Global.dbObject.updateIntoTable(table_name, values, query);
            }
        }

        Intent intent = new Intent(this, dcr_view_detail.class);

        if (master_data == null)
            Log.d("msg", "DELETE DCR MASTER master_data null");
        else
            Log.d("msg", "DELETE DCR MASTER master_data ok" + master_data[2]);

        intent.putExtra("master", master_data);
        startActivity(intent);
        this.finish();
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
            Intent intent = new Intent(dcr_view_detail.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyDetailListAdapter extends ArrayAdapter<String[]> {
        private Context context;
        private ArrayList<String[]> data;

        public MyDetailListAdapter(Context con, ArrayList<String[]> d) {
            super(con, R.layout.dcr_view_detail_list_item, d);
            context = con;
            data = d;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.dcr_view_detail_list_item,
                    parent, false);

            TextView codeItem = (TextView) rowView.findViewById(R.id.codeItem);
            TextView codeItemName = (TextView) rowView
                    .findViewById(R.id.codeItemName);
            TextView quantityItem = (TextView) rowView
                    .findViewById(R.id.quantityItem);
            TextView agentName = (TextView) rowView
                    .findViewById(R.id.agentName);
            TextView agentMobile = (TextView) rowView
                    .findViewById(R.id.agentMobile);
            TextView agentType = (TextView) rowView
                    .findViewById(R.id.agentType);

            ImageButton addButton = (ImageButton) rowView
                    .findViewById(R.id.addButton1);
            ImageButton deleteButton = (ImageButton) rowView
                    .findViewById(R.id.deleteButton1);

            LinearLayout itemLayout = (LinearLayout) rowView
                    .findViewById(R.id.itemInfo);
            LinearLayout quantityLayout = (LinearLayout) rowView
                    .findViewById(R.id.quantityInfo);

            if (!master_data[2].matches("1") && !master_data[2].matches("2")
                    && !master_data[2].matches("6")) {
                itemLayout.setVisibility(LinearLayout.GONE);
                quantityLayout.setVisibility(LinearLayout.GONE);
            }

            if (data.get(position)[3].matches("1")) {
                // agentType.setText("Teacher");
                agentMobile.setText(data.get(position)[19] + "("
                        + data.get(position)[5] + ")");

                ArrayList<String[]> teacherName = Global.dbObject
                        .queryFromTable("SET_TEACHER_INFO",
                                new String[]{"TEACHER_NAME"},
                                "TEACHER_MOBILE=" + data.get(position)[5]);
                if (teacherName.size() != 0)
                    agentName.setText(teacherName.get(0)[0]);
            } else {
                // agentType.setText("Client");
                agentMobile.setText(data.get(position)[10]);

                ArrayList<String[]> clientName = Global.dbObject
                        .queryFromTable("SET_CLIENT_INFO",
                                new String[]{"CLIENT_NAME"},
                                "CLIENT_MOBILE=" + data.get(position)[10]);
                if (clientName.size() != 0)
                    agentName.setText(clientName.get(0)[0]);
            }

            agentName.setTextAppearance(context, R.style.viewTitlevalue);
            codeItem.setTextAppearance(context, R.style.viewTitlevalue);

            if (data.get(position)[16].matches("1")) {
                agentType.setVisibility(View.VISIBLE);
                agentType.setText(data.get(position)[17] + "(OnBehalf)");
            }

            // here codeItemName set
            if (master_data[2].matches("1") || master_data[2].matches("6")) {
                codeItem.setText(data.get(position)[6]);
                quantityItem.setText(data.get(position)[7]);
                ArrayList<String[]> bookName = Global.dbObject.queryFromTable(
                        "SET_SPECIMEN", new String[]{"SPECIMEN_NAME"},
                        "SPECIMEN_NO=" + data.get(position)[6]);
                if (bookName.size() != 0)
                    codeItemName.setText(bookName.get(0)[0]);
            } else if (master_data[2].matches("2")) {
                codeItem.setText(data.get(position)[11]);
                quantityItem.setText(data.get(position)[12]);
                ArrayList<String[]> bookName = Global.dbObject.queryFromTable(
                        "SET_PROMO_ITEM", new String[]{"PROMO_ITEM_NAME"},
                        "PROMO_ITEM_NO=" + data.get(position)[11]);
                if (bookName.size() != 0)
                    codeItemName.setText(bookName.get(0)[0]);
            }

            String action_date = Global.getDateValue(Long
                    .parseLong(master_data[1]));

            if (!action_date.matches(Global.currentDate()))
                deleteButton.setVisibility(View.GONE);
            deleteButton.setFocusable(false);
            deleteButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    childAlertDialogYesorNo(data.get(position));
                }
            });

            if (!action_date.matches(Global.currentDate()))
                addButton.setVisibility(View.GONE);
            addButton.setFocusable(false);
            addButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("msg", "Child Add button clicked");

                    Intent intent = new Intent(context, EntryActivity.class);

                    if (master_data[2].matches("1")
                            || master_data[2].matches("6"))
                        intent.putExtra("workType", "SD-Specimen Distribution");
                    else if (master_data[2].matches("2"))
                        intent.putExtra("workType", "MP-Marketing Promotion");
                    else if (master_data[2].matches("3"))
                        intent.putExtra("workType", "TC-Teacher Contact");
                    else if (master_data[2].matches("4"))
                        intent.putExtra("workType", "LC-Library Contact");
                    else
                        intent.putExtra("workType", "OW-Official Work");

                    intent.putExtra("child", data.get(position));
                    ((Activity) context).startActivityForResult(intent, 1);
                }
            });
            return rowView;
        }
    }
}
