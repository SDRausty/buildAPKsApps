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

package com.bwx.bequick.fwk;

public class RangeSetting extends Setting {

	public int value;
	public int max;
	public int min;
	public int minIconId;
	public int maxIconId;
	
	public RangeSetting(int id, int iconId, int title, int min, int max) {
		super(id, iconId, title, null);
		this.min = min;
		this.max = max;
	}
}
