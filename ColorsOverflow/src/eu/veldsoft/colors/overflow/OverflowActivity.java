/*==============================================================================
 =                                                                             =
 = Overflow is very simple but very addictive board game. The game is for two  =
 = players who try to conquer all stones of the opposite player. The game was  =
 = developed as master thesis in New Bulgarian University, Sofia, Bulgaria.    =
 =                                                                             =
 = Copyright (C) 2012 by Yuriy Stanchev  ( i_stanchev@ml1.net )                =
 =                                                                             =
 = This program is free software: you can redistribute it and/or modify        =
 = it under the terms of the GNU General Public License as published by        =
 = the Free Software Foundation, either version 3 of the License, or           =
 = (at your option) any later version.                                         =
 =                                                                             =
 = This program is distributed in the hope that it will be useful,             =
 = but WITHOUT ANY WARRANTY; without even the implied warranty of              =
 = MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               =
 = GNU General Public License for more details.                                =
 =                                                                             =
 = You should have received a copy of the GNU General Public License           =
 = along with this program. If not, see <http://www.gnu.org/licenses/>.        =
 =                                                                             =
 =============================================================================*/

package eu.veldsoft.colors.overflow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * This class is used to start a new game.
 * 
 * @author Yuriy Stanchev
 * 
 * @email i_stanchev@ml1.net
 * 
 * @date 11 Mar 2012
 */
public class OverflowActivity extends Activity {

	/**
	 * Called when the activity is first created.
	 * 
	 * @param bundle
	 *            is used to save the state of the created Activity.
	 * 
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		Board board = (Board) extras.get("board");
		GameView view = new GameView(this, board);
		setContentView(view);
	}
}
