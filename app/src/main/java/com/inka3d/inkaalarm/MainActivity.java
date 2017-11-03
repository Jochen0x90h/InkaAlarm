package com.inka3d.inkaalarm;

import java.util.Calendar;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Switch;
import android.widget.ToggleButton;


// https://material.io/icons
public class MainActivity extends AppCompatActivity {
	Data data;
	AlarmsFragment alarmsFragment = null;
	NotificationsFragment notificationsFragment = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		this.data = new Data(this);


		// from https://developer.android.com/training/basics/fragments/fragment-ui.html
		// check that the activity is using the layout version with the fragment_container FrameLayout
		if (findViewById(R.id.single_fragment) != null) {

			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null) {
				Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.single_fragment);

				if (fragment instanceof AlarmsFragment)
					this.alarmsFragment = (AlarmsFragment)fragment;
				else if (fragment instanceof NotificationsFragment)
					this.notificationsFragment = (NotificationsFragment)fragment;

				return;
			}

			// Create a new Fragment to be placed in the activity layout
			this.alarmsFragment = new AlarmsFragment();

			// In case this activity was started with special instructions from an
			// Intent, pass the Intent's extras to the fragment as arguments
			this.alarmsFragment.setArguments(getIntent().getExtras());

			// Add the fragment to the 'single_fragment' FrameLayout
			getSupportFragmentManager().beginTransaction().add(R.id.single_fragment, this.alarmsFragment).commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// save data
		//this.data.save();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// switch fragment according to menu button
		switch (item.getItemId()) {
			case R.id.alarms:
				if (this.alarmsFragment == null) {
					this.alarmsFragment = new AlarmsFragment();
					getSupportFragmentManager().beginTransaction()
							.replace(R.id.single_fragment, this.alarmsFragment)
							.commit();
					this.notificationsFragment = null;
				}
				return true;
			case R.id.notifications:
				if (this.notificationsFragment == null) {
					this.notificationsFragment = new NotificationsFragment();
					getSupportFragmentManager().beginTransaction()
							.replace(R.id.single_fragment, this.notificationsFragment)
							.commit();
					this.alarmsFragment = null;
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
