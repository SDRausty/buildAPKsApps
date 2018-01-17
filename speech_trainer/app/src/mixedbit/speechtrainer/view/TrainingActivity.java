/**
 * This file is part of Speech Trainer.
 * Copyright (C) 2011 Jan Wrobel <wrr@mixedbit.org>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mixedbit.speechtrainer.view;

import mixedbit.speechtrainer.R;
import mixedbit.speechtrainer.TrainingApplication;
import mixedbit.speechtrainer.controller.AudioEventListener;
import mixedbit.speechtrainer.controller.AutomaticTrainingController;
import mixedbit.speechtrainer.controller.ControllerFactory;
import mixedbit.speechtrainer.controller.InteractiveTrainingController;
import mixedbit.speechtrainer.controller.TrainingController;
import mixedbit.speechtrainer.model.AudioEventCollector;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Interacts with the user during the training session. Updates the UI in
 * response to audio events.
 */
public class TrainingActivity extends Activity implements OnSharedPreferenceChangeListener,
AudioEventListener {
    // Preferences that determine whether the training should be interactive or
    // automatic.
    private SharedPreferences sharedPreferences;

    // Set to be automaticTrainingController or interactiveTrainingController
    // depending on sharedPreferences.
    private TrainingController activeTrainingController;
    private AutomaticTrainingController automaticTrainingController;
    private InteractiveTrainingController interactiveTrainingController;

    // Collects the history of audio events and passes audio events to
    // the TrainingActivity.
    private AudioEventCollector audioEventCollector;
    // Plots recently recorded and played buffers. Invalidated each time a new
    // buffer is recorded or played.
    private AudioEventView audioEventView;
    // View that displays device that is active (microphone, speaker or
    // nothing).
    private ImageView activeDeviceView;
    // Elements that are used only during interactive training (recordButton is
    // actually audioEventView, alias is provided for clarity).
    private ImageButton recordButton;
    private View horizontalDividerView;
    private ImageButton replayButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.training);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Volume controls should change music volume, not ringer volume as it
        // is by default.
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        activeDeviceView = (ImageView) findViewById(R.id.activeDeviceView);

        // audioEvenCollector should pass received audio events to this
        // activity.
        audioEventCollector = new AudioEventCollector(this);

        audioEventView = (AudioEventView) findViewById(R.id.recordButton);
        // The audioEventCollector provides history of events to be displayed in
        // the audioEventView.
        audioEventView.setAudioEventHistory(audioEventCollector);

        final TrainingApplication application = (TrainingApplication) getApplication();
        final ControllerFactory controllerFactory = application.getControllerFactory();

        // Controllers need to pass audio events to the audioEventCollector. The
        // collector will pass them further to the TrainingApplication.
        try {
            automaticTrainingController = controllerFactory
            .createAutomaticTrainingController(audioEventCollector);
            interactiveTrainingController = controllerFactory
            .createInteractiveTrainingController(audioEventCollector);
        } catch (final ControllerFactory.InitializationException ex) {
            displayErrorAndFinishActivity(ex.getMessage());
            return;
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Get notification when preferences change. Preferences determine which
        // training controller should be used.
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        // The whole area in which audio events are plotted is a record button.
        recordButton = audioEventView;
        replayButton = (ImageButton) findViewById(R.id.replayButton);

        horizontalDividerView = findViewById(R.id.horizontalDividerView);

        configureButtons();
        configureActiveSession();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Can be null when the controller initialization failed.
        if (activeTrainingController != null) {
            activeTrainingController.startTraining();
            final boolean tooltipEnabled = this.sharedPreferences.getBoolean("showTooltip", true);
            if (tooltipEnabled) {
                showTooltip();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTraining();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Training can be safely stopped even if it is not started.
        stopTraining();
        configureActiveSession();
    }

    /**
     * Display an activity select by the user in the options menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settingsMenuItem:
                startActivity(new Intent(this, TrainingPreferenceActivity.class));
                break;
            case R.id.helpMenuItem:
                final Intent helpIntent = new Intent(this, FileViewerActivity.class);
                helpIntent.putExtra(FileViewerActivity.FILE_TO_DISPLAY,
                        getString(R.string.helpFile));
                helpIntent.putExtra(FileViewerActivity.WINDOW_TITLE_SUFFIX,
                        getString(R.string.helpTitleSuffix));
                startActivity(helpIntent);
                break;
            case R.id.aboutMenuItem:
                final Intent aboutIntent = new Intent(this, FileViewerActivity.class);
                aboutIntent.putExtra(FileViewerActivity.FILE_TO_DISPLAY,
                        getString(R.string.aboutFile));
                aboutIntent.putExtra(FileViewerActivity.WINDOW_TITLE_SUFFIX,
                        getString(R.string.aboutTitleSuffix));
                startActivity(aboutIntent);
                break;
            case R.id.rateMenuItem:
                final Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=mixedbit.speechtrainer"));
                startActivity(browserIntent);
                break;
        }
        return true;
    }

    @Override
    public void playingStarted() {
        activeDeviceView.post(new Runnable() {
            @Override
            public void run() {
                // Show the speaker image.
                activeDeviceView.setImageResource(R.drawable.speaker);
                activeDeviceView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void playingStopped() {
        activeDeviceView.post(new Runnable() {
            @Override
            public void run() {
                if (activeTrainingController == interactiveTrainingController) {
                    // Hide the speaker image, but only in the interactive mode.
                    // In the automatic mode recording starts immediately and
                    // hiding the speaker image causes unnecessary blinking.
                    activeDeviceView.setVisibility(View.GONE);
                }
            }
        });
    }

    // Request the plot with audio events to be redrawn when a buffer is played
    // or recorded.
    @Override
    public void audioBufferPlayed(int audioBufferId, double soundLevel) {
        audioEventView.postInvalidate();
    }

    @Override
    public void audioBufferRecorded(int audioBufferId, double soundLevel) {
        audioEventView.postInvalidate();
    }


    // recordStatusView should be enabled only when recording is in progress.
    @Override
    public void recordingStarted() {
        activeDeviceView.post(new Runnable() {
            @Override
            public void run() {
                // Show the microphone image.
                activeDeviceView.setImageResource(R.drawable.microphone);
                activeDeviceView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void recordingStopped() {
        // Do not hide the microphone image. In both modes, playing starts
        // immediately after final recording has stopped.
    }

    // When recording fails, the TrainingActivity is terminated.
    @Override
    public void audioBufferRecordingFailed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                displayErrorAndFinishActivity("Recording failed. "
                        + "Please make sure no other application is using the microphone.");
            }
        });
    }

    private void stopTraining() {
        if (activeTrainingController != null) {
            activeTrainingController.stopTraining();
            // Clear the history of audio events. Keeping old audio events on
            // the screen would be misleading, because the old events can no be
            // played after the training was stopped.
            audioEventCollector.resetHistory();
        }
    }

    private void showTooltip() {
        String tooltipText = null;
        if (activeTrainingController == interactiveTrainingController) {
            tooltipText = getString(R.string.interactiveTrainingTooltip);
        } else {
            tooltipText = getString(R.string.automaticTrainingTooltip);
        }
        final Toast toast = Toast.makeText(getApplicationContext(), tooltipText, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void displayErrorAndFinishActivity(String errorMessage) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(errorMessage);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.closeButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                TrainingActivity.this.setResult(RESULT_CANCELED);
                TrainingActivity.this.finish();
            }
        });
        builder.create().show();
    }

    private void configureButtons() {
        // Buttons control only interactive training session (they are hidden
        // during automatic training session).
        recordButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    interactiveTrainingController.record();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    interactiveTrainingController.play();
                }
                return false;
            }
        });

        replayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg) {
                if (replayButton.isEnabled()) {
                    interactiveTrainingController.play();
                }
            }
        });
    }

    /**
     * Based on a training mode select by the user, determines which controller
     * should be active and configures controls needed in the select mode.
     */
    private void configureActiveSession() {
        final boolean speechDetectionMode = this.sharedPreferences.getBoolean(
                "speechDetectionMode", true);
        if (speechDetectionMode) {
            activeTrainingController = automaticTrainingController;
        } else {
            activeTrainingController = interactiveTrainingController;
        }
        configureControlsAccordingToMode(speechDetectionMode);
    }

    private void configureControlsAccordingToMode(boolean speechDetectionMode) {
        if (speechDetectionMode) {
            // recordButton is not hidden because it acts also as
            // the AudioEventView.
            recordButton.setEnabled(false);
            replayButton.setVisibility(View.GONE);
            horizontalDividerView.setVisibility(View.GONE);
        } else {
            recordButton.setEnabled(true);
            replayButton.setVisibility(View.VISIBLE);
            horizontalDividerView.setVisibility(View.VISIBLE);
            activeDeviceView.setVisibility(View.GONE);
        }
    }

}
