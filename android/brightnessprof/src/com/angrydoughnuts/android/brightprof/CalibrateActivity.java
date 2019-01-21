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
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CalibrateActivity extends Activity {

  private int minBrightness = 20;
  private DbAccessor db;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.calibrate);

    Button okButton = (Button) findViewById(R.id.ok_button);
    Button cancelButton = (Button) findViewById(R.id.cancel_button);

    db = new DbAccessor(this);

    okButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        db.setMinimumBrightness(minBrightness);
        // If the new minimum brightness is greater than the current screen
        // setting, update the setting.
        if (minBrightness > Util.getSystemBrightness(getContentResolver())) {
          Util.setSystemBrightness(getContentResolver(), minBrightness);
        }
        setResult(RESULT_OK);
        finish();
      }
    });

    cancelButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        setResult(RESULT_CANCELED);
        finish();
      }
    });
  }

  @Override
  protected void onDestroy() {
    db.closeConnections();
    super.onDestroy();
  }

  @Override
  protected void onResume() {
    super.onResume();
    updateDisplay();
    Util.setActivityBrightness(getWindow(), minBrightness);

    TextView current_minimum_brightness =
      (TextView) findViewById(R.id.current_min_brightness);
    current_minimum_brightness.setText("" + db.getMinimumBrightness());
  }

  @Override
  protected void onPause() {
    // Return the brightness to it's previous state when the dialog closes.
    Util.setActivityBrightness(getWindow(), -1);
    super.onPause();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_VOLUME_DOWN:
        // Don't allow this value to go to 0. It shuts the screen off.
        if (minBrightness > 1) {
          minBrightness -= 1;
          updateDisplay();
          Util.setActivityBrightness(getWindow(), minBrightness);
        }
        return true;
      case KeyEvent.KEYCODE_VOLUME_UP:
        if (minBrightness < 255) {
          minBrightness += 1;
          updateDisplay();
          Util.setActivityBrightness(getWindow(), minBrightness);
        }
        return true;
      default:
        return super.onKeyDown(keyCode, event);
    }
  }

  private void updateDisplay() {
    TextView test_min_brightness =
      (TextView) findViewById(R.id.test_min_brightness);

    test_min_brightness.setText("" + minBrightness);
  }
}
