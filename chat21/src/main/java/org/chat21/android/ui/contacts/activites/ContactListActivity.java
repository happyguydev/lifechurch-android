package org.chat21.android.ui.contacts.activites;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.chat21.android.core.contacts.synchronizers.Contact;
import org.chat21.android.core.contacts.synchronizers.ContactFetcher;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.messages.activities.MessageListActivity;
import com.bumptech.glide.Glide;
import com.iamhabib.easy_preference.EasyPreference;

import org.chat21.android.R;
import org.chat21.android.connectivity.AbstractNetworkReceiver;
import org.chat21.android.core.messages.models.Message;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.contacts.fragments.ContactsListFragment;
import org.chat21.android.ui.contacts.listeners.OnContactClickListener;
import org.chat21.android.utils.image.CropCircleTransformation;

import java.util.ArrayList;

/**
 * Created by stefano on 25/08/2015.
 */
public class ContactListActivity extends AppCompatActivity implements OnContactClickListener {

    private static final String TAG = ContactListActivity.class.getSimpleName();

    private ContactsListFragment contactsListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        int result= ContextCompat.checkSelfPermission(ContactListActivity.this, Manifest.permission.READ_CONTACTS);
        if (result != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ContactListActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 7);
        }
        else{
//            progressBar.setVisibility(View.VISIBLE);
            contactsListFragment = new ContactsListFragment();
            contactsListFragment.setOnContactClickListener(this);
        }
        contactsListFragment = new ContactsListFragment();
        contactsListFragment.setOnContactClickListener(this);


        // #### BEGIN TOOLBAR ####
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // #### END  TOOLBAR ####

        // #### BEGIN CONTAINER ####
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, contactsListFragment)
                .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "ContactListActivity.onOptionsItemSelected");

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        contactsListFragment.onBackPressed();
        super.onBackPressed();
    }

    @Override
    public void onContactClicked(IChatUser contact, int position) {
        Log.d(TAG, "ContactListActivity.onRecyclerItemClicked:" +
                " contact == " + contact.toString() + ", position ==  " + position);

        if (ChatUI.getInstance().getOnContactClickListener() != null) {
            ChatUI.getInstance().getOnContactClickListener().onContactClicked(contact, position);
        }

        // start the conversation activity
        startMessageListActivity(contact);
    }

    private void startMessageListActivity(IChatUser contact) {
        Log.d(TAG, "ContactListActivity.startMessageListActivity");

        Intent intent = new Intent(this, MessageListActivity.class);
        intent.putExtra(ChatUI.BUNDLE_RECIPIENT, contact);
        intent.putExtra(ChatUI.BUNDLE_CHANNEL_TYPE, Message.DIRECT_CHANNEL_TYPE);

        startActivity(intent);

        // finish the contact list activity when it start a new conversation
        finish();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ChatUI.REQUEST_CODE_CREATE_GROUP) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 7) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(ContactListActivity.this, "Can't read contacts as you have declined the permission.", Toast.LENGTH_SHORT).show();
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
}