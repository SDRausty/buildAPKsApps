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

package com.bwx.bequick;

import android.os.Build;

public class Constants {

	public static final boolean DEBUG = false;
	public static final String TAG = "bwx.qs";
	
	public static final String PREFS_COMMON = "Common";
	public static final String PREFS_RUNTIME = "Runtime";

	public static final String PREF_STATUSBAR_INTEGRATION = "statusBarIntegration";
	public static final String PREF_APPEARANCE = "viewMode";
	public static final String PREF_INVERSE_VIEW_COLOR = "inverseSatusbarColor";
	public static final String PREF_HAPTIC = "hapticFeedback";
	public static final String PREF_LIGHT_SENSOR = "lightSensor";
	public static final String PREF_FLASHLIGHT = "flashlight";
	public static final String PREF_FLASHLIGHT_TYPE = "flashlightType";
	public static final String PREF_FLASHLIGHT_SWITCH = "flightSwitch";
	public static final String PREF_DISABLE_MMS = "disableMms";
	public static final String PREF_APN_MODIFIER = "apnModifier";
	public static final String PREF_RESTORE_PREFERRED_APN = "restorePreferredApn";
	public static final String PREF_MOBILE_DISABLE_MSG_OK = "disableMobileOk";
	public static final String PREF_PREFERRED_APN_ID = "preferredApn";
	public static final String PREF_NO_CONFIRM_AIRMODE = "noConfirmationAirmode";
	public static final String PREF_VERSION = "_version"; // old "version" property was an integer, new "_version" is a string
	public static final String PREF_ABOUT = "about";
	public static final String PREF_DOC = "doc";
	public static final String PREF_ABOUT_QUICKER = "about_quicker";
	public static final String PREF_ADS_SHOWN = "quickerAds";
	public static final String PREF_MOBILE_DATA_MODE = "mobileDataMode";
	public static final String PREF_GPS_MODE = "gpsMode";
	
	public static final String ACTION_UPDATE_STATUSBAR_INTEGRATION = "com.bwx.bequick.UPDATE_STATUSBAR_INTEGRATION";
	public static final String ACTION_START_QS = "com.bwx.bequick.START_QS";
	public static final String EXTRA_INT_STATUS = "status";
	public static final String EXTRA_INT_APPEARANCE = "appearence";
	public static final String EXTRA_BOOL_INVERSE_COLOR = "inversed";

	public static final String ACTION_VOLUME_UPDATED = "com.bwx.bequick.VOLUME_UPDATED";

	public static final int STATUS_WHITE_ICON = 3;
	public static final int STATUS_BLACK_ICON = 2;
	public static final int STATUS_NO_ICON = 1;
	public static final int STATUS_NO_INTEGRATION = 0;

	// 1.5 compatible version value
	public static final int SDK_VERSION = Integer.parseInt(Build.VERSION.SDK);
	
}
