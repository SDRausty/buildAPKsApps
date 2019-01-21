package fr.isima.chuckNorrisFactsV2.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import fr.isima.chuckNorrisFactsV2.entities.Fact;

public interface FileParser {
	
	public Map<Integer, Fact> parseFileIntoMap(InputStream is) throws IOException;
	public SortableFactList parseFileIntoList(InputStream is) throws IOException;

}
