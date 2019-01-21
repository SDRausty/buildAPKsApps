package fr.isima.chuckNorrisFactsV1.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import fr.isima.chuckNorrisFactsV1.entities.Fact;

public class CSVParser implements FileParser{

	

	public Map<Integer, Fact> parseFile(InputStream is) throws IOException {
		Map<Integer, Fact> ret = new HashMap<Integer, Fact>();

		BufferedReader buff = new BufferedReader(new InputStreamReader(is));

		String line;
		while ((line = buff.readLine()) != null) {
			String[] split = line.split(";");
			int id;
			try {
				id = Integer.parseInt(split[0]);
			} catch (NumberFormatException e) {
				id = -1;
			}
			// listFacts.add(new Fact(id, split[1], split[2]));
			ret.put(id, new Fact(id, split[1], split[2]));
		}

		return ret;
	}

}
