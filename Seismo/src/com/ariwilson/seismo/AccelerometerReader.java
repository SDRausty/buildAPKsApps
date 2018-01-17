package com.ariwilson.seismo;

import android.content.Context;
import android.hardware.Sensor; 
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AccelerometerReader { 
  public volatile float x = 0;
  public volatile float y = 0;
  public volatile float z = 0;

  public AccelerometerReader(Context ctx) 
      throws UnsupportedOperationException { 
    SensorManager sensor_manager = (SensorManager) ctx.getSystemService(
        Context.SENSOR_SERVICE);
    Sensor accelerometer = sensor_manager.getDefaultSensor(
        Sensor.TYPE_ACCELEROMETER);
    sensor_manager.registerListener(listener_, accelerometer,
    		                            SensorManager.SENSOR_DELAY_FASTEST); 
  }

  private SensorEventListener listener_ = new SensorEventListener(){
    public void onAccuracyChanged(Sensor arg0, int arg1) {}

    public void onSensorChanged(SensorEvent evt) {
      float vals[] = evt.values;
      
      if(evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
        x = vals[0];
        y = vals[1];
        z = vals[2];
      }
    }
  };
}
