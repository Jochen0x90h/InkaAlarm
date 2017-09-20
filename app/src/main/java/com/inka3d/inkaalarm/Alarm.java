package com.inka3d.inkaalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;


public class Alarm {
	// id of alarm (needed to cancel the alarm)
	int id;

	// alarm is enabled
	boolean enabled;

	// alarm time
	int hour;
	int minute;

	// for each day a flag indicating if the alarm should take place
	int dayBits;

	// id of notification to start when alarm goes off
	int notificationId;

	Alarm(int id) {
		this.id = id;
		this.enabled = false;
		this.hour = 0;
		this.minute = 0;
		this.dayBits = 0;
		this.notificationId = -1;
	}

	Alarm(int id, boolean enabled, int hour, int minute, int dayBits, int notificationId) {
		this.id = id;
		this.enabled = enabled;
		this.hour = hour;
		this.minute = minute;
		this.dayBits = dayBits;
		this.notificationId = notificationId;
	}

	void set(int hour, int minute) {
		this.hour = hour;
		this.minute = minute;
	}

	/**
	 * Set alarm to android AlarmManager
	 * @param context context (e.g. activity) to used to get the AlarmManager
	 */
	void set(Context context) {
		if (this.enabled && this.dayBits != 0) {
			final int millisPerDay = 24 * 60 * 60 * 1000;
			AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

			Calendar calendar = Calendar.getInstance();
			long now = calendar.getTimeInMillis();
			calendar.setLenient(true);
			calendar.set(Calendar.HOUR_OF_DAY, hour);
			calendar.set(Calendar.MINUTE, minute);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.getTimeInMillis(); // recompute internal time
			long time = calendar.getTimeInMillis();
			int weekDay = getWeekDay(calendar);

			// check if rollover to next day
			if (time < now) {
				time += millisPerDay;
				++weekDay;
			}

			//Intent intent = new Intent(context, AlarmReceiver.class);
			Intent intent = new Intent("com.inka3d.inkaalarm.ALARM_RECIEVED");
			intent.putExtra("id", this.notificationId);
			for (int d = weekDay; d < weekDay + 7; ++d) {
				int day = d % 7;
				if ((this.dayBits & (1 << day)) != 0) {
					PendingIntent pendingIntent = PendingIntent.getBroadcast(context, this.id * 7 + day, intent, PendingIntent.FLAG_UPDATE_CURRENT);
					am.setRepeating(AlarmManager.RTC_WAKEUP, time, 7 * millisPerDay, pendingIntent);
				}

				// next day
				time += millisPerDay;
			}
		}
	}

	/**
	 * Cancel alarm in android AlarmManager
	 * @param context context (e.g. activity) to used to get the AlarmManager
	 */
	void cancel(Context context) {
		if (this.enabled && this.dayBits != 0) {
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

			Intent intent = new Intent("com.inka3d.inkaalarm.ALARM_RECIEVED");
			for (int day = 0; day < 7; ++day) {
				if ((this.dayBits & (1 << day)) != 0) {
					PendingIntent pendingIntent = PendingIntent.getBroadcast(context, this.id * 7 + day, intent, 0);
					am.cancel(pendingIntent);
				}
			}
		}
	}

	// get day of week when the alarm takes place the next time (ignoring dayBits)
	static int getWeekDay(Calendar calendar) {
		switch (calendar.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.MONDAY:
				return 0;
			case Calendar.TUESDAY:
				return 1;
			case Calendar.WEDNESDAY:
				return 2;
			case Calendar.THURSDAY:
				return 3;
			case Calendar.FRIDAY:
				return 4;
			case Calendar.SATURDAY:
				return 5;
			default:
				return 6;
		}
	}
}
