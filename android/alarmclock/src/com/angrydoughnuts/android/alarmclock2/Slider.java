/****************************************************************************
 * Copyright 2016 kraigs.android@gmail.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ****************************************************************************/

package com.angrydoughnuts.android.alarmclock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

public class Slider extends FrameLayout {
  private final View slide;
  private boolean tracking = false;
  private Listener listener;
  public void setListener(Listener l) { listener = l; }

  public Slider(Context c) {
    this(c, null);
  }

  public Slider(Context c, AttributeSet a) {
    this(c, a, 0);
  }

  public Slider(Context c, AttributeSet a, int defStyleAttr) {
    this(c, a, defStyleAttr, 0);
  }

  public Slider(Context c, AttributeSet a, int defStyleAttr, int defStyleRes) {
    super(c, a, defStyleAttr, defStyleRes);

    ((LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
      .inflate(R.layout.slider, this, true);
    slide = findViewById(R.id.slider_slide);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getActionMasked()) {
    case MotionEvent.ACTION_DOWN:
      if (slide.getVisibility() != View.VISIBLE)
        break;
      if (event.getX() < slide.getLeft() || event.getX() > slide.getRight())
        break;
      tracking = true;
      return true;

    case MotionEvent.ACTION_MOVE:
      if (!tracking)
        break;
      slide.offsetLeftAndRight((int)
          (event.getX() - slide.getLeft() - getLeft() - slide.getWidth()/2));
      if (event.getX() > (getLeft() + getWidth() * COMPLETION)) {
        if (listener != null)
          listener.onComplete();
        reset();
      }
      return true;

    case MotionEvent.ACTION_UP:
    case MotionEvent.ACTION_CANCEL:
      if (!tracking)
        break;
      reset();
      return true;
    }
    return super.onTouchEvent(event);
  }

  private void reset() {
    tracking = false;
    TranslateAnimation a =
      new TranslateAnimation(slide.getLeft(), getLeft(), 0, 0);
    a.setDuration(SLIP_MS);
    a.setInterpolator(new DecelerateInterpolator(SLIP_ACCEL));
    slide.startAnimation(a);
    slide.offsetLeftAndRight(getLeft() - slide.getLeft());
  }

  private static final double COMPLETION = 0.8;
  private static final int SLIP_MS = 1000;
  private static final float SLIP_ACCEL = 3.0f;

  public static interface Listener {
    abstract void onComplete();
  }
}
