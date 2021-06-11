package com.lifechurch.talk.model;

public class Feedback {
    public String text;
    public String firstname;
    public String imageurl;
    public String state;

    public Feedback(String text, String firstname, String imageurl,String state) {
        this.text = text;
        this.firstname = firstname;
        this.imageurl = imageurl;
        this.state = state;
    }
    public Feedback(){

    }

    public String gettext() {
        return text;
    }

    public String getfirstname() {
        return firstname;
    }

    public String getimageurl() {
        return imageurl;
    }

    public String getstate() {
        return state;
    }
}
