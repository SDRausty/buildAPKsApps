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

import mixedbit.speechtrainer.controller.AudioBufferAllocator.AudioBuffer;
import android.media.AudioRecord;

/**
 * Wrapper over AudioRecord that exposes minimal interface for recording
 * AudioBuffers. Informs AudioEventListener about each executed action
 * (recording started, audio buffer recorded, recording of audio buffer failed,
 * recording stopped). As in case of the Player interface, this interface is
 * extracted from the RecorderImpl to allow mocking with the EasyMock.
 */
interface Recorder {

    /**
     * Starts recording. Calls to readAudioBuffer are allowed only after
     * recording was started.
     */
    public abstract void startRecording();

    /**
     * Reads recorded audio buffer. Requires recording to be started
     * (startRecording called). Can block until there is enough audio data
     * recorded. Returns false if recording failed (this happens for instance
     * when microphone is used by some other application).
     */
    public abstract boolean readAudioBuffer(AudioBuffer audioBuffer);

    /**
     * Stops recording. Recording can be started again with the startRecording
     * method.
     */
    public abstract void stopRecording();

}

class RecorderImpl implements Recorder {
    private final AudioRecord audioRecord;
    private final AudioEventListener audioEventListener;

    /**
     * @param audioRecord
     *            AudioRecord object configured by a caller.
     * @param audioEventListener
     *            Listener that is informed about each action executed by the
     *            recorder.
     */
    public RecorderImpl(AudioRecord audioRecord, AudioEventListener audioEventListener) {
        this.audioRecord = audioRecord;
        this.audioEventListener = audioEventListener;
    }

    @Override
    public void startRecording() {
        audioRecord.startRecording();
        audioEventListener.recordingStarted();
    }

    @Override
    public boolean readAudioBuffer(AudioBuffer audioBuffer) {
        final short[] audioData = audioBuffer.getAudioData();
        int totalReadDataLength = 0;
        // On all tested devices, read() fills the whole buffer in a single
        // call. But since the API documentation is not clear about this, the
        // loop handles the case of read() filling only part of a buffer.
        while (totalReadDataLength < audioData.length) {
            final int readDataLength = audioRecord.read(audioData, totalReadDataLength,
                    audioData.length - totalReadDataLength);
            if (readDataLength <= 0) {
                // Even if buffer was partially read, discard recorded data and
                // return an error.
                audioBuffer.audioDataStored(0);
                audioEventListener.audioBufferRecordingFailed();
                return false;
            }
            totalReadDataLength += readDataLength;
        }
        audioBuffer.audioDataStored(totalReadDataLength);
        audioEventListener.audioBufferRecorded(audioBuffer.getAudioBufferId(), audioBuffer
                .getSoundLevel());
        return true;
    }

    @Override
    public void stopRecording() {
        audioRecord.stop();
        audioEventListener.recordingStopped();
    }
}
