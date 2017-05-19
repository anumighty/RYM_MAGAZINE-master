package com.rym.magazine.chat.model;

import com.google.firebase.database.Exclude;

/**
 * Created by Alessandro Barreto on 22/06/2016.
 */
public class UserModel {

    private String id;
    private String name;
    private String photo_profile;
    public String firebaseToken;

    private String counsellor;
    private String counsellorStatus;
    private String userStatus;

    public UserModel() {
    }

    public UserModel(String name, String photo_profile, String id, String firebaseToken) {
        this.name = name;
        this.photo_profile = photo_profile;
        this.id = id;
        this.firebaseToken = firebaseToken;
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

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
