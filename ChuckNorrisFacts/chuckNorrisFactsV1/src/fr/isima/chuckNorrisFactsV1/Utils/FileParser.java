package fr.isima.chuckNorrisFactsV1.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import fr.isima.chuckNorrisFactsV1.entities.Fact;

public interface FileParser {
	
	public Map<Integer, Fact> parseFile(InputStream is) throws IOException;

}
