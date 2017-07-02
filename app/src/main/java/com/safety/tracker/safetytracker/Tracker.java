package com.safety.tracker.safetytracker;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by jsilv on 8/10/2016.
 */
public class Tracker extends Activity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor sensor;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracker);

        sensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        } else {

        }

    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            final float[] accelerationVector = event.values;

            TextView x = (TextView)findViewById(R.id.x);
            TextView y = (TextView)findViewById(R.id.y);
            TextView z = (TextView)findViewById(R.id.z);

            x.setText(accelerationVector[0]+"");
            y.setText(accelerationVector[1]+"");
            z.setText(accelerationVector[2]+"");

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}