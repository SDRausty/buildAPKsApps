package fr.isima.chuckNorrisFactsV2.Model;

import java.util.Observable;

import fr.isima.chuckNorrisFactsV2.entities.Fact;
import fr.isima.chuckNorrisFactsV2.exceptions.GarbageCollectedException;

public class FactModel extends Observable {
	private Fact mCurrentFact;
	private static volatile FactModel instance = null;
	
	private FactModel(){
		
	}
	
	public final static FactModel getInstance() {
		if (FactModel.instance == null) {
			synchronized (FactModel.class) {
				if (FactModel.instance == null) {
					FactModel.instance = new FactModel();
				}
			}
		}
		return FactModel.instance;
	}

	public Fact getCurrentFact() {
		return mCurrentFact;
	}

	public void setCurrentFact(Fact mCurrentFact) {
		this.mCurrentFact = mCurrentFact;
		
		setChanged();
		notifyObservers();
	}
	
	public void goToNextFact() throws GarbageCollectedException{
		setCurrentFact(ListFactModel.getInstance().getNextFact(mCurrentFact));
	}
	
	public void goToPreviousFact() throws GarbageCollectedException{
		setCurrentFact(ListFactModel.getInstance().getPreviousFact(mCurrentFact));
	}
	
	public void goToRandomFact() throws GarbageCollectedException{
		setCurrentFact(ListFactModel.getInstance().getRandomFact());
	}

	public void setToNull() {
		mCurrentFact = null;		
	}
	
	
	
	
	
}
