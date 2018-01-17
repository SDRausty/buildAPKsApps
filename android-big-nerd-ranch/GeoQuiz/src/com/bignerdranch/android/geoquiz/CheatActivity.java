package com.bignerdranch.android.geoquiz;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CheatActivity extends Activity {
	
	private boolean mAnswerIsTrue;
	private boolean mIsAnswerShown;
	
	private TextView mAnswerTextView;
	private TextView mApiVersionTextView;
	private Button mShowAnswer;
	
	public static final String EXTRA_ANSWER_IS_TRUE = "com.bignerdranch.android.geoquiz.answer_is_true";
	public static final String EXTRA_ANSWER_IS_SHOWN = "com.bignerdranch.android.geoquiz.answer_shown";
	
	public static final String KEY_ANSWER_SHOWN = "mIsAnswerShown";
	
	private void setAnswerShowResult(boolean isAnswerShown) {
		mIsAnswerShown = isAnswerShown;
		Intent data = new Intent();
		data.putExtra(EXTRA_ANSWER_IS_SHOWN, isAnswerShown);
		setResult(RESULT_OK, data);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cheat);
		
		if (savedInstanceState != null)
		{
			setAnswerShowResult(savedInstanceState.getBoolean(KEY_ANSWER_SHOWN));
		} else {
			// Answer will not be shown until the user presses the button
			setAnswerShowResult(false);
		}
		
		mAnswerIsTrue = getIntent().getBooleanExtra(EXTRA_ANSWER_IS_TRUE, false);
		
		mAnswerTextView = (TextView)findViewById(R.id.answerTextView);
		mShowAnswer = (Button)findViewById(R.id.showAnswerButton);
		mShowAnswer.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mAnswerIsTrue) {
					mAnswerTextView.setText(R.string.true_button);
				} else {
					mAnswerTextView.setText(R.string.false_button);
				}
				setAnswerShowResult(true);
			}
		});
		
		mApiVersionTextView = (TextView)findViewById(R.id.apiVersionsTextView);
		mApiVersionTextView.setText(String.format("API level %d", Build.VERSION.SDK_INT));
	}
	
	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putBoolean(KEY_ANSWER_SHOWN, mIsAnswerShown);
	}

}
