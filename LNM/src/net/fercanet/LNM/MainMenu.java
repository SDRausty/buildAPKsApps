//   -----------------------------------------------------------------------------
//    Copyright 2010 Ferran Caellas Puig

//    This file is part of Learn Music Notes.
//
//    Learn Music Notes is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.

//    Learn Music Notes is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.

//    You should have received a copy of the GNU General Public License
//    along with Learn Music Notes.  If not, see <http://www.gnu.org/licenses/>.
//   -----------------------------------------------------------------------------


package net.fercanet.LNM;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;





public class MainMenu extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button mintest = (Button) findViewById(R.id.mintest);
        mintest.setOnClickListener(ClickListener);
        Button training = (Button) findViewById(R.id.training);
        training.setOnClickListener(ClickListener);
        Button hof = (Button) findViewById(R.id.hof);
        hof.setOnClickListener(ClickListener);
        Button endgame = (Button) findViewById(R.id.endgame);
        endgame.setOnClickListener(ClickListener);
    }
    
    
    // Click listener for the four menu buttons
    OnClickListener ClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			switch (v.getId()) {
			case R.id.mintest:
		    	intent.setClassName("net.fercanet.LNM", "net.fercanet.LNM.Game");
		    	intent.putExtra("omt", true);
		    	startActivity(intent);
			break;
			case R.id.training:	
		    	intent.setClassName("net.fercanet.LNM", "net.fercanet.LNM.Game");
		    	intent.putExtra("omt", false);
		    	startActivity(intent);
			break;
			case R.id.hof:	
		    	intent.setClassName("net.fercanet.LNM", "net.fercanet.LNM.Hof");
		    	startActivity(intent);

	    	break;
			case R.id.endgame:	
		    	moveTaskToBack(true);

	    	break;
			}
		}
    };	

    
    // when the phone menu item is pressed show game_menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
        return true;
    }
    
    
    // handles the events for all the options in game_menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent intent = new Intent();
        switch (item.getItemId()) {
        case R.id.menu_1mintest:
        	intent.setClassName("net.fercanet.LNM", "net.fercanet.LNM.Game");
        	intent.putExtra("omt", true);
        	startActivity(intent);
            return true;
        case R.id.menu_training:
        	intent.setClassName("net.fercanet.LNM", "net.fercanet.LNM.Game");
        	intent.putExtra("omt", false);
        	startActivity(intent);
            return true;
        case R.id.menu_hof:	
        	intent.setClassName("net.fercanet.LNM", "net.fercanet.LNM.Hof");
        	startActivity(intent);
        	return true;
        case R.id.menu_settings:	
        	intent.setClassName("net.fercanet.LNM", "net.fercanet.LNM.Settings");
        	startActivity(intent);
        	return true;
        case R.id.menu_Quit:	
        	moveTaskToBack(true);
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    

    
    
}
