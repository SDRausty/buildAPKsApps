package se.johanhil.duckduckgo;

/*
 * Copyright Johan Hilding (2010)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

/**
 * This class is purely here to get search queries and route them to
 * the global {@link Intent#ACTION_WEB_SEARCH}.
 */
public class DuckDuckGo extends Activity {
    private static final String TAG = "DuckDuckGo";

    // The template URL we should use to format DDG requests.
    private String duckDuckGoBaseUrl = "http://duckduckgo.com/?q=";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String action = intent != null ? intent.getAction() : null;
        if (Intent.ACTION_WEB_SEARCH.equals(action) || Intent.ACTION_SEARCH.equals(action)) {
            handleWebSearchIntent(intent);
        }
        else
        {
        	Log.d(TAG, "unknown search action: " + action);
        }
        
        finish();
    }

    private void handleWebSearchIntent(Intent intent) {
        String query = intent.getStringExtra(SearchManager.QUERY);
        if (TextUtils.isEmpty(query)) {
            Log.w(TAG, "Got search intent with no query.");
            return;
        }

        try {
            String searchUri = duckDuckGoBaseUrl + URLEncoder.encode(query, "UTF-8");
            Intent launchDDGSearchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(searchUri));
            launchDDGSearchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launchDDGSearchIntent);
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "Error", e);
        }
    }
}
