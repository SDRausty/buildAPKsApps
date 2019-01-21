package fr.isima.chuckNorrisFactsV2.Utils;

import java.util.ArrayList;
import java.util.Collections;

import fr.isima.chuckNorrisFactsV2.entities.Fact;

public class SortableFactList extends ArrayList<Fact>{

	private static final long serialVersionUID = 6768490348298779860L;
	
	public void sortById(){
		Collections.sort(this);
	}

}
