package com.inka3d.inkaalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

/**
 * called by android whan an alarm goes off
 */
public class AlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP |
				PowerManager.ON_AFTER_RELEASE, "Alarm");
		wakeLock.acquire();

		Data data = new Data(context);

		// get id of alarm that went off
		int id = intent.getIntExtra("id", -1);
		Alarm alarm = data.getAlarmById(id);

		// set next alarm
		alarm.set(context);

		// start NotificationActivity
		Intent i = new Intent(context, NotificationActivity.class);
		i.putExtra("id", alarm.getNotificationId());
		i.addFlags(Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		context.startActivity(i);

		wakeLock.release();
	}
}
