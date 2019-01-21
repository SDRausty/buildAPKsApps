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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncAdapterType;
import android.os.Bundle;
import android.util.Log;

import com.bwx.bequick.Constants;
import com.bwx.bequick.handlers.autosync.AutoSyncSettingHandler.SyncControl;

public class SyncControl20 implements SyncControl {

	private static final String TAG = "SyncControl20";
	
	private final Context mContext;
	
	public SyncControl20(Context context) {
		mContext = context;
		
		// Ensure we have this method in API ...
		// This is important for CyanogenMod 1.6, 
		// which does not fail with VerifyError like the others do, 
		// though it does not have this method either.
		ContentResolver.getMasterSyncAutomatically();
	}
	
	public void activate() {
		// do nothing
	}

	public void deactivate() {
		// do nothing
	}

	public boolean isSyncActivated() {
        return ContentResolver.getMasterSyncAutomatically();
	}

	public void setSyncActivated(boolean enabled) {
		
        ContentResolver.setMasterSyncAutomatically(enabled);
        
        if (enabled) {
        	SyncAdapterType[] types = ContentResolver.getSyncAdapterTypes();
        	AccountManager accmgr = AccountManager.get(mContext);
        	for (SyncAdapterType type : types) {
        		
        		Account[] accounts = accmgr.getAccountsByType(type.accountType);
        		for (Account account : accounts) {
        			
        			if (Constants.DEBUG) {
        				Log.d(TAG, "synching account, name:" + account.name  + ", type: " + account.type);
        			}
        			
        			enabled = ContentResolver.getSyncAutomatically(account, type.authority);
        			if (enabled) {
        				// trigger update for next account
        				ContentResolver.requestSync(account, type.authority, new Bundle());
        			}
        		}
        	}
        }
	}

}
