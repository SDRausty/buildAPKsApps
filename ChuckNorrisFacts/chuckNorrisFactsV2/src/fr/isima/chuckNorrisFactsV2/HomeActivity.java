package fr.isima.chuckNorrisFactsV2;

import java.util.List;
import java.util.Observable;

import android.app.SearchManager;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import fr.isima.chuckNorrisFactsV2.Model.ListFactModel;
import fr.isima.chuckNorrisFactsV2.entities.Fact;
import fr.isima.chuckNorrisFactsV2.exceptions.GarbageCollectedException;

public class HomeActivity extends AbstractBasicActivity implements
		OnItemClickListener, TextWatcher {

	private EditText mEditTextFilter;
	private ListView mListViewFacts;
	private ListFactModel mModel;
	private FactAdapter mAdapter;

	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long arg3) {
		Fact f = (Fact) adapter.getItemAtPosition(position);
		Log.i(this.getClass().toString(), f.toString());

		Intent i = new Intent(this, DetailsActivity.class);
		mModel.setFactToPass(f);
		this.startActivity(i);

	}

	public void setFactAdapterFromList(List<Fact> list) {
		mAdapter = new FactAdapter(getBaseContext(), list);
	}

	public void update(Observable observable, Object data) {
		setFactAdapterFromList(mModel.getListFacts());
		this.mListViewFacts.setAdapter(mAdapter);

	}

	// ////////////////TEXT WATCHER///////////////////////////////////////
	public void afterTextChanged(Editable s) {
		mAdapter.getFilter().filter(s);
	}

	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub

	}

	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	// //////////////////////////////////////////////////////////////////////

	// /////////////BASIC ACTIVITY////////////////////////////////////////////
	@Override
	public void setBindings() {
		mEditTextFilter = ((EditText) findViewById(R.id.editText_filter));
		mListViewFacts = ((ListView) findViewById(R.id.listView_facts));
	}

	@Override
	public void registerListeners() {
		mListViewFacts.setOnItemClickListener(this);
		mEditTextFilter.addTextChangedListener(this);

	}

	@Override
	public void init() {
		// Get the intent, verify the action and get the query
		Intent intent = getIntent();
		if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			try {
				mModel.retrieveDataById(query, this.getBaseContext());
			} catch (GarbageCollectedException e) {
				Toast.makeText(this.getBaseContext(),
						"error : try to relaunch app", Toast.LENGTH_SHORT);
			}
		} else {
			Log.i(this.getClass().toString(), "pas dedans");
			mModel.retrieveData(this.getBaseContext());
		}
	}

	@Override
	public int getLayoutId() {
		return R.layout.home_activity;
	}

	@Override
	public void initModels() {
		mModel = ListFactModel.getInstance();

	}

	@Override
	public void initObservers() {
		mModel.addObserver(this);
	}

	@Override
	public void deleteObservers() {
		mModel.deleteObserver(this);

	}

	@Override
	public void deleteModels() {
		// mModel.setToNull();
		mModel = null;
	}

	// ///////////////////////////////////////////////////////////

}
