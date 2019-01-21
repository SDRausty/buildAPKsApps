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

package com.bwx.bequick.handlers;

import android.content.Context;

import com.bwx.bequick.R;

public class ScreenTimeoutValues {

	public static final int INDEX_NEVER = -1;

	// timeout intervals
	private static int[] VALUES = {15, 30, 1, 2, 10};
	private static int[] TIMEOUTS = {15000, 30000, 60000, 120000, 600000};

	private final Context mContext;
	
	public ScreenTimeoutValues(Context context) {
		mContext = context;
	}
	
	public String getDescriptionByIndex(int index) {
		String descr;
		switch (index) {
			case 0:
			case 1:
				descr = mContext.getString(R.string.txt_screen_timeout_value_seconds, VALUES[index]);
				break;
			case 2:
				descr = mContext.getString(R.string.txt_screen_timeout_value_minute, VALUES[index]);
				break;
			case 3:
			case 4:
			case 5:
				descr = mContext.getString(R.string.txt_screen_timeout_value_minutes, VALUES[index]);
				break;
			default:
				descr = mContext.getString(R.string.txt_screen_timeout_value_never);
				break;
		}
		return descr;
	}

	public int getTimeoutByIndex(int index) {
		return index <= INDEX_NEVER ? -1 : TIMEOUTS[index];
	}
	
	public int getNumberOfValues() {
		return VALUES.length;
	}
	
}
