package com.lifechurch.talk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;


import org.chat21.android.core.ChatManager;
import org.chat21.android.core.chat_groups.models.ChatGroup;
import org.chat21.android.core.chat_groups.syncronizers.GroupsSyncronizer;
import org.chat21.android.core.conversations.models.Conversation;
import org.chat21.android.core.messages.models.Message;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.chat_groups.WizardNewGroup;
import org.chat21.android.ui.chat_groups.activities.GroupAdminPanelActivity;
import org.chat21.android.ui.contacts.fragments.ContactsListFragment;
import org.chat21.android.ui.contacts.listeners.OnContactClickListener;
import org.chat21.android.ui.login.activities.GroupModel;
import org.chat21.android.ui.messages.activities.MessageListActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.chat21.android.utils.DebugConstants.DEBUG_GROUPS;

public class HomeFragment extends Fragment implements OnContactClickListener{

    private static final String TAG = HomeFragment.class.getName();
    private ImageView create_group,  setting;
    private ContactsListFragment contactsListFragment;
    private List<IChatUser> selectedList;
    GroupModel groupModel;
    private GroupsSyncronizer groupsSyncronizer;
    IChatUser loggedUser;
    ProgressBar progressBar;
    LinearLayout linearCreateGroup;

    public HomeFragment() {
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab_home, container, false);

        progressBar = rootView.findViewById(org.chat21.android.R.id.progress);
        setHasOptionsMenu(false);
        selectedList = new ArrayList<>();
        groupsSyncronizer = ChatManager.getInstance().getGroupsSyncronizer();
        int result= ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS);
        if (result != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 7);
        }
        else{
//            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 7);
            contactsListFragment = new ContactsListFragment();
            contactsListFragment.setOnContactClickListener(this);
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, contactsListFragment)
                    .commit();
        }

        create_group = (ImageView) rootView.findViewById(R.id.group);

//        initBoxCreateGroup();

//        create_group.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startCreateGroupActivity();
//            }
//        });

        linearCreateGroup = (LinearLayout) rootView.findViewById(R.id.linearCreateGroup);
        linearCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCreateGroupActivity();
            }
        });
        return rootView;
    }

    //    private void initBoxCreateGroup() {
//        Log.d(TAG, "ContactListActivity.initBoxCreateGroup");
//
//        if (ChatUI.getInstance().areGroupsEnabled()) {
//
//            // box click
//            create_group.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (AbstractNetworkReceiver.isConnected(getActivity())) {
//
//                        if (ChatUI.getInstance().getOnCreateGroupClickListener() != null) {
//                            ChatUI.getInstance().getOnCreateGroupClickListener()
//                                    .onCreateGroupClicked();
//                        }
//                        Log.v("************","ahhhhhh");
//
//
//                        startCreateGroupActivity();
//                    } else {
//                        Toast.makeText(getActivity(),
//                                getString(org.chat21.android.R.string.activity_contact_list_error_cannot_create_group_offline_label),
//                                Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//        }
//    }
    private void startCreateGroupActivity() {
        Log.d(TAG, "ContactListActivity.startCreateGroupActivity");
        progressBar.setVisibility(View.VISIBLE);
        WizardNewGroup.getInstance().dispose();
        loggedUser = ChatManager.getInstance().getLoggedUser();

        Map<String, Integer> memberMap = new HashMap<>();
        memberMap.put(loggedUser.getId(),1);
        final String chatGroupName = loggedUser.getFullName();
        final String chatGroupImage = "NOICON";

        Intent intent = new Intent(getActivity(), GroupAdminPanelActivity.class);
        intent.putExtra(ChatUI.BUNDLE_GROUP_ID, "");
        intent.putExtra("addmemeberkey","ok");
        intent.putExtra("isowner","ok");
        startActivity(intent);


//        groupsSyncronizer.createChatGroup(chatGroupName, chatGroupImage,memberMap, new ChatGroupCreatedListener() {
//            @Override
//            public void onChatGroupCreated(ChatGroup chatGroup, ChatRuntimeException chatException) {
//                Log.i(DEBUG_GROUPS, "NewGroupActivity.onActionNextClicked.onChatGroupCreated");
//                // clear the wizard
//                WizardNewGroup.getInstance().dispose();
//                if (chatException == null) {
//                    Log.i(DEBUG_GROUPS, "NewGroupActivity.onActionNextClicked" +
//                            ".onChatGroupCreated: chatGroup == " + chatGroup.toString());
////                    Conversation conversation = createConversationForAdapter(chatGroup);
////                    ChatManager.getInstance().getConversationsHandler().addConversation(conversation);
//                    Intent intent = new Intent(getActivity(), GroupAdminPanelActivity.class);
//                    intent.putExtra(ChatUI.BUNDLE_GROUP_ID, chatGroup.getGroupId());
//                    intent.putExtra("addmemeberkey","ok");
//                    intent.putExtra("isowner","ok");
//                    startActivity(intent);
//                } else {
//                    Log.i(DEBUG_GROUPS, "NewGroupActivity.onActionNextClicked" +
//                            ".onChatGroupCreated: " + chatException.getLocalizedMessage());
//                    // TODO: 29/01/18
//                    getActivity().setResult(RESULT_CANCELED);
//                    return;
//                }
//            }
//        });

    }


    // convert the list of contact to a map of members
    private Map<String, Integer> convertListToMap(List<IChatUser> contacts) {
        Map<String, Integer> members = new HashMap<>();
        for (IChatUser contact : contacts) {
            // the value "1" is a default value with no usage
            members.put(contact.getId(), 1);
        }

        // add the current user to members list in background
        members.put(ChatManager.getInstance().getLoggedUser().getId(), 1);

        return members;
    }
    // create a conversation on the fly
    private Conversation createConversationForAdapter(ChatGroup chatGroup) {
        Conversation conversation = new Conversation();
        conversation.setChannelType(Message.GROUP_CHANNEL_TYPE);
        conversation.setConversationId(chatGroup.getGroupId());
        conversation.setTimestamp(chatGroup.getCreatedOnLong());
        conversation.setIs_new(true);
        conversation.setRecipientFullName(chatGroup.getName());
        conversation.setConvers_with(chatGroup.getGroupId());
        conversation.setConvers_with_fullname(chatGroup.getName());
        Log.i(DEBUG_GROUPS, "NewGroupActivity.onActionNextClicked" +
                ".onChatGroupCreated: it has been created on the fly the conversation == " + conversation.toString());
        return conversation;
    }



    @Override
    public void onContactClicked(IChatUser contact, int position) {
        Log.d(TAG, "ContactListActivity.onRecyclerItemClicked:" +
                " contact == " + contact.toString() + ", position ==  " + position);

        if (ChatUI.getInstance().getOnContactClickListener() != null) {
            ChatUI.getInstance().getOnContactClickListener().onContactClicked(contact, position);
        }
        // start the conversation activity
        startMessageListActivity(contact);
    }

    private void startMessageListActivity(IChatUser contact) {
        Log.d(TAG, "ContactListActivity.startMessageListActivity");

        Intent intent = new Intent(getActivity(), MessageListActivity.class);
        intent.putExtra(ChatUI.BUNDLE_RECIPIENT, contact);
        intent.putExtra(ChatUI.BUNDLE_CHANNEL_TYPE, Message.DIRECT_CHANNEL_TYPE);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 7) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                progressBar.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(getActivity(), "Can't read contacts as you have declined the permission.", Toast.LENGTH_SHORT).show();
            }
        }

    }



}