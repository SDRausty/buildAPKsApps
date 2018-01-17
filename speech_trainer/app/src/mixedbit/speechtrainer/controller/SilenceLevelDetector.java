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

import mixedbit.speechtrainer.SpeechTrainerConfig;

/**
 * Estimates a silence level based on measurements of sound level. Determines if
 * a given sound level is above the silence level.
 * 
 * The general idea is to find and update a mean sound level of samples that
 * included silence. All samples for which sound level is less than the mean
 * plus some threshold are determined to be silence.
 * 
 * A very similar approach along with comparison to alternatives is described in
 * 'A Comparative Study of Speech Detection Methods' by Stefaan Van Gerven and
 * Fei Xie.
 * 
 * A more detailed description follows:
 * 
 * The silence level is initialized to a large value, so the first sound level
 * measurement is guaranteed to be below the silence level (it is not important
 * if it really is silence).
 * 
 * A list of recent measurements of sound levels that were below the silence
 * level is kept. The length of the list is limited, so the silence level is not
 * skewed by the old measures.
 * 
 * -If a measurement of sound level is below the silence level, the measurement
 * is added to the list. The silence level is updated to be a mean of all
 * measures on the list plus SILENCE_LEAVE_MARGIN.
 * 
 * -If a measurement of sound level is above the silence level, the silence
 * level is updated to be a mean of all measures on the list plus
 * SILENCE_ENTER_MARGIN. The measure is not added to the list.
 */
class SilenceLevelDetector {
    // When silence is recorded, silence level is that much above the mean of
    // recent sound levels of samples with silence.
    public static final double SILENCE_LEAVE_MARGIN = 5.0;
    // When meaningful sound is recorded, silence level is that much above the
    // mean of recent sound levels of samples with silence.
    public static final double SILENCE_ENTER_MARGIN = 2.0;
    // Silence sound level is discarded if combined length of newer measures
    // with silence exceeds this const.
    public static final int SILENCE_HISTORY_LENGTH_S = 5;
    // History length but in number of samples, not in seconds.
    public static final int SILENCE_HISTORY_LENGTH = SILENCE_HISTORY_LENGTH_S
    * SpeechTrainerConfig.numberOfBuffersPerSecond();
    // Keeps the most recent measures of the sound level but only for samples
    // that were below silence level.
    private final LinkedList<Double> silenceHistory = new LinkedList<Double>();
    // Sum of all values on the silenceHistory list.
    private double silenceHistorySum = 0.0;
    private double silenceLevel = Double.MAX_VALUE;

    public void addSoundLevelMeasurement(double soundLevel) {
        if (!isAboveSilenceLevel(soundLevel)) {
            silenceHistory.add(soundLevel);
            if (silenceHistory.size() == SILENCE_HISTORY_LENGTH) {
                final double removedValue = silenceHistory.remove();
                silenceHistorySum -= removedValue;
            }
            silenceHistorySum += soundLevel;
            silenceLevel = mean() + SILENCE_LEAVE_MARGIN;
        } else {
            silenceLevel = mean() + SILENCE_ENTER_MARGIN;
        }
    }

    public boolean isAboveSilenceLevel(double soundLevel) {
        return soundLevel > silenceLevel;
    }

    private double mean() {
        return silenceHistorySum / silenceHistory.size();
    }
}
