package com.safety.tracker.safetytracker;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jsilv on 8/10/2016.
 */
public class Tracker extends Activity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor sensor;
    private int calibrationCounter;
    private float CalibrationValue;
    private boolean calibrated;
    private double force;
    private StringBuilder stringbuilder;
    private int infractionCounter;
    private SensorEventListener sensorEventListener;
    private static final int PERMISSION_REQUEST_CODE = 1;
// adds 9 character string at beginning
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private ProgressDialog progressDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracker);

        ImageView backgroundImg = (ImageView) findViewById(R.id.imageView);
        backgroundImg.setBackgroundColor(Color.rgb(255, 255, 255));

        calibrated = false;
        calibrationCounter = 0;
        CalibrationValue = 0;
        sensorEventListener = this;

        stringbuilder = new StringBuilder();
        infractionCounter = 0;

        calibrateDialogShow();

        sensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        }

        Button endTrip = (Button)findViewById(R.id.angry_btn);
        endTrip.setOnClickListener(new View.OnClickListener()   {
            public void onClick(View v)  {
                try {
                    endTripAndGenerateReport();
                    sensorManager.unregisterListener(sensorEventListener,sensor);
                    finish();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void calibrateDialogShow(){


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please keep phone still"); // Setting Message
        progressDialog.setTitle("Calibrating..."); // Setting Title
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); // Progress Dialog Style Spinner
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        new Thread(new Runnable() {
            public void run() {
                try {
                    while (calibrationCounter < 100 )
                        progressDialog.setProgress(calibrationCounter);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }
        }).start();
/*
        progress=new ProgressDialog(this);
        progress.setMessage("Downloading Music");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(true);
        progress.setProgress(0);
        progress.show();

        final int totalProgressTime = 100;
        final Thread t = new Thread() {
            @Override
            public void run() {
                int jumpTime = 0;

                while(jumpTime < totalProgressTime) {
                    try {
                        sleep(200);
                        jumpTime += 5;
                        progress.setProgress(jumpTime);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();

        */
    }
    private void endTripAndGenerateReport(){
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd @ HH:mm");
        String currentDateandTime = sdf.format(new Date());
        String summary = "Trip Summary : Total number of infractions made on " +currentDateandTime+ " were " + infractionCounter;
       // openFile(summary);
        SmsManager smsManager = SmsManager.getDefault();
        askPermisions();
        smsManager.sendTextMessage("9092007064", null, summary, null, null);
    }

    private void askPermisions(){

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_DENIED) {

                Log.d("permission", "permission denied to SEND_SMS - requesting it");
                String[] permissions = {Manifest.permission.SEND_SMS};

                requestPermissions(permissions, PERMISSION_REQUEST_CODE);

            }
        }
    }
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            final float[] accelerationVector = event.values;

            float x = accelerationVector[0];
            float y = accelerationVector[1];
            float z = accelerationVector[2];

            if (!calibrated) {
                calibrationSet(x, y, z);
                if(calibrationCounter % 20 == 0){
                    TextView forceTextThatIsCalbrationNow = (TextView) findViewById(R.id.force);
                    forceTextThatIsCalbrationNow.setText(forceTextThatIsCalbrationNow.getText() +".");
                }
            }
            else
                analyzeValues(x, y , z );

        }
    }

    private void calibrationSet(float x, float y, float z) {
        calibrationCounter++;
        CalibrationValue += calculateForce(x,y,z);

        if (calibrationCounter == 100) {
            calibrated = true;
            CalibrationValue = CalibrationValue / 100;
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
                Toast.LENGTH_SHORT).show();
        infractionCounter++;
    }

    private boolean analyzeInfraction(float x, float y, float z){
        force = calculateForce (x,y,z) - CalibrationValue;
        TextView forceText = (TextView) findViewById(R.id.force);
        forceText.setText(force + " m/s");
        return determineIfInfractionOccured(force);
    }

    private boolean determineIfInfractionOccured(double force){
        return force > 5.0;
    }

    private double calculateForce(  float x,float y, float z){
        float xSquared = x * x;
        float ySquared = y * y;
        float zSquared = z * z;

        float axisValuesTotaled = xSquared + ySquared + zSquared;
        double rootValue = Math.pow(axisValuesTotaled, .5);

        return rootValue;
    }

    private void openFile(String summary) {
        try {
            // catches IOException below
            final String TESTSTRING = new String(summary);

       /* We have to use the openFileOutput()-method
       * the ActivityContext provides, to
       * protect your file from others and
       * This is done for security-reasons.
       * We chose MODE_WORLD_READABLE, because
       *  we have nothing to hide in our file */
            FileOutputStream fOut = openFileOutput("safetyTracker.txt",
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

            FileInputStream fIn = openFileInput("safetyTracker.txt");
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