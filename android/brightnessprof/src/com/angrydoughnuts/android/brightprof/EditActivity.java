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
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditActivity extends Activity {
  private static final int UNKNOWN_ID = -1;

  private int profile_id_ = UNKNOWN_ID;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.edit);

    Button okButton = (Button) findViewById(R.id.ok_button);
    Button cancelButton = (Button) findViewById(R.id.cancel_button);

    cancelButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        setResult(RESULT_CANCELED);
        finish();
      }
    });

    final EditText nameBox = (EditText) findViewById(R.id.edit_name);
    final EditText brightnessBox = (EditText) findViewById(R.id.edit_brightness);

    final String name;
    final int brightness;

    Bundle extras = getIntent().getExtras();
    if (extras == null) {
      profile_id_ = UNKNOWN_ID;
      name = "";
      brightness = -1;
    } else {
      profile_id_ = extras.getInt(DbHelper.PROF_ID_COL);
      name = extras.getString(DbHelper.PROF_NAME_COL);
      brightness = extras.getInt(DbHelper.PROF_VALUE_COL);

      nameBox.setText(name);
      brightnessBox.setText(Integer.toString(brightness));
    }

    okButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        String newName = nameBox.getText().toString();
        String brightnessText = brightnessBox.getText().toString();
        int newBrightness = brightness;
        try {
          newBrightness = Integer.parseInt(brightnessText);
        } catch (NumberFormatException e) {
          newBrightness = -1;
        }

        if (newName.length() == 0 || newBrightness < 0 || newBrightness > 100) {
          // TODO display some kind of error message?
          return;
        }

        // If nothing changed, act as though cancel was pressed.
        if (name.equals(newName) && brightness == newBrightness) {
          setResult(RESULT_CANCELED);
          finish();
        }
        // Bundle up the new values.
        Bundle bundle = new Bundle();
        bundle.putInt(DbHelper.PROF_ID_COL, profile_id_);
        bundle.putString(DbHelper.PROF_NAME_COL, newName);
        bundle.putInt(DbHelper.PROF_VALUE_COL, newBrightness);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
      }
    });
  }
}
