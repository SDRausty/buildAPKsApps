package com.marakana.android.startactivityforresult;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class FirstActivity extends Activity implements OnClickListener {
	// Define a symbolic constant to identify which Activity responds
	private static final int GET_NAME_ACTIVITY = 1;
	private TextView mTextDisplayName;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first);
        
		mTextDisplayName = (TextView) findViewById(R.id.text_display_name);
        Button chooseName = (Button) findViewById(R.id.button_choose_name);
        chooseName.setOnClickListener(this);
    }

	public void onClick(View v) {
		Intent intent = new Intent(this, SecondActivity.class);
		startActivityForResult(intent, GET_NAME_ACTIVITY);
	}
	
	// Handle the result from the Activity started
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		String name;
		
		// Identify which Activity responded with a result
		switch (requestCode) {
		case GET_NAME_ACTIVITY:
			if (resultCode == RESULT_OK) {
				name = data.getStringExtra("NAME");
			}
			else {
				name = "No name provided";
			}
			mTextDisplayName.setText(name);
			break;
		}
	}
    
}