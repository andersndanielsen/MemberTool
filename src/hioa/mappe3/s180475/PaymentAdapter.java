package hioa.mappe3.s180475;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

//This class creates each row in the payment listview
public class PaymentAdapter extends ArrayAdapter<Payment>{
	private Context con;
	private ArrayList<Payment> paymentList;
	private TextView year;
	
	public PaymentAdapter(Context context, int layoutResource, int textViewName, ArrayList<Payment> payments){
		super(context, layoutResource, textViewName, payments);
		con = context;
		paymentList = payments;
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
		LayoutInflater inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row =  inflater.inflate(R.layout.payment_list_item, parent, false);
		year = (TextView) row.findViewById(R.id.year);
		String date = paymentList.get(position).getYear();
		
		year.setText(date);
		
		return row;
	}
}