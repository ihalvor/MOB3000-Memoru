package com.example.memorutest1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class ActivityMyItems extends AppCompatActivity {

    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setTitle("My Items");
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_my_items);
        // TODO: log user out if not authorized
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Set navbar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ((TextView) findViewById(R.id.txt_list)).setTextColor(getColor(R.color.button_color));
        } else {
            ((TextView) findViewById(R.id.txt_list)).setTextColor(0xFF5584AC);
        }
        ((TextView) findViewById(R.id.txt_grid)).setOnClickListener((View view) -> {
            finish();
            startActivity(new Intent(this, ActivityMyGrid.class));
        });

        Database database = Database.getInstance();
        database.downloadUserItems(user.getUid()).addOnCompleteListener((Task<QuerySnapshot> task) -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Map<String, Object> item = document.getData();

                    String itemID = document.getId();
                    String userID = user.getUid();

                    database.downloadImage(Database.findImageAddress(userID, itemID, Database.ImageType.ITEM))
                            .addOnCompleteListener(uriTask -> {
                                ((LinearLayout) findViewById(R.id.layout_scroll))
                                        .addView(getItemDisplay(item, uriTask, itemID));
                            });
                }
            } else {
                Toast.makeText(this, "Could not load items", Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private View getItemDisplay(Map<String, Object> item, Task<Uri> task, String itemID) {
        View layout = getLayoutInflater().inflate(R.layout.my_item, null);

        ImageView imageView = layout.findViewById(R.id.mini_image);
        if(task.isSuccessful()) {
            Picasso.get()
                    .load(task.getResult().toString())
                    .resize(160, 160)
                    .centerCrop()
                    .into(imageView);
        }

        ((TextView) layout.findViewById(R.id.txt_name)).setText(item.get("name").toString());
        ((TextView) layout.findViewById(R.id.txt_desc)).setText(item.get("description").toString());

        layout.setOnClickListener((View view) -> {
            startActivity(new Intent(this, ActivityViewItem.class)
                    .putExtra("itemID", itemID));
        });

        return layout;
    }
}