package com.rym.magazine.chat.model;


/**
 * Created by Alessandro Barreto on 17/06/2016.
 */
public class ChatModel {

    private String id;
    private UserModel userModel;
    private String message;
    private String timeStamp;
    private FileModel file;
    private AudioModel audio;

    public ChatModel() {
    }

    public ChatModel(UserModel userModel, String message, String timeStamp, FileModel file, String id) {
        this.userModel = userModel;
        this.message = message;
        this.timeStamp = timeStamp;
        this.file = file;
        this.id = id;
    }

    public ChatModel(UserModel userModel, String timeStamp, AudioModel audio, String id) {
        this.userModel = userModel;
        this.timeStamp = timeStamp;
        this.audio = audio;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public FileModel getFile() {
        return file;
    }

    public void setFile(FileModel file) {
        this.file = file;
    }

    public AudioModel getAudioModel() {
        return audio;
    }

    public void setAudioModel(AudioModel audio) {
        this.audio = audio;
    }

    @Override
    public String toString() {
        return "ChatModel{" +
                "id=" + id +
                "audio=" + audio +
                ", file=" + file +
                ", timeStamp='" + timeStamp + '\'' +
                ", message='" + message + '\'' +
                ", userModel=" + userModel +
                '}';
    }
}
