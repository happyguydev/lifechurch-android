package org.chat21.android.ui.contacts.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iamhabib.easy_preference.EasyPreference;
import com.squareup.picasso.Picasso;

import org.chat21.android.core.contacts.synchronizers.Contact;
import org.chat21.android.core.contacts.synchronizers.ContactFetcher;
import org.chat21.android.core.messages.models.Message;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.contacts.adapters.ContactListAdapter;
import org.chat21.android.ui.contacts.listeners.OnContactClickListener;
import org.chat21.android.ui.decorations.ItemDecoration;

import org.chat21.android.R;
import org.chat21.android.core.ChatManager;
import org.chat21.android.core.contacts.synchronizers.ContactsSynchronizer;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.messages.activities.MessageListActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by stefanodp91 on 02/03/18.
 */

public class ContactsListFragment extends Fragment {
    private static final String TAG = ContactsListFragment.class.getName();

    private ContactsSynchronizer contactsSynchronizer;
    private OnContactClickListener onContactClickListener;

    private SearchView searchView;

    // contacts list recyclerview
    private RecyclerView recyclerViewContacts;
    private LinearLayoutManager lmRvContacts;
    private ContactListAdapter contactsListAdapter;
    // no contacts layout
    private RelativeLayout noContactsLayout;
    // phone number match
    private CopyOnWriteArrayList<IChatUser> lists = new CopyOnWriteArrayList<IChatUser>();
    private CopyOnWriteArrayList<IChatUser> newLists = new CopyOnWriteArrayList<IChatUser>();
    DatabaseReference contactsNode,friendsnode,friendsnode1,myContact,myPhonenode,myfriendsnode,newUsernode, friendAutoAddNode;
    ArrayList<String> comparedKeys = new ArrayList<>();
    ArrayList<String> dbphones = new ArrayList<>();
    ArrayList<String> dbkeys = new ArrayList<>();
    ArrayList<String> temp=new ArrayList<>();
    int i;
    private Context mContext;
    ArrayList<Contact> listContacts;
    StringBuilder sb = new StringBuilder();
    ArrayList<String> localphones = new ArrayList<>();
    String allPhonenumers = "";
    ///////by ilya contact list import
    ImageView profile_picture;
    TextView fullname,email,phonenumber;
    LinearLayout  Himself_area;
    IChatUser loggedUser;
    String myPhone,newUser, autoAddValue, friendAutoValue;
    ProgressBar progressBar;
    //////////////////////

    public static Fragment newInstance() {
        Fragment mFragment = new ContactsListFragment();
        return mFragment;
    }

    @Override
    public void onCreate(@org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listContacts = new ContactFetcher(getActivity()).fetchAll();

        for(int i =0; i< listContacts.size(); i++) {
            sb.append(listContacts.get(i).getallphone());
            allPhonenumers += listContacts.get(i).getallphone();

            localphones.add(listContacts.get(i).getallphone());
        }

        String[] separated = allPhonenumers.split(",");

        for(int i=0; i<separated.length; i++) {
           // String[] array = separated[i].split("-");
            temp.add(separated[i]);
        }
//        Log.d("1111111111111111111", "Contact List:::" + sb.toString());
//        Log.v("1111111111111111111", "Temp value:::" + String.valueOf(temp));
//        Log.v("1111111111111111111", "Compare Value" + String.valueOf(PhoneNumberUtils.compare("+11234567890",  " (012) 3456-7890")));

//        PhoneNumberUtils.compare("+11234567890",  "+1 (123) 456-7890");
        EasyPreference.with(getActivity()).addString("phonebookdata", sb.toString()).save();
        setHasOptionsMenu(true);

        contactsSynchronizer = ChatManager.getInstance().getContactsSynchronizer();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "ContactsListFragment.onCreateView");
        final View view = inflater.inflate(R.layout.fragment_contacts_list, container, false);
//////ilya added contactlist activity////////
//        lists.clear();
//        newLists.clear();
//////////////
        profile_picture = view.findViewById(R.id.profile_picture);
        email = view.findViewById(R.id.email);
        fullname = view.findViewById(R.id.fullname);
        phonenumber = view.findViewById(R.id.phonenumber);
        Himself_area = view.findViewById(R.id.himself_area);
        // init RecyclerView
        recyclerViewContacts = view.findViewById(R.id.contacts_list);
        recyclerViewContacts.addItemDecoration(new ItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL,
                getResources().getDrawable(R.drawable.decorator_activity_contact_list)));
        lmRvContacts = new LinearLayoutManager(getActivity());
        recyclerViewContacts.setLayoutManager(lmRvContacts);
        loggedUser = ChatManager.getInstance().getLoggedUser();

        lists = contactsSynchronizer.getContacts();
        progressBar = (ProgressBar) view.findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);
        noContactsLayout = view.findViewById(R.id.layout_no_contacts);

        friendsnode1 = FirebaseDatabase.getInstance().getReference().child("friends");
        friendsnode = FirebaseDatabase.getInstance().getReference().child("friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        myfriendsnode = FirebaseDatabase.getInstance().getReference().child("friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
//        newUsernode = FirebaseDatabase.getInstance().getReference().child("friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("new user");
        myContact = FirebaseDatabase.getInstance().getReference()
                .child("/apps/" + ChatManager.Configuration.appId + "/contacts/").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status");
        contactsNode = FirebaseDatabase.getInstance().getReference()
            .child("/apps/" + ChatManager.Configuration.appId + "/contacts/");
        myPhonenode = FirebaseDatabase.getInstance()
                .getReference()
                .child("/apps/" + ChatManager.Configuration.appId + "/contacts/" + loggedUser.getId());
        myPhonenode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myPhone = dataSnapshot.child("phonenumber").getValue(String.class);
                autoAddValue = dataSnapshot.child("autoAdd").getValue(String.class);

                init_table();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //self click
        Himself_area.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                IChatUser contact1 = null;
                Intent intent = new Intent(getActivity(), MessageListActivity.class);
                intent.putExtra(ChatUI.BUNDLE_RECIPIENT, loggedUser);
                intent.putExtra(ChatUI.BUNDLE_CHANNEL_TYPE, Message.DIRECT_CHANNEL_TYPE);
                startActivity(intent);
                // finish the contact list activity when it start a new conversation
            }
        });

        return view;
    }

    private void init_table() {
        friendsnode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    if (snapshot.child("phonenumber").getValue() != null) {
                        String phonenumber = snapshot.child("phonenumber").getValue().toString();
                        String status = snapshot.child("status").getValue().toString();
                        String uid = snapshot.getKey();

                        if (status.equals("F")) {
                            if (checkPhonenumberContains(phonenumber)
                                    && !phonenumber.equals(myPhone)) {
                                if (autoAddValue.equals("T")) {
                                    friendsnode.child(uid).child("status").setValue(autoAddValue);
                                    IChatUser ft = getChatUserById(uid);
                                    if (ft != null && checkUserInFriendList(ft)) {
                                        newLists.add(ft);
                                    }
                                }
                            }
                        } else {
                            if (!phonenumber.equals(myPhone)) {
                                IChatUser ft = getChatUserById(uid);
                                if (ft != null && checkUserInFriendList(ft)) {
                                    newLists.add(ft);
                                }
                            }
                        }
                    }
                }

                for (int i =0; i<lists.size(); i++) {
                    if (lists.get(i).getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        fullname.setText(lists.get(i).getFullName());
                        Picasso.get().load(lists.get(i).getProfilePictureUrl()).into(profile_picture);

                        myContact.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot2) {
                                email.setText(dataSnapshot2.getValue().toString());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
//                    Log.d("1111111111111", "Auto Add value::::" + loggedUser.getAutoAdd() + ":::" + newLists);
                HashSet<IChatUser> hashSet = new HashSet<IChatUser>();
                hashSet.addAll(newLists);
                newLists.clear();
                newLists.addAll(hashSet);
                updateContactListAdapter(newLists);
                progressBar.setVisibility(View.GONE);

                // no contacts layout
                toggleNoContactsLayoutVisibility(contactsListAdapter.getItemCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean checkPhonenumberContains(String number) {
        for (int i = 0; i < temp.size(); i++) {
            String phoneNo = temp.get(i);
            if (String.valueOf(PhoneNumberUtils.compare(phoneNo, number)).equals("true")) {
                return true;
            }
        }

        return false;
    }

    private IChatUser getChatUserById(String id) {
        for (int i =0; i<lists.size(); i++) {
            if (lists.get(i).getId().equals(id)) {
                return lists.get(i);
            }
        }

        return null;
    }

    private boolean checkUserInFriendList(IChatUser ft) {
        for (int i = 0; i < newLists.size(); i++) {
            if (newLists.get(i).getId().equals(ft.getId())) {
                return false;
            }
        }

        return true;
    }

    public void updateContactListAdapter(List<IChatUser> list) {
        if (contactsListAdapter == null) {
            // init RecyclerView adapter
            contactsListAdapter = new ContactListAdapter(list);
            if (getOnContactClickListener() != null)
                contactsListAdapter.setOnContactClickListener(getOnContactClickListener());
            recyclerViewContacts.setAdapter(contactsListAdapter);
        } else {
            contactsListAdapter.setList(list);
            contactsListAdapter.notifyDataSetChanged();
        }
    }

    // toggle the no contacts layout visibilty.
    // if there are items show the list of item, otherwise show a placeholder layout
    private void toggleNoContactsLayoutVisibility(int itemCount) {
        if (itemCount > 0) {
            // show the item list
            recyclerViewContacts.setVisibility(View.VISIBLE);
            noContactsLayout.setVisibility(View.GONE);
        } else {
            // show the placeholder layout
            recyclerViewContacts.setVisibility(View.GONE);
            noContactsLayout.setVisibility(View.VISIBLE);
        }
    }

    public void setOnContactClickListener(OnContactClickListener onContactClickListener) {
        this.onContactClickListener = onContactClickListener;
    }

    public OnContactClickListener getOnContactClickListener() {
        return onContactClickListener;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        menu.clear();
        inflater.inflate(R.menu.menu_activity_contacts_list, menu);
        Himself_area.setVisibility(View.GONE);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        MenuItemCompat.setShowAsAction(item,
                MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW |
                        MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setActionView(item, searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                if (contactsListAdapter != null) contactsListAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (contactsListAdapter != null) contactsListAdapter.getFilter().filter(query);
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater); // attach the activity menu
    }

    public void onBackPressed() {
        // close search view on back button pressed
        if (searchView != null && !searchView.isIconified()) {
//            searchView.setIconified(true);
            searchView.onActionViewCollapsed();
            return;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }
}
