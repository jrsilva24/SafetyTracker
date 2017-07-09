package com.safety.tracker.safetytracker;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.w3c.dom.Text;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by jsilv on 8/10/2016.
 */
public class Tracker extends Activity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor sensor;
    private int calibrationCounter;
    private float xCalibrationValue;
    private float yCalibrationValue;
    private float zCalibrationValue;
    private boolean calibrated;
    private StringBuilder stringbuilder;
    private int infractionCounter;
// adds 9 character string at beginning
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracker);

        calibrated = false;
        calibrationCounter = 0;
        xCalibrationValue = 0;
        yCalibrationValue = 0;
        zCalibrationValue = 0;
        stringbuilder = new StringBuilder();
        infractionCounter = 0;

        sensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            final float[] accelerationVector = event.values;

            float x = accelerationVector[0];
            float y = accelerationVector[1];
            float z = accelerationVector[2];

            if (!calibrated)
                calibrationSet(x, y, z);
            else
                analyzeValues(x - xCalibrationValue, y - yCalibrationValue, z - zCalibrationValue);


            TextView xText = (TextView) findViewById(R.id.x);
            TextView yText = (TextView) findViewById(R.id.y);
            TextView zText = (TextView) findViewById(R.id.z);
            xText.setText(x - xCalibrationValue + "");
            yText.setText(y - yCalibrationValue + "");
            zText.setText(z - zCalibrationValue + "");
        }
    }

    private void calibrationSet(float x, float y, float z) {
        xCalibrationValue += x;
        yCalibrationValue += y;
        zCalibrationValue += z;

        if (calibrationCounter++ == 100) {
            calibrated = true;
            xCalibrationValue = xCalibrationValue / 100;
            yCalibrationValue = yCalibrationValue / 100;
            zCalibrationValue = zCalibrationValue / 100;
            Toast.makeText(this, "Calibration Complete!",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void analyzeValues(float x, float y, float z) {
        boolean isInfraction = analyzeInfraction(x,y,z);
        if(isInfraction)
            registerInfraction();
    }
    private void registerInfraction() {
        Toast.makeText(this, "Infraction Occured!",
                Toast.LENGTH_LONG).show();
        infractionCounter++;
    }

    private boolean analyzeInfraction(float x, float y, float z){
        double force = calculateForce (x,y,z);
        return determineIfInfractionOccured(force);
    }

    private boolean determineIfInfractionOccured(double force){
        return force > 1.0;
    }

    private double calculateForce(float x,float y, float z){
        float xSquared = x * x;
        float ySquared = y * y;
        float zSquared = z * z;

        float axisValuesTotaled = xSquared + ySquared + zSquared;
        double rootValue = Math.pow(axisValuesTotaled, .5);

        return rootValue;
    }

    private void openFile() {
        try {
            // catches IOException below
            final String TESTSTRING = new String("Hello Android");

       /* We have to use the openFileOutput()-method
       * the ActivityContext provides, to
       * protect your file from others and
       * This is done for security-reasons.
       * We chose MODE_WORLD_READABLE, because
       *  we have nothing to hide in our file */
            FileOutputStream fOut = openFileOutput("samplefile.txt",
                    MODE_WORLD_READABLE);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);

            // Write the string to the file
            osw.write(TESTSTRING);

       /* ensure that everything is
        * really written out and close */
            osw.flush();
            osw.close();

//Reading the file back...

       /* We have to use the openFileInput()-method
        * the ActivityContext provides.
        * Again for security reasons with
        * openFileInput(...) */

            FileInputStream fIn = openFileInput("samplefile.txt");
            InputStreamReader isr = new InputStreamReader(fIn);

        /* Prepare a char-Array that will
         * hold the chars we read back in. */
            char[] inputBuffer = new char[TESTSTRING.length()];

            // Fill the Buffer with data from the file
            isr.read(inputBuffer);

            // Transform the chars to a String
            String readString = new String(inputBuffer);

            // Check if we read back the same chars that we had written out
            boolean isTheSame = TESTSTRING.equals(readString);

            Log.i("File Reading stuff", "success = " + isTheSame);

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Tracker Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.safety.tracker.safetytracker/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Tracker Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.safety.tracker.safetytracker/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}