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

import mixedbit.speechtrainer.Assertions;
import mixedbit.speechtrainer.SpeechTrainerConfig;

/**
 * A stateful filter that detects complete chunks of meaningful audio data. The
 * filtering strategy is following:
 *
 * -Drop the whole leading silence (Audio buffers below silence level that are
 * not proceeded by any audio buffers above silence level).
 *
 * -Accept all buffers above silence level.
 *
 * -Accept short silence periods if these are proceeded by data above silence
 * level (such short silence periods are for instance pauses between words in a
 * sentence).
 *
 * -If a long period of silence is detected and already accepted buffers are
 * long enough, play all accepted data, but drop a part of the trailing silence
 * period. The part of the trailing silence is dropped to reduce a delay between
 * player stopping playing meaningful audio data and recorder starting recording
 * again. Dropping all trailing silence buffers does not work well, it
 * terminates playing too abruptly.
 *
 * -If a long period of silence is detected but already accepted buffers are not
 * long enough, all data is dropped. This rejects audio data above silence level
 * that is too short to contain anything meaningful and worth playing.
 *
 * -Each time the filter requests recorded data to be rejected or played, the
 * filter state is reset.
 *
 * The filter does not perform any action, it just informs the caller what
 * action should be performed.
 */
class SilenceFilter {
    /**
     * The action that the SilenceFilter user should perform after
     * filterRecordedBuffer returns.
     */
    public enum Action {
        // Accept the buffer.
        ACCEPT_BUFFER,
        // Drop all buffers accepted so far (including the buffer passed to the
        // filterRecordedBuffer call that returned this action).
        DROP_ALL_ACCEPTED_BUFFERS,
        // Drop some number of trailing buffers and play all other accepted
        // buffers. The number of buffers to drop is hold in the FilterResult.
        DROP_TRAILING_BUFFERS_AND_PLAY,
    }

    /**
     * The result of filterRecordedBuffer call. Wraps Action enum to include
     * information how many buffers should be dropped when action is
     * DROP_TRAILING_BUFFERS_AND_PLAY.
     */
    public class FilterResult {
        private final Action action;
        private final int numberOfTrailingBuffersToDrop;

        FilterResult(Action action, int numberOfTrailingBuffersToDrop) {
            Assertions.check(action == Action.DROP_TRAILING_BUFFERS_AND_PLAY);
            this.action = action;
            this.numberOfTrailingBuffersToDrop = numberOfTrailingBuffersToDrop;
        }

        FilterResult(Action action) {
            Assertions.check(action != Action.DROP_TRAILING_BUFFERS_AND_PLAY);
            this.action = action;
            numberOfTrailingBuffersToDrop = 0;
        }

        public Action getAction() {
            return action;
        }

        /**
         * Can be called only when getAction() is
         * DROP_TRAILING_BUFFERS_AND_PLAY. Returns how many buffers should be
         * dropped. The number includes the buffer that was passed to the
         * filterRecordedBuffer call that returned this FilterResult.
         */
        public int getNumberOfTrailingBuffersToDrop() {
            Assertions.illegalStateIfFalse(action == Action.DROP_TRAILING_BUFFERS_AND_PLAY);
            return numberOfTrailingBuffersToDrop;
        }
    }

    // When LONG_SILENCE_INTERVAL_MS of silence is detected and combined length
    // of accepted data above silence level exceeds
    // MIN_LENGTH_OF_MEANINGFUL_DATA_TO_PLAY_MS, the accepted data is
    // played. Otherwise, the accepted data is dropped.
    public static final int LONG_SILENCE_INTERVAL_MS = 200;
    public static final int MIN_LENGTH_OF_MEANINGFUL_DATA_TO_PLAY_MS = 150;
    // When accepted data is played, trailing buffers of combined length that is
    // larger or equal to TRAILING_SILENCE_TO_DROP_MS are dropped.
    public static final int TRAILING_SILENCE_TO_DROP_MS = 20;

    private final SilenceLevelDetector silenceLevelDetector;
    // The combined length of all buffers above silence level since the last
    // filter reset.
    private long buffersAboveSilenceLevelCombinedLengthMicroS = 0;
    // The combined length of recently detected silence. Each buffer above
    // silence level resets this. It is reset also when the filter is reset.
    private long recentSilenceCombinedLengthMicroS = 0;
    // The length of each individual recent buffer with silence. Reset together
    // with recentSilenceCombinedLengthMicros. Sum of all individual lengths
    // is equal to recentSilenceCombinedLengthMicros.
    private final LinkedList<Long> recentSilenceBuffersLengthMicros = new LinkedList<Long>();

    public SilenceFilter(SilenceLevelDetector silenceLevelDetector) {
        this.silenceLevelDetector = silenceLevelDetector;
    }

    /**
     * Resets the state of the filter. The state is also reset automatically
     * when filterRecorderBuffer returns DROP_ALL_ACCEPTED_BUFFERS, or
     * DROP_TRAILING_BUFFERS_AND_PLAY,
     */
    public void reset() {
        buffersAboveSilenceLevelCombinedLengthMicroS = 0;
        recentSilenceCombinedLengthMicroS = 0;
        recentSilenceBuffersLengthMicros.clear();
    }

    /**
     * Based on the current state of the filter, sound level and length of newly
     * recorded buffer, decides what action should be performed.
     *
     * @param bufferSoundLevel
     *            sound level of the recorded buffer.
     * @param bufferLengthInShorts
     *            length of the recorded buffer.
     * @return Action that the caller should perform.
     */
    public FilterResult filterRecorderBuffer(double bufferSoundLevel, int bufferLengthInShorts) {
        final long bufferLengthMicros = lengthInShortsToMicros(bufferLengthInShorts);
        silenceLevelDetector.addSoundLevelMeasurement(bufferSoundLevel);

        if (silenceLevelDetector.isAboveSilenceLevel(bufferSoundLevel)) {
            buffersAboveSilenceLevelCombinedLengthMicroS += bufferLengthMicros;
            recentSilenceCombinedLengthMicroS = 0;
            recentSilenceBuffersLengthMicros.clear();
        } else {
            recentSilenceCombinedLengthMicroS += bufferLengthMicros;
            recentSilenceBuffersLengthMicros.addLast(bufferLengthMicros);

            if (buffersAboveSilenceLevelCombinedLengthMicroS == 0) {
                // Drop each buffer of leading silence.
                reset();
                return new FilterResult(Action.DROP_ALL_ACCEPTED_BUFFERS);
            }
            if (micros2Milis(recentSilenceCombinedLengthMicroS) >= LONG_SILENCE_INTERVAL_MS) {
                // Long silence detected.

                if (micros2Milis(buffersAboveSilenceLevelCombinedLengthMicroS)
                        >= MIN_LENGTH_OF_MEANINGFUL_DATA_TO_PLAY_MS) {
                    // Data above the silence level is long enough. Drop a part
                    // of trailing silence and play.
                    final int trailingBuffersToDrop = computeNumberOfTrailingBuffersToDrop();
                    final FilterResult result = new FilterResult(
                            Action.DROP_TRAILING_BUFFERS_AND_PLAY, trailingBuffersToDrop);
                    reset();
                    return result;
                } else {
                    // Data above the silence level is too short. Drop it.
                    reset();
                    return new FilterResult(Action.DROP_ALL_ACCEPTED_BUFFERS);

                }
            }
            // Short silence, accept it.
        }
        return new FilterResult(Action.ACCEPT_BUFFER);
    }

    private long micros2Milis(long microSeconds) {
        return microSeconds / 1000;
    }

    private long lengthInShortsToMicros(int bufferLengthInShorts) {
        return 1000000L * bufferLengthInShorts / SpeechTrainerConfig.SAMPLE_RATE_HZ;
    }

    private int computeNumberOfTrailingBuffersToDrop() {
        int trailingBuffersToDrop = 0;
        long trailingBuffersToDropLengthMicroS = 0;
        // Count how many trailing buffers have length equal or larger than
        // TRAILING_SILENCE_TO_DROP_MS.
        while (!recentSilenceBuffersLengthMicros.isEmpty()
                && micros2Milis(trailingBuffersToDropLengthMicroS) < TRAILING_SILENCE_TO_DROP_MS) {
            trailingBuffersToDropLengthMicroS += recentSilenceBuffersLengthMicros.removeLast();
            trailingBuffersToDrop += 1;
        }
        return trailingBuffersToDrop;
    }

}
