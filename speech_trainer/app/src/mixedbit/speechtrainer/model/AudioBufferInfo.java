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

/**
 * Information about a recorded buffer needed by the UI.
 */
public interface AudioBufferInfo {
    /**
     * @return Unique, increasing with time id of the recorded buffer.
     */
    public int getAudioBufferId();

    /**
     * @return Sound level of the recorded buffer.
     */
    public double getSoundLevel();

    /**
     * @return Should this buffer be marked as played? Only the most recently
     *         played buffers are marked.
     */
    public boolean isPlayed();
}
