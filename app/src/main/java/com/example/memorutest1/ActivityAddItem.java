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
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ActivityAddItem extends AppCompatActivity {

    private Bitmap image;
    private Database database;

    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        (ActivityResult result) -> {
            if(result.getResultCode() != RESULT_OK || result.getData() == null) return;

            Bundle bundle = result.getData().getExtras();

            image = (Bitmap)  bundle.get("data");
            ((ImageView) findViewById(R.id.img_my_image)).setImageBitmap(image);

            database.uploadCompressedImage(
                    image,
                    "a/image.png",
                    Bitmap.CompressFormat.PNG,
                    100);

        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = new Database();
        setContentView(R.layout.activity_add_item);

        findViewById(R.id.txt_retake).setOnClickListener(view -> takePicture());

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

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            activityResultLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {

        }
    }
}