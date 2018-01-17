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

import java.util.concurrent.ConcurrentLinkedQueue;

import mixedbit.speechtrainer.Assertions;

/**
 * Keeps a fixed pool of buffers for audio samples. Allocates memory for the
 * buffers once and then reuses the buffers. This decreases allocation and
 * garbage collection overhead and guarantees that memory usage is bounded. The
 * class is thread safe.
 */
class AudioBufferAllocator {
    /**
     * Buffer for audio samples with additional information about samples
     * (number of samples, combined sound level of all samples kept in the
     * buffer). This class in not thread safe.
     */
    final class AudioBuffer {
        private static final int DECIBEL_MULTIPLIER = 10;
        // State is kept only for contract violation checks and is not exposed
        // to the user.
        private BufferState bufferState;
        private final short[] audioData;
        private int audioDataLengthInShorts;
        private double soundLevel;
        private int audioBufferId;

        /**
         * Only {@link AudioBufferAllocator} can create AudioBuffers.
         */
        private AudioBuffer(int bufferSize) {
            audioData = new short[bufferSize];
            bufferState = BufferState.AVAILABLE;
        }

        /**
         * @return buffer for audio data. After content of the buffer is
         *         modified, audioDataStored must be called.
         */
        public short[] getAudioData() {
            checkStateIs(BufferState.ALLOCATED);
            return audioData;
        }

        /**
         * Must be always called after audio data is modified.
         * 
         * @param audioDataLengthInShorts
         *            Number of samples stored in the audio data buffer, can not
         *            be larger than length of audio data array.
         */
        public void audioDataStored(int audioDataLengthInShorts) {
            Assertions.illegalStateIfFalse(audioDataLengthInShorts <= audioData.length,
            "Audio data length too long.");
            checkStateIs(BufferState.ALLOCATED);
            this.audioDataLengthInShorts = audioDataLengthInShorts;
            computeSoundLevel();
        }

        /**
         * @return Unique id of the allocated buffer. The id is guaranteed to
         *         increase for buffers returned by subsequent
         *         allocateAudioBuffer calls. Once buffer is released, its id is
         *         forgotten and a new id is assigned when the buffer is
         *         allocated again.
         */
        public int getAudioBufferId() {
            checkStateIs(BufferState.ALLOCATED);
            return audioBufferId;
        }

        /**
         * @return Length of audio data stored in the buffer.
         */
        public int getAudioDataLengthInShorts() {
            checkStateIs(BufferState.ALLOCATED);
            return audioDataLengthInShorts;
        }

        /**
         * @return Sound level of audio data stored in the buffer. See
         *         {@link http://en.wikipedia.org/wiki/Sound_pressure}. The
         *         returned sound level is not an absolute, device independent
         *         value.
         */
        public double getSoundLevel() {
            checkStateIs(BufferState.ALLOCATED);
            return this.soundLevel;
        }

        private void changeStateTo(BufferState state) {
            this.bufferState = state;
        }

        private void checkStateIs(BufferState state) {
            if (this.bufferState != state) {
                throw new IllegalStateException("Audio buffer is in illegal state "
                        + this.bufferState.name());
            }
        }

        private void computeSoundLevel() {
            long sum = 0;
            for (int i = 0; i < audioDataLengthInShorts; ++i) {
                sum += audioData[i] * audioData[i];
            }
            if (sum != 0) {
                this.soundLevel = DECIBEL_MULTIPLIER
                * Math.log10(1.0 * sum / audioDataLengthInShorts);
            } else {
                this.soundLevel = 0.0;
            }
        }

        private void setAudioBufferId(int audioBufferId) {
            this.audioBufferId = audioBufferId;
        }
    }

    /**
     * State of an audio buffer.
     */
    private enum BufferState {
        AVAILABLE, ALLOCATED
    }

    ConcurrentLinkedQueue<AudioBuffer> availableBuffers;
    private final int numberOfBuffers;
    private int nextAudioBufferId = 0;

    /**
     * All audio buffers are allocated during the construction of
     * {@link AudioBufferAllocator}.
     * 
     * @param numberOfBuffers
     * @param singleBufferSize
     */
    public AudioBufferAllocator(int numberOfBuffers, int singleBufferSize) {
        this.numberOfBuffers = numberOfBuffers;
        availableBuffers = new ConcurrentLinkedQueue<AudioBuffer>();
        for (int i = 0; i < numberOfBuffers; ++i) {
            availableBuffers.add(new AudioBuffer(singleBufferSize));
        }
    }

    /**
     * Allocates an audio buffer. The ownership of the allocated buffer is
     * passed to the caller. When caller is done using the buffer, it must
     * return it to the pool by calling releaseAudioBuffer.
     * 
     * @return AudioBuffer with empty audio data that can be initialized and
     *         used by the caller, null if there are no buffers available.
     */
    public AudioBuffer allocateAudioBuffer() {
        final AudioBuffer buffer = availableBuffers.poll();
        if (buffer != null) {
            buffer.setAudioBufferId(nextAudioBufferId);
            ++nextAudioBufferId;
            buffer.changeStateTo(BufferState.ALLOCATED);
        }
        return buffer;
    }

    /**
     * Releases an audio buffer. The caller returns the ownership of the buffer
     * to the {@link AudioBufferAllocator} and can not use the buffer anymore.
     * Calling any method on a released buffer is forbidden.
     */
    public void releaseAudioBuffer(AudioBuffer audioBuffer) {
        audioBuffer.checkStateIs(BufferState.ALLOCATED);
        // Clear audio data of the released buffer.
        audioBuffer.audioDataStored(0);
        audioBuffer.changeStateTo(BufferState.AVAILABLE);
        availableBuffers.add(audioBuffer);
    }

    /**
     * Sanity check that can be executed by a user in places where all buffers
     * should be released and available for allocation. Helps to ensure audio
     * buffers do not leak.
     */
    public void assertAllAudioBuffersAvailable() {
        Assertions.check(this.numberOfBuffers == availableBuffers.size());
    }
}
