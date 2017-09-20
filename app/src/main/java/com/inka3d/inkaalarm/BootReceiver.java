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
		// device has booted: load alarms and set to alarm manager
		List<Alarm> alarms = new ArrayList<>();
		try {
			// read alarms
			Data data = new Data();
			try {
				data.load(context);
			} catch (Exception e) {
			}

			// set alarms to android
			for (Alarm alarm : data.alarms) {
				alarm.set(context);
			}
		} catch (Exception e) {
			// could not read alarms. ignore for now
		}
	}
}
