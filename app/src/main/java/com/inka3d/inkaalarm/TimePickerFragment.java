package com.inka3d.inkaalarm;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;


// https://developer.android.com/guide/topics/ui/dialogs.html
// https://developer.android.com/guide/topics/ui/controls/pickers.html
// https://developer.android.com/reference/android/app/DialogFragment.html
public class TimePickerFragment extends DialogFragment
		implements TimePickerDialog.OnTimeSetListener {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// use the current time as the default values for the picker
		//final Calendar c = Calendar.getInstance();
		//int hour = c.get(Calendar.HOUR_OF_DAY);
		//int minute = c.get(Calendar.MINUTE);

		// get time from arguments
		int hour = getArguments().getInt("hour");
		int minute = getArguments().getInt("minute");

		// create a new instance of TimePickerDialog and return it
		return new TimePickerDialog(getActivity(), this, hour, minute,
				DateFormat.is24HourFormat(getActivity()));
	}

	public void onTimeSet(TimePicker view, int hour, int minute) {
		// time was chosen by the user
		int index = getArguments().getInt("index");
		AlarmsFragment alarmsFragment = ((MainActivity)getActivity()).alarmsFragment;
		if (alarmsFragment != null)
			alarmsFragment.setTime(index, hour, minute);
	}
}
