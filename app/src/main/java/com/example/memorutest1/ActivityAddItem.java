package com.example.memorutest1;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class ActivityAddItem extends AppCompatActivity {

    public static final String TAG = "ADD_ITEM";

    private Bitmap image;
    private Bitmap receiptImage;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Database database;

    private Intent intent;
    private Map<String, Object> item;

    private String itemID;
    private String userID;

    private ActivityResultLauncher<Intent> receiptImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (ActivityResult result) -> {
                if(result.getResultCode() != RESULT_OK || result.getData() == null) return;

                Bundle bundle = result.getData().getExtras();

                receiptImage = (Bitmap)  bundle.get("data");
                ((ImageView) findViewById(R.id.img_my_receipt)).setImageBitmap(image);
            });

    private ActivityResultLauncher<Intent> itemImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (ActivityResult result) -> {
                if(result.getResultCode() != RESULT_OK || result.getData() == null) return;

                Bundle bundle = result.getData().getExtras();

                image = (Bitmap)  bundle.get("data");
                ((ImageView) findViewById(R.id.img_my_image)).setImageBitmap(image);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setTitle("Add Item");
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        checkForUser();
        database = Database.getInstance();

        setContentView(R.layout.activity_add_item);

        findViewById(R.id.txt_retake).setOnClickListener((View view) -> takePicture());
        findViewById(R.id.txt_add_receipt).setOnClickListener((View view) -> takeReceiptPicture());


        intent = getIntent();

        if(intent.hasExtra("itemID")) {
            itemID = intent.getStringExtra("itemID");

            findViewById(R.id.btn_save).setOnClickListener((View view) -> updateItem());

            database.downloadUserItem(userID, itemID)
                    .addOnSuccessListener((DocumentSnapshot document) -> {
                        item = document.getData();
                        displayItem();
                    })
                    .addOnFailureListener((Exception e) -> {
                        finish();
                    });

        } else {
            findViewById(R.id.btn_save).setOnClickListener((View view) -> uploadItem());
            takePicture();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkForUser() {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if(user != null) {
            userID = user.getUid();
        } else {
            finish();
            startActivity(new Intent(this, ActivityLogIn.class));
        }
    }

    private void takeReceiptPicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            receiptImageLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            String errorMsg = "Could not take receipt image";
            Log.e(TAG, errorMsg);
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            itemImageLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            String errorMsg = "Could not take item image";
            Log.e(TAG, errorMsg);
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    }

    private void displayItem() {
        String name         = item.get("name").toString();
        String location     = item.get("location").toString();
        String description  = item.get("description").toString();

        ((EditText) findViewById(R.id.edt_name)).setText(name);
        ((EditText) findViewById(R.id.edt_location)).setText(location);
        ((EditText) findViewById(R.id.edt_description)).setText(description);

        database.downloadImage(Database.findImageAddress(userID, itemID, Database.ImageType.ITEM))
                .addOnSuccessListener((Uri imageUri) -> {
                    ImageView imageView = findViewById(R.id.img_my_image);
                    Picasso.get().load(imageUri).into(imageView);

                    // receipt image
                    database.downloadImage(Database
                            .findImageAddress(userID, itemID, Database.ImageType.RECEIPT))
                            .addOnSuccessListener((Uri receiptUri) -> {
                                ImageView receiptView = findViewById(R.id.img_my_receipt);
                                Picasso.get()
                                        .load(receiptUri)
                                        .resize(150, 150)
                                        .centerCrop()
                                        .into(receiptView);
                            });
                })
                .addOnFailureListener((Exception e) -> {
                    Toast.makeText(this, "Could not load image", Toast.LENGTH_SHORT)
                            .show();
                });
    }

    private void updateItem() {
        String name             = ((EditText) findViewById(R.id.edt_name)).getText().toString();
        String location         = ((EditText) findViewById(R.id.edt_location)).getText().toString();
        String description      = ((EditText) findViewById(R.id.edt_description)).getText().toString();

        String oldName          = item.get("name").toString();
        String oldLocation      = item.get("location").toString();
        String oldDescription   = item.get("description").toString();

        if(name != oldName) database.updateItem(userID, itemID,"name", name);
        if(location != oldLocation) database.updateItem(userID, itemID,"location", location);
        if(description != oldDescription) database.updateItem(userID, itemID,"description", description);

        finish();
    }

    private void uploadItem() {
        String name = ((EditText) findViewById(R.id.edt_name)).getText().toString();
        String location = ((EditText) findViewById(R.id.edt_location)).getText().toString();
        String description = ((EditText) findViewById(R.id.edt_description)).getText().toString();
        String userID = user.getUid();

        Database database = Database.getInstance();

        database.uploadItem(name, location, description, userID)
                .addOnSuccessListener(this, (DocumentReference reference) -> {
                    // Upload item image
                    if(newItemImage) {
                        database.uploadCompressedImage(
                                        image,
                                        Database.findImageAddress(userID, reference.getId(), Database.ImageType.ITEM),
                                        Bitmap.CompressFormat.PNG,
                                        100)
                                .addOnSuccessListener(this, s -> {
                                    Toast.makeText(this, "Item uploaded", Toast.LENGTH_SHORT).show();

                                    // Upload receipt
                                    if(newReceiptImage) {
                                        database.uploadCompressedImage(
                                                        receiptImage,
                                                        Database.findImageAddress(
                                                                userID,
                                                                reference.getId(),
                                                                Database.ImageType.RECEIPT),
                                                        Bitmap.CompressFormat.PNG,
                                                        100)
                                                .addOnFailureListener((Exception e) -> {
                                                    Log.e(TAG, e.toString());

                                                    Toast.makeText(
                                                                    this,
                                                                    "Could not upload receipt",
                                                                    Toast.LENGTH_SHORT)
                                                            .show();
                                                })
                                                .addOnSuccessListener(_s -> finish());
                                    } else {
                                        finish();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Item uploaded", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(this, a -> {
                    Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                });
    }

}