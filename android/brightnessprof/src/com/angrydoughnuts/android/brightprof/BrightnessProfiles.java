/****************************************************************************
 * Copyright 2009 kraigs.android@gmail.com
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

package com.angrydoughnuts.android.brightprof;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class BrightnessProfiles extends Activity {
  private static final int ACTIVITY_EDIT = 0;
  private static final int ACTIVITY_CALIBRATE = 1;

  private static final int MENU_EDIT = 0;
  private static final int MENU_DELETE = 1;

  private static final int OPTION_CALIBRATE = 0;

  private int appBrightness;
  private DbAccessor dbAccessor;
  private Cursor listViewCursor;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Initialize the database helper.
    dbAccessor = new DbAccessor(this);
    setContentView(R.layout.main);

    // Button to close the main window.
    Button closeBtn = (Button) findViewById(R.id.close_button);
    closeBtn.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        finish();
      }
    });
    // Button to open the edit dialog (in add mode).
    Button addBtn = (Button) findViewById(R.id.add_button);
    addBtn.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        Intent i = new Intent(getApplication(), EditActivity.class);
        startActivityForResult(i, ACTIVITY_EDIT);
      }
    });

    // Setup auto brightness checkbox handler.
    CheckBox checkbox = (CheckBox) findViewById(R.id.auto_brightness);
    checkbox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
      public void onCheckedChanged(
          CompoundButton buttonView, boolean isChecked) {
        Util.setAutoBrightnessEnabled(getContentResolver(), isChecked);
        lockBrightnessControls(isChecked);
        // Update the app brightness in case auto brightness changed it.
        appBrightness = Util.getPhoneBrighness(getContentResolver(), dbAccessor);
        setBrightness(appBrightness);
        refreshDisplay();
      }
    });

    // Setup slider.
    SeekBar slider = (SeekBar) findViewById(R.id.slider);
    slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      public void onProgressChanged(SeekBar seekBar, int progress,
          boolean fromTouch) {
        if (fromTouch) {
          setBrightness(progress);
          refreshDisplay();
        }
      }

      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    // Get a database cursor.
    listViewCursor = dbAccessor.getAllProfiles();
    startManagingCursor(listViewCursor);
    // Populate the list view using the Cursor.
    String[] from = new String[] { "name" };
    int[] to = new int[] { R.id.profile_name };
    SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
        R.layout.profile, listViewCursor, from, to);
    ListView profileList = (ListView) findViewById(R.id.profile_list);
    profileList.setAdapter(adapter);
    // Set the per-item click handler.
    profileList.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position,
          long id) {
        listViewCursor.moveToPosition(position);
        int brightness = listViewCursor.getInt(listViewCursor
            .getColumnIndexOrThrow(DbHelper.PROF_VALUE_COL));
        setBrightness(brightness);
        // TODO(cgallek): This will terminate the application after a profile
        // is selected. Consider making this a configurable option.
        finish();
      }
    });
    registerForContextMenu(profileList);
  }

  @Override
  protected void onDestroy() {
    dbAccessor.closeConnections();
    super.onDestroy();
  }

  @Override
  protected void onResume() {
    // Lookup the initial system brightness and set our app's brightness
    // percentage appropriately.
    appBrightness = Util.getPhoneBrighness(getContentResolver(), dbAccessor);
    // Set the value for the brightness text field and slider.
    refreshDisplay();

    super.onResume();
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenuInfo menuInfo) {
    menu.add(Menu.NONE, MENU_EDIT, Menu.NONE, R.string.edit);
    menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, R.string.delete);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    switch (item.getItemId()) {
      case MENU_EDIT:
        listViewCursor.moveToPosition(info.position);
        Intent i = new Intent(getApplication(), EditActivity.class);
        i.putExtra(DbHelper.PROF_ID_COL, listViewCursor.getInt(listViewCursor
            .getColumnIndexOrThrow(DbHelper.PROF_ID_COL)));
        i.putExtra(DbHelper.PROF_NAME_COL, listViewCursor
            .getString(listViewCursor
                .getColumnIndexOrThrow(DbHelper.PROF_NAME_COL)));
        i.putExtra(DbHelper.PROF_VALUE_COL, listViewCursor
            .getInt(listViewCursor
                .getColumnIndexOrThrow(DbHelper.PROF_VALUE_COL)));
        startActivityForResult(i, ACTIVITY_EDIT);
        return true;
      case MENU_DELETE:
        listViewCursor.moveToPosition(info.position);
        int id = listViewCursor.getInt(listViewCursor
            .getColumnIndexOrThrow(DbHelper.PROF_ID_COL));
        dbAccessor.deletProfile(id);
        listViewCursor.requery();
        return true;
      default:
        return super.onContextItemSelected(item);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    boolean result = super.onCreateOptionsMenu(menu);
    MenuItem calibrate = menu.add(0, OPTION_CALIBRATE, 0, R.string.calibrate);
    calibrate.setIcon(android.R.drawable.ic_menu_preferences);
    return result;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    boolean result = super.onPrepareOptionsMenu(menu);
    // Don't setup the calibrate menu item if auto brightness is enabled.
    // Trying to calibrate while it's on is weird...
    MenuItem calibrate = menu.findItem(OPTION_CALIBRATE);
    if (Util.supportsAutoBrightness(getContentResolver()) &&
        Util.getAutoBrightnessEnabled(getContentResolver())) {
      calibrate.setEnabled(false);
    } else {
      calibrate.setEnabled(true);
    }
    return result;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case OPTION_CALIBRATE:
        Intent i = new android.content.Intent(getApplicationContext(),
            CalibrateActivity.class);
        startActivityForResult(i, ACTIVITY_CALIBRATE);
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_VOLUME_DOWN:
        setBrightness(getBrightness() - 10);
        return true;
      case KeyEvent.KEYCODE_VOLUME_UP:
        setBrightness(getBrightness() + 10);
        return true;
      default:
        return super.onKeyDown(keyCode, event);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_CANCELED) {
      return;
    }

    if (requestCode != ACTIVITY_EDIT) {
      return;
    }

    Bundle extras = data.getExtras();
    int id = extras.getInt(DbHelper.PROF_ID_COL);
    switch (resultCode) {
      case Activity.RESULT_OK:
        String name = extras.getString(DbHelper.PROF_NAME_COL);
        int brightness = extras.getInt(DbHelper.PROF_VALUE_COL);

        dbAccessor.updateProfile(id, name, brightness);
        listViewCursor.requery();
        break;
    }
  }

  private void refreshDisplay() {
    TextView brightnessText = (TextView) findViewById(R.id.brightness);
    brightnessText.setText(getString(R.string.brightness) + " "
        + getBrightness() + "%");

    SeekBar slider = (SeekBar) findViewById(R.id.slider);
    slider.setProgress(getBrightness());
 
    // Show/Hide the auto brightness check box.
    CheckBox checkbox = (CheckBox) findViewById(R.id.auto_brightness);
    if (Util.supportsAutoBrightness(getContentResolver())) {
      checkbox.setVisibility(View.VISIBLE);
      if (Util.getAutoBrightnessEnabled(getContentResolver())) {
        checkbox.setChecked(true);
        lockBrightnessControls(true);
      } else {
        checkbox.setChecked(false);
        lockBrightnessControls(false);
      }
    } else {
      checkbox.setVisibility(View.GONE);
      lockBrightnessControls(false);
    }
  }

  private int getBrightness() {
    return appBrightness;
  }

  private void setBrightness(int brightness) {
    // Don't try to adjust brightness if auto brightness is enabled.
    if (Util.supportsAutoBrightness(getContentResolver()) &&
        Util.getAutoBrightnessEnabled(getContentResolver())) {
      return;
    }

    if (brightness < 0) {
      appBrightness = 0;
    } else if (brightness > 100) {
      appBrightness = 100;
    } else {
      appBrightness = brightness;
    }
    Util.setPhoneBrightness(getContentResolver(), getWindow(), dbAccessor,
        appBrightness);
    refreshDisplay();
  }

  private void lockBrightnessControls(boolean lock) {
    SeekBar slider = (SeekBar) findViewById(R.id.slider);
    ListView profileList = (ListView) findViewById(R.id.profile_list);

    // Note: setEnabled() doesn't seem to work with this ListView, nor does
    // calling setEnabled() on the individual children of the ListView.
    // The items become grayed out, but the click handlers are still registered.
    // As a work around, simply hide the entire list view.
    if (lock) {
      profileList.setVisibility(View.GONE);
      slider.setEnabled(false);
    } else {
      profileList.setVisibility(View.VISIBLE);
      slider.setEnabled(true);
    }
  }
}
