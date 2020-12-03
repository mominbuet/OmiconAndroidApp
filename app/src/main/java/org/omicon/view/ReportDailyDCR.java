package org.omicon.view;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.omicon.R;
import org.omicon.entry.EntryActivity;
import org.omicon.initial.Global;
import org.omicon.initial.HomeActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class ReportDailyDCR extends Activity {

    private EditText tvShowDate;
    private SimpleDateFormat sdfOfflineDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.report_activity);
        tvShowDate = (EditText) findViewById(R.id.tvShowDate);
        tvShowDate.setText(Global.currentDate());
        showReportDataTable();

        sdfOfflineDate = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

        DatePickerDialog.OnDateSetListener dateListener = new OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {

                if (!Global.dateValidation(dayOfMonth, monthOfYear, year)) {
                    tvShowDate.setText("");
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

                tvShowDate.setText(date);
                showReportDataTable();
            }
        };
        Calendar cal = Calendar.getInstance();
        final DatePickerDialog dialog = new DatePickerDialog(ReportDailyDCR.this, dateListener,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        tvShowDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.show();
            }
        });
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
            Intent intent = new Intent(ReportDailyDCR.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    /*private void showReportDataTable() {
		// TODO Auto-generated method stub
		String strDCRquery = null;
		Integer fareAmount = 0 ;
		TextView tvItem = null;
		TableRow tlRow = null;
		
		strDCRquery = getDcrQuery(tvShowDate.getText().toString() );	
		Log.d("dbRow", strDCRquery);
		
		ArrayList<String[]> dataSetDcr = Global.dbObject.rawqueryFromDatabase(strDCRquery);
		
		if(dataSetDcr.size()< 1){
			//Toast.makeText(getApplicationContext(), "No DCR found", Toast.LENGTH_SHORT).show();
			//Global.showToastText(getApplicationContext(), "No DCR Found", true);
		}else{
			
			for (String[] dataRowDcr : dataSetDcr) {
				Log.d("str_length", String.valueOf(dataRowDcr.length));
				
				//tlRow = new TableRow(getApplicationContext());
				
				tlRow=new TableRow(ReportDailyDCR.this);
				
				for (int i=0; i < dataRowDcr.length ; i++) {
					dataRowDcr[i] = dataRowDcr[i] == null ? "xx" : dataRowDcr[i];
					Log.d("col_val", dataRowDcr[i]);
					//tvItem = new TextView(getApplicationContext());
					tvItem=new TextView(ReportDailyDCR.this);
					tvItem.setText(dataRowDcr[i]);
					
					tlRow.addView(tvItem);
				}
				
				fareAmount = fareAmount + Integer.parseInt(dataRowDcr[6]) ;
				tlShowReport.addView(tlRow);
			}
						
		}
		
		strDCRquery = getDerQuery(tvShowDate.getText().toString());fareAmount = 0 ;
		Log.d("msg",strDCRquery);
		
		if(strDCRquery.length()> 1){
			ArrayList<String[]> dataSetDer = Global.dbObject.rawqueryFromDatabase(strDCRquery);
			
			if(dataSetDer.size()< 1){
				Toast.makeText(getApplicationContext(), "No DER found", Toast.LENGTH_SHORT).show();
			}else{
				
				for (String[] dataRowDer : dataSetDer) {
					Log.d("str_length", String.valueOf(dataRowDer.length));
					tlRow = new TableRow(getApplicationContext());
					for (int i=0; i < dataRowDer.length ; i++) {
						dataRowDer[i] = dataRowDer[i] == null ? "xx" : dataRowDer[i];
						Log.d("der_val", dataRowDer[i]);
						tvItem = new TextView(getApplicationContext());
						tvItem.setText(dataRowDer[i]);
						
						tlRow.addView(tvItem);
					}
					
					fareAmount = fareAmount + Integer.parseInt(dataRowDer[1]) ;
					tlShowReport.addView(tlRow);
				}
				
			}
		}
	}*/

    private void showReportDataTable() {

        String strDCRquery = getDcrQuery(tvShowDate.getText().toString());
        Log.d("dbRow", strDCRquery);

        ArrayList<String[]> dataSetDcr = Global.dbObject.rawqueryFromDatabase(strDCRquery);
        DCRReportAdapter dcr_adapter = new DCRReportAdapter(ReportDailyDCR.this, dataSetDcr);

        ListView listDCR = (ListView) findViewById(R.id.reportList);
        listDCR.setAdapter(dcr_adapter);

        double amountDCR = 0.00;
        for (int i = 0; i < dataSetDcr.size(); i++)
            amountDCR += Double.parseDouble(dataSetDcr.get(i)[6]);

        TextView tv = (TextView) findViewById(R.id.subTotal);
        tv.setText(String.format("%.2f", amountDCR));

        strDCRquery = getDerQuery(tvShowDate.getText().toString());
        Log.d("dbRow", strDCRquery);

        ArrayList<String[]> dataSetDer = new ArrayList<String[]>();
        if (!strDCRquery.matches("")) {
            dataSetDer = Global.dbObject.rawqueryFromDatabase(strDCRquery);
        }
        DERReportAdapter der_adapter = new DERReportAdapter(ReportDailyDCR.this, dataSetDer);
        ListView listDER = (ListView) findViewById(R.id.reportListDER);
        listDER.setAdapter(der_adapter);


        Utility.setListViewHeightBasedOnChildren(listDCR);
        Utility.setListViewHeightBasedOnChildren(listDER);

        double amountDER = 0.00;
        for (int i = 0; i < dataSetDer.size(); i++)
            amountDER += Double.parseDouble(dataSetDer.get(i)[1]);

        tv = (TextView) findViewById(R.id.subTotalder);
        tv.setText(String.format("%.2f", amountDER));

        tv = (TextView) findViewById(R.id.reportGrandTotal);
        tv.setText(String.format("%.2f", amountDER + amountDCR));


    }

    private String getDcrQuery(String ln_date) {
        SharedPreferences prefs = getSharedPreferences("MY_PREFS", 0);
        // TODO Auto-generated method stub
        String query = "SELECT WORK_AREA_FROM_NAME, WORK_AREA_TO_NAME, TIME_FROM, TIME_TO,  INSTITUTE_NAME,  TRANS_TYPE_NAME, FARE_AMT,OFFLINE_DCR_NO,DCR_TYPE_NO " +
                " FROM (	SELECT WORK_AREA_FROM_NAME, WORK_AREA_TO_NAME, TIME_FROM, TIME_TO, INSTITUTE_NO, TRANS_TYPE_NO, FARE_AMT,OFFLINE_DCR_NO,DCR_TYPE_NO FROM " +
                " TRN_DCR where TRN_DCR_DATE = '" + ln_date + "'  AND ENTRY_STATE <> 3 AND USER_NO=" + prefs.getString("user_no", "") + ")AA  LEFT JOIN ( SELECT INSTITUTE_NAME, INSTITUTE_NO FROM  SET_INSTITUTE) BB " +
                " ON BB.INSTITUTE_NO = AA.INSTITUTE_NO LEFT JOIN(  SELECT TRANS_TYPE_NAME,TRANS_TYPE_NO FROM  SET_TRANSPORT_TYPE " +
                ") CC ON CC.TRANS_TYPE_NO = AA.TRANS_TYPE_NO ";

        return query;
    }

    private String getDerQuery(String ln_date) {
        SharedPreferences prefs = getSharedPreferences("MY_PREFS", 0);
        String query = "";
        ArrayList<String[]> offlineExpList = Global.dbObject.queryFromTable("TRN_EXPENSE", new String[]{"OFFLINE_EXP_NO"}, " TRN_EXP_DATE = '" + ln_date + "' AND USER_NO=" + prefs.getString("user_no", ""));
        if (offlineExpList.size() > 0) {
            String offline_exp_no = offlineExpList.get(0)[0];
            query = "SELECT EXP_TYPE_NAME, EXP_AMT FROM (SELECT EXP_TYPE_NO, EXP_AMT FROM TRN_EXPENSE_DET  " +
                    "WHERE OFFLINE_EXP_NO = " + offline_exp_no + ")AA  LEFT JOIN(SELECT EXP_TYPE_NO, EXP_TYPE_NAME FROM " +
                    "SET_EXP_TYPE )BB ON BB.EXP_TYPE_NO = AA.EXP_TYPE_NO";
        } else {
            Log.d("exp_master", "no rows for master");
        }

        return query;
    }

    class DCRReportAdapter extends ArrayAdapter<String[]> {
        private ArrayList<String[]> data;
        private Context context;

        private String[] workType = {"SD", "MP"
                , "TC", "LC", "OW"};

        public DCRReportAdapter(Context con, ArrayList<String[]> d) {
            super(con, R.layout.report_activity_dcr, d);
            context = con;
            data = d;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.report_activity_dcr, parent, false);

            TextView tv = (TextView) rowView.findViewById(R.id.reportFromLoc);
            tv.setText(data.get(position)[0]);

            tv = (TextView) rowView.findViewById(R.id.reportToLoc);
            tv.setText(data.get(position)[1]);

            tv = (TextView) rowView.findViewById(R.id.reportFromTime);
            tv.setText(Global.getTimeValue(Long.parseLong(data.get(position)[2])));

            tv = (TextView) rowView.findViewById(R.id.reportToTime);
            tv.setText(Global.getTimeValue(Long.parseLong(data.get(position)[3])));

            tv = (TextView) rowView.findViewById(R.id.reportWorkArea);
            tv.setText(data.get(position)[4]);

            tv = (TextView) rowView.findViewById(R.id.reportTransport);
            tv.setText(data.get(position)[5]);

            tv = (TextView) rowView.findViewById(R.id.reportFare);
            tv.setText(String.format("%.2f", Double.parseDouble(data.get(position)[6])));

            ArrayList<String[]> mobileNumbers = Global.dbObject.queryFromTable("TRN_DCR_DET",
                    new String[]{"TEACHER_MOBILE", "CLIENT_MOBILE"}, "OFFLINE_DCR_NO=" + data.get(position)[7]);
            String listMobiles = "";
            for (int i = 0; i < mobileNumbers.size(); i++) {
                Log.d("msg", "t" + mobileNumbers.get(i)[0] + " c" + mobileNumbers.get(i)[1]);
                if (mobileNumbers.get(i)[0] != null && !mobileNumbers.get(i)[0].matches(""))
                    listMobiles += mobileNumbers.get(i)[0] + "\n";

                else if (mobileNumbers.get(i)[1] != null && !mobileNumbers.get(i)[1].matches(""))
                    listMobiles += mobileNumbers.get(i)[1] + "\n";
            }
            tv = (TextView) rowView.findViewById(R.id.reportMeetPerson);
            tv.setText(listMobiles);

            tv = (TextView) rowView.findViewById(R.id.reportPurpose);
            try {
                int index = Integer.parseInt(data.get(position)[8]);
                if (index >= 1 && index < 6)
                    tv.setText(workType[index - 1]);
            } catch (Exception e) {
            }

            return rowView;
        }
    }

    class DERReportAdapter extends ArrayAdapter<String[]> {
        private ArrayList<String[]> data;
        private Context context;

        public DERReportAdapter(Context con, ArrayList<String[]> d) {
            super(con, R.layout.report_activity_der, d);
            context = con;
            data = d;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.report_activity_der, parent, false);

            TextView tv = (TextView) rowView.findViewById(R.id.reportExpen);
            tv.setText(data.get(position)[0]);

            tv = (TextView) rowView.findViewById(R.id.reportAmount);
            tv.setText(String.format("%.2f", Double.parseDouble(data.get(position)[1])));

            return rowView;
        }
    }
	/*class Utility {

	    public  void setListViewHeightBasedOnChildren(ListView listView) {
	        ListAdapter listAdapter = listView.getAdapter();
	        if (listAdapter == null) {
	            // pre-condition
	            return;
	        }

	        int totalHeight = 0;
	        int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.UNSPECIFIED);
	        for (int i = 0; i < listAdapter.getCount(); i++) {
	            View listItem = listAdapter.getView(i, null, listView);
	            listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
	            totalHeight += listItem.getMeasuredHeight();
	        }

	        ViewGroup.LayoutParams params = listView.getLayoutParams();
	        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
	        listView.setLayoutParams(params);
	        listView.requestLayout();
	        
	        ListAdapter listAdapter = listView.getAdapter(); 
	        if (listAdapter == null) {
	            // pre-condition
	            return;
	        }

	        int totalHeight = 0;
	        for (int i = 0; i < listAdapter.getCount(); i++) {
	            View listItem = listAdapter.getView(i, null, listView);
	            listItem.measure(0, 0);
	            totalHeight += listItem.getMeasuredHeight();
	        }

	        ViewGroup.LayoutParams params = listView.getLayoutParams();
	        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
	        listView.setLayoutParams(params);
	    }
	}*/

}
