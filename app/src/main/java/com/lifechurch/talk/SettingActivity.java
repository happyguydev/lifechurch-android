package com.lifechurch.talk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iamhabib.easy_preference.EasyPreference;
import com.makeramen.roundedimageview.RoundedImageView;

import org.chat21.android.core.ChatManager;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.login.listeners.OnLogoutClickListener;
import org.chat21.android.utils.image.CropCircleTransformation;

import com.lifechurch.talk.usage.LocaleHelper;

public class SettingActivity extends AppCompatActivity {
    DatabaseReference contactsNode;
    private IChatUser loggedUser;
    private Button Profileinfo_edt;
    private RoundedImageView Useravatar;
    private TextView Username,Userphonenumber,Useremail,Updatetxt;

    /////////////////
    RelativeLayout Logout;
    RelativeLayout Friends;
    RelativeLayout Feedback;
    RelativeLayout Theme;
    //////
    ImageView Back;

    private FirebaseAuth mAuth;
    private ImageView imgTheme;
    private TextView txtFontLang;
    private SharedPreferences prefs;
    String name, photo;
    private int color;
    private float fontsize;
    private String selectedLang;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //back button
        Back = findViewById(R.id.back);
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(SettingActivity.this,TabActivity.class);
//                startActivity(intent);
//                finish();
                onBackPressed();
            }
        });
        ///friends part
        Updatetxt = findViewById(R.id.updatetxt);
        Updatetxt.setText("Updated on " + EasyPreference.with(SettingActivity.this).getString("updatesync", ""));

        prefs  = this.getSharedPreferences("themeinfo", MODE_PRIVATE);
        if (prefs != null) {
            color = prefs.getInt("setbackgroundcolor", Color.parseColor("#B2C7DA"));
            fontsize = prefs.getFloat("setfontsize",14);
        }

//      Profile Part
        Useravatar = findViewById(R.id.useravatar);
        Username = findViewById(R.id.username);
        Userphonenumber = findViewById(R.id.userphonenumber);
        Useremail = findViewById(R.id.useremail);

        imgTheme = (ImageView) findViewById(R.id.themeColor);
        imgTheme.setBackgroundColor(color);
        txtFontLang = (TextView) findViewById(R.id.txtFontLang);
        String dd = LocaleHelper.getLanguage(this);
        switch (dd) {
            case "ko":
                selectedLang = "한국어";
                break;
            case "en":
                selectedLang = "English";
                break;
        }

        txtFontLang.setText(" , " + String.format("%.0f", fontsize) + " , " + selectedLang);

        loggedUser = ChatManager.getInstance().getLoggedUser();
        ProfileViewSet(loggedUser);
        Profileinfo_edt = findViewById(R.id.profileinfo_edt);
        Profileinfo_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this,UserProfileActivity.class);
                startActivity(intent);
            }
        });

        Logout = findViewById(R.id.logout);
        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 09/01/18 logout must be embebbed inside chatUI
                performLogout(new OnLogoutClickListener() {
                    @Override
                    public void onLogoutClicked() {
                        // get the main activity name from manifest
                        String packageName = SettingActivity.this.getPackageName();
                        Intent launchIntent = SettingActivity.this.getPackageManager().getLaunchIntentForPackage(packageName);
                        String className = launchIntent.getComponent().getClassName();
                        try {
                            Class<?> clazz = Class.forName(className);
                            Intent intent = new Intent(SettingActivity.this, clazz);
                            // clear the activity stack
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent); // start the main activity
                            finish(); // finish this activity
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        Feedback = findViewById(R.id.feedback);
        Feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mAuth = FirebaseAuth.getInstance();

                FirebaseUser user = mAuth.getCurrentUser();
                final String userId = user.getUid();

                loggedUser = ChatManager.getInstance().getLoggedUser();

                contactsNode = FirebaseDatabase.getInstance()
                        .getReference()
                        .child("/apps/" + ChatManager.Configuration.appId + "/contacts/" + loggedUser.getId());
                contactsNode.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String email = dataSnapshot.child("email").getValue(String.class);
                        name = dataSnapshot.child("firstname").getValue(String.class);
                        photo = dataSnapshot.child("imageurl").getValue(String.class);

                        if (email.equals("admin@test.com")){
                            Intent intent = new Intent(SettingActivity.this,FeedbackListActivity.class);
                            startActivity(intent);
                        }else{
                            FragmentFeedback feedbackmanage = (FragmentFeedback) FragmentFeedback.newInstance();

                            getSupportFragmentManager().beginTransaction()
                                    .add(R.id.container, feedbackmanage, FragmentFeedback.class.getName())
                                    .addToBackStack(null)
                                    .commit();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

        Friends = findViewById(R.id.friends);
        Friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendManageFragment frindmanage = (FriendManageFragment) FriendManageFragment.newInstance();

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, frindmanage, FriendManageFragment.class.getName())
                        .addToBackStack(null)
                        .commit();

            }
        });

        Theme = findViewById(R.id.theme);
        Theme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThemeFragment themefragment = (ThemeFragment) ThemeFragment.newInstance();

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, themefragment, ThemeFragment.class.getName())
                        .addToBackStack(null)
                        .commit();
            }
        });

    }

    private void performLogout(OnLogoutClickListener onLogoutClickListener) {

        // sign out from firebase
        FirebaseAuth.getInstance().signOut();
        ChatManager.getInstance().dispose();
        onLogoutClickListener.onLogoutClicked();
    }

    private void ProfileViewSet(final IChatUser loggedUser) {
        contactsNode = FirebaseDatabase.getInstance()
                .getReference()
                .child("/apps/" + ChatManager.Configuration.appId + "/contacts/" + loggedUser.getId());
        contactsNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String phone = dataSnapshot.child("phonenumber").getValue(String.class);
                String fullName = dataSnapshot.child("firstname").getValue(String.class);
                String email = dataSnapshot.child("email").getValue(String.class);
                Username.setText(fullName);
                Userphonenumber.setText(phone);
                Useremail.setText(email);

                // profile picture
                Glide.with(SettingActivity.this)
                        .load(loggedUser.getProfilePictureUrl())
                        .placeholder(R.drawable.ic_person_avatar)
                        .bitmapTransform(new CropCircleTransformation(SettingActivity.this))
                        .into(Useravatar);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
