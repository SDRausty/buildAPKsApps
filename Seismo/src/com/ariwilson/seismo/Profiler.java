package com.ariwilson.seismo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import android.util.Log;

public class Profiler {
  public void start() {
    time_ = new Date().getTime();
    events_ = new HashMap<String, ProfilerRecord>();
  }

  public synchronized void record(String event) {
    ProfilerRecord record;
    long new_time = new Date().getTime();
    if (events_.containsKey(event)) {
      record = events_.get(event);
    } else {
      record = new ProfilerRecord();
    }
    record.sum_time += new_time - time_;
    ++record.num_events;
    events_.put(event, record);
    time_ = new_time;
  }

  public synchronized void print() {
    long total_time = 0;
    for (Entry<String, ProfilerRecord> event : events_.entrySet()) {
      Log.i("Profiler", event.getKey() + ": " +
                        Long.toString(event.getValue().sum_time) + ", " +
                        Long.toString(event.getValue().num_events));
      total_time += event.getValue().sum_time;
    }
    Log.i("Profiler", "Total time: " + Long.toString(total_time));
  }

  private class ProfilerRecord {
    public long sum_time = 0;
    public long num_events = 0;
  }

  private long time_;
  private HashMap<String, ProfilerRecord> events_;
}
