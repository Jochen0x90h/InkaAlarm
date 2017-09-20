package com.inka3d.inkaalarm;


import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NotificationsFragment extends Fragment {

	RecyclerView recyclerView;
	RecyclerView.LayoutManager layoutManager;
	NotificationsAdapter adapter;

	Ringtone ringtone;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// inflate the layout for this fragment
		View view = inflater.inflate(R.layout.notifications, container, false);

		// init recycler view
		// https://developer.android.com/training/material/lists-cards.html
		// https://www.androidhive.info/2016/01/android-working-with-recycler-view/
		this.recyclerView = view.findViewById(R.id.notification_recycler);
		this.recyclerView.setHasFixedSize(true);
		this.layoutManager = new LinearLayoutManager(getContext());
		this.recyclerView.setLayoutManager(this.layoutManager);

		// add swipe callback to remove items on swipe
		ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0,
				ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
			@Override
			public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
				return false;
			}

			@Override
			public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
				// remove swiped item from list and notify recyclerView
				int index = viewHolder.getAdapterPosition();
				Log.i("ItemTouchHelper", "onSwiped " + swipeDir);

				// set notificationId of alarms to -1 that use this notification
				Notification notification = adapter.notifications.get(index);
				for (Alarm alarm : adapter.alarms) {
					if (alarm.notificationId == notification.id)
						alarm.notificationId = -1;
				}

				adapter.notifications.remove(index);
				adapter.notifyItemRemoved(index);
			}
		};
		ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
		itemTouchHelper.attachToRecyclerView(this.recyclerView);

		// set list of alarms
		this.adapter = new NotificationsAdapter(this);
		this.recyclerView.setAdapter(this.adapter);

		// add listenener to floating add button
		View addButton = view.findViewById(R.id.add_notification);
		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				addNotification(view);
			}
		});

		return view;
	}

	int createNotificationId() {
		for (int id = 0; ; ++id) {
			boolean free = true;
			for (Notification notification : this.adapter.notifications) {
				if (notification.id == id) {
					free = false;
					break;
				}
			}
			if (free)
				return  id;
		}
	}

	public void addNotification(View view) {
		Log.i("NotificationsFragment", "addNotification");
		int index = this.adapter.notifications.size();
		this.adapter.notifications.add(new Notification(createNotificationId()));
		this.adapter.notifyItemInserted(index);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (this.ringtone != null) {
			this.ringtone.stop();
			this.ringtone = null;
		}

	}

	void play(String uri) {
		if (this.ringtone == null || !this.ringtone.isPlaying()) {
			if (uri != null) {
				this.ringtone = RingtoneManager.getRingtone(getContext(), Uri.parse(uri));
				this.ringtone.play();
			}
		} else {
			this.ringtone.stop();
			this.ringtone = null;
		}
	}
}