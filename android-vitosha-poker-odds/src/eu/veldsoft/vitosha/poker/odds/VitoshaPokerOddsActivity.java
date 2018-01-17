/*******************************************************************************
 *                                                                             *
 * VitoshaPokerOdds is Texas hold'em odds calculator written in Bulgaria.      *
 *                                                                             *
 * Copyright (C) 2009-2012 by Todor Balabanov ( tdb@tbsoft.eu )                *
 * Technological School of Electronic Systems                                  *
 * Technical University of Sofia                                               *
 * Sofia, Bulgaria                                                             *
 *                                                                             *
 * This program is free software: you can redistribute it and/or modify        *
 * it under the terms of the GNU General Public License as published by        *
 * the Free Software Foundation, either version 3 of the License, or           *
 * (at your option) any later version.                                         *
 *                                                                             *
 * This program is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
 * GNU General Public License for more details.                                *
 *                                                                             *
 * You should have received a copy of the GNU General Public License           *
 * along with this program. If not, see <http://www.gnu.org/licenses/>.        *
 *                                                                             *
 ******************************************************************************/

package eu.veldsoft.vitosha.poker.odds;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import eu.veldsoft.vitosha.poker.odds.model.MonteCarlo;

/**
 * Main window interface.
 * 
 * @author Todor Balabanov
 * 
 * @email tdb@tbsoft.eu
 * 
 * @date 09 Aug 2012
 */
public class VitoshaPokerOddsActivity extends Activity {

	/**
	 * Handler that is being associated to the main thread.
	 */
	private Handler calculatorHandler = null;

	/**
	 * Handler that is being associated to the main thread.
	 */
	private Handler displayrHandler = null;

	/**
	 * Monte-Carlo calculator.
	 */
	private MonteCarlo calculator = null;

	/**
	 * 
	 * @author Todor Balabanov
	 * 
	 * @email tdb@tbsoft.eu
	 * 
	 * @date 13 Aug 2012
	 */
	private void storeGuiInPreferences() {
		Spinner spinner = null;
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();

		spinner = (Spinner) findViewById(R.id.hand1);
		editor.putString("hand1", spinner.getSelectedItem().toString());

		spinner = (Spinner) findViewById(R.id.hand2);
		editor.putString("hand2", spinner.getSelectedItem().toString());

		spinner = (Spinner) findViewById(R.id.flop1);
		editor.putString("flop1", spinner.getSelectedItem().toString());

		spinner = (Spinner) findViewById(R.id.flop2);
		editor.putString("flop2", spinner.getSelectedItem().toString());

		spinner = (Spinner) findViewById(R.id.flop3);
		editor.putString("flop3", spinner.getSelectedItem().toString());

		spinner = (Spinner) findViewById(R.id.turn);
		editor.putString("turn", spinner.getSelectedItem().toString());

		spinner = (Spinner) findViewById(R.id.river);
		editor.putString("river", spinner.getSelectedItem().toString());

		spinner = (Spinner) findViewById(R.id.iterations);
		editor.putString("iterations", spinner.getSelectedItem().toString());

		spinner = (Spinner) findViewById(R.id.players);
		editor.putString("players", spinner.getSelectedItem().toString());

		editor.commit();
	}

	/**
	 * 
	 * @author Todor Balabanov
	 * 
	 * @email tdb@tbsoft.eu
	 * 
	 * @date 13 Aug 2012
	 */
	private void doSimulationOnBackground() {
		new Thread(new Runnable() {
			private double chance = 0.0;

			public void run() {
				chance = calculator.start();

				calculatorHandler.postDelayed(new Runnable() {
					public void run() {
						((TextView) findViewById(R.id.chance)).setText("" + chance);
					}
				}, 0);
			}
		}).start();
	}

	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 * 
	 * @author Todor Balabanov
	 * 
	 * @email tdb@tbsoft.eu
	 * 
	 * @date 09 Aug 2012
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		calculatorHandler = new Handler();

		displayrHandler = new Handler();
		new Thread(new Runnable() {
			public void run() {
				final int SLEEP_FOR_MS = 500;

				while (true) {
					if (calculator != null) {
						displayrHandler.postDelayed(new Runnable() {
							public void run() {
								double chance = calculator.willWinIn();

								((TextView) findViewById(R.id.chance)).setText("" + chance);
							}
						}, 0);
					}

					try {
						Thread.sleep(SLEEP_FOR_MS);
					} catch (InterruptedException exception) {
					}
				}
			}
		}).start();

		Spinner spinner = null;
		ArrayAdapter<CharSequence> adapter = null;
		SharedPreferences settings = getPreferences(MODE_PRIVATE);

		spinner = (Spinner) findViewById(R.id.hand1);
		adapter = ArrayAdapter.createFromResource(this, R.array.cards_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(adapter.getPosition(settings.getString("hand1", getString(R.string.default_card_value))));

		spinner = (Spinner) findViewById(R.id.hand2);
		adapter = ArrayAdapter.createFromResource(this, R.array.cards_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(adapter.getPosition(settings.getString("hand2", getString(R.string.default_card_value))));

		spinner = (Spinner) findViewById(R.id.flop1);
		adapter = ArrayAdapter.createFromResource(this, R.array.cards_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(adapter.getPosition(settings.getString("flop1", getString(R.string.default_card_value))));

		spinner = (Spinner) findViewById(R.id.flop2);
		adapter = ArrayAdapter.createFromResource(this, R.array.cards_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(adapter.getPosition(settings.getString("flop2", getString(R.string.default_card_value))));

		spinner = (Spinner) findViewById(R.id.flop3);
		adapter = ArrayAdapter.createFromResource(this, R.array.cards_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(adapter.getPosition(settings.getString("flop3", getString(R.string.default_card_value))));

		spinner = (Spinner) findViewById(R.id.turn);
		adapter = ArrayAdapter.createFromResource(this, R.array.cards_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(adapter.getPosition(settings.getString("turn", getString(R.string.default_card_value))));

		spinner = (Spinner) findViewById(R.id.river);
		adapter = ArrayAdapter.createFromResource(this, R.array.cards_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(adapter.getPosition(settings.getString("river", getString(R.string.default_card_value))));

		spinner = (Spinner) findViewById(R.id.iterations);
		adapter = ArrayAdapter.createFromResource(this, R.array.iterations_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(
				adapter.getPosition(settings.getString("iterations", getString(R.string.default_iterations_value))));

		spinner = (Spinner) findViewById(R.id.players);
		adapter = ArrayAdapter.createFromResource(this, R.array.number_of_players_array,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(adapter
				.getPosition(settings.getString("players", getString(R.string.default_number_of_players_value))));

		((Button) findViewById(R.id.clear)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				((Spinner) findViewById(R.id.hand1)).setSelection(0);
				((Spinner) findViewById(R.id.hand2)).setSelection(0);
				((Spinner) findViewById(R.id.flop1)).setSelection(0);
				((Spinner) findViewById(R.id.flop2)).setSelection(0);
				((Spinner) findViewById(R.id.flop3)).setSelection(0);
				((Spinner) findViewById(R.id.turn)).setSelection(0);
				((Spinner) findViewById(R.id.river)).setSelection(0);
				((Spinner) findViewById(R.id.iterations)).setSelection(0);
				((Spinner) findViewById(R.id.players)).setSelection(0);
				((TextView) findViewById(R.id.chance)).setText("");
			}
		});

		((Button) findViewById(R.id.stop)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (calculator != null) {
					calculator.stop();

					((TextView) findViewById(R.id.chance)).setText("");

					storeGuiInPreferences();
				}
			}
		});

		((Button) findViewById(R.id.calculate)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				((TextView) findViewById(R.id.chance)).setText("");

				String hand1 = "" + ((Spinner) findViewById(R.id.hand1)).getSelectedItem();
				String hand2 = "" + ((Spinner) findViewById(R.id.hand2)).getSelectedItem();
				String flop1 = "" + ((Spinner) findViewById(R.id.flop1)).getSelectedItem();
				String flop2 = "" + ((Spinner) findViewById(R.id.flop2)).getSelectedItem();
				String flop3 = "" + ((Spinner) findViewById(R.id.flop3)).getSelectedItem();
				String turn = "" + ((Spinner) findViewById(R.id.turn)).getSelectedItem();
				String river = "" + ((Spinner) findViewById(R.id.river)).getSelectedItem();
				String players = "" + ((Spinner) findViewById(R.id.players)).getSelectedItem();
				String iterations = "" + ((Spinner) findViewById(R.id.iterations)).getSelectedItem();

				/*
				 * Player always have two know cards.
				 */
				if (hand1.equals(getString(R.string.default_card_value)) == true
						|| hand2.equals(getString(R.string.default_card_value)) == true) {
					Toast.makeText(getApplicationContext(), R.string.player_s_cards_needed_toast, Toast.LENGTH_SHORT)
							.show();
					return;
				}

				/*
				 * Flop always has three cards.
				 */
				if ((flop1.equals(getString(R.string.default_card_value)) == true
						|| flop2.equals(getString(R.string.default_card_value)) == true
						|| flop3.equals(getString(R.string.default_card_value)) == true)
						&& (flop1.equals(getString(R.string.default_card_value)) == false
								|| flop2.equals(getString(R.string.default_card_value)) == false
								|| flop3.equals(getString(R.string.default_card_value)) == false)) {
					Toast.makeText(getApplicationContext(), R.string.flop_has_3_cards_toast, Toast.LENGTH_SHORT).show();
					return;
				}

				/*
				 * Turn should be known before river.
				 */
				if (turn.equals(getString(R.string.default_card_value)) == true
						&& river.equals(getString(R.string.default_card_value)) == false) {
					Toast.makeText(getApplicationContext(), R.string.do_not_miss_turn_before_river_toast,
							Toast.LENGTH_SHORT).show();
					return;
				}

				/*
				 * Player's card, flop, turn and river should be unique cards.
				 */
				String[] known = { hand1, hand2, flop1, flop2, flop3, turn, river };
				for (int i = 0; i < known.length; i++) {
					for (int j = i + 1; j < known.length; j++) {
						if (known[i].equals(getString(R.string.default_card_value)) == false
								&& known[i].equals(known[j]) == true) {
							Toast.makeText(getApplicationContext(), R.string.cards_are_not_unique_tast,
									Toast.LENGTH_SHORT).show();
							return;
						}
					}
				}

				long numberOfLoops = new Long(iterations);
				int numberOfPlayers = new Integer(players);
				String[] faces = getResources().getStringArray(R.array.cards_array);
				String[] codes = getResources().getStringArray(R.array.card_codes_array);
				String knownCards = "";

				/*
				 * Assemble known cards from interface and resource codes.
				 */
				for (int i = 0; i < known.length; i++) {
					if (known[i].equals(getString(R.string.default_card_value)) == true) {
						break;
					}

					for (int j = 0; j < faces.length; j++) {
						if (faces[j].equals(known[i]) == true) {
							knownCards += codes[j];
							break;
						}
					}
				}

				calculator = new MonteCarlo(knownCards, numberOfLoops, numberOfPlayers);
				doSimulationOnBackground();
			}
		});

		/*
		 * Add About information button and Exit Button.
		 * 
		 * @author Yuriy Stanchev
		 * 
		 * @date 23 Aug 2012
		 */

		((Button) findViewById(R.id.bExit)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});

		((Button) findViewById(R.id.bAbout)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent about = new Intent(getApplicationContext(), AboutActivity.class);
				String title = "About Vitosha Poker Odds";
				String file = "file:///android_asset/about.html";
				about.putExtra("title", title);
				about.putExtra("file", file);
				startActivity(about);
			}
		});

		/*
		 * End of adding About Information and Exit Button.
		 */
	}

	/**
	 * 
	 * @author Todor Balabanov
	 * 
	 * @email tdb@tbsoft.eu
	 * 
	 * @date 09 Aug 2012
	 */
	@Override
	protected void onStop() {
		super.onStop();

		storeGuiInPreferences();
	}
}
