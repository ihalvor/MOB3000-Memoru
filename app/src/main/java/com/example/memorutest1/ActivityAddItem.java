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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.squareup.picasso.Picasso;

public class ActivityAddItem extends AppCompatActivity {

    private Bitmap image;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        (ActivityResult result) -> {
            if(result.getResultCode() != RESULT_OK || result.getData() == null) return;

            Bundle bundle = result.getData().getExtras();

            image = (Bitmap)  bundle.get("data");
            ((ImageView) findViewById(R.id.img_my_image)).setImageBitmap(image);

            Database.getInstance().uploadCompressedImage(
                    image,
                    "a/image.png",
                    Bitmap.CompressFormat.PNG,
                    100);

        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkForUser();
        Database database = Database.getInstance();

        setContentView(R.layout.activity_add_item);

        findViewById(R.id.txt_retake).setOnClickListener((View view) -> takePicture());

        findViewById(R.id.btn_save).setOnClickListener((View view) -> uploadItem());

        /*database.downloadImage("a/image.png").addOnCompleteListener(uriTask -> {
                try {
                    ImageView imageView = findViewById(R.id.img_my_image);
                    Picasso.get().load(uriTask.getResult().toString()).into(imageView);
                } catch(Exception e) {
                    Log.e("123 test", "onCreate: " + e.toString());
                }
        });*/

        takePicture();
    }

    private void checkForUser() {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if(user != null) {

        } else {
            finish();
            startActivity(new Intent(this, ActivityLogIn.class));
        }
    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            activityResultLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {

        }
    }

    private void uploadItem() {
        String name = ((EditText) findViewById(R.id.edt_name)).getText().toString();
        String location = ((EditText) findViewById(R.id.edt_location)).getText().toString();
        String description = ((EditText) findViewById(R.id.edt_description)).getText().toString();
        String userID = user.getUid();

        Database database = Database.getInstance();

        database.uploadItem(name, location, description, userID)
                .addOnSuccessListener(this, (DocumentReference reference) -> {
                    database.uploadCompressedImage(
                            image,
                            Database.findImageAddress(userID, reference.getId(), Database.ImageType.ITEM),
                            Bitmap.CompressFormat.PNG,
                            100)
                            .addOnSuccessListener(this, s -> {
                                Toast.makeText(this, "Item uploaded", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(this, a -> {
                    Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                });
    }

}