package fr.isima.chuckNorrisFactsV2.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import fr.isima.chuckNorrisFactsV2.entities.Fact;

public class CSVParser implements FileParser {

	public Map<Integer, Fact> parseFileIntoMap(InputStream is)
			throws IOException {
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
			ret.put(id, new Fact(id, split[1], split[2]));
		}

		return ret;
	}

	public SortableFactList parseFileIntoList(InputStream is)
			throws IOException {
		SortableFactList ret = new SortableFactList();

		BufferedReader buff = new BufferedReader(new InputStreamReader(is));

		String line;
		while ((line = buff.readLine()) != null) {
			String[] split = line.split(";");
			int id = -1;
			String fact = "Unknown";
			String category = "Unknown";
			
			
			try {
				id = Integer.parseInt(split[0]);
			} catch (NumberFormatException e) {
				id = -1;
			}

			try {
				fact = split[1];
			} catch (ArrayIndexOutOfBoundsException e2) {
				fact = "Unknown";
			}

			try {
				category = split[2];
			} catch (ArrayIndexOutOfBoundsException e2) {
				category = "Unknown";
			}
			
			
			ret.add(new Fact(id, fact, category));
		}
		ret.sortById();
		return ret;
	}

}
