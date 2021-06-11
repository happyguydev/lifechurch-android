package org.chat21.android.ui.login.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
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
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.contacts.activites.ContactListActivity;
import org.chat21.android.ui.conversations.listeners.OnNewConversationClickListener;
import org.chat21.android.utils.StringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.chat21.android.core.contacts.synchronizers.ContactsSynchronizer.decodeContactSnapShop;
import static org.chat21.android.utils.DebugConstants.DEBUG_LOGIN;

public class VerifyPhoneActivity extends AppCompatActivity {
    private static final String TAG = "VerifyPhoneActivity";

    private Button next;
    private EditText num1,num2,num3,num4,num5,num6,count;
    String code1,code2,code3,code4,code5,code6,code,phonenumber,countnum;
    private String verificationId;
    private FirebaseAuth mAuth,mAuth1;
    private CountDownTimer mCountDownTimer;

    private static final long START_TIME_IN_MILLIS = 180000;
    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;


    private interface OnUserLookUpComplete {
        void onUserRetrievedSuccess(IChatUser loggedUser);

        void onUserRetrievedError(Exception e);
    }

    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);
        phonenumber = ChatUI.getPhone_number();
        sendVerificationCode(phonenumber);
        startTimer();
        mAuth = FirebaseAuth.getInstance();
//        mAuth1 = FirebaseAuth.getInstance();
//        progressBar = findViewById(R.id.progress_bar1);
        num1 = findViewById(R.id.num1);
        num2 = findViewById(R.id.num2);
        num3 = findViewById(R.id.num3);
        num4 = findViewById(R.id.num4);
        num5 = findViewById(R.id.num5);
        num6 = findViewById(R.id.num6);
        num1.requestFocus();
        count = findViewById(R.id.count);
//        count.setPaintFlags(0);
        num1.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                // you can call or do what you want with your EditText here

                code1 = num1.getText().toString();
                if (!code1.equals(null) && code1.length() != 0)
                    num2.requestFocus();
                // yourEditText...
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }
        });
        num2.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                // you can call or do what you want with your EditText here
                code2 = num2.getText().toString();
                if (!code2.equals(null) && code2.length() != 0)
                    num3.requestFocus();
                // yourEditText...
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
        });
        num3.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                // you can call or do what you want with your EditText here
                code3 = num3.getText().toString();
                if (!code3.equals(null) && code3.length() != 0)
                    num4.requestFocus();
                // yourEditText...
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });
        num4.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                // you can call or do what you want with your EditText here
                code4 = num4.getText().toString();
                if (!code4.equals(null) && code4.length() != 0)
                    num5.requestFocus();
                // yourEditText...
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });
        num5.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                // you can call or do what you want with your EditText here
                code5 = num5.getText().toString();
                if (!code5.equals(null) && code5.length() != 0)
                    num6.requestFocus();
                // yourEditText...
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });
        num6.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                // you can call or do what you want with your EditText here
                code6= num6.getText().toString();

                // yourEditText...
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
        });


        next = findViewById(R.id.nextBtn);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if(!code1.equals(null)  && !code2.equals(null) && !code3.equals(null) && !code4.equals(null) && !code5.equals(null)&& !code6.equals(null) &&
                        code1 != "" && code2 != "" && code3 != "" && code4 != "" && code5 != "" && code6 != ""){
                        code = code1 + code2 + code3 + code4 + code5 + code6;
                        verifyCode(code);

                }
                else
                {
                    Toast.makeText(getApplicationContext(),getString(R.string.chat_verifyphone_activity_not_valid_code_label),Toast.LENGTH_SHORT).show();
                    num1.requestFocus();
                    return;
                }
            }
        });
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        SharedPreferences.Editor editor = getSharedPreferences("MyRef", MODE_PRIVATE).edit();
        editor.putString("verifyCode", code);
        editor.apply();
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
//                          mAuth.signOut();
                            Intent intent = new Intent(VerifyPhoneActivity.this, ProfileInputActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivityForResult(intent, ChatUI.REQUEST_CODE_PROFILE_ACTIVITY);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(VerifyPhoneActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void sendVerificationCode(String number) {
//        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallBack
        );

    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
//                editText.setText(code);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(VerifyPhoneActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };


    //TODO; implemented by ilya

//
    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onStop() {
        super.onStop();

        hideProgressDialog();

    }



    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        showProgressDialog();
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
//                                                    .firebaseUrl(ChatManager.Configuration.firebaseUrl)
//                                                    .storageBucket(ChatManager.Configuration.storageBucket)
                                                    .build();


                                    ChatManager.start(VerifyPhoneActivity.this, mChatConfiguration, loggedUser);
                                    Log.i(TAG, "chat has been initialized with success");

//                                    // get device token
                                    new RefreshFirebaseInstanceIdTask().execute();

                                    ChatUI.getInstance().setContext(VerifyPhoneActivity.this);
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

                            Toast.makeText(VerifyPhoneActivity.this, "Authentication failed.",
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
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
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

        if (requestCode == ChatUI.REQUEST_CODE_PROFILE_ACTIVITY) {
            if (resultCode == RESULT_OK) {

                // set username
                String email = data.getStringExtra(ChatUI.BUNDLE_SIGNED_UP_USER_EMAIL);
//                vEmail.setText(email);

                // set password
                String password = data.getStringExtra(ChatUI.BUNDLE_SIGNED_UP_USER_PASSWORD);
//                vPassword.setText(password);

                signIn(email, password);
            }
        }
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
            public void onDataChange(DataSnapshot dataSnapshot) {
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
            public void onCancelled(DatabaseError databaseError) {
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


        String first = (String) map.get("firstname");
        String lastname = (String) map.get("status");
        String imageurl = (String) map.get("imageurl");
        String email = (String) map.get("email");
        String autoAdd = (String) map.get("autoAdd");


        IChatUser contact = new ChatUser();
        contact.setId(uid);
        contact.setEmail(email);
        contact.setFullName(first);
        contact.setProfilePictureUrl(imageurl);
        contact.setAutoAdd(autoAdd);
        Log.v(TAG, "decodeContactSnapShop.contact : " + contact);

        return contact;
    }

    private void startTimer() {
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }
            @Override
            public void onFinish() {
//                mTimerRunning = false;
//                mButtonStartPause.setText("Start");
//                mButtonStartPause.setVisibility(View.INVISIBLE);
//                mButtonReset.setVisibility(View.VISIBLE);
            }
        }.start();
//        mTimerRunning = true;
//        mButtonStartPause.setText("pause");
//        mButtonReset.setVisibility(View.INVISIBLE);
    }

    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        count.setText(timeLeftFormatted + " " + "Left");
    }


}
