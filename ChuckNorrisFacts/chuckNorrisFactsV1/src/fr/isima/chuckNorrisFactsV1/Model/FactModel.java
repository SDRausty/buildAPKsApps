package fr.isima.chuckNorrisFactsV1.Model;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Random;

import android.content.Context;
import android.util.Log;
import fr.isima.chuckNorrisFactsV1.R;
import fr.isima.chuckNorrisFactsV1.Utils.CSVParser;
import fr.isima.chuckNorrisFactsV1.Utils.FileParser;
import fr.isima.chuckNorrisFactsV1.entities.Fact;

public class FactModel extends Observable{
	private Map<Integer, Fact> mListFacts;

	public FactModel() {
		super();
		this.mListFacts = new HashMap<Integer, Fact>();
	}

	public Map<Integer, Fact> getListFacts() {
		return mListFacts;
	}

	public void setListFacts(Map<Integer, Fact> mListFacts) {
		this.mListFacts = mListFacts;
		
		setChanged();
		notifyObservers();
	}

	public Fact getRandomFact() {
		int rand = new Random().nextInt(mListFacts.size());

		int i = 0;
		for (Fact f : mListFacts.values()) {
			if (i == rand)
				return f;
			++i;
		}

		return null;
	}

	public void retrieveData(Context context) {

		InputStream is = context.getResources().openRawResource(
				R.raw.chucknorris);

		try {
			FileParser parser = new CSVParser();
			setListFacts(parser.parseFile(is));
		} catch (IOException e) {
			Log.e(this.getClass().toString(),
					"Error while reading file :" + e.getMessage());
		}
	}

	public Fact getFactById(int id) {
		Fact ret = null;

		if (mListFacts.containsKey(id)) {
			ret = mListFacts.get(id);
		}
		return ret;
	}

	public void setToNull() {
		this.mListFacts.clear();
		this.mListFacts = null;
	}

}
