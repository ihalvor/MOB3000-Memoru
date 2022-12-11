package com.example.memorutest1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private boolean welcomeUser;
    private boolean justCreated = false;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);

        checkForUser();
        if(checkForJustLoggedIn()) {
            Toast.makeText(this, "Signed in as " + user.getEmail(), Toast.LENGTH_SHORT).show();
        }

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

    /**
     * Checks if the user has just logged in, or is returning from another activity
     * @return whether the user has just logged in.
     */
    private boolean checkForJustLoggedIn() {
        return getIntent().getBooleanExtra(ActivityLogIn.FROM_LOGIN_INTENT, false);
    }

    /**
     * Throw the user out if they are not logged in
     */
    private void checkForUser() {

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

            if(user != null) {

        } else {
            finish();
            startActivity(new Intent(this, ActivityLogIn.class));
        }
    }
}