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
 * Controls the training session. Turns on recording and playing, manages
 * recorded audio data. Recording and playing are performed in a separate
 * thread. Multiple controllers can never be started simultaneously.
 */
public interface TrainingController {
    /**
     * Starts training. After the training is started, recorder and player can
     * be started.
     */
    public void startTraining();

    /**
     * Stops training. Releases all resources used during the training session
     * (stop recorder, stop player, release allocated audio buffers). After the
     * training is stopped, startTraining can be called again.
     */
    public void stopTraining();

}
