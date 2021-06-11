package org.chat21.android.ui.login.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.renderscript.Script;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.chat21.android.ui.ChatUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.chat21.android.R;
import org.chat21.android.core.ChatManager;
import org.chat21.android.utils.StringUtils;

/**
 * Created by stefanodp91 on 04/01/18.
 */

public class ChatSignUpActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private FirebaseAuth mAuth;

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private EditText inputEmail;

    private EditText inputPassword;
    private EditText inputRepeatPassword;
    private Button btnSignup;
    private EditText inputPhonenumber;
    private CountryCodePicker countrycode;
    DatabaseReference contactsNode;
    private ArrayList<String> idList = new ArrayList<>();
    private ArrayList<String> phoneNoList = new ArrayList<>();
    public ArrayList<Map<String, Object>> usersList = new ArrayList<>();
    private interface OnUserCreatedOnFirebaseCallback {
        void onUserCreatedSuccess(String userUID);

        void onUserCreatedError(Exception e);
    }

    private interface OnUserCreatedOnContactsCallback {
        void onUserCreatedSuccess();

        void onUserCreatedError(Exception e);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_signup);

        // get Firebase auth instance
        mAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        progressBar = findViewById(R.id.progress_bar);

        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        inputRepeatPassword = findViewById(R.id.repeat_password);
        btnSignup = findViewById(R.id.signup);
        inputPhonenumber = findViewById(R.id.phone);
        countrycode = findViewById(R.id.ccp);

        contactsNode = FirebaseDatabase.getInstance()
                .getReference()
                .child("/apps/" + ChatManager.Configuration.appId + "/contacts/");

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // validate email
                final String email = inputEmail.getText().toString().trim();
                boolean isValidEmail = validateEmail(email);
                if (!isValidEmail) {
                    return;
                }

                // validate password and repeated password
                final String password = inputPassword.getText().toString().trim();
                String repeatedPassword = inputRepeatPassword.getText().toString().trim();
                boolean areValidPasswords = validatePassword(password, repeatedPassword);
                if (!areValidPasswords) {
                    return;
                }
                final String code = countrycode.getSelectedCountryCode();
                //validate phone
                String phonenumber = inputPhonenumber.getText().toString().trim();
                if (phonenumber.isEmpty() || phonenumber.length() < 10) {
                    inputPhonenumber.setError("Valid number is required");
                    inputPhonenumber.requestFocus();
                    return;
                }
                final String phone = "+" + code + phonenumber;

                // perform the user creation
//                progressBar.setVisibility(View.VISIBLE);
                if (isValidEmail && areValidPasswords) {

                    contactsNode.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            StringBuilder stringBuilder = new StringBuilder();
                            StringBuilder stringBuilder_1 = new StringBuilder();
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                Map<String, Object> map = (Map<String, Object>) ds.getValue();
                                String phoneNo = (String) map.get("phonenumber");
                                String emailInfo = (String) map.get("email");

                                stringBuilder.append(phoneNo + "_");
                                stringBuilder_1.append(emailInfo+ "_");
                            }
                            if (stringBuilder.toString().contains(phone + "_")) {
                                Toast.makeText(ChatSignUpActivity.this,
                                        "You can't use this number because this number already registered.\nPlease entry other phone number.",
                                        Toast.LENGTH_LONG).show();
                            } else if (stringBuilder_1.toString().contains((email + "_"))) {
                                Toast.makeText(ChatSignUpActivity.this,
                                        "You can't use this email because this email already registered.\nPlease entry other email address..",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                ChatUI.setUser_email(email);
                                ChatUI.setPhone_number(phone);
                                ChatUI.setUser_pass(password);
                                Intent intent = new Intent(ChatSignUpActivity.this, TermofServiceActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }

    // email validation
    private boolean validateEmail(String email) {
        if (!StringUtils.isValid(email) || (StringUtils.isValid(email) && !StringUtils.validateEmail(email))) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.chat_signup_activity_not_valid_email_label),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // first name validation
    private boolean validateFirstName(String firstName) {
        if (!StringUtils.isValid(firstName)) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.chat_signup_activity_not_valid_first_name_label),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // last name validation
    private boolean validateLastName(String lastName) {
        if (!StringUtils.isValid(lastName)) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.chat_signup_activity_not_valid_last_name_label),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // password validation
    private boolean validatePassword(String password, String repeatedPassword) {
        if (!StringUtils.isValid(password)) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.chat_signup_activity_not_valid_password_label),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.chat_signup_activity_not_valid_password_length_label, MIN_PASSWORD_LENGTH),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!StringUtils.isValid(repeatedPassword)) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.chat_signup_activity_not_valid_repeated_password_label),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (repeatedPassword.length() < MIN_PASSWORD_LENGTH) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.chat_signup_activity_not_valid_repeated_password_length_label, MIN_PASSWORD_LENGTH),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(repeatedPassword)) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.chat_signup_activity_passwords_mismatch_label),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    private void createUserOnFirebaseAuthentication(final String email,
                                                    final String password,
                                                    final OnUserCreatedOnFirebaseCallback onUserCreatedOnFirebaseCallback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(ChatSignUpActivity.this, new OnCompleteListener<AuthResult>() {
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
}