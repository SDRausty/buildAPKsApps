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

import android.content.Context;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeUtil {
  public static Calendar nextOccurrence(int secondsPastMidnight, int repeat) {
    return nextOccurrence(Calendar.getInstance(), secondsPastMidnight, repeat);
  }

  public static Calendar nextOccurrence(
      int secondsPastMidnight, int repeat, long nextSnooze) {
    return nextOccurrence(
        Calendar.getInstance(), secondsPastMidnight, repeat, nextSnooze);
  }

  public static Calendar nextOccurrence(
      Calendar now, int secondsPastMidnight, int repeat) {
    Calendar then = (Calendar)now.clone();
    then.set(Calendar.DAY_OF_YEAR, 1);  // Explicitly not a DST transition day
    then.set(Calendar.HOUR_OF_DAY, 0);
    then.set(Calendar.MINUTE, 0);
    then.set(Calendar.SECOND, 0);
    then.set(Calendar.MILLISECOND, 0);
    then.add(Calendar.SECOND, secondsPastMidnight);
    then.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR));
    if (then.before(now))
      then.add(Calendar.DATE, 1);
    while (repeat > 0 && !dayIsRepeat(then.get(Calendar.DAY_OF_WEEK), repeat))
      then.add(Calendar.DATE, 1);
    return then;
  }

  public static Calendar nextOccurrence(
      Calendar now, int secondsPastMidnight, int repeat, long nextSnooze) {
    Calendar next = nextOccurrence(now, secondsPastMidnight, repeat);
    if (nextSnooze > now.getTimeInMillis()) {
      Calendar snooze = Calendar.getInstance();
      snooze.setTimeInMillis(nextSnooze);
      if (snooze.before(next))
        return snooze;
    }
    return next;
  }

  private static boolean dayIsRepeat(int calendarDow, int repeat) {
    switch (calendarDow) {
      case Calendar.SUNDAY:
        return (1 & repeat) != 0;
      case Calendar.MONDAY:
        return (2 & repeat) != 0;
      case Calendar.TUESDAY:
        return (4 & repeat) != 0;
      case Calendar.WEDNESDAY:
        return (8 & repeat) != 0;
      case Calendar.THURSDAY:
        return (16 & repeat) != 0;
      case Calendar.FRIDAY:
        return (32 & repeat) != 0;
      case Calendar.SATURDAY:
        return (64 & repeat) != 0;
    }
    return true;
  }

  private static final int EVERYDAY = 1 | 2 | 4 | 8 | 16 | 32 | 64;
  private static final int WEEKDAYS = 2 | 4 | 8 | 16 | 32;
  private static final int WEEKENDS = 1 | 64;
  public static String repeatString(Context c, int repeat) {
    if (repeat <= 0)
      return "";
    else if (repeat == EVERYDAY)
      return c.getString(R.string.everyday);
    else if (repeat == WEEKDAYS)
      return c.getString(R.string.weekdays);
    else if (repeat == WEEKENDS)
      return c.getString(R.string.weekends);

    String s = "";
    if ((1 & repeat) != 0)
      s += c.getString(R.string.dow_sun_short) + " ";
    if ((2 & repeat) != 0)
      s += c.getString(R.string.dow_mon_short) + " ";
    if ((4 & repeat) != 0)
      s += c.getString(R.string.dow_tue_short) + " ";
    if ((8 & repeat) != 0)
      s += c.getString(R.string.dow_wed_short) + " ";
    if ((16 & repeat) != 0)
      s += c.getString(R.string.dow_thu_short) + " ";
    if ((32 & repeat) != 0)
      s += c.getString(R.string.dow_fri_short) + " ";
    if ((64 & repeat) != 0)
      s += c.getString(R.string.dow_sat_short) + " ";
    return s;
  }

  public static Calendar nextMinute() {
    return nextMinute(Calendar.getInstance());
  }

  public static Calendar nextMinute(int minutes) {
    return nextMinute(Calendar.getInstance(), minutes);
  }

  public static Calendar nextMinute(Calendar now) {
    return nextMinute(now, 1);
  }

  public static Calendar nextMinute(Calendar now, int minutes) {
    Calendar then = (Calendar)now.clone();
    then.set(Calendar.SECOND, 0);
    then.set(Calendar.MILLISECOND, 0);
    then.add(Calendar.MINUTE, minutes);
    return then;
  }

  public static long nextMinuteDelay() {
    Calendar now = Calendar.getInstance();
    Calendar then = nextMinute(now);
    return then.getTimeInMillis() - now.getTimeInMillis();
  }

  public static String until(Context c, Calendar alarm) {
    Calendar now = Calendar.getInstance();
    now.set(Calendar.SECOND, 0);
    now.set(Calendar.MILLISECOND, 0);
    return until(c, now, alarm);
  }

  public static String until(Context c, Calendar from, Calendar to) {
    long minutes = (to.getTimeInMillis() - from.getTimeInMillis()) / 1000 / 60;
    long days = minutes / 1440;
    minutes -= (days * 1440);
    long hours = minutes / 60;
    minutes -= (hours * 60);

    String s = "";
    if (days > 0)
      s += days > 1 ? c.getString(R.string.days, days) + " " :
        c.getString(R.string.day, days) + " ";
    if (hours > 0)
      s += hours > 1 ? c.getString(R.string.hours, hours) + " " :
        c.getString(R.string.hour, hours) + " ";
    if (minutes > 0)
      s += minutes > 1 ? c.getString(R.string.minutes, minutes) + " " :
        c.getString(R.string.minute, minutes) + " ";
    return s;
  }

  public static String format(Context context, Calendar c) {
    SimpleDateFormat f = DateFormat.is24HourFormat(context) ?
      new SimpleDateFormat("HH:mm") :
      new SimpleDateFormat("h:mm");
    return f.format(c.getTime());
  }

  public static String formatLong(Context context, Calendar c) {
    SimpleDateFormat f = DateFormat.is24HourFormat(context) ?
      new SimpleDateFormat("HH:mm") :
      new SimpleDateFormat("h:mm a");
    return f.format(c.getTime());
  }
}
