package com.lifechurch.talk;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import com.lifechurch.talk.adapter.FeedbackAdapter;
import com.lifechurch.talk.model.Feedback;


public class FeedbackListActivity extends AppCompatActivity {

    DatabaseReference databaseReference;
    ListView FeedbackList;
    ProgressDialog pd;
    List<Feedback> feedbackitems = new ArrayList<>();
    FeedbackAdapter FeedbackAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedbacklist);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("feedback");

        pd = new ProgressDialog(FeedbackListActivity.this);
        pd.setMessage("Loading...");
        pd.show();

        FeedbackList = findViewById(R.id.feedbackList);

        FeedbackAdapter = new FeedbackAdapter(FeedbackListActivity.this,feedbackitems);

        FeedbackList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Feedback Item = feedbackitems.get(i);

                String name = Item.getfirstname();
                String state = Item.getstate();
                String image=Item.getimageurl();
                String text=Item.gettext();

                ViewUserDetail(name,text,image,state);

            }
        });


    }

    public void ViewUserDetail(String Name, final String text, final String image, String state) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(FeedbackListActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.feedbackview_dialog, null);
        dialogBuilder.setView(dialogView);

        TextView nameTV = (TextView) dialogView.findViewById(R.id.editTextFullName);
        TextView textTV = (TextView) dialogView.findViewById(R.id.detail);
        ImageView imageAvatar = (ImageView) dialogView.findViewById(R.id.Profile_image);
        ImageView imageStatus = (ImageView) dialogView.findViewById(R.id.state);

        nameTV.setText(Name);
        textTV.setText(text);

        Picasso.get().load(image).into(imageAvatar);

        if (state.equals("100")){
            Picasso.get().load(R.drawable.feel1).into(imageStatus);
        }else if(state.equals("75")){
            Picasso.get().load(R.drawable.feel2).into(imageStatus);
        }else if(state.equals("50")){
            Picasso.get().load(R.drawable.feel3).into(imageStatus);
        }else if(state.equals("25")){
            Picasso.get().load(R.drawable.feel4).into(imageStatus);
        }else {
            Picasso.get().load(R.drawable.feel5).into(imageStatus);
        }

        final AlertDialog b = dialogBuilder.create();
        b.show();


    }

    @Override
    public void onStart() {
        super.onStart();
        databaseReference.addValueEventListener(mValueEventListener);

    }

    ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            //clearing the previous Item list
            feedbackitems.clear();
            //getting all nodes
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                //getting Item from firebase console
                Feedback Item = postSnapshot.getValue(Feedback.class);

                feedbackitems.add( Item );
            }

            FeedbackList.setAdapter(FeedbackAdapter);
            pd.dismiss();

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

}