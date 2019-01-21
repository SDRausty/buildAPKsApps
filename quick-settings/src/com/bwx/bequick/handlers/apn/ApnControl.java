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

package com.bwx.bequick.handlers.apn;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.bwx.bequick.Constants;

/**
 * Modifies all current APN configurations according to user preferences. It adds 
 * a prefix or a suffix to {@link #COLUMN_APN} and {@link #COLUMN_TYPE} values of 
 * each APN configuration in order to disable them and removes any know prefixes or 
 * suffixes to enable them back.
 * 
 * @author sergej@beworx.com 
 */
public class ApnControl {

	private static final String TAG = "bwx.ApnControl";
	
	// this state is returned if we have only MMS but we are not allowed to disable them
	public static final int STATE_MMS_ONLY = -1; 
	// this state is returned if we have no APNs configured
	public static final int STATE_NO_APNS = 0;
	// we have APNs to disable
	public static final int STATE_OFF = 1;
	// we have APNs to enable
	public static final int STATE_ON = 2;
	
	private static final String SUFFIX_QS_CLASSIC = "[disabled]"; // type 0
	private static final String SUFFIX_APN = "apndroid"; // type 1
	private static final String PREFIX_MINUS = "-"; // type 2
	
	private static final String MMS = "mms";
	
	private static final String COLUMN_ID = BaseColumns._ID;
	private static final String COLUMN_APN = "apn";
	private static final String COLUMN_TYPE = "type";
	private static final String COLUMN_APN_ID = "apn_id"; // for preferred APN
	
	private static final String[] PROJECTION = new String[] { COLUMN_ID, COLUMN_APN, COLUMN_TYPE };
	private static final Uri CURRENT_APNS = Uri.parse("content://telephony/carriers/current");
	private static final Uri PREFERRED_APN = Uri.parse("content://telephony/carriers/preferapn");
	
	public static void setApnState(Context context, SharedPreferences prefs, boolean enabled) {
		
		boolean shouldDisableMms = shouldDisableMms(prefs);
		boolean restorePreferedApn = prefs.getBoolean(Constants.PREF_RESTORE_PREFERRED_APN, false);
		int modifierType = Integer.parseInt(prefs.getString(Constants.PREF_APN_MODIFIER, "2")); // "-" by default as most reliable
		
		// store APN for further restore always
		if (!enabled) storePreferredApn(context, prefs);
		
		ContentResolver resolver = context.getContentResolver();
		ContentValues values = new ContentValues();
		Cursor cursor = null;

		String[] args = new String[1];
		try {
			// COLUMN_ID{0}, COLUMN_APN{1}, COLUMN_TYPE{2}
			cursor = resolver.query(CURRENT_APNS, PROJECTION, null, null, null);
			int idIndex = cursor.getColumnIndex(COLUMN_ID);
			int apnIndex = cursor.getColumnIndex(COLUMN_APN);
			int typeIndex = cursor.getColumnIndex(COLUMN_TYPE);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {

				String typeValue = cursor.getString(typeIndex);
				if (!enabled // we should disable
						&& isMms(typeValue) // and this is MMS
						&& !shouldDisableMms) { // but users disallow us to
												// disable MMS
					// ignore
					cursor.moveToNext();
					continue;
				}

				args[0] = String.valueOf(cursor.getInt(idIndex)); // id
				values.put(COLUMN_APN, getAdaptedValue(cursor.getString(apnIndex), enabled, modifierType));
				values.put(COLUMN_TYPE, getAdaptedValue(typeValue, enabled, modifierType));

				resolver.update(CURRENT_APNS, values, COLUMN_ID + "=?", args);

				// move to next
				cursor.moveToNext();
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}		
		
		if (restorePreferedApn && enabled) {
			restorePreferredApn(context, prefs);
		}
		
	}
	
	private static void storePreferredApn(Context context, SharedPreferences prefs) {
		long id = getPreferedApnIdFromProvider(context);
		prefs.edit().putLong(Constants.PREF_PREFERRED_APN_ID, id).commit();
		Log.d(TAG, "Stored prefered APN id=" + id);
	}
	
	private static void restorePreferredApn(Context context, SharedPreferences prefs) {
		
		long id = getPreferedApnIdFromProvider(context);
		if (id == -1L) {
			id = getPreferedApnIdFromPreferences(context, prefs);
			if (id == -1L) {
				id = getFirstCurrentApnId(context);
			}
		}
		
		if (id == -1L) {
			Log.d(TAG, "No prefered APN can be restored");
		} else {
			
			ContentResolver resolver = context.getContentResolver();
			
			// refresh preferred APN
			
	        ContentValues values = new ContentValues();
	        values.putNull(COLUMN_APN_ID);
	        resolver.update(PREFERRED_APN, values, null, null);
	        
	        values.put(COLUMN_APN_ID, id);
	        resolver.update(PREFERRED_APN, values, null, null);
			
			Log.d(TAG, "Restored prefered APN id=" + id);
		}
	}

	private static long getPreferedApnIdFromPreferences(Context context, SharedPreferences prefs) {
		long id = prefs.getLong(Constants.PREF_PREFERRED_APN_ID, -1L);
		if (id == -1L) return id;
		
		// verify that there is still APN with such id
		ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(CURRENT_APNS, new String[] {COLUMN_ID}, COLUMN_ID + "=" + id, null, null);
        try {
        	cursor.moveToFirst();
        	if (!cursor.isAfterLast()){
        		// yes! it is still there
        		return id;
        	} else {
        		// no such APN anymore, return "not found"
        		return -1;
        	}
        } finally {
        	if (cursor != null) cursor.close();
        }
	}
	
	private static long getFirstCurrentApnId(Context context) {
		ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(CURRENT_APNS, new String[] {COLUMN_ID, COLUMN_TYPE}, null, null, null);
        try {
        	cursor.moveToFirst();
        	String type;
        	while (!cursor.isAfterLast()){
        		type = cursor.getString(1);
        		if (!isMms(type)) {
        			return cursor.getLong(0); // stop here, this is first not MMS
        		}
        		cursor.moveToNext();
        	}
        } finally {
        	if (cursor != null) cursor.close();
        }
        return -1;
	}

	private static long getPreferedApnIdFromProvider(Context context) {
		ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(PREFERRED_APN, new String[] {COLUMN_ID}, null, null, null);
        try {
        	cursor.moveToFirst();
        	if (!cursor.isAfterLast()){
        		return cursor.getLong(0);
        	}
        } finally {
        	if (cursor != null) cursor.close();
        }
        return -1L;
	}
	
	public static int getApnState(Context context, SharedPreferences prefs) {
		
		boolean shouldDisableMms = shouldDisableMms(prefs);
		
		ContentResolver resolver = context.getContentResolver();
		boolean hasMMS = false;
		int counter = 0;
		Cursor cursor = null;
		try {
			cursor = resolver.query(CURRENT_APNS, PROJECTION, null, null, null);
			int typeIndex = cursor.getColumnIndex(COLUMN_TYPE);
			cursor.moveToNext();
			while (!cursor.isAfterLast()) {
				
				String type = cursor.getString(typeIndex);

				if (isDisabled(type)) {
					return STATE_OFF; // no need to continue
				}

				if (!isMms(type) || shouldDisableMms) { 
					counter++;
				} else {
					hasMMS = true;
				}

				cursor.moveToNext();
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return counter == 0 ? (hasMMS ? STATE_MMS_ONLY : STATE_NO_APNS) : STATE_ON;
	}
	
	
	private static boolean isMms(String type) {
		return type != null && type.toLowerCase().contains(MMS);
	}
	
	private static boolean isDisabled(String value) {
		return value != null && (
				value.startsWith(PREFIX_MINUS) || 
				value.endsWith(SUFFIX_APN) || 
				value.endsWith(SUFFIX_QS_CLASSIC));
	}
	
	public static boolean shouldDisableMms(SharedPreferences prefs) {
		return prefs.getBoolean(Constants.PREF_DISABLE_MMS, false);
	}
	
	private static String getAdaptedValue(String value, boolean enable, int modifierType) {
		
		final String modifier = getPreferedModifier(modifierType);
		
		// handle null-value
		if (value == null) return enable ? value : modifier;
		
		// remove any modifier so that value becomes enabled in any case
		value = removeModifiers(value); 
		
		if (!enable) { // add required modifier
			value = addModifier(value, modifierType, modifier);
		}
		
		return value;
	}
	
	private static String getPreferedModifier(int modifierType) {
		if (modifierType == 2) { // the prefix
			return PREFIX_MINUS;
		} else { // a suffix
			return modifierType == 0 ? SUFFIX_APN : SUFFIX_QS_CLASSIC;
		}
	}
	
	private static String removeModifiers(String value) {
		if (value.startsWith(PREFIX_MINUS)) return value.substring(PREFIX_MINUS.length());
		if (value.endsWith(SUFFIX_QS_CLASSIC)) return value.substring(0, value.length() - SUFFIX_QS_CLASSIC.length());
		if (value.endsWith(SUFFIX_APN)) return value.substring(0, value.length() - SUFFIX_APN.length());
		return value;
	}
	
	private static String addModifier(String value, int modifierType, String modifier) {
		if (modifierType == 2) { // the prefix
			return modifier + value;
		} else { // a suffix
			return value + modifier;
		}
	}
}
