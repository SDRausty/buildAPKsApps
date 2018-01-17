// GameMaster Dice
// Copyright (C) 2014 David Pflug
// Copyright (C) 2011-2014 Georg Lukas
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

package de.duenndns.gmdice;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.*;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class GameMasterDice extends ListActivity
		implements OnClickListener, OnLongClickListener
{
	private static String TAG = "GameMasterDice";

	// map button IDs to dice
	int button_ids[] = { R.id.die0, R.id.die1, R.id.die2, R.id.die3 };
	Button buttons[];
	Button button_more;
	int button_colors[] = { 0xfff0b0f0, 0xffc0c0f0, 0xffc0f0c0, 0xfff0c0c0, 0xffb0f0f0 };
	TextView resultview;
	RollResultAdapter resultlog;
	SharedPreferences prefs;

	DiceSet button_cfg[] = {
		DiceSet.getDiceSet(DiceSet.DSA),
		DiceSet.getDiceSet(1, 20, 0),
		DiceSet.getDiceSet(1, 6, 0),
		DiceSet.getDiceSet(1, 6, 4)
	};
	DiceCache dicecache = new DiceCache(10);
	Random generator = new Random();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_gmdice);
		setTitle(R.string.app_name_long);


		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		buttons = new Button[button_ids.length];
		for (int i = 0; i < button_ids.length; i++) {
			buttons[i] = (Button)findViewById(button_ids[i]);
			buttons[i].setOnClickListener(this);
			buttons[i].setOnLongClickListener(this);
			buttons[i].getBackground().setColorFilter(button_colors[i], PorterDuff.Mode.MULTIPLY);
		}
		button_more = (Button)findViewById(R.id.more);
		button_more.setOnClickListener(this);
		button_more.getBackground().setColorFilter(button_colors[4], PorterDuff.Mode.MULTIPLY);
		resultview = (TextView)findViewById(R.id.rollresult);
		resultlog = new RollResultAdapter(this);
		setListAdapter(resultlog);


		if (savedInstanceState != null) {
			getListView().onRestoreInstanceState(savedInstanceState.getParcelable("resultlog"));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadDicePrefs();
		for (int i = 0; i < button_ids.length; i++)
			buttons[i].setText(button_cfg[i].toString());
		configKeepScreenOn();
	}

	@Override
	protected void onPause() {
		super.onPause();
		storeDicePrefs();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelable("resultlog", getListView().onSaveInstanceState());
		super.onSaveInstanceState(savedInstanceState);
	}

	// Options menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_context, menu);
		return true;
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.screen_on);
		if (item == null)
			return false;
		item.setIcon(prefs.getBoolean("keepscreen", false) ?
			android.R.drawable.button_onoff_indicator_on :
			android.R.drawable.button_onoff_indicator_off);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.screen_on:
			toggleKeepScreenOn();
			return true;
		case R.id.clear_log:
			resultlog.clear();
			resultview.setText(R.string.roll_placeholder);
			return true;
		case R.id.about:
			aboutDialog();
			return true;
		}
		return false;
	}
	void configKeepScreenOn() {
		if (prefs.getBoolean("keepscreen", false)) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}
	void toggleKeepScreenOn() {
		boolean new_state = !prefs.getBoolean("keepscreen", false);
		prefs.edit().putBoolean("keepscreen", new_state).commit();
		configKeepScreenOn();
	}
	void aboutDialog() {
		String versionTitle = getString(R.string.app_name_long);
		try {
			PackageInfo pi = getPackageManager()
						.getPackageInfo(getPackageName(), 0);
			versionTitle += " " + pi.versionName;
		} catch (NameNotFoundException e) {
		}
		String about = getString(R.string.about_text) + getString(R.string.about_gpl);
		new AlertDialog.Builder(this)
			.setTitle(versionTitle)
			.setIcon(android.R.drawable.ic_dialog_info)
			.setMessage(about)
			.setPositiveButton(android.R.string.ok, null)
			.setNeutralButton(R.string.about_home, 
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(Intent.ACTION_VIEW,
									Uri.parse("https://github.com/ge0rg/gamemasterdice/wiki")));
						}
					})
			.create().show();
	}

	// preferences
	void loadDicePrefs() {
		String btn_str = prefs.getString("buttons", null);
		if (btn_str == null)
			return;
		String[] btn_dice = btn_str.split("\\|");
		for (int i = 0; i < btn_dice.length; i++) {
			Log.d(TAG, "load: " + btn_dice[i]);
			button_cfg[i] = DiceSet.getDiceSet(btn_dice[i]);
		}
		dicecache.loadFromString(prefs.getString("cache", null));
	}
	void storeDicePrefs() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < button_cfg.length; i++) {
			sb.append(button_cfg[i]);
			if (i < button_cfg.length - 1)
			sb.append("|");
		}
		prefs.edit().putString("buttons", sb.toString())
			.putString("cache", dicecache.toString())
			.commit();
	}

	// OnClickListener callback for dice button clicks
	public void onClick(View view) {
		Button btn = (Button)view;
		if (btn == button_more) {
			selectDice(DiceSet.getDiceSet(), true, new OnDiceChange() {
				public void onDiceChange(DiceSet ds) {
					roll(ds, button_colors[4]);
				 }});
		} else {
			for (int i = 0; i < buttons.length; i++) {
				if (buttons[i] == btn) {
					String diceVal = btn.getText().toString();
					DiceSet ds = DiceSet.getDiceSet(diceVal);
					roll(ds, button_colors[i]);
				}
			}
		}
	}
	
	public void roll(DiceSet ds, int color) {
		String roll = ds.roll(this, generator);
		dicecache.add(ds);

		resultview.setText(roll);

		String rolllog = ds.toString() + ": " + roll;
		Log.d(TAG, "rolled: " + rolllog);
		resultlog.add(new RollResult(rolllog, color));
	}

	// OnLongClickListener callback for dice reconfiguration
	public boolean onLongClick(View view) {
		final Button btn = (Button)view;
		Log.d(TAG, "onLongClicked " + btn);
		String diceVal = btn.getText().toString();
		selectDice(DiceSet.getDiceSet(diceVal), false, new OnDiceChange() {
			public void onDiceChange(DiceSet ds) {
				btn.setText(ds.toString());
				// store button config
				for (int i = 0; i < buttons.length; i++) {
					if (btn == buttons[i])
						button_cfg[i] = ds;
				}
			}});
		return true;
	}

	static final Integer[] SPIN_COUNT = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	static final Integer[] SPIN_SIDES = { 2, 3, 4, 6, 8, 10, 12, 20, 30, 100 };
	static final Integer[] SPIN_MODIFIER = { -10, -9, -8, -7, -6, -5, -4, -3, -2, -1,
						0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

	NumberPicker setupNumPicker(View group, int r_id, int defVal) {
		NumberPicker sp = (NumberPicker)group.findViewById(r_id);
		sp.setCurrent(defVal);
		return sp;
	}

	Spinner setupSpinner(View group, int r_id, Integer[] values, int defVal) {
		Spinner sp = (Spinner)group.findViewById(r_id);
		ArrayAdapter adapter = new ArrayAdapter<Integer>(this, R.layout.spinner_item, values);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp.setAdapter(adapter);
		for (int i = 0; i < values.length; i++) {
			if (values[i] == defVal) {
				sp.setSelection(i);
			}
		}
		return sp;
	}

	// create a DiceSet by setting count, sides, modifier
	void configureDice(DiceSet defaults, final OnDiceChange onOk) {
		android.view.LayoutInflater inflater = (android.view.LayoutInflater)getSystemService(
			      LAYOUT_INFLATER_SERVICE);
		View group = inflater.inflate(R.layout.dg_configure, null, false);
		final NumberPicker np_c = setupNumPicker(group, R.id.spin_count, defaults.count);
		final Spinner sp_s = setupSpinner(group, R.id.spin_sides, SPIN_SIDES, defaults.sides);
		final NumberPicker np_m = setupNumPicker(group, R.id.spin_modifier, defaults.modifier);

		new AlertDialog.Builder(this)
			.setTitle(R.string.ds_config)
			.setView(group)
			.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						DiceSet ds = DiceSet.getDiceSet(np_c.getCurrent(),
							(Integer)sp_s.getSelectedItem(),
							(Integer)np_m.getCurrent());
						onOk.onDiceChange(ds);
					}
				})
			.setNegativeButton(android.R.string.cancel, null)

			.create().show();
	}

	// choose a DiceSet from the last-used list
	void selectDice(final DiceSet defaults, boolean hideBtns, final OnDiceChange onOk) {
		final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,
				android.R.layout.simple_spinner_dropdown_item);
		dicecache.populate(adapter, hideBtns ? java.util.Arrays.asList(button_cfg)
							: new ArrayList<DiceSet>());
		adapter.add(getString(R.string.ds_custom));
		new AlertDialog.Builder(this)
			.setTitle(R.string.ds_choose)
			.setAdapter(adapter, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String ds = adapter.getItem(which).toString();
						Log.d(TAG, "item clicked: " + which + " - " + ds);
						if (which == adapter.getCount() - 1)
							configureDice(defaults, onOk);
						else
							onOk.onDiceChange(DiceSet.getDiceSet(ds));
					}
				})
			.setNegativeButton(android.R.string.cancel, null)
			.create().show();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		resultlog.getItem(position).showDetails(this);
	}
}

abstract class OnDiceChange {
	abstract public void onDiceChange(DiceSet ds);
}

// this class contains all the data needed to store the results of a single dice roll
class RollResult {
	String result;
	int color;

	RollResult(String res, int col) {
		result = res;
		color = col;
	}

	public void setColor(int col) {
		color = col;
	}

	public void showDetails(Context ctx) {
		new AlertDialog.Builder(ctx)
			.setTitle(R.string.roll_result)
			.setMessage(result)
			.setPositiveButton(android.R.string.ok, null)
			.create().show();
	}

	@Override
	public String toString() {
		return result;
	}
}

class RollResultAdapter extends ArrayAdapter<RollResult> {

	RollResultAdapter(Context ctx) {
		super(ctx, R.layout.view_log);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View res = super.getView(position, convertView, parent);
		((TextView)res).setTextColor(getItem(position).color);
		return res;
	}
}
