package com.inka3d.inkaalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

// called after android has booted
public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent bootIntent) {
		// device has booted: set alarms to alarm manager

		Data data = new Data(context);

		// set alarms to android
		int count = data.getAlarmCount();
		for (int i = 0; i < count; ++i) {
			data.getAlarm(i).set(context);
		}
	}
}
