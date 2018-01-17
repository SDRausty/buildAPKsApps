package fr.isima.chuckNorrisFactsV1;

import java.util.Observable;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import fr.isima.chuckNorrisFactsV1.Model.FactModel;
import fr.isima.chuckNorrisFactsV1.entities.Fact;

public class HomeActivity extends AbstractBasicActivity implements
		OnClickListener {

	private Button mButtonDisplayFactById;
	private Button mButtonDisplayRandomFact;
	private EditText mEditTextId;
	private FactModel mModel;

	public void onClick(View v) {
		Fact f = null;
		if (v.getId() == mButtonDisplayRandomFact.getId())
			f = mModel.getRandomFact();
		else
			f = mModel.getFactById(Integer.parseInt(mEditTextId.getText()
					.toString()));

		if (f != null) {
			Intent i = new Intent(this, DetailsActivity.class);
			i.putExtra(DetailsActivity.KEY_FACT, f);
			this.startActivity(i);

		} else
			Toast.makeText(
					this,
					"Problem : ID " + mEditTextId.getText().toString()
							+ " unknown", Toast.LENGTH_SHORT).show();

	}

	// ///////////////////BASIC ACTIVITY//////////////////////////////////
	@Override
	public void deleteObservers() {
		// we don't have observers
	}

	@Override
	public void deleteModels() {
		mModel.setToNull();
		mModel = null;
	}

	@Override
	public void initModels() {
		mModel = new FactModel();
	}

	@Override
	public void initObservers() {
		// we don't need observers
	}

	@Override
	public void setBindings() {
		mEditTextId = (EditText) findViewById(R.id.editText_factId);
		mButtonDisplayFactById = (Button) findViewById(R.id.button_displayFact);
		mButtonDisplayRandomFact = (Button) findViewById(R.id.button_randomFact);
	}

	@Override
	public void registerListeners() {
		mButtonDisplayFactById.setOnClickListener(this);
		mButtonDisplayRandomFact.setOnClickListener(this);
	}

	@Override
	public void init() {
		mModel.retrieveData(this.getBaseContext());
	}

	@Override
	public int getLayoutId() {
		return R.layout.home_activity;
	}

	public void update(Observable arg0, Object arg1) {
		// never called
	}
	// ///////////////////////////////////////////////////////////////////////

}
