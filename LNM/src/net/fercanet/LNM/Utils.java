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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.ContextWrapper;




class Score implements Comparable<Score> {
    int score;
    String name;
    String date;

    public Score(int score, String name, String date) {
        this.score = score;
        this.name = name;
        this.date = date;
    }
    

    @Override
    public int compareTo(Score o) {
        return score < o.score ? -1 : score > o.score ? 1 : 0;
    }
}


public class Utils {
	

	// Is this score in the top scores?
    public static Boolean isInTheTopScores(Integer userscore, Context context) {
      
		List<Score> scores = new ArrayList<Score>();
        
        String filecontent = getStringFromFile("halloffame", context);              // This file is formated in this way: user1,score1;user2,score2;user3,score3; ...
        
        if (filecontent != "") {
	        String scoresarray[] = filecontent.split(";");                 // Split the string in "user,score" strings into an array
	        
	        for (int i = 0; i < scoresarray.length; i++){  	               // Loop Through the scorearray, split user and score pairs and add them to the list
	        	String scorearray[];
	        	scorearray = scoresarray[i].split(",");  
	        	scores.add(new Score(Integer.parseInt(scorearray[0]), scorearray[1], scorearray[2]));
	        }
        }
  
        Collections.sort(scores);
        ListIterator<Score> i = scores.listIterator(); 
        
        Preferences prefs = new Preferences(context); 
        
        if (i.hasNext() && scores.size()>=prefs.scoresnum) {
        	Score worstscore=(Score)i.next();
        	return (userscore >= worstscore.score) ? true : false;
        }
        else return true;    
    }
	    
    
    // Save the score list to halloffame file
    private static void saveListInFile(List<Score> scores, Context context){
    	
    	ListIterator<Score> i = scores.listIterator(scores.size());
      
        FileOutputStream fos = null;
        
        try {
 			fos = context.openFileOutput("halloffame", Context.MODE_PRIVATE);
 			
 		} catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 		}
         
 		int entries = 1;
 		
 		Preferences prefs = new Preferences(context); 
 		
        while(i.hasPrevious() && entries<=prefs.scoresnum)
        {	
        	Score entry=(Score)i.previous();

         	String name = String.valueOf(entry.score) + "," + entry.name + "," + entry.date + ";";
 			
 			try {
 				
 				fos.write(name.getBytes());
 				
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			entries++;			
        }
        try {
        	
        	fos.close();
        	
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
    }
    
    
    // Save the new user score into the file maintaining the score order (inverse, top scores up)
    public static void saveUserScore(Score userscore, Context context) {
      
		List<Score> scores = new ArrayList<Score>();
        
        String filecontent = getStringFromFile("halloffame", context);      // This file is formated in this way: user1,score1;user2,score2;user3,score3; ...
        
        if (filecontent != "") {
	        String scoresarray[] = filecontent.split(";");             // Split the string in "user,score" strings into an array
	        
	        for (int i=0; i<scoresarray.length; i++){  	               // Loop Through the scorearray, split user and score pairs and add them to the list
	        	String scorearray[];
	        	scorearray = scoresarray[i].split(",");  
	        	scores.add(new Score(Integer.parseInt(scorearray[0]), scorearray[1], scorearray[2]));
	        }
        }
        scores.add(userscore);                                     // Add the new score into the list
        
        Collections.sort(scores);
        
        saveListInFile(scores, context);    
    }
    
    public static void reloadScores(Context context) {
        
		List<Score> scores = new ArrayList<Score>();
        
        String filecontent = getStringFromFile("halloffame", context);      // This file is formated in this way: user1,score1;user2,score2;user3,score3; ...
        
        if (filecontent != "") {
	        String scoresarray[] = filecontent.split(";");             // Split the string in "user,score" strings into an array
	        
	        for (int i=0; i<scoresarray.length; i++){  	               // Loop Through the scorearray, split user and score pairs and add them to the list
	        	String scorearray[];
	        	scorearray = scoresarray[i].split(",");  
	        	scores.add(new Score(Integer.parseInt(scorearray[0]), scorearray[1], scorearray[2]));
	        }
        }
        
        Collections.sort(scores);
        
        saveListInFile(scores, context);    
    }

    // Returns the file content in a string
    public static String getStringFromFile(String file, Context context){
    	FileInputStream fis;
        int ch;
        StringBuffer strContent = new StringBuffer("");
		try {
			fis = context.openFileInput(file);
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
    
}
