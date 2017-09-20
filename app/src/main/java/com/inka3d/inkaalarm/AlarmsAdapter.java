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


public class AlarmsAdapter extends RecyclerView.Adapter<AlarmsAdapter.ViewHolder> {
	FragmentManager fragmentManager;
	public List<Notification> notifications = new ArrayList<>();
	public List<Alarm> alarms = new ArrayList<>();


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
			List<String> list = new ArrayList<>();
			list.add("Default");
			for (Notification notification : notifications) {
				list.add(notification.name);
			}
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(v.getContext(),
					android.R.layout.simple_spinner_item, list);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			this.notification.setAdapter(dataAdapter);
			//this.notification.setSelection(0);
		}

		public void set(Alarm alarm) {
			setTime(alarm);
			this.enabled.setChecked(alarm.enabled);
			for (int i = 0; i < 7; ++i) {
				days[i].setChecked((alarm.dayBits & (1 << i)) != 0);
			}

			// find index of notification (0 is default)
			int i = 1;
			for (Notification notification : notifications) {
				if (notification.id == alarm.notificationId) {
					this.notification.setSelection(i);
				}
			}
			if (i > notifications.size())
				this.notification.setSelection(0);
		}

		public void setTime(Alarm alarm) {
			// set time view
			android.text.format.DateFormat dateFormat = new android.text.format.DateFormat();
			if (dateFormat.is24HourFormat(this.time.getContext())) {
				// 24 hour
				this.time.setText(alarm.hour + ":" + String.format("%02d", alarm.minute));
				this.ampm.setText("");
			} else {
				// 12 hour am/pm
				int hour = alarm.hour % 12;
				hour = hour == 0 ? 12 : hour;
				this.time.setText(hour + ":" + String.format("%02d", alarm.minute));
				this.ampm.setText(alarm.hour < 12 ? "AM" : "PM");
			}

			setCountdown(alarm, Calendar.getInstance());
		}

		public void setCountdown(Alarm alarm, Calendar now) {
			if (alarm.enabled && alarm.dayBits != 0) {
				int aMinutes = alarm.hour * 60 + alarm.minute;
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
					if ((alarm.dayBits & (1 << day)) != 0) {

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
		this.notifications = mainActivity.data.notifications;
		this.alarms = mainActivity.data.alarms;
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
		final Alarm alarm = this.alarms.get(position);

		// replace the contents of the view for the list row
		viewHolder.set(alarm);

		// set listener to alarm time
		viewHolder.time.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// create the time pickerdialog
				DialogFragment f = new TimePickerFragment();

				// set initial time
				Bundle args = new Bundle();
				args.putInt("index", position);
				args.putInt("hour", alarm.hour);
				args.putInt("minute", alarm.minute);
				f.setArguments(args);

				// show TimePickerFragment to select a time
				f.show(fragmentManager, "timePicker");
			}
		});

		// set listener to notification dropdown
		viewHolder.notification.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
				if (position >= 1 && position <= notifications.size())
					alarm.notificationId = notifications.get(position - 1).id;
				else
					alarm.notificationId = -1;
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

				// cancel alarm
				alarm.cancel(view.getContext());

				// set enabled state to alarm
				alarm.enabled = sw.isChecked();

				// set alarm to view (update or hide countdown)
				viewHolder.setCountdown(alarm, Calendar.getInstance());

				// set alarm to android
				alarm.set(view.getContext());

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

				// cancel alarm
				alarm.cancel(view.getContext());

				// set day to alarm
				if (button.isChecked())
					alarm.dayBits |= dayBit;
				else
					alarm.dayBits &= ~dayBit;

				// set alarm to view (update or hide countdown)
				viewHolder.setCountdown(alarm, Calendar.getInstance());

				// set alarm to android
				alarm.set(view.getContext());

				Log.i("toggleDay", viewHolder.getAdapterPosition() + " " + dayBit + " " + button.isChecked());
			}
		};
		for (int i = 0; i < 7; ++i)
			viewHolder.days[i].setOnClickListener(l);
	}

	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount() {
		return alarms.size();
	}
}
