package org.chat21.android.ui.chat_groups.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.chat21.android.core.chat_groups.listeners.ChatGroupCreatedListener;
import org.chat21.android.core.messages.models.Message;
import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.storage.ImageHelper;
import org.chat21.android.ui.ChatUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.chat21.android.R;
import org.chat21.android.connectivity.AbstractNetworkReceiver;
import org.chat21.android.core.ChatManager;
import org.chat21.android.core.chat_groups.listeners.ChatGroupsListener;
import org.chat21.android.core.chat_groups.models.ChatGroup;
import org.chat21.android.core.chat_groups.syncronizers.GroupsSyncronizer;
import org.chat21.android.core.exception.ChatRuntimeException;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.chat_groups.WizardNewGroup;
import org.chat21.android.ui.chat_groups.adapters.GroupMembersListAdapter;
import org.chat21.android.ui.chat_groups.fragments.BottomSheetGroupAdminPanelMember;
import org.chat21.android.ui.chat_groups.listeners.OnGroupMemberClickListener;
import org.chat21.android.ui.decorations.ItemDecoration;
import org.chat21.android.ui.login.activities.ChatSignUpActivity;
import org.chat21.android.ui.login.activities.ProfileInputActivity;
import org.chat21.android.ui.messages.activities.MessageListActivity;
import org.chat21.android.utils.StringUtils;
import org.chat21.android.utils.TimeUtils;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static org.chat21.android.utils.DebugConstants.DEBUG_GROUPS;

/**
 * Created by stefanodp91 on 29/06/17.
 */
public class GroupAdminPanelActivity extends AppCompatActivity implements
        OnGroupMemberClickListener, ChatGroupsListener {
    private static final String TAG = GroupAdminPanelActivity.class.getName();
    private static final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 100;

    private RecyclerView mMemberList;
    private GroupMembersListAdapter mGroupMembersListAdapter;
    private RoundedImageView mGroupImage;
//    private ImageView mBoxAddMember;
    private LinearLayout mBoxMembers;
    private LinearLayout mBoxUnavailableMembers;
//    private ImageView startChat;
    private GroupsSyncronizer groupsSyncronizer;
    private String addmemeberkey;
    private String isowner;
    private String groupId;
    private ChatGroup chatGroup;
    private List<IChatUser> groupAdmins;
    private List<IChatUser> grouponemembers;
    private Button btnGroup, btnInvite;
    private EditText edtTitle;
    private ImageView imgPencil, imgCamera;
    private TextView txtDetails;
    ProgressBar progressBar;
    CountDownTimer mCountDownTimer;
    int i=0;
    private int clickCount = 0;
    private IChatUser loggedUser;
    File imgFile = null;
    private StorageReference groupImageRef;
    private FirebaseAuth mAuth;
    String downloadUri = "";

    private interface OnUserCreatedOnContactsCallback {
        void onUserCreatedSuccess();

        void onUserCreatedError(Exception e);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_admin_panel);
//        startChat = (ImageView) findViewById(R.id.startchat);
        btnGroup = (Button) findViewById(R.id.btnChat);
        progressBar = findViewById(R.id.progressbar);
        edtTitle = (EditText) findViewById(R.id.edt_title);
        imgPencil = (ImageView) findViewById(R.id.img_pencil);
        txtDetails = (TextView) findViewById(R.id.txtDetails);
        imgCamera = (ImageView) findViewById(R.id.img_camera);

//        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(i);
        mCountDownTimer=new CountDownTimer(1000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.v("Log_tag", "Tick of Progress"+ i+ millisUntilFinished);
                i++;
                progressBar.setProgress((int)i*100/(1000/1000));
            }

            @Override
            public void onFinish() {
                //Do what you want
                i++;
                progressBar.setProgress(100);
                progressBar.setVisibility(View.INVISIBLE);
            }
        };
        mCountDownTimer.start();

        registerViews();

        loggedUser = ChatManager.getInstance().getLoggedUser();

        // retrieve the groupId (from MessageListActivity)
        groupId = getIntent().getStringExtra(ChatUI.BUNDLE_GROUP_ID);

        mAuth = FirebaseAuth.getInstance();
        groupImageRef = FirebaseStorage.getInstance().getReference().child("Group Images");

        this.groupsSyncronizer = ChatManager.getInstance().getGroupsSyncronizer();
        groupsSyncronizer.addGroupsListener(this);
        groupsSyncronizer.connect();

        chatGroup = groupsSyncronizer.getById(groupId);

        Bundle extras = getIntent().getExtras();
        isowner =  extras.getString("isowner");
        if(isowner==null) {
            groupAdmins = getGroupAdmins();
        } else {
            groupAdmins = new ArrayList<>();
            groupAdmins.add(loggedUser);
            grouponemembers = new ArrayList<>();
            grouponemembers.add(loggedUser);
        }

        Log.d("111111111111", "Chat Chat group::::" + chatGroup);
        if (chatGroup != null) {
            edtTitle.setText(chatGroup.getName());
            downloadUri = chatGroup.getIconURL();
            if (groupAdmins.get(0).getId().equalsIgnoreCase(loggedUser.getId())) {
                imgPencil.setVisibility(View.VISIBLE);
                imgCamera.setVisibility(View.VISIBLE);
            } else {
                imgPencil.setVisibility(View.INVISIBLE);
                imgCamera.setVisibility(View.INVISIBLE);
            }
        }

        setToolbar();
        setCreatedBy();
        setCreatedOn();
        setGroupId();
        initRecyclerViewMembers();
        toggleAddMemberButton();

        btnGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chatGroup != null) {
                    IChatUser groupRecipient = new ChatUser(chatGroup.getGroupId(), chatGroup.getName());

                    // start the message list activity
                    Intent intent = new Intent(GroupAdminPanelActivity.this, MessageListActivity.class);
                    intent.putExtra(ChatUI.BUNDLE_RECIPIENT, groupRecipient);
                    intent.putExtra(ChatUI.BUNDLE_CHANNEL_TYPE, Message.GROUP_CHANNEL_TYPE);
                    startActivity(intent);
                    finish();
                } else {
                    if (edtTitle.getText().toString().equalsIgnoreCase("Edit Group Name")) {
                        Toast.makeText(GroupAdminPanelActivity.this, getString(R.string.please_edit_group_name), Toast.LENGTH_SHORT).show();
                    } else {
                        createGroup();
                    }
                }
            }
        });


        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasPermission(Manifest.permission.CAMERA) && hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    pickImage();
                else
                    requestPermissionsSafely(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
            }
        });
//        startChat.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                IChatUser groupRecipient = new ChatUser(chatGroup.getGroupId(), chatGroup.getName());
//
//                // start the message list activity
//                Intent intent = new Intent(GroupAdminPanelActivity.this, MessageListActivity.class);
//                intent.putExtra(ChatUI.BUNDLE_RECIPIENT, groupRecipient);
//                intent.putExtra(ChatUI.BUNDLE_CHANNEL_TYPE, Message.GROUP_CHANNEL_TYPE);
//                startActivity(intent);
//                finish();
//            }
//        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissionsSafely(String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean hasPermission(String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void pickImage() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(GroupAdminPanelActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ASK_MULTIPLE_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean permission1 = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean permission2 = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (permission1 && permission2) {
                        pickImage();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.please_give_permissions, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                final Uri resultUri = result.getUri();
                Glide.with(getApplicationContext())
                        .load(resultUri)
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                mGroupImage.setImageBitmap(resource);
                            }
                        });

                final StorageReference filePath = groupImageRef.child(UUID.randomUUID().toString() + ".jpg");

                filePath.putFile(resultUri)
                        .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }

                                // Continue with the task to get the download URL
                                return filePath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            downloadUri = task.getResult().toString();
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(GroupAdminPanelActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    private void createGroup() {
        Map<String, Integer> memberMap = new HashMap<>();
        memberMap.put(loggedUser.getId(),1);
        final String chatGroupName = edtTitle.getText().toString();
        final String chatGroupImage = "NOICON";

        groupsSyncronizer.createChatGroup(chatGroupName, chatGroupImage,memberMap, new ChatGroupCreatedListener() {
            @Override
            public void onChatGroupCreated(ChatGroup chatGroup, ChatRuntimeException chatException) {
                Log.i(DEBUG_GROUPS, "NewGroupActivity.onActionNextClicked.onChatGroupCreated");
                // clear the wizard
                WizardNewGroup.getInstance().dispose();
                if (chatException == null) {
                    Log.i(DEBUG_GROUPS, "NewGroupActivity.onActionNextClicked" +
                            ".onChatGroupCreated: chatGroup == " + chatGroup.toString());
                    //                    Conversation conversation = createConversationForAdapter(chatGroup);
                    //                    ChatManager.getInstance().getConversationsHandler().addConversation(conversation);
                    IChatUser groupRecipient = new ChatUser(chatGroup.getGroupId(), chatGroup.getName());

                    // start the message list activity
                    Intent intent = new Intent(GroupAdminPanelActivity.this, MessageListActivity.class);
                    intent.putExtra(ChatUI.BUNDLE_RECIPIENT, groupRecipient);
                    intent.putExtra(ChatUI.BUNDLE_CHANNEL_TYPE, Message.GROUP_CHANNEL_TYPE);
                    startActivity(intent);
                    finish();
                } else {
                    Log.i(DEBUG_GROUPS, "NewGroupActivity.onActionNextClicked" +
                            ".onChatGroupCreated: " + chatException.getLocalizedMessage());
                    // TODO: 29/01/18
                    return;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {

        groupsSyncronizer.removeGroupsListener(this);

        super.onDestroy();
    }

    private void registerViews() {
        Log.d(TAG, "registerViews");

        mMemberList = (RecyclerView) findViewById(R.id.members);
        mGroupImage =  (RoundedImageView) findViewById(R.id.image);
        btnInvite = (Button) findViewById(R.id.btnInvite);
//        mBoxAddMember = (ImageView) findViewById(R.id.box_add_member);
        mBoxMembers = (LinearLayout) findViewById(R.id.box_members);
        mBoxUnavailableMembers = (LinearLayout) findViewById(R.id.box_unavailable_members);
    }

    private List<IChatUser> getGroupAdmins() {
        List<IChatUser> admins = new ArrayList<>();

        String owner = chatGroup.getOwner(); // it always exists

        for (IChatUser member : chatGroup.getMembersList()) {
            if (member.getId().equals(owner)) {
                admins.add(member);
                break;
            }
        }

        return admins;
    }

    private void setToolbar() {
        Log.d(TAG, "GroupAdminPanelActivity.setToolbar");
        final InputMethodManager imm = (InputMethodManager)this.getSystemService(Service.INPUT_METHOD_SERVICE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView tvTitle = (TextView) findViewById(R.id.txtTitle);
        TextView tvNext = (TextView) findViewById(R.id.txtNext);

        tvTitle.setText(getString(R.string.app_name_uper));
        tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chatGroup != null) {
                    chatGroup.setName(edtTitle.getText().toString());
                    groupsSyncronizer.updateChatImageAndName(chatGroup.getGroupId(), edtTitle.getText().toString(), downloadUri);
                }
                onBackPressed();
            }
        });

        imgPencil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chatGroup != null) {
                    edtTitle.setText(chatGroup.getName());
                } else {
                    edtTitle.setText("");
                }
                edtTitle.setEnabled(true);
                edtTitle.setFocusable(true);
                edtTitle.requestFocus();
                imm.showSoftInput(edtTitle, 0);
            }
        });

//        TextView toolbarSubTitle = findViewById(R.id.toolbar_subtitle);
//        toolbarSubTitle.setText("");

        // minimal settings
        if (getActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        }
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    clickCount = 0;
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }


    private void createUserOnContacts(String key, Map<String, Object> user,
                                      final OnUserCreatedOnContactsCallback onUserCreatedOnContactsCallback) {
        DatabaseReference contactsNode;
        if (StringUtils.isValid(ChatManager.Configuration.firebaseUrl)) {
            contactsNode = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(ChatManager.Configuration.firebaseUrl)
                    .child("/apps/" + ChatManager.Configuration.appId + "/contacts");
        } else {
            contactsNode = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("/apps/" + ChatManager.Configuration.appId + "/contacts");
        }

        // save the user on contacts node
        contactsNode.child(key)
                .setValue(user, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            // all gone right
                            onUserCreatedOnContactsCallback.onUserCreatedSuccess();
                        } else {
                            // errors
                            onUserCreatedOnContactsCallback.onUserCreatedError(databaseError.toException());
                        }
                    }
                });
    }


    private void setCreatedBy() {
        Log.d(TAG, "GroupAdminPanelActivity.setCreatedByOn");

        TextView createdByView = (TextView) findViewById(R.id.created_by);

        // if the creator of the chatGroup is the logged user set it
        // otherwise retrieve the chatGroup creator from the chatGroup member list
        if (chatGroup != null) {
            String creator = loggedUser.getId()
                    .equals(chatGroup.getOwner()) ? loggedUser.getFullName() : "";
            for (IChatUser member : chatGroup.getMembersList()) {
                if (member.getId().equals(chatGroup.getOwner())) {
                    creator = member.getFullName();
                    break;
                }
            }
            createdByView.setText(creator);
        } else {
            createdByView.setText(loggedUser.getFullName());
        }
    }

    private void setCreatedOn() {
        TextView createdOnView = (TextView) findViewById(R.id.created_on);

        // retrieve the creation date
        String timestamp = "";
        if (chatGroup != null) {
            timestamp = TimeUtils.getFormattedTimestamp(this, chatGroup.getCreatedOnLong());
        } else {
            timestamp = "";
        }

        // format the user creator and creating date string
//        String createOn = getString(R.string.activity_group_admin_panel_formatted_created_on_label, timestamp);

        // show the text
        createdOnView.setText(timestamp);
    }

    private void setGroupId() {
        TextView groupIdView = findViewById(R.id.group_id);
        if (chatGroup != null) {
            groupIdView.setText(chatGroup.getGroupId());
        } else {
            groupIdView.setText("");
        }
    }

    private void initRecyclerViewMembers() {
        Log.d(TAG, "initRecyclerViewMembers");
//        mMemberList.addItemDecoration(new ItemDecoration(this,
//                DividerItemDecoration.VERTICAL,
//                getResources().getDrawable(R.drawable.decorator_activity_group_admin_panel_members_list)));
        mMemberList.setLayoutManager(new LinearLayoutManager(this));
        if (chatGroup != null) {
            updateGroupMemberListAdapter(chatGroup.getMembersList());
        } else {
            updateGroupMemberListAdapter(grouponemembers);
        }
    }


    private void toggleGroupMembersVisibility() {
        Log.d(TAG, "initCardViewMembers");

        if (chatGroup != null) {
            if (chatGroup.getMembersList() != null && chatGroup.getMembersList().size() > 0) {
                mBoxUnavailableMembers.setVisibility(View.GONE);
                mBoxMembers.setVisibility(View.VISIBLE);

            } else {
                Log.e(TAG, "GroupAdminPanelActivity.toggleCardViewMembers: " +
                        "groupMembers is not valid");
                mBoxMembers.setVisibility(View.GONE);
                mBoxUnavailableMembers.setVisibility(View.VISIBLE);
            }

            int size = chatGroup.getMembersList().size();
            txtDetails.setText(size + " member");

            Glide.with(this)
                    .load(chatGroup.getIconURL())
                    .asBitmap()
                    .error(R.drawable.ic_sheep)
                    .into(mGroupImage);
        } else {
            if (grouponemembers != null && grouponemembers.size() > 0) {
                mBoxUnavailableMembers.setVisibility(View.GONE);
                mBoxMembers.setVisibility(View.VISIBLE);

            } else {
                Log.e(TAG, "GroupAdminPanelActivity.toggleCardViewMembers: " +
                        "groupMembers is not valid");
                mBoxMembers.setVisibility(View.GONE);
                mBoxUnavailableMembers.setVisibility(View.VISIBLE);
            }

            txtDetails.setText("1 member");
            Picasso.get()
                    .load(R.drawable.ic_sheep)
//                    .transform(new RoundedCornersTransformation(0, 1))
                    .into(mGroupImage);

        }
    }

    private void updateGroupMemberListAdapter(List<IChatUser> members) {
        Log.d(TAG, "updateGroupMemberListAdapter");

        if (mGroupMembersListAdapter == null) {
            mGroupMembersListAdapter = new GroupMembersListAdapter(this, members);
            mGroupMembersListAdapter.setOnGroupMemberClickListener(this);
            mMemberList.setAdapter(mGroupMembersListAdapter);
        } else {
            mGroupMembersListAdapter.setList(members);
            mGroupMembersListAdapter.notifyDataSetChanged();
        }

        for (IChatUser admin : groupAdmins) {
            mGroupMembersListAdapter.addAdmin(admin);
        }

        toggleGroupMembersVisibility();
    }

    private void toggleAddMemberButton() {
        Log.d(TAG, "toggleAddMemberButton");
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            addmemeberkey= null;
        } else {
            addmemeberkey= extras.getString("addmemeberkey");
        }

        // check if the current user is an admin and a member of the group

//            if (groupAdmins.contains(loggedUser) && chatGroup.getMembersList().contains(loggedUser)) {
//                showAddMember();
//            } else {
//                if (addmemeberkey.equals("ok")) {
//                    showAddMember();
//                } else {
//                    hideAddMember();
//                }
//            }

        if (groupAdmins.contains(loggedUser)) {
            showAddMember();
        } else {
            if (addmemeberkey != null) {
                if (addmemeberkey.equals("ok")) {
                    showAddMember();
                } else {
                    hideAddMember();
                }
            }
        }
    }

    private void showAddMember() {
        Log.d(TAG, "GroupAdminPanelActivity.showAddMember");

        // shows the add member box
//        mBoxAddMember.setVisibility(View.VISIBLE);

        btnInvite.setVisibility(View.VISIBLE);
        btnInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AbstractNetworkReceiver.isConnected(getApplicationContext())) {
                    startAddMemberActivity();
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.menu_activity_group_admin_panel_activity_cannot_add_member_offline),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

//        // set the click listener
//        mBoxAddMember.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                if (AbstractNetworkReceiver.isConnected(getApplicationContext())) {
//                    startAddMemberActivity();
//                } else {
//                    Toast.makeText(getApplicationContext(),
//                            getString(R.string.menu_activity_group_admin_panel_activity_cannot_add_member_offline),
//                            Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }

    private void hideAddMember() {
        Log.d(TAG, "GroupAdminPanelActivity.hideAddMember");

        btnInvite.setVisibility(View.GONE);
        btnInvite.setOnClickListener(null);

//        // hides the add member box
//        mBoxAddMember.setVisibility(View.GONE);
//
//        // unset the click listener
//        mBoxAddMember.setOnClickListener(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v("onOptionsItemSelected",String.valueOf(item.getItemId()));
        if(chatGroup.getMembers().size() == 1){
            groupsSyncronizer.deleteGroup(chatGroup.getGroupId());

        }
        onBackPressed();
        return true;
    }

    private void startAddMemberActivity() {
        Log.d(TAG, "startAddMemberActivity");
        WizardNewGroup.getInstance().getTempChatGroup().setName(edtTitle.getText().toString());
        WizardNewGroup.getInstance().getTempChatGroup().setIconURL(downloadUri);

        Intent intent = new Intent(this, AddMemberToChatGroupActivity.class);
        intent.putExtra(ChatUI.BUNDLE_CHAT_GROUP, chatGroup);
        startActivity(intent);
        finish();
    }

    // handles the click on a member
    @Override
    public void onGroupMemberClicked(IChatUser member, int position) {
        Log.i(TAG, "onGroupMemberClicked");

        showOnMemberClickedBottomSheet(member, chatGroup);
    }

    private void showOnMemberClickedBottomSheet(IChatUser groupMember, ChatGroup chatGroup) {
        Log.d(TAG, "showOnMemberClickedBottomSheet");

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        BottomSheetGroupAdminPanelMember dialog = BottomSheetGroupAdminPanelMember
                .newInstance(groupMember, chatGroup);
        dialog.show(ft, BottomSheetGroupAdminPanelMember.TAG);
    }

    @Override
    public void onGroupAdded(ChatGroup chatGroup, ChatRuntimeException e) {
        if (e == null) {
            if (chatGroup.getGroupId().equals(groupId)) {
                Log.d(TAG, "GroupAdminPanelActivity.onGroupAdded.chatGroup:" +
                        " chatGroup == " + chatGroup.toString());
                this.chatGroup = chatGroup;
                updateGroupMemberListAdapter(chatGroup.getMembersList()); // update members
            }
        } else {
            Log.e(TAG, "GroupAdminPanelActivity.onGroupAdded: " + e.toString());
        }
    }

    @Override
    public void onGroupChanged(ChatGroup chatGroup, ChatRuntimeException e) {
        if (e == null) {
            this.chatGroup = chatGroup;
            if (chatGroup.getGroupId().equals(groupId)) {
                Log.d(TAG, "GroupAdminPanelActivity.onGroupChanged.chatGroup: " +
                        "chatGroup == " + chatGroup.toString());
                updateGroupMemberListAdapter(chatGroup.getMembersList()); // update members
            }
        } else {
            Log.e(TAG, "GroupAdminPanelActivity.onGroupChanged: " + e.toString());
        }
    }

    @Override
    public void onGroupRemoved(ChatRuntimeException e) {
        if (e == null) {
            Log.d(TAG, "GroupAdminPanelActivity.onGroupRemoved");
        } else {
            Log.e(TAG, "GroupAdminPanelActivity.onGroupRemoved: " + e.toString());
        }
    }

}