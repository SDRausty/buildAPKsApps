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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import mixedbit.speechtrainer.controller.AudioBufferAllocator.AudioBuffer;
import mixedbit.speechtrainer.controller.RecordPlayTaskManager.RecordPlayTaskState;


/**
 * Controls the training session according to commands that come from the user
 * (play and record).
 */
public class InteractiveTrainingController implements TrainingController, RecordPlayStrategy {
    private final RecordPlayTaskManager recordPlayTaskManager;
    private final AudioBufferAllocator audioBufferAllocator;
    private final Queue<AudioBuffer> recordedBuffers = new LinkedList<AudioBuffer>();
    private Iterator<AudioBuffer> nextBufferToPlay;

    public InteractiveTrainingController(RecordPlayTaskManager recordPlayTaskManager,
            AudioBufferAllocator audioBufferAllocator) {
        this.audioBufferAllocator = audioBufferAllocator;
        this.recordPlayTaskManager = recordPlayTaskManager;
    }

    /**
     * Interactive training controller does nothing when the training is
     * started. It waits for a user to start recording.
     * 
     * @see mixedbit.speechtrainer.controller.TrainingController#startTraining()
     */
    @Override
    public void startTraining() {
        // Sanity check to make sure audio buffers do not leak.
        audioBufferAllocator.assertAllAudioBuffersAvailable();
    }

    /**
     * Stops a currently running task (if any) and releases all recorded audio
     * data.
     * 
     * @see mixedbit.speechtrainer.controller.TrainingController#stopTraining()
     */
    @Override
    public void stopTraining() {
        recordPlayTaskManager.terminateTaskIfRunning();
        releaseRecordedBuffers();
    }

    /**
     * Stops a currently running task (if any), releases all already recorded
     * audio data and starts a new background task to record audio. The
     * recording task is terminated when play(), record() or stopTraining() are
     * called or when there are no more AudioBuffers available to hold recorded
     * data.
     */
    public void record() {
        recordPlayTaskManager.terminateTaskIfRunning();
        releaseRecordedBuffers();
        audioBufferAllocator.assertAllAudioBuffersAvailable();
        recordPlayTaskManager.startTask(RecordPlayTaskState.RECORD, this);
    }

    /**
     * Stops a currently running task (if any), and starts a new background task
     * to play recorded data. play can be called multiple times to replay the
     * same recorded data. The playing task is terminated when play(), record()
     * or stopTraining() are called or when all recorded data is played.
     */
    public void play() {
        recordPlayTaskManager.terminateTaskIfRunning();
        nextBufferToPlay = recordedBuffers.iterator();
        recordPlayTaskManager.startTask(RecordPlayTaskState.PLAY, this);
    }

    /**
     * Records and saves an audio buffer. Requests recording to terminate if
     * there are no more audio buffers available or if recording failed.
     * Otherwise requests recording to continue.
     * 
     * @see mixedbit.speechtrainer.controller.RecordPlayStrategy#handleRecord(mixedbit.speechtrainer.controller.Recorder)
     */
    @Override
    public RecordPlayTaskState handleRecord(Recorder recorder) {
        final AudioBuffer audioBuffer = audioBufferAllocator.allocateAudioBuffer();
        if (audioBuffer == null) {
            return RecordPlayTaskState.TERMINATE;
        }
        if (!recorder.readAudioBuffer(audioBuffer)) {
            audioBufferAllocator.releaseAudioBuffer(audioBuffer);
            return RecordPlayTaskState.TERMINATE;
        }
        recordedBuffers.add(audioBuffer);
        return RecordPlayTaskState.RECORD;
    }

    /**
     * Plays the next audio buffer. Requests playing to terminate if there are
     * no more buffers to play, otherwise requests playing to continue.
     * 
     * @see mixedbit.speechtrainer.controller.RecordPlayStrategy#handlePlay(mixedbit.speechtrainer.controller.Player)
     */
    @Override
    public RecordPlayTaskState handlePlay(Player player) {
        if (nextBufferToPlay.hasNext()) {
            player.writeAudioBuffer(nextBufferToPlay.next());
            return RecordPlayTaskState.PLAY;
        } else {
            return RecordPlayTaskState.TERMINATE;
        }
    }

    private void releaseRecordedBuffers() {
        while (!recordedBuffers.isEmpty()) {
            audioBufferAllocator.releaseAudioBuffer(recordedBuffers.remove());
        }
    }
}
