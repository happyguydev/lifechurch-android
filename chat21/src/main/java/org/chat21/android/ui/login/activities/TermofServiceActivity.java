package org.chat21.android.ui.login.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import org.chat21.android.R;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.contacts.activites.ContactListActivity;

public class TermofServiceActivity extends AppCompatActivity {


   private Button next;
   private CheckBox checkbox_terms,checkbox_privacy,checkbox_age,checkbox_access,checkbox_all;
   private TextView termsTxv,privacyTxv;
   private WebView web;
   private boolean terms,privacy,age,access,all;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avtivity_agreement);
        all = true;
        checkbox_terms = findViewById(R.id.checkbox_terms);
        checkbox_privacy = findViewById(R.id.checkbox_privacy);
        checkbox_age = findViewById(R.id.checkbox_age);
        checkbox_access = findViewById(R.id.checkbox_access);
        checkbox_all = findViewById(R.id.checkbox_all);
        termsTxv = findViewById(R.id.termstxv);
        privacyTxv = findViewById(R.id.privacytxv);
        web = findViewById(R.id.webView);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        web.getSettings().setBuiltInZoomControls(true);
        // zoom if you want
        web.getSettings().setSupportZoom(true);
        // to support url redirections
        web.setWebViewClient(new WebViewClient());
        // extra settings
        web.getSettings().setLoadWithOverviewMode(false);
        web.getSettings().setUseWideViewPort(true);
        web.setScrollContainer(true);
        // setting for lollipop and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
            web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        checkbox_terms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if(isChecked)
                {
                    terms = true;
                    if (privacy == true && age == true && access == true)
                        checkbox_all.setChecked(true);
                }
                else
                {
                    terms = false;
                    all = false;
                    checkbox_all.setChecked(false);
                }
            }
        });
        termsTxv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                web.setVisibility(View.VISIBLE);
                web.loadUrl("https://www.pharmatalk.kr/pages/terms-of-service/");
            }
        });

        privacyTxv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                web.setVisibility(View.VISIBLE);
                web.loadUrl("https://www.pharmatalk.kr/pages/privacy-policy/");
            }
        });
        checkbox_privacy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if(isChecked)
                {
                    privacy = true;
                    if (terms ==true && age == true && access == true)
                        checkbox_all.setChecked(true);
                }
                else
                {
                    privacy = false;
                    all = false;
                    checkbox_all.setChecked(false);
                }
            }
        });
        checkbox_age.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if(isChecked)
                {
                    age = true;
                    if (terms ==true && privacy == true && access == true)
                        checkbox_all.setChecked(true);
                }
                else
                {
                    age = false;
                    all = false;
                    checkbox_all.setChecked(false);
                }
            }
        });
        checkbox_access.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if(isChecked)
                {
                    int result= ContextCompat.checkSelfPermission(TermofServiceActivity.this, Manifest.permission.READ_CONTACTS);
                    if (result != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(TermofServiceActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 7);
                    }
                    else{
                        access = true;
                        if (terms ==true && privacy == true && age == true)
                            checkbox_all.setChecked(true);
//            progressBar.setVisibility(View.VISIBLE);
                    }
                    access = true;
                    if (terms ==true && privacy == true && age == true)
                        checkbox_all.setChecked(true);
                }
                else
                {
                    access = false;
                    all = false;
                    checkbox_all.setChecked(false);

                }
            }
        });
        checkbox_all.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if(isChecked)
                {
                    all = true;
                    terms = true;
                    checkbox_terms.setChecked(true);
                    privacy = true;
                    checkbox_privacy.setChecked(true);

                    age = true;
                    checkbox_age.setChecked(true);

                    access = true;
                    checkbox_access.setChecked(true);

                }
                else
                {
                    all = false;
                    terms = false;
                    checkbox_terms.setChecked(false);
                    privacy = false;
                    checkbox_privacy.setChecked(false);

                    age = false;
                    checkbox_age.setChecked(false);

                    access = false;
                    checkbox_access.setChecked(false);

                }
            }
        });


        next = findViewById(R.id.nextBtn);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (all == true)
                {

                    Intent intent = new Intent(TermofServiceActivity.this, VerifyPhoneActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                else Toast.makeText(getApplicationContext(),
                        getString(R.string.chat_terms_activity_not_valid_agree_label),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        web.setVisibility(View.GONE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 7) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(TermofServiceActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 9);

            } else {
                Toast.makeText(TermofServiceActivity.this, "Can't read contacts as you have declined the permission.", Toast.LENGTH_SHORT).show();
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
