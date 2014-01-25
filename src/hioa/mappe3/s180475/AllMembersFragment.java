package hioa.mappe3.s180475;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AllMembersFragment extends ListFragment{

	// Interface that AllMembersFragment use to communicate with MainActivity
	public interface AllMembersCallback{
		public void memberClicked(String tag, Member member);
		public void addMemberWhenEmpty();
		public void sendMail(ArrayList<Member> membersList, String tag);
		public void onPreExecute();
		public void onPostExecute();
	}

	public static final String TAG = "ALLMEMBERSFRAGMENT";

	private AllMembersCallback mCallback;
	private LoadAllMembers mTask;
	private boolean mRunning;

	protected static ArrayList<Member> membersList;
	// We'll use mCurrentMemberList to add all members from DB to avoid changing content of the adapter before we make it to notify ListView when
	// ContentChange suddenly turns in
	protected static ArrayList<Member> mCurrentMemberList;
	protected static ArrayAdapter<Member> adapter;
	private ListView lv;
	private TextView header, searchbox; // Header shows nr of members in
										// listview

	// url to get all members list
	private static String url_all_members = "http://nodlandanielsen.com/membertool/get_all_members.php";
	private static String url_thisYear_members = "http://www.nodlandanielsen.com/membertool/get_thisyear_members.php";
	private static String url_last2Years_members = "http://www.nodlandanielsen.com/membertool/get_lasttwoyears_members.php";	

	// Creating JSON Parser object
	JSONParser jParser = new JSONParser();
	JSONArray members = null;
	JSONArray members2 = null;

	// Makes sure interface AllMembersCallback is implemented by activity
	public void onAttach(Activity activity){
		super.onAttach(activity);

		try{
			mCallback = (AllMembersCallback) activity;
		}catch(ClassCastException e){
			Log.e(TAG, activity.toString() + " must implement AllMembersCallback");
		}
	}

	// sets my own layout to fragment
	public View onCreateView(LayoutInflater inflater, ViewGroup view, Bundle bundle){
		return inflater.inflate(R.layout.all_members, null);
	}

	// This method is called when MainActivity is created
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);

		header = (TextView) getActivity().findViewById(R.id.listView_header);
		searchbox = (TextView) getActivity().findViewById(R.id.listView_search);
		searchbox.addTextChangedListener(new TextChangedListener());

		lv = getListView();
		registerForContextMenu(lv);
		lv.setOnItemClickListener(new MemberlistClickListener());
		if(membersList != null)
			header.setText(getActivity().getText(R.string.listView_header) + " " + membersList.size()); // Writes in TextView how many members in list
	}

	/**
	 * This method is called only once when the Fragment is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(true); // Makes Fragmentmanager hold on to this Fragment even after detach
		membersList = new ArrayList<Member>(); // Arraylist for ListView
		mCurrentMemberList = new ArrayList<Member>();
		adapter = new MemberAdapter(getActivity(), R.layout.list_item, R.id.name, membersList, 1);
		setListAdapter(adapter);
		startBackgroundTask(MainActivity.SORT_THIS_OR_LAST, "");
	}

	/**
	 * Set the callback to null so we don't accidentally leak the Activity instance.
	 */
	@Override
	public void onDetach(){
		super.onDetach();
		mCallback = null;
	}

	/**
	 * This method is <em>not</em> called when the Fragment is being retained across Activity instances.
	 */

	@Override
	public void onDestroy(){
		super.onDestroy();
		cancelBackgroundTask();
	}

	// This menu pops up when long-clicking an item
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.contextmenu, menu);
	}

	// What happens when we click an item in the contextmenu(pops up after longclicking in list)
	public boolean onContextItemSelected(MenuItem item){
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Member memberClicked;
		ArrayList<Member> memList;

		switch(item.getItemId()){
		case R.id.contextMenu_editMember:
			memberClicked = membersList.get(info.position);
			mCallback.memberClicked("edit", memberClicked); // callback to MainActivity
			return true;
		case R.id.contextMenu_sendMail_empty:
			memberClicked = membersList.get(info.position);
			memList = new ArrayList<Member>();
			memList.add(0, memberClicked);
			mCallback.sendMail(memList, "emptyMail"); // callback to MainActivity
			return true;
		case R.id.contextMenu_sendMail_membership:
			memberClicked = membersList.get(info.position);
			memList = new ArrayList<Member>();
			memList.add(0, memberClicked);
			mCallback.sendMail(memList, "membership"); // callback to MainActivity
			return true;
		case R.id.contextMenu_sendMail_membership_notPaid:
			memberClicked = membersList.get(info.position);
			memList = new ArrayList<Member>();
			memList.add(0, memberClicked);
			mCallback.sendMail(memList, "notPaidMembership"); // callback to MainActivity
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * Start the background task.
	 */
	public void startBackgroundTask(int sortCriteria, String tag){
		if(!mRunning){
			mTask = new LoadAllMembers(sortCriteria, tag);
			mTask.execute();
			mRunning = true;
		}
	}

	/**
	 * Cancel the background task.
	 */
	public void cancelBackgroundTask(){
		if(mRunning){
			mTask.cancel(false);
			mTask = null;
			mRunning = false;
		}
	}

	/***************************/
	/***** BACKGROUND TASK *****/
	/***************************/

	// Background Async Task to Load all members by making HTTP Request
	class LoadAllMembers extends AsyncTask<Void, Void, Void>{

		private int sort; // if we're sorting on all members, those who paid this year or those who has payed this or last year.
		private int success;
		private int success2;
		private String tag;

		public LoadAllMembers(int sortCriteria, String t){
			sort = sortCriteria;
			tag = t;
		}

		// Before starting background thread Show Progress Dialog
		@Override
		protected void onPreExecute(){
			if(mCallback != null){
				mCallback.onPreExecute();
			}
			((MemberAdapter) adapter).setTypeOfSort(sort);
			membersList.clear();
		}

		// getting All members from url
		protected Void doInBackground(Void... ignore){
			List<NameValuePair> params = new ArrayList<NameValuePair>(); // Building Parameters
			JSONObject json; // getting JSON string from URL
			JSONObject json2 = null;

			if(sort == 1){
				json = jParser.makeHttpRequest(url_all_members, "GET", params);
				json2 = jParser.makeHttpRequest(url_thisYear_members, "GET", params);
			}else if(sort == 2)
				json = jParser.makeHttpRequest(url_thisYear_members, "GET", params);
			else{
				json = jParser.makeHttpRequest(url_last2Years_members, "GET", params);
				json2 = jParser.makeHttpRequest(url_thisYear_members, "GET", params);
			}

			try{
				// Checking for SUCCESS TAG. If sort==1 or sort==3 we also
				// checks for payments with json2-object because we want to give each member a 
				// red or green color to mark if they have paid or not.
				success = json.getInt(MainActivity.TAG_SUCCESS);
				if(sort == 1 || sort == 3)
					success2 = json2.getInt(MainActivity.TAG_SUCCESS);
				if(success == 1){
					members = json.getJSONArray(MainActivity.TAG_MEMBERS); // Members found. Getting Array of Members
					if((sort == 1 || sort == 3) && success2 == 1){ // Payments found. Getting array of payments
						members2 = json2.getJSONArray(MainActivity.TAG_MEMBERS);
					}
					mCurrentMemberList.clear();
					// looping through All Members
					for(int i = 0; i < members.length(); i++){
						JSONObject c = members.getJSONObject(i);
						// Storing each json item in variable
						String id = c.getString(MainActivity.TAG_ID);
						String firstname = c.getString(MainActivity.TAG_FIRSTNAME);
						String lastname = c.getString(MainActivity.TAG_LASTNAME);
						String start_membership = c.getString(MainActivity.TAG_START_MEMBERSHIP);
						String birth = c.getString(MainActivity.TAG_BIRTH);
						String streetadr = c.getString(MainActivity.TAG_STREETADR);
						String postnr = c.getString(MainActivity.TAG_POSTNR);
						String city = c.getString(MainActivity.TAG_CITY);
						String email = c.getString(MainActivity.TAG_EMAIL);
						String tlf = c.getString(MainActivity.TAG_TLF);
						String info = c.getString(MainActivity.TAG_INFO);

						Member mem = new Member(id, firstname, lastname, start_membership, birth, streetadr, postnr, city, email, tlf, info);
						if((sort == 1 || sort == 3) && success2 == 1){ // if we're looking for info about who has payed, and table is not empty
							for(int j = 0; j < members2.length(); j++){
								JSONObject obj = members2.getJSONObject(j);
								if(obj.getString(MainActivity.TAG_ID).equals(id))
									mem.setPaidThisYear(true);
							}
						}
						mCurrentMemberList.add(mem);
						if(isCancelled()){
							mRunning = false;
							break;
						}
					}
				}
			}catch(JSONException e){
				Log.e(TAG, e.toString());
			}
			return null;
		}

		protected void onPostExecute(Void result){
			adapter.addAll(mCurrentMemberList);
			mRunning = false;
			if(mCallback != null){
				mCallback.onPostExecute();
				header.setText(getActivity().getText(R.string.listView_header) + " " + membersList.size()); // Writes in TextView how many members in
																											// list
				// Wan't the possibility to add a new member only when we're looking at 'all members'
				if(success != 1 && sort == MainActivity.ALL){
					mCallback.addMemberWhenEmpty();
				}
				//empty list
				else if((success != 1 && sort == MainActivity.SORT_THIS_YEAR) || (success != 1 && sort == MainActivity.SORT_THIS_OR_LAST)){
					membersList.clear();
				}

				// If LoadAllMembers was started to get mailaddresses we return membersList
				if(tag.equals("notPaidMembership_all") || tag.equals("membership_all") || tag.equals("emptyMail")){ 
					mCallback.sendMail(membersList, tag);
				}
			}
		}
	} // End of LoadAllMembers

	/*******************/
	/**** LISTENERS ****/
	/*******************/

	class TextChangedListener implements TextWatcher{

		@Override
		public void afterTextChanged(Editable s){
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after){
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count){
			Member m;
			int pos;
			boolean found = false;
			if(count >= 1){
				ListIterator<Member> iter = membersList.listIterator();
				while(!found){
					if(iter.hasNext()){
						pos = iter.nextIndex();
						m = iter.next();
						if(m.getFirstname().regionMatches(true, start, s.toString(), start, count)){
							lv.smoothScrollToPosition(pos);
							found = true;
						}
					}else
						found = true;
				}
			}
		}
	}

	class MemberlistClickListener implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long i){
			Member memberClicked = membersList.get(position);
			mCallback.memberClicked("payment", memberClicked); // callback to MainActivity
		}
	}

} // end of AllMembers Fragment