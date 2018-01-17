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

import java.lang.reflect.Method;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.bwx.bequick.handlers.autosync.AutoSyncSettingHandler.SyncControl;

public class SyncControl16 implements SyncControl {

	private static final String TAG = "SyncControl16";
	
	private final ContentResolver mContentResolver;
	private final String[] PROVIDERS = new String[] {"gmail-ls", "contacts", "calendar"};
	
	// initialize accessors
	private final Object mContentService;
	private final Method[] mMethods = new Method[3];
	
	public SyncControl16(ContentResolver contentResolver) throws Exception {
		mContentResolver = contentResolver;
		
		Method[] methods = mMethods;
		// initialize immediately
		Method getContentService = mContentResolver.getClass().getMethod("getContentService");
		Object contentService = getContentService.invoke(mContentResolver);
		methods[0] = contentService.getClass().getMethod("setListenForNetworkTickles", Boolean.TYPE);
		methods[1] = contentService.getClass().getMethod("getListenForNetworkTickles");
		methods[2] = contentService.getClass().getMethod("getSyncProviderAutomatically", String.class);
		
		mContentService = contentService;
	}

	public boolean isSyncActivated() {
		try {
			Boolean result = (Boolean) mMethods[1].invoke(mContentService);
			return result == null ? false : result.booleanValue();
		} catch (Exception e) {
			Log.e(TAG, "", e);
			return false;
		}
	}
	
	public void setSyncActivated(boolean flag) {
		try {
			// change property
			mMethods[0].invoke(mContentService, flag);
			// initiate / cancel sync
			if (flag) {
				for (String provider:PROVIDERS) {
					// start sync in case it is enabled for a provider
					if (getSyncProviderAutomatically(provider)) startSync(provider);
				} 
			} else {
				for (String provider:PROVIDERS) {
					cancelSync(provider);
				} 
			}
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
	}

	private boolean getSyncProviderAutomatically(String provider) {
		try {
			Boolean result = (Boolean) mMethods[2].invoke(mContentService, provider);
			return result == null ? false : result.booleanValue();
		} catch (Exception e) {
			Log.e(TAG, "", e);
			return false;
		}
	}
	
    private void startSync(String authority) {
        Uri uriToSync = (authority != null) ? Uri.parse("content://" + authority) : null;
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
        mContentResolver.startSync(uriToSync, extras);
    }
	
    private void cancelSync(String authority) {
    	mContentResolver.cancelSync(Uri.parse("content://" + authority));
    }
    
	public void activate() {
		// do nothing
	}

	public void deactivate() {
		// do nothing
	}

}
