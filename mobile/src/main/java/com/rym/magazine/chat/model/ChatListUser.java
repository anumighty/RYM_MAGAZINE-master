package com.rym.magazine.chat.model;

import com.google.firebase.database.Exclude;

/**
 * Created by Anumightytm on 5/5/2017.
 */

public class ChatListUser {
    private String counsellor;
    private String name;
    private String photo_profile;
    private String counsellorStatus;
    private String userStatus;


    public ChatListUser() {
    }

    public ChatListUser(String name, String photo_profile, String counsellorStatus, String userStatus, String counsellor) {
        this.name = name;
        this.photo_profile = photo_profile;
        this.counsellor = counsellor;
        this.counsellorStatus = counsellorStatus;
        this.userStatus = userStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto_profile() {
        return photo_profile;
    }

    public void setPhoto_profile(String photo_profile) {
        this.photo_profile = photo_profile;
    }

    public String getCounsellorStatus() {
        return counsellorStatus;
    }

    public void setCounsellorStatus(String counsellorStatus) {
        this.counsellorStatus = counsellorStatus;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getCounsellor() {
        return counsellor;
    }

    public void setCounsellor(String counsellor) {
        this.counsellor = counsellor;
    }
}
