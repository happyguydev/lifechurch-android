package com.lifechurch.talk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.chat21.android.utils.image.CropCircleTransformation;

import java.util.ArrayList;

public class ContactsAdapter extends ArrayAdapter<Contact> {

    public ContactsAdapter(Context context, ArrayList<Contact> contacts) {
        super(context, 0, contacts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item
        final Contact contact = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.adapter_contact_item, parent, false);
        }
        // Populate the data into the template view using the data object
        TextView tvName = (TextView) view.findViewById(R.id.fullname);
        TextView tvEmail = (TextView) view.findViewById(R.id.email_address);
        TextView tvPhone = (TextView) view.findViewById(R.id.phonenumber);
        ImageView avatar = (ImageView) view.findViewById(R.id.profile_picture);
        tvName.setText(contact.name);
        Glide.with(view.getContext())
                .load(contact.image)
                .placeholder(org.chat21.android.R.drawable.ic_person_avatar)
                .bitmapTransform(new CropCircleTransformation(view.getContext()))
                .into(avatar);
        tvEmail.setText("");
        tvPhone.setText("");
        if (contact.emails.size() > 0 && contact.emails.get(0) != null) {
            tvEmail.setText(contact.emails.get(0).address);
        }
        if (contact.numbers.size() > 0 && contact.numbers.get(0) != null) {
            tvPhone.setText(contact.numbers.get(0).number);
        }
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getContext(), contact.id, Toast.LENGTH_LONG).show();
//            }
//        });
        return view;
    }

}