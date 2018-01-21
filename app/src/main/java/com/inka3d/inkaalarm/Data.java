package com.inka3d.inkaalarm;

import android.content.Context;
import android.net.Uri;
import android.util.AtomicFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Data container for all data (alarms, notifications) that uses json files
 */
public class Data {
	protected Context context;
	protected JSONObject jRoot;
	protected JSONArray jNotifications;
	protected JSONArray jAlarms;


	class JsonNotification extends Notification {
		JSONObject jNotification;

		JsonNotification(int id, JSONObject jNotification) {
			super(id);
			this.jNotification = jNotification;
		}

		@Override
		public void setName(String name) {
			try {
				this.jNotification.put("name", name);
			} catch (JSONException e) {
			}
			save();
		}

		@Override
		public String getName() {
			return jNotification.optString("name");
		}

		@Override
		public void setRingtone(String ringtoneName, Uri ringtoneUri) {
			try {
				this.jNotification.put("ringtoneName", ringtoneName);
				this.jNotification.put("ringtoneUri", ringtoneUri.toString());
			} catch (JSONException e) {
			}
			save();
		}

		@Override
		public String getRingtoneName() {
			return jNotification.optString("ringtoneName");
		}

		@Override
		public Uri getRingtoneUri() {
			// returns empty string ("") if not found
			return Uri.parse(jNotification.optString("ringtoneUri"));
		}

		@Override
		public void setCommand(int blinds, int slat) {//String host, String command) {
			try {
				//this.jNotification.put("host", host);
				//this.jNotification.put("command", command);
				this.jNotification.put("blinds", blinds);
				this.jNotification.put("slat", slat);
			} catch (JSONException e) {
			}
			save();
		}

		@Override
		public Command getCommand() {
			return new Command(
					jNotification.optInt("blinds"),
					jNotification.optInt("slat"));
			//return new Command(
			//		jNotification.optString("host"),
			//		jNotification.optString("command"));
		}
	}

	class JsonAlarm extends Alarm {
		JSONObject jAlarm;

		JsonAlarm(int id, JSONObject jAlarm) {
			super(id);
			this.jAlarm = jAlarm;
		}

		@Override
		void setEnabled(boolean enabled) {
			cancel(context);
			try {
				this.jAlarm.put("enabled", enabled);
			} catch (JSONException e) {
			}
			set(context);
			save();
		}

		@Override
		boolean isEnabled() {
			return this.jAlarm.optBoolean("enabled");
		}

		@Override
		void setTime(int hour, int minute) {
			cancel(context);
			try {
				this.jAlarm.put("hour", hour);
				this.jAlarm.put("minute", minute);
			} catch (JSONException e) {
			}
			set(context);
			save();
		}

		@Override
		int getHour() {
			return this.jAlarm.optInt("hour");
		}

		@Override
		int getMinute() {
			return this.jAlarm.optInt("minute");
		}

		@Override
		void setDays(int days) {
			cancel(context);
			try {
				this.jAlarm.put("days", days);
			} catch (JSONException e) {
			}
			set(context);
			save();
		}

		@Override
		int getDays() {
			return this.jAlarm.optInt("days");
		}

		@Override
		void setNotificationId(int notificationId) {
			try {
				this.jAlarm.put("notificationId", notificationId);
			} catch (JSONException e) {
			}
			save();
		}

		@Override
		int getNotificationId() {
			return this.jAlarm.optInt("notificationId");
		}
	}


	Data(Context context) {
		this.context = context;

		File dir = this.context.getFilesDir();
		File path = new File(dir, "data.json");

		// load data
		try (InputStream is = new FileInputStream(path)) {
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			String json = new String(buffer, "UTF-8");
			this.jRoot = new JSONObject(json);

			// get notifications
			this.jNotifications = this.jRoot.getJSONArray("notifications");

			// get alarms
			this.jAlarms = this.jRoot.getJSONArray("alarms");

		} catch (Exception e) {
			// load failed: create new json objects
			try {
				this.jRoot = new JSONObject();
				this.jNotifications = new JSONArray();
				this.jRoot.put("notifications", jNotifications);
				this.jAlarms = new JSONArray();
				this.jRoot.put("alarms", jAlarms);
			} catch (JSONException e2) {
			}
		}
	}

	int getNotificationCount() {
		return this.jNotifications.length();
	}

	int addNotification() {
		// get next id (assumes that id's are in ascending order and may be reused)
		int id = 1;
		int index = this.jNotifications.length();
		if (index > 0)
			id = getNotificationId(index - 1) + 1;

		// add new notification
		try {
			JSONObject jNotification = new JSONObject();
			jNotification.put("id", id);
			this.jNotifications.put(jNotification);
		} catch (JSONException e) {
		}
		return index;
	}

	void removeNotification(int index) {
		int notificationId = getNotificationId(index);

		// set notificationId of alarms to 0 that use this notification
		for (int alarmIndex = 0; alarmIndex < this.jNotifications.length(); ++alarmIndex) {
			try {
				JSONObject jAlarm = this.jAlarms.getJSONObject(alarmIndex);
				if (jAlarm.getInt("id") == notificationId)
					jAlarm.put("id", 0);
			} catch (JSONException e) {
			}
		}

		// remove notification
		this.jNotifications.remove(index);
		save();
	}

	Notification getNotification(int index) {
		try {
			JSONObject jNotification = this.jNotifications.getJSONObject(index);
			int id = jNotification.getInt("id");
			return new JsonNotification(id, jNotification);
		} catch (JSONException e) {
			return null;
		}
	}

	int getNotificationId(int index) {
		try {
			JSONObject jNotification = this.jNotifications.getJSONObject(index);
			int id = jNotification.getInt("id");
			return id;
		} catch (JSONException e) {
			return -1;
		}
	}

	Notification getNotificationById(int id) {
		for (int index = 0; index < this.jNotifications.length(); ++index) {
			if (getNotificationId(index) == id)
				return getNotification(index);
		}
		return null;
	}

	int getNotificationIndexById(int id) {
		for (int index = 0; index < this.jNotifications.length(); ++index) {
			if (getNotificationId(index) == id)
				return index;
		}
		return -1;
	}

	void getNotificationNames(List<String> names) {
		for (int index = 0; index < this.jNotifications.length(); ++index) {
			try {
				JSONObject jNotification = this.jNotifications.getJSONObject(index);
				names.add(jNotification.optString("name"));
			} catch (JSONException e) {
			}
		}
	}

	int getAlarmCount() {
		return this.jAlarms.length();
	}

	int addAlarm() {
		// get next id (assumes that id's are in ascending order and may be reused)
		int id = 1;
		int index = this.jAlarms.length();
		if (index > 0)
			id = getAlarmId(index - 1) + 1;

		// add new alarm
		try {
			JSONObject jAlarm = new JSONObject();
			jAlarm.put("id", id);
			this.jAlarms.put(jAlarm);
		} catch (JSONException e) {
		}
		return index;
	}

	void removeAlarm(int index) {
		this.jAlarms.remove(index);
		save();
	}

	Alarm getAlarm(int index) {
		try {
			JSONObject jAlarm = this.jAlarms.getJSONObject(index);
			int id = jAlarm.getInt("id");
			return new JsonAlarm(id, jAlarm);
		} catch (JSONException e) {
			return null;
		}
	}

	int getAlarmId(int index) {
		try {
			JSONObject jAlarm = this.jAlarms.getJSONObject(index);
			int id = jAlarm.getInt("id");
			return id;
		} catch (JSONException e) {
			return 0;
		}
	}

	Alarm getAlarmById(int id) {
		for (int index = 0; index < this.jAlarms.length(); ++index) {
			if (getAlarmId(index) == id)
				return getAlarm(index);
		}
		return null;
	}
/*
	int getAlarmIndexById(int id) {
		for (int index = 0; index < this.jAlarms.length(); ++index) {
			if (getAlarmId(index) == id)
				return index;
		}
		return -1;
	}
*/
	void save() {
		File dir = this.context.getFilesDir();
		File path = new File(dir, "data.json");
		AtomicFile file = new AtomicFile(path);
		try {
			FileOutputStream s = file.startWrite();
			s.write(this.jRoot.toString().getBytes(StandardCharsets.UTF_8));
			file.finishWrite(s);
		} catch (IOException e) {
		}
	}
}
