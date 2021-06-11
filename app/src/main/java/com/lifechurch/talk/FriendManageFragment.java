package com.lifechurch.talk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.iamhabib.easy_preference.EasyPreference;

import org.chat21.android.core.ChatManager;
import org.chat21.android.core.users.models.IChatUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FriendManageFragment extends Fragment {
    private static final String TAG = ChurchFragment.class.getName();
    private Switch Autoadd,Profilesync;
    private ImageView Updatesync;
    private TextView Updatetxt,Bolckusermanage;
    DatabaseReference autoAddNode;
    IChatUser loggedUser;

    String newDateStr;
    String strAutoAdd;

    public static Fragment newInstance() {
        FriendManageFragment fragment = new FriendManageFragment();

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_frined_manage, container, false);
        Updatetxt = view.findViewById(R.id.updatetxt);
        Updatetxt.setText("Updated on " + EasyPreference.with(getActivity().getApplicationContext()).getString("updatesync", ""));

        loggedUser = ChatManager.getInstance().getLoggedUser();
        autoAddNode = FirebaseDatabase.getInstance().getReference()
                .child("/apps/" + ChatManager.Configuration.appId + "/contacts/").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("autoAdd");

        strAutoAdd = loggedUser.getAutoAdd();
        Autoadd = view.findViewById(R.id.autoaddswitch);
        if (strAutoAdd.equalsIgnoreCase("T"))
            Autoadd.setChecked(true);
        else
            Autoadd.setChecked(false);

//        if (!EasyPreference.with(getActivity().getApplicationContext()).getString("autoadd", "").equals("no")){
//            Autoadd.setChecked(true);
//        }
//        else{
//            Autoadd.setChecked(false);
//        }

        Autoadd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    EasyPreference.with(getActivity().getApplicationContext()).addString("autoadd", "ok").save();
                    strAutoAdd = "T";
                } else {
                    // The toggle is disabled
                    EasyPreference.with(getActivity().getApplicationContext()).addString("autoadd", "no").save();
                    strAutoAdd = "F";
                }
                autoAddNode.setValue(strAutoAdd);
            }
        });

        Profilesync = view.findViewById(R.id.syncswitch);
        if (!EasyPreference.with(getActivity().getApplicationContext()).getString("profilesync", "").equals("no")){
            Profilesync.setChecked(true);
        }
        else{
            Profilesync.setChecked(false);
        }
        Profilesync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    EasyPreference.with(getActivity().getApplicationContext()).addString("profilesync", "ok").save();

                } else {
                    // The toggle is disabled
                    EasyPreference.with(getActivity().getApplicationContext()).addString("profilesync", "no").save();

                }
            }
        });
        Updatetxt = view.findViewById(R.id.updatetxt);
        Updatesync = view.findViewById(R.id.updatesync);
        Updatesync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Date dateObj = Calendar.getInstance().getTime();
                SimpleDateFormat postFormater = new SimpleDateFormat("MMMM dd, yyyy");
                String newDateStr = postFormater.format(dateObj);
                newDateStr = postFormater.format(dateObj);
                String updatetext = "Updated on " + newDateStr;
                EasyPreference.with(getActivity().getApplicationContext()).addString("updatesync", newDateStr).save();
                Updatetxt.setText(updatetext);
            }
        });


        Bolckusermanage = view.findViewById(R.id.bolckusermanage);
        Bolckusermanage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BlockUserActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

}
