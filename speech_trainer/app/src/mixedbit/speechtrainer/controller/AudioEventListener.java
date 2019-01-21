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

/**
 * Interface for receiving information about the state and progress of recording
 * and playing. Audio event listener can assume that playing and recording never
 * happen simultaneously (recording is stopped always before playing is started
 * and playing is stopped always before recording is started).
 */
public interface AudioEventListener {

    /**
     * Informs the listener that recording has started. Listener can assume it
     * is always called before calls to audioBufferRecorded are made. Also
     * recordingStarted is never called if recording or playing are already in
     * progress.
     */
    public void recordingStarted();

    /**
     * Informs the listener that an audio buffer with a given id and sound level
     * was recorded.
     */
    public void audioBufferRecorded(int audioBufferId, double soundLevel);

    /**
     * Informs the listener that recording of an audio buffer failed. This
     * usually happens when some other application starts using the microphone.
     * Listener should not assume that recording has stopped when this callback
     * is invoked, recordingStopped() will still be called.
     */
    public void audioBufferRecordingFailed();

    /**
     * Informs the listener that recording has stopped. Listener can assume no
     * more calls to audioBufferRecorded will be made until recordingStarted is
     * called again.
     */
    public void recordingStopped();

    /**
     * Informs the listener that playing has started. Listener can assume it is
     * always called before calls to audioBufferPlayed are made. Also
     * playingStarted is never called if recording or playing are already in
     * progress.
     */
    public void playingStarted();

    /**
     * Informs the listener that an audio buffer with a given id and sound level
     * was played. Buffer id is unique, so listener can compare ids to identify
     * which recorded buffer was played.
     */
    public void audioBufferPlayed(int audioBufferId, double soundLevel);

    /**
     * Informs the listener that playing has stopped. Listener can assume no
     * more calls to audioBufferPlayed will be made until playingStarted is
     * called again.
     */
    public void playingStopped();
}