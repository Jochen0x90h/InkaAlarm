package com.inka3d.inkaalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

abstract public class Alarm {
	// id of alarm (key in database and alarm manager)
	final int id;

	abstract void setEnabled(boolean enabled);
	abstract boolean isEnabled();

	abstract void setTime(int hour, int minute);
	abstract int getHour();
	abstract int getMinute();

	abstract void setDays(int dayBits);
	abstract int getDays();

	abstract void setNotificationId(int notificationId);
	abstract int getNotificationId();


	public Alarm(int id) {
		this.id = id;
	}

	/**
	 * Set alarm to android AlarmManager
	 * @param context context (e.g. activity) to used to get the AlarmManager
	 */
	protected void set(Context context) {
		int days = getDays();
		if (isEnabled() && days != 0) {
			AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

			Calendar calendar = Calendar.getInstance();
			long now = calendar.getTimeInMillis();
			calendar.setLenient(true);
			calendar.set(Calendar.HOUR_OF_DAY, getHour());
			calendar.set(Calendar.MINUTE, getMinute());
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			long time = calendar.getTimeInMillis();
			int weekDay = getWeekDay(calendar);

			// check if rollover to next day
			if (time <= now) {
				time += AlarmManager.INTERVAL_DAY;
				++weekDay;
			}

			Intent intent = new Intent(context, AlarmReceiver.class);
			//Intent intent = new Intent("com.inka3d.inkaalarm.ALARM_RECIEVED");
			intent.putExtra("id", this.id);//this.notificationId);
			for (int d = weekDay; d < weekDay + 7; ++d) {
				int day = d % 7;
				if ((days & (1 << day)) != 0) {
					PendingIntent pendingIntent = PendingIntent.getBroadcast(context, this.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
					am.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
					break;
				}

				// next day
				time += AlarmManager.INTERVAL_DAY;
			}
		}
	}

	/**
	 * Cancel alarm in android AlarmManager
	 * @param context context (e.g. activity) to used to get the AlarmManager
	 */
	protected void cancel(Context context) {
		if (isEnabled() && getDays() != 0) {
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

			Intent intent = new Intent(context, AlarmReceiver.class);
			//Intent intent = new Intent("com.inka3d.inkaalarm.ALARM_RECIEVED");
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, this.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			am.cancel(pendingIntent);
		}
	}

	// get day of week when the alarm takes place the next time (ignoring dayBits)
	public static int getWeekDay(Calendar calendar) {
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
