package com.lifechurch.talk.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import com.lifechurch.talk.R;
import com.lifechurch.talk.model.Feedback;

public class FeedbackAdapter extends ArrayAdapter<Feedback> {

    private final Activity context;
    List<Feedback> feedbackitems;

    public FeedbackAdapter(Activity context, List<Feedback> feedbackitems) {
        super(context, R.layout.adapter_feedback_item,feedbackitems);
        this.context = context;
        this.feedbackitems = feedbackitems;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.adapter_feedback_item, null,true);

        TextView text_name = (TextView) rowView.findViewById(R.id.fullname);
        TextView text_feedback = (TextView) rowView.findViewById(R.id.text_feedback);
        ImageView imageAvatar = (ImageView)rowView.findViewById(R.id.profile_picture);
        ImageView imageStatus = (ImageView)rowView.findViewById(R.id.status_picture);

        Feedback Item = feedbackitems.get(position);

        final String FullName = Item.getfirstname();
        final String imageUrl = Item.getimageurl();
        final String text = Item.gettext();
        final String status = Item.getstate();

        text_name.setText(FullName);
        text_feedback.setText(text);

        Picasso.get().load(imageUrl).into(imageAvatar);

//        Glide.with(view.getContext())
//                .load(imageUrl)
//                .placeholder(org.chat21.android.R.drawable.ic_person_avatar)
//                .bitmapTransform(new CropCircleTransformation(view.getContext()))
//                .into(imageAvatar);

        if (status.equals("100")){
            Picasso.get().load(R.drawable.feel1).into(imageStatus);
        }else if(status.equals("75")){
            Picasso.get().load(R.drawable.feel2).into(imageStatus);
        }else if(status.equals("50")){
            Picasso.get().load(R.drawable.feel3).into(imageStatus);
        }else if(status.equals("25")){
            Picasso.get().load(R.drawable.feel4).into(imageStatus);
        }else {
            Picasso.get().load(R.drawable.feel5).into(imageStatus);
        }


        return rowView;

    }
}