package org.chat21.android.ui.login.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.TestLooperManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iamhabib.easy_preference.EasyPreference;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.chat21.android.R;
import org.chat21.android.core.ChatManager;
import org.chat21.android.core.authentication.task.RefreshFirebaseInstanceIdTask;
import org.chat21.android.core.contacts.synchronizers.Contact;
import org.chat21.android.core.contacts.synchronizers.ContactFetcher;
import org.chat21.android.core.contacts.synchronizers.ContactsSynchronizer;
import org.chat21.android.core.exception.ChatFieldNotFoundException;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.contacts.activites.ContactListActivity;
import org.chat21.android.ui.conversations.listeners.OnNewConversationClickListener;
import org.chat21.android.utils.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.chat21.android.core.contacts.synchronizers.ContactsSynchronizer.decodeContactSnapShop;
import static org.chat21.android.ui.ChatUI.REQUEST_CODE_PROFILE_ACTIVITY;
import static org.chat21.android.utils.DebugConstants.DEBUG_LOGIN;

import android.provider.ContactsContract;

public class ProfileInputActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int GalleryPick = 100;
    private ProgressDialog loadingBar;
    private StorageReference userProfileImageRef;
    private FirebaseAuth mAuth,mAuth1;
    private ProgressBar progressBar;
    private String userUID;
    DatabaseReference friendsNode;

    String downloadUri = "";

    private EditText name;
    private EditText status;
    private TextView phone;
    private TextView email;
    private CheckBox check_phonebook;
    private RoundedImageView imv_user_avatar_setting;
    private Button next;
    private String useremail, userpassword,userphonenumber;
    private Boolean phonebook;
    private static final int MY_PHONEBOOK_REQUEST_CODE = 100;

    private CopyOnWriteArrayList<IChatUser> lists = new CopyOnWriteArrayList<IChatUser>();
    ArrayList<String> temp = new ArrayList<>();

    private interface OnUserCreatedOnFirebaseCallback {
        void onUserCreatedSuccess(String userUID);

        void onUserCreatedError(Exception e);
    }

    private interface OnUserCreatedOnContactsCallback {
        void onUserCreatedSuccess();

        void onUserCreatedError(Exception e);
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_input);
//        mAuth.getInstance().signOut();
        phonebook = true;
        name = findViewById(R.id.name);
        status = findViewById(R.id.status);
        phone = findViewById(R.id.phone);
        email = findViewById(R.id.email);
        check_phonebook = findViewById(R.id.checkbox_phonebook);
        imv_user_avatar_setting = findViewById(R.id.imv_user_avatar_setting);
        next = findViewById(R.id.nextBtn);

        //Todo; profile info set
         phone.setText(ChatUI.getPhone_number());
         email.setText(ChatUI.getUser_email());
         useremail = ChatUI.getUser_email();
         userpassword = ChatUI.getUser_pass();
         userphonenumber = ChatUI.getPhone_number();


         //Todo;checkbox

        if(phonebook == true){
            requestPermissions();
        }
        check_phonebook.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if(isChecked) {
                    phonebook = true;
                    requestPermissions();
                    EasyPreference.with(ProfileInputActivity.this).addString("autoadd", "ok").save();
                } else {
                    phonebook = false;
                    EasyPreference.with(ProfileInputActivity.this).addString("autoadd", "no").save();
                }
            }
        });
        // get Firebase auth instance
        mAuth = FirebaseAuth.getInstance();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        loadingBar = new ProgressDialog(this);

        progressBar = findViewById(R.id.progress_bar);
        imv_user_avatar_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(ProfileInputActivity.this);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // validate name
                final String username = name.getText().toString().trim();
                boolean isValidName = validateName(username);
                if (!isValidName) {
                    return;
                }

                // validate status
                final String userstatus = status.getText().toString().trim();
                boolean isValidStatus = validateStatus(userstatus);
                if (!isValidStatus) {
                    return;
                }
                //validate imageurl
                boolean isValidimageurl = validateimageurl(downloadUri);
                if (!isValidimageurl) {
                    return;
                }

//                if (phonebook.equals(null) || phonebook.equals(false)){
//
//                    Toast.makeText(getApplicationContext(),
//                            "please check add friend from your phone-book",
//                            Toast.LENGTH_SHORT).show();
//                    return;
//                }

                ///
                ////
                // perform the user creation
                progressBar.setVisibility(View.VISIBLE);
                mAuth.signOut();

                createUserOnFirebaseAuthentication(useremail, userpassword, new OnUserCreatedOnFirebaseCallback() {

                    @Override
                    public void onUserCreatedSuccess(final String userUID) {

                        final Map<String, Object> userMap = new HashMap<>();
                        userMap.put("email", useremail);
                        userMap.put("phonenumber", userphonenumber);
                        userMap.put("imageurl", downloadUri);
                        userMap.put("firstname", username);
                        userMap.put("lastname","");
                        userMap.put("status", userstatus);
                        userMap.put("timestamp", new Date().getTime());
                        userMap.put("uid", userUID);
                        if (phonebook)
                            userMap.put("autoAdd", "T");
                        else
                            userMap.put("autoAdd", "F");
                        userMap.put("friend_up", "");

                        createUserOnContacts(userUID, userMap, new OnUserCreatedOnContactsCallback() {

                            @Override
                            public void onUserCreatedSuccess() {

                                SharedPreferences.Editor editor = getSharedPreferences("loggedInfo", MODE_PRIVATE).edit();
                                editor.putString("email", ChatUI.getUser_email());
                                editor.putString("password", ChatUI.getUser_pass());
                                editor.apply();

                                Intent intent = getIntent();
                                intent.putExtra(ChatUI.BUNDLE_SIGNED_UP_USER_EMAIL, ChatUI.getUser_email());
                                intent.putExtra(ChatUI.BUNDLE_SIGNED_UP_USER_PASSWORD, ChatUI.getUser_pass());

                                if (StringUtils.isValid(ChatManager.Configuration.firebaseUrl)) {
                                    friendsNode = FirebaseDatabase.getInstance()
                                            .getReferenceFromUrl(ChatManager.Configuration.firebaseUrl)
                                            .child("friends");
                                } else {
                                    friendsNode = FirebaseDatabase.getInstance()
                                            .getReference()
                                            .child("friends");
                                }
                                // save the user on contacts node

                                friendsNode.child(userUID).child(userUID).child("status").setValue(userMap.get("autoAdd").toString());

                                addFriend(userUID, userMap.get("phonenumber").toString(), userMap.get("autoAdd").toString());

                                setResult(200, intent);
                                finish();

//                                resultFunc(200, -1, intent);
                            }


                            @Override
                            public void onUserCreatedError(Exception e) {
                                // TODO: 04/01/18
                                Toast.makeText(ProfileInputActivity.this, "Saving user on contacts failed." + e,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onUserCreatedError(Exception e) {
                        // TODO: 04/01/18  string
                        Toast.makeText(ProfileInputActivity.this, "Authentication failed." + e,
                                Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

    private void getContacts() {
        ArrayList<Contact> listContacts = new ContactFetcher(this).fetchAll();
        StringBuilder sb = new StringBuilder();
        String allPhonenumers = "";

        for(int i =0; i< listContacts.size(); i++) {
            sb.append(listContacts.get(i).getallphone());
            allPhonenumers += listContacts.get(i).getallphone();
        }

        String[] separated = allPhonenumers.split(",");

        for(int i=0; i<separated.length; i++) {
            temp.add(separated[i]);
        }
    }

    private void addFriend(final String userUID, final String myPhone, final String autoAddValue) {
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

        contactsNode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StringBuilder stringBuilder = new StringBuilder();


                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    String phonenumber = snapshot.child("phonenumber").getValue().toString();
                    String uid = snapshot.getKey();

                    if (checkPhonenumberContains(phonenumber)
                            && !phonenumber.equals(myPhone)) {
                        friendsNode.child(userUID).child(uid).child("status").setValue(autoAddValue);
                        friendsNode.child(userUID).child(uid).child("phonenumber").setValue(phonenumber);
                        friendsNode.child(uid).child(userUID).child("status").setValue("F");
                        friendsNode.child(uid).child(userUID).child("phonenumber").setValue(myPhone);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean checkPhonenumberContains(String number) {
        for (int i = 0; i < temp.size(); i++) {
            String phoneNo = temp.get(i);
            if (String.valueOf(PhoneNumberUtils.compare(phoneNo, number)).equals("true")) {
                return true;
            }
        }

        return false;
    }

    // first name validation
    private boolean validateName(String name) {
        if (!StringUtils.isValid(name)) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.chat_signup_activity_not_valid_name_label),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // last name validation
    private boolean validateStatus(String status) {
        if (!StringUtils.isValid(status)) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.chat_signup_activity_not_valid_status_label),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    // imageurl validation
    private boolean validateimageurl(String imageurl) {
        if (imageurl.equals("") || imageurl.equals(null)) {
            Toast.makeText(getApplicationContext(), "please select avatar image", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(ProfileInputActivity.this.getContentResolver(), resultUri);
                                imv_user_avatar_setting.setImageBitmap(bitmap);
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
                                    Toast.makeText(ProfileInputActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    private void createUserOnFirebaseAuthentication(final String email,
                                                    final String password,
                                                    final OnUserCreatedOnFirebaseCallback onUserCreatedOnFirebaseCallback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(ProfileInputActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        Toast.makeText(ChatSignUpActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            onUserCreatedOnFirebaseCallback.onUserCreatedError(task.getException());
                        } else {
                            // user created with success
                            FirebaseUser firebaseUser = mAuth.getCurrentUser(); // retrieve the created user
                            onUserCreatedOnFirebaseCallback.onUserCreatedSuccess(firebaseUser.getUid());
                        }
                    }
                });
    }

    private void createUserOnContacts(String key, Map<String, Object> user,
                                      final OnUserCreatedOnContactsCallback onUserCreatedOnContactsCallback) {
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

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CONTACTS}, MY_PHONEBOOK_REQUEST_CODE);
        } else {
            getContacts();
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PHONEBOOK_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "phonebook permission granted", Toast.LENGTH_LONG).show();
                getContacts();
            } else {
                Toast.makeText(this, "phonebook permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}
