package com.ariwilson.seismo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.SensorManager;
import android.view.SurfaceHolder;
import android.widget.Toast;

public class SeismoViewThread extends Thread {
  public SeismoViewThread(Context ctx, SurfaceHolder holder, boolean filter,
                          int axis, int period) {
    scale_paint_.setARGB(255, 137, 137, 137);
    scale_paint_.setAntiAlias(true);
    line_paint_.setARGB(255, 0, 0, 0);
    line_paint_.setAntiAlias(false);
    pts_ = new float[SECONDS_TO_SAVE * 4000 / period];

    holder_ = holder;
    setFilter(filter);
    setAxis(axis);
    db_ = SeismoDbAdapter.getAdapter();
    ctx_ = ctx;
    period_ = period;
  }

  @Override
  public void run() {
    while (running_) {
      // Retrieve measurements from queue, blocking until at least one is
      // available.
      accelerations_.clear();
      try {
        ArrayList<Float> acceleration = history_queue_.poll(
            period_, TimeUnit.MILLISECONDS);
        if (acceleration != null) accelerations_.add(acceleration);
      } catch (Exception e) {
        // Ignore.
      }
      history_queue_.drainTo(accelerations_);
      synchronized (history_) {
        for (ArrayList<Float> acceleration : accelerations_) {
          history_.add(acceleration);
          while (acceleration.get(0) - history_.get(0).get(0) >
                 SECONDS_TO_SAVE * 1000) {
            history_.remove(0);
          }
          while (acceleration.get(0) - history_.get(start_).get(0) >
                 SECONDS_TO_DISPLAY * 1000) {
            ++start_;
          }
        }
        synchronized (holder_) {
          Canvas canvas = holder_.lockCanvas();
          if (canvas == null) continue;
          canvas.drawARGB(255, 255, 255, 255);
  
          scale_paint_.setStrokeWidth(canvas_width_ / 300f);
          float text_size = canvas_width_ / 35f;
          scale_paint_.setTextSize(text_size);
          
          // Draw g scale.
          scale_paint_.setTextAlign(Paint.Align.CENTER);
          for (int i = -MAX_G + 1; i <= MAX_G - 1; ++i) {
            float x = canvas_width_ / 2 * (1 + (float)i / MAX_G);
            canvas.drawLine(x, 0, x, canvas_height_ / 20, scale_paint_);
            canvas.drawText(Integer.toString(i) + "g", x,
                            canvas_height_ / 20 + 1.2f * text_size,
                            scale_paint_);
          }
  
          // Draw time scale in seconds.
          // Don't want to determine scale if no values written yet.
          float end_time = -1, start_time = -1;
          if (history_.size() > 0) {
            end_time = history_.get(history_.size() - 1).get(0) / 1000;
            start_time = end_time - SECONDS_TO_DISPLAY;
            scale_paint_.setTextAlign(Paint.Align.LEFT);
            for (int s = (int) Math.floor(end_time);
                 s >= Math.max(Math.floor(start_time), 0);
                 --s) {
              float y = canvas_height_ * (s - start_time) / SECONDS_TO_DISPLAY;
              canvas.drawLine(0, y, canvas_width_ / 20, y, scale_paint_);
              canvas.drawText(Integer.toString(s) + "s",
                              canvas_width_ / 20 + 0.2f * text_size,
                              y + 0.5f * text_size, scale_paint_);
            }
          }

          // Draw line.
          for (int i = start_ + 1; i < history_.size(); ++i) {
            ArrayList<Float> history1 = history_.get(i - 1),
                             history2 = history_.get(i);
            int j = i - start_ - 1;
            pts_[j * 4] = canvas_width_ / 2 *
                          (1 + history1.get(axis_ + 1) / MAX_ACCELERATION);
            pts_[j * 4 + 1] = canvas_height_ *
                              (history1.get(0) / 1000 - start_time) /
                              SECONDS_TO_DISPLAY;
            pts_[j * 4 + 2] = canvas_width_ / 2 *
                              (1 + history2.get(axis_ + 1) / MAX_ACCELERATION);
            pts_[j * 4 + 3] = canvas_height_ *
                              (history2.get(0) / 1000 - start_time) /
                              SECONDS_TO_DISPLAY;
          }
          line_paint_.setStrokeWidth(canvas_width_ / 300f);
          canvas.drawLines(pts_, 0, (history_.size() - start_) * 4,
                           line_paint_);
          holder_.unlockCanvasAndPost(canvas);
        }
      }
    }
  }

  public void update(float x, float y, float z) {
    ArrayList<Float> acceleration = new ArrayList<Float>(3);
    acceleration.add((float)(new Date().getTime() - start_time_));
    if (filter_) {
      filter_acceleration_[0] = x * FILTERING_FACTOR +
                         filter_acceleration_[0] * (1.0f - FILTERING_FACTOR);
      acceleration.add(x - filter_acceleration_[0]);
      filter_acceleration_[1] = y * FILTERING_FACTOR +
                         filter_acceleration_[1] * (1.0f - FILTERING_FACTOR);
      acceleration.add(y - filter_acceleration_[1]);
      filter_acceleration_[2] = z * FILTERING_FACTOR +
                         filter_acceleration_[2] * (1.0f - FILTERING_FACTOR);
      acceleration.add(z - filter_acceleration_[2]);
    } else {
      acceleration.add(x);
      acceleration.add(y);
      acceleration.add(z);
    }
    try {
      history_queue_.put(acceleration);
    } catch (Exception e) {
      // Do nothing.
    }
  }

  public void setSurfaceSize(int canvas_width, int canvas_height) {
    synchronized (holder_) {
      canvas_width_ = canvas_width;
      canvas_height_ = canvas_height;
      start_time_ = new Date().getTime();
      start_ = 0;
    }
  }

  public void setRunning(boolean running) {
    running_ = running;
  }

  public void setPaused(boolean paused) {
    if (paused) {
      start_paused_time_ = new Date().getTime();
    } else {
      synchronized (history_) {
        start_time_ += new Date().getTime() - start_paused_time_;
      }
    }
  }

  public void setFilter(boolean filter) {
    filter_ = filter;
  }
  
  public void setAxis(int axis) {
    axis_ = axis;
  }

  public void save() {
    SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date = new Date();
    String name = date_format.format(date);

    db_.open(ctx_);
    synchronized (history_) {
      if (db_.createGraph(name, history_) >= 0) {
        Toast.makeText(ctx_, "Saved graph as " + name + ".", Toast.LENGTH_LONG)
            .show();
      } else {
        Toast.makeText(ctx_, "Failed to save graph. Please try again.",
                       Toast.LENGTH_LONG).show();
      }
    }
    db_.close();
  }

  // Constants.
  private static final int MAX_G = 3;
  private static final float MAX_ACCELERATION = MAX_G *
                             SensorManager.GRAVITY_EARTH;
  private static final float FILTERING_FACTOR = 0.1f;
  private static final int SECONDS_TO_SAVE = 60;
  private static final int SECONDS_TO_DISPLAY = 10;

  // Reused member variables.
  LinkedList<ArrayList<Float>> accelerations_ =
      new LinkedList<ArrayList<Float>>();
  private Paint scale_paint_ = new Paint();
  private Paint line_paint_ = new Paint();
  private float[] pts_;

  // Important preferences and history.
  // TODO(ariw): Worst data structure choice ever.
  private ArrayList<ArrayList<Float>> history_ =
      new ArrayList<ArrayList<Float>>();
  private ArrayBlockingQueue<ArrayList<Float>> history_queue_ =
      new ArrayBlockingQueue<ArrayList<Float>>(20);
  private int start_ = 0;
  private float[] filter_acceleration_ = new float[3];
  private long start_time_ = new Date().getTime();
  private long start_paused_time_ = start_time_;
  private int canvas_height_ = 1;
  private int canvas_width_ = 1;
  private boolean running_ = true;
  private boolean filter_;
  private int axis_;
  private SeismoDbAdapter db_;
  private SurfaceHolder holder_;
  private Context ctx_;
  private int period_;
}
