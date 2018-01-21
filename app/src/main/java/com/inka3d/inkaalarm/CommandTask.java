package com.inka3d.inkaalarm;

import android.os.AsyncTask;
import android.util.Log;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Sends a command to the smart home system
 */
public class CommandTask extends AsyncTask<Notification.Command, Void, Integer> {

	protected Integer doInBackground(Notification.Command... commands) {
		int failCount = 0;
		for (Notification.Command command : commands) {

			try {
				// send command
				URL url = new URL("http://192.168.1.1:8080/node/4?position.blinds=" + command.blinds + "&position.slat=" + command.slat);
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.setRequestMethod("POST");
				connection.connect();
				int code = connection.getResponseCode();

				/*
				// get csrf token
				URL tokenUrl = new URL("http://" + command.host + "/fhem?XHR=1");
				HttpURLConnection tokenConnection = (HttpURLConnection)tokenUrl.openConnection();
				tokenConnection.connect();
				int tokenCode = tokenConnection.getResponseCode();
				Map<String, List<String>> map = tokenConnection.getHeaderFields();
				List<String> tokenList = map.get("X-FHEM-csrfToken");
				if (!tokenList.isEmpty()) {
					String token = tokenList.get(0);

					// send command
					String encodedCommand = URLEncoder.encode(command.command, "UTF-8");
					URL url = new URL("http://" + command.host + "/fhem?cmd=" + encodedCommand);
					HttpURLConnection connection = (HttpURLConnection)url.openConnection();
					connection.setRequestMethod("POST");
					connection.setDoInput(true);
					OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
					wr.write("fwcsrf=" + token);
					wr.flush();
					wr.close();
					connection.connect();
					int code = connection.getResponseCode();
				}*/

			} catch (Exception e) {
				//Log.e("InkaAlarm", "exception", e);
			}

			// escape early if cancel() was called
			if (isCancelled())
				break;
		}
		return failCount;
	}
}
