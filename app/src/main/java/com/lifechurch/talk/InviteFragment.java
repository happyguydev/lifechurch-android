package com.lifechurch.talk;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by stefanodp91 on 08/01/18.
 */

public class InviteFragment extends Fragment{

    public Button make_call, send_email;
    public TextView title, message;
    public InviteFragment() {
    }

    /**
     * Returns a new instance of this fragment
     */
    public static InviteFragment newInstance() {
        InviteFragment fragment = new InviteFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.custom_alert, container, false);

        make_call = (Button) view.findViewById(R.id.send_sms);
        send_email = (Button) view.findViewById(R.id.send_email);
        title = view.findViewById(R.id.invite_title);
        message = view.findViewById(R.id.invite_message);
        title.setText(R.string.tab_invite_title);
        message.setText("Let's talk via LifeChurch app!\nhttps://play.google.com/store/apps/");
        send_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.putExtra(Intent.EXTRA_TEXT   , message.getText().toString());
                i.setType("text/plain");

                if (i.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(Intent.createChooser(i, getString(R.string.invite_to_kakao)));
                } else {
                    Toast.makeText(getActivity(), R.string.InviteActivity_no_app_to_share_to, Toast.LENGTH_LONG).show();
                }
            }
        });
        make_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inviteToSMS();
            }
        });
        return view;
    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 7) {
//            if (grantResults.length > 0
//                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            } else {
//                Toast.makeText(getActivity(), "Can't read contacts as you have declined the permission.", Toast.LENGTH_SHORT).show();
//            }
//        }
//        if (requestCode == 9) {
//            if (grantResults.length > 0
//                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//            } else {
//                //permission denied
//            }
//        }
//
//    }
    public void inviteToSMS () {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

}
