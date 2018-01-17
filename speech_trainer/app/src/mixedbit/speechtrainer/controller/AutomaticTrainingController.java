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

import java.util.LinkedList;

import mixedbit.speechtrainer.controller.AudioBufferAllocator.AudioBuffer;
import mixedbit.speechtrainer.controller.RecordPlayTaskManager.RecordPlayTaskState;
import mixedbit.speechtrainer.controller.SilenceFilter.FilterResult;


/**
 * Automatically switches between recording and playing. Starts in a recording
 * state. Records until a SilenceFilter detects that meaningful and complete
 * audio data of considerable length was recorded. Plays recorded data that was
 * not rejected by the SilenceFilter and switches to recording again.
 */
public class AutomaticTrainingController implements TrainingController, RecordPlayStrategy {
    private final AudioBufferAllocator audioBufferAllocator;
    private final LinkedList<AudioBuffer> recordedBuffers = new LinkedList<AudioBuffer>();
    private final SilenceFilter silenceFilter;
    private final RecordPlayTaskManager recordPlayTaskManager;

    public AutomaticTrainingController(RecordPlayTaskManager recordPlayTaskManager,
            SilenceFilter silenceFilter, AudioBufferAllocator audioBufferAllocator) {
        this.audioBufferAllocator = audioBufferAllocator;
        this.silenceFilter = silenceFilter;
        this.recordPlayTaskManager = recordPlayTaskManager;
    }

    /**
     * Automatic training controller starts recording when the training is
     * started.
     * 
     * @see mixedbit.speechtrainer.controller.TrainingController#startTraining()
     */
    @Override
    public void startTraining() {
        audioBufferAllocator.assertAllAudioBuffersAvailable();
        silenceFilter.reset();
        recordPlayTaskManager.startTask(RecordPlayTaskState.RECORD, this);
    }

    /**
     * Stops a currently running task and releases all recorded audio data.
     * 
     * @see mixedbit.speechtrainer.controller.TrainingController#stopTraining()
     */
    @Override
    public void stopTraining() {
        recordPlayTaskManager.terminateTaskIfRunning();
        releaseRecordedBuffers();
    }

    /**
     * Records an audio buffer. Switches to playing if there are no more audio
     * buffers available. Terminates if recording of an audio buffer failed.
     * Passes parameters of the newly recorded buffer to the silence filter.
     * Depending on an action returned by the filter saves or rejects the buffer
     * and continues recording or switches to playing.
     * 
     * @see mixedbit.speechtrainer.controller.RecordPlayStrategy#handleRecord(mixedbit.speechtrainer.controller.Recorder)
     */
    @Override
    public RecordPlayTaskState handleRecord(Recorder recorder) {
        final AudioBuffer audioBuffer = audioBufferAllocator.allocateAudioBuffer();
        if (audioBuffer == null) {
            // No more audio buffers, play all recorded data to free memory.
            return RecordPlayTaskState.PLAY;
        }
        if (!recorder.readAudioBuffer(audioBuffer)) {
            audioBufferAllocator.releaseAudioBuffer(audioBuffer);
            return RecordPlayTaskState.TERMINATE;
        }
        recordedBuffers.addLast(audioBuffer);
        final FilterResult filterResult = silenceFilter.filterRecorderBuffer(audioBuffer
                .getSoundLevel(), audioBuffer.getAudioDataLengthInShorts());
        switch (filterResult.getAction()) {
            case ACCEPT_BUFFER:
                break;
            case DROP_TRAILING_BUFFERS_AND_PLAY:
                for (int i = 0; i < filterResult.getNumberOfTrailingBuffersToDrop(); ++i) {
                    audioBufferAllocator.releaseAudioBuffer(recordedBuffers.removeLast());
                }
                return RecordPlayTaskState.PLAY;
            case DROP_ALL_ACCEPTED_BUFFERS:
                releaseRecordedBuffers();
                break;
        }
        return RecordPlayTaskState.RECORD;
    }

    /**
     * Plays the next audio buffer. Requests recording to start if there are no
     * more buffers to play, otherwise requests playing to continue. Releases
     * each played buffer.
     * 
     * @see mixedbit.speechtrainer.controller.RecordPlayStrategy#handlePlay(mixedbit.speechtrainer.controller.Player)
     */
    @Override
    public RecordPlayTaskState handlePlay(Player player) {
        if (!recordedBuffers.isEmpty()) {
            final AudioBuffer bufferToPlay = recordedBuffers.removeFirst();
            player.writeAudioBuffer(bufferToPlay);
            audioBufferAllocator.releaseAudioBuffer(bufferToPlay);
            return RecordPlayTaskState.PLAY;
        } else {
            return RecordPlayTaskState.RECORD;
        }
    }

    private void releaseRecordedBuffers() {
        while (!recordedBuffers.isEmpty()) {
            audioBufferAllocator.releaseAudioBuffer(recordedBuffers.remove());
        }
    }
}
