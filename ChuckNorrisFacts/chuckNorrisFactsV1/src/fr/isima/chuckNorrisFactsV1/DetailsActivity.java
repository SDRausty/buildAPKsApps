package fr.isima.chuckNorrisFactsV1;

import java.util.Observable;

import android.widget.EditText;
import fr.isima.chuckNorrisFactsV1.entities.Fact;

public class DetailsActivity extends AbstractBasicActivity {

	static final String KEY_FACT = "fact_key";

	private EditText et_id;
	private EditText et_fact;

	// ///////////////////////////BASIC ACTIVITY///////////
	public void update(Observable arg0, Object arg1) {
		// nothing to do : no models
	}

	@Override
	public void deleteObservers() {
		// nothing to do : no models
	}

	@Override
	public void deleteModels() {
		// nothing to do : no models
	}

	@Override
	public void initModels() {
		// nothing to do : no models
	}

	@Override
	public void initObservers() {
		// nothing to do : no models
	}

	@Override
	public void setBindings() {
		et_id = (EditText) findViewById(R.id.editText_details_id);
		et_fact = (EditText) findViewById(R.id.editText_details_fact);
	}

	@Override
	public void registerListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {
		Fact f = getIntent().getParcelableExtra(KEY_FACT);

		if (f != null) {
			et_id.setText(f.getId() + "");
			et_fact.setText(f.getFact());

		}

	}

	@Override
	public int getLayoutId() {
		return R.layout.details_activity;
	}

	// /////////////////////////////////////////////////////////////////////

}
