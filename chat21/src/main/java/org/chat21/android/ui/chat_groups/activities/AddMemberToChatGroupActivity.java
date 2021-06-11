package org.chat21.android.ui.chat_groups.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.chat21.android.core.chat_groups.listeners.ChatGroupCreatedListener;
import org.chat21.android.core.conversations.models.Conversation;
import org.chat21.android.core.exception.ChatRuntimeException;
import org.chat21.android.core.messages.models.Message;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.chat_groups.WizardNewGroup;

import org.chat21.android.R;
import org.chat21.android.core.ChatManager;
import org.chat21.android.core.chat_groups.models.ChatGroup;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.chat_groups.adapters.SelectedContactListAdapter;
import org.chat21.android.ui.chat_groups.listeners.OnRemoveClickListener;
import org.chat21.android.ui.contacts.fragments.ContactsListFragment;
import org.chat21.android.ui.contacts.listeners.OnContactClickListener;
import org.chat21.android.ui.login.activities.GroupModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.chat21.android.utils.DebugConstants.DEBUG_GROUPS;

/**
 * Created by stefanodp91 on 07/03/18.
 */

public class AddMemberToChatGroupActivity extends AppCompatActivity implements OnContactClickListener, OnRemoveClickListener {

    private static final String TAG = AddMemberToChatGroupActivity.class.getName();

    private ContactsListFragment contactsListFragment;

    private List<IChatUser> selectedList;
    private CardView cvSelectedContacts;
    private RecyclerView rvSelectedList;
    private SelectedContactListAdapter selectedContactsListAdapter;

    private MenuItem actionNextMenuItem;

    private ChatGroup chatGroup;

    GroupModel groupModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member_to_chat_group);

        selectedList = new ArrayList<>();
        cvSelectedContacts = findViewById(R.id.cardview_selected_contacts);
        rvSelectedList = findViewById(R.id.selected_list);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this,
                        LinearLayoutManager.HORIZONTAL, false);
        rvSelectedList.setLayoutManager(layoutManager);
        rvSelectedList.setItemAnimator(new DefaultItemAnimator());
        updateSelectedContactListAdapter(selectedList, 0);

        contactsListFragment = new ContactsListFragment();
        contactsListFragment.setOnContactClickListener(this);

        // #### BEGIN TOOLBAR ####
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // #### END  TOOLBAR ####

        // #### BEGIN CONTAINER ####
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, contactsListFragment)
                .commit();
        // #### BEGIN CONTAINER ####
    }

    @Override
    protected void onStart() {
        long startTime = System.currentTimeMillis();

        // retrieve the chatGroup if exists
        if (getIntent().hasExtra(ChatUI.BUNDLE_CHAT_GROUP)) {
            chatGroup = (ChatGroup) getIntent().getSerializableExtra(ChatUI.BUNDLE_CHAT_GROUP);
        }

        super.onStart();
    }

    private void updateSelectedContactListAdapter(List<IChatUser> list, int position) {

        if (selectedContactsListAdapter == null) {
            selectedContactsListAdapter = new SelectedContactListAdapter(this, list);
            selectedContactsListAdapter.setOnRemoveClickListener(this);
            rvSelectedList.setAdapter(selectedContactsListAdapter);
        } else {
            selectedContactsListAdapter.setList(list);
            selectedContactsListAdapter.notifyDataSetChanged();
        }

        if (selectedContactsListAdapter.getItemCount() > 0) {
            cvSelectedContacts.setVisibility(View.VISIBLE);
            if (actionNextMenuItem != null) {
                actionNextMenuItem.setVisible(true);
            }// show next action
        } else {
            cvSelectedContacts.setVisibility(View.GONE);
            if (actionNextMenuItem != null) {
                actionNextMenuItem.setVisible(false); // hide next action
            }
        }

        rvSelectedList.smoothScrollToPosition(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_add_members, menu);

        actionNextMenuItem = menu.findItem(R.id.action_next);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "ChooseContactActivity.onOptionsItemSelected");
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_next) {
            onActionNextClicked();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onActionNextClicked() {
        // convert the members list to a sanified format
        Map<String, Integer> membersMap = convertListToMap(selectedList);
        groupModel = new GroupModel(selectedList);
        if (chatGroup != null) {
            ChatManager.getInstance().getGroupsSyncronizer()
                    .addMembersToChatGroup(chatGroup.getGroupId(), membersMap);

            Intent intent = new Intent(AddMemberToChatGroupActivity.this, GroupAdminPanelActivity.class);
            intent.putExtra(ChatUI.BUNDLE_GROUP_ID, chatGroup.getGroupId());
            intent.putExtra("isowner","ok");
            startActivity(intent);
            finish();
        } else {
            WizardNewGroup.getInstance().getTempChatGroup().addMembers(membersMap);

            final String chatGroupName = WizardNewGroup.getInstance().getTempChatGroup().getName();
            final String chatGroupImage = WizardNewGroup.getInstance().getTempChatGroup().getIconURL();
            Map<String, Integer> chatGroupMembers = WizardNewGroup.getInstance().getTempChatGroup().getMembers();

            ChatManager.getInstance().getGroupsSyncronizer().createChatGroup(chatGroupName, chatGroupImage,chatGroupMembers, new ChatGroupCreatedListener() {
                @Override
                public void onChatGroupCreated(final ChatGroup chatGroup, ChatRuntimeException chatException) {
                    Log.i(DEBUG_GROUPS, "NewGroupActivity.onActionNextClicked.onChatGroupCreated");

                    // clear the wizard
                    WizardNewGroup.getInstance().dispose();

                    if (chatException == null) {
                        Log.i(DEBUG_GROUPS, "NewGroupActivity.onActionNextClicked" +
                                ".onChatGroupCreated: chatGroup == " + chatGroup.toString());

                        // create a conversation on the fly
                        Conversation conversation = createConversationForAdapter(chatGroup);

                        // add the conversation to the conversation adapter
                        ChatManager.getInstance().getConversationsHandler().addConversation(conversation);

                        Toast.makeText(AddMemberToChatGroupActivity.this, "Chat Group was created", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AddMemberToChatGroupActivity.this, GroupAdminPanelActivity.class);
                        intent.putExtra(ChatUI.BUNDLE_GROUP_ID, chatGroup.getGroupId());
                        intent.putExtra("isowner","ok");
                        startActivity(intent);
                        finish();
                    } else {
                        Log.i(DEBUG_GROUPS, "NewGroupActivity.onActionNextClicked" +
                                ".onChatGroupCreated: " + chatException.getLocalizedMessage());
                        // TODO: 29/01/18
                        setResult(RESULT_CANCELED);
                        return;
                    }
                }
            });
        }
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

    @Override
    public void onContactClicked(IChatUser contact, int position) {
        // add a contact only if it not exists
        addMemberToGroup(contact, selectedList, position);
    }

    private void addMemberToGroup(IChatUser contact, List<IChatUser> contactList, int position) {
        // add a contact only if it not exists
        if (!isContactAlreadyAdded(contact, contactList)) {
            // add the contact to the contact list and update the adapter
            contactList.add(contact);
        }

//        contactsListAdapter.addToAlreadyAddedList(contact, position);

        updateSelectedContactListAdapter(contactList, position);
    }

    // check if a contact is already added to a list
    private boolean isContactAlreadyAdded(IChatUser toCheck, List<IChatUser> mlist) {
        boolean exists = false;
        for (IChatUser contact : mlist) {
            String contactId = contact.getId();

            if (contactId.equals(toCheck.getId())) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    @Override
    public void onRemoveClickListener(int position) {

        IChatUser contact = selectedList.get(position);
        // remove the contact only if it exists
        if (isContactAlreadyAdded(contact, selectedList)) {
            // remove the item at position from the contacts list and update the adapter
            selectedList.remove(position);

//            contactsListAdapter.removeFromAlreadyAddedList(contact);

            updateSelectedContactListAdapter(selectedList, position);
        } else {
            Snackbar.make(findViewById(R.id.coordinator),
                    getString(R.string.add_members_activity_contact_not_added_label),
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        contactsListFragment.onBackPressed();
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ChatUI.REQUEST_CODE_CREATE_GROUP) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }
}
