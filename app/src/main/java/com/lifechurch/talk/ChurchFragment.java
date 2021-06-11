package com.lifechurch.talk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class ChurchFragment extends Fragment {
    private static final String TAG = ChurchFragment.class.getName();

    private RelativeLayout lifePreach;

    public static Fragment newInstance() {
        ChurchFragment fragment = new ChurchFragment();

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_church, container, false);
        lifePreach = (RelativeLayout) view.findViewById(R.id.lightoflife);
        lifePreach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), WebViewActivity.class));
            }
        });
        return view;
    }

}

