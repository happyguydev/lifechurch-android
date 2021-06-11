package org.chat21.android.ui.chat_groups.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.chat_groups.WizardNewGroup;

import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.chat21.android.R;
import org.chat21.android.core.ChatManager;
import org.chat21.android.core.chat_groups.listeners.ChatGroupCreatedListener;
import org.chat21.android.core.chat_groups.models.ChatGroup;
import org.chat21.android.core.chat_groups.syncronizers.GroupsSyncronizer;
import org.chat21.android.core.conversations.models.Conversation;
import org.chat21.android.core.exception.ChatRuntimeException;
import org.chat21.android.core.messages.models.Message;
import org.chat21.android.ui.login.activities.GroupModel;
import org.chat21.android.utils.StringUtils;

import static org.chat21.android.utils.DebugConstants.DEBUG_GROUPS;

/**
 * Created by stefanodp91 on 26/01/18.
 */

public class NewGroupActivity extends AppCompatActivity {

    private GroupsSyncronizer groupsSyncronizer;

    private EditText groupNameView;
    private RoundedImageView groupImageView;
    private MenuItem actionNextMenuItem;
    String downloadUri = "";
    private ProgressDialog loadingBar;
    private StorageReference groupProfileImageRef;
    Map<String, Integer> chatGroupMembers;
    DatabaseReference contactsNode;
    ArrayList<String> uidList = new ArrayList<>();
    ArrayList<String> nameList = new ArrayList<>();
    List<IChatUser> selectedMember = new ArrayList<>();
    GroupModel groupModel;
    IChatUser loggedUser;
    StringBuilder stringBuilder = new StringBuilder();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        groupsSyncronizer = ChatManager.getInstance().getGroupsSyncronizer();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadingBar = new ProgressDialog(this);
        groupProfileImageRef = FirebaseStorage.getInstance().getReference().child("Group Images");
        chatGroupMembers = WizardNewGroup.getInstance().getTempChatGroup().getMembers();
        groupNameView = findViewById(R.id.group_name);

        for (String key: chatGroupMembers.keySet()) {
            Log.i("key : ", key);
            uidList.add(key);
        }
        loggedUser = ChatManager.getInstance().getLoggedUser();
        nameList.add(loggedUser.getFullName());

        selectedMember = groupModel.selectedUser;
        for (int i = 0; i < selectedMember.size(); i++) {
            if (selectedMember.get(i).getFullName().equals(loggedUser.getFullName())) {
                selectedMember.remove(i);
            }else {
                nameList.add(selectedMember.get(i).getFullName());
            }
        }

        for (int i = 0; i < nameList.size(); i ++) {
            if (nameList.size() > 3) {
                if (i < 3) {
                    stringBuilder.append(nameList.get(i));
                    if (i != 2) {
                        stringBuilder.append(", ");
                    }
                }
            } else {
                stringBuilder.append(nameList.get(i));
                if (i != nameList.size() - 1) {
                    stringBuilder.append(", ");
                }
            }

        }

        if (nameList.size() > 2) {
            stringBuilder.append(" & " + (nameList.size() - 3) + " members");

        }

        groupNameView.setText(stringBuilder.toString());
        WizardNewGroup.getInstance().getTempChatGroup().setName(groupNameView.getText().toString());

        groupNameView.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (actionNextMenuItem != null) {
                    if (isValidGroupName() && actionNextMenuItem != null) {

                        WizardNewGroup.getInstance().getTempChatGroup().setName(groupNameView.getText().toString());
                        actionNextMenuItem.setVisible(true);
                    } else {
                        actionNextMenuItem.setVisible(false);
                    }
                }
            }
        });

        groupImageView = findViewById(R.id.group_icon);
        groupImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(NewGroupActivity.this);
       }
        });

    }

    // check if the group name is valid
    // if yes show the "next" button, hide it otherwise
    private boolean isValidGroupName() {

        String groupName = groupNameView.getText().toString();
        return StringUtils.isValid(groupName);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_new_group, menu);

        actionNextMenuItem = menu.findItem(R.id.action_next);
        actionNextMenuItem.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_next) {
            onActionNextClicked();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void onActionNextClicked() {
        final String chatGroupName = WizardNewGroup.getInstance().getTempChatGroup().getName();
        final String chatGroupImage = WizardNewGroup.getInstance().getTempChatGroup().getIconURL();
        Map<String, Integer> chatGroupMembers = WizardNewGroup.getInstance().getTempChatGroup().getMembers();

        groupsSyncronizer.createChatGroup(chatGroupName, chatGroupImage,chatGroupMembers, new ChatGroupCreatedListener() {
            @Override
            public void onChatGroupCreated(ChatGroup chatGroup, ChatRuntimeException chatException) {
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

                    Toast.makeText(NewGroupActivity.this, "\"" + groupNameView.getText().toString() + "\"" + " created", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(NewGroupActivity.this, GroupAdminPanelActivity.class);
                    intent.putExtra(ChatUI.BUNDLE_GROUP_ID, chatGroup.getGroupId());
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("set_gooup_profile_image");
                loadingBar.setMessage("Please wait, your profile image is uploading");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                final Uri resultUri = result.getUri();
                final StorageReference filePath = groupProfileImageRef.child(UUID.randomUUID().toString() + ".jpg");

                filePath.putFile(resultUri)
                        .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(NewGroupActivity.this.getContentResolver(), resultUri);
                                groupImageView.setImageBitmap(bitmap);
                                // Continue with the task to get the download URL
                                return filePath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            downloadUri = task.getResult().toString();
                            WizardNewGroup.getInstance().getTempChatGroup().setIconURL(downloadUri);


                            loadingBar.dismiss();




                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(NewGroupActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
