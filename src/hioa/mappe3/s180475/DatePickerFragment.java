package hioa.mappe3.s180475;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{
	OnDateChange mCallback;
	
	//Interface for communicating with MainActivity
	public interface OnDateChange{
		public void dateSet(String tag, int year, int month, int day);
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState){
		Bundle bundle = this.getArguments();
		final Calendar cal = Calendar.getInstance(); //Default date for the picker
		int day = bundle.getInt("day", cal.get(Calendar.DAY_OF_MONTH));
		int month = bundle.getInt("month", cal.get(Calendar.MONTH));
		int year = bundle.getInt("year", cal.get(Calendar.YEAR));
		
		return new DatePickerDialog(getActivity(), this, year, month-1, day);
	}

	//When date is changed in the datepickerfragment this method is called
	@Override
	public void onDateSet(DatePicker view, int year, int month, int day) {

		int correctMonth = month + 1;	//Januar=0
		Bundle bundle = this.getArguments();
		String someDate = bundle.getString("date");
		mCallback.dateSet(someDate, year, correctMonth, day);
	}
	//Makes sure MainActivity has implemented OnDateChange
	public void onAttach(Activity activity){
		super.onAttach(activity);
		
		try{
			mCallback = (OnDateChange) activity;
		}catch(ClassCastException e){
			throw new ClassCastException(activity.toString() + " must implement OnDateChange");
		}
	}
}
