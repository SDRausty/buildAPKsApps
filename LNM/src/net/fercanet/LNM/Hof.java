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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;



public class Hof extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hof); 
        Button bt = (Button) findViewById(R.id.exithof);
        bt.setOnClickListener(ClickListener);
        loadHofTable(); 
    }
    
    
    // Returns the file content in a string
    public String getStringFromFile(String file){
    	FileInputStream fis;
        int ch;
        StringBuffer strContent = new StringBuffer("");
		try {
			fis = openFileInput(file);
			 while((ch = fis.read()) != -1)
			        strContent.append((char)ch);
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return strContent.toString();
    }
    
    
    // Draws Hall of fame table with a row for every score saved in halloffame file
    public void loadHofTable(){
		
    	TableLayout tl = (TableLayout)findViewById(R.id.hoftable);       
       
		String filecontent = getStringFromFile("halloffame");        
       
        if (filecontent != "") {
        	
	        String scores[] = filecontent.split(";");
	    	
	        for (int i=0; i<scores.length; i++){
	            
	        	String score[];
	        	score = scores[i].split(",");
	        	
	        	TableRow tr = new TableRow(this);
	            tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
	
	            TextView points = new TextView(this);
	            points.setGravity(Gravity.CENTER_VERTICAL);
	            points.setGravity(Gravity.CENTER_HORIZONTAL);
	            points.setTextColor(Color.BLACK);
	            points.setText(score[0]);
	            points.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
	            tr.addView(points);
	           
	            TextView playername = new TextView(this);
	            playername.setGravity(Gravity.CENTER_VERTICAL);
	            playername.setGravity(Gravity.CENTER_HORIZONTAL);
	            playername.setTextColor(Color.BLACK);
	            playername.setText(score[1]);
	            playername.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
	            tr.addView(playername);
	            
	            TextView dat = new TextView(this);
	            dat.setGravity(Gravity.CENTER_VERTICAL);
	            dat.setGravity(Gravity.CENTER_HORIZONTAL);
	            dat.setTextColor(Color.BLACK);
	            dat.setText(score[2]);
	            dat.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
	            tr.addView(dat);
	            
	            tl.addView(tr,new TableLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));		
	        }
        }  
    }
    
    
    // Click listener for the exit button
    OnClickListener ClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClassName("net.fercanet.LNM", "net.fercanet.LNM.MainMenu");
			startActivity(intent);
		}
		
    };
	
	
}