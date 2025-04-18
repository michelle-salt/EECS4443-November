package com.example.novemberproject;

import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.Locale;

public class TrackingDataActivity extends AppCompatActivity {

    private TextView tvTrackingData;
    private TableLayout tableLayout;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_data);

        Toolbar toolbar = findViewById(R.id.toolbarTracking);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tracking Data");
        }

        tableLayout = findViewById(R.id.tableLayoutTracking);
        dbHelper = new DBHelper(this);

        // Query all data from DB
        TableRow header = new TableRow(this);
        String[] headers = {"Start (s)", "End (s)", "Total Time (s)", "Avg Time (s)", "Likes", "Pulsing", "Pulse Interval"};
        for (String h : headers) {
            TextView tv = new TextView(this);
            tv.setText(h);
            tv.setPadding(8, 8, 8, 8);
            header.addView(tv);
        }
        tableLayout.addView(header);

        // Add data rows
        Cursor cursor = dbHelper.getAllTrackingData();
        while (cursor.moveToNext()) {
            long sessionStartMs = cursor.getLong(cursor.getColumnIndexOrThrow("session_start"));
            long sessionEndMs = cursor.getLong(cursor.getColumnIndexOrThrow("session_end"));
            long totalSessionMs = cursor.getLong(cursor.getColumnIndexOrThrow("total_session_time"));
            long avgTimeMs = cursor.getLong(cursor.getColumnIndexOrThrow("avg_time_per_post"));
            int likes = cursor.getInt(cursor.getColumnIndexOrThrow("total_likes"));
            int pulsingOn = cursor.getInt(cursor.getColumnIndexOrThrow("pulsing_on"));
            int pulseInterval = cursor.getInt(cursor.getColumnIndexOrThrow("pulse_interval"));

            double sessionStartSec = sessionStartMs / 1000.0;
            double sessionEndSec = sessionEndMs / 1000.0;
            double totalSessionSec = totalSessionMs / 1000.0;
            double avgTimeSec = avgTimeMs / 1000.0;

            TableRow row = new TableRow(this);
            String[] rowData = {
                    String.format(Locale.getDefault(), "%.2f", sessionStartSec),
                    String.format(Locale.getDefault(), "%.2f", sessionEndSec),
                    String.format(Locale.getDefault(), "%.2f", totalSessionSec),
                    String.format(Locale.getDefault(), "%.2f", avgTimeSec),
                    String.valueOf(likes),
                    (pulsingOn == 1 ? "true" : "false"),
                    String.valueOf(pulseInterval)
            };

            for (String cell : rowData) {
                TextView tv = new TextView(this);
                tv.setText(cell);
                tv.setPadding(8, 8, 8, 8);
                row.addView(tv);
            }
            tableLayout.addView(row);
        }
        cursor.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
