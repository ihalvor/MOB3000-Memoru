package com.example.memorutest1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class ActivityViewReceipt extends AppCompatActivity {

    public static final String EXTRA_URI_KEY = "EXTRA_URI_KEY";
    public static final String EXTRA_ITEM_NAME = "EXTRA_ITEM_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_receipt);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String name = intent.getStringExtra(EXTRA_ITEM_NAME);
        Uri uri = intent.getParcelableExtra(EXTRA_URI_KEY);

        if(name == null || uri == null) {
            Toast.makeText(this, "Could not view receipt", Toast.LENGTH_SHORT).show();
            finish();
        }

        ImageView imageView = findViewById(R.id.img_my_receipt);
        Picasso.get().load(uri).into(imageView);

        getSupportActionBar().setTitle(name + "'s receipt");
    }

    // Enable back button in action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}