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

package mixedbit.speechtrainer.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mixedbit.speechtrainer.SpeechTrainerConfig;
import mixedbit.speechtrainer.controller.RecordPlayTaskManager.RecordPlayTaskPriority;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;

/**
 * Creates training controllers. It is OK to create multiple controllers with
 * this class, but the user must ensure that multiple controllers are never
 * started simultaneously.
 */
public class ControllerFactory {
    public class InitializationException extends Exception {
        private static final long serialVersionUID = -4092114494178018622L;

        public InitializationException(String detailMessage) {
            super(detailMessage);
        }
    }

    // Objects that are reused between successive controllers to avoid
    // reallocations of large buffers and recreation of an executor thread.
    private final AudioBufferAllocator audioBufferAllocator;
    private final ExecutorService executor;
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private int audioTrackBufferSizeInBytes;

    public ControllerFactory() {
        audioBufferAllocator = new AudioBufferAllocator(
                SpeechTrainerConfig.NUMBER_OF_AUDIO_BUFFERS,
                SpeechTrainerConfig.SINGLE_AUDIO_BUFFER_SIZE_IN_SHORTS);
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * @param audioEventListener
     *            Listener that will be informed about events handled by the
     *            created controller.
     */
    public InteractiveTrainingController createInteractiveTrainingController(
            AudioEventListener audioEventListener) throws InitializationException {
        if (audioRecord == null) {
            createAudioRecord();
        }
        if (audioTrack == null) {
            createAudioTrack();
        }
        final Recorder recorder = new RecorderImpl(audioRecord, audioEventListener);
        final Player player = new PlayerImpl(audioTrack, audioTrackBufferSizeInBytes,
                audioEventListener);
        final RecordPlayTaskManager recordPlayTaskManager = new RecordPlayTaskManager(recorder,
                player, executor, RecordPlayTaskPriority.HIGH);
        return new InteractiveTrainingController(recordPlayTaskManager, audioBufferAllocator);
    }

    /**
     * @param audioEventListener
     *            Listener that will be informed about events handled by the
     *            created controller.
     */
    public AutomaticTrainingController createAutomaticTrainingController(
            AudioEventListener audioEventListener) throws InitializationException {
        if (audioRecord == null) {
            createAudioRecord();
        }
        if (audioTrack == null) {
            createAudioTrack();
        }
        final Player player = new PlayerImpl(audioTrack, audioTrackBufferSizeInBytes,
                audioEventListener);
        final Recorder recorder = new RecorderImpl(audioRecord, audioEventListener);
        final RecordPlayTaskManager recordPlayTaskManager = new RecordPlayTaskManager(recorder,
                player, executor, RecordPlayTaskPriority.HIGH);
        return new AutomaticTrainingController(recordPlayTaskManager, new SilenceFilter(
                new SilenceLevelDetector()), audioBufferAllocator);
    }

    private void createAudioRecord() throws InitializationException {
        // The AudioRecord configurations parameters used here, are guaranteed
        // to be supported on all devices.

        // AudioFormat.CHANNEL_IN_MONO should be used in place of deprecated
        // AudioFormat.CHANNEL_CONFIGURATION_MONO, but it is not available for
        // API level 3.

        // Unlike AudioTrack buffer, AudioRecord buffer could be larger than
        // minimum without causing any problems. But minimum works well.
        final int audioRecordBufferSizeInBytes = AudioRecord.getMinBufferSize(
                SpeechTrainerConfig.SAMPLE_RATE_HZ, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        if (audioRecordBufferSizeInBytes <= 0) {
            throw new InitializationException("Failed to initialize recording.");
        }

        // CHANNEL_IN_MONO is guaranteed to work on all devices.
        // ENCODING_PCM_16BIT is guaranteed to work on all devices.
        audioRecord = new AudioRecord(AudioSource.MIC, SpeechTrainerConfig.SAMPLE_RATE_HZ,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
                audioRecordBufferSizeInBytes);
        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            audioRecord = null;
            throw new InitializationException("Failed to initialize recording.");
        }
    }

    private void createAudioTrack() throws InitializationException {
        // The AudioTrack configurations parameters used here, are guaranteed to
        // be supported on all devices.

        // AudioFormat.CHANNEL_OUT_MONO should be used in place of deprecated
        // AudioFormat.CHANNEL_CONFIGURATION_MONO, but it is not available for
        // API level 3.

        // Output buffer for playing should be as short as possible, so
        // AudioBufferPlayed events are not invoked long before audio buffer is
        // actually played. Also, when AudioTrack is stopped, it is filled with
        // silence of length audioTrackBufferSizeInBytes. If the silence is too
        // long, it causes a delay before the next recorded data starts playing.
        audioTrackBufferSizeInBytes = AudioTrack.getMinBufferSize(
                SpeechTrainerConfig.SAMPLE_RATE_HZ,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        if (audioTrackBufferSizeInBytes <= 0) {
            throw new InitializationException("Failed to initialize playback.");
        }

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SpeechTrainerConfig.SAMPLE_RATE_HZ,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
                audioTrackBufferSizeInBytes,
                AudioTrack.MODE_STREAM);
        if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
            audioTrack = null;
            throw new InitializationException("Failed to initialize playback.");
        }
    }


}
