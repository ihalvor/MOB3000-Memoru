package com.example.memorutest1;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class ActivityViewItem extends AppCompatActivity {

    private FirebaseUser user;
    private Database database;
    private String itemID;
    private String userID;

    private ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (ActivityResult result) -> {
                if(result.getResultCode() == RESULT_OK) {
                    downloadItem();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_item);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        user = FirebaseAuth.getInstance().getCurrentUser();

        

        if(user == null) {
            setResult(RESULT_CANCELED);
            finish();
        }

        database = Database.getInstance();
        itemID = getIntent().getStringExtra("itemID");
        userID = user.getUid();

        findViewById(R.id.btn_edit).setOnClickListener((View view) -> {
            Intent editIntent = new Intent(this, ActivityAddItem.class)
                    .putExtra("itemID", itemID);
            editLauncher.launch(editIntent);
        });

        downloadItem();
    }

    private void downloadItem() {

        database.downloadUserItem(userID, itemID)
                .addOnSuccessListener((DocumentSnapshot snapshot) -> {
                    Map<String, Object> item = snapshot.getData();

                    database.downloadImage(Database.findImageAddress(
                                    userID, itemID, Database.ImageType.ITEM))
                            .addOnSuccessListener((Uri imageUri) -> {
                                displayItemData(item, imageUri);

                                // Get receipt
                                database.downloadImage(Database.findImageAddress(
                                                userID, itemID, Database.ImageType.RECEIPT))
                                        .addOnSuccessListener((Uri receiptUri) -> {
                                            displayReceipt(receiptUri, item.get("name").toString());
                                        });
                            })
                            .addOnFailureListener((Exception e) -> {
                                Toast.makeText(this,
                                        "No image found",
                                        Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener((Exception e) -> {
                    Toast.makeText(this, "No item found", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {

            Intent intent = getIntent();
            boolean edited = false;
            if(intent.hasExtra("edit")) {
                edited = intent.getBooleanExtra("edit", false);
            }

            setResult(RESULT_OK, new Intent().putExtra("edit", edited));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayReceipt(Uri receiptUri, String itemName) {
        ImageView imageView = findViewById(R.id.img_my_receipt);
        imageView.setVisibility(View.VISIBLE);

        TextView label = findViewById(R.id.txt_receipt_label);
        label.setVisibility(View.VISIBLE);

        Picasso.get()
                .load(receiptUri)
                .resize(300, 300)
                .centerCrop()
                .into(imageView);

        // View larger receipt
        imageView.setOnClickListener((View view) -> inspectReceipt(receiptUri, itemName));
        label.setOnClickListener((View view) -> inspectReceipt(receiptUri, itemName));
    }
    
    private void inspectReceipt(Uri receiptUri, String itemName) {
        Intent intent = new Intent(this, ActivityViewReceipt.class)
                .putExtra(ActivityViewReceipt.EXTRA_ITEM_NAME, itemName)
                .putExtra(ActivityViewReceipt.EXTRA_URI_KEY, receiptUri);

        startActivity(intent);
    }

    private void displayItemData(Map<String, Object> item, Uri imageUri) {

        String name         = item.get("name").toString();
        String location     = item.get("location").toString();
        String description  = item.get("description").toString();

        ImageView imageView = findViewById(R.id.img_my_image);
        Picasso.get().load(imageUri).into(imageView);

        getSupportActionBar().setTitle(name);
        ((TextView) findViewById(R.id.txt_location)).setText(location);
        ((TextView) findViewById(R.id.txt_desc)).setText(description);
    }
}