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

import mixedbit.speechtrainer.controller.ControllerFactory;
import android.app.Application;

/**
 * Keeps ControllerFactory that should be reused between successive
 * TrainingActivities (to avoid large reallocations of memory for audio
 * buffers).
 */
public class TrainingApplication extends Application {
    private ControllerFactory controllerFactory;

    @Override
    public void onCreate() {
        super.onCreate();
        this.controllerFactory = new ControllerFactory();
    }

    public ControllerFactory getControllerFactory() {
        return controllerFactory;
    }
}
