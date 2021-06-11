package com.lifechurch.talk;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.chat21.android.core.ChatManager;
import org.chat21.android.utils.image.CropCircleTransformation;

import java.util.ArrayList;

public class BlockUserAdapter extends ArrayAdapter<BlockUser> {

     DatabaseReference blockDatabase;
     int checkedstate = 0;
    public BlockUserAdapter(Context context, ArrayList<BlockUser> blockusers) {
        super(context, 0, blockusers);
    }
//    Boolean messageblock,profileblock,reportuser,unblock;
    String blockStatus;
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        // Get the data item
        final BlockUser blockUser = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.adapter_blockuser_item, parent, false);
        }
        // Populate the data into the template view using the data object
        TextView tvName = (TextView) view.findViewById(R.id.fullname);
        TextView tvStatus = (TextView) view.findViewById(R.id.blockstatus);
        Button btnmanage = (Button) view.findViewById(R.id.manageblockbtn);

        ImageView avatar = (ImageView) view.findViewById(R.id.profile_picture);
        tvName.setText(blockUser.name);
        Glide.with(view.getContext())
                .load(blockUser.image)
                .placeholder(org.chat21.android.R.drawable.ic_person_avatar)
                .bitmapTransform(new CropCircleTransformation(view.getContext()))
                .into(avatar);
        tvStatus.setText(blockUser.status);




        btnmanage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Set up the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Manage Block for " + "<" + blockUser.name + ">");
                final String[] items = {"Block Message", "Block Message,Profile",  "Unblock"};
                if(blockUser.status.equals("Unblocked")){
                    checkedstate = 2;
                }else if(blockUser.status.equals("Message Blocked")){
                    checkedstate = 0;
                }else if(blockUser.status.equals("Message, Profile Blocked")){
                    checkedstate = 1;
                }


                builder.setSingleChoiceItems(items, checkedstate, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {

                        switch(item)
                        {
                            case 0:

//                                Toast.makeText(v.getContext(), "Block Message Clicked", Toast.LENGTH_LONG).show();
                                blockStatus = "Message Blocked";

                                break;
                            case 1:

//                                Toast.makeText(v.getContext(), "Block Message,Profile Clicked", Toast.LENGTH_LONG).show();
                                blockStatus = "Message, Profile Blocked";

                                break;
                            case 2:

//                                Toast.makeText(v.getContext(), "Unblock Clicked", Toast.LENGTH_LONG).show();
                                blockStatus = "Unblocked";
                                break;
                        }
//                        dialog.dismiss();
                    }
                });





                // Add OK and Cancel buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // The user clicked OK
                        blockDatabase = FirebaseDatabase.getInstance().getReference().child("/apps/" + ChatManager.Configuration.appId + "/BlockUser/");
                        blockDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(blockUser.id).child("blockStatus").setValue(blockStatus);

                        ((Activity)getContext()).finish();

                        Intent intent= new Intent(getContext(), BlockUserActivity.class);
                        getContext().startActivity(intent);
                    }
                });
                builder.setNegativeButton("Cancel", null);

                // Create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();


            }
        });


        return view;
    }




}