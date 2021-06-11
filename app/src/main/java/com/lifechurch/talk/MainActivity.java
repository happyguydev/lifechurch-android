package com.lifechurch.talk;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {
    ArrayList<Contact> listContacts;
    ArrayList<String> namelist;
    ListView lvContacts;
    Toolbar toolbar;
    ContactsAdapter adapterContacts;
    private SearchView searchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        lvContacts = (ListView) findViewById(R.id.lvContacts);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int result= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (result != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 7);
        }
        else{
//            if (!EasyPreference.with(MainActivity.this).getString("autoadd", "").equals("no")){
                readContact();
//            }
//            else{
//                Toast.makeText(MainActivity.this, "Auto add user From phone-book Disabled!", Toast.LENGTH_SHORT).show();
//
//            }

        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        9);
            }
        }

    }

    private void readContact() {
        listContacts = new ContactFetcher(this).fetchAll();
//        namelist = null;
//        for(int i = 0;i<listContacts.size();i++){
//            namelist.add(listContacts.get(i).name);
//        }
        Collections.sort(listContacts, new Comparator<Contact>() {

            @Override
            public int compare(Contact o1, Contact o2) {
                return o1.name.compareTo(o2.name);
            }
        });

        adapterContacts = new ContactsAdapter(this, listContacts);
        lvContacts.setAdapter(adapterContacts);
        lvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Log.v("phone", listContacts.get(position).numbers.get(0).number);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("SMS invite?")
                        .setMessage("Let's talk via LifeChurch app!\nhttps://play.google.com/store/apps/")

                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                PendingIntent pi= PendingIntent.getActivity(MainActivity.this, 0, intent,0);
                                SmsManager sms= SmsManager.getDefault();
                                sms.sendTextMessage(listContacts.get(position).numbers.get(0).number, null, "Let's talk via LifeChurch app!\nhttps://play.google.com/store/apps/", pi,null);
                                Toast.makeText(MainActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu, inflater);
//        menu.clear();
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
                if (adapterContacts != null) adapterContacts.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (adapterContacts != null) adapterContacts.getFilter().filter(query);
//                Log.d(TAG, "ContactListActivity.OnQueryTextListener.onQueryTextChange:" +
//                        " query == " + query);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu); // attach the activity menu
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 7) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readContact();
            } else {
                Toast.makeText(this, "Can't read contacts as you have declined the permission.", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == 9) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                //permission denied
            }
        }

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