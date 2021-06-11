package org.chat21.android.ui.login.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.chat21.android.R;

public class NewPasswordActivity extends AppCompatActivity {
    private Button next;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);

        next = findViewById(R.id.nextBtn);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewPasswordActivity.this, ChatLoginActivity.class));
                finish();
            }
        });
    }

}
