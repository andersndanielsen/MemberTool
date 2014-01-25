package hioa.mappe3.s180475;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
 
public class NewMemberFragment extends Fragment {
	
	public interface NewMemberListener{
		public void onSavedMember();
		public void onPreExecute();
		public void onPostExecute();
	}
	
	public static final String TAG = "NEWMEMBERFRAGMENT";
	
	private NewMemberListener mCallback;
	private CreateNewMember mTask;
	private ProgressDialog pDialog;
	private DialogFragment datePicker;
	private boolean mRunning;
	private EditText inputFirstname, inputLastname, inputStart, inputBirth, inputStreetAdr, inputPostNr, inputCity, inputEmail, inputTlf, inputInfo;
    
    private static String url_create_member = "http://nodlandanielsen.com/membertool/create_member.php";
 
    JSONParser jsonParser = new JSONParser();
    
    /**
  	 * makes sure MainActivity has implemented NewMemberListener
  	 */
  	public void onAttach(Activity activity){
		super.onAttach(activity);
		
		try{
			mCallback = (NewMemberListener) activity;
		}catch(ClassCastException e){
			Log.e(TAG, activity.toString() + " must implement NewMemberListener");
		}
	}
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	//set my own layout resource
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.add_member, container, false);
        return view;
    }
    public void onActivityCreated(Bundle savedInstanceState){	//Starts up when activity is created and ready
    	super.onActivityCreated(savedInstanceState);
    	
    	View customView = getView();
        inputFirstname = (EditText) customView.findViewById(R.id.inputFirstname);
    	inputLastname = (EditText) customView.findViewById(R.id.inputLastname);
    	inputBirth = (EditText) customView.findViewById(R.id.inputBirth);
    	inputStreetAdr = (EditText) customView.findViewById(R.id.inputStreetAdr);
    	inputPostNr = (EditText) customView.findViewById(R.id.inputPostNr);
    	inputCity = (EditText) customView.findViewById(R.id.inputCity);
    	inputEmail = (EditText) customView.findViewById(R.id.inputEmail);
    	inputTlf = (EditText) customView.findViewById(R.id.inputTlf);
    	inputStart = (EditText) customView.findViewById(R.id.inputStart);
    	inputInfo = (EditText) customView.findViewById(R.id.inputInfo);
    	
    	//listen on EditText for focus, and opens DatePickerFragment
    	inputBirth.setOnFocusChangeListener(new EditTextFocusListener());
    	inputStart.setOnFocusChangeListener(new EditTextFocusListener());
    	//Button btnCreateMember = (Button) customView.findViewById(R.id.btnAddMember);
    	getActivity().getActionBar().getTabAt(1).select();
    }
    
    /**
     * This method is called only once when the Fragment is first created.
     */
    public void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);
    	setRetainInstance(true);
    }
    
    /**
     * Clear all EditText fields
     */
    public void clearFields(){
    	inputFirstname.setText("");
    	inputLastname.setText("");
    	inputStart.setText("");
    	inputBirth.setText("");
    	inputStreetAdr.setText("");
    	inputPostNr.setText("");
    	inputCity.setText("");
    	inputEmail.setText("");	
    	inputTlf.setText("");
    	inputInfo.setText("");
    }
  	
  	/**
	 * Set the callback to null so we don't accidentally leak the Activity instance.
	 */
  	public void onDetach(Activity activity){
  		super.onDetach();
  		if(pDialog != null)
  			pDialog.dismiss();
  		mCallback = null;
  	}
  	
  	/**
	 * Start the background task.
	 */
	public void startBackgroundTask(Context con){
		if(!mRunning){
			mTask = new CreateNewMember(con);
			mTask.execute();
			mRunning = true;
		}
	}
	
	/**
	   * This will show a dialog where user can pick a date. We send a string to identify which edittext we want to insert date to
	   */
	  	public void showDatePickerFragment(View v){
	  		Bundle bundle = new Bundle();
	  		if(v.getId() == R.id.inputBirth){
	  			bundle.putString("date","birth");
	  		}
	  		else{
	  			bundle.putString("date", "start");
	  		}
	  		datePicker = new DatePickerFragment();
	  		datePicker.setArguments(bundle);
	  		datePicker.show(getFragmentManager(), "editmemberDP");
	  	}
	
	public EditText getStart(){
  		return inputStart;
  	}
  	
  	public EditText getBirth(){
  		return inputBirth;
  	}
 
    /**
     * Background Async Task to Create new member
     * */
    class CreateNewMember extends AsyncTask<Void, Void, Void> {
 
    	private Context con;
    	private int success;
    	private int numErrors;
    	private JSONObject json;
    	private StringBuilder builder;
    	private Toast toast;
    	
    	public CreateNewMember(Context c){
    		con = c;
    	}
    	
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            if(mCallback != null){
            	pDialog = new ProgressDialog(getActivity());
            	pDialog.setOnCancelListener(new OnCancelListener(){	//get called if numErrors > 0
            		public void onCancel(DialogInterface dialog){
            		toast = Toast.makeText(con, builder.append(" " + getString(R.string.regexFaultEnd).toString()), Toast.LENGTH_LONG);
        			toast.show();
            	}
            });
            pDialog.setMessage(getText(R.string.pDialog_CreateMember));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
            }
        }
 
        /**
         * Creating member in backgroundtask
         * */
        protected Void doInBackground(Void... ignore) {
        	builder = new StringBuilder(con.getString(R.string.regexFaultStart));
        	String firstname = inputFirstname.getText().toString();
            String lastname = inputLastname.getText().toString();
            String start_membership = inputStart.getText().toString();
            String birth = inputBirth.getText().toString();
            String streetadr = inputStreetAdr.getText().toString();
            String postnr = inputPostNr.getText().toString();
            String city = inputCity.getText().toString();
            String email = inputEmail.getText().toString();
            String tlf = inputTlf.getText().toString();
            String info = inputInfo.getText().toString();
 
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
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
    		
    		//use regex to control input
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
    		
    		if(numErrors > 0){	//One or more errors in input
    			pDialog.cancel();
    			return null;
    		}
            
            // getting JSON Object
            json = jsonParser.makeHttpRequest(url_create_member, "POST", params);
 
            // check for success tag
            try {
            	success = json.getInt(MainActivity.TAG_SUCCESS);
            } catch (JSONException e) {
                Log.e(TAG,e.toString());
            }
 
            return null;
        }
 
        /**
         * After completing background task Dismiss the progress dialog
         */
        protected void onPostExecute(Void ignore) {
        	mRunning = false;
        	pDialog.dismiss();
            if(numErrors == 0){
            	clearFields();
            	mCallback.onSavedMember();
            }
        }
 
    }
    
    /********************/
    /**** LISTENERES ****/
    /********************/
    
    /**
     * EditTextFocusListener opens a DatePickerFragment when the EditText field is in focus
     */
    class EditTextFocusListener implements OnFocusChangeListener{

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
		    if(hasFocus){
		        showDatePickerFragment(v);
		    }
		}
    }
}