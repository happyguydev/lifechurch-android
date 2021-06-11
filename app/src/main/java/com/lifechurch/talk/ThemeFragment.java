package com.lifechurch.talk;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;


import com.lifechurch.talk.usage.LocaleHelper;
import yuku.ambilwarna.AmbilWarnaDialog;

import static android.content.Context.MODE_PRIVATE;

public class ThemeFragment<prefs> extends Fragment {
    private static final String TAG = ThemeFragment.class.getName();
    private SeekBar bar;
    private TextView view;
    private RadioGroup radiolanGroup;
    private RadioButton KO, EN;
    private RadioButton radioButton;
    private ImageView ThemeColor,default_color;
    private Button btn_default_color,btn_default_font;
    private int color;
    private float fontsize;
//    int color = EasyPreference.with(ThemeFragment.this.getContext()).getInt("backgroundcolor",-1);
//    int color =            PreferenceManager.getDefaultSharedPreferences(getActivity(),-1);
    private SharedPreferences prefs;
    private Context context;

    public static Fragment newInstance() {
        ThemeFragment fragment = new ThemeFragment();

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_theme, container, false);
        context = getContext();

        prefs  = context.getSharedPreferences("themeinfo", MODE_PRIVATE);
        if (prefs != null) {
            color = prefs.getInt("setbackgroundcolor", Color.parseColor("#B2C7DA"));
            fontsize = prefs.getFloat("setfontsize",14);
        }

        view = root.findViewById(R.id.changeFont);
        bar = root.findViewById(R.id.seekBar);
        radiolanGroup = root.findViewById(R.id.radioLanguage);
        ThemeColor = root.findViewById(R.id.themecolor);
        KO = root.findViewById(R.id.radioko);
        EN = root.findViewById(R.id.radioen);
        btn_default_color = root.findViewById(R.id.btn_default_color);
        btn_default_font = root.findViewById(R.id.btn_default_font);

        initTheme();

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                view.setTextSize(Float.valueOf(i));
                Log.v("##", String.valueOf(Float.valueOf(i)));
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("themeinfo", MODE_PRIVATE).edit();
                editor.putFloat("setfontsize", Float.valueOf(i));
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        radiolanGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find the radiobutton by returned id
                radioButton = root.findViewById(checkedId);
                Log.v("***", (String) radioButton.getText());
                String lan = String.valueOf(radioButton.getText());

                // checkedId is the RadioButton selected
                if(lan.equals("Korean")) {
                    LocaleHelper.setLocale(context, "ko");
                } else {
                    LocaleHelper.setLocale(context, "en");
                }

                ThemeFragment themefragment = (ThemeFragment) ThemeFragment.newInstance();
                getFragmentManager().beginTransaction()
                        .add(R.id.container, themefragment, ThemeFragment.class.getName())
                        .commit();
            }
        });

        ThemeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog(true);
            }
        });

        btn_default_font.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                view.setTextSize(15);
                Log.v("##", String.valueOf(15));
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("themeinfo", MODE_PRIVATE).edit();
                editor.putFloat("setfontsize", 15);
                editor.apply();
            }
        });
        btn_default_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThemeColor.setBackgroundColor(Color.parseColor("#B2C7DA"));
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("themeinfo", MODE_PRIVATE).edit();
                editor.putInt("setbackgroundcolor", Color.parseColor("#B2C7DA"));
                editor.apply();
            }
        });

        return root;
    }

    private void initTheme() {
        ThemeColor.setBackgroundColor(color);

        String dd = LocaleHelper.getLanguage(context);
        switch (dd) {
            case "ko":
                KO.setChecked(true);
                break;
            case "en":
                EN.setChecked(true);
                break;
        }

        bar.setProgress((int) fontsize);
        view.setTextSize(fontsize);
    }

    void openDialog(boolean supportsAlpha) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(getActivity(), color, supportsAlpha, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
//                Toast.makeText(getActivity(), "ok", Toast.LENGTH_SHORT).show();
                ThemeFragment.this.color = color;
                displayColor();
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
//                Toast.makeText(getActivity(), "cancel", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }
    void displayColor() {
        ThemeColor.setBackgroundColor(color);

        SharedPreferences.Editor editor = getActivity().getSharedPreferences("themeinfo", MODE_PRIVATE).edit();
        editor.putInt("setbackgroundcolor", color);
        editor.apply();
    }


}

