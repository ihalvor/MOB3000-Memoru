package com.example.memorutest1;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Database {

    public enum ImageType {
        MAIN_IMAGE, RECIEEPT_IMAGE
    }

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseFirestore firestore;

    public Database() {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        firestore = FirebaseFirestore.getInstance();
    }

    public UploadTask uploadCompressedImage(Bitmap image, String address, Bitmap.CompressFormat format, int compression) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(format, compression, stream);
        return storageReference.child(address).putBytes(stream.toByteArray());
    }

    public Task<Uri> downloadImage(String address) {
        return storageReference.child(address).getDownloadUrl();
    }

    public Task<DocumentReference> uploadItem(String name, String location, String description, String userID) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("location", location);
        item.put("description", description);
        item.put("userID", userID);

        return firestore.collection(userID).add(item);
    }

    public void downloadUserItems(String userID) {

    }

    public static String findImageAddress(String userID, String itemID, ImageType type) {
        return userID + "/" 
                + (type == ImageType.MAIN_IMAGE? "/images/" : "/receipts/")
                + itemID;
    }



}
