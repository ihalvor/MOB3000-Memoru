package com.example.memorutest1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class ActivityMyGrid extends AppCompatActivity {

    private FirebaseUser user;
    private String userID;
    private GridView gridView;

    private int imageWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_grid);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) finish();
        userID = user.getUid();
        gridView = findViewById(R.id.grid_view);

        // Set navbar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ((TextView) findViewById(R.id.txt_grid)).setTextColor(getColor(R.color.button_color));
        } else {
            ((TextView) findViewById(R.id.txt_grid)).setTextColor(0xFF5584AC);
        }
        ((TextView) findViewById(R.id.txt_list)).setOnClickListener((View view) -> {
            finish();
            startActivity(new Intent(this, ActivityMyItems.class));
        });

        // Find application width
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        imageWidth = displayMetrics.widthPixels / 3;

        // Load items
        Database.getInstance().downloadUserItems(userID)
                .addOnSuccessListener((QuerySnapshot query) -> {
                    ArrayList<String> itemIDs = new ArrayList<>();
                    for(QueryDocumentSnapshot document : query) {
                        itemIDs.add(document.getId());
                    }

                    GridViewAdapter adapter = new GridViewAdapter(itemIDs);
                    gridView.setAdapter(adapter);
                });

    }

    public class GridViewAdapter extends BaseAdapter {
        private ArrayList<String> itemIDs = new ArrayList<>();

        GridViewAdapter(ArrayList<String> itemIDs) { this.itemIDs = itemIDs; }


        @Override
        public int getCount() { return itemIDs.size(); }

        @Override
        public Object getItem(int i) { return null; }

        @Override
        public long getItemId(int i) { return 0; }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            String itemID = itemIDs.get(i);

            ImageView imageView = new ImageView(getApplicationContext());

            // Go to the item view
            imageView.setOnClickListener((View view2) -> {
                Intent intent = new Intent(getApplicationContext(), ActivityViewItem.class);
                intent.putExtra("itemID", itemID);
                startActivity(intent);
            });

            // Load image
            Database.getInstance()
                    .downloadImage(Database.findImageAddress(userID, itemID, Database.ImageType.ITEM))
                    .addOnSuccessListener((Uri uri) -> {
                        Picasso.get().load(uri).resize(imageWidth, imageWidth).into(imageView);
                    });

            return imageView;
        }
    }
}