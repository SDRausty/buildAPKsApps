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

package mixedbit.speechtrainer.view;

import java.util.Iterator;

import mixedbit.speechtrainer.model.AudioBufferInfo;
import mixedbit.speechtrainer.model.AudioEventHistory;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * Draws a plot with sound levels of recently recorded or played buffers. The
 * data to be plotted is obtained from the AudioEventHistory.
 * 
 * The creator of the AudioEventView must set AudioEventHistory.
 */
public class AudioEventView extends ImageButton {
    private static final int RECORDED_BUFFER_COLOR = 0xffd00000;
    private static final int PLAYED_BUFFER_COLOR = 0xff990000;
    private final Paint recordedBufferPaint;
    private final Paint playedBufferPaint;
    private AudioEventHistory audioEventHistory;

    public AudioEventView(Context context, AttributeSet attrs) {
        super(context, attrs);
        recordedBufferPaint = new Paint();
        recordedBufferPaint.setColor(RECORDED_BUFFER_COLOR);
        playedBufferPaint = new Paint();
        playedBufferPaint.setColor(PLAYED_BUFFER_COLOR);
    }

    /**
     * Must be called before recording or playing is started for the first time.
     * 
     * @param audioEventHistory
     *            Provides a data to be plotted. Not null.
     */
    public void setAudioEventHistory(AudioEventHistory audioEventHistory) {
        this.audioEventHistory = audioEventHistory;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int viewWidth = getWidth();
        final int viewHeight = getHeight();

        final Iterator<AudioBufferInfo> buffersIterator =
            audioEventHistory.getIteratorOverAudioEventsToPlot(viewWidth);

        for (int i = 0; i < viewWidth && buffersIterator.hasNext(); ++i) {
            final AudioBufferInfo audioBufferInfo = buffersIterator.next();
            final int height = getHeightForSoundLevel(audioBufferInfo.getSoundLevel(), viewHeight);
            final int lineStart = (viewHeight - height) / 2;
            if (audioBufferInfo.isPlayed()) {
                canvas.drawRect(i, lineStart, i + 1, lineStart + height, playedBufferPaint);
            } else {
                canvas.drawRect(i, lineStart, i + 1, lineStart + height, recordedBufferPaint);
            }
        }
    }

    /**
     * Calculates height of a line to represent a given soundLevel. Makes sure
     * the lowest and the smallest recorded sound levels fit in the plot.
     */
    private int getHeightForSoundLevel(double soundLevel, int canvasHeight) {
        final double minSoundLevel = audioEventHistory.getMinSoundLevel();
        final double maxSoundLevel = audioEventHistory.getMaxSoundLevel();
        return (int) ((soundLevel - minSoundLevel) * (canvasHeight - 1)
                / (maxSoundLevel - minSoundLevel));
    }
}