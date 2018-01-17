package com.ariwilson.seismo;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class Help extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                         WindowManager.LayoutParams.FLAG_FULLSCREEN);
    FrameLayout layout = new FrameLayout(this);
    TextView help_view = new TextView(this);
    help_view.setTextSize(18);
    help_view.setBackgroundColor(0xFFFFFFFF);
    help_view.setTextColor(0xFF000000);
    help_view.setText(R.string.help);
    ScrollView scroll_view = new ScrollView(this);
    scroll_view.setBackgroundColor(0xFFFFFFFF);
    scroll_view.addView(help_view);
    layout.addView(scroll_view);
    setContentView(layout);
  }
}
