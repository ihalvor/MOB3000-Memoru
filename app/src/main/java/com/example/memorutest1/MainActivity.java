package com.example.memorutest1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_new_item).setOnClickListener(view -> {
            startActivity(new Intent(this, ActivityAddItem.class));
        });

        findViewById(R.id.btn_my_item).setOnClickListener(view -> {
            startActivity(new Intent(this, ActivityMyItems.class));
        });

        findViewById(R.id.btn_settings).setOnClickListener(view -> {
            startActivity(new Intent(this, ActivitySettings.class));
        });
    }
}