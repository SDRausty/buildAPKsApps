package com.chmod0.manpages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Inspired from http://www.jondev.net/articles/Unzipping_Files_with_Android_%28
 * Programmatically%29
 * 
 * @author jon
 */
public class ExtractZipTask extends RotationAwareTask<Void, Integer, Void> {

	private String zipFile;
	private String location;
	private ManualActivity activity;
	private int filesCount;
	
	public ExtractZipTask(ManualActivity activity, String zipFile, String location) {
		super(activity);
		this.activity = activity;
		this.zipFile = zipFile;
		this.location = location;
		this.filesCount = 0;
		
		dirChecker("");
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			FileInputStream fin = new FileInputStream(zipFile);
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze = null;
			while ((ze = zin.getNextEntry()) != null) {
				publishProgress(1);
				filesCount++;
				
				if (ze.isDirectory()) {
					dirChecker(ze.getName());
				} else {
					FileOutputStream fout = new FileOutputStream(location
							+ ze.getName());
					for (int c = zin.read(); c != -1; c = zin.read()) {
						fout.write(c);
					}

					zin.closeEntry();
					fout.close();
				}

			}
			zin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		activity.progressDialog.dismiss();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		ProgressBar bar = (ProgressBar) activity.progressDialog.findViewById(R.id.progressBar);
		bar.setMax(1686);
		bar.setProgress(0);
		activity.progressDialog.setTitle("Extracting pages...");
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		if (!isCancelled()) {
			ProgressBar bar = (ProgressBar) activity.progressDialog.findViewById(R.id.progressBar);
			bar.incrementProgressBy(1);
			TextView tv = (TextView) activity.progressDialog.findViewById(R.id.progressText);
			tv.setText(filesCount + "/1686 files");
		}
	}

	private void dirChecker(String dir) {
		File f = new File(location + dir);

		if (!f.isDirectory()) {
			f.mkdirs();
		}
	}
}
