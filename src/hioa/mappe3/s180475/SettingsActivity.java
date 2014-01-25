package hioa.mappe3.s180475;

import android.os.Bundle;
import android.preference.PreferenceActivity;

//this class is 'parent' of SettingsFragment
public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	getFragmentManager().beginTransaction()
		.replace(android.R.id.content, new SettingsFragment())
		.commit();
	}
}