package hioa.mappe3.s180475;

import hioa.mappe3.s180475.DatePickerFragment.OnDateChange;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditMemberActivity extends FragmentActivity implements OnDateChange{

	private EditText txtFirstname, txtLastname, txtStart, txtBirth, txtStreetAdr, txtPostNr, txtCity, txtEmail, txtTlf, txtInfo;
	private Button btnSave, btnDelete;
	private Member mem;
	private DialogFragment datePicker;
	private String id;
	private ProgressDialog pDialog;

	// url to update member
	private static final String url_update_member = "http://nodlandanielsen.com/membertool/update_member.php";
	// url to delete member
	private static final String url_delete_member = "http://nodlandanielsen.com/membertool/delete_member.php";
	// url to delete all payments
	private static final String url_delete_all_payments = "http://nodlandanielsen.com/membertool/delete_all_payments.php";

	// JSON parser class
	JSONParser jsonParser = new JSONParser();	

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_member);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		Intent i = getIntent();

		// getting member from intent
		mem = (Member) i.getSerializableExtra(MainActivity.TAG_MEMBER);
		id = mem.getId();

		btnSave = (Button) findViewById(R.id.btnSave);
		btnDelete = (Button) findViewById(R.id.btnDelete);

		txtFirstname = (EditText) findViewById(R.id.inputFirstname);
		txtLastname = (EditText) findViewById(R.id.inputLastname);
		txtStart = (EditText) findViewById(R.id.inputStart);
		txtBirth = (EditText) findViewById(R.id.inputBirth);
		txtStreetAdr = (EditText) findViewById(R.id.inputStreetAdr);
		txtPostNr = (EditText) findViewById(R.id.inputPostNr);
		txtCity = (EditText) findViewById(R.id.inputCity);
		txtEmail = (EditText) findViewById(R.id.inputEmail);
		txtTlf = (EditText) findViewById(R.id.inputTlf);
		txtInfo = (EditText) findViewById(R.id.inputInfo);

		txtFirstname.setText(mem.getFirstname());
		txtLastname.setText(mem.getLastname());
		txtStart.setText(mem.getStart());
		txtBirth.setText(mem.getBirth());
		txtStreetAdr.setText(mem.getStreetadr());
		txtPostNr.setText(mem.getPostnr());
		txtCity.setText(mem.getCity());
		txtEmail.setText(mem.getEmail());
		txtTlf.setText(mem.getTlf());
		txtInfo.setText(mem.getInfo());

		txtBirth.setOnFocusChangeListener(new OnFocusChangeListener(){ // get called when EditText gets focus
			@Override
			public void onFocusChange(View v, boolean hasFocus){
				if(hasFocus){
					showDatePickerFragment(v);
				}
			}
		});
		txtStart.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus){
				if(hasFocus){
					showDatePickerFragment(v);
				}
			}
		});
	} // end of onCreate

	// method for buttonclicks. Are set as listener in xml-resource
	public void onBtnClick(View v){
		if(v.getId() == R.id.btnSave)
			new SaveMemberDetails(this).execute();
		else if(v.getId() == R.id.btnDelete){
			showConfirmDeleteDialog();
		}		
	}

	// This will show a dialog where user can pick a date. We send a string to identify which edittext we want to insert date
	public void showDatePickerFragment(View v){
		Bundle bundle = new Bundle();
		int day, month, year;
		
		if(v.getId() == R.id.inputBirth){
			day = Integer.parseInt(txtBirth.getText().toString().substring(8, 10));
			month = Integer.parseInt(txtBirth.getText().toString().substring(5, 7));
			year = Integer.parseInt(txtBirth.getText().toString().substring(0, 4));
			
			bundle.putInt("day", day);
			bundle.putInt("month", month);
			bundle.putInt("year", year);
			bundle.putString("date", "birth");
		}
		else{
			day = Integer.parseInt(txtStart.getText().toString().substring(8, 10));
			month = Integer.parseInt(txtStart.getText().toString().substring(5, 7));
			year = Integer.parseInt(txtStart.getText().toString().substring(0, 4));
			
			bundle.putInt("day", day);
			bundle.putInt("month", month);
			bundle.putInt("year", year);
			bundle.putString("date", "start");
		}
		bundle.putString("class", "edit_member");
		datePicker = new DatePickerFragment();
		datePicker.setArguments(bundle);
		datePicker.show(getFragmentManager(), "editmemberDP");
	}

	public EditText getStart(){
		return txtStart;
	}

	public EditText getBirth(){
		return txtBirth;
	}
	
	private void showConfirmDeleteDialog(){
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.deleteDialogTitle)
		.setMessage(R.string.deleteDialogText)
		.setPositiveButton(R.string.deleteDialogPositiveButton, new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				new DeleteMember().execute();
			}
			
		})
		.setNegativeButton(R.string.deleteDialogNegativeButton, new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
								
			}
			
		})
		.create().show();
	}

	// Saves member in background task
	class SaveMemberDetails extends AsyncTask<Void, Void, Void>{

		private Context con;
		Toast toast;
		int numErrors; // counts nr of errors in edittexts
		StringBuilder builder;

		public SaveMemberDetails(Context c){
			con = c;
		}

		// Before starting background thread Show Progress Dialog
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			pDialog = new ProgressDialog(EditMemberActivity.this);
			pDialog.setOnCancelListener(new OnCancelListener(){ // get called if numErrors > 0
				public void onCancel(DialogInterface dialog){
					toast = Toast.makeText(con, builder.append(" " + getString(R.string.regexFaultEnd).toString()), Toast.LENGTH_LONG);
					toast.show();
				}
			});
			pDialog.setMessage(getText(R.string.pDialog_SavingMember));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		/**
		 * Saving member
		 * */
		protected Void doInBackground(Void... args){
			builder = new StringBuilder(con.getString(R.string.regexFaultStart)); // Builds up an error-string when EditText fields are not filled inn
																					// correct
			// getting updated data from EditTexts
			String firstname = txtFirstname.getText().toString();
			String lastname = txtLastname.getText().toString();
			String start_membership = txtStart.getText().toString();
			String birth = txtBirth.getText().toString();
			String streetadr = txtStreetAdr.getText().toString();
			String postnr = txtPostNr.getText().toString();
			String city = txtCity.getText().toString();
			String email = txtEmail.getText().toString();
			String tlf = txtTlf.getText().toString();
			String info = txtInfo.getText().toString();

			// use regex to control input
			numErrors = 0;
			if(!firstname.matches("\\D+")){
				builder.append(" '").append(con.getString(R.string.hint_firstname)).append("'");
				numErrors++;
			}
			if(!lastname.matches("\\D+")){
				builder.append(" '").append(con.getString(R.string.hint_lastname)).append("'");
				numErrors++;
			}
			if(!postnr.matches("\\d+") && !postnr.matches("")){
				builder.append(" '").append(con.getString(R.string.hint_postNr)).append("'");
				numErrors++;
			}
			if(!city.matches("\\D+") && !city.matches("")){
				builder.append(" '").append(con.getString(R.string.hint_city)).append("'");
				numErrors++;
			}
			if(!email.matches("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}") && !email.matches("")){
				builder.append(" '").append(con.getString(R.string.hint_email)).append("'");
				numErrors++;
			}
			if(!tlf.matches("\\d+") && !tlf.matches("")){
				builder.append(" '").append(con.getString(R.string.hint_tlf)).append("'");
				numErrors++;
			}
			if(numErrors > 0){ // One or more errors in input
				pDialog.cancel();
				return null;
			}
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(MainActivity.TAG_ID, id));
			params.add(new BasicNameValuePair(MainActivity.TAG_FIRSTNAME, firstname));
			params.add(new BasicNameValuePair(MainActivity.TAG_LASTNAME, lastname));
			params.add(new BasicNameValuePair(MainActivity.TAG_START_MEMBERSHIP, start_membership));
			params.add(new BasicNameValuePair(MainActivity.TAG_BIRTH, birth));
			params.add(new BasicNameValuePair(MainActivity.TAG_STREETADR, streetadr));
			params.add(new BasicNameValuePair(MainActivity.TAG_POSTNR, postnr));
			params.add(new BasicNameValuePair(MainActivity.TAG_CITY, city));
			params.add(new BasicNameValuePair(MainActivity.TAG_EMAIL, email));
			params.add(new BasicNameValuePair(MainActivity.TAG_TLF, tlf));
			params.add(new BasicNameValuePair(MainActivity.TAG_INFO, info));

			// sending modified data through http request
			// Notice that update member url accepts POST method
			JSONObject json = jsonParser.makeHttpRequest(url_update_member, "POST", params);

			// check json success tag
			try{
				int success = json.getInt(MainActivity.TAG_SUCCESS);

				if(success == 1){ // successfully updated
					Intent i = getIntent();
					setResult(100, i); // send result code 100 to notify about member update
					finish();
				}
			}catch(JSONException e){
				e.printStackTrace();
			}

			return null;
		}

		// After completing background task Dismiss the progress dialog
		protected void onPostExecute(Void result){
			pDialog.dismiss();
		}
	}

	// Background Async Task to Delete member
	class DeleteMember extends AsyncTask<String, String, String>{

		// Before starting background thread Show Progress Dialog
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			pDialog = new ProgressDialog(EditMemberActivity.this);
			pDialog.setMessage(getText(R.string.pDialog_DeleteMember));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		protected String doInBackground(String... args){

			// Check for success tag
			int success;
			try{
				// Building Parameters
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair(MainActivity.TAG_ID, id));

				// delets members, and payment if they have, by making HTTP request
				JSONObject json = jsonParser.makeHttpRequest(url_delete_member, "POST", params);
				JSONObject json2 = jsonParser.makeHttpRequest(url_delete_all_payments, "POST", params);

				// json success tag
				success = json.getInt(MainActivity.TAG_SUCCESS);
				if(success == 1){
					// member successfully deleted and we notify previous activity by sending code 101
					Intent i = getIntent();
					setResult(101, i);
					finish();
				}
			}catch(JSONException e){
				e.printStackTrace();
			}

			return null;
		}

		// After completing background task Dismiss the progress dialog
		protected void onPostExecute(String file_url){
			// dismiss the dialog once member deleted
			pDialog.dismiss();
		}

	}

	// Method for communicating with DatePickerFragment
	@Override
	public void dateSet(String tag, int year, int month, int day){
		String correctedDay = (day > 10 ? day + "" : "0" + day);
		String correctedMonth = (month > 10 ? month + "" : "0" + month);
		
		if(tag.equals("birth")){
			txtBirth.setText(year + "-" + correctedMonth + "-" + correctedDay);
		}
		else{
			txtStart.setText(year + "-" + correctedMonth + "-" + correctedDay);
		}
	}
}