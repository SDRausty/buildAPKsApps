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

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.preference.PreferenceManager;

public class DummyProvider extends ContentProvider {
	
	private static final String TAG = "DummyProvider";
	private static final int SUGGEST_URI_MATCH = 1;
	
	private UriMatcher suggestionUriMatcher = null;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public boolean onCreate() {
		/*
		suggestionUriMatcher = new UriMatcher(0);
		suggestionUriMatcher.addURI("duckduckgo", SearchManager.SUGGEST_URI_PATH_QUERY, SUGGEST_URI_MATCH);
		*/
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		

		// the uri looks like "content://duckduckgo/search_suggest_query/f?limit=50"
		// the last path segment is whatever the user has typed
		String query = uri.getLastPathSegment();
				
		if ("search_suggest_query".equals(query))
		{
			return null;
		}
		
		// if the user is typing a bang, we don't want to suggest other bangs.
		if (query.startsWith("!"))
		{
			return null;
		}
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		// the cursor is usually pointing at the table "suggestions"
		// which has 4 columns:
		// _id (integer), display1 (text), query (text), date (long)
		// TODO perhaps we should display something smart @ display2
		
		// we'll simulate this through a MatrixCursor.
		MatrixCursor cursor = new MatrixCursor(new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_QUERY, "_id"});
		cursor.addRow(formatRow(query, preferences.getString("bang_1_preference", "!google"), 0));
		cursor.addRow(formatRow(query, preferences.getString("bang_2_preference", "!amazon"), 1));
		cursor.addRow(formatRow(query, preferences.getString("bang_3_preference", "!bing"), 2));
		cursor.addRow(formatRow(query, preferences.getString("bang_4_preference", "!news"), 2));
		
		return cursor;
	}
	
	private Object[] formatRow(String query, String bang, int index)
	{
		return new Object[]{bang + " " + query, bang + " " + query, index}; 
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
}
