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

public interface Flashlight {

	public static final int TYPE_DROID22 = 4;
	public static final int TYPE_FROYO = 3;
	public static final int TYPE_HTC = 2;
	public static final int TYPE_MOTO21 = 1;
	
	public static final int TYPES_NUMBER = 4;
	
	boolean isSupported(Context context);
	boolean isOn(Context context);
	void setOn(boolean on, Context context);
	
	int getType();
}
