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

package com.bwx.bequick.handlers.wimax;

import com.bwx.bequick.R;

/**
 * @author Brian Rodgers 
 */
public enum FourGState {
	
	TURNING_ON(R.string.txt_status_turning_on), 
	TURNING_OFF(R.string.txt_status_turning_off), 
	ON(R.string.txt_status_turned_on), 
	OFF(R.string.txt_status_turned_off), 
	UNKNOWN(R.string.txt_status_unknown);

	private final int statusResourceId;

	private FourGState(int statusResourceId) {
		this.statusResourceId = statusResourceId;
	}

	public int getStatusResourceId() {
		return statusResourceId;
	}
}
