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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import mixedbit.speechtrainer.Assertions;

/**
 * Strategy passed to the RecordPlayTask. Allows to customize how recording and
 * playing are performed. The strategy is responsible for storing and discarding
 * recorded audio data.
 */
interface RecordPlayStrategy {

    /**
     * Blocking method that is invoked repetitively by RecordPlayTask in the
     * RECORD state. Records audio data using a given recorder. The amount of
     * data recorded in a single call should not be large to make RecordPlayTask
     * responsive to cancellation.
     * 
     * @param recorder
     *            Started recorder. Strategy can use only
     *            Recorder.recordAudioData method. RecordPlayTask is responsible
     *            for executing Recorder.StartRecording and
     *            Recorder.StopRecording.
     * 
     * @return State to which RecordPlayTask should switch. RECORD - stay in the
     *         RECORD state and call handleRecord again (unless RecordPlayTask
     *         was terminated asynchronously). PLAY - switch to the PLAY state
     *         and call handlePlay (unless RecordPlayTask was terminated
     *         asynchronously). TERMINATE - terminate RecordPlayTask.
     */
    public RecordPlayTaskManager.RecordPlayTaskState handleRecord(Recorder recorder);

    /**
     * Blocking method that is invoked repetitively by RecordPlayTask in the
     * PLAY state. Plays recorded audio data using a given player. The amount of
     * data played in a single call should not be large to make RecordPlayTask
     * responsive to cancellation.
     * 
     * @param player
     *            Started player. Strategy can use only Player.playAudioData
     *            method. RecordPlayTask is responsible for executing
     *            Player.StartPlaying and Player.StopPlaying.
     * 
     * @return State to which RecordPlayTask should switch. PLAY - stay in the
     *         PLAY state and call handlePlay again (unless RecordPlayTask was
     *         terminated asynchronously). RECORD - switch to the RECORD state
     *         and call handleRecord (unless RecordPlayTask was terminated
     *         asynchronously). TERMINATE - terminate RecordPlayTask.
     */
    public RecordPlayTaskManager.RecordPlayTaskState handlePlay(Player player);
}

/**
 * Starts and terminates RecordPlayTask using a provided executor service. Every
 * started task needs to be terminated before another task can be started.
 */
class RecordPlayTaskManager {

    /**
     * State in which RecordPlayTask is. RECORD - the task is recording audio
     * data, PLAY - the task is playing audio data, TERMINATE - the task is
     * terminating.
     */
    enum RecordPlayTaskState {
        RECORD, PLAY, TERMINATE
    }

    /**
     * Should RecordPlayTask priority be increased. In production setup HIGH
     * priority should be always used. Increasing the priority fails in the
     * standard JUnit environment (it works only when JUnit tests are executed
     * on an Android device or emulator which is slow and doesn't support
     * mocking with EasyMock).
     */
    enum RecordPlayTaskPriority {
        HIGH, TEST,
    }

    private class RecordPlayTask implements Runnable {
        private RecordPlayTaskState recordPlayTaskState;
        private final RecordPlayStrategy recordPlayStrategy;
        private volatile boolean terminateRequested = false;

        /**
         * @param initialState
         *            Initial state in which the task is started.
         * @param recordPlayStrategy
         *            Strategy to which the task delegates recording and
         *            playing.
         */
        public RecordPlayTask(RecordPlayTaskState initialState,
                RecordPlayStrategy recordPlayStrategy) {
            this.recordPlayTaskState = initialState;
            this.recordPlayStrategy = recordPlayStrategy;
        }

        /**
         * Asynchronous request to terminate the task. The task terminates after
         * it finishes processing currently executed action (recording or
         * playing). Can be called if the task already terminated.
         */
        public void requestTerminate() {
            terminateRequested = true;
        }

        public boolean terminateRequested() {
            return terminateRequested;
        }

        @Override
        public void run() {
            if (recordPlayTaskPriority == RecordPlayTaskPriority.HIGH) {
                android.os.Process.setThreadPriority(
                        android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            }
            startServiceNeededInState(recordPlayTaskState);
            while (!terminateRequested()) {
                RecordPlayTaskState nextState = null;
                switch (recordPlayTaskState) {
                    case RECORD:
                        nextState = recordPlayStrategy.handleRecord(recorder);
                        break;
                    case PLAY:
                        nextState = recordPlayStrategy.handlePlay(player);
                        break;
                    case TERMINATE:
                        Assertions.check(false);
                }
                if (recordPlayTaskState != nextState) {
                    switchState(nextState);
                }
            }
            stopServiceNeededInState(recordPlayTaskState);
        }

        /**
         * Switches the task to the new state. Request the task to terminate if
         * the new state is TERMINATE. Stops a service used in the old state
         * (recorder or player) and starts a service needed in the new state.
         */
        private void switchState(RecordPlayTaskState newState) {
            stopServiceNeededInState(recordPlayTaskState);
            startServiceNeededInState(newState);
            if (newState == RecordPlayTaskState.TERMINATE) {
                requestTerminate();
            }
            recordPlayTaskState = newState;
        }

        private void startServiceNeededInState(RecordPlayTaskState state) {
            switch (state) {
                case PLAY:
                    player.startPlaying();
                    break;
                case RECORD:
                    recorder.startRecording();
                    break;
                case TERMINATE:
                    break;
            }
        }

        private void stopServiceNeededInState(RecordPlayTaskState state) {
            switch (state) {
                case PLAY:
                    player.stopPlaying();
                    break;
                case RECORD:
                    recorder.stopRecording();
                    break;
                case TERMINATE:
                    break;
            }
        }
    }

    private final Recorder recorder;
    private final Player player;
    // Currently running task or null if the task is not running.
    private RecordPlayTask recordPlayTask;
    // Future associated with the currently running task.
    private Future<?> recordPlayTaskFuture;
    private final ExecutorService executor;
    private final RecordPlayTaskPriority recordPlayTaskPriority;

    /**
     * @param executor
     *            Executor service for executing RecordPlay tasks.
     * @param recordPlayTaskPriority
     *            HIGH for production code, LOW_USE_ONLY_FOR_TESTS for unit
     *            tests.
     */
    public RecordPlayTaskManager(Recorder recorder, Player player, ExecutorService executor,
            RecordPlayTaskPriority recordPlayTaskPriority) {
        this.recorder = recorder;
        this.player = player;
        this.executor = executor;
        this.recordPlayTaskPriority = recordPlayTaskPriority;
    }

    /**
     * Starts a new task to play and record audio. If another task has been
     * already started, terminateRecordPlayTask must be called before
     * startRecordPlayTask can be called again.
     * 
     * @param initialState
     *            Defines if the task should start recording or playing.
     * @param recordPlayStrategy
     *            Strategy to be used by the task.
     */
    public void startTask(RecordPlayTaskState initialState, RecordPlayStrategy recordPlayStrategy) {
        Assertions.check(recordPlayTask == null);
        recordPlayTask = new RecordPlayTask(initialState, recordPlayStrategy);
        recordPlayTaskFuture = this.executor.submit(recordPlayTask);
    }

    /**
     * Request a running task to terminate and waits until the task exits. Can
     * be called if the task already terminated or wasn't started.
     */
    public void terminateTaskIfRunning() {
        if (recordPlayTask == null) {
            return;
        }
        recordPlayTask.requestTerminate();

        boolean waitInterrupted = false;
        do {
            try {
                waitInterrupted = false;
                this.recordPlayTaskFuture.get();
            } catch (final InterruptedException e) {
                waitInterrupted = true;
            } catch (final ExecutionException e) {
            } catch (final CancellationException e) {
            }
        } while (waitInterrupted);

        recordPlayTask = null;
        recordPlayTaskFuture = null;
    }
}
