package com.chmod0.manpages;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadZipTask extends RotationAwareTask<Void, Integer, Void> {

	private String destinationPath;
	private String sourcePath;
	private ManualActivity activity;
	private int bytesCount;

	public DownloadZipTask(ManualActivity activity, String srcPath,
			String destPath) {
		super(activity);
		this.activity = activity;
		this.destinationPath = destPath;
		this.sourcePath = srcPath;
		this.bytesCount = 0;
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(
					sourcePath).openConnection();

			conn.setDoInput(true);
			conn.setConnectTimeout(10000); // timeout 10 secs
			conn.connect();
			InputStream input = conn.getInputStream();
			FileOutputStream fOut = new FileOutputStream(destinationPath
					+ "manpages.zip");
			byte[] buffer = new byte[4096];
			int bytesRead = -1;
			while ((bytesRead = input.read(buffer)) != -1) {
				fOut.write(buffer, 0, bytesRead);
				bytesCount += bytesRead;
				publishProgress(4096);
			}
			fOut.flush();
			fOut.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		if (!isCancelled()) {
			ProgressBar bar = (ProgressBar) activity.progressDialog.findViewById(R.id.progressBar);
			bar.incrementProgressBy(4096);
			TextView tv = (TextView) activity.progressDialog.findViewById(R.id.progressText);
			tv.setText(bytesCount/1000 + "/5 400 KB");
		}
	}
	
	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		activity.extractZipFile(destinationPath);
	}

}
