package eu.veldsoft.vitoshadm;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;

public class DecisionMakerActivity extends Activity {
	private static Random PRNG = new Random();

	private ImageView sectors[] = { null, null, null, null, null, null, null,
			null };

	private boolean rollMode = false;

	private int currentSectorIndex = 0;

	private int randomSectorIndex = 0;

	private Timer timer = new Timer();

	private SoundPool sounds = null;

	private int popSoundId = -1;

	private int glassSoundId = -1;

	private long rollStartTime = 0;

	private void roll() {
		if (rollMode == false) {
			return;
		}

		// TODO Send time interval as activity parameter.
		if (System.currentTimeMillis() - rollStartTime > 1000
				&& currentSectorIndex == randomSectorIndex) {
			sounds.play(glassSoundId, 0.99f, 0.99f, 0, 0, 1);
			rollMode = false;
			return;
		}

		currentSectorIndex = (currentSectorIndex + 1) % sectors.length;

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < sectors.length; i++) {
					sectors[i].setAlpha(0.1F);
				}
				sectors[currentSectorIndex].setAlpha(1.0F);
				sounds.play(popSoundId, 0.99f, 0.99f, 0, 0, 1);
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_decision_maker);

		sounds = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
		popSoundId = sounds.load(DecisionMakerActivity.this, R.raw.pop, 1);
		glassSoundId = sounds.load(DecisionMakerActivity.this, R.raw.glass, 1);

		// TODO Send time interval as activity parameter.
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				roll();
			}
		}, 0, 100);

		sectors[0] = (ImageView) findViewById(R.id.sector01);
		sectors[1] = (ImageView) findViewById(R.id.sector02);
		sectors[2] = (ImageView) findViewById(R.id.sector03);
		sectors[3] = (ImageView) findViewById(R.id.sector04);
		sectors[4] = (ImageView) findViewById(R.id.sector05);
		sectors[5] = (ImageView) findViewById(R.id.sector06);
		sectors[6] = (ImageView) findViewById(R.id.sector07);
		sectors[7] = (ImageView) findViewById(R.id.sector08);

		((Button) findViewById(R.id.roll))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						randomSectorIndex = PRNG.nextInt(sectors.length);
						rollStartTime = System.currentTimeMillis();
						rollMode = true;
					}
				});
	}

	@Override
	protected void onPause() {
		super.onPause();

		rollMode = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		sounds.release();
		sounds = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_decision_maker, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.help:
			startActivity(new Intent(this, HelpActivity.class));
			break;
		case R.id.about:
			startActivity(new Intent(this, AboutActivity.class));
			break;
		}
		return true;
	}
}
