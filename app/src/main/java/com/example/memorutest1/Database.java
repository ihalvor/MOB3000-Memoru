package com.example.memorutest1;

import android.graphics.Bitmap;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;


public class Database {

    private FirebaseStorage storage;
    private StorageReference storageReference;

    public Database() {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    public void uploadCompressedImage(Bitmap image, String address, Bitmap.CompressFormat format, int compression) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(format, compression, stream);
        storageReference.child(address).putBytes(stream.toByteArray());
    }

    public Task<Uri> downloadImage(String address) {
        return storageReference.child(address).getDownloadUrl();
    }

}
