package hioa.mappe3.s180475;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

//This class creates each row in the listview.
public class MemberAdapter extends ArrayAdapter<Member> implements Filterable{
	private Context con;
	private ArrayList<Member> membersList;
	private TextView name, regDate;
	private ImageView image;	//Red or green gradient color
	private int typeOfSort;		//How list is sorted
	
	public MemberAdapter(Context context, int layoutResource, int textViewName, ArrayList<Member> members, int sort){
		super(context, layoutResource, textViewName, members);
		con = context;
		membersList = members;
		typeOfSort = sort;
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
		LayoutInflater inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row =  inflater.inflate(R.layout.list_item, parent, false);
		
		name = (TextView) row.findViewById(R.id.name);
		regDate = (TextView) row.findViewById(R.id.members_regDate);
		image = (ImageView) row.findViewById(R.id.paidOrNot);
		
		name.setText(membersList.get(position).getFirstname() + " " + membersList.get(position).getLastname());
		regDate.setText("Registrert: " + membersList.get(position).getStart());
		if(typeOfSort == 1 || typeOfSort == 3){	//Only want to show color if we're looking at 'all' or 'this or last year'
			if(!membersList.get(position).getPaidThisYear())
				image.setImageResource(R.drawable.gradient_notpaid);
			else
				image.setImageResource(R.drawable.gradient_paid);
		}
		return row;
	}
	
	public void setTypeOfSort(int s){
		typeOfSort = s;
	}
}