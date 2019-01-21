/*
 * Copyright (C) 2010 Sergej Shafarenka, beworx.com
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

package com.bwx.bequick.flashlight;

import com.bwx.bequick.R;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;

public class DelaySwitchDetector extends SwitchDetector implements Runnable {

	private Handler mHandler = new Handler();
	
	public DelaySwitchDetector(Switchable switchable) {
		super(switchable, R.string.msg_flashlight_delay);
	}

	@Override
	public void activate(Context context) {
		mHandler.postAtTime(this, SystemClock.uptimeMillis() + 3000);
	}

	@Override
	public void inactivate(Context context) {
		mHandler.removeCallbacks(this);
	}

	public void run() {
		mSwitchable.switchLight(true);
	}

}
