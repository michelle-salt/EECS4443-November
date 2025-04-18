package com.example.novemberproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {

    private Switch pulsatingSwitch;
    private SeekBar pulseIntervalSeekBar;
    private TextView tvPulseIntervalValue;
    private Button btnViewTracking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Tool bar at the top
        Toolbar toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        pulsatingSwitch = findViewById(R.id.switchPulsating);
        pulseIntervalSeekBar = findViewById(R.id.seekBarPulseInterval);
        tvPulseIntervalValue = findViewById(R.id.tvPulseIntervalValue);
        btnViewTracking = findViewById(R.id.btnViewTracking);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = prefs.edit();

        boolean pulsatingEnabled = prefs.getBoolean("pulsing_enabled", false);
        int pulseInterval = prefs.getInt("pulse_interval", 5);
        pulsatingSwitch.setChecked(pulsatingEnabled);
        pulseIntervalSeekBar.setProgress(pulseInterval);
        tvPulseIntervalValue.setText(String.valueOf(pulseInterval));

        pulsatingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("pulsing_enabled", isChecked);
                editor.apply();
            }
        });

        pulseIntervalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = Math.max(1, progress);
                tvPulseIntervalValue.setText(String.valueOf(value));
                editor.putInt("pulse_interval", value);
                editor.apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        btnViewTracking.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, TrackingDataActivity.class));
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
