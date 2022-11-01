package com.example.memorutest1;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Executor;

public class Database {

    Bitmap image;
    ByteArrayOutputStream stream = new ByteArrayOutputStream();

    public Database() {
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);

        FirebaseStorage storage = FirebaseStorage.getInstance();

        String path = "memoru/images/image.png";

        StorageReference storageRef = storage.getReference(path);

        UploadTask uploadTask = storageRef.putBytes(stream.toByteArray());

        StorageReference mountainsRef = storageRef.child("mountains.jpg");

        // Create a reference to 'images/mountains.jpg'
        StorageReference mountainImagesRef = storageRef.child("images/mountains.jpg");

        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
           @Override
           public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

            }
        });


    }


    /*
    // While the file names are the same, the references point to different files
            mountainsRef.getName().equals(mountainImagesRef.getName());    // true
            mountainsRef.getPath().equals(mountainImagesRef.getPath());    // false

    // Create a reference with an initial file path and name
    StorageReference pathReference = storageRef.child("images/stars.jpg");

    // Create a reference to a file from a Cloud Storage URI
    StorageReference gsReference = storage.getReferenceFromUrl("gs://bucket/images/stars.jpg");

    // Create a reference from an HTTPS URL
    // Note that in the URL, characters are URL escaped!
    StorageReference httpsReference = storage.getReferenceFromUrl("https://firebasestorage.googleapis.com/b/bucket/o/images%20stars.jpg");
     */
}
