package hioa.mappe3.s180475;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;

public class TabListener<T extends Fragment> implements ActionBar.TabListener {

	private android.app.Fragment mFragment;
	private final Activity mActivity;
	private final String mTag;
	private final Class<T> mClass;
	
	public TabListener(Activity act, String tag, Class<T> clz){
		mActivity = act;
		mTag = tag;
		mClass = clz;
		
		// Check to see if we already have a fragment for this tab, probably
        // from a previously saved state.  If so, deactivate it, because our
        // initial state is that a tab isn't shown.
        mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
        if (mFragment != null && !mFragment.isDetached()) {
            FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
            ft.detach(mFragment);
            ft.commit();
        }
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if(mFragment == null){
			mFragment = Fragment.instantiate(mActivity, mClass.getName());
			ft.add(android.R.id.content, mFragment, mTag);
		}
		else{
			ft.attach(mFragment);
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		if(mFragment != null){
			ft.detach(mFragment);
		}
	}
	
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		
	}
}
