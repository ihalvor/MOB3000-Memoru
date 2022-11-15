package com.example.memorutest1;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Database {

    public enum ImageType {
        ITEM, RECEIPT;

        @NonNull
        @Override
        public String toString() {
            switch(this) {
                case ITEM:      return "/images/";
                case RECEIPT:   return "/receipts/";
                default:        return "/other/";
            }
        }
    }

    private static Database database = new Database();

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseFirestore firestore;

    private Database() {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        firestore = FirebaseFirestore.getInstance();
    }

    public static Database getInstance() {
        return database;
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

    public void updateItem(String userID, String itemID, String field, String value) {
        firestore.collection(userID).document(itemID).update(field, value);
    }

    public Task<QuerySnapshot> downloadUserItems(String userID) {
        return firestore.collection(userID)
                .orderBy("name", Query.Direction.DESCENDING)
                .get();
    }

    public Task<DocumentSnapshot> downloadUserItem(String userID, String itemID) {
        return firestore.collection(userID)
                .document(itemID)
                .get();
    }

    public static String findImageAddress(String userID, String itemID, ImageType type) {
        return userID + "/" 
                + type.toString()
                + itemID;
    }



}
