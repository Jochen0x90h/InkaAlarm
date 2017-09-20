package com.inka3d.inkaalarm;

import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;


public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
	NotificationsFragment notificationsFragment;
	List<Notification> notifications = new ArrayList<>();
	List<Alarm> alarms = new ArrayList<>();

	// ringtone names and uris that are available on this phone
	List<String> ringtoneNames = new ArrayList<>();
	List<String> ringtoneUris = new ArrayList<>();


	// Provide a reference to the views for each data item
	public class ViewHolder extends RecyclerView.ViewHolder {
		public EditText name;
		TextWatcher nameWatcher;
		public Spinner ringtone;
		ImageButton testRingtone;
		public EditText request;

		public ViewHolder(View v) {
			super(v);
			this.name = v.findViewById(R.id.name);
			this.ringtone = v.findViewById(R.id.ringtone);
			this.testRingtone = v.findViewById(R.id.test_ringtone);
			this.request = v.findViewById(R.id.request);

			// drop-down list: https://www.mkyong.com/android/android-spinner-drop-down-list-example/
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(v.getContext(),
					android.R.layout.simple_spinner_item, ringtoneNames);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			this.ringtone.setAdapter(dataAdapter);
		}

		public void set(Notification notification) {
			this.name.setText(notification.name);
			int index = ringtoneUris.indexOf(notification.ringtoneUri);
			if (index == -1)
				index = 0;
			this.ringtone.setSelection(index);
			this.request.setText(notification.request);
		}
	}

	// constructor
	public NotificationsAdapter(NotificationsFragment fragment) {
		this.notificationsFragment = fragment;
		MainActivity mainActivity = (MainActivity)fragment.getActivity();
		this.notifications = mainActivity.data.notifications;
		this.alarms = mainActivity.data.alarms;

		this.ringtoneNames.add("Default");
		this.ringtoneUris.add(null);

		// get names and urls of ringtones
		RingtoneManager manager = new RingtoneManager(fragment.getContext());
		manager.setType(RingtoneManager.TYPE_ALARM);
		Cursor cursor = manager.getCursor();
		if (cursor.moveToFirst()) {
			do {
				int position = cursor.getPosition();

				String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
				String uri = manager.getRingtoneUri(position).toString();
				//String uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
				//Log.i("NotificationsAdapter.ViewHolder", "title " + title + " uri " + uri);
				this.ringtoneNames.add(title);
				this.ringtoneUris.add(uri);
			} while (cursor.moveToNext());
		}

	}

	// create new views (invoked by the layout manager)
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		//Log.i("AlarmListAdapter", "onCreateViewHolder");

		// create a new view
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_row, parent, false);

		// wrap into a ViewHolder that caches pointers to the sub-views
		return new ViewHolder(v);
	}

	// replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position) {
		// get alarm at given position
		final Notification notification = this.notifications.get(position);

		// replace the contents of the view for the list row
		viewHolder.set(notification);

		// set listeners
		viewHolder.name.removeTextChangedListener(viewHolder.nameWatcher);
		viewHolder.name.addTextChangedListener(viewHolder.nameWatcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				notification.name = charSequence.toString();
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
		viewHolder.ringtone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
				notification.ringtoneName = ringtoneNames.get(position);
				notification.ringtoneUri = ringtoneUris.get(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});
		viewHolder.testRingtone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				notificationsFragment.play(notification.ringtoneUri);
			}
		});
	}

	@Override
	public int getItemCount() {
		return this.notifications.size();
	}
}
