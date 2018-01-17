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

import java.util.Iterator;

/**
 * Stores and manages information about recorded and played buffers for
 * displaying in the UI. Provides an iterator pointing at the first buffer to be
 * plotted.
 */
public interface AudioEventHistory {

    /**
     * @return The minimum sound level of all recorded buffers. Along with
     *         getMaxSoundLevel() allows to scale the plot of sound levels.
     */
    public double getMinSoundLevel();

    /**
     * @return The maximum sound level of all recorded buffers.
     */
    public double getMaxSoundLevel();

    /**
     * 
     * @param plotWidth
     *            How many events can fit on the plot.
     * @return An iterator pointing at the first event to be plotted. If playing
     *         is in progress and the first played buffer is outside of the plot
     *         of recently recorded buffers, the iterator is positioned in such
     *         a way that the most recently played buffer is in the middle of
     *         the plot. In all other cases, the iterator points at the most
     *         recently recorded buffer. The iterator remains valid until the
     *         next call to the getIteratorOverAudioEventsToPlot.
     */
    public Iterator<AudioBufferInfo> getIteratorOverAudioEventsToPlot(int plotWidth);

    /**
     * Clears the history of recorded and played buffers. Resets min and max
     * sound levels.
     */
    public void resetHistory();
}