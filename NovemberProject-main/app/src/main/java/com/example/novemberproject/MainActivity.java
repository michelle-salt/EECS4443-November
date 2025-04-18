package com.example.novemberproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    String[] hashtags = {"nature", "technology", "art", "travel", "food", "sports", "music", "fashion", "science", "history"};
    ListView hashtagListView;
    Button btnViewFeed;
    Button btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hashtagListView = findViewById(R.id.hashtagListView);
        btnViewFeed = findViewById(R.id.btnViewFeed);
        btnSettings = findViewById(R.id.btnSettings);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice, hashtags);
        hashtagListView.setAdapter(adapter);
        hashtagListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        btnViewFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> selectedTags = new ArrayList<>();
                for (int i = 0; i < hashtagListView.getCount(); i++) {
                    if (hashtagListView.isItemChecked(i)) {
                        selectedTags.add(hashtags[i]);
                    }
                }
                if (selectedTags.isEmpty()) {
                    selectedTags.add(hashtags[0]);
                }
                Intent intent = new Intent(MainActivity.this, FeedActivity.class);
                intent.putStringArrayListExtra("selectedTags", selectedTags);
                startActivity(intent);
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
    }
}
