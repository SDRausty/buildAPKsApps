package fr.isima.chuckNorrisFactsV2;

import java.util.Observer;

import android.app.Activity;
import android.os.Bundle;

public abstract class AbstractBasicActivity extends Activity implements
		Observer {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(getLayoutId());

		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onStart() {

		initBasicActivity();
		super.onStart();
	}
	
	@Override
	protected void onDestroy() {
		deleteObservers();
		deleteModels();
		super.onDestroy();
	}

	
	public void initBasicActivity(){
		setBindings();
		registerListeners();
		initModels();
		initObservers();
		init();
	}

	public abstract void deleteObservers();
	
	public abstract void deleteModels();
	
	public abstract void initModels();
	
	public abstract void initObservers();

	public abstract void setBindings();

	public abstract void registerListeners();

	public abstract void init();

	public abstract int getLayoutId();

}
