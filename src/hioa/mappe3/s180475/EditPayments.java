package hioa.mappe3.s180475;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class EditPayments extends FragmentActivity {
	
	private String id, year, membership;
	private int indexClicked;
	private ProgressDialog pDialog;
    protected static ArrayList<Payment> paymentList;
	protected static ArrayAdapter<Payment> adapter;
    private ListView lv;
    private Member member;
 
    // url
    private static final String url_payment_details = "http://nodlandanielsen.com/membertool/get_payment_details.php";
    private static final String url_save_payment = "http://nodlandanielsen.com/membertool/save_payment.php";
    private static final String url_delete_payment = "http://nodlandanielsen.com/membertool/delete_payment.php";
    
    // JSON Node names
    private static final String TAG_PAYMENTS = "payments";
    private static final String TAG_DATE = "date";
    private static final String TAG_YEAR = "year";
    
    JSONParser jsonParser = new JSONParser();
    JSONArray payments = null;
    
    private TextView txtFullname, txtStart, txtBirth, txtAddress, txtEmail, txtTlf, txtInfo;
    private EditText payForYear;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_payments);
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
        .permitAll().build();
        StrictMode.setThreadPolicy(policy);
		
        Intent i = getIntent();
        member = (Member) i.getSerializableExtra(MainActivity.TAG_MEMBER);
        id = member.getId();
        
        txtFullname = (TextView) findViewById(R.id.tw_full_name);
        txtStart = (TextView) findViewById(R.id.tw_start);
        txtBirth = (TextView) findViewById(R.id.tw_birth);
        txtAddress = (TextView) findViewById(R.id.tw_address);
        txtEmail = (TextView) findViewById(R.id.tw_email);
        txtTlf = (TextView) findViewById(R.id.tw_tlf);
        txtInfo = (TextView) findViewById(R.id.tw_info);
        payForYear = (EditText) findViewById(R.id.et_payForYear);
        
        txtFullname.setText(member.getFirstname() + " " + member.getLastname());
        txtStart.setText(getText(R.string.tw_start) + " " + member.getStart());
        txtBirth.setText(getText(R.string.tw_birth) + " " + member.getBirth());
        txtAddress.setText(getText(R.string.tw_address) + " " + member.getStreetadr() + ", " + member.getPostnr() + " " + member.getCity());
        txtEmail.setText(getText(R.string.tw_email) + " " + member.getEmail());
        txtTlf.setText(getText(R.string.tw_tlf) + " " + member.getTlf());
        txtInfo.setText(getText(R.string.tw_info) + " " + member.getInfo());
        
        //Fills in EditTexts with default amount for preferences, and this year
        GregorianCalendar cal = new GregorianCalendar();
        payForYear.setText(cal.get(Calendar.YEAR)+"");
        
        paymentList = new ArrayList<Payment>();
        lv = (ListView) findViewById(R.id.list);
        registerForContextMenu(lv);	//menu when long-clicking on list
        lv.setOnItemClickListener(new OnItemClickListener() {
        	@Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long i) {
            	Payment paymentClicked = paymentList.get(position);            	
            	showPaymentInfoDialog(paymentClicked);
            }
        });
        lv.setOnItemLongClickListener(new OnItemLongClickListener(){	//Deletes payment when longclicking
        	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long i) {
        		indexClicked = position;
            	id = paymentList.get(position).getMemberID();
            	year = paymentList.get(position).getYear();
            	new DeletePayment(getApplication()).execute();
            	return true;
            }
        });
        
     // Getting complete member details in background thread
        new GetPaymentDetails(this).execute();
	}
	
	//Pops up a dialog with info about clicked payment
	public void showPaymentInfoDialog(Payment p){
		PaymentInfoDialog dialog = PaymentInfoDialog.newInstance(getText(R.string.pDialogTitle).toString(), p.getRegDate(), p.getTypeOfMembership());
    	dialog.show(getSupportFragmentManager(),"paymentInfo");
	}
	
	//buttonlistener set in xml-resource
	public void onBtnClick(View v){
    	if(v.getId() == R.id.btn_addPayment)
    		new SavePaymentDetails(this).execute();
    }
	
	public void radioButtonListener(View v){
		switch(v.getId()){
		case R.id.rb_familyMember:
			membership = MainActivity.TAG_FAMILY_MEMBER;
			break;
		case R.id.rb_regularMember:
			membership = MainActivity.TAG_REGULAR_MEMBER;
			break;
		case R.id.rb_newMember:
			membership = MainActivity.TAG_NEW_MEMBER;
			break;
		}
	}
	
	// Background Async Task to Get complete member details
    class GetPaymentDetails extends AsyncTask<Void, Void, Void> {
    	private Context con;
    	JSONObject json;
    	
    	public GetPaymentDetails(Context c){
    		con = c;
    	}
    	
    	// Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EditPayments.this);
            pDialog.setMessage(getText(R.string.pDialog_LoadingPayment));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
 
        // Getting payment details in background thread
        protected Void doInBackground(Void... params) {
        	// Check for success tag
        	int success;
        	try {
        	// Building Parameters
        	List<NameValuePair> params1 = new ArrayList<NameValuePair>();
        	params1.add(new BasicNameValuePair(MainActivity.TAG_ID, id));

        	// getting payment details by making HTTP request, note that details url will use GET request
        	json = jsonParser.makeHttpRequest(url_payment_details, "GET", params1);
        	
        	if(json != null){
        		success = json.getInt(MainActivity.TAG_SUCCESS);
        		if (success == 1) {	// successfully received member details
        			payments = json.getJSONArray(TAG_PAYMENTS);
        	
        			// looping through All Payments
        			for (int i = 0; i < payments.length(); i++) {
        				JSONObject c = payments.getJSONObject(i);
        				// Storing each json item in variable
        				String id = c.getString(MainActivity.TAG_ID);
        				String date = c.getString(TAG_DATE);
        				String year = c.getString(TAG_YEAR);
        				String membership = c.getString(MainActivity.TAG_MEMBERSHIP);
                
        				Payment p = new Payment(id, date, year, membership);
                
        				paymentList.add(p);
        				adapter = new PaymentAdapter(con, R.layout.payment_list_item, R.id.name, paymentList);                        
        			}
        		}	
        	}
        } catch (JSONException e) {
            e.printStackTrace();
          }

        return null;
        }
        protected void onPostExecute(Void v) {
            pDialog.dismiss();
            if(json != null)
        		lv.setAdapter(adapter);
        }
    }
    
    
    //Background Async Task to  Save member Details
    class SavePaymentDetails extends AsyncTask<Void, Void, Void> {
 
    	private Context con;
    	private Toast toast;
    	int success;
    	
    	public SavePaymentDetails(Context c){
    		con = c;
    	}
    	
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EditPayments.this);
            pDialog.setMessage(getText(R.string.pDialog_SavingPayment));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
 
        protected Void doInBackground(Void... args) {
 
            // getting updated data from EditText
            String year = payForYear.getText().toString();
 
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(MainActivity.TAG_ID, id));
            params.add(new BasicNameValuePair(TAG_YEAR, year));
            params.add(new BasicNameValuePair(MainActivity.TAG_MEMBERSHIP, membership));
            
            // sending modified data through http request, notice that update member url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_save_payment, "POST", params);
            
            try {
                success = json.getInt(MainActivity.TAG_SUCCESS);
            } catch (JSONException e) {
                e.printStackTrace();
            }
 
            return null;
        }
 
        protected void onPostExecute(Void v) {
                	if (success == 1) {
                		Intent intent = getIntent();
                		setResult(200, intent);
                		toast = Toast.makeText(con, con.getString(R.string.toast_savedPayment), Toast.LENGTH_LONG);
                		toast.show();
                    } else {
                    	Intent intent = getIntent();
                		setResult(200, intent);
                    	toast = Toast.makeText(con, con.getString(R.string.toast_NotsavedPayment), Toast.LENGTH_LONG);
                		toast.show();
                    }
                	pDialog.dismiss();
                	paymentList.clear();
                	new GetPaymentDetails(con).execute();
                }
    }
 
    // Background Async Task to Delete member
    class DeletePayment extends AsyncTask<Void, Void, Void> {
 
    	private Context con;
    	private Toast toast;
    	private int success;
    	
    	public DeletePayment(Context c){
    		con = c;
    	}
    	
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EditPayments.this);
            pDialog.setMessage(getText(R.string.pDialog_DeletePayment));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
 
        protected Void doInBackground(Void... args) {
 
            // Check for success tag
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair(MainActivity.TAG_ID, id));
                params.add(new BasicNameValuePair(TAG_YEAR, year));
 
                // getting member details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(url_delete_payment, "POST", params);
 
                success = json.getInt(MainActivity.TAG_SUCCESS);
                
            } catch (JSONException e) {
                e.printStackTrace();
            }
 
            return null;
        }
 
        protected void onPostExecute(Void v) {
            if (success == 1) {
            	Intent intent = getIntent();
        		setResult(200, intent);
        		toast = Toast.makeText(con, con.getString(R.string.toast_deletedPayment), Toast.LENGTH_LONG);
        		toast.show();
            } else {
            	Intent intent = getIntent();
        		setResult(200, intent);
            	toast = Toast.makeText(con, con.getString(R.string.toast_NotDeletedPayment), Toast.LENGTH_LONG);
        		toast.show();
            }
            pDialog.dismiss(); 
            paymentList.remove(indexClicked);
            adapter.notifyDataSetChanged();
        } 
    }

}
