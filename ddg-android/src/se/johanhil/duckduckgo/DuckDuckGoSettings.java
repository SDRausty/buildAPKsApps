package se.johanhil.duckduckgo;

/*
 * Copyright Johan Hilding (2010)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
*/


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class DuckDuckGoSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private static final String HELP_KEY = "help";
	private static final String SEARCH_KEY = "search";
	private static final String BANG_1_KEY = "bang_1_preference";
	private static final String BANG_2_KEY = "bang_2_preference";
	private static final String BANG_3_KEY = "bang_3_preference";
	private static final String BANG_4_KEY = "bang_4_preference";

	ListPreference bang1 = null;
	ListPreference bang2 = null;
	ListPreference bang3 = null;
	ListPreference bang4 = null;

	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        
        Preference search = findPreference(SEARCH_KEY);
        Preference help = findPreference(HELP_KEY);
        
        initializeLists();
        
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        
        search.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				DuckDuckGoSettings.this.onSearchRequested();
				return true;
			}
		});

        help.setIntent(new Intent(this, Help.class));
	}
	
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateLists();
    }
    
    private void initializeLists()
    {
    	bang1 = (ListPreference) findPreference(BANG_1_KEY);
        bang2 = (ListPreference) findPreference(BANG_2_KEY);
        bang3 = (ListPreference) findPreference(BANG_3_KEY);
        bang4 = (ListPreference) findPreference(BANG_4_KEY);
        
        updateLists();
    }
    
    private void updateLists()
    {
        bang1.setSummary(bang1.getEntry());
        bang2.setSummary(bang2.getEntry());
        bang3.setSummary(bang3.getEntry());
        bang4.setSummary(bang4.getEntry());
    }
}
