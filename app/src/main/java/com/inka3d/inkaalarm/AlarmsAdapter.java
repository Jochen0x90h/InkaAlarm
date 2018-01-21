package com.inka3d.inkaalarm;

import android.app.DialogFragment;
import android.os.Bundle;
import android.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Represents one row in the list of alarms in a AlarmsFragment
 */
public class AlarmsAdapter extends RecyclerView.Adapter<AlarmsAdapter.ViewHolder> {
	FragmentManager fragmentManager;
	public Data data;


	public static final int [] dayIds = {
			R.id.monday,
			R.id.tuesday,
			R.id.wednesday,
			R.id.thursday,
			R.id.friday,
			R.id.saturday,
			R.id.sunday};

	// Provide a reference to the views for each data item
	public class ViewHolder extends RecyclerView.ViewHolder {
		public TextView time;
		public TextView ampm;
		public TextView countdown;
		public Spinner notification;
		public Switch enabled;
		public ToggleButton [] days = new ToggleButton[7];

		public ViewHolder(View v) {
			super(v);
			this.time = v.findViewById(R.id.time);
			this.ampm = v.findViewById(R.id.ampm);
			this.countdown = v.findViewById(R.id.countdown);
			this.notification = v.findViewById(R.id.notification);
			this.enabled = v.findViewById(R.id.enable);
			for (int i = 0; i < 7; ++i)
				this.days[i] = v.findViewById(dayIds[i]);

			// fill drop-down list: https://www.mkyong.com/android/android-spinner-drop-down-list-example/
			List<String> names = new ArrayList<>();
			names.add("Default");
			data.getNotificationNames(names);
			/*
			for (Notification notification : data.notifications) {
				names.add(notification.name);
			}*/
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(v.getContext(),
					android.R.layout.simple_spinner_item, names);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			this.notification.setAdapter(dataAdapter);
		}

		public void set(Alarm alarm) {
			setTime(alarm);
			this.enabled.setChecked(alarm.isEnabled());
			int days = alarm.getDays();
			for (int i = 0; i < 7; ++i) {
				this.days[i].setChecked((days & (1 << i)) != 0);
			}

			// set index of notification to dropdown (0 is default, 1 is first notification)
			this.notification.setSelection(data.getNotificationIndexById(alarm.getNotificationId()) + 1);
		}

		public void setTime(Alarm alarm) {
			int hour = alarm.getHour();
			int minute = alarm.getMinute();

			// set time view
			android.text.format.DateFormat dateFormat = new android.text.format.DateFormat();
			if (dateFormat.is24HourFormat(this.time.getContext())) {
				// 24 hour
				this.time.setText(hour + ":" + String.format("%02d", minute));
				this.ampm.setText("");
			} else {
				// 12 hour am/pm
				int h12 = hour % 12;
				h12 = h12 == 0 ? 12 : h12;
				this.time.setText(h12 + ":" + String.format("%02d", minute));
				this.ampm.setText(hour < 12 ? "AM" : "PM");
			}

			setCountdown(alarm, Calendar.getInstance());
		}

		public void setCountdown(Alarm alarm, Calendar now) {
			int days = alarm.getDays();
			if (alarm.isEnabled() && days != 0) {
				int aMinutes = alarm.getHour() * 60 + alarm.getMinute();
				int nMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
				int weekDay = Alarm.getWeekDay(now);

				// calculate countdown time
				int countdown = aMinutes - nMinutes;
				if (countdown < 0) {
					// alarm time has already passed for current day
					countdown += 24 * 60;
					++weekDay;
				}

				// search for first day for which the alarm is enabled
				for (int d = weekDay; d < weekDay + 7; ++d) {
					int day = d % 7;
					if ((days & (1 << day)) != 0) {

						break;
					}
					countdown += 24 * 60;
				}

				// set countdown view
				this.countdown.setText(countdown / 60 + ":" + String.format("%02d", countdown % 60));
			} else {
				this.countdown.setText("");
			}
		}
	}

	// constructor
	public AlarmsAdapter(AlarmsFragment fragment) {
		MainActivity mainActivity = (MainActivity)fragment.getActivity();
		this.fragmentManager = mainActivity.getFragmentManager();
		this.data = mainActivity.data;
	}

	// create new views (invoked by the layout manager)
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		//Log.i("AlarmListAdapter", "onCreateViewHolder");

		// create a new view
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_row, parent, false);

		// wrap into a ViewHolder that caches pointers to the sub-views
		return new ViewHolder(view);
	}

	// replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
		// get alarm at given position
		final Alarm alarm = this.data.getAlarm(position);

		// replace the contents of the view for the list row
		viewHolder.set(alarm);

		// set listener to alarm time
		viewHolder.time.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// create the time picker dialog
				DialogFragment f = new TimePickerFragment();

				// set current time of alarm for time picker
				Bundle args = new Bundle();
				args.putInt("index", position);
				args.putInt("hour", alarm.getHour());
				args.putInt("minute", alarm.getMinute());
				f.setArguments(args);

				// show TimePickerFragment to select a time
				f.show(fragmentManager, "timePicker");
			}
		});

		// set listener to notification dropdown
		viewHolder.notification.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int dropdownPosition, long id) {
				// get notification id for dropdown position (position 0 is default)
				int notificationId = 0;
				if (dropdownPosition >= 1 && dropdownPosition <= data.getNotificationCount())
					notificationId = data.getNotificationId(dropdownPosition - 1);

				// set notification id
				if (alarm.getNotificationId() != notificationId)
					alarm.setNotificationId(notificationId);
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});

		// set listener to on/off switch
		viewHolder.enabled.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Switch sw = (Switch)view;

				// set enabled state to alarm
				alarm.setEnabled(sw.isChecked());

				// set alarm to view (update or hide countdown)
				viewHolder.setCountdown(alarm, Calendar.getInstance());

				Log.i("toggleAlarm", viewHolder.getAdapterPosition() + " " + sw.isChecked());
			}
		});

		// set listener to day buttons
		View.OnClickListener l = new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ToggleButton button = (ToggleButton)view;

				// get day bit
				int dayBit = 1;
				for (int id : AlarmsAdapter.dayIds) {
					if (id == view.getId()) {
						break;
					}
					dayBit <<= 1;
				}

				// set day to alarm
				if (button.isChecked())
					alarm.setDays(alarm.getDays() | dayBit);
				else
					alarm.setDays(alarm.getDays() & ~dayBit);

				// set alarm to view (update or hide countdown)
				viewHolder.setCountdown(alarm, Calendar.getInstance());

				Log.i("toggleDay", viewHolder.getAdapterPosition() + " " + dayBit + " " + button.isChecked());
			}
		};
		for (int i = 0; i < 7; ++i)
			viewHolder.days[i].setOnClickListener(l);
	}

	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount() {
		return data.getAlarmCount();
	}
}
