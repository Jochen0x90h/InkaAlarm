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
import android.widget.SeekBar;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one row in the list of notifications in a NotificationsFragment
 */
public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
	NotificationsFragment notificationsFragment;
	Data data;

	// ringtone names and uris that are available on this phone
	List<String> ringtoneNames = new ArrayList<>();
	List<Uri> ringtoneUris = new ArrayList<>();


	// Provide a reference to the views for each data item
	public class ViewHolder extends RecyclerView.ViewHolder {
		public EditText name;
		TextWatcher nameWatcher;

		public Spinner ringtone;
		ImageButton testRingtone;

		/*public EditText host;
		TextWatcher hostWatcher;
		public EditText command;
		TextWatcher commandWatcher;*/
		public SeekBar blinds;
		public SeekBar slat;
		ImageButton testCommand;

		public ViewHolder(View v) {
			super(v);
			this.name = v.findViewById(R.id.name);

			this.ringtone = v.findViewById(R.id.ringtone);
			this.testRingtone = v.findViewById(R.id.test_ringtone);

			//this.host = v.findViewById(R.id.host);
			//this.command = v.findViewById(R.id.command);
			this.blinds = v.findViewById(R.id.blinds);
			this.slat = v.findViewById(R.id.slat);
			this.testCommand = v.findViewById(R.id.test_command);

			// drop-down list: https://www.mkyong.com/android/android-spinner-drop-down-list-example/
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(v.getContext(),
					android.R.layout.simple_spinner_item, ringtoneNames);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			this.ringtone.setAdapter(dataAdapter);
		}

		public void set(Notification notification) {
			this.name.setText(notification.getName());

			int index = ringtoneUris.indexOf(notification.getRingtoneUri());
			if (index == -1)
				index = 0;
			this.ringtone.setSelection(index);

			Notification.Command command = notification.getCommand();
			//this.host.setText(command.host);
			//this.command.setText(command.command);
			this.blinds.setProgress(command.blinds);
			this.slat.setProgress(command.slat);
		}
	}

	// constructor
	public NotificationsAdapter(NotificationsFragment fragment) {
		this.notificationsFragment = fragment;
		MainActivity mainActivity = (MainActivity)fragment.getActivity();
		this.data = mainActivity.data;

		// add default alarm tone
		this.ringtoneNames.add("Default");
		this.ringtoneUris.add(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));

		// get list of installed alarm tones
		RingtoneManager manager = new RingtoneManager(fragment.getContext());
		manager.setType(RingtoneManager.TYPE_ALARM);
		Cursor cursor = manager.getCursor();
		if (cursor.moveToFirst()) {
			do {
				int position = cursor.getPosition();

				String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
				Uri uri = manager.getRingtoneUri(position);
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
	public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
		// get alarm at given position
		final Notification notification = this.data.getNotification(position);

		// replace the contents of the view for the list row
		viewHolder.set(notification);

		// set name listener
		viewHolder.name.removeTextChangedListener(viewHolder.nameWatcher);
		viewHolder.name.addTextChangedListener(viewHolder.nameWatcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				notification.setName(charSequence.toString());
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});

		// set ringtone listeners
		viewHolder.ringtone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int dropdownPosition, long id) {
				notification.setRingtone(ringtoneNames.get(dropdownPosition), ringtoneUris.get(dropdownPosition));
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});
		viewHolder.testRingtone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				notificationsFragment.testRingtone(notification.getRingtoneUri());
			}
		});

		// set command listeners
		SeekBar.OnSeekBarChangeListener l = new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
				if (fromUser)
					notification.setCommand(viewHolder.blinds.getProgress(), viewHolder.slat.getProgress());
			}

			@Override
			public void onStartTrackingTouch(SeekBar var1) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar var1) {
			}
		};
		viewHolder.blinds.setOnSeekBarChangeListener(l);
		viewHolder.slat.setOnSeekBarChangeListener(l);
		/*
		viewHolder.host.removeTextChangedListener(viewHolder.hostWatcher);
		viewHolder.host.addTextChangedListener(viewHolder.hostWatcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				notification.setCommand(charSequence.toString(), viewHolder.command.getText().toString());
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
		viewHolder.command.removeTextChangedListener(viewHolder.commandWatcher);
		viewHolder.command.addTextChangedListener(viewHolder.commandWatcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				notification.setCommand(viewHolder.host.getText().toString(), charSequence.toString());
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});*/
		viewHolder.testCommand.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				new CommandTask().execute(notification.getCommand());
			}
		});
	}

	@Override
	public int getItemCount() {
		return this.data.getNotificationCount();
	}
}
