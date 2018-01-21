package com.inka3d.inkaalarm;


import android.content.Context;
import android.media.AudioAttributes;
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

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * This fragment is for configuring the notifications (bell icon in the menu bar)
 */
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

				adapter.data.removeNotification(index);
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

	public void addNotification(View view) {
		Log.i("NotificationsFragment", "addNotification");
		int index = this.adapter.data.addNotification();
		this.adapter.notifyItemInserted(index);
	}

	@Override
	public void onPause() {
		super.onPause();

		// stop ringtone if it is still playing
		if (this.ringtone != null) {
			this.ringtone.stop();
			this.ringtone = null;
		}
	}

	// called when play button next to the ringtone name is pressed
	void testRingtone(Uri uri) {
		if (this.ringtone == null || !this.ringtone.isPlaying()) {
			this.ringtone = Notification.playRingtone(getContext(), uri);
		} else {
			this.ringtone.stop();
			this.ringtone = null;
		}
	}
}