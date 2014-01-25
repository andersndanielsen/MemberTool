package hioa.mappe3.s180475;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener{
	private static SharedPreferences sharedPref;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
		sharedPref = getPreferenceManager().getSharedPreferences();
		
		//Sets summary on setting-screen
		String subjectMembership = "'" + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.prefET_subject_membership_key), "") + "'";
		String messageMembership = "'" + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.prefET_message_membership_key), "") + "'";
		
		String subjectNotPaid = "'" + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.prefET_subject_notPaid_key), "") + "'";
		String messageNotPaid = "'" + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.prefET_message_notPaid_key), "") + "'";
		
		String priceRegularMember = "'" + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.prefET_priceRegularMember_key), "") + "'";
		String priceNewMember = "'" + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.prefET_priceNewMember_key), "") + "'";
		String priceFamilyMember = "'" + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.prefET_priceFamilyMember_key), "") + "'";

		String acountNr = "'" + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.prefET_acountNR_key), "") + "'";
		
		Preference sub = findPreference("subject_membership_all");
		sub.setSummary(subjectMembership);
		
		Preference mess = findPreference("message_membership_all");
		mess.setSummary(messageMembership);
		
		Preference sub2 = findPreference("subject_notPaid");
		sub2.setSummary(subjectNotPaid);
		
		Preference mess2 = findPreference("message_notPaid");
		mess2.setSummary(messageNotPaid);
		
		Preference sumPriceRegularMember = findPreference("pref_regularmember");
		sumPriceRegularMember.setSummary(priceRegularMember);
		
		Preference sumPriceNewMember = findPreference("pref_newmember");
		sumPriceNewMember.setSummary(priceNewMember);
		
		Preference sumPriceFamilyMember = findPreference("pref_familymember");
		sumPriceFamilyMember.setSummary(priceFamilyMember);
		
		Preference sumAcountNr = findPreference("pref_acountNr");
		sumAcountNr.setSummary(acountNr);
	}
	
	@Override
    public void onResume() {
        super.onResume();
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    //this is called when we change any preferences in the SettingsActivity
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		
	}	
}