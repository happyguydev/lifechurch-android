package com.lifechurch.talk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.chat21.android.core.ChatManager;
import org.chat21.android.core.contacts.listeners.ContactListener;
import org.chat21.android.core.contacts.synchronizers.ContactsSynchronizer;
import org.chat21.android.core.exception.ChatRuntimeException;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.login.listeners.OnLogoutClickListener;
import org.chat21.android.utils.StringUtils;
import org.chat21.android.utils.image.CropCircleTransformation;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.chat21.android.utils.DebugConstants.DEBUG_CONTACTS_SYNC;

/**
 * Created by stefanodp91 on 08/01/18.
 */

public class UserProfileActivity extends AppCompatActivity implements ContactListener {
    private static final String TAG = UserProfileActivity.class.getName();

    private RoundedImageView mProfilePicture;
    private EditText mFullName, mStatus;
    private TextView mEmail, mPhone;
    private TextView mUserId;
    private TextView mAppName;
    private TextView mAppVersion;
    private Button mLogout, mEdit;
    private ProgressDialog loadingBar;
    private IChatUser loggedUser;
    private ContactsSynchronizer contactsSynchronizer;
    private StorageReference userProfileImageRef;
    private boolean isClicked = false;
    private String userUID;
    ImageView Back;
    DatabaseReference contactsNode;
    String downloadUri = "";
    private interface OnUserCreatedOnContactsCallback {
        void onUserCreatedSuccess();

        void onUserCreatedError(Exception e);
    }

    public UserProfileActivity() {
    }

    /**
     * Returns a new instance of this fragment
     */
    public static UserProfileActivity newInstance() {
        UserProfileActivity fragment = new UserProfileActivity();

        return fragment;
    }

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        contactsSynchronizer = ChatManager.getInstance().getContactsSynchronizer();
//        contactsSynchronizer.upsertContactsListener(this);
//        Log.d(DEBUG_CONTACTS_SYNC, "  UserProfileFragment.onCreateView: contactsSynchronizer attached");
//
//        super.onCreate(savedInstanceState);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_profile);
        loggedUser = ChatManager.getInstance().getLoggedUser();
        mProfilePicture = findViewById(R.id.profile_picture);
        mFullName = findViewById(R.id.fullname);
        mStatus = findViewById(R.id.status);
        mEmail = findViewById(R.id.email);
        mUserId = findViewById(R.id.userid);
        mAppName = findViewById(R.id.app_name);
        mAppVersion = findViewById(R.id.app_version);
        mLogout = findViewById(R.id.logout);
        mPhone = findViewById(R.id.phone);

        mEdit = findViewById(R.id.profile_edit);
        loadingBar = new ProgressDialog(UserProfileActivity.this);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        contactsSynchronizer = ChatManager.getInstance().getContactsSynchronizer();
        contactsSynchronizer.upsertContactsListener(this);
        Glide.with(UserProfileActivity.this)
                .load(loggedUser.getProfilePictureUrl())
                .placeholder(R.drawable.ic_person_avatar)
                .bitmapTransform(new CropCircleTransformation(UserProfileActivity.this))
                .into(mProfilePicture);
        mProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isClicked) {
                    return;
                }
                else {
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1, 1)
                            .start(UserProfileActivity.this);
                }

            }
        });

        //back button
        Back = findViewById(R.id.back);
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, SettingActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 09/01/18 logout must be embebbed inside chatUI
                performLogout(new OnLogoutClickListener() {
                    @Override
                    public void onLogoutClicked() {
                        // get the main activity name from manifest
                        String packageName = UserProfileActivity.this.getApplicationContext().getPackageName();
                        Intent launchIntent = UserProfileActivity.this.getApplicationContext().getPackageManager().getLaunchIntentForPackage(packageName);
                        String className = launchIntent.getComponent().getClassName();
                        try {
                            Class<?> clazz = Class.forName(className);
                            Intent intent = new Intent(UserProfileActivity.this.getApplicationContext(), clazz);
                            // clear the activity stack
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent); // start the main activity
                            UserProfileActivity.this.finish(); // finish this activity
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isClicked) {
                    mFullName.setEnabled(true);
                    mStatus.setEnabled(true);
                    isClicked = true;
                    mEdit.setText(R.string.profile_save_btn);
                } else {
                    mFullName.setEnabled(false);
                    mStatus.setEnabled(false);
                    isClicked = false;
                    userInfoUpdate();

                    mEdit.setText(R.string.profile_edit_btn);
                }
            }
        });



        contactsSynchronizer.connect();

        contactsNode = FirebaseDatabase.getInstance()
                .getReference()
                .child("/apps/" + ChatManager.Configuration.appId + "/contacts/" + loggedUser.getId());
        contactsNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String phone = dataSnapshot.child("phonenumber").getValue(String.class);
                String fullName = dataSnapshot.child("firstname").getValue(String.class);
                String status = dataSnapshot.child("status").getValue(String.class);
                String email = dataSnapshot.child("email").getValue(String.class);
                mFullName.setText(fullName);
                mPhone.setText(phone);
                mStatus.setText(status);
                mEmail.setText(email);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (!isClicked) {
            mFullName.setEnabled(false);
            mFullName.requestFocus();
            mStatus.setEnabled(false);
        }

    }

    @Override
    public void onDestroy() {

        if(contactsSynchronizer != null) {
            contactsSynchronizer.removeContactsListener(this);
            Log.d(DEBUG_CONTACTS_SYNC, "  UserProfileFragment.onDestroy: contactsSynchronizer detached");
        }

        super.onDestroy();
    }



    private void userInfoUpdate() {
        final Map<String, Object> userMap = new HashMap<>();
        userUID = loggedUser.getId();

        userMap.put("email", loggedUser.getEmail());
        userMap.put("phonenumber", mPhone.getText().toString());
        userMap.put("imageurl", downloadUri);
        userMap.put("firstname", mFullName.getText().toString());
        userMap.put("lastname", "");
        userMap.put("status", mStatus.getText().toString());
        userMap.put("timestamp", new Date().getTime());
        userMap.put("uid", userUID);

        createUserOnContacts(userUID, userMap, new OnUserCreatedOnContactsCallback() {

            @Override
            public void onUserCreatedSuccess() {

                Toast.makeText(UserProfileActivity.this, "Update successful !", Toast.LENGTH_LONG).show();
            }


            @Override
            public void onUserCreatedError(Exception e) {
                // TODO: 04/01/18
                Toast.makeText(UserProfileActivity.this, "Saving user on contacts failed." + e,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createUserOnContacts(String key, Map<String, Object> user,
                                      final UserProfileActivity.OnUserCreatedOnContactsCallback onUserCreatedOnContactsCallback) {
        DatabaseReference contactsNode;
        if (StringUtils.isValid(ChatManager.Configuration.firebaseUrl)) {
            contactsNode = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(ChatManager.Configuration.firebaseUrl)
                    .child("/apps/" + ChatManager.Configuration.appId + "/contacts");
        } else {
            contactsNode = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("/apps/" + ChatManager.Configuration.appId + "/contacts");
        }

        // save the user on contacts node
        contactsNode.child(key)
                .setValue(user, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            // all gone right
                            onUserCreatedOnContactsCallback.onUserCreatedSuccess();
                        } else {
                            // errors
                            onUserCreatedOnContactsCallback.onUserCreatedError(databaseError.toException());
                        }
                    }
                });
    }

    private void updateUserUI(IChatUser user) {
        // profile picture
        Glide.with(UserProfileActivity.this.getApplicationContext())
                .load(user.getProfilePictureUrl())
                .placeholder(R.drawable.ic_person_avatar)
                .bitmapTransform(new CropCircleTransformation(UserProfileActivity.this.getApplicationContext()))
                .into(mProfilePicture);

        mEmail.setText(user.getEmail());
        mUserId.setText(user.getId());
    }

    private void performLogout(OnLogoutClickListener onLogoutClickListener) {

        // sign out from firebase
        FirebaseAuth.getInstance().signOut();

//        // check if the user has been really signed out
//        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
//            Log.e(TAG, "user not signed out");
//        } else {
//            Log.d(TAG, "user signed out with success");
//        }

        ChatManager.getInstance().dispose();

        onLogoutClickListener.onLogoutClicked();
    }

    @Override
    public void onContactReceived(IChatUser contact, ChatRuntimeException e) {
        // do nothing
    }

    @Override
    public void onContactChanged(IChatUser contact, ChatRuntimeException e) {
        if (e == null) {
            if (contact.getId().equals(this.loggedUser.getId())) {
                this.loggedUser = ChatManager.getInstance().getLoggedUser();
                updateUserUI(this.loggedUser);
            }
        }
    }

    @Override
    public void onContactRemoved(IChatUser contact, ChatRuntimeException e) {
        // do nothing
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("set_profile_image");
                loadingBar.setMessage("Please wait, your profile image is uploading");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                final Uri resultUri = result.getUri();
                final StorageReference filePath = userProfileImageRef.child(UUID.randomUUID().toString() + ".jpg");

                filePath.putFile(resultUri)
                        .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(UserProfileActivity.this.getContentResolver(), resultUri);
                                mProfilePicture.setImageBitmap(bitmap);
                                // Continue with the task to get the download URL
                                return filePath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            downloadUri = task.getResult().toString();
                            loadingBar.dismiss();




                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(UserProfileActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }else{
            Log.v("","");
        }
    }
}

