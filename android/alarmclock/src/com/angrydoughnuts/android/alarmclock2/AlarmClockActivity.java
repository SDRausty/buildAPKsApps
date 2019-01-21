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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import java.util.Calendar;
import java.util.LinkedList;

public class AlarmClockActivity extends Activity {
  private Runnable refresh_tick;
  private final Handler handler = new Handler();
  private final TimePicker.OnTimePickListener new_alarm =
    new TimePicker.OnTimePickListener() {
      @Override
      public void onTimePick(int s) {
        AlarmNotificationService.newAlarm(getApplicationContext(), s);
      }
    };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.alarm_list);

    // Map AlarmEntry fields to list view item views.
    final ResourceCursorAdapter adapter = new ResourceCursorAdapter(
        this, R.layout.alarm_list_item, null, 0) {
        @Override
        public void bindView(View v, Context context, Cursor c) {
          final int secondsPastMidnight =
            c.getInt(c.getColumnIndex(AlarmClockProvider.AlarmEntry.TIME));
          final int enabled =
            c.getInt(c.getColumnIndex(AlarmClockProvider.AlarmEntry.ENABLED));
          final String label =
            c.getString(c.getColumnIndex(AlarmClockProvider.AlarmEntry.NAME));
          final int repeats =
            c.getInt(c.getColumnIndex(AlarmClockProvider.AlarmEntry.DAY_OF_WEEK));
          final long nextSnooze =
            c.getLong(c.getColumnIndex(AlarmClockProvider.AlarmEntry.NEXT_SNOOZE));
          final Calendar next =
            TimeUtil.nextOccurrence(secondsPastMidnight, repeats, nextSnooze);

          ((TextView)v.findViewById(R.id.time))
            .setText(TimeUtil.formatLong(getApplicationContext(), next));
          ((TextView)v.findViewById(R.id.countdown))
            .setText(TimeUtil.until(getApplicationContext(), next));
          ((TextView)v.findViewById(R.id.label))
            .setText(label);
          ((TextView)v.findViewById(R.id.repeat))
            .setText(TimeUtil.repeatString(getApplicationContext(), repeats));
          ((CheckBox)v.findViewById(R.id.enabled))
            .setChecked(enabled != 0);
        }
      };

    // Setup list view to enable/disable entries upon click.
    ListView list = (ListView)findViewById(R.id.alarm_list);
    list.setAdapter(adapter);
    list.setEmptyView(findViewById(R.id.empty_list));
    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int x, long id) {
          boolean check = ((CheckBox)v.findViewById(R.id.enabled)).isChecked();

          ContentValues val = new ContentValues();
          val.put(AlarmClockProvider.AlarmEntry.ENABLED, !check);
          val.put(AlarmClockProvider.AlarmEntry.NEXT_SNOOZE, 0);
          getContentResolver().update(
              ContentUris.withAppendedId(AlarmClockProvider.ALARMS_URI, id),
              val, null, null);

          if (check) {
            AlarmNotificationService.removeAlarmTrigger(
                getApplicationContext(), id);
          } else {
            DbUtil.Alarm a = DbUtil.Alarm.get(getApplicationContext(), id);
            long nextUTC = TimeUtil.nextOccurrence(a.time, a.repeat)
              .getTimeInMillis();
            AlarmNotificationService.scheduleAlarmTrigger(
                getApplicationContext(), id, nextUTC);
          }
        }
      });
    // And to display the options menu on long click.
    list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> p, View v, int x, long id) {
          AlarmOptions options = new AlarmOptions();
          Bundle b = new Bundle();
          b.putLong(AlarmNotificationService.ALARM_ID, id);
          options.setArguments(b);
          options.show(getFragmentManager(), "alarm_options");
          return true;
        }
      });

    // Define the cursor loader for the list view to query all AlarmEntry
    // columns and sort by time.
    final Loader<Cursor> loader = getLoaderManager().initLoader(
        0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
              return new CursorLoader(
                  getApplicationContext(), AlarmClockProvider.ALARMS_URI,
                  new String[] {
                    AlarmClockProvider.AlarmEntry._ID,
                    AlarmClockProvider.AlarmEntry.TIME,
                    AlarmClockProvider.AlarmEntry.ENABLED,
                    AlarmClockProvider.AlarmEntry.NAME,
                    AlarmClockProvider.AlarmEntry.DAY_OF_WEEK,
                    AlarmClockProvider.AlarmEntry.NEXT_SNOOZE },
                  null, null, AlarmClockProvider.AlarmEntry.TIME + " ASC");
            }
            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
              adapter.changeCursor(data);
            }
            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
              adapter.changeCursor(null);
            }
          });

    // Force the cursor loader to refresh on the minute every minute to
    // recompute countdown displays.
    refresh_tick = new Runnable() {
        @Override
        public void run() {
          loader.forceLoad();
          handler.postDelayed(refresh_tick, TimeUtil.nextMinuteDelay());
        }
      };

    // For debug binaries, display a button that creates an alarm 5 seconds
    // in the future.
    if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
      final View v = findViewById(R.id.test_alarm);
      v.setVisibility(View.VISIBLE);
      v.setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              final Calendar c = Calendar.getInstance();
              final int secondsPastMidnight = 5 +
                c.get(Calendar.HOUR_OF_DAY) * 3600 +
                c.get(Calendar.MINUTE) * 60 +
                c.get(Calendar.SECOND);
              AlarmNotificationService.newAlarm(
                  getApplicationContext(), secondsPastMidnight);
            }
          });
    }

    // Setup the new alarm button.
    findViewById(R.id.new_alarm).setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            TimePicker time_pick = new TimePicker();
            time_pick.setListener(new_alarm);
            time_pick.show(getFragmentManager(), "new_alarm");
          }
        });

    // Listener can not be serialized in time picker, so it must be explicitly
    // set each time.
    if (savedInstanceState != null) {
      TimePicker t = (TimePicker)getFragmentManager()
        .findFragmentByTag("new_alarm");
      if (t != null)
        t.setListener(new_alarm);
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    handler.postDelayed(refresh_tick, TimeUtil.nextMinuteDelay());
    AlarmNotificationService.maybeShowDismiss(this);
  }

  @Override
  public void onRestart() {
    super.onRestart();
    getLoaderManager().getLoader(0).forceLoad();
  }

  @Override
  public void onStop() {
    super.onStop();
    handler.removeCallbacks(refresh_tick);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.alarm_list_menu, menu);
    menu.findItem(R.id.display_notification)
      .setChecked(PreferenceManager.getDefaultSharedPreferences(this)
        .getBoolean(AlarmNotificationService.DISPLAY_NOTIFICATION, true));
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.default_options:
      AlarmOptions options = new AlarmOptions();
      Bundle b = new Bundle();
      b.putLong(AlarmNotificationService.ALARM_ID, DbUtil.Settings.DEFAULTS_ID);
      options.setArguments(b);
      options.show(getFragmentManager(), "default_alarm_options");
      return true;

    case R.id.display_notification:
      boolean new_val = !item.isChecked();
      item.setChecked(new_val);
      PreferenceManager.getDefaultSharedPreferences(this)
        .edit()
        .putBoolean(AlarmNotificationService.DISPLAY_NOTIFICATION, new_val)
        .commit();
      AlarmNotificationService.refreshNotificationBar(this);
      return true;

    case R.id.delete_all:
      new DeleteAllConfirmation()
        .show(getFragmentManager(), "confirm_delete_all");

      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  public static class DeleteAllConfirmation extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      return new AlertDialog.Builder(getContext())
        .setTitle(R.string.delete)
        .setMessage(R.string.delete_all_sure)
        .setNegativeButton(R.string.cancel, null)
        .setPositiveButton(
            R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  // Find all of the enabled alarm ids.
                  LinkedList<Long> ids = new LinkedList<Long>();
                  Cursor c = getContext().getContentResolver().query(
                      AlarmClockProvider.ALARMS_URI,
                      new String[] { AlarmClockProvider.AlarmEntry._ID },
                      AlarmClockProvider.AlarmEntry.ENABLED + " == 1",
                          null, null);
                  while (c.moveToNext())
                    ids.add(c.getLong(c.getColumnIndex(
                        AlarmClockProvider.AlarmEntry._ID)));
                  c.close();
                  // Delete the entire alarm table.
                  getContext().getContentResolver().delete(
                      AlarmClockProvider.ALARMS_URI, null, null);
                  // Unschedule any alarms that were active.
                  for (long id : ids)
                    AlarmNotificationService.removeAlarmTrigger(
                        getContext(), id);
                }
              }).create();
    }
  }
}
