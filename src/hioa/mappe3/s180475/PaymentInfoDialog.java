package hioa.mappe3.s180475;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class PaymentInfoDialog extends DialogFragment{
	
	static PaymentInfoDialog newInstance(String titel, String date, String typeOfMembership){
		PaymentInfoDialog rfd = new PaymentInfoDialog();
		Bundle args = new Bundle();
		args.putString("Titel", titel);
		args.putString("Date", date);
		args.putString("TypeOfMembership", typeOfMembership);
		rfd.setArguments(args);
		return rfd;
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState){
		String title = getArguments().getString("Titel");
		String message = "Registrert: " + getArguments().getString("Date") + "\nType medlemskap: " + getArguments().getString("TypeOfMembership");
		Dialog myDialog = new AlertDialog.Builder(getActivity())
		.setTitle(title)
		.setMessage(message)
		.create();
		
		return myDialog;
	}
}