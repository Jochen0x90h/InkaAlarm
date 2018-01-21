package com.inka3d.inkaalarm;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * This fragment is for configuring the alarms (alarm clock icon in the menu bar)
 */
public class AlarmsFragment extends Fragment implements Runnable {

	RecyclerView recyclerView;
	RecyclerView.LayoutManager layoutManager;
	AlarmsAdapter adapter;

	// countdown refresh
	final int REFRESH_INTERVAL = 10000;
	Handler handler = new Handler();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// inflate the layout for this fragment
		View view = inflater.inflate(R.layout.alarms, container, false);

		// init recycler view
		// https://developer.android.com/training/material/lists-cards.html
		// https://www.androidhive.info/2016/01/android-working-with-recycler-view/
		this.recyclerView = view.findViewById(R.id.alarm_recycler);
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
				adapter.data.getAlarm(index).cancel(getContext());
				adapter.data.removeAlarm(index);
				adapter.notifyItemRemoved(index);
			}
		};
		ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
		itemTouchHelper.attachToRecyclerView(this.recyclerView);

		// set list of alarms
		this.adapter = new AlarmsAdapter(this);
		this.recyclerView.setAdapter(this.adapter);

		// add listenener to floating add button
		View addButton = view.findViewById(R.id.add_alarm);
		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				addAlarm(view);
			}
		});

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		// set countdowns and start countdown refresh
		run();
	}

	@Override
	public void onPause() {
		super.onPause();

		// stop countdown refresh
		this.handler.removeCallbacks(this);
	}

	public void addAlarm(View view) {
		Log.i("AlarmsFragment", "addAlarm");
		int index = this.adapter.data.addAlarm();
		this.adapter.notifyItemInserted(index);
	}

	// gets called from the TimePickerFragment
	void setTime(int index, int hour, int minute) {
		Log.i("setTime", index + " " + hour + ":" + minute);

		// get alarm
		Alarm alarm = this.adapter.data.getAlarm(index);

		// set time to alarm
		alarm.setTime(hour, minute);

		// set alarm time to view if this alarm is currently visible in the list
		AlarmsAdapter.ViewHolder viewHolder = (AlarmsAdapter.ViewHolder)recyclerView.findViewHolderForAdapterPosition(index);
		if (viewHolder != null) {
			viewHolder.setTime(alarm);
		}
	}

	@Override
	public void run() {
		// refresh countdowns
		this.adapter.notifyDataSetChanged();

		// request next call to run() after an interval
		this.handler.postDelayed(this, REFRESH_INTERVAL);
	}
}
