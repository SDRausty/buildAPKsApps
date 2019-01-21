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

package mixedbit.speechtrainer.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;

import mixedbit.speechtrainer.Assertions;
import mixedbit.speechtrainer.SpeechTrainerConfig;
import mixedbit.speechtrainer.controller.AudioEventListener;

/**
 * The AudioEventCollector is separated from a GUI component ( udioEventView) to
 * simplify testing. The non-trivial logic is encapsulated in the
 * AudioEventCollector and provided to the GUI via AudioHistoryProvider
 * interface. This minimizes the GUI component responsibilities.
 */
public class AudioEventCollector implements AudioEventListener, AudioEventHistory {

    private class AudioBufferInfoImpl implements AudioBufferInfo {
        private final int audioBufferId;
        private final double soundLevel;

        private AudioBufferInfoImpl(int audioBufferId, double soundLevel) {
            this.audioBufferId = audioBufferId;
            this.soundLevel = soundLevel;
        }

        @Override
        public int getAudioBufferId() {
            return audioBufferId;
        }

        @Override
        public double getSoundLevel() {
            return soundLevel;
        }

        @Override
        public boolean isPlayed() {
            synchronized (AudioEventCollector.this) {
                return firstBufferPlayed != null
                && firstBufferPlayed.getAudioBufferId() <= audioBufferId
                && audioBufferId <= lastBufferPlayed.getAudioBufferId();
            }
        }
    }

    // How many most recent AudioBufferInfo to keep.
    public static final int HISTORY_SIZE = SpeechTrainerConfig.NUMBER_OF_AUDIO_BUFFERS;
    // A chained listener to which all audio events are also passed (In
    // production code this is the GUI component that displays audio events).
    private final AudioEventListener nextListener;

    // The two sorted sets are kept in sync. They could be replaced with a
    // single NavigableSet that supports iteration in a reverse order.
    // Unfortunately the NavigableSet is available only from Android API
    // level 9.
    private final SortedSet<AudioBufferInfo> recordedBuffersLastRecordedLast;
    private final SortedSet<AudioBufferInfo> recordedBuffersLastRecordedFirst;

    // Recently recorded buffers are not added directly to recordedBuffers in
    // order not to invalidate iterator that can be in use by the GUI.
    private final Queue<AudioBufferInfo> recentlyRecordedBuffers = new LinkedList<AudioBufferInfo>();
    private double maxSoundLevel;
    private double minSoundLevel;
    private AudioBufferInfoImpl firstBufferPlayed = null;
    private AudioBufferInfoImpl lastBufferPlayed = null;

    /**
     * @param nextListener
     *            A chained listener to which all audio events are passed.
     */
    public AudioEventCollector(AudioEventListener nextListener) {
        this.nextListener = nextListener;
        final Comparator<AudioBufferInfo> lastRecordedBufferLastComparator =
            new Comparator<AudioBufferInfo>() {
            @Override
            public int compare(AudioBufferInfo a, AudioBufferInfo b) {
                if (a.getAudioBufferId() < b.getAudioBufferId()) {
                    return -1;
                }
                if (a.getAudioBufferId() > b.getAudioBufferId()) {
                    return 1;
                }
                return 0;
            }
        };
        recordedBuffersLastRecordedLast = new TreeSet<AudioBufferInfo>(
                lastRecordedBufferLastComparator);
        recordedBuffersLastRecordedFirst = new TreeSet<AudioBufferInfo>(
                Collections.reverseOrder(lastRecordedBufferLastComparator));
        resetHistory();
    }

    @Override
    public void resetHistory() {
        maxSoundLevel = 0.0;
        minSoundLevel = Double.MAX_VALUE;
        recentlyRecordedBuffers.clear();
        recordedBuffersLastRecordedLast.clear();
        recordedBuffersLastRecordedFirst.clear();
    }

    @Override
    public void audioBufferPlayed(int audioBufferId, double soundLevel) {
        synchronized (this) {
            if (firstBufferPlayed == null) {
                firstBufferPlayed = new AudioBufferInfoImpl(audioBufferId, soundLevel);
            }
            lastBufferPlayed = new AudioBufferInfoImpl(audioBufferId, soundLevel);
        }
        nextListener.audioBufferPlayed(audioBufferId, soundLevel);
    }

    @Override
    public void audioBufferRecorded(int audioBufferId, double soundLevel) {
        synchronized (this) {
            Assertions.illegalStateIfFalse(firstBufferPlayed == null,
            "Recorded buffer but recording not started.");
            minSoundLevel = Math.min(soundLevel, minSoundLevel);
            maxSoundLevel = Math.max(soundLevel, maxSoundLevel);
            recentlyRecordedBuffers.add(new AudioBufferInfoImpl(audioBufferId, soundLevel));
        }
        nextListener.audioBufferRecorded(audioBufferId, soundLevel);
    }

    @Override
    public void playingStarted() {
        synchronized (this) {
            firstBufferPlayed = null;
            lastBufferPlayed = null;
        }
        nextListener.playingStarted();
    }

    @Override
    public void playingStopped() {
        nextListener.playingStopped();
    }

    @Override
    public synchronized void recordingStarted() {
        synchronized (this) {
            firstBufferPlayed = null;
            lastBufferPlayed = null;
        }
        nextListener.recordingStarted();
    }

    @Override
    public void recordingStopped() {
        nextListener.recordingStopped();
    }

    @Override
    public void audioBufferRecordingFailed() {
        nextListener.audioBufferRecordingFailed();
    }

    @Override
    public synchronized double getMinSoundLevel() {
        return minSoundLevel;
    }

    @Override
    public synchronized double getMaxSoundLevel() {
        return maxSoundLevel;
    }

    @Override
    public synchronized Iterator<AudioBufferInfo> getIteratorOverAudioEventsToPlot(
            int plotWidth) {
        moveRecentlyRecordedBuffersToRecordedBuffers();
        // This invalidates iterator returned by the previous call to
        // getIteratorOverAudioEventsToPlot, which is OK according to the
        // contract.
        removeOldRecordedBuffers();

        if (isPlaying() && !isOnPlotOfRecentlyRecordedBuffers(firstBufferPlayed, plotWidth)) {
            return centerPlotOn(lastBufferPlayed, plotWidth);
        } else {
            // An iterator pointing at the most recently recorded buffer.
            return recordedBuffersLastRecordedFirst.iterator();
        }
    }

    private boolean isPlaying() {
        return firstBufferPlayed != null;
    }

    private boolean isOnPlotOfRecentlyRecordedBuffers(AudioBufferInfoImpl audioBuffer, int plotWidth) {
        return (recordedBuffersLastRecordedLast.tailSet(audioBuffer).size() <= plotWidth);
    }

    private Iterator<AudioBufferInfo> centerPlotOn(AudioBufferInfoImpl audioBufferToCenter,
            int plotWidth) {
        // Point the iterator at the buffer to be placed in the center.
        final Iterator<AudioBufferInfo> iterator =
            recordedBuffersLastRecordedLast.tailSet(audioBufferToCenter).iterator();
        AudioBufferInfo startBuffer = recordedBuffersLastRecordedLast.last();
        // Move the iterator by half of the plot width, so the centered buffer
        // is actually in the center.
        for (int i = 0; i <= plotWidth / 2; ++i) {
            if (!iterator.hasNext()) {
                break;
            }
            startBuffer = iterator.next();
        }
        // This is equivalent to reversing the direction of the iterator that
        // was adjusted in previous steps.
        return recordedBuffersLastRecordedFirst.tailSet(startBuffer).iterator();
    }

    private void removeOldRecordedBuffers() {
        while (recordedBuffersLastRecordedLast.size() > HISTORY_SIZE) {
            final AudioBufferInfo bufferToRemove = recordedBuffersLastRecordedLast.first();
            recordedBuffersLastRecordedLast.remove(bufferToRemove);
            recordedBuffersLastRecordedFirst.remove(bufferToRemove);
        }
    }

    private void moveRecentlyRecordedBuffersToRecordedBuffers() {
        while (!recentlyRecordedBuffers.isEmpty()) {
            final AudioBufferInfo bufferToAdd = recentlyRecordedBuffers.remove();
            recordedBuffersLastRecordedLast.add(bufferToAdd);
            recordedBuffersLastRecordedFirst.add(bufferToAdd);
        }
    }
}