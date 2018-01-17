package com.mnm.seekbarpreference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.PreferenceActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public final class SeekBarPreference extends DialogPreference implements OnSeekBarChangeListener {

    // Namespaces to read attributes
    private static final String PREFERENCE_NS = "http://schemas.android.com/apk/res/com.mnm.seekbarpreference";
    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    // Attribute names
    private static final String ATTR_DEFAULT_VALUE = "defaultValue";
    private static final String ATTR_MIN_VALUE = "minValue";
    private static final String ATTR_MAX_VALUE = "maxValue";

    // Default values for defaults
    private static final int DEFAULT_CURRENT_VALUE = 50;
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = 100;

    // Real defaults
    private final int mDefaultValue;
    private final int mMaxValue;
    private final int mMinValue;
    
    // Current value
    private int mCurrentValue;
    
    // View elements
    private SeekBar mSeekBar;
    private TextView mValueText;
	private Context context;

    public SeekBarPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	
	this.context = context;

	// Read parameters from attributes
	mDefaultValue = attrs.getAttributeIntValue(ANDROID_NS, ATTR_DEFAULT_VALUE, DEFAULT_CURRENT_VALUE);
	
	TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.com_mnm_seekbarpreference_SeekBarPreference);
	mMinValue = array.getInteger(R.styleable.com_mnm_seekbarpreference_SeekBarPreference_minValue, DEFAULT_MIN_VALUE);
	mMaxValue = array.getInteger(R.styleable.com_mnm_seekbarpreference_SeekBarPreference_maxValue, DEFAULT_MAX_VALUE);
    }

    @Override
    protected View onCreateDialogView() {
	// Get current value from preferences
	mCurrentValue = getPersistedInt(mDefaultValue);

	// Inflate layout
	LayoutInflater inflater = ((PreferenceActivity) context).getLayoutInflater();
	View view = inflater.inflate(R.layout.dialog_slider, null);

	// Setup minimum and maximum text labels
	((TextView) view.findViewById(R.id.min_value)).setText(Integer.toString(mMinValue));
	((TextView) view.findViewById(R.id.max_value)).setText(Integer.toString(mMaxValue));

	// Setup SeekBar
	mSeekBar = (SeekBar) view.findViewById(R.id.seek_bar);
	mSeekBar.setMax(mMaxValue - mMinValue);
	mSeekBar.setProgress(mCurrentValue - mMinValue);
	mSeekBar.setOnSeekBarChangeListener(this);

	// Setup text label for current value
	mValueText = (TextView) view.findViewById(R.id.current_value);
	mValueText.setText(Integer.toString(mCurrentValue));

	return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
	super.onDialogClosed(positiveResult);

	// Return if change was cancelled
	if (!positiveResult) {
	    return;
	}
	
	// Persist current value if needed
	if (shouldPersist()) {
	    persistInt(mCurrentValue);
	}

	// Notify activity about changes (to update preference summary line)
	notifyChanged();
    }

    @Override
    public CharSequence getSummary() {
	// Format summary string with current value
	String summary = super.getSummary().toString();
	int value = getPersistedInt(mDefaultValue);
	return String.format(summary, value);
    }
    
    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
	// Update current value
	mCurrentValue = value + mMinValue;
	// Update label with current value
	mValueText.setText(Integer.toString(mCurrentValue));
    }

    public void onStartTrackingTouch(SeekBar seek) {
	// Not used
    }

    public void onStopTrackingTouch(SeekBar seek) {
	// Not used
    }
}