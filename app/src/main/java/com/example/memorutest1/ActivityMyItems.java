package com.example.memorutest1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_items);
        // TODO: log user out if not authorized
        user = FirebaseAuth.getInstance().getCurrentUser();

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

    private View getItemDisplay(Map<String, Object> item, Task<Uri> task, String itemID) {
        View layout = getLayoutInflater().inflate(R.layout.my_item, null);

        ImageView imageView = layout.findViewById(R.id.mini_image);
        Picasso.get()
                .load(task.getResult().toString())
                .resize(160, 160)
                .centerCrop()
                .into(imageView);

        ((TextView) layout.findViewById(R.id.txt_name)).setText(item.get("name").toString());
        ((TextView) layout.findViewById(R.id.txt_desc)).setText(item.get("description").toString());

        layout.setOnClickListener((View view) -> {
            startActivity(new Intent(this, ActivityViewItem.class)
                    .putExtra("itemID", itemID));
        });

        return layout;
    }
}