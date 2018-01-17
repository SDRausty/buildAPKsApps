/*
 * Copyright (C) 2010 beworx.com
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

package com.bwx.bequick.handlers.autosync;

import java.util.Iterator;
import java.util.Map;

import android.content.ContentQueryMap;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.bwx.bequick.handlers.autosync.AutoSyncSettingHandler.SyncControl;


public class SyncControl15 extends ContentQueryMap implements SyncControl {
	
	private static final String LISTEN_FOR_TICKLES = "listen_for_tickles";
	private static final String SYNC_PROVIDER_PREFIX = "sync_provider_";
	
	protected final ContentResolver mContentResolver;

	public SyncControl15(ContentResolver contentResolver, Cursor cursor) {
		super(cursor, KEY, false, null);
		mContentResolver = contentResolver;
	}

	private boolean getBoolean(String name, boolean def) {
		ContentValues values = getValues(name);
		return isEnabled(values, def);
	}
	
	private boolean isEnabled(ContentValues values, boolean def) {
		return values == null || !values.containsKey(VALUE) ? def : values.getAsBoolean(VALUE);
	}
	
	private void putBoolean(ContentResolver contentResolver, String name, boolean val) {
		ContentValues values = new ContentValues();
		values.put(KEY, name);
		values.put(VALUE, Boolean.toString(val));
		contentResolver.insert(CONTENT_URI, values);
	}
	
    protected void startSync(String providerName) {
        Uri uriToSync = (providerName != null) ? Uri.parse("content://" + providerName) : null;
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
        mContentResolver.startSync(uriToSync, extras);
    }

    protected void cancelSync(String authority) {
    	mContentResolver.cancelSync(Uri.parse("content://" + authority));
    }
	
	public void setSyncActivated(boolean flag) {
		setListenForNetworkTickles(mContentResolver, flag);
		cancelOrStartSyncForEnabledProviders(flag);
	}

	public boolean isSyncActivated() {
		return getBoolean(LISTEN_FOR_TICKLES, true);
	}

	public void setListenForNetworkTickles(ContentResolver contentResolver, boolean flag) {
		putBoolean(contentResolver, LISTEN_FOR_TICKLES, flag);
	}

    public void cancelOrStartSyncForEnabledProviders(boolean startSync) {
    	Map<String, ContentValues> rows = getRows();
    	if (rows != null) {
    		Iterator<String> keys = rows.keySet().iterator();
    		while (keys.hasNext()) {
    			String key = keys.next();
    			if (key.startsWith(SYNC_PROVIDER_PREFIX)) {
    				ContentValues values = rows.get(key);
    				if (isEnabled(values, true)) {
	    				String provider = key.substring(SYNC_PROVIDER_PREFIX.length());
						if (startSync) {
							startSync(provider);
						} else {
							cancelSync(provider);
						}
    				}
    			}
    		}
    	}
    }

	public void activate() {
		setKeepUpdated(true);
	}

	public void deactivate() {
		setKeepUpdated(false);
	}

}
