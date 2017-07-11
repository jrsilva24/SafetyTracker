package com.safety.tracker.safetytracker;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends AppCompatActivity {

    private Activity activity = this;
    private EditText name;
    private EditText phoneNumber;
    private Switch textSwitchEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        name = (EditText) findViewById(R.id.editText);
        phoneNumber = (EditText) findViewById(R.id.PhoneNumberInput);
        textSwitchEnabled = (Switch) findViewById(R.id.switchForTexting);

        if (textSwitchEnabled != null) {
            textSwitchEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Toast.makeText(activity, "The Switch is " + (isChecked ? "on" : "off"),
                            Toast.LENGTH_SHORT).show();
                    if(isChecked) {
                        //do stuff when Switch is ON
                    } else {
                        //do stuff when Switch if OFF
                    }
                }
            });
        }

        Button saveButton = (Button) findViewById(R.id.Save);
        saveButton.setOnClickListener(new View.OnClickListener()   {
            public void onClick(View v)  {
                try {
                    //TODO save things
                    name.getText();
                    phoneNumber.getText();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

}
