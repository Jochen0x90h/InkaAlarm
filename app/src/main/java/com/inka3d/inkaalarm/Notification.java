package com.inka3d.inkaalarm;


public class Notification {
	// id of notification (needed to find the notification in AlarmActivity)
	int id;

	String name;

	String ringtoneName;
	String ringtoneUri;

	String request;

	public Notification(int id) {
		this.id = id;
		this.name = "";
	}

	public Notification(int id, String name, String ringtoneName, String ringtoneUri) {
		this.name = name;
		this.ringtoneName = ringtoneName;
		this.ringtoneUri = ringtoneUri;
	}
}
