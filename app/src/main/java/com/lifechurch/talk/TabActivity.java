package com.lifechurch.talk;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import org.chat21.android.core.ChatManager;
import org.chat21.android.ui.ChatUI;

import java.util.HashMap;
import java.util.Map;

public class TabActivity extends AppCompatActivity {
    private static final String TAG = TabActivity.class.getName();

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private boolean isClicked = false;
    private String flag  = "1";
    ImageView setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        mViewPager.setAdapter(mSectionsPagerAdapter);



        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        ChatUI.getInstance().processRemoteNotification(getIntent());


        final int[] ICONS = new int[]{
                R.drawable.contact,
                R.drawable.message,
                R.drawable.invite,
                R.drawable.church};


        tabLayout.getTabAt(0).setIcon(ICONS[0]);
        tabLayout.getTabAt(1).setIcon(ICONS[1]);
        tabLayout.getTabAt(2).setIcon(ICONS[2]);
        tabLayout.getTabAt(3).setIcon(ICONS[3]);



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageView Setting = (ImageView) toolbar.findViewById(R.id.setting);

        Setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TabActivity.this, SettingActivity.class);
                startActivity(intent);

            }
        });

    }

    @Override
    protected void onResume() {
        ChatManager.getInstance().getMyPresenceHandler().connect();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        ChatManager.getInstance().getMyPresenceHandler().dispose();
        super.onDestroy();
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private int tabsCount;
        private Map<String, Item> tabsMap;
        private String[] tabsTags;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

            tabsMap = new HashMap<>();

            // create a map with all tabs
            tabsMap.put(getString(R.string.tag_home),
                    new Item(getString(R.string.tab_home_title),0));
            tabsMap.put(getString(R.string.tag_chat),
                    new Item(getString(R.string.tab_chat_title), 1));
            tabsMap.put(getString(R.string.tag_invite),
                    new Item(getString(R.string.tab_invite_title), 2));
            tabsMap.put(getString(R.string.tag_profile),
                    new Item(getString(R.string.tab_church_title), 3));

            // retrieve tab tags
            tabsTags = getResources().getStringArray(R.array.tab_tags);
            tabsCount = tabsTags.length; // count tab tags
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.

            if (flag.equals("1")) {
                String tabTag = getTagByPosition(position);
                if (tabTag.equals(getString(R.string.tag_home))) {
                    return HomeFragment.newInstance();
                } else if (tabTag.equals(getString(R.string.tag_chat))) {
                    return ChatFragment.newInstance();
                } else if (tabTag.equals(getString(R.string.tag_invite))) {
                    return InviteFragment.newInstance();
                } else if (tabTag.equals(getString(R.string.tag_profile))) {
                    return ChurchFragment.newInstance();
                } else {
                    // default load home
                    return HomeFragment.newInstance();
                }
            }
            flag = "";
            return HomeFragment.newInstance();

        }

        @Override
        public int getCount() {
            return tabsCount;
        }

        private String getTagByPosition(int position) {
            return tabsTags[position];
        }

        private String getTitleByTag(String tag) {
            return tabsMap.get(tag).getTitle();
        }

        private int getIconByTag(String tag) {
            return tabsMap.get(tag).getIcon();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String tabTag = getTagByPosition(position);
            return getTitleByTag(tabTag);
        }

        private class Item {
            private String title;
            private int icon;

            private Item(String title, int  icon) {
                setTitle(title);
                setIcon(icon);
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public int getIcon() {
                return icon;
            }

            public void setIcon(int icon) {
                this.icon=icon;

            }

            @Override
            public String toString() {
                return "Item{" +
                        "title='" + title + '\'' +
                        ", icon='" + icon + '\'' +
                        '}';
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == R.id.setting) {
//            Log.v("clicked?", "clicked");
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 7) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent starterIntent = getIntent();
                finish(); startActivity(starterIntent);
            } else {
                Toast.makeText(TabActivity.this, "Can't read contacts as you have declined the permission.", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == 9) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(TabActivity.this, "Can't access your camera as you have declined the permission.", Toast.LENGTH_SHORT).show();
            }
        }

    }
}