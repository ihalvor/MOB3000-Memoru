package com.example.memorutest1;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class ActivityMyItems extends AppCompatActivity {

    public static final String TAG = "MY_ITEMS";
    private FirebaseUser user;
    private ArrayList<MyItem> myItems = new ArrayList<>();
    private ArrayList<MyItem> filteredItems = new ArrayList<>();

    private Database database;

    private ItemAdapter itemAdapter = new ItemAdapter(myItems, filteredItems);
    private ListView listView;

    // Use an activityResultLauncher to launch the item, the result is wether the item was changed
    // or not.
    private ActivityResultLauncher<Intent> viewItemLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (ActivityResult result) -> {
                if(result.getResultCode() == RESULT_OK) {
                    Intent intent = result.getData();

                    boolean edited = false;
                    if(intent.hasExtra("edit")) {
                        edited = intent.getBooleanExtra("edit", false);
                    }
                    if(edited) downloadItems();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setTitle("My Items");
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_my_items);

        // Exit if not signed in
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) finish();

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

        database = Database.getInstance();
        downloadItems();

        // Filter items as we type our search string
        SearchView search = findViewById(R.id.search);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) { return false; }

            @Override
            public boolean onQueryTextChange(String s) {
                itemAdapter.getFilter().filter(s);
                return false;
            }
        });

    }

    /**
     * Download the user's items from the database, and display them in the ListView
     */
    private void downloadItems() {
        filteredItems.clear();
        myItems.clear();
        // Downloading users items
        database.downloadUserItems(user.getUid()).addOnCompleteListener((Task<QuerySnapshot> task) -> {
                    if (task.isSuccessful()) {

                        listView = findViewById(R.id.layout_scroll);
                        listView.setAdapter(itemAdapter);

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> item = document.getData();

                            String itemID = document.getId();
                            String userID = user.getUid();

                            MyItem myItem = new MyItem(item, itemID, userID, null, null);

                            filteredItems.add(myItem);
                            myItems.add(myItem);
                        }
                    } else {
                        Toast.makeText(this, "Could not load items", Toast.LENGTH_SHORT).show();
                    }

                })
                .addOnFailureListener((Exception e) -> {
                    Log.e("MY_ITEMS", e.toString());
                });
    }

    // Adapter for our listview, displaying the items
    public class ItemAdapter extends BaseAdapter implements Filterable {
        @Override
        public Filter getFilter() {
            return filter;
        }

        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                ArrayList<MyItem> filteredList = new ArrayList<>();

                if(charSequence.toString().isEmpty()) {
                    filteredList.addAll(myItems);
                } else {
                    for(MyItem item : myItems) {
                        if(item.toString().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                            filteredList.add(item);
                        }
                    }
                }

                FilterResults result = new FilterResults();
                result.values = filteredList;

                return result;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredItems.clear();
                filteredItems.addAll((Collection<? extends MyItem>) filterResults.values);
                notifyDataSetChanged();
            }
        };

        private ArrayList<MyItem> myItems;
        private ArrayList<MyItem> filteredItems;

        public ItemAdapter(ArrayList<MyItem> myItems, ArrayList<MyItem> filteredItems) {
            this.myItems = myItems;
            this.filteredItems = filteredItems;
        }

        @Override
        public int getCount() { return filteredItems.size(); }

        @Override
        public Object getItem(int i) { return filteredItems.get(i); }

        @Override
        public long getItemId(int i) { return 0; }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            MyItem item = filteredItems.get(i);

            if(view == null) {
                return getItemDisplay(item.getMap(), item.getUserID(), item.getItemID(), null);
            } else {
                getItemDisplay(item.getMap(), item.getUserID(), item.getItemID(), view);
            }

            return view;
        }

    }

    // Enable the back button in the actionbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Generate a view for the item
     * @param item a hasmap containing; name, location, description and favourite for the item.
     * @param userID google account unique ID
     * @param itemID document ID
     * @param view a view we wish to recycle, pass null to create a new view
     * @return a view displaying the item
     */
    private View getItemDisplay(Map<String, Object> item, String userID, String itemID, View view) {
        boolean favourite;
        try {
            favourite = item.get("fav").toString() == "true";
        } catch (Exception e) {
            favourite = false;
        }

        // For older APIs we can not load the fav icon into an imageView,
        // we therefor have two different versions of my_item
        View layout;

        // We can recycle the view for newer API levels.
        if(view != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            layout = view;

            ((ImageView) layout.findViewById(R.id.img_star)).setImageIcon(
                    Icon.createWithResource(getApplicationContext(), favourite
                                    ? R.drawable.ic_baseline_star_24
                                    : R.drawable.ic_baseline_star_border_24)
            );

        } else {
            layout = getLayoutInflater().inflate(
                    favourite
                            ? R.layout.my_item_fav
                            : R.layout.my_item,
                    null);
        }

        // Download and display image if item has one
        ImageView imageView = layout.findViewById(R.id.mini_image);
        Database.getInstance()
                .downloadImage(Database.findImageAddress(userID, itemID, Database.ImageType.ITEM))
                .addOnSuccessListener((Uri uri) -> {
                    Picasso.get()
                            .load(uri)
                            .resize(160, 160)
                            .centerCrop()
                            .into(imageView);
                });

        ((TextView) layout.findViewById(R.id.txt_name)).setText(item.get("name").toString());
        ((TextView) layout.findViewById(R.id.txt_location)).setText(item.get("location").toString());

        // Create a reference to the boolean we can use in lambda expression
        boolean[] favArr = { favourite };
        layout.findViewById(R.id.img_star).setOnClickListener((View innerView) -> {

            // Create local reference to this lambda
            favArr[0] = !favArr[0];
            boolean isFav = favArr[0];

            // Update database
            Database.getInstance().favItem(user.getUid(), itemID, isFav);

            // Update icon when pressed
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ((ImageView) innerView).setImageIcon(Icon.createWithResource(
                        getApplicationContext(),
                        isFav
                            ? R.drawable.ic_baseline_star_24
                            : R.drawable.ic_baseline_star_border_24));
            } else {
                // For older APIs, refresh the page to show correct favourite icon
                startActivity(new Intent(this, ActivityMyItems.class));
                finish();
            }
        });

        layout.setOnClickListener((View innerView) -> {
            viewItemLauncher.launch(new Intent(this, ActivityViewItem.class)
                    .putExtra("itemID", itemID));
        });

        return layout;
    }
}