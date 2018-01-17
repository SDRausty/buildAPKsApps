package com.ariwilson.seismo;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SeismoView extends SurfaceView implements SurfaceHolder.Callback {
  public SeismoView(Context ctx, int period) {
    super(ctx);

    SurfaceHolder holder = getHolder();
    holder.addCallback(this);
    setKeepScreenOn(true);
    ctx_ = ctx;
    period_ = period;
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int width,
                             int height) {
    view_thread_.setSurfaceSize(width, height);
  }

  public void surfaceCreated(SurfaceHolder holder) {
    AccelerometerReader reader = new AccelerometerReader(ctx_);
    view_thread_ = new SeismoViewThread(ctx_, holder, filter_, axis_, period_);
    reader_thread_ = new AccelerometerReaderThread(reader, view_thread_,
                                                   paused_, period_);
    view_thread_.start();
    reader_thread_.start();
  }

  public void surfaceDestroyed(SurfaceHolder holder) {
    view_thread_.setRunning(false);
    reader_thread_.setRunning(false);
    try {
      view_thread_.join();
      reader_thread_.join();
    } catch (InterruptedException e) {
      // Do nothing.
    }
  }

  public void pause() {
    paused_ = true;
    view_thread_.setPaused(true);
    reader_thread_.setPaused(true);
  }

  public void resume() {
    paused_ = false;
    view_thread_.setPaused(false);
    reader_thread_.setPaused(false);
  }

  public void filter() {
    filter_ = true;
    view_thread_.setFilter(true);
  }

  public void unfilter() {
    filter_ = false;
    view_thread_.setFilter(false);
  }
  
  public void x() {
    axis_ = 0;
    view_thread_.setAxis(0);
  }

  public void y() {
    axis_ = 1;
    view_thread_.setAxis(1);
  }

  public void z() {
    axis_ = 2;
    view_thread_.setAxis(2);
  }

  public void save() {
    view_thread_.save();
  }

  private AccelerometerReaderThread reader_thread_;
  private SeismoViewThread view_thread_;
  private boolean paused_ = false;
  private boolean filter_ = true;
  private int axis_ = 2;
  private Context ctx_;
  private int period_;
}
