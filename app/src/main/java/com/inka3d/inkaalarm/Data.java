package com.inka3d.inkaalarm;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;


public class Data {
	public ArrayList<Notification> notifications = new ArrayList<>();
	public ArrayList<Alarm> alarms = new ArrayList<>();


	void load(Context context) throws Exception {
		File dir = context.getFilesDir();
		File path = new File(dir, "alarms.json");

		try (InputStream is = new FileInputStream(path)) {
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			String json = new String(buffer, "UTF-8");
			JSONObject jRoot = new JSONObject(json);

			// load notifications
			JSONArray jNotifications = jRoot.getJSONArray("notifications");
			for (int i = 0; i < jNotifications.length(); ++i) {
				JSONObject jNotification = jNotifications.getJSONObject(i);
				int id = jNotification.getInt("id");
				String name = jNotification.getString("name");
				String ringtoneName = jNotification.getString("ringtoneName");
				String ringtoneUri = jNotification.getString("ringtoneUri");
				this.notifications.add(new Notification(id, name, ringtoneName, ringtoneUri));
			}

			// load alarms
			JSONArray jAlarms = jRoot.getJSONArray("alarms");
			for (int i = 0; i < jAlarms.length(); ++i) {
				JSONObject jAlarm = jAlarms.getJSONObject(i);
				boolean enabled = jAlarm.getBoolean("enabled");
				int id = jAlarm.getInt("id");
				int hour = jAlarm.getInt("hour");
				int minute = jAlarm.getInt("minute");
				int dayBits = jAlarm.getInt("dayBits");
				int notificationId = jAlarm.getInt("notificationId");
				this.alarms.add(new Alarm(id, enabled, hour, minute, dayBits, notificationId));
			}
		}
	}

	void save(Context context) throws Exception {
		JSONObject jRoot = new JSONObject();

		// save notifications
		JSONArray jNotifications = new JSONArray();
		jRoot.put("notifications", jNotifications);
		for (Notification notification : this.notifications) {
			JSONObject jNotification = new JSONObject();
			jNotification.put("id", notification.id);
			jNotification.put("name", notification.name);
			jNotification.put("ringtoneName", notification.ringtoneName);
			jNotification.put("ringtoneUri", notification.ringtoneUri);
			jNotifications.put(jNotification);
		}

		// save alarms
		JSONArray jAlarms = new JSONArray();
		jRoot.put("alarms", jAlarms);
		for (Alarm alarm : this.alarms) {
			JSONObject jAlarm = new JSONObject();
			jAlarm.put("id", alarm.id);
			jAlarm.put("enabled", alarm.enabled);
			jAlarm.put("hour", alarm.hour);
			jAlarm.put("minute", alarm.minute);
			jAlarm.put("dayBits", alarm.dayBits);
			jAlarm.put("notificationId", alarm.notificationId);
			jAlarms.put(jAlarm);
		}


		File dir = context.getFilesDir();
		File path = new File(dir, "alarms.json");
		try (FileWriter file = new FileWriter(path)) {
			file.write(jRoot.toString());
		}
	}
}
