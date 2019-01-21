/*
 * Copyright (C) 2012  Ciaran Gultnieks, ciaran@ciarang.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.ciarang.tallyphant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class ItemEdit extends Activity {

    private DB db;

    // The name of the item we're editing, or null if we created a new one.
    private String mItemName;
    // The item we're editing.
    private DB.Item mItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        db = new DB(this);

        setContentView(R.layout.itemedit);
        setTitle(R.string.edititem_title);

        Intent i = getIntent();
        if (!i.hasExtra("itemname")) {
            mItemName = null;
            mItem = db.new Item();
            mItem.count = 0;
            mItem.name = "New Item";
            mItem.labelstyle = DB.LabelStyle.Item;
            mItem.buttonstyle = DB.ButtonStyle.MinusPlus;
        } else {
            mItemName = i.getStringExtra("itemname");
            mItem = db.getItem(mItemName);
        }

        // Find the views...
        final TextView tvname = (TextView) findViewById(R.id.editName);
        final TextView tvcount = (TextView) findViewById(R.id.editCount);
        final TextView tvpname = (TextView) findViewById(R.id.editPlural);
        final Spinner labelstyle = (Spinner) findViewById(R.id.spinnerLabelStyle);
        final Spinner buttonstyle = (Spinner) findViewById(R.id.spinnerButtonStyle);
        final View pluralrow = findViewById(R.id.pluralRow);

        // Populate all the fields...
        tvname.setText(mItem.name);
        tvcount.setText(Integer.toString(mItem.count));
        tvpname.setText(mItem.pname);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this, R.array.labelstyles,
                        android.R.layout.simple_spinner_item);
        adapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        labelstyle.setAdapter(adapter);
        labelstyle.setSelection(mItem.labelstyle.ordinal());
        adapter = ArrayAdapter.createFromResource(this, R.array.buttonstyles,
                android.R.layout.simple_spinner_item);
        adapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        buttonstyle.setAdapter(adapter);
        buttonstyle.setSelection(mItem.buttonstyle.ordinal());

        // Hide the plural field unless it's being used...
        if (mItem.labelstyle != DB.LabelStyle.Items)
            pluralrow.setVisibility(View.GONE);
        labelstyle.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                Spinner spinner = (Spinner) arg0;
                if (DB.LabelStyle.values()[spinner.getSelectedItemPosition()] == DB.LabelStyle.Items) {
                    pluralrow.setVisibility(View.VISIBLE);
                    if(tvpname.getText().length()==0)
                        tvpname.setText(tvname.getText());
                } else {
                    pluralrow.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // Shouldn't happen should it?
            }

        });

        Button okbutton = (Button) findViewById(R.id.okbutton);
        okbutton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String newname = tvname.getText().toString();
                    String pname = tvpname.getText().toString();
                    String newcount = tvcount.getText().toString();
                    // Allow leaving the box empty as a shortcut for
                    // 0...
                    if (newcount.length() == 0)
                        newcount = "0";
                    db.updateItem(mItemName, newname, Integer
                            .parseInt(newcount), pname,
                            DB.ButtonStyle.values()[buttonstyle
                                    .getSelectedItemPosition()], DB.LabelStyle
                                    .values()[labelstyle
                                    .getSelectedItemPosition()]);
                    setResult(RESULT_OK);
                    finish();
                } catch (NumberFormatException e) {
                    // The input field type will hopefully prevent
                    // most
                    // invalid input, but even then you can still
                    // enter a
                    // value too large to fit in an integer...
                    Toast.makeText(ItemEdit.this, R.string.validnumber,
                            Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(ItemEdit.this, e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        Button cancelbutton = (Button) findViewById(R.id.cancelbutton);
        cancelbutton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        Button deletebutton = (Button) findViewById(R.id.deletebutton);
        if (mItem.name == null) {
            deletebutton.setVisibility(View.GONE);
        } else {
            deletebutton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Might want an 'are you sure?' here!
                    if (mItem.name != null)
                        db.deleteItem(mItem.name);
                    setResult(RESULT_OK);
                    finish();
                }
            });
        }

    }

    @Override
    protected void onStop() {
        db.close();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
