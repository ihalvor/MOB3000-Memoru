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
import java.util.concurrent.Executor;

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

    private boolean newItemImage = false;
    private boolean newReceiptImage = false;

    // Activity launcher for taking an image for the receipt. Result is the image
    private ActivityResultLauncher<Intent> receiptImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (ActivityResult result) -> {
                if(result.getResultCode() != RESULT_OK || result.getData() == null) return;
                Bundle bundle = result.getData().getExtras();

                receiptImage = (Bitmap) bundle.get("data");
                ((ImageView) findViewById(R.id.img_my_receipt)).setImageBitmap(receiptImage);
                newReceiptImage = true;
            });

    // Activity launcher for taking an image for the item. Result is the image
    private ActivityResultLauncher<Intent> itemImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (ActivityResult result) -> {
                if(result.getResultCode() != RESULT_OK || result.getData() == null) return;

                Bundle bundle = result.getData().getExtras();

                image = (Bitmap) bundle.get("data");
                ((ImageView) findViewById(R.id.img_my_image)).setImageBitmap(image);
                newItemImage = true;
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setTitle("Add Item");
        super.onCreate(savedInstanceState);
        // enabling the back button in the ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        checkForUser();
        database = Database.getInstance();

        setContentView(R.layout.activity_add_item);

        findViewById(R.id.txt_retake).setOnClickListener((View view) -> takePicture());
        findViewById(R.id.txt_add_receipt).setOnClickListener((View view) -> takeReceiptPicture());


        intent = getIntent();
        // If the item is being edited
        if(intent.hasExtra("itemID")) {
            getSupportActionBar().setTitle("Edit item");
            itemID = intent.getStringExtra("itemID");

            findViewById(R.id.btn_save).setOnClickListener((View view) -> updateItem());
            // Downloading the item and displaying it
            database.downloadUserItem(userID, itemID)
                    .addOnSuccessListener((DocumentSnapshot document) -> {
                        item = document.getData();
                        // Set title of ActionBar to name of the item
                        getSupportActionBar().setTitle("Edit " + item.get("name"));
                        displayItem();
                    })
                    .addOnFailureListener((Exception e) -> {
                        setResult(RESULT_CANCELED, new Intent());
                        finish();
                    });
        // If the item is new and being added
        } else {
            getSupportActionBar().setTitle("Add new item");
            findViewById(R.id.btn_save).setOnClickListener((View view) -> uploadItem());
            takePicture();
        }
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

    /**
     * Force the user out if they are not signed in
     */
    private void checkForUser() {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        // Checks if a user is logged in, if not finish the activity
        if(user != null) {
            userID = user.getUid();
        } else {
            finish();
            startActivity(new Intent(this, ActivityLogIn.class));
        }
    }

    /**
     * Launch the camera to take a picture of the receipt
     */
    private void takeReceiptPicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Launching camera
        try {
            receiptImageLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            String errorMsg = "Could not take receipt image";
            Log.e(TAG, errorMsg);
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Launch the camera to take a picture of the item
     */
    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Launching camera
        try {
            itemImageLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            String errorMsg = "Could not take item image";
            Log.e(TAG, errorMsg);
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Display the item. Used if we are editing it
     */
    private void displayItem() {
        // Get information about the item and displays it
        String name         = item.get("name").toString();
        String location     = item.get("location").toString();
        String description  = item.get("description").toString();

        ((EditText) findViewById(R.id.edt_name)).setText(name);
        ((EditText) findViewById(R.id.edt_location)).setText(location);
        ((EditText) findViewById(R.id.edt_description)).setText(description);

        // Downloading item image
        database.downloadImage(Database.findImageAddress(userID, itemID, Database.ImageType.ITEM))
                .addOnSuccessListener((Uri imageUri) -> {
                    ImageView imageView = findViewById(R.id.img_my_image);
                    Picasso.get().load(imageUri).into(imageView);

                    // Downloading receipt image
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

    /**
     * Upload changes to the item to the database and storage
     */
    private void updateItem() {
        // Get new information about the item
        String name             = ((EditText) findViewById(R.id.edt_name)).getText().toString();
        String location         = ((EditText) findViewById(R.id.edt_location)).getText().toString();
        String description      = ((EditText) findViewById(R.id.edt_description)).getText().toString();

        // Get old information about the item
        String oldName          = item.get("name").toString();
        String oldLocation      = item.get("location").toString();
        String oldDescription   = item.get("description").toString();

        // If the new information is different from the old, update item
        if(name != oldName) database.updateItem(userID, itemID,"name", name);
        if(location != oldLocation) database.updateItem(userID, itemID,"location", location);
        if(description != oldDescription) database.updateItem(userID, itemID,"description", description);

        // Upload new photo
        if(newItemImage) {
            database.uploadCompressedImage(
                image,
                Database.findImageAddress(userID, itemID, Database.ImageType.ITEM),
                Bitmap.CompressFormat.PNG,
                100)
                    .addOnSuccessListener(_result -> {
                        if(newReceiptImage) {
                            database.uploadCompressedImage(image,
                                Database.findImageAddress(userID, itemID, Database.ImageType.RECEIPT),
                                Bitmap.CompressFormat.PNG,
                                100)
                                    .addOnFailureListener((Exception e) -> {
                                        Toast.makeText(this, "Failed to upload receipt", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnSuccessListener(_result2 -> finishUpdatingItem());
                        } else {
                            finishUpdatingItem();
                        }
                    })
                    .addOnFailureListener((Exception e) -> {
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });

            // Upload new receipt
        } else if(newReceiptImage) {
            database.uploadCompressedImage(
                receiptImage,
                Database.findImageAddress(userID, itemID, Database.ImageType.RECEIPT),
                Bitmap.CompressFormat.PNG,
                100)
                    .addOnFailureListener((Exception e) -> {
                        Toast.makeText(this, "Failed to upload receipt", Toast.LENGTH_SHORT).show();
                    })
                    .addOnSuccessListener(_result -> finishUpdatingItem());
        } else {
            finishUpdatingItem();
        }
    }

    /**
     * If we have updated an existing item, set result to edited, so the other views can refresh
     * the data
     */
    private void finishUpdatingItem() {
        setResult(RESULT_OK, new Intent().putExtra("edit", true));
        finish();
    }

    /**
     * Upload an item to the database, and the images to the cloud storage
     */
    private void uploadItem() {
        // Get information about new item
        String name = ((EditText) findViewById(R.id.edt_name)).getText().toString();
        String location = ((EditText) findViewById(R.id.edt_location)).getText().toString();
        String description = ((EditText) findViewById(R.id.edt_description)).getText().toString();
        String userID = user.getUid();

        Database database = Database.getInstance();
        // Upload Item to database
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