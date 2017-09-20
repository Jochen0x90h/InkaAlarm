package com.inka3d.inkaalarm;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

// this activity starts when an alarm goes off
public class AlarmActivity extends Activity {

	Ringtone ringtone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.alarm);

		// load alarms
		Data data = new Data();
		try {
			data.load(this);
		} catch (Exception e) {
		}

		// find the alarm that went off
		int id = getIntent().getIntExtra("id", -1);
		Button stop = findViewById(R.id.stop);
		stop.setText(Integer.toString(id));
		for (Notification notification : data.notifications) {
			if (notification.id == id) {
				if (notification.ringtoneUri != null) {
					Uri uri = Uri.parse(notification.ringtoneUri);
					this.ringtone = RingtoneManager.getRingtone(this, uri);
				}
			}
		}

		// no ringtone was found, get default alarm
		if (this.ringtone == null) {
			Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
			this.ringtone = RingtoneManager.getRingtone(this, uri);
		}

		// set usage to alarm so that alarm volume is used
		this.ringtone.setAudioAttributes(new AudioAttributes.Builder()
			.setUsage(AudioAttributes.USAGE_ALARM)
			.build());

		// play alarm
		this.ringtone.play();

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	void stop(View v) {
		this.ringtone.stop();

		// start main activity again
		Intent i = new Intent(this, MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}
}
