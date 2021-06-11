package com.lifechurch.talk;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;

import org.chat21.android.core.ChatManager;
import org.chat21.android.core.users.models.IChatUser;


public class FragmentFeedback extends Fragment {
    private static final String TAG = ChurchFragment.class.getName();
    private RoundedImageView Feel1,Feel2,Feel3,Feel4,Feel5;
    private EditText Feedbacktxt;

    private Button Submit;
    private String Feelstate = "";
    private String Feedbackstring;
    private String Feedbackmessage;

    private String channelType;
    private DatabaseReference databaseReference,contactsNode;
    private FirebaseAuth mAuth;
    String name, photo;

    private IChatUser loggedUser;


    public static Fragment newInstance() {
        FragmentFeedback fragment = new FragmentFeedback();

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);
        Feedbacktxt = view.findViewById(R.id.feedback_message);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        final String userId = user.getUid();

        loggedUser = ChatManager.getInstance().getLoggedUser();

        contactsNode = FirebaseDatabase.getInstance()
                .getReference()
                .child("/apps/" + ChatManager.Configuration.appId + "/contacts/" + loggedUser.getId());
        contactsNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String email = dataSnapshot.child("email").getValue(String.class);
                name = dataSnapshot.child("firstname").getValue(String.class);
                photo = dataSnapshot.child("imageurl").getValue(String.class);

//                if (email.equals("admin@test.com")){
//                    Intent intent = new Intent(getActivity(),FeedbackListActivity.class);
//                    startActivity(intent);
//                    getActivity().finish();
//                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Submit = view.findViewById(R.id.submit);
        Feel1 = view.findViewById(R.id.feel1);
        Feel2 = view.findViewById(R.id.feel2);
        Feel3 = view.findViewById(R.id.feel3);
        Feel4 = view.findViewById(R.id.feel4);
        Feel5 = view.findViewById(R.id.feel5);
        Feel1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Feel1.setBackgroundResource(R.drawable.bg_feel_selected);
                Feel2.setBackgroundResource(R.drawable.bg_shadow_circle);
                Feel3.setBackgroundResource(R.drawable.bg_shadow_circle);
                Feel4.setBackgroundResource(R.drawable.bg_shadow_circle);
                Feel5.setBackgroundResource(R.drawable.bg_shadow_circle);

                Feelstate = "100";

            }
        });
        Feel2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Feel1.setBackgroundResource(R.drawable.bg_shadow_circle);
                Feel2.setBackgroundResource(R.drawable.bg_feel_selected);
                Feel3.setBackgroundResource(R.drawable.bg_shadow_circle);
                Feel4.setBackgroundResource(R.drawable.bg_shadow_circle);
                Feel5.setBackgroundResource(R.drawable.bg_shadow_circle);

                Feelstate = "75";
            }
        });
        Feel3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Feel1.setBackgroundResource(R.drawable.bg_shadow_circle);
                Feel2.setBackgroundResource(R.drawable.bg_shadow_circle);
                Feel3.setBackgroundResource(R.drawable.bg_feel_selected);
                Feel4.setBackgroundResource(R.drawable.bg_shadow_circle);
                Feel5.setBackgroundResource(R.drawable.bg_shadow_circle);
                Feelstate = "50";
            }
        });
        Feel4.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                    Feel1.setBackgroundResource(R.drawable.bg_shadow_circle);
                    Feel2.setBackgroundResource(R.drawable.bg_shadow_circle);
                    Feel3.setBackgroundResource(R.drawable.bg_shadow_circle);
                    Feel4.setBackgroundResource(R.drawable.bg_feel_selected);
                    Feel5.setBackgroundResource(R.drawable.bg_shadow_circle);
                    Feelstate = "25";
            }
        });
        Feel5.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                Feel1.setBackgroundResource(R.drawable.bg_shadow_circle);
                Feel2.setBackgroundResource(R.drawable.bg_shadow_circle);
                Feel3.setBackgroundResource(R.drawable.bg_shadow_circle);
                Feel4.setBackgroundResource(R.drawable.bg_shadow_circle);
                Feel5.setBackgroundResource(R.drawable.bg_feel_selected);
                Feelstate = "0";
            }
        });

        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Feedbackstring = Feedbacktxt.getText().toString();
                Feedbackmessage = Feedbackstring + "(" + Feelstate + ")";
                Log.v("--feedback->",Feedbackmessage);

                databaseReference.child("feedback").child(userId).child("text").setValue(Feedbackstring);
                databaseReference.child("feedback").child(userId).child("state").setValue(Feelstate);
                databaseReference.child("feedback").child(userId).child("firstname").setValue(name);
                databaseReference.child("feedback").child(userId).child("imageurl").setValue(photo);

                Toast.makeText(getActivity(),"Success",Toast.LENGTH_SHORT).show();
                getActivity().finish();


///////////////////////////////////////////////////////////////////////////////////////////////////////////

//                if (!StringUtils.isValid(Feedbackmessage)) {
//                    return;
//                }
//                channelType = Message.DIRECT_CHANNEL_TYPE;
//                ChatManager.getInstance().sendTextMessage("Mturb8PggZWfkEW6oRu2lzitF943", "ilya",
//                        Feedbackmessage, channelType, null, new SendMessageListener() {
//                            @Override
//                            public void onBeforeMessageSent(Message message, ChatRuntimeException chatException) {
//                                if (chatException == null) {
//                                    // if the message exists update it, else add it
//                                    Log.d(TAG, "sendTextMessage.onBeforeMessageSent.message.id: " + message.getId());
//                                    Log.d(TAG, "sendTextMessage.onBeforeMessageSent.message.recipient: " + message.getRecipient());
//
//                                    messageListAdapter.updateMessage(message);
////                                    scrollToBottom();
//                                } else {
//
//                                    Toast.makeText(getActivity(),
//                                            "Failed to send message",
//                                            Toast.LENGTH_SHORT).show();
//
//                                    Log.e(TAG, "sendTextMessage.onBeforeMessageSent: ", chatException);
//                                }
//                            }
//
//                            @Override
//                            public void onMessageSentComplete(Message message, ChatRuntimeException chatException) {
//                                if (chatException == null) {
//
//                                    Log.d(TAG, "message sent: " + message.toString());
//                                } else {
//                                    Toast.makeText(getActivity(),
//                                            "Failed to send message",
//                                            Toast.LENGTH_SHORT).show();
//                                    Log.e(TAG, "error sending message : ", chatException);
//                                }
//                            }
//                        });
///////////////////////////////////////////////////////////////////////////////////////////////////////////




            }
        });

        return view;
    }
}
