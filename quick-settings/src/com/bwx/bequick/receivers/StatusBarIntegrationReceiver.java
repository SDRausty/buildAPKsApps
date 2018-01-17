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

package com.bwx.bequick.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.widget.RemoteViews;

import com.bwx.bequick.Constants;
import com.bwx.bequick.R;
import com.bwx.bequick.ShowSettingsActivity;

public class StatusBarIntegrationReceiver extends BroadcastReceiver {

	//private static final String TAG = "StatusBarIntegrationReceiver";
	private static final int SHORTCUT_NOTIFICATION = 0;
	
	// cache
	private Notification mNotification;
	
	@Override
	public void onReceive(final Context context, Intent intent) {

		String action = intent.getAction();
		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			
			// read configuration
			SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_COMMON, 0);
			
			int status = Integer.parseInt(prefs.getString(Constants.PREF_STATUSBAR_INTEGRATION, "-1"));
			if (status > 0) {
				// show notification if it was configured to be shown
				int appearence = Integer.parseInt(prefs.getString(Constants.PREF_APPEARANCE, "0"));
				boolean invert = prefs.getBoolean(Constants.PREF_INVERSE_VIEW_COLOR, false);
				sendNotification(context, status, appearence, invert);
			}

		} else if (Constants.ACTION_UPDATE_STATUSBAR_INTEGRATION.equals(action)) {

			int status = intent.getIntExtra(Constants.EXTRA_INT_STATUS, -1);
			int appearance = intent.getIntExtra(Constants.EXTRA_INT_APPEARANCE, 0);
			boolean inverse = intent.getBooleanExtra(Constants.EXTRA_BOOL_INVERSE_COLOR, false);
			sendNotification(context, status, appearance, inverse);
			
		} else if (Constants.ACTION_START_QS.equals(action)) {
			
			new Handler().post(new Runnable() {
				public void run() {
	               Intent intent = new Intent(context, ShowSettingsActivity.class);
	               intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	               context.startActivity(intent); 					
				}
			});
		}
	}
	
	private synchronized void sendNotification(Context context, int status, int appearance, boolean inverse) {
		
		Notification notification = mNotification;
		if (notification == null) {
			
			// create and cache notification
			notification = new Notification();
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			notification.flags |= Notification.FLAG_NO_CLEAR;
			RemoteViews view = notification.contentView = new RemoteViews(context.getPackageName(), R.layout.status_bar_event);

			// update view color
			view.setImageViewResource(R.id.image1, inverse ? R.drawable.ic_logo_white : R.drawable.ic_logo_black);
			int color = inverse ? Color.WHITE : Color.BLACK;
			view.setTextColor(R.id.text1, color);
			view.setTextColor(R.id.text2, color);
			
			mNotification = notification;
		}
		
        //Intent intent = appearance == 0 ? new Intent("com.bwx.bequick.SHOW_FULLSCREEN") : new Intent("com.bwx.bequick.SHOW_DIALOG");

		// create intent depending on the appearance
		Intent intent = new Intent();
        String className = appearance == 0 ? "com.bwx.bequick.MainSettingsActivity" : "com.bwx.bequick.DialogSettingsActivity";
        intent.setClassName("com.bwx.bequick", className);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		notification.contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
		
		NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (status == Constants.STATUS_NO_INTEGRATION) {
			
			mgr.cancel(SHORTCUT_NOTIFICATION);
		} else {
			
			boolean sdk9OrLater = Constants.SDK_VERSION >= 9 /*2.3*/;
			
			boolean visible = status != Constants.STATUS_NO_ICON;
			notification.icon = visible 
				? (status == Constants.STATUS_BLACK_ICON ? R.drawable.ic_logo_black : R.drawable.ic_logo_white) 
				: sdk9OrLater ? R.drawable.ic_placeholder : -1;
				
			long hiddenTime = sdk9OrLater ? -Long.MAX_VALUE : Long.MAX_VALUE;
			notification.when = visible ? System.currentTimeMillis() : hiddenTime; // align left (0) / right (max) in status bar
			mgr.notify(SHORTCUT_NOTIFICATION, notification);
		}
	}
}
