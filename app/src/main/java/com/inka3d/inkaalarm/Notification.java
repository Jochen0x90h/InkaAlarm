package com.inka3d.inkaalarm;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

/**
 * Data container for a notification (contains a list of actions that are done when an alarm goes off)
 */
abstract public class Notification {
	// id of notification (key in database)
	final int id;

	public Notification(int id) {
		this.id = id;
	}

	abstract public void setName(String name);
	abstract public String getName();

	abstract public void setRingtone(String ringtoneName, Uri ringtoneUri);
	abstract public String getRingtoneName();
	abstract public Uri getRingtoneUri();

	public static class Command {
		int blinds;
		int slat;

		Command(int blinds, int slat) {
			this.blinds = blinds;
			this.slat = slat;
		}
		/*String host;
		String command;

		Command(String host, String command) {
			this.host = host;
			this.command = command;
		}*/
	}

	//abstract public void setCommand(String host, String command);
	abstract public void setCommand(int blinds, int slat);
	abstract public Command getCommand();


	static Ringtone playRingtone(Context context, Uri uri) {
		Ringtone ringtone = RingtoneManager.getRingtone(context, uri);

		// set usage to alarm so that alarm volume is used
		ringtone.setAudioAttributes(new AudioAttributes.Builder()
				.setUsage(AudioAttributes.USAGE_ALARM)
				.build());

		// play alarm tone
		ringtone.play();

		return ringtone;
	}
}
