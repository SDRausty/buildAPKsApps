package com.marakana.android.startactivityforresult;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SecondActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.second);
		Button okButton = (Button) findViewById(R.id.button_ok); 
		Button cancelButton = (Button) findViewById(R.id.button_cancel);
		okButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
	}

	public void onClick(View button) {
		int id = button.getId();
		switch (id) {
		case R.id.button_ok:
			EditText editName = (EditText) findViewById(R.id.edit_name);
			String name = editName.getText().toString();
			
			// Construct the Intent with the result information
			Intent result = new Intent();
			result.putExtra("NAME", name);
			
			// Indicate a successful response
			setResult(RESULT_OK, result);
			break;
		case R.id.button_cancel:
			// Indicate an unsuccessful response, with no Intent
			setResult(RESULT_CANCELED, null);
			break;
		}
		
		// Terminate ourself
		finish();
	}

}
