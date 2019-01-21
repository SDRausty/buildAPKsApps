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

package mixedbit.speechtrainer;

/**
 * Speech Trainer global configuration parameters.
 */
public class SpeechTrainerConfig {
    // The only rate that is guaranteed to work for recording and playing on all
    // devices.
    public static final int SAMPLE_RATE_HZ = 44100;

    // The memory required for audio data is SINGLE_AUDIO_BUFFER_SIZE_IN_SHORTS
    // * 2 * NUMBER_OF_AUDIO_BUFFERS = ~6MB. The maximum time of recording is
    // SINGLE_AUDIO_BUFFER_SIZE_IN_SHORTS * NUMBER_OF_AUDIO_BUFFERS /
    // SAMPLE_RATE_HZ seconds = ~68 seconds. Should be more than enough for
    // the speech training purposes.
    public static final int SINGLE_AUDIO_BUFFER_SIZE_IN_SHORTS = 1000;
    public static final int NUMBER_OF_AUDIO_BUFFERS = 3000;

    // Approximate number of audio buffers needed to store a second of sound.
    public static int numberOfBuffersPerSecond() {
        return SAMPLE_RATE_HZ / SINGLE_AUDIO_BUFFER_SIZE_IN_SHORTS;
    }

}