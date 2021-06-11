package com.lifechurch.talk;

import android.os.Bundle;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iamhabib.easy_preference.EasyPreference;

import org.chat21.android.core.ChatManager;
import org.chat21.android.core.contacts.synchronizers.Contact;
import org.chat21.android.core.contacts.synchronizers.ContactFetcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class BlockUserActivity extends AppCompatActivity {
    //This flag is required to avoid first time onResume refreshing
    static boolean loaded = false;



    ArrayList<BlockUser> listBlockusers = new ArrayList<>();
    ArrayList<String> namelist = new ArrayList<>();
    ArrayList<String> uidList = new ArrayList<>();
    ListView lvBlockuser;
    Toolbar toolbar;
    BlockUserAdapter adapterBlockusers;
    DatabaseReference contactDatabase,blockDatabase;
    private SearchView searchView;
    String status = "";
    ArrayList<String> localphones = new ArrayList<>();
    ArrayList<Contact> listContacts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_blockuser);
        lvBlockuser = (ListView) findViewById(R.id.lvBlockuser);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contactDatabase = FirebaseDatabase.getInstance().getReference().child("/apps/" + ChatManager.Configuration.appId + "/contacts/");
        blockDatabase = FirebaseDatabase.getInstance().getReference().child("/apps/" + ChatManager.Configuration.appId + "/BlockUser/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
        readContact();


    }

    private void readContact() {
        listContacts = new ContactFetcher(BlockUserActivity.this).fetchAll();
        for(int  i =0;i< listContacts.size();i++)
        {
            localphones.add(listContacts.get(i).getallphone());
        }


        if (!EasyPreference.with(BlockUserActivity.this).getString("autoadd", "").equals("no")){
            contactDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        listBlockusers.clear();
                        for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                            Log.v("*************", ds.getKey());
                            final String firstname = ds.child("firstname").getValue(String.class);
                            final String image = ds.child("imageurl").getValue(String.class);
                            final String phonenumber = ds.child("phonenumber").getValue(String.class);
                            for (int i = 0; i < localphones.size(); i ++) {
                                if (phonenumber.equals(localphones.get(i))) {
                                    blockDatabase.child(ds.getKey()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                status = dataSnapshot.child("blockStatus").getValue(String.class);

                                            }
                                            else {
                                                status = "Unblocked";
                                            }
                                            BlockUser blockUser = new BlockUser(ds.getKey(), firstname, image, status);

                                            listBlockusers.add(blockUser);



                                            Collections.sort(listBlockusers, new Comparator<BlockUser>() {
                                                @Override
                                                public int compare(BlockUser o1, BlockUser o2) {
                                                    return o1.name.compareTo(o2.name);
                                                }
                                            });

                                            adapterBlockusers = new BlockUserAdapter(BlockUserActivity.this, listBlockusers);
                                            lvBlockuser.setAdapter(adapterBlockusers);


                                        }



                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }

//                            blockDatabase.child(ds.getKey()).addValueEventListener(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(DataSnapshot dataSnapshot) {
//                                    if (dataSnapshot.exists()) {
//                                        status = dataSnapshot.child("blockStatus").getValue(String.class);
//
//                                    }
//                                    else {
//                                        status = "Unblocked";
//                                    }
//                                    BlockUser blockUser = new BlockUser(ds.getKey(), firstname, image, status);
//
//                                    listBlockusers.add(blockUser);
//
//
//
//                                    Collections.sort(listBlockusers, new Comparator<BlockUser>() {
//                                        @Override
//                                        public int compare(BlockUser o1, BlockUser o2) {
//                                            return o1.name.compareTo(o2.name);
//                                        }
//                                    });
//
//                                    adapterBlockusers = new BlockUserAdapter(BlockUserActivity.this, listBlockusers);
//                                    lvBlockuser.setAdapter(adapterBlockusers);
//
//
//                                }
//
//
//
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
//
//                                }
//                            });

                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            }
            else{
                Toast.makeText(BlockUserActivity.this, "Auto add user From phone-book Disabled!", Toast.LENGTH_SHORT).show();

            }




        Log.v("-oioioi->", String.valueOf(listBlockusers));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(org.chat21.android.R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        MenuItemCompat.setShowAsAction(item,
                MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW |
                        MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setActionView(item, searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                if (adapterBlockusers != null) adapterBlockusers.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (adapterBlockusers != null) adapterBlockusers.getFilter().filter(query);

                return true;
            }
        });

        return super.onCreateOptionsMenu(menu); // attach the activity menu
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }





}