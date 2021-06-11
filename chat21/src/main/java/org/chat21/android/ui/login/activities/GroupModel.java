package org.chat21.android.ui.login.activities;

import org.chat21.android.core.users.models.IChatUser;

import java.util.List;

public class GroupModel {
    public static List<IChatUser> selectedUser;

    public GroupModel (List<IChatUser> selectedUser) {
        this.selectedUser = selectedUser;
    }
}
