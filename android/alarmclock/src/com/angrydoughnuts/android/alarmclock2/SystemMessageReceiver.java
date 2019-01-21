/****************************************************************************
 * Copyright 2016 kraigs.android@gmail.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ****************************************************************************/

package com.angrydoughnuts.android.alarmclock;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public class SystemMessageReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context c, Intent i) {
    if (i.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
      Log.i(TAG, "Timezone change...");
      rescheduleAll(c);
    } else if (i.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
      Log.i(TAG, "Installed...");
      scheduleAll(c);
    } else if (i.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
      Log.i(TAG, "Boot complete...");
      scheduleAll(c);
    }
  }

  private void clearSnooze(Context c) {
    ContentValues val = new ContentValues();
    val.put(AlarmClockProvider.AlarmEntry.NEXT_SNOOZE, 0);
    c.getContentResolver().update(
        AlarmClockProvider.ALARMS_URI, val, null, null);
  }

  private void rescheduleAll(Context c) {
    clearSnooze(c);
    for (Alarm a : enabledAlarms(c.getContentResolver())) {
      Log.i(TAG, "Rescheduling alarm " + a.id);
      AlarmNotificationService.removeAlarmTrigger(c, a.id);
      AlarmNotificationService.scheduleAlarmTrigger(c, a.id, a.tsUTC);
    }
  }

  private void scheduleAll(Context c) {
    clearSnooze(c);
    for (Alarm a : enabledAlarms(c.getContentResolver())) {
      Log.i(TAG, "Scheduling alarm " + a.id);
      AlarmNotificationService.scheduleAlarmTrigger(c, a.id, a.tsUTC);
    }
  }

  private static class Alarm {
    final long id;
    final long tsUTC;
    public Alarm(Cursor c) {
      this.id = c.getLong(c.getColumnIndex(AlarmClockProvider.AlarmEntry._ID));
      this.tsUTC = TimeUtil.nextOccurrence(
          c.getInt(c.getColumnIndex(AlarmClockProvider.AlarmEntry.TIME)),
          c.getInt(c.getColumnIndex(AlarmClockProvider.AlarmEntry.DAY_OF_WEEK)),
          c.getInt(c.getColumnIndex(AlarmClockProvider.AlarmEntry.NEXT_SNOOZE)))
        .getTimeInMillis();
    }
  }

  private List<Alarm> enabledAlarms(ContentResolver r) {
    LinkedList<Alarm> ids = new LinkedList<Alarm>();
    Cursor c = r.query(
        AlarmClockProvider.ALARMS_URI,
        new String[] {
          AlarmClockProvider.AlarmEntry._ID,
          AlarmClockProvider.AlarmEntry.TIME,
          AlarmClockProvider.AlarmEntry.DAY_OF_WEEK,
          AlarmClockProvider.AlarmEntry.NEXT_SNOOZE },
        AlarmClockProvider.AlarmEntry.ENABLED + " == 1",
        null, null);
    while (c.moveToNext())
      ids.add(new Alarm(c));
    c.close();
    return ids;
  }

  private static final String TAG = SystemMessageReceiver.class.getSimpleName();
}
