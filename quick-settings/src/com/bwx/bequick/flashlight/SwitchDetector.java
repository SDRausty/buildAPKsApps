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

import android.content.Context;

public abstract class SwitchDetector {

	public static interface Switchable {
		void switchLight(boolean on);
	}
	
	protected final Switchable mSwitchable;
	protected final int mTextId;
	
	public SwitchDetector(Switchable switchable, int textId) {
		mSwitchable = switchable;
		mTextId = textId;
	}

	public int getTextId() {
		return mTextId;
	}
	
	public abstract void activate(Context context);
	public abstract void inactivate(Context context);
}
