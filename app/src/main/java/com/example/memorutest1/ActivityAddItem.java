package com.example.memorutest1;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;

public class ActivityAddItem extends AppCompatActivity {

    private Bitmap image;
    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        (ActivityResult result) -> {
            if(result.getResultCode() != RESULT_OK || result.getData() == null) return;

            Bundle bundle = result.getData().getExtras();

            image = (Bitmap)  bundle.get("data");
            ((ImageView) findViewById(R.id.img_my_image)).setImageBitmap(image);
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        findViewById(R.id.txt_retake).setOnClickListener(view -> takePicture());

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