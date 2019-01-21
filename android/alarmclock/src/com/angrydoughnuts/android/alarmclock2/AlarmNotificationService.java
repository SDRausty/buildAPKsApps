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

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Log;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;

public class AlarmNotificationService extends Service {
  public static final String DISPLAY_NOTIFICATION = "NOTIFICATION_ICON";
  public static final String ALARM_ID = "alarm_id";
  private static final String TIME_UTC = "time_utc";

  /**
   * Write new alarm information to the data store and schedule it.
   */
  public static long newAlarm(Context c, int secondsPastMidnight) {

    ContentValues v = new ContentValues();
    v.put(AlarmClockProvider.AlarmEntry.TIME, secondsPastMidnight);
    Uri u = c.getContentResolver().insert(AlarmClockProvider.ALARMS_URI, v);
    long alarmid = ContentUris.parseId(u);
    Log.i(TAG, "New alarm: " + alarmid + " (" + u +")");

    // Inserted entry is ENABLED by default with no options.  Schedule the
    // first occurrence.
    Calendar ts = TimeUtil.nextOccurrence(secondsPastMidnight, 0);
    scheduleAlarmTrigger(c, alarmid, ts.getTimeInMillis());

    return alarmid;
  }

  /**
   * Schedule an alarm event for a previously created alarm.
   */
  public static void scheduleAlarmTrigger(Context c, long alarmid, long tsUTC) {
    c.startService(new Intent(c, AlarmNotificationService.class)
                   .putExtra(COMMAND, SCHEDULE_TRIGGER)
                   .putExtra(ALARM_ID, alarmid)
                   .putExtra(TIME_UTC, tsUTC));
  }

  /**
   * Un-schedule a previously scheduled alarm event.
   */
  public static void removeAlarmTrigger(Context c, long alarmid) {
    c.startService(new Intent(c, AlarmNotificationService.class)
                   .putExtra(COMMAND, REMOVE_TRIGGER)
                   .putExtra(ALARM_ID, alarmid));
  }

  /**
   * Dismiss all of the currently firing alarms.  Any marked for repeat will
   * be rescheduled appropriately.
   */
  public static void dismissAllAlarms(Context c) {
    c.startService(new Intent(c, AlarmNotificationService.class)
                   .putExtra(COMMAND, DISMISS_ALL));
  }

  /**
   * Snooze all of the currently firing alarms.
   */
  public static void snoozeAllAlarms(Context c, long snoozeUtc) {
    c.startService(new Intent(c, AlarmNotificationService.class)
                   .putExtra(COMMAND, SNOOZE_ALL)
                   .putExtra(TIME_UTC, snoozeUtc));
  }

  /**
   * Trigger a notification bar refresh.
   */
  public static void refreshNotificationBar(Context c) {
    c.startService(new Intent(c, AlarmNotificationService.class)
                   .putExtra(COMMAND, REFRESH));
  }

  /**
   * Show the dismiss activity if an alarm is firing.
   */
  public static void maybeShowDismiss(Context c) {
    c.startService(new Intent(c, AlarmNotificationService.class)
                   .putExtra(COMMAND, MAYBE_SHOW_DISMISS));
  }

  private ActiveAlarms activeAlarms = null;

  @Override
  public int onStartCommand(Intent i, int flags, int startId) {
    long alarmid;
    long ts;

    // NOTE: The service should continue running while there are any active
    // alarms.
    switch (i.hasExtra(COMMAND) ? i.getExtras().getInt(COMMAND) : -1) {
    case TRIGGER_ALARM_NOTIFICATION:
      handleTriggerAlarm(i);
      return START_NOT_STICKY;
    case DISMISS_ALL:
      dismissAll();
      stopSelf();
      return START_NOT_STICKY;
    case SNOOZE_ALL:
      ts = i.getLongExtra(TIME_UTC, -1);
      snoozeAll(ts);
      stopSelf();
      return START_NOT_STICKY;
    case SCHEDULE_TRIGGER:
      alarmid = i.getLongExtra(ALARM_ID, -1);
      ts = i.getLongExtra(TIME_UTC, -1);
      scheduleTrigger(alarmid, ts);
      break;
    case REMOVE_TRIGGER:
      alarmid = i.getLongExtra(ALARM_ID, -1);
      removeTrigger(alarmid);
      break;
    case REFRESH:
      refreshNotifyBar();
      break;
    case MAYBE_SHOW_DISMISS:
      if (activeAlarms != null && !activeAlarms.alarmids.isEmpty()) {
        startActivity(
            new Intent(this, AlarmNotificationActivity.class)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(ALARM_ID, activeAlarms.alarmids.iterator().next()));

      }
      break;
    }

    if (activeAlarms == null)
      stopSelf(startId);

    return START_NOT_STICKY;
  }

  @Override
  public void onDestroy() {
    if (activeAlarms == null)
      return;

    activeAlarms.release();
    activeAlarms = null;
  }

  @Override
  public IBinder onBind(Intent intent) { return null; }

  private void handleTriggerAlarm(Intent i) {
    final long alarmid = i.getLongExtra(ALARM_ID, -1);
    final DbUtil.Settings settings =
      DbUtil.Settings.get(getApplicationContext(), alarmid);

    PowerManager.WakeLock w = null;
    if (i.hasExtra(AlarmTriggerReceiver.WAKELOCK_ID)) {
      w = AlarmTriggerReceiver.consumeLock(
          i.getExtras().getInt(AlarmTriggerReceiver.WAKELOCK_ID));
    }

    if (w == null)
      Log.e(TAG, "No wake lock present for alarm trigger " + alarmid);

    if (activeAlarms == null) {
      activeAlarms = new ActiveAlarms(getApplicationContext(), w, settings);
    } else {
      Log.i(TAG, "Already wake-locked, releasing extra lock");
      w.release();
    }
    activeAlarms.alarmids.add(alarmid);

    String labels = "";
    for (long id : activeAlarms.alarmids) {
      String label = DbUtil.Alarm.get(getApplicationContext(), id).label;
      if (!label.isEmpty()) {
        if (labels.isEmpty())
          labels = label;
        else
          labels += ", " + label;
      }
    }

    Intent notify = new Intent(this, AlarmNotificationActivity.class)
      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      .putExtra(ALARM_ID, alarmid);

    final Notification notification =
      new Notification.Builder(this)
      .setContentTitle(getString(R.string.app_name))
      .setContentText(labels.isEmpty() ? getString(R.string.dismiss) : labels)
      .setSmallIcon(R.drawable.ic_alarm_on)
      .setContentIntent(PendingIntent.getActivity(this, 0, notify, 0))
      .setCategory(Notification.CATEGORY_ALARM)
      .setPriority(Notification.PRIORITY_MAX)
      .setVisibility(Notification.VISIBILITY_PUBLIC)
      .setOngoing(true)
      .setLights(Color.WHITE, 1000, 1000)
      .setVibrate(settings.vibrate ? new long[] {1000, 1000} : null)
      .build();
    notification.flags |= Notification.FLAG_INSISTENT;  // Loop sound/vib/blink
    startForeground(FIRING_ALARM_NOTIFICATION_ID, notification);

    refreshNotifyBar();

    startActivity(notify);
  }

  private void dismissAll() {
    if (activeAlarms == null) {
      Log.w(TAG, "No active alarms when dismissed");
      return;
    }

    for (long alarmid : activeAlarms.alarmids) {
      final DbUtil.Alarm a = DbUtil.Alarm.get(this, alarmid);
      ContentValues v = new ContentValues();
      v.put(AlarmClockProvider.AlarmEntry.NEXT_SNOOZE, 0);
      if (a.repeat == 0)
        v.put(AlarmClockProvider.AlarmEntry.ENABLED, false);
      int r = getContentResolver().update(
          ContentUris.withAppendedId(AlarmClockProvider.ALARMS_URI, alarmid),
          v, null, null);
      if (r < 1) {
        Log.e(TAG, "Failed to dismiss " + alarmid);
      }
      if (a.repeat != 0) {
        final long nextUTC =
          TimeUtil.nextOccurrence(a.time, a.repeat).getTimeInMillis();
        AlarmNotificationService.scheduleAlarmTrigger(this, alarmid, nextUTC);
      }
    }

    activeAlarms.alarmids.clear();
    refreshNotifyBar();
  }

  private void snoozeAll(long snoozeUTC) {
    if (activeAlarms == null) {
      Log.w(TAG, "No active alarms when snoozed");
      return;
    }

    for (long alarmid : activeAlarms.alarmids) {
      ContentValues v = new ContentValues();
      v.put(AlarmClockProvider.AlarmEntry.NEXT_SNOOZE, snoozeUTC);
      int r = getContentResolver().update(
          ContentUris.withAppendedId(AlarmClockProvider.ALARMS_URI, alarmid),
          v, null, null);
      if (r < 1) {
        Log.e(TAG, "Failed to snooze " + alarmid);
      }
      scheduleTrigger(alarmid, snoozeUTC);
    }

    activeAlarms.alarmids.clear();
    refreshNotifyBar();
  }

  private void scheduleTrigger(long alarmid, long tsUTC) {
    // Intents are considered equal if they have the same action, data, type,
    // class, and categories.  In order to schedule multiple alarms, every
    // pending intent must be different.  This means that we must encode
    // the alarm id in the request code.
    PendingIntent schedule = PendingIntent.getBroadcast(
        this, (int)alarmid, new Intent(this, AlarmTriggerReceiver.class)
        .putExtra(ALARM_ID, alarmid), 0);

    ((AlarmManager)getSystemService(Context.ALARM_SERVICE))
        .setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tsUTC, schedule);
    refreshNotifyBar();
  }

  private void removeTrigger(long alarmid) {
    PendingIntent schedule = PendingIntent.getBroadcast(
        this, (int)alarmid, new Intent(this, AlarmTriggerReceiver.class)
        .putExtra(ALARM_ID, alarmid), 0);

    ((AlarmManager)getSystemService(Context.ALARM_SERVICE)).cancel(schedule);
    refreshNotifyBar();
  }

  private void refreshNotifyBar() {
    final NotificationManager manager =
      (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    // Recursive trigger.
    final PendingIntent tick = PendingIntent.getService(
        this, 0, new Intent(this, AlarmNotificationService.class)
        .putExtra(AlarmNotificationService.COMMAND,
                  AlarmNotificationService.REFRESH), 0);

    final Cursor c = getContentResolver().query(
        AlarmClockProvider.ALARMS_URI,
        new String[] { AlarmClockProvider.AlarmEntry.TIME,
                       AlarmClockProvider.AlarmEntry.ENABLED,
                       AlarmClockProvider.AlarmEntry.NAME,
                       AlarmClockProvider.AlarmEntry.DAY_OF_WEEK,
                       AlarmClockProvider.AlarmEntry.NEXT_SNOOZE },
        AlarmClockProvider.AlarmEntry.ENABLED + " == 1",
        null, null);

    // Clear notification bar when there are no alarms enabled or when there
    // is an alarm currently firing (that alarm will create its own
    // notification), or when the setting is disabled.
    if (c.getCount() == 0 ||
        (activeAlarms != null && !activeAlarms.alarmids.isEmpty()) ||
        PreferenceManager.getDefaultSharedPreferences(this)
        .getBoolean(DISPLAY_NOTIFICATION, true) == false) {
      ((AlarmManager)getSystemService(Context.ALARM_SERVICE)).cancel(tick);
      manager.cancel(NEXT_ALARM_NOTIFICATION_ID);
      c.close();
      return;
    }

    // Find the next alarm.
    final Calendar now = Calendar.getInstance();
    Calendar next = null;
    String next_label = "";
    while (c.moveToNext()) {
      final DbUtil.Alarm a = new DbUtil.Alarm(c);
      final Calendar n =
        TimeUtil.nextOccurrence(now, a.time, a.repeat, a.next_snooze);
      if (next == null || n.before(next)) {
        next = n;
        next_label = a.label;
      }
    }
    c.close();

    // Build notification and display it.
    manager.notify(
        NEXT_ALARM_NOTIFICATION_ID,
        new Notification.Builder(this)
        .setContentTitle(next_label.isEmpty() ?
                         getString(R.string.app_name) :
                         next_label)
        .setContentText(TimeUtil.formatLong(this, next) + " : " +
                        TimeUtil.until(getApplicationContext(), next))
        .setSmallIcon(R.drawable.ic_alarm)
        .setCategory(Notification.CATEGORY_STATUS)
        .setVisibility(Notification.VISIBILITY_PUBLIC)
        .setOngoing(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this, 0, new Intent(this, AlarmClockActivity.class), 0))
        .build());

    // Schedule an update for the notification on the next minute.
    final Calendar wake = TimeUtil.nextMinute();
    ((AlarmManager)getSystemService(Context.ALARM_SERVICE)).setExact(
        AlarmManager.RTC, wake.getTimeInMillis(), tick);
  }

  private static final String TAG =
    AlarmNotificationService.class.getSimpleName();
  // Commands
  private static final String COMMAND = "command";
  private static final int TRIGGER_ALARM_NOTIFICATION = 1;
  private static final int SCHEDULE_TRIGGER = 2;
  private static final int REMOVE_TRIGGER = 3;
  private static final int DISMISS_ALL = 4;
  private static final int SNOOZE_ALL = 5;
  private static final int REFRESH = 6;
  private static final int MAYBE_SHOW_DISMISS = 7;
  // Notification ids
  private static final int FIRING_ALARM_NOTIFICATION_ID = 42;
  private static final int NEXT_ALARM_NOTIFICATION_ID = 69;

  private static class ActiveAlarms {
    public static final int TRIGGER_INC = 1;
    public static final int RESET_VOLUME = 2;

    private PowerManager.WakeLock wakelock = null;
    private HashSet<Long> alarmids = new HashSet<Long>();
    private MediaPlayer player = null;
    private Handler handler = null;
    private Runnable timeout = null;

    public ActiveAlarms(final Context c, PowerManager.WakeLock w,
                        final DbUtil.Settings s) {
      // Since we will be changing the notification channel volume, store
      // the initial value so we can reset it afterward.
      final AudioManager a = (AudioManager)c.getSystemService(
          Context.AUDIO_SERVICE);
      final int init_volume = a.getStreamVolume(AudioManager.STREAM_ALARM);
      a.setStreamVolume(AudioManager.STREAM_ALARM,
                        a.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);

      wakelock = w;
      player = new MediaPlayer();
      // This handler will be used to asynchronously trigger volume adjustments.
      handler = new Handler() {
          @Override
          public void handleMessage(Message m) {
            switch (m.what) {
            case TRIGGER_INC:
              float inc = s.volume_time > 0 ?
                (s.volume_ending - s.volume_starting) / (float)s.volume_time :
                s.volume_ending;
              float next = Math.min(s.volume_ending, (float)m.obj + inc);
              float norm = (float)((Math.pow(5, next/100.0)-1)/4);
              Log.i(TAG, "Incrementing volume to " + norm);
              player.setVolume(norm, norm);
              if (next < s.volume_ending) {
                Message m2 = new Message();
                m2.what = TRIGGER_INC;
                m2.obj = next;
                sendMessageDelayed(m2, 1000);
              }
              break;
            case RESET_VOLUME:
              a.setStreamVolume(AudioManager.STREAM_ALARM, init_volume, 0);
              break;
            }
          }
        };

      // Setup a watchdog to dismiss this alarm if it goes unanswered for
      // 10 minutes.  Otherwise the screen would stay on indefinitely.
      timeout = new Runnable() {
          @Override
          public void run() {
            Log.w(TAG, "Alarm timeout");
            AlarmNotificationService.dismissAllAlarms(c);
            Intent timeout = new Intent(c, AlarmNotificationActivity.class)
              .putExtra(AlarmNotificationActivity.TIMEOUT, true)
              .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(timeout);
          }
        };
      handler.postDelayed(timeout, 10 * 60 * 1000);

      // NOTE: We use the alarm channel for notification sound.
      player.setAudioStreamType(AudioManager.STREAM_ALARM);
      player.setLooping(true);
      final float start = s.volume_starting/(float)100.0;
      player.setVolume(start, start);
      Log.i(TAG, "Starting volume: " + start);

      // Try to load the configured media, but fall back to the system
      // default if that fails.
      try {
        player.setDataSource(c, s.tone_url);
      } catch (IOException e) {
        Log.e(TAG, "Failed loading tone: " + e.toString());
        try {
          player.setDataSource(c, Settings.System.DEFAULT_NOTIFICATION_URI);
        } catch (IOException e2) {
          Log.e(TAG, "Failed loading backup tone: " + e2.toString());
        }
      }

      try {
        player.prepare();
        player.start();
      } catch (IOException | IllegalStateException e) {
        Log.e(TAG, "prepare failed: " + e.toString());
      }

      // Begin the volume fade.
      Message m = new Message();
      m.what = TRIGGER_INC;
      m.obj = (float)s.volume_starting;
      handler.sendMessage(m);
    }

    public void release() {
      if (!alarmids.isEmpty())
        Log.w(TAG, "Releasing wake lock with active alarms! (" +
              alarmids.size() + ")");
      Log.i(TAG, "Releasing wake lock");
      wakelock.release();
      wakelock = null;

      handler.removeCallbacks(timeout);
      handler.removeMessages(ActiveAlarms.TRIGGER_INC);
      handler.sendEmptyMessage(ActiveAlarms.RESET_VOLUME);
      handler = null;

      if (player.isPlaying())
        player.stop();
      player.reset();
      player.release();
      player = null;
    }
  }

  public static class AlarmTriggerReceiver extends BroadcastReceiver {
    public static final String WAKELOCK_ID = "wakelock_id";
    private static final ArrayMap<Integer, PowerManager.WakeLock> locks =
      new ArrayMap<Integer, PowerManager.WakeLock>();
    private static int nextid = 0;

    @Override
    public void onReceive(Context c, Intent i) {
      final long alarmid = i.getLongExtra(ALARM_ID, -1);

      @SuppressWarnings("deprecation")  // SCREEN_DIM_WAKE_LOCK
      PowerManager.WakeLock w =
        ((PowerManager)c.getSystemService(Context.POWER_SERVICE))
        .newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK |
                     PowerManager.ACQUIRE_CAUSES_WAKEUP, "wake id " + nextid);
      w.setReferenceCounted(false);
      w.acquire();
      locks.put(nextid, w);
      Log.i(TAG, "Acquired lock " + nextid + " for alarm " + alarmid);

      c.startService(new Intent(c, AlarmNotificationService.class)
                     .putExtra(ALARM_ID, alarmid)
                     .putExtra(COMMAND, TRIGGER_ALARM_NOTIFICATION)
                     .putExtra(WAKELOCK_ID, nextid++));
    }

    public static PowerManager.WakeLock consumeLock(int id) {
      return locks.remove(id);
    }
  }
}
