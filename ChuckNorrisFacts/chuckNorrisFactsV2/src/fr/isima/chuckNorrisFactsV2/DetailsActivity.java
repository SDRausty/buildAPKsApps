package fr.isima.chuckNorrisFactsV2;

import java.util.Observable;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import fr.isima.chuckNorrisFactsV2.Model.FactModel;
import fr.isima.chuckNorrisFactsV2.Model.ListFactModel;
import fr.isima.chuckNorrisFactsV2.entities.Fact;
import fr.isima.chuckNorrisFactsV2.exceptions.GarbageCollectedException;

public class DetailsActivity extends AbstractBasicActivity implements
		OnClickListener {

	static final String KEY_FACT = "fact_key";

	private FactModel mModel;

	private EditText et_id;
	private EditText et_fact;
	private Button b_next;
	private Button b_previous;
	private Button b_random;

	public void onClick(View v) {
		try {
			if (v.getId() == b_next.getId()) {
				mModel.goToNextFact();
			} else if (v.getId() == b_previous.getId()) {
				mModel.goToPreviousFact();
			} else if (v.getId() == b_random.getId()) {
				mModel.goToRandomFact();
			}
		} catch (GarbageCollectedException e) {
			Toast.makeText(this.getBaseContext(),
					"garbage collected, back to main activity",
					Toast.LENGTH_SHORT).show();
		}

	}

	public void update(Observable observable, Object data) {
		Fact f = mModel.getCurrentFact();

		if (f != null) {
			String str_id = f.getId() + "";
			if (f.getId() == -1)
				str_id = getString(R.string.unknown_id);
			et_id.setText(str_id);
			et_fact.setText(f.getFact());
		}
	}

	// //////////////////////////////BASIC
	// ACTIVITY///////////////////////////////////
	@Override
	public void initModels() {
		mModel = FactModel.getInstance();
	}

	@Override
	public void initObservers() {
		mModel.addObserver(this);

	}

	@Override
	public void setBindings() {
		et_id = (EditText) findViewById(R.id.editText_details_id);
		et_fact = (EditText) findViewById(R.id.editText_details_fact);
		b_previous = (Button) findViewById(R.id.button_previous);
		b_next = (Button) findViewById(R.id.button_next);
		b_random = (Button) findViewById(R.id.button_random);
	}

	@Override
	public void registerListeners() {
		b_previous.setOnClickListener(this);
		b_next.setOnClickListener(this);
		b_random.setOnClickListener(this);
	}

	@Override
	public void init() {
		mModel.setCurrentFact(ListFactModel.getInstance().getFactToPass());

	}

	@Override
	public int getLayoutId() {
		return R.layout.details_activity;
	}

	@Override
	public void deleteObservers() {
		mModel.deleteObserver(this);

	}

	@Override
	public void deleteModels() {
		mModel = null;
	}

	// ////////////////////////////////////////////////////////////////////
}
