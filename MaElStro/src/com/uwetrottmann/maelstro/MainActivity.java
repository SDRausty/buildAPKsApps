package com.uwetrottmann.maelstro;

import java.util.Random;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView mTextViewPin;
	private PinCreatorTask mPinTask;
	private DataHelper mDataHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setupViews();

		mDataHelper = new DataHelper(this);
	}

	private void setupViews() {
		mTextViewPin = (TextView) findViewById(R.id.textViewNumber);
		mTextViewPin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPinTask == null
						|| mPinTask.getStatus() == AsyncTask.Status.FINISHED) {
					mPinTask = (PinCreatorTask) new PinCreatorTask().execute();
				}
			}
		});
	}

	private class PinCreatorTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			Random r = new Random();

			int randomPin;
			do {
				randomPin = r.nextInt(10000);
			} while (!mDataHelper.insertNumber(randomPin));

			return randomPin;
		}

		@Override
		protected void onPostExecute(Integer result) {
			String pinString = String.valueOf(result);
			while (pinString.length() < 4) {
				pinString = "0" + pinString;
			}
			mTextViewPin.setText(pinString);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_clear:
			mDataHelper.clearDatabase();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
