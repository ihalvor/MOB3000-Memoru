package com.example.memorutest1;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ActivitySettings extends AppCompatActivity {

    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setTitle("Settings");
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_settings);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) finish();

        findViewById(R.id.btn_faq).setOnClickListener(view -> {
            startActivity(new Intent(this, ActivityFAQ.class));
        });

        findViewById(R.id.btn_license).setOnClickListener(view -> {
            startActivity(new Intent(this, ActivityLicense.class));
        });

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, ActivityLogIn.class));
                    finish();
                })
                .setNegativeButton("No", (dialogInterface, i) -> {});

        findViewById(R.id.btn_log_out).setOnClickListener(view -> {
            dialogBuilder.show();
        });

    }

    // Enable the back button for the action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}