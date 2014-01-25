package hioa.mappe3.s180475;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

// MainActivity have no UI, and only serves Fragments
public class MainActivity extends Activity implements DatePickerFragment.OnDateChange, NewMemberFragment.NewMemberListener,
																AllMembersFragment.AllMembersCallback{
	public static final int ALL = 1;
	public static final int SORT_THIS_YEAR = 2;
	public static final int SORT_THIS_OR_LAST = 3;
	public static final String TAG_SUCCESS = "success";
	public static final String TAG_MEMBERS = "members";
	public static final String TAG_MEMBER = "member";
	public static final String TAG_ID = "id";
	public static final String TAG_FIRSTNAME = "firstname";
	public static final String TAG_LASTNAME = "lastname";
	public static final String TAG_START_MEMBERSHIP = "start_membership";
	public static final String TAG_BIRTH = "birth";
	public static final String TAG_STREETADR = "streetadr";
	public static final String TAG_POSTNR = "postnr";
	public static final String TAG_CITY = "city";
	public static final String TAG_EMAIL = "email";
	public static final String TAG_TLF = "tlf";
	public static final String TAG_INFO = "info";
	public static final String TAG_MEMBERSHIP = "membership";
	public static final String TAG_FAMILY_MEMBER = "Familie";
	public static final String TAG_REGULAR_MEMBER = "Vanlig";
	public static final String TAG_NEW_MEMBER = "Ny";

	private DatePickerFragment datePicker;
	private ProgressDialog pDialog;
	private FragmentManager fManager;
	private AllMembersFragment ama;

	Tab tab1, tab2, tab3;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false); // sets the defaultvalues for the first use of app
		pDialog = new ProgressDialog(this);
		
		/*
		 * SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this); if(!pref.contains("logedIn")){ Intent logIn = new
		 * Intent(this, LoginActivity.class); startActivity(logIn); }
		 */

		// creates tabs in actionbar/menu
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		tab1 = actionBar.newTab().setIcon(R.drawable.social_group)
				.setTabListener(new TabListener<AllMembersFragment>(this, "allMembers", AllMembersFragment.class));
		actionBar.addTab(tab1);

		tab1 = actionBar.newTab().setIcon(R.drawable.content_new)
				.setTabListener(new TabListener<NewMemberFragment>(this, "newMember", NewMemberFragment.class));
		actionBar.addTab(tab1);
	}
	
	public void onPause(){
		super.onPause();
		if(pDialog.isShowing()){
			pDialog.dismiss();
		}
	}

	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.activity_main, menu);
		return true;
	}

	// When buttons in actionbar get clicked
	public boolean onOptionsItemSelected(MenuItem i){
		if(fManager == null)
			fManager = getFragmentManager();
		// If the Fragment is non-null, then it is currently being
		// retained across a configuration change.
		if(ama == null){
			ama = new AllMembersFragment();
			fManager.beginTransaction().add(ama, "allMembers").commit();
		}
		if(i.getItemId() == R.id.submenu_emptyMail){
			ama.startBackgroundTask(SORT_THIS_OR_LAST, "emptyMail");
		}
		else if(i.getItemId() == R.id.submenu_membership_all){
			ama.startBackgroundTask(SORT_THIS_OR_LAST, "membership_all");
		}
		else if(i.getItemId() == R.id.submenu_notPaidMembership_all){
			ama.startBackgroundTask(SORT_THIS_OR_LAST, "notPaidMembership_all");
		}
		else if(i.getItemId() == R.id.menu_settings){
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		}
		return true;
	}

	// Response
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(fManager == null)
			fManager = getFragmentManager();
		// If the Fragment is non-null, then it is currently being
		// retained across a configuration change.
		ama = (AllMembersFragment) fManager.findFragmentByTag("allMembers");
		if(ama == null){
			ama = new AllMembersFragment();
			fManager.beginTransaction().add(ama, "allMembers").commit();
		}
		if(resultCode == 100){
			// if result code 100 means user edited member
			ama.startBackgroundTask(ALL, "");
			Toast.makeText(this, getText(R.string.toast_editedMember), Toast.LENGTH_LONG).show();
		}else if(resultCode == 101){
			// if result code 101 means user deleted member
			ama.startBackgroundTask(ALL, "");
			Toast.makeText(this, getText(R.string.toast_deletedMember), Toast.LENGTH_LONG).show();
		}else if(resultCode == 200){
			// if result code 200 means user edited/deleted payment
			ama.startBackgroundTask(ALL, "");
		}
	}

	// ButtonListener
	public boolean onBtnClick(View v){
		if(fManager == null)
			fManager = getFragmentManager();
		// If the Fragment is non-null, then it is currently being
		// retained across a configuration change.
		ama = (AllMembersFragment) fManager.findFragmentByTag("allMembers");
		if(ama == null){
			ama = new AllMembersFragment();
			fManager.beginTransaction().add(ama, "allMembers").commit();
		}
		NewMemberFragment nma;
		switch(v.getId()){
		case R.id.btn_all_all:
			ama.startBackgroundTask(ALL, "");
			return true;
		case R.id.btn_all_this:
			ama.startBackgroundTask(SORT_THIS_YEAR, "");
			return true;
		case R.id.btn_all_onoftwo:
			ama.startBackgroundTask(SORT_THIS_OR_LAST, "");
			return true;
		default:
			// creating new member in background thread
			nma = (NewMemberFragment)fManager.findFragmentByTag("newMember"); 
			nma.startBackgroundTask(this);
		}
		return true;
	}

	// Starts up DatePickerFragment, and send a tag to know which EditText that calls
	public void showDatePickerFragment(View v){
		Bundle bundle = new Bundle();
		if(v.getId() == R.id.inputBirth){
			bundle.putString("date", "birth");
		}
		else{
			bundle.putString("date", "start");
		}
		datePicker = new DatePickerFragment();
		datePicker.setArguments(bundle);
		datePicker.show(getFragmentManager(), "editmemberDP");
	}

	/**************************/
	/**** Callback Methods ****/
	/**************************/

	// Method for communicating with DatePickerFragment. Sets date from DatePickerFragment into the right EditText
	@Override
	public void dateSet(String tag, int year, int month, int day){
		Fragment f = getFragmentManager().findFragmentByTag("newMember");
		NewMemberFragment nma;

		String correctedDay = (day > 10 ? day + "" : "0" + day);
		String correctedMonth = (month > 10 ? month + "" : "0" + month);
		
		if(f != null && f.isVisible()){
			nma = (NewMemberFragment) f;
			if(tag.equals("birth"))
				nma.getBirth().setText(year + "-" + correctedMonth + "-" + correctedDay);
			else
				nma.getStart().setText(year + "-" + correctedMonth + "-" + correctedDay);
		}
	}

	// Goes to 'All' tab when member is saved
	@Override
	public void onSavedMember(){
		if(fManager == null)
			fManager = getFragmentManager();
		// If the Fragment is non-null, then it is currently being
		// retained across a configuration change.
		ama = (AllMembersFragment) fManager.findFragmentByTag("allMembers");
		if(ama == null){
			ama = new AllMembersFragment();
			fManager.beginTransaction().add(ama, "allMembers").commit();
		}
		ama.startBackgroundTask(ALL, "");
		getActionBar().getTabAt(0).select();
	}

	// Methods for communicating with AllMembersFragment

	@Override
	public void onPreExecute(){
		pDialog.setMessage(getText(R.string.pDialog_LoadingMembers));
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(true);
		pDialog.show();
	}

	@Override
	public void onPostExecute(){
		pDialog.dismiss();
	}

	@Override
	public void memberClicked(String tag, Member member){
		Intent intent;
		if(tag.equals("edit")){
			intent = new Intent(this, EditMemberActivity.class);
			intent.putExtra(TAG_MEMBER, member);
			startActivityForResult(intent, 100);
		}else if(tag.equals("payment")){
			intent = new Intent(this, EditPayments.class);
			intent.putExtra(TAG_MEMBER, member);
			startActivityForResult(intent, 200);
		}
	}

	// If table is empty we want to put in a new member
	@Override
	public void addMemberWhenEmpty(){
		getActionBar().getTabAt(1).select();
	}

	// receive an arraylist from AllMembersFragment with all the members. We'll start up an email-client with members e-mail addresses.
	@Override
	public void sendMail(ArrayList<Member> membersList, String tag){
		Iterator<Member> iter = membersList.iterator();
		String[] addresses = new String[membersList.size()];	// Every singel mailaddress
		String[] addresses_notPaid = new String[membersList.size()];	// Every mailaddress for those whose not paid so far this year.
		int i = 0;
		
		while(iter.hasNext()){
			Member member = iter.next();
			addresses[i] = member.getEmail();
			if(!member.getPaidThisYear()){
				addresses_notPaid[i] = member.getEmail();
			}
			i++;
		}
		Intent intent = new Intent(Intent.ACTION_SEND);
		
		// Gets the yearly fee and acountnumber from preferences to send with mail.
		String priceRegularMember = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.prefET_priceRegularMember_key), "");
		String priceFamilyMember = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.prefET_priceFamilyMember_key), "");
		String acountNr = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.prefET_acountNR_key), "");

		// Send mail to selected person that has not paid yet for this year.
		if(tag.equals("notPaidMembership")){
			// Get subject and message from Preferences and format message to 'complete' xml
			String subject = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.prefET_subject_notPaid_key), "");
			String messageFormatted = String.format(
					PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.prefET_message_notPaid_key), ""), priceRegularMember,
					priceFamilyMember, acountNr);
	
			intent.setType("message/rfc822");
			intent.putExtra(Intent.EXTRA_EMAIL, addresses);
			intent.putExtra(Intent.EXTRA_SUBJECT, subject);
			intent.putExtra(Intent.EXTRA_TEXT, messageFormatted);
		}
				
		// Send mail to everyone whose not paid yet for this year.
		else if(tag.equals("notPaidMembership_all")){
		// Get subject and message from Preferences and format message to 'complete' xml
			String subject = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.prefET_subject_notPaid_key), "");
			String messageFormatted = String.format(
					PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.prefET_message_notPaid_key), ""), priceRegularMember,
					priceFamilyMember, acountNr);
	
			intent.setType("message/rfc822");
			intent.putExtra(Intent.EXTRA_EMAIL, addresses_notPaid);
			intent.putExtra(Intent.EXTRA_SUBJECT, subject);
			intent.putExtra(Intent.EXTRA_TEXT, messageFormatted);
		}
		
		// Send mail to everyone, or 1 selected member, with info about how to pay. Usually the first mail of the year, and next is 'notPaid-mails'.
		else if(tag.equals("membership_all") || tag.equals("membership")){
			// Get subject and message from Preferences and format message to 'complete' xml
			String subject = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.prefET_subject_membership_key), "");
			String messageFormatted = String.format(
					PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.prefET_message_membership_key), ""),
					priceRegularMember, priceFamilyMember, acountNr);

			intent.setType("message/rfc822");
			intent.putExtra(Intent.EXTRA_EMAIL, addresses);
			intent.putExtra(Intent.EXTRA_SUBJECT, subject);
			intent.putExtra(Intent.EXTRA_TEXT, messageFormatted);
		}
		
		// Send an empty mail to everyone or 1 selected member
		else if(tag.equals("emptyMail")){
			intent.setType("message/rfc822");
			intent.putExtra(Intent.EXTRA_EMAIL, addresses);
		}
		
		try{
			startActivity(Intent.createChooser(intent, getText(R.string.sendMail)));
		}
		catch(android.content.ActivityNotFoundException e){
			Toast.makeText(this, getText(R.string.exception_activityNotFound), Toast.LENGTH_LONG).show();
		}
	}
}