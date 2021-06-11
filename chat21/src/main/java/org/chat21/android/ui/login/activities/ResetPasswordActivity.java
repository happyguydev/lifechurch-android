package org.chat21.android.ui.login.activities;

import android.app.AppComponentFactory;
import android.content.Intent;
import android.content.RestrictionEntry;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.chat21.android.R;

public class ResetPasswordActivity extends AppCompatActivity {

    FirebaseAuth auth;
    private EditText email;
    private String useremail;

    private ProgressBar progressBar;
    private Button next;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        email = findViewById(R.id.email);
        progressBar = findViewById(R.id.progress_bar);
        next = findViewById(R.id.nextBtn);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setVisibility(View.VISIBLE);
                useremail = email.getText().toString().trim();

                auth.getInstance().sendPasswordResetEmail(useremail)

                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ResetPasswordActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(ResetPasswordActivity.this, ChatLoginActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    Toast.makeText(ResetPasswordActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();

                                }

                                progressBar.setVisibility(View.GONE);
                            }
                        });




            }
        });
    }
}
