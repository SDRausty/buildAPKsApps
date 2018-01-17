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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;

public class TimePicker extends DialogFragment {
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    super.onCreateDialog(savedInstanceState);
    // Default state in now with no repeats.
    final Calendar now = Calendar.getInstance();
    hour = now.get(Calendar.HOUR_OF_DAY);
    minute = now.get(Calendar.MINUTE);
    repeat = 0;

    // Override defaults with user-specified state.
    if (getArguments() != null) {
      int secondsPastMidnight = getArguments().getInt(TIME, -1);
      if (secondsPastMidnight >= 0) {
        hour = secondsPastMidnight / 3600;
        minute = secondsPastMidnight / 60 - hour * 60;
      }
      repeat = getArguments().getInt(REPEAT, 0);
    }

    // Override all state with previous state.
    if (savedInstanceState != null) {
      hour = savedInstanceState.getInt("hour");
      minute = savedInstanceState.getInt("minute");
      repeat = savedInstanceState.getInt("repeat");
    }

    final View v = View.inflate(getContext(), R.layout.time_picker, null);

    final AlertDialog d = new AlertDialog.Builder(getContext())
      .setView(v)
      .setNegativeButton(R.string.cancel, null)
      .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            if (listener != null)
              listener.onTimePick(seconds());
          }
        })
      .create();

    // Force the soft keyboard to display for faster input.
    d.getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

    final TextView t = (TextView)v.findViewById(R.id.picker_countdown);
    t.setText(until());

    // Setup an AM/PM button, but only for locales that use it.
    final Button am_pm = (Button)v.findViewById(R.id.picker_am_pm);
    if (DateFormat.is24HourFormat(getContext())) {
      am_pm.setVisibility(View.GONE);
    } else {
      am_pm.setVisibility(View.VISIBLE);
      am_pm.setText(ampm());
      am_pm.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            if (hour < 12)
              hour += 12;
            else
              hour -= 12;
            am_pm.setText(ampm());
            t.setText(until());
          }
        });
    }

    // The edit field should be fully selected and focused at creation time.
    final EditText e = (EditText)v.findViewById(R.id.picker_time_entry);
    e.setText(time());
    e.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        @Override
        public void onTextChanged(CharSequence s, int st, int b, int c) {}
        @Override
        public void afterTextChanged(Editable s) {
          // Disable the 'OK' button until we have valid input.
          d.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

          // Strip out colons and leading zeros.
          String hhmm = s.toString().replaceAll(":", "");
          int new_hour;
          int new_minute;
          // Both 12 and 24 hour formats require at least 3 digits in a valid
          // time.
          if (hhmm.length() < 3)
            return;
          try {
            // Try to parse the last two digits as minutes and the remaining
            // leading digits as hour.
            new_hour = Integer.parseInt(hhmm.substring(0, hhmm.length() - 2));
            new_minute = Integer.parseInt(hhmm.substring(
                hhmm.length() - 2, hhmm.length()));
          } catch (NumberFormatException e) {
            return;
          }

          // Validate the hour field.  We internally represent using 24 hour
          // time, so 12 hour input must also be converted during validation.
          if (DateFormat.is24HourFormat(getContext())) {
            if (new_hour < 0 || new_hour > 23)
              return;
          } else {
            if (new_hour < 1 || new_hour > 12)
              return;
            // Note that 12 midnight is represented as 0 in 24 hour time.
            if (new_hour == 12)
              new_hour = 0;
            // If the previous valid our was PM, add 12 to represent PM for the
            // new value.
            if (hour > 11)
              new_hour += 12;
          }
          // Validate the minute field.
          if (new_minute < 0 || new_minute > 59)
            return;

          // Re-enable the OK button now that we have valid input.
          d.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);

          final boolean changed = hour != new_hour || minute != new_minute;
          hour = new_hour;
          minute = new_minute;

          if (changed) {
            // Simply calling setText here will trigger an infinite loop.
            // Temporarily remove our self as a listener while updating text.
            e.removeTextChangedListener(this);
            refresh(e, t, am_pm);
            e.addTextChangedListener(this);
          }
        }
      });
    // Simulate clicking 'OK' when the done key is pressed on the keyboard
    // (but only of 'OK' is enabled with valid input).
    e.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int action, KeyEvent e) {
          Button ok = d.getButton(DialogInterface.BUTTON_POSITIVE);
          if (action == EditorInfo.IME_ACTION_DONE && ok.isEnabled()) {
            ok.performClick();
            return true;
          }
          return false;
        }
      });

    final View.OnTouchListener button_up = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          if ((event.getAction() & MotionEvent.ACTION_MASK) ==
              MotionEvent.ACTION_UP)
            increment.removeCallbacksAndMessages(null);
          return false;
        }
      };

    final View hour_inc = v.findViewById(R.id.hour_inc);
    hour_inc.setOnTouchListener(button_up);
    hour_inc.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          ++hour;
          if (hour > 23) hour = 0;
          refresh(e, t, am_pm);
        }
      });
    hour_inc.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
          increment.post(new Runnable() {
              @Override
              public void run() {
                ++hour;
                if (hour > 23) hour = 0;
                refresh(e, t, am_pm);
                increment.postDelayed(this, 200);
              }
            });
          return true;
        }
      });

    final View hour_dec = v.findViewById(R.id.hour_dec);
    hour_dec.setOnTouchListener(button_up);
    hour_dec.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            --hour;
            if (hour < 0) hour = 23;
            refresh(e, t, am_pm);
          }
        });
    hour_dec.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
          increment.post(new Runnable() {
              @Override
              public void run() {
                --hour;
                if (hour < 0) hour = 23;
                refresh(e, t, am_pm);
                increment.postDelayed(this, 200);
              }
            });
          return true;
        }
      });

    final View minute_inc = v.findViewById(R.id.minute_inc);
    minute_inc.setOnTouchListener(button_up);
    minute_inc.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          ++minute;
          if (minute > 59) minute = 0;
          refresh(e, t, am_pm);
        }
      });
    minute_inc.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
          increment.post(new Runnable() {
              @Override
              public void run() {
                minute = minute / 5 * 5 + 5;
                if (minute > 59) minute = 0;
                refresh(e, t, am_pm);
                increment.postDelayed(this, 200);
              }
            });
          return true;
        }
      });

    final View minute_dec = v.findViewById(R.id.minute_dec);
    minute_dec.setOnTouchListener(button_up);
    minute_dec.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            --minute;
            if (minute < 0) minute = 59;
            refresh(e, t, am_pm);
          }
        });
    minute_dec.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
          increment.post(new Runnable() {
              @Override
              public void run() {
                int new_minute = minute / 5 * 5;
                if (new_minute == minute)
                  new_minute -= 5;
                if (new_minute < 0) new_minute = 59;
                minute = new_minute;
                refresh(e, t, am_pm);
                increment.postDelayed(this, 200);
              }
            });
          return true;
        }
      });

    return d;
  }

  private int seconds() { return hour * 3600 + minute * 60; }
  private Calendar next() { return TimeUtil.nextOccurrence(seconds(), repeat); }
  private String time() { return TimeUtil.format(getContext(), next()); }
  private String until() { return TimeUtil.until(getContext(), next()); }
  private int ampm() { return (hour < 12) ? R.string.am : R.string.pm; }

  private void refresh(EditText time, TextView countdown, Button am_pm) {
    time.setText(time());
    time.setSelection(time.getText().length());
    countdown.setText(until());
    am_pm.setText(ampm());
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt("hour", hour);
    outState.putInt("minute", minute);
    outState.putInt("repeat", repeat);
  }

  private final Handler increment = new Handler();
  private int hour;
  private int minute;
  private int repeat;
  private OnTimePickListener listener = null;
  public void setListener(OnTimePickListener l) { listener = l; }

  // Input parameters
  public static final String TIME = "time";
  public static final String REPEAT = "repeat";
  public static interface OnTimePickListener {
    abstract void onTimePick(int secondsPastMidnight);
  }
}
