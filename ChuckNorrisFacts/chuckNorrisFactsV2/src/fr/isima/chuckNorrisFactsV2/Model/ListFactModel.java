package fr.isima.chuckNorrisFactsV2.Model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;
import java.util.Random;

import android.content.Context;
import android.util.Log;
import fr.isima.chuckNorrisFactsV2.FactAdapter;
import fr.isima.chuckNorrisFactsV2.R;
import fr.isima.chuckNorrisFactsV2.Utils.CSVParser;
import fr.isima.chuckNorrisFactsV2.Utils.FileParser;
import fr.isima.chuckNorrisFactsV2.Utils.SortableFactList;
import fr.isima.chuckNorrisFactsV2.entities.Fact;
import fr.isima.chuckNorrisFactsV2.exceptions.GarbageCollectedException;

public class ListFactModel extends Observable {
	private SortableFactList mListFacts;
	private static volatile ListFactModel instance = null;
	FactAdapter mAdapter;
	Fact factToPass;

	public final static ListFactModel getInstance() {
		if (ListFactModel.instance == null) {
			synchronized (ListFactModel.class) {
				if (ListFactModel.instance == null) {
					ListFactModel.instance = new ListFactModel();
				}
			}
		}
		return ListFactModel.instance;
	}

	private ListFactModel() {
		super();
		this.mListFacts = new SortableFactList();
	}

	public Fact getRandomFact() throws GarbageCollectedException {
		if (mListFacts == null)
			throw new GarbageCollectedException();
		int rand = new Random().nextInt(mListFacts.size());

		return mListFacts.get(rand);
	}

	public SortableFactList getListFacts() {
		return mListFacts;
	}

	public void setListFacts(SortableFactList listFacts) {
		this.mListFacts = listFacts;

		setChanged();
		notifyObservers();
	}

	public void retrieveData(Context context) {

		InputStream is = context.getResources().openRawResource(
				R.raw.chucknorris);

		try {
			FileParser parser = new CSVParser();
			setListFacts(parser.parseFileIntoList(is));
		} catch (IOException e) {
			Log.e(this.getClass().toString(),
					"Error while reading file :" + e.getMessage());
		}
	}

	public void retrieveDataById(String research, Context context)
			throws GarbageCollectedException {
		if (mListFacts == null)
			throw new GarbageCollectedException();
		SortableFactList ret = new SortableFactList();

		for (Fact fact : mListFacts) {
			if (Integer.toString(fact.getId()).contains(research))
				ret.add(fact);
		}

		setListFacts(ret);
	}

	public Fact getNextFact(Fact currentFact) throws GarbageCollectedException {
		if (mListFacts == null)
			throw new GarbageCollectedException();

		Fact ret = null;

		int index = mListFacts.indexOf(currentFact);

		if (index >= 0) {
			++index;
			if (index == mListFacts.size())
				index = 0;
			ret = mListFacts.get(index);
		}
		return ret;
	}

	public Fact getPreviousFact(Fact currentFact) throws GarbageCollectedException {
		if (mListFacts == null)
			throw new GarbageCollectedException();

		Fact ret = null;

		int index = mListFacts.indexOf(currentFact);

		if (index >= 0) {
			--index;
			if (index < 0)
				index = mListFacts.size() - 1;
			ret = mListFacts.get(index);
		}
		return ret;
	}

	public Fact getFactToPass() {
		return factToPass;
	}

	public void setFactToPass(Fact factToPass) {
		this.factToPass = factToPass;
	}

	public void setToNull() {
		this.mListFacts = null;
	}
}
