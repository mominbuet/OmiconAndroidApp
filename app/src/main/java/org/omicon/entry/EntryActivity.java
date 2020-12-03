package org.omicon.entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.omicon.R;
import org.omicon.initial.Global;
import org.omicon.initial.HomeActivity;
import org.omicon.initial.logger;
import org.omicon.view.dcr_view_detail;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class EntryActivity extends Activity {
    LinearLayout teacherInfo, behalfMobileInfo;
    EditText quantity, onBehalfMobile, behalfNickname;
    AutoCompleteTextView code, mobile, nick;
    TextView bookName, teacherName, teacherMobile;
    TextView codeText, mobileText, quantityText;
    CheckBox onBehalf;

    RadioGroup copyType;
    Button save;

    ArrayList<EntryItem> data = new ArrayList<EntryItem>();
    Context context;
    ListView list;
    ArrayList<String> codeIds = new ArrayList<String>();
    ArrayList<String> teacherMobiles = new ArrayList<String>();
    ArrayList<String> teacherTable = new ArrayList<String>();
    ArrayList<String> clientTable = new ArrayList<String>();

    Map<String, String> codePrimary = new HashMap<String, String>();
    //Map<String,String> primaryToCode=new HashMap<String, String>();
    Map<String, String> teacherPrimary = new HashMap<String, String>();

    String workType;
    TableLayout tableInfos;

    long dcr_no;
    int is_teacher = 1;
    String[] child_item = null;
    boolean isMobileValid = false, nickvalid = false;
    boolean isOnBehalfMobileValid = false, isOnBehalfNickValid = false;
    ;
    String USER_NO, ADD_NEW_DETAIL;
    int is_first_detail = 0;
    ArrayList<String> phoneNos;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_entry);
        phoneNos = new ArrayList<String>();
        workType = getIntent().getStringExtra("workType");
        dcr_no = getIntent().getLongExtra("MASTER_ID", -1);

        if (dcr_no != -1) {
            ContentValues entry_values = new ContentValues();
            entry_values.put("IS_UPLOADED", "0");
            Global.dbObject.updateIntoTable("TRN_DCR", entry_values, "OFFLINE_DCR_NO=" + dcr_no);
        }

        SharedPreferences prefs = getSharedPreferences("MY_PREFS", 0);
        USER_NO = prefs.getString("user_no", "");

        Log.d("msg", "Work Type::" + workType + " dcr_no:" + dcr_no);

        context = this;
        initCodeIds();
        initTeacherMobiles();
        initiaLisation();

        teacherInfo.setVisibility(LinearLayout.GONE);
        behalfMobileInfo.setVisibility(LinearLayout.GONE);
        bookName.setVisibility(View.INVISIBLE);

		/*list=(ListView) findViewById(R.id.listview);
        EntryAdapter adapter=new EntryAdapter(this, data,list,workType);
		list.setAdapter(adapter);
		*/

        tableInfos = (TableLayout) findViewById(R.id.showInfo);
        TableRow rowHeader = (TableRow) LayoutInflater.from(this).inflate(R.layout.table_row_header, null);
        ((LinearLayout) rowHeader.findViewById(R.id.headerImage)).setVisibility(LinearLayout.GONE);
        if (!workType.matches("SD-Specimen Distribution") && !workType.matches("MP-Marketing Promotion")) {
            ((LinearLayout) rowHeader.findViewById(R.id.itemInfoHeader)).setVisibility(LinearLayout.GONE);
            ((LinearLayout) rowHeader.findViewById(R.id.quantityInfoHeader)).setVisibility(LinearLayout.GONE);
        }
        tableInfos.addView(rowHeader);

        bindListener();

        checkforupdate();

        if (!workType.matches("SD-Specimen Distribution") && !workType.matches("MP-Marketing Promotion")) {
            ((LinearLayout) findViewById(R.id.entryInfo)).setVisibility(LinearLayout.GONE);

            ((LinearLayout) findViewById(R.id.onBehalfMobileLayout)).setVisibility(LinearLayout.GONE);
        }
        if (workType.matches("SD-Specimen Distribution") || workType.matches("TC-Teacher Contact")) {
            ((LinearLayout) findViewById(R.id.agentType)).setVisibility(LinearLayout.GONE);
        }
        if (workType.matches("MP-Marketing Promotion")) {
            //code.setHint("Promotional Item No.");
            codeText.setText("Promotional Item No.");
            //mobile.setHint("Mobile NO.");
            mobileText.setText("Mobile NO.");
            quantityText.setText("Quantity/Amount");

        } else if (workType.matches("LC-Library Contact")) {
            //mobile.setHint("Client Mobile No.");
            mobileText.setText("Client Mobile No.");
        }
    }

    public void bindListener() {
        SuggestionAdapter sadapter = new SuggestionAdapter(context, R.layout.suggestion_label, codeIds);
        code.setAdapter(sadapter);
        code.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(android.widget.AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String str = (String) arg0.getItemAtPosition(arg2);
                String code_str = str.substring(str.lastIndexOf("(") + 1, str.lastIndexOf(")"));
                code.setText(code_str);
                code.setSelection(code_str.length());

                bookName.setText(str.substring(0, str.lastIndexOf("(")));
                bookName.setVisibility(View.VISIBLE);
            }

            ;
        });
        code.setThreshold(1);

        sadapter = new SuggestionAdapter(context, R.layout.suggestion_label, teacherMobiles);
        mobile.setAdapter(sadapter);
        mobile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(android.widget.AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String str = (String) arg0.getItemAtPosition(arg2);
                String code_str = str.substring(str.lastIndexOf("(") + 1, str.lastIndexOf(")"));
                mobile.setText(code_str);
                mobile.setSelection(code_str.length());

                teacherInfo.setVisibility(LinearLayout.VISIBLE);
                teacherName.setText(str.substring(0, str.lastIndexOf("(")));
                teacherMobile.setText(code_str);
            }

            ;
        });

        mobile.addTextChangedListener(mobileWatcher);
        code.addTextChangedListener(codeWatcher);

        save = (Button) findViewById(R.id.submit);
        save.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (!mobile.getText().toString().matches("^(\\+88){0,1}01[156789][0-9]{8}$")) {
                    mobile.setError("Phone Number is not Valid");
                    isMobileValid = false;
                } else {
                    isMobileValid = true;
                    mobile.setError(null);
                }
                if (nick.getText().length() < 3) {
                    nick.setError("Nick name must be 3 characters long");
                    nickvalid = false;
                } else {
                    nickvalid = true;
                    mobile.setError(null);
                }
                if (onBehalf.isChecked()) {
                    if (behalfNickname.getText().length() < 3) {
                        behalfNickname.setError("On behalf nickname is too short");
                        isOnBehalfNickValid = false;
                    } else {
                        isOnBehalfNickValid = true;
                        behalfNickname.setError(null);

                    }
                    if (!onBehalfMobile.getText().toString().matches("^(\\+88){0,1}01[156789][0-9]{8}$")) {
                        onBehalfMobile.setError("OnBehalf Phone no is not Valid");
                        isOnBehalfMobileValid = false;
                    } else {
                        isOnBehalfMobileValid = true;
                        onBehalfMobile.setError(null);
                    }
                }


                if (checkValidity()) {
                    if (child_item == null) {
                        ((Activity) context).runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                if ((workType.matches("TC-Teacher Contact") || workType.matches("LC-Library Contact")))
                                    data.add(new EntryItem("", "", "", teacherName.getText().toString(), mobile.getText().toString(), nick.getText().toString()));
                                else
                                    data.add(new EntryItem(code.getText().toString(), bookName.getText().toString(),
                                            quantity.getText().toString(), teacherName.getText().toString(), mobile.getText().toString(), nick.getText().toString()));
                                /*EntryAdapter adapter=(EntryAdapter) list.getAdapter();
                                adapter.notifyDataSetChanged();*/
                                addRow();
                                insertEntry();

                                ((EntryActivity) context).allClear();
                                mobile.requestFocus();
                            }
                        });
                        Log.d("msg", "Data Entered In TRN_DCR_DET");
                    } else {
                        updateChildEntry();
                        finish();
                    }
                } else {
                    Toast toast = Toast.makeText(EntryActivity.this, "Validation Failed", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    Log.d("msg", "No Entry in the TRN_DCR_DET");
                }
            }
        });

        copyType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                if (arg0.getCheckedRadioButtonId() == R.id.teacherType) {
                    is_teacher = 1;
                    teacherMobiles = teacherTable;
                } else {
                    is_teacher = 0;
                    teacherMobiles = clientTable;
                }
                SuggestionAdapter nwAdapter = new SuggestionAdapter(context, R.layout.suggestion_label, teacherMobiles);
                mobile.setAdapter(nwAdapter);
            }
        });

        onBehalf.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    behalfMobileInfo.setVisibility(View.VISIBLE);
                } else
                    behalfMobileInfo.setVisibility(LinearLayout.GONE);
            }
        });

        mobile.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View arg0, boolean hasFocus) {
                if (!hasFocus && !mobile.getText().toString().matches("")) {
                    if (!mobile.getText().toString().matches("^(\\+88){0,1}01[0-9]{9}$")) {
                        mobile.setError("Phone Number Is not Valid");
                        isMobileValid = false;
                    } else
                        isMobileValid = true;
                }
            }
        });
        onBehalfMobile.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View arg0, boolean hasFocus) {
                if (!hasFocus && !onBehalfMobile.getText().toString().matches("")) {
                    if (!onBehalfMobile.getText().toString().matches("^(\\+88){0,1}01[0-9]{9}$")) {
                        onBehalfMobile.setError("Phone Number Is not Valid");
                        isOnBehalfMobileValid = false;
                    } else
                        isOnBehalfMobileValid = true;
                }
            }
        });

    }

    public void addRow() {
        TableRow rowView = (TableRow) LayoutInflater.from(context).inflate(R.layout.table_row_sd_pm, null);

        TextView codeItem = (TextView) rowView.findViewById(R.id.codeItem);
        TextView codeItemName = (TextView) rowView.findViewById(R.id.codeItemName);
        TextView quantityItem = (TextView) rowView.findViewById(R.id.quantityItem);
        TextView agentName = (TextView) rowView.findViewById(R.id.agentName);
        TextView agentMobile = (TextView) rowView.findViewById(R.id.agentMobile);
        TextView agentType = (TextView) rowView.findViewById(R.id.agentType);

        ImageButton addButton = (ImageButton) rowView.findViewById(R.id.addButton1);
        ImageButton deleteButton = (ImageButton) rowView.findViewById(R.id.deleteButton1);

        LinearLayout itemLayout = (LinearLayout) rowView.findViewById(R.id.itemInfo);
        LinearLayout quantityLayout = (LinearLayout) rowView.findViewById(R.id.quantityInfo);

        if (!workType.matches("SD-Specimen Distribution") && !workType.matches("MP-Marketing Promotion")) {
            itemLayout.setVisibility(LinearLayout.GONE);
            quantityLayout.setVisibility(LinearLayout.GONE);
        }

        codeItem.setText(code.getText().toString());
        codeItemName.setText(bookName.getText().toString());
        quantityItem.setText(quantity.getText().toString());
        agentName.setText(teacherName.getText().toString());
        agentMobile.setText(nick.getText().toString() + "(" + mobile.getText().toString() + ")");

        if (is_teacher == 1)
            agentType.setText("Teacher");
        else
            agentType.setText("Client");

        LinearLayout buttonLayout = (LinearLayout) rowView.findViewById(R.id.rowButton);
        buttonLayout.setVisibility(LinearLayout.GONE);

        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("msg", "delete button clicked");
                ViewGroup grp = (ViewGroup) v.getParent().getParent();
                if (grp == null) {
                    Log.d("View Parent ", "This is Null");
                }
                tableInfos.removeView(grp);
            }
        });
        addButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("msg", "Add button clicked");

                ViewGroup grp = (ViewGroup) v.getParent().getParent();
                if (grp == null) {
                    Log.d("View Parent ", "This is Null");
                }
                tableInfos.removeView(grp);
            }
        });
        tableInfos.addView(rowView);

    }

    public void updateChildEntry() {
        ContentValues entry_values = new ContentValues();

        entry_values.put("ACTION_OFFLINE_TIME", getCurrentTime());
        entry_values.put("IS_FOR_TEACHER", is_teacher);
        if (is_teacher == 1) {
            entry_values.put("CLIENT_NO", "");
            entry_values.put("CLIENT_MOBILE", "");

            entry_values.put("IS_FOR_CLIENT", "0");
            entry_values.put("TEACHER_NO", "");
            entry_values.put("TEACHER_NICK", nick.getText().toString());
        } else {
            entry_values.put("TEACHER_NO", "");
            entry_values.put("TEACHER_MOBILE", "");

            entry_values.put("IS_FOR_CLIENT", "1");
            entry_values.put("CLIENT_NO", "");
            entry_values.put("CLIENT_MOBILE", mobile.getText().toString());
        }

        if (workType.matches("SD-Specimen Distribution")) {
            //entry_values.put("SPECIMEN_NO",codePrimary.get(code.getText().toString()));
            entry_values.put("SPECIMEN_NO", code.getText().toString());
            entry_values.put("SPECIMEN_QTY", quantity.getText().toString());
        } else if (workType.matches("MP-Marketing Promotion")) {
            //entry_values.put("PROMO_ITEM_NO",codePrimary.get(code.getText().toString()));
            entry_values.put("PROMO_ITEM_NO", code.getText().toString());
            entry_values.put("PROMO_ITEM_QTY", quantity.getText().toString());
        }

        if (onBehalf.isChecked()) {
            entry_values.put("IS_ON_BEHALF", "1");
            entry_values.put("BEHALF_MOBILE", onBehalfMobile.getText().toString());
            entry_values.put("BEHALF_NICK", behalfNickname.getText().toString());
        } else {
            entry_values.put("IS_ON_BEHALF", "0");
            entry_values.put("BEHALF_MOBILE", "");
        }

        if (child_item[14].matches("1")) {
            entry_values.put("IS_UPLOADED", "0");
            entry_values.put("ENTRY_STATE", "2");
            entry_values.put("IS_COMPLETE_NEW", "0");
        }

        Global.dbObject.updateIntoTable("TRN_DCR_DET", entry_values, "OFFLINE_DCR_DET_NO=" + child_item[0]);
        Log.d("msg_db", "Child Detail is updated");

        entry_values = new ContentValues();
        entry_values.put("IS_UPLOADED", "0");
        entry_values.put("ENTRY_STATE", "2");
        Global.dbObject.updateIntoTable("TRN_DCR", entry_values, "OFFLINE_DCR_NO=" + child_item[1]);
        Log.d("msg_db", "Master Detail is updated for Child Update");
    }

    public void checkforupdate() {
        child_item = getIntent().getStringArrayExtra("child");
        if (child_item != null) {
            bookName.setVisibility(View.VISIBLE);
            if (workType.matches("SD-Specimen Distribution")) {
                code.setText(child_item[6]);
                quantity.setText(child_item[7]);
                bookName.setText(codePrimary.get(child_item[6]));
            } else if (workType.matches("MP-Marketing Promotion")) {
                code.setText(child_item[11]);
                quantity.setText(child_item[12]);
                bookName.setText(codePrimary.get(child_item[11]));
            }


            is_teacher = Integer.parseInt(child_item[3]);
            teacherInfo.setVisibility(View.VISIBLE);
            if (is_teacher == 1) {
                mobile.setText(child_item[5]);
                teacherName.setText(teacherPrimary.get(child_item[5]));
            } else {
                ((RadioButton) (findViewById(R.id.clientType))).setChecked(true);
                mobile.setText(child_item[10]);
                teacherName.setText(teacherPrimary.get(child_item[10]));
            }

            if (child_item[16].matches("1")) {
                behalfMobileInfo.setVisibility(View.VISIBLE);
                onBehalf.setChecked(true);
                onBehalfMobile.setText(child_item[17]);
                behalfNickname.setText(child_item[18]);
            }
            nick.setText(child_item[19]);
            save.setText("update");
        }
    }

    public void initiaLisation() {
        teacherInfo = (LinearLayout) findViewById(R.id.teacherInfo);
        behalfMobileInfo = (LinearLayout) findViewById(R.id.onBehalfMobile);

        code = (AutoCompleteTextView) findViewById(R.id.code);
        quantity = (EditText) findViewById(R.id.quantity);
        mobile = (AutoCompleteTextView) findViewById(R.id.mobile);
        nick = (AutoCompleteTextView) findViewById(R.id.nickname);
        bookName = (TextView) findViewById(R.id.bookName);
        teacherName = (TextView) findViewById(R.id.teacherName);
        teacherMobile = (TextView) findViewById(R.id.mobileNum);

        copyType = (RadioGroup) findViewById(R.id.copytype);

        mobileText = (TextView) findViewById(R.id.mobileText);
        codeText = (TextView) findViewById(R.id.codeText);
        quantityText = (TextView) findViewById(R.id.quantityText);
        onBehalf = (CheckBox) findViewById(R.id.onBehalf);
        onBehalfMobile = (EditText) findViewById(R.id.behalfMobileNo);
        behalfNickname = ((EditText) findViewById(R.id.behalfNickname));
    }

    public boolean checkValidity() {
        if ((workType.matches("TC-Teacher Contact") || workType.matches("LC-Library Contact"))) {

            String mobileno = mobile.getText().toString();
            if (isMobileValid) {
                if (!phoneNos.contains(mobileno)) {
                    phoneNos.add(mobileno);
                    return true;
                } else {
                    Toast.makeText(context, "Same mobile again", Toast.LENGTH_LONG);
                    mobile.requestFocus();
                    return false;
                }
            } else {
                mobile.requestFocus();
                return false;
            }
        }
        boolean isValid = true;
        if (quantity.getText().toString().matches("")) {
            isValid = false;
            quantity.setError("Quantity Value Empty");
            quantity.requestFocus();
        }
        if (code.getText().toString().matches("")) {
            isValid = false;
            code.setError("Code Value Empty");
            code.requestFocus();
        } else if (!codePrimary.containsKey(code.getText().toString())) {
            isValid = false;
            code.setError("Code Value Not Valid");
            code.requestFocus();
        } else {
            if (child_item == null) {
                String query = "";
                if (workType.matches("SD-Specimen Distribution"))
                    query = "SELECT TRN_DCR_DET.OFFLINE_DCR_DET_NO" +
                            " FROM TRN_DCR,TRN_DCR_DET" +
                            " WHERE TRN_DCR.OFFLINE_DCR_NO=TRN_DCR_DET.OFFLINE_DCR_NO AND TRN_DCR.TRN_DCR_DATE='" + Global.currentDate() + "' AND USER_NO=" + USER_NO +
                            " AND (CLIENT_MOBILE='" + mobile.getText().toString() + "' OR TEACHER_MOBILE='" + mobile.getText().toString() + "')" +
                            " AND SPECIMEN_NO=" + code.getText().toString();
                else if (workType.matches("MP-Marketing Promotion"))
                    query = "SELECT TRN_DCR_DET.OFFLINE_DCR_DET_NO" +
                            " FROM TRN_DCR,TRN_DCR_DET" +
                            " WHERE TRN_DCR.OFFLINE_DCR_NO=TRN_DCR_DET.OFFLINE_DCR_NO AND TRN_DCR.TRN_DCR_DATE='" + Global.currentDate() + "' AND USER_NO=" + USER_NO +
                            " AND (CLIENT_MOBILE='" + mobile.getText().toString() + "' OR TEACHER_MOBILE='" + mobile.getText().toString() + "')" +
                            " AND PROMO_ITEM_NO=" + code.getText().toString();
                else {
                    if (is_teacher == 1)
                        query = "SELECT TRN_DCR_DET.OFFLINE_DCR_DET_NO" +
                                " FROM TRN_DCR,TRN_DCR_DET" +
                                " WHERE TRN_DCR.OFFLINE_DCR_NO=TRN_DCR_DET.OFFLINE_DCR_NO AND TRN_DCR.TRN_DCR_DATE='" + Global.currentDate() + "' AND USER_NO=" + USER_NO +
                                " AND (TEACHER_MOBILE='" + mobile.getText().toString() + "')";
                    else
                        query = "SELECT TRN_DCR_DET.OFFLINE_DCR_DET_NO" +
                                " FROM TRN_DCR,TRN_DCR_DET" +
                                " WHERE TRN_DCR.OFFLINE_DCR_NO=TRN_DCR_DET.OFFLINE_DCR_NO AND TRN_DCR.TRN_DCR_DATE='" + Global.currentDate() + "' AND USER_NO=" + USER_NO +
                                " AND (CLIENT_MOBILE='" + mobile.getText().toString() + "')";
                }
                if (!query.matches("")) {
                    ArrayList<String[]> data_for_valid = Global.dbObject.rawqueryFromDatabase(query);
                    if (data_for_valid.size() != 0) {
                        showDialog("Error Code:100\nAlready Added Item Today ...");
                        isValid = false;
                    }
                }
            }
        }
        if (onBehalf.isChecked() && !isOnBehalfMobileValid) {
            isValid = false;
            onBehalfMobile.requestFocus();
        }
        if (!isMobileValid) {
            isValid = false;
            mobile.requestFocus();
        }
        if (!nickvalid) {
            isValid = false;
            nick.requestFocus();
        }
        if (onBehalf.isChecked() && !isOnBehalfNickValid) {
            isValid = false;
            behalfNickname.requestFocus();
        }
        return isValid;
    }

    public void showDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EntryActivity.this);
        builder.setTitle("Validation Failed");
        builder.setMessage(message);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        builder.create().show();
    }
/*
    public void forAddItem(EntryItem tch) {
        code.setText(tch.id);
        quantity.setText(tch.quantity);
        mobile.setText(tch.mobile);
        bookName.setText(tch.name);
        teacherName.setText(tch.teacher);
        teacherMobile.setText(tch.mobile);
    }*/

    public void allClear() {
        code.setText("");
        quantity.setText("1");

        teacherInfo.setVisibility(LinearLayout.GONE);

        bookName.setText("");
        bookName.setVisibility(View.INVISIBLE);
    }

    public long getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        return cal.getTimeInMillis();
    }

    public void insertEntry() {
        ContentValues entry_values = new ContentValues();
        long row_id = Global.dbObject.getLastRowID("TRN_DCR_DET", "OFFLINE_DCR_DET_NO");

        entry_values.put("OFFLINE_DCR_DET_NO", row_id + 1);
        entry_values.put("OFFLINE_DCR_NO", dcr_no);
        entry_values.put("ACTION_OFFLINE_TIME", getCurrentTime());
        entry_values.put("IS_FOR_TEACHER", is_teacher);
        if (is_teacher == 1) {
            entry_values.put("IS_FOR_CLIENT", "0");
            entry_values.put("TEACHER_NO", "");
            entry_values.put("TEACHER_MOBILE", mobile.getText().toString());
            entry_values.put("TEACHER_NICK", nick.getText().toString());
        } else {
            entry_values.put("IS_FOR_CLIENT", "1");
            entry_values.put("CLIENT_NO", "");
            entry_values.put("CLIENT_MOBILE", mobile.getText().toString());
        }
        if (workType.matches("SD-Specimen Distribution")) {
            entry_values.put("SPECIMEN_NO", code.getText().toString());
            entry_values.put("SPECIMEN_QTY", quantity.getText().toString());
        } else if (workType.matches("MP-Marketing Promotion")) {
            entry_values.put("PROMO_ITEM_NO", code.getText().toString());
            entry_values.put("PROMO_ITEM_QTY", quantity.getText().toString());
        }

        if (onBehalf.isChecked()) {
            entry_values.put("IS_ON_BEHALF", "1");
            entry_values.put("BEHALF_MOBILE", onBehalfMobile.getText().toString());
            entry_values.put("BEHALF_NICK", behalfNickname.getText().toString());
        }
        try {
            Global.dbObject.insertIntoTable("TRN_DCR_DET", entry_values);

            if (is_first_detail == 0) {
                ContentValues values = new ContentValues();
                values.put("IS_UPLOADED", "0");
                Global.dbObject.updateIntoTable("TRN_DCR", values, "OFFLINE_DCR_NO=" + dcr_no);
                ++is_first_detail;
            }

            Toast toast = Toast.makeText(EntryActivity.this, "Added", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } catch (Exception ex) {
            Toast toast = Toast.makeText(EntryActivity.this, "Failed to add", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            new logger(context).appendLog("exception in dcr detail insert " + ex.getMessage());
        }

		/*runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				loadDataToList();
			}
		});
		*/
    }

    // for initialize Specimen/Promotional IDs
    public void initCodeIds() {
		/*
		codeIds.add(new String("Bangla(100)"));
		codeIds.add(new String("English(101)"));
		codeIds.add(new String("Math(102)"));
		codeIds.add(new String("Physics(103)"));
		codeIds.add(new String("Chemistry(104)"));
		codeIds.add(new String("Biology(105)"));
		*/
        if (workType.matches("SD-Specimen Distribution")) {
            codeIds = new ArrayList<String>();
            //ArrayList<String []> specData=Global.dbObject.queryFromTable("SET_SPECIMEN", new String[]{"SPECIMEN_CODE","SPECIMEN_NAME","SPECIMEN_NO"},null);
            ArrayList<String[]> specData = Global.dbObject.queryFromTable("SET_SPECIMEN", new String[]{"SPECIMEN_NO", "SPECIMEN_NAME", "SPECIMEN_NO"}, null);
            for (int i = 0; i < specData.size(); i++) {
                codeIds.add(new String(specData.get(i)[1] + "(" + specData.get(i)[0] + ")"));
                codePrimary.put(specData.get(i)[0], specData.get(i)[1]);
                //primaryToCode.put(specData.get(i)[2],specData.get(i)[0]);
            }
        } else {
            codeIds = new ArrayList<String>();
            //ArrayList<String []> specData=Global.dbObject.queryFromTable("SET_PROMO_ITEM", new String[]{"PROMO_ITEM_CODE","PROMO_ITEM_NAME","PROMO_ITEM_NO"},null);
            ArrayList<String[]> specData = Global.dbObject.queryFromTable("SET_PROMO_ITEM", new String[]{"PROMO_ITEM_NO", "PROMO_ITEM_NAME", "PROMO_ITEM_NO"}, null);
            for (int i = 0; i < specData.size(); i++) {
                codeIds.add(new String(specData.get(i)[1] + "(" + specData.get(i)[0] + ")"));
                codePrimary.put(specData.get(i)[0], specData.get(i)[1]);
                //primaryToCode.put(specData.get(i)[2],specData.get(i)[0]);
            }
        }
        Log.d("msg", "Size Code Ids " + codeIds.size());
    }

    public void initTeacherMobiles() {
		/*teacherMobiles.add(new String("Mr.Jabber(01720454569)"));
		teacherMobiles.add(new String("Mr.Karim(01720454569)"));
		teacherMobiles.add(new String("Mr.Rahim(01720454569)"));
		teacherMobiles.add(new String("Mr.Madhu(01720454569)"));
		teacherMobiles.add(new String("Mr.Rajon(01720454569)"));*/
        if (is_teacher == 1) {
            ArrayList<String[]> data_s = Global.dbObject.queryFromTable("SET_TEACHER_INFO", new String[]{"TEACHER_MOBILE", "TEACHER_NAME"}, null);
            Map<String, String> map = new HashMap<String, String>();
            for (int i = 0; i < data_s.size(); i++) {
                teacherTable.add(new String(data_s.get(i)[1] + "(" + data_s.get(i)[0] + ")"));
                map.put(data_s.get(i)[0], data_s.get(i)[1]);
            }
            teacherMobiles = teacherTable;
            teacherPrimary = map;
        } else {
            ArrayList<String[]> data_s = Global.dbObject.queryFromTable("SET_CLIENT_INFO", new String[]{"CLIENT_MOBILE", "CLIENT_NAME"}, null);
            Map<String, String> map = new HashMap<String, String>();
            for (int i = 0; i < data_s.size(); i++) {
                clientTable.add(new String(data_s.get(i)[1] + "(" + data_s.get(i)[0] + ")"));
                map.put(data_s.get(i)[0], data_s.get(i)[1]);
            }
            teacherMobiles = clientTable;
            teacherPrimary = map;
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
            Intent intent = new Intent(EntryActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private TextWatcher mobileWatcher = new TextWatcher() {

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
            if (mobile.getText().length() == 11) {
                if (teacherPrimary.containsKey(arg0.toString())) {
                    teacherInfo.setVisibility(LinearLayout.VISIBLE);
                    teacherName.setText(teacherPrimary.get(arg0.toString()));
                    teacherMobile.setText(arg0.toString());
                } else
                    teacherInfo.setVisibility(LinearLayout.GONE);
            } else {
                teacherInfo.setVisibility(LinearLayout.GONE);
            }
        }
    };
    private TextWatcher codeWatcher = new TextWatcher() {

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
            if (codePrimary.containsKey(arg0.toString())) {
                bookName.setVisibility(View.VISIBLE);
                bookName.setText(codePrimary.get(arg0.toString()));
            } else {
                bookName.setVisibility(View.INVISIBLE);
            }
        }
    };
	/*public void loadDataToList(){
		ArrayList<String []> data=Global.dbObject.queryFromTable("TRN_DCR_DET", null, "ENTRY_STATE<>3 AND OFFLINE_DCR_NO="+dcr_no);
		Log.d("msg","load Data to List ,Size:"+data.size());

		listView=(ListView) findViewById(R.id.listDetailEntry);
		listView.setAdapter(new MyDetailListAdapter(this,data));
	}
	public void deleteDCRMaster(String table_name,String query)
	{
		ArrayList<String []> master_data1=Global.dbObject.queryFromTable(table_name,new String[]{"IS_UPLOADED","IS_COMPLETE_NEW"},query);
		if(master_data1.get(0)[0].matches("1")){
			ContentValues values=new ContentValues();
			values.put("ENTRY_STATE", "3");
			values.put("IS_UPLOADED", "0");
			values.put("IS_COMPLETE_NEW", "0");
			
			Global.dbObject.updateIntoTable(table_name, values, query);
		}
		else	
		{
			if(master_data1.get(0)[1].matches("1"))
				Global.dbObject.deleteFromTable(table_name, query);
			else{
				ContentValues values=new ContentValues();
				values.put("ENTRY_STATE", "3");
				values.put("IS_UPLOADED", "0");
				values.put("IS_COMPLETE_NEW", "0");
				
				Global.dbObject.updateIntoTable(table_name, values, query);	
			}
		}
		
	}
	public void childAlertDialogYesorNo(final String []item)
	{
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setTitle("Delete Detail Entry");
		builder.setMessage("Do you want to delete ?");
		builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				Log.d("msg","Enter for Detail Entry Delete ... ");
				deleteDCRMaster("TRN_DCR_DET","OFFLINE_DCR_DET_NO="+item[0]);
				
				ContentValues entry_values=new ContentValues();
				entry_values.put("IS_UPLOADED","0");
				Global.dbObject.updateIntoTable("TRN_DCR", entry_values, "OFFLINE_DCR_NO="+item[1]);
			}
		});
		builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
			}
		});
	    builder.create().show();
	}
	private class MyDetailListAdapter extends ArrayAdapter<String[]>{
		private Context context;
		private ArrayList<String[]> data;
		public MyDetailListAdapter(Context con,ArrayList<String[]> d){
			super(con,R.layout.dcr_view_detail_list_item,d);
			context=con;
			data=d;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView=inflater.inflate(R.layout.dcr_view_detail_list_item,parent,false);
			
			TextView codeItem=(TextView) rowView.findViewById(R.id.codeItem);
			TextView codeItemName=(TextView) rowView.findViewById(R.id.codeItemName);
			TextView quantityItem=(TextView) rowView.findViewById(R.id.quantityItem);
			TextView agentName=(TextView) rowView.findViewById(R.id.agentName);
			TextView agentMobile=(TextView) rowView.findViewById(R.id.agentMobile);
			TextView agentType=(TextView) rowView.findViewById(R.id.agentType);
			
			ImageButton addButton=(ImageButton) rowView.findViewById(R.id.addButton1);
			ImageButton deleteButton=(ImageButton) rowView.findViewById(R.id.deleteButton1);
			
			LinearLayout itemLayout=(LinearLayout) rowView.findViewById(R.id.itemInfo);
			LinearLayout quantityLayout=(LinearLayout) rowView.findViewById(R.id.quantityInfo);
			
			if(!workType.matches("SD-Specimen Distribution") && !workType.matches("MP-Marketing Promotion")){
				itemLayout.setVisibility(LinearLayout.GONE);
				quantityLayout.setVisibility(LinearLayout.GONE);
			}
			
			if(data.get(position)[3].matches("1"))
			{
				//agentType.setText("Teacher");
				agentMobile.setText(data.get(position)[5]);
				
				ArrayList<String[]> teacherName=Global.dbObject.queryFromTable("SET_TEACHER_INFO", new String[]{"TEACHER_NAME"}, "TEACHER_MOBILE="+data.get(position)[5]);
				if(teacherName.size() != 0)
					agentName.setText(teacherName.get(0)[0]);
			}
			else
			{
				//agentType.setText("Client");
				agentMobile.setText(data.get(position)[10]);
				
				ArrayList<String[]> clientName=Global.dbObject.queryFromTable("SET_CLIENT_INFO", new String[]{"CLIENT_NAME"}, "CLIENT_MOBILE="+data.get(position)[10]);
				if(clientName.size() != 0)
					agentName.setText(clientName.get(0)[0]);
			}
			
			agentName.setTextAppearance(context,R.style.viewTitlevalue);
			codeItem.setTextAppearance(context,R.style.viewTitlevalue);
			
			if(data.get(position)[16].matches("1"))
			{
				agentType.setVisibility(View.VISIBLE);
				agentType.setText(data.get(position)[17]+"(OnBehalf)");
			}
			
			// here codeItemName set
			if(workType.matches("SD-Specimen Distribution")){
				codeItem.setText(data.get(position)[6]);
				quantityItem.setText(data.get(position)[7]);
				ArrayList<String[]> bookName=Global.dbObject.queryFromTable("SET_SPECIMEN", new String[]{"SPECIMEN_NAME"}, "SPECIMEN_NO="+data.get(position)[6]);
				if(bookName.size() != 0)
					codeItemName.setText(bookName.get(0)[0]);
			}
			else if(workType.matches("MP-Marketing Promotion")){
				codeItem.setText(data.get(position)[11]);
				quantityItem.setText(data.get(position)[12]);
				ArrayList<String[]> bookName=Global.dbObject.queryFromTable("SET_PROMO_ITEM", new String[]{"PROMO_ITEM_NAME"}, "PROMO_ITEM_NO="+data.get(position)[11]);
				if(bookName.size() != 0)
					codeItemName.setText(bookName.get(0)[0]);	
			}
			
			deleteButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					childAlertDialogYesorNo(data.get(position));
				}
			});

			addButton.setVisibility(View.GONE);
				
			return rowView;
		}
	}*/
}
