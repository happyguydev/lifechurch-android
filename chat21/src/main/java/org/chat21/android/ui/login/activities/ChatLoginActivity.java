package org.chat21.android.ui.login.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.contacts.activites.ContactListActivity;
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

import org.chat21.android.R;
import org.chat21.android.core.ChatManager;
import org.chat21.android.core.authentication.task.RefreshFirebaseInstanceIdTask;
import org.chat21.android.core.exception.ChatFieldNotFoundException;
import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.conversations.listeners.OnNewConversationClickListener;
import org.chat21.android.utils.LocaleHelper;
import org.chat21.android.utils.StringUtils;

import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static org.chat21.android.utils.DebugConstants.DEBUG_LOGIN;

/**
 * Created by stefanodp91 on 21/12/17.
 */

public class ChatLoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ChatLoginActivity";

    private Toolbar toolbar;
    private EditText vEmail;
    private EditText vPassword;
    private Button vLogin;
    private Button vSignUp;
    private Button vForgot;
    private ToggleButton tbLanguage;
    private FirebaseAuth mAuth;
    private String special = "";
    private CheckBox remember;
    private boolean isChecked = false;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int MY_STORAGE_REQUEST_CODE = 101;
    ProgressBar progressBar;

//    private String email, username, password;

    private interface OnUserLookUpComplete {
        void onUserRetrievedSuccess(IChatUser loggedUser);

        void onUserRetrievedError(Exception e);
    }

    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_login);

//        special = getIntent().getStringExtra("resister");
//        if (special== "ok"){
//            signIn(ChatUI.getUser_email(),ChatUI.getUser_pass());
//        }

//        requestPermissions();
        mAuth = FirebaseAuth.getInstance();

//        ChatAuthentication.getInstance().setTenant(ChatManager.getInstance().getTenant());
//        ChatAuthentication.getInstance().createAuthListener();

//        Log.d(DEBUG_LOGIN, "ChatLoginActivity.onCreate: auth state listener created ");


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        vLogin = (Button) findViewById(R.id.login);
        vLogin.setOnClickListener(this);

        vSignUp = (Button) findViewById(R.id.signup);
        vSignUp.setOnClickListener(this);

        vForgot = (Button) findViewById(R.id.forgot);
        vForgot.setOnClickListener(this);

        vEmail = (EditText) findViewById(R.id.email);
        vPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setVisibility(View.GONE);
        SharedPreferences prefs = getSharedPreferences("loggedInfo", MODE_PRIVATE);
        if (prefs != null) {
            String email = prefs.getString("email", "");
            String pass = prefs.getString("password", "");
            vEmail.setText(email);
            vPassword.setText(pass);
        }

        remember = (CheckBox) findViewById(R.id.checkbox_remember);
        remember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isChecked) {
                    remember.setChecked(true);
                    isChecked = true;
                    SharedPreferences.Editor editor = getSharedPreferences("loggedInfo", MODE_PRIVATE).edit();
                    editor.putString("email", vEmail.getText().toString());
                    editor.putString("password", vPassword.getText().toString());
                    editor.apply();
                } else {
                    remember.setChecked(false);
                    isChecked = false;
                    SharedPreferences.Editor editor = getSharedPreferences("loggedInfo", MODE_PRIVATE).edit();
                    editor.putString("email", "");
                    editor.putString("password", "");
                    editor.apply();
                }
            }
        });

        setToggleButton();
        initPasswordIMEAction();
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CONTACTS}, MY_CAMERA_REQUEST_CODE);
        }else {
            return;
        }

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {

        super.onStop();

//        hideProgressDialog();

    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        if (viewId == R.id.login) {
            signIn(vEmail.getText().toString(), vPassword.getText().toString());
//            performLogin();
        } else if (viewId == R.id.signup) {
            startSignUpActivity();
        } else if (viewId == R.id.forgot) {

            startResetPasswordActivity();
        }
    }

    private void setToggleButton() {
        tbLanguage = (ToggleButton) findViewById(R.id.toggleLanguage);
        String dd = LocaleHelper.getLanguage(this);
        switch (dd) {
            case "ko":
                tbLanguage.setChecked(true);
                break;
            case "en":
                tbLanguage.setChecked(false);
                break;
        }

        tbLanguage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    LocaleHelper.setLocale(ChatLoginActivity.this, "ko");
                } else {
                    LocaleHelper.setLocale(ChatLoginActivity.this, "en");
                }
                ChatLoginActivity.this.recreate();
            }
        });
    }

    private void initPasswordIMEAction() {
        Log.d(DEBUG_LOGIN, "initPasswordIMEAction");

        /**
         * on ime click
         * source:
         * http://developer.android.com/training/keyboard-input/style.html#Action
         */
        vPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.d(DEBUG_LOGIN, "ChatLoginActivity.initPasswordIMEAction");

//                    performLogin();
                    signIn(vEmail.getText().toString(), vPassword.getText().toString());

                    handled = true;
                }
                return handled;
            }
        });
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);

        vEmail.setText(email);
        vPassword.setText(password);

        if (!validateForm()) {
            return;
        }

//        showProgressDialog();
        progressBar.setVisibility(View.VISIBLE);

        if (!remember.isChecked()) {
            SharedPreferences.Editor editor = getSharedPreferences("loggedInfo", MODE_PRIVATE).edit();
            editor.putString("email", "");
            editor.putString("password", "");
            editor.apply();
        }

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            lookUpContactById(user.getUid(), new OnUserLookUpComplete() {
                                @Override
                                public void onUserRetrievedSuccess(IChatUser loggedUser) {
                                    Log.d(TAG, "ChatLoginActivity.signInWithEmail.onUserRetrievedSuccess: loggedUser == " + loggedUser.toString());

                                    ChatManager.Configuration mChatConfiguration =
                                            new ChatManager.Configuration.Builder(ChatManager.Configuration.appId)
                                                    .build();

                                    ChatManager.start(ChatLoginActivity.this, mChatConfiguration, loggedUser);
                                    Log.i(TAG, "chat has been initialized with success");

//                                    // get device token
                                    new RefreshFirebaseInstanceIdTask().execute();

                                    ChatUI.getInstance().setContext(ChatLoginActivity.this);
                                    Log.i(TAG, "ChatUI has been initialized with success");

                                    ChatUI.getInstance().enableGroups(true);

                                    // set on new conversation click listener
                                    // final IChatUser support = new ChatUser("support", "Chat21 Support");
                                    final IChatUser support = null;
                                    ChatUI.getInstance().setOnNewConversationClickListener(new OnNewConversationClickListener() {
                                        @Override
                                        public void onNewConversationClicked() {
                                            if (support != null) {
                                                ChatUI.getInstance().openConversationMessagesActivity(support);
                                            } else {
                                                Intent intent = new Intent(getApplicationContext(),
                                                        ContactListActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // start activity from context
                                                startActivity(intent);
                                            }
                                        }
                                    });

                                    Log.i(TAG, "ChatUI has been initialized with success");

                                    setResult(Activity.RESULT_OK);
                                    finish();
                                }

                                @Override
                                public void onUserRetrievedError(Exception e) {
                                    Log.d(TAG, "ChatLoginActivity.signInWithEmail.onUserRetrievedError: " + e.toString());
                                }
                            });

                            // enable persistence must be made before any other usage of FirebaseDatabase instance.
                            try {
                                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
                            } catch (DatabaseException databaseException) {
                                Log.w(TAG, databaseException.toString());
                            } catch (Exception e) {
                                Log.w(TAG, e.toString());
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(ChatLoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

//                            setResult(Activity.RESULT_CANCELED);
//                            finish();
//                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
//                            setResult(Activity.RESULT_CANCELED);
//                            finish();
//                            mStatusTextView.setText(R.string.auth_failed);
                        }
                        hideProgressDialog();
                        progressBar.setVisibility(View.GONE);
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

    private void startSignUpActivity() {
//        Intent intent = new Intent(ChatLoginActivity.this, ChatSignUpActivity.class);
//        startActivityForResult(intent, ChatUI.REQUEST_CODE_SIGNUP_ACTIVITY);

        Intent intent = new Intent(ChatLoginActivity.this, ChatSignUpActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, ChatUI.REQUEST_CODE_PROFILE_ACTIVITY);

//        startActivity(intent);
    }


    private void startResetPasswordActivity() {
        Intent intent = new Intent(ChatLoginActivity.this, ResetPasswordActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = vEmail.getText().toString();
        if (!StringUtils.isValid(email)) {
            vEmail.setError("Required.");
            valid = false;
        } else if (StringUtils.isValid(email) && !StringUtils.validateEmail(email)) {
            vEmail.setError("Not valid email.");
            valid = false;
        } else {
            vEmail.setError(null);
        }
        String password = vPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            vPassword.setError("Required.");
            valid = false;
        } else {
            vPassword.setError(null);
        }

        return valid;
    }


    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
//            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (requestCode == ChatUI.REQUEST_CODE_PROFILE_ACTIVITY) {
            if (resultCode == 0) {

                // set username
                String email = ChatUI.getUser_email();
//                vEmail.setText(email);

                // set password
                String password = ChatUI.getUser_pass();
//                vPassword.setText(password);

                signIn(email, password);
            }
//        }
    }

    private void lookUpContactById(String userId, final OnUserLookUpComplete onUserLookUpComplete) {
        DatabaseReference contactsNode;
        if (StringUtils.isValid(ChatManager.Configuration.firebaseUrl)) {
            contactsNode = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(ChatManager.Configuration.firebaseUrl)
                    .child("/apps/" + ChatManager.Configuration.appId + "/contacts/" + userId);
        } else {
            contactsNode = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("/apps/" + ChatManager.Configuration.appId + "/contacts/" + userId);
        }

        contactsNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(DEBUG_LOGIN, "ChatLoginActivity.lookUpContactById: dataSnapshot == " + dataSnapshot.toString());

                if (dataSnapshot.getValue() != null) {
                    try {
                        IChatUser loggedUser = decodeContactSnapShop(dataSnapshot);
                        Log.d(DEBUG_LOGIN, "ChatLoginActivity.lookUpContactById.onDataChange: loggedUser == " + loggedUser.toString());
                        onUserLookUpComplete.onUserRetrievedSuccess(loggedUser);
                    } catch (ChatFieldNotFoundException e) {
                        Log.e(DEBUG_LOGIN, "ChatLoginActivity.lookUpContactById.onDataChange: " + e.toString());
                        onUserLookUpComplete.onUserRetrievedError(e);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(DEBUG_LOGIN, "ChatLoginActivity.lookUpContactById: " + databaseError.toString());
                onUserLookUpComplete.onUserRetrievedError(databaseError.toException());
            }
        });
    }

    private static IChatUser decodeContactSnapShop(DataSnapshot dataSnapshot) throws ChatFieldNotFoundException {
        Log.v(TAG, "decodeContactSnapShop called");

        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

//        String contactId = dataSnapshot.getKey();

        String uid = (String) map.get("uid");
        if (uid == null) {
            throw new ChatFieldNotFoundException("Required uid field is null for contact id : " + uid);
        }

//        String timestamp = (String) map.get("timestamp");

        String firstname = (String) map.get("firstname");
        String imageurl = (String) map.get("imageurl");
        String email = (String) map.get("email");
        String phone = (String) map.get("phonenumber");
        String autoAdd = (String) map.get("autoAdd");

        IChatUser contact = new ChatUser();
        contact.setId(uid);
        contact.setEmail(email);
        contact.setFullName(firstname);
        contact.setPhonenumber(phone);
        contact.setProfilePictureUrl(imageurl);
        contact.setAutoAdd(autoAdd);

        Log.v(TAG, "decodeContactSnapShop.contact : " + contact);

        return contact;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "phonebook permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "phonebook permission denied", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == MY_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}