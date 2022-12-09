package com.example.memorutest1;

import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

public class MyItem {

    private String name;
    private String location;
    private String desc;

    private boolean fav;

    private String userID;
    private String itemID;

    private Uri itemImage;
    private Uri receiptImage;

    @Override
    public String toString() {
        return name + " " + location;
    }


    public MyItem(Map<String, Object> item, String itemID, String userID, Uri itemImage, Uri receiptImage) {
        this.name = item.get("name").toString();
        this.location = item.get("location").toString();
        this.desc = item.get("description").toString();
        this.fav = item.get("fav").toString() == "true";

        this.itemID = itemID;
        this.userID = userID;

        this.itemImage = itemImage;
        this.receiptImage = receiptImage;
    }

    public Map<String, Object> getMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("name", name);
        map.put("location", location);
        map.put("description", desc);
        map.put("fav", fav? "true" : "false");

        return map;
    }

    public boolean isFav() {
        return fav;
    }

    public void setFav(boolean fav) {
        this.fav = fav;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Uri getItemImage() {
        return itemImage;
    }

    public void setItemImage(Uri itemImage) {
        this.itemImage = itemImage;
    }

    public Uri getReceiptImage() {
        return receiptImage;
    }

    public void setReceiptImage(Uri receiptImage) {
        this.receiptImage = receiptImage;
    }

}
