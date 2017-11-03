package com.inka3d.inkaalarm;


abstract public class Notification {
	// id of notification (key in database)
	final int id;

	public Notification(int id) {
		this.id = id;
	}

	abstract public void setName(String name);
	abstract public String getName();

	abstract public void setRingtone(String ringtoneName, String ringtoneUri);
	abstract public String getRingtoneName();
	abstract public String getRingtoneUri();

	abstract public void setRequest(String request);
	abstract public String getRequest();
}
