package org.chat21.android.core.users.models;

import java.io.Serializable;

/**
 * Created by frontiere21 on 27/09/16.
 */
public interface IChatUser extends Serializable {

    void setId(String id);

    String getId();

    void setPhonenumber(String phonenumber);

    String getPhonenumber();

    void setFullName(String name);

    String getFullName();

    void setEmail(String email);

    String getEmail();

//    void setPassword(String password);
//
//    String getPassword();

    String getProfilePictureUrl();

    void setProfilePictureUrl(String profilePictureUrl);

    String getAutoAdd();

    void setAutoAdd(String autoAdd);

//    String getAuth();
//
//    void setAuth(String auth);

    @Override
    String toString();

    int compareTo(IChatUser another);



}
