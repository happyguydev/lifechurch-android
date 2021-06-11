package org.chat21.android.ui.contacts.fragments;

public class Contactlistmodel {

    public String id;
    public String email;
    public String fullName;
    public String profilePictureUrl;

    public Contactlistmodel(String id, String email, String fullName,String profilePictureUrl) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.profilePictureUrl = profilePictureUrl;
    }
}
