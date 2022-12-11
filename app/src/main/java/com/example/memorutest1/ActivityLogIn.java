package com.example.memorutest1;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class ActivityLogIn extends AppCompatActivity {

    private static final String TAG = "LOGIN";
    public static final String FROM_LOGIN_INTENT = "fromLogin";

    // One tap sign in/ sign up
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private BeginSignInRequest signUpRequest;

    // For when the user is not signed in to any google account on the device
    private GoogleSignInOptions gSignInOptions;
    private GoogleSignInClient gSignInClient;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    // Booleans to make sure we only prompt the user to login once
    private boolean showOneTapUI = true;
    private boolean signedUpWithGoogle = false;

    // Activity launcher for oneTapSignIn and oneTapSignUp
    private ActivityResultLauncher<IntentSenderRequest> oneTapLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            (ActivityResult result) -> {
                try {
                    String idToken = oneTapClient
                            .getSignInCredentialFromIntent(result.getData())
                            .getGoogleIdToken();

                    if(idToken != null) signInWithCredentials(idToken);

                } catch(ApiException e) {
                    switch(e.getStatusCode()) {

                        case CommonStatusCodes.CANCELED:
                            // Canceled
                            showSignInButton();
                            break;

                        case CommonStatusCodes.NETWORK_ERROR:
                            // Network error on device
                            showSignInButton();
                            break;

                        default:
                            // All other errors
                            showSignInButton();
                            Log.e(TAG, ": " + e.toString());
                            break;
                    }
                }
            });

    // Activity launcher for the old google sign in
    private ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (ActivityResult result) -> {

                String idToken = GoogleSignIn
                        .getLastSignedInAccount(getApplicationContext())
                        .getIdToken();

                if(idToken != null) {
                    signInWithCredentials(idToken);
                } else {
                    // The user did not sign. Show login
                    showSignInButton();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mAuth = FirebaseAuth.getInstance();
        setFirebaseUser();

        oneTapClient = Identity.getSignInClient(this);

        // Sign in request with .setFilterByAuthorizedAccounts(true)
        // in case the user is singed into multiple android users on the device,
        // this will only shows those who are already registered for this app
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest
                        .GoogleIdTokenRequestOptions
                        .builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .setFilterByAuthorizedAccounts(true)
                        .build())
                .setAutoSelectEnabled(false)
                .build();

        // Start tge sign in process if showOneTapUI is still enabled. If we create this
        // activity while already signed in, this would be set to false
        if(showOneTapUI) {
            oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener(this, (BeginSignInResult result) -> {
                        oneTapLauncher.launch(new IntentSenderRequest
                                .Builder(result.getPendingIntent()
                                .getIntentSender())
                                .build());
                    })
                    .addOnFailureListener(this, (@NonNull Exception e) -> {

                        // The first sign in attempt failed. There could be multiple reasons
                        // for this. One such case is that the user has not yet logged into
                        // the application before, and the .setFilterByAuthorizedAccounts(true)
                        // prevented the user from signing in with new accounts. Therefore we need
                        // to try signing up for the app, this time setting
                        // .setFilterByAuthorizedAccounts(false)
                        signUpRequest = BeginSignInRequest.builder()
                                .setGoogleIdTokenRequestOptions(BeginSignInRequest
                                        .GoogleIdTokenRequestOptions
                                        .builder()
                                        .setSupported(true)
                                        .setServerClientId(
                                                getString(R.string.default_web_client_id))
                                        .setFilterByAuthorizedAccounts(false)
                                        .build())
                                .setAutoSelectEnabled(false)
                                .build();

                        oneTapClient.beginSignIn(signUpRequest)
                                .addOnSuccessListener(this, (BeginSignInResult result) -> {
                                    oneTapLauncher.launch(new IntentSenderRequest
                                            .Builder(result.getPendingIntent()
                                            .getIntentSender())
                                            .build());
                                })
                                .addOnFailureListener(this, (@NonNull Exception e2) -> {

                                    // If this signup also fails, this could be because the user
                                    // is not signed into any google account on the android device
                                    // Here we have to call the old google sign in API and use
                                    // it to get a FirebaseUser later
                                    signIntoGoogleOnDevice();
                                });
                    });
        }
    }

    /**
     * If firebase user is signed in, continue to the menu
     */
    private void setFirebaseUser() {
        user = mAuth.getCurrentUser();
        if(user != null) {
            showOneTapUI = false;
            // Launch new activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("fromLogin", true);
            finish();
            startActivity(intent);
        }
    }

    /**
     * Sign into the firebase auth with google credentials
     * @param idToken unique google ID token
     */
    private void signInWithCredentials(String idToken) {
        AuthCredential fireCredential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(fireCredential).addOnCompleteListener(
                this,
                (@NonNull Task<AuthResult> task) -> {
                    if(task.isSuccessful()) {
                        setFirebaseUser();
                    }
                });
    }

    /**
     * Sign in with the old google login API if the user is not signed in to any google accounts
     * on the device.
     */
    private void signIntoGoogleOnDevice() {
        if(signedUpWithGoogle) return;
        else signedUpWithGoogle = true;

        gSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestServerAuthCode(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        gSignInClient = GoogleSignIn.getClient(this, gSignInOptions);
        googleSignInLauncher.launch(gSignInClient.getSignInIntent());

    }

    /**
     * If the user does not log in, display a button they can press to log in
     */
    private void showSignInButton() {
        Button loginButton = findViewById(R.id.google_login_button);

        loginButton.setVisibility(View.VISIBLE);
        loginButton.setOnClickListener((View view) -> {
            signedUpWithGoogle = false;
            signIntoGoogleOnDevice();
        });
    }
}