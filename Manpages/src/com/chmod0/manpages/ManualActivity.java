package com.chmod0.manpages;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import com.chmod0.manpages.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ManualActivity extends Activity {

	private ArrayList<Page> manPages;
	private AlertDialog downloadDialog;
	AlertDialog progressDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		this.manPages = initPages();
		ArrayAdapter<Page> manPagesAdapter = new ArrayAdapter<Page>(this,
				android.R.layout.simple_list_item_1, manPages);
		ArrayAdapter<Page> manPagesCompleteAdapter = new ArrayAdapter<Page>(
				this, android.R.layout.simple_dropdown_item_1line, manPages);

		// Main list of all manpages
		ListView manPagesView = (ListView) findViewById(R.id.pages_names);
		manPagesView.setAdapter(manPagesAdapter);
		manPagesView.setOnItemClickListener(new OnManpageClickListener(this));

		// List of manpages displayed in the text field suggestions
		AutoCompleteTextView manPagesCompleteTextView = (AutoCompleteTextView) findViewById(R.id.search_input);
		manPagesCompleteTextView.setAdapter(manPagesCompleteAdapter);
		manPagesCompleteTextView
				.setOnItemClickListener(new OnManpageClickListener(this));

		// Check the availability of files on external storage
		if (!checkPageFilesOnExternalStorage(null))
			suggestDownload();

		// Handle the Enter key in the TextView
		// Get the input TextView
		AutoCompleteTextView input = (AutoCompleteTextView) findViewById(R.id.search_input);
		// Create a new handler for the Enter key ("Go" key here)
		TextView.OnEditorActionListener enterKeyListener = new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// If search key was hit, search for this manpage
				if (actionId == EditorInfo.IME_ACTION_SEARCH
						|| actionId == EditorInfo.IME_NULL) {
					Page p = getManpageCalled(v.getText().toString());
					if (p != null) {
						// display it if found,
						displayManpage(p);
					} else {
						// else display an error message
						Toast.makeText(getApplicationContext(),
								"This manpage does not exist...",
								Toast.LENGTH_LONG).show();
					}
				}
				return true;
			}
		};
		input.setOnEditorActionListener(enterKeyListener);
	}

	/**
	 * Initialize the list of manpages from the files that contains its list
	 * 
	 * @return list of all manpages names
	 */
	public ArrayList<Page> initPages() {
		ArrayList<Page> manPages = new ArrayList<Page>();
		manPages.addAll(loadPagesNames(R.raw.section1, 1));
		manPages.addAll(loadPagesNames(R.raw.section2, 2));
		manPages.addAll(loadPagesNames(R.raw.section3, 3));
		manPages.addAll(loadPagesNames(R.raw.section4, 4));
		manPages.addAll(loadPagesNames(R.raw.section5, 5));
		manPages.addAll(loadPagesNames(R.raw.section7, 7));
		manPages.addAll(loadPagesNames(R.raw.section8, 8));

		return manPages;
	}

	/**
	 * Open a file which contains manpages names of a section, and add it into a
	 * list
	 * 
	 * @param resourceId
	 *            reference of the file containing the manpages names
	 * @param section
	 *            section number in initialisation
	 * @return list of all the manpages of the section
	 */
	public ArrayList<Page> loadPagesNames(int resourceId, int section) {
		ArrayList<Page> pages = new ArrayList<Page>();
		try {
			InputStream is = getApplicationContext().getResources()
					.openRawResource(resourceId);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = br.readLine()) != null) {
				pages.add(new Page(line, section));
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			Log.d("MANPAGES",
					"Files containing the list of manpages is missing... this is very annoying.");
		}
		return pages;
	}

	/**
	 * Display a manpage in a webview, launched in a new activity
	 * 
	 * @param manpage
	 *            page to display
	 */
	public void displayManpage(Page manpage) {
		Bundle bundle = new Bundle();
		bundle.putSerializable("manpage", manpage);

		Intent intentBrowser = new Intent(this, BrowserActivity.class);
		intentBrowser.putExtra("manpage", bundle);
		startActivity(intentBrowser);
	}

	/**
	 * Search for a manpage name in all manpages It returns the first occurrence
	 * if it exists, or null
	 * 
	 * @param name
	 *            name of the manpage to search
	 * @return the first occurrence of the manpage, or null
	 */
	public Page getManpageCalled(String name) {
		for (Page p : this.manPages) {
			if (p.getName().compareTo(name) == 0) {
				return p;
			}
		}
		return null;
	}

	public static boolean checkPageFilesOnExternalStorage(String pagePath) {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return false;
		}

		String fullPath = Environment.getExternalStorageDirectory()
				+ "/Android/data/com.chmod0.manpages/";
		if (pagePath == null)
			fullPath += "htmlman/htmlman1/chmod.1.html";
		else
			fullPath += pagePath;

		File pageFile = new File(fullPath);
		if (!pageFile.exists())
			return false;

		return true;
	}

	/**
	 * Display a popup that suggests the user to download manpages locally
	 */
	public void suggestDownload() {
		LayoutInflater factory = getLayoutInflater();
		AlertDialog.Builder downloadDialogBuilder = new AlertDialog.Builder(this);
		downloadDialogBuilder.setTitle("Download pages?");
		downloadDialogBuilder.setIcon(R.drawable.ic_launcher);
		downloadDialogBuilder.setView(factory.inflate(R.layout.download_popup, null,
				false));
		
		downloadDialogBuilder.setPositiveButton("Download pages", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				downloadDialog.dismiss();
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				downloadPages();
			}
		});
		
		downloadDialogBuilder.setNegativeButton("Use online pages", null);
		downloadDialog = downloadDialogBuilder.create();
		downloadDialog.show();
	}

	/**
	 * Downloads a remote file and stores it locally from
	 * http://stackoverflow.com
	 * /questions/3402165/android-download-zip-to-sd-card
	 * 
	 * @return download succeed or failed
	 */
	private boolean downloadPages() {
		String to = Environment.getExternalStorageDirectory()
				+ "/Android/data/com.chmod0.manpages/";
		
		// Check if external storage is mounted
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Toast.makeText(this, R.string.no_external_storage,
					Toast.LENGTH_LONG).show();
			return false;
		}

		// Remote URL of the file to download
		String from = "http://dl.dropbox.com/u/2755540/manpages.zip";
		
		File appDir = new File(to);
		if(! appDir.exists() || ! appDir.isDirectory()) {
			Log.d("MANPAGES", "No app dir, creating it");
			appDir.mkdirs();
		}

		// check for wifi or 3g
		ConnectivityManager mgrConn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		TelephonyManager mgrTel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		if ((mgrConn.getActiveNetworkInfo() != null && mgrConn
				.getActiveNetworkInfo().getState() == android.net.NetworkInfo.State.CONNECTED)
				|| mgrTel.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS) {

			// Show a dialog for download progress
			LayoutInflater factory = getLayoutInflater();
			AlertDialog.Builder progressDialogBuilder = new AlertDialog.Builder(this);
			progressDialogBuilder.setTitle("Downloading pages...");
			progressDialogBuilder.setIcon(R.drawable.ic_launcher);
			View progressView = factory.inflate(R.layout.progress_popup, null,
					false);
			progressDialogBuilder.setView(progressView);
			progressDialog = progressDialogBuilder.create();
			progressDialog.show();
			ProgressBar progressBar = (ProgressBar) progressView.findViewById(R.id.progressBar);
			progressBar.setMax(5404063);
			
			// Connect and download the archive
			DownloadZipTask downloadZipTask = new DownloadZipTask(this, from, to);
			downloadZipTask.execute((Void)null);
			
			return true;
			
		} else {
			Toast.makeText(this, R.string.no_connection, Toast.LENGTH_LONG)
					.show();
			return false;
		}
	}
	
	public void extractZipFile(String dirPath) {
		ExtractZipTask extractZipTask = new ExtractZipTask(this, dirPath + "manpages.zip", dirPath);
		extractZipTask.execute((Void) null);
	}

	/**
	 * Create the options menu of the activity
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(getApplication()).inflate(R.menu.options_manual, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Actions to do when the options menu is clicked
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		LayoutInflater factory = getLayoutInflater();
		switch (item.getItemId()) {
		case R.id.about_menu:
			AlertDialog.Builder aboutDialog = new AlertDialog.Builder(this);
			aboutDialog.setTitle("About...");
			aboutDialog.setIcon(R.drawable.ic_launcher);
			aboutDialog.setView(factory.inflate(R.layout.about_popup, null,
					false));
			aboutDialog.setPositiveButton("Ok", null);
			aboutDialog.show();
			break;
			
		case R.id.download_menu:
			this.suggestDownload();
			break;
			
		case R.id.section_menu:
			AlertDialog.Builder sectionsDialog = new AlertDialog.Builder(this);
			sectionsDialog.setTitle("Sections");
			sectionsDialog.setView(factory.inflate(R.layout.sections_popup,
					null, false));
			sectionsDialog.setPositiveButton("Ok", null);
			sectionsDialog.show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
