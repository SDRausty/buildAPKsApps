package com.mnm.seekbarpreference;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public final class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    // Check /res/xml/preferences.xml file for this preference
    private static final String PREFERENCE_KEY = "seekBarPreference";

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	addPreferencesFromResource(R.xml.preferences);

	// Register for changes (for example only)
	getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	if (key.equals(PREFERENCE_KEY)) {
	    // Notify that value was really changed
	    int value = sharedPreferences.getInt(PREFERENCE_KEY, 0);
	    Toast.makeText(this, getString(R.string.summary, value), Toast.LENGTH_LONG).show();
	}
    }

    @Override
    protected void onDestroy() {
	// Unregister from changes
	getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	super.onDestroy();
    }
}