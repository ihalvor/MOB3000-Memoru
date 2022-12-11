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

/**
 * Singleton class to interact with the google firestore database, and the firestore image server
 */
public class Database {

    /**
     * The type of the image, item image or receipt image
     */
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

    /**
     * Private constructor to enable singleton architecture
     */
    private Database() {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Get and instance of the Database Singleton class
     * @return The instance of the Database
     */
    public static Database getInstance() {
        return database;
    }

    /**
     * Upload an image to the database
     * @param image The bitmap of the image
     * @param address The address to upload the image
     * @param format The format to compress the image to
     * @param compression What quality to compress to
     * @return The UploadTask for the image
     */
    public UploadTask uploadCompressedImage(Bitmap image, String address, Bitmap.CompressFormat format, int compression) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(format, compression, stream);
        return storageReference.child(address).putBytes(stream.toByteArray());
    }

    /**
     * Download the Uri of an image
     * @param address the address of the image
     * @return Task of type Uri
     */
    public Task<Uri> downloadImage(String address) {
        return storageReference.child(address).getDownloadUrl();
    }

    /**
     * Upload an item to the firestore database
     * @param name the name of the item
     * @param location the location of the item
     * @param description the description for the item
     * @param userID the user's unique googleID
     * @return Task of type DocumentReference
     */
    public Task<DocumentReference> uploadItem(String name, String location, String description, String userID) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("location", location);
        item.put("description", description);
        item.put("userID", userID);
        item.put("fav", false);

        return firestore.collection(userID).add(item);
    }

    /**
     * Update a field for an item in the firestore database
     * @param userID The user's unique google ID
     * @param itemID the item's unique document ID
     * @param field Which field to update
     * @param value The new value for the field
     */
    public void updateItem(String userID, String itemID, String field, String value) {
        firestore.collection(userID).document(itemID).update(field, value);
    }

    /**
     * Favourite an item in the firestore database
     * @param userID the user's unique google ID
     * @param itemID the item's unique document ID
     * @param fav Wethere the item should be favourited or not
     */
    public void favItem(String userID, String itemID, boolean fav) {
        firestore.collection(userID).document(itemID).update("fav", fav);
    }

    /**
     * Download all items for a user from the firestore database
     * @param userID the user's unique google ID
     * @return task of type QuerySnapshot
     */
    public Task<QuerySnapshot> downloadUserItems(String userID) {
        return firestore.collection(userID)
                .orderBy("fav", Query.Direction.DESCENDING)
                .orderBy("name", Query.Direction.ASCENDING)
                .get();
    }

    /**
     * Download one specific item
     * @param userID the user's unique google ID
     * @param itemID the item's unique document ID
     * @return task of type DocumentSnapshot
     */
    public Task<DocumentSnapshot> downloadUserItem(String userID, String itemID) {
        return firestore.collection(userID)
                .document(itemID)
                .get();
    }

    /**
     * Get the address for an image based on the user id, item id and imagetype
     * @param userID the user's unique google ID
     * @param itemID the item's unique document ID
     * @param type The type of the image, either item or receipt
     * @return The address for the specified image
     */
    public static String findImageAddress(String userID, String itemID, ImageType type) {
        return userID
                + type.toString()
                + itemID;
    }

    /**
     * Delete an item from the firestore database, and it's images in the cloud storage
     * @param userID the user's unique google ID
     * @param itemID the item's unique document ID
     */
    public void deleteItem(String userID, String itemID) {
        firestore.collection(userID).document(itemID).delete();
        storageReference.child(userID + "/images/" + itemID).delete();
        storageReference.child(userID + "/receipts/" + itemID).delete();
    }


}
