package com.rym.magazine.chat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.rym.magazine.MainApplication;
import com.rym.magazine.chat.adapter.ChatFirebaseAdapter;
import com.rym.magazine.chat.adapter.CircleTransform;
import com.rym.magazine.chat.adapter.ClickListenerChatFirebase;
import com.rym.magazine.chat.fcm.FcmNotificationBuilder;
import com.rym.magazine.chat.model.AudioModel;
import com.rym.magazine.chat.model.ChatModel;
import com.rym.magazine.chat.model.FileModel;
import com.rym.magazine.chat.model.UserModel;
import com.rym.magazine.chat.util.NotificationUtils;
import com.rym.magazine.chat.util.SharedPrefUtil;
import com.rym.magazine.chat.util.Util;
import com.rym.magazine.R;
import com.rym.magazine.chat.view.FullScreenImageActivity;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import android.text.format.DateUtils;


public class Chat extends AppCompatActivity implements View.OnClickListener, ClickListenerChatFirebase  {

    private static final int IMAGE_GALLERY_REQUEST = 1;
    private static final int IMAGE_CAMERA_REQUEST = 2;
    static final String TAG = Chat.class.getSimpleName();

    //Firebase and GoogleApiClient
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mFirebaseDatabaseReference;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    //CLass Model
    private UserModel userModel,autoMessageModel;

    //Views UI
    private RecyclerView rvListMessage;
    private LinearLayoutManager mLinearLayoutManager;
    private ImageView btSendMessage, btSendImage, btEmoji, ivUser;
    private Button btSendRecord;
    private EmojiconEditText edMessage;
    private View contentRoot;
    private EmojIconActions emojIcon;
    private TextView tvUser, onlineStatus;

    //File
    private File filePathImageCamera;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private String Topic, Surname, Name, ChatListNameClicked, CounselleeToken, CounsellorToken, Counsellor, openChat, profPicsUrl, picsUrlOpenChat;
    private LinearLayout v;
    private MediaRecorder mRecorder;
    private String audioPath;
    private static final String LOG_TAG = "Record_log";
    private ProgressDialog progressDialog;
    private boolean recordStart = false;

    private long delay = 2000; // 1 seconds after user stops typing
    private long last_text_edit = 0;

    private Set<String> ids;
    private boolean notify;
    private boolean fromNotif = false;
    private String receiverFirebaseToken;

    public static void startActivity(Context context,
                                     String receiver,
                                     String receiverUid,
                                     String firebaseToken) {
        Intent intent = new Intent(context, Chat.class);
        intent.putExtra(Constants.ARG_RECEIVER, receiver);
        //intent.putExtra(Constants.ARG_RECEIVER_UID, receiverUid);
        intent.putExtra(Constants.ARG_FIREBASE_TOKEN, firebaseToken);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ivUser = (ImageView)toolbar.findViewById(R.id.avatar);
        tvUser = (TextView)toolbar.findViewById(R.id.title);
        onlineStatus = (TextView)toolbar.findViewById(R.id.online);

        final SharedPreferences pref = getApplicationContext().getSharedPreferences("RYM_Mag_11", MODE_PRIVATE);
        Surname = pref.getString("Surname", "UnknownUser");
        Name = pref.getString("OtherName", "");
        v = (LinearLayout)findViewById(R.id.contentRoot);
        //Progress dialog initiation
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading chat! Please Wait...");
        progressDialog.show();

        Bundle bundle = getIntent().getExtras();
        //Open chat from notification
        openChat = bundle.getString("openChat");
        if(openChat != null){
            Topic = bundle.getString("topic");
            TextView topic = (TextView)findViewById(R.id.topic);
            topic.setText(Topic);
            if(pref.getBoolean("Counsellor",false)){
                tvUser.setText(openChat);
                picsUrlOpenChat = bundle.getString("picsUrl");
                Glide.with(this).load(bundle.getString("picsUrl")).centerCrop().transform(new CircleTransform(this)).override(40,40).into(ivUser);
            }else {
                //Do nothing...
            }
        }else{//Open chat normally
            if(pref.getBoolean("Counsellor",false)){
                ChatListNameClicked = bundle.getString("Full_Name");
                tvUser.setText(bundle.getString("Full_Name"));
                profPicsUrl = bundle.getString("userPicsUrl");
                Glide.with(this).load(bundle.getString("userPicsUrl")).centerCrop().transform(new CircleTransform(this)).override(40,40).into(ivUser);
                TextView topic = (TextView)findViewById(R.id.topic);
                topic.setText(pref.getString("ClickedTopic",null));

            } else{
                Topic = bundle.getString("Topic");
                TextView topic = (TextView)findViewById(R.id.topic);
                topic.setText(Topic);
            }
        }



        if (!Util.verificaConexao(this)) {
            Util.initToast(this, "Turn ON internet to start application");
        }
        bindViews();
        verificaUsuarioLogado();


        //Check if user is typing
        CheckTyping();

        //Check for read status
        if(openChat != null){
            //Read status
            ReadStatus2();
        }else{
            //Read status
            ReadStatus();
        }


        //File to save audio recordings
        String time = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
        audioPath = getApplicationContext().getFilesDir().getAbsolutePath();
        audioPath+=time+".3gp";
        /*audioPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        audioPath+="/audioRec"+time+".3gp";*/

        btSendImage = (ImageView)findViewById(R.id.buttonImage);
        btSendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animFadein = AnimationUtils.loadAnimation(Chat.this, R.anim.fade_in);
                btSendImage.startAnimation(animFadein);
                photoGalleryIntent();
            }
        });

        btSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animFadein = AnimationUtils.loadAnimation(Chat.this, R.anim.fade_in);
                btSendMessage.startAnimation(animFadein);
                if(!edMessage.getText().toString().equals("")){
                    sendMessageFirebase();
                }else{
                    Snackbar.make(v, "Enter message first...", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }

            }
        });



        btSendRecord = (Button) findViewById(R.id.buttonRec);
        btSendRecord.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent){

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    Animation recordBig = AnimationUtils.loadAnimation(Chat.this, R.anim.record_button_big);
                    btSendRecord.startAnimation(recordBig);
                   btSendRecord.setOnLongClickListener(new View.OnLongClickListener() {
                        public boolean onLongClick(View v) {
                            recordStart = true;
                            startRecording();
                            edMessage.setText("Recording started...");
                            return true;
                        }
                    });
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    Animation recordSmall = AnimationUtils.loadAnimation(Chat.this, R.anim.record_button_small);
                    btSendRecord.startAnimation(recordSmall);
                    if(recordStart){
                        stopRecording();
                        edMessage.setText("");
                        recordStart = false;
                    }else{
                        Snackbar.make(v, "Hold to start recording!", Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();
                    }
                }
                return false;
            }
        });

        /*btSendRecord.setOnTouchListener(new View.OnTouchListener() {
            boolean recordStart = false;
            Timer timer = new Timer();
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch ( event.getAction() ) {
                    case MotionEvent.ACTION_DOWN:
                        //start timer
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                // invoke intent
                                //Toast.makeText(Chat.this, "Recording started!", Toast.LENGTH_LONG).show();
                                edMessage.setHint("Recording started!");
                                startRecording();
                                recordStart = true;
                            }
                        }, 2000); //time out 2s
                        return true;
                    case MotionEvent.ACTION_UP:
                        if(recordStart){
                            stopRecording();
                            //stop timer
                            timer.cancel();
                            recordStart = false;

                        } else{
                            //stop timer
                            timer.cancel();
                            Snackbar.make(v, "Hold to start recording!", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                            recordStart = false;

                        }
                        return true;
                }
                return false;
            }
        });*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        final StorageReference storageRef = storage.getReferenceFromUrl(Util.URL_STORAGE_REFERENCE).child(Util.FOLDER_STORAGE_IMG);

        //Upload image to server
        if (requestCode == IMAGE_GALLERY_REQUEST) {
            if (resultCode == RESULT_OK) {
                final Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    Snackbar.make(v, "Do you want to send Image?", Snackbar.LENGTH_LONG)
                            .setAction("YES", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    sendFileFirebase(storageRef, selectedImageUri);
                                    Snackbar.make(v, "Sending Image, please wait...", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                            }).show();
                } else {
                    //URI IS NULL
                }
            }

        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
        }
    }

    private void startRecording(){
        try {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(audioPath);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mRecorder.start();
    }

    private void stopRecording(){
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        edMessage.setHint("");
        Snackbar.make(v, "Do you want to send Recording?", Snackbar.LENGTH_LONG)
                .setAction("YES", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar.make(v, "Sending Recording, please wait...", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        uploadAudio();
                    }
                }).show();
    }

    private void uploadAudio(){
        StorageReference audioStorageRef = storage.getReferenceFromUrl(Util.URL_STORAGE_REFERENCE).child(Util.FOLDER_STORAGE_AUDIO);
        final String time = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("RYM_Mag_11", MODE_PRIVATE);

        if(pref.getBoolean("Counsellor",false)){
            Bundle bundle = getIntent().getExtras();
            //Open chat from notification
            openChat = bundle.getString("openChat");
            if (openChat != null) {
                StorageReference recordRef = audioStorageRef.child("Recordings" + Topic + "/" + openChat + "/" + time);
                Uri uri = Uri.fromFile(new File(audioPath));
                recordRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Snackbar.make(v, "Voice note sent", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        //save the file_type,image_url,name and size to FileModel
                        AudioModel audioModel = new AudioModel("3gp", downloadUrl.toString(), time, "");
                        ChatModel chatModel = new ChatModel(userModel, Calendar.getInstance().getTime().getTime()+"", audioModel,"");
                        //Update the chat model with the file just uploaded
                        mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_CHAT + Topic + "/" + openChat).push().setValue(chatModel);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(v, "Voice note sending failed", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
            } else{
                StorageReference recordRef = audioStorageRef.child("Recordings" + pref.getString("ClickedTopic",null) + "/" + ChatListNameClicked + "/" + time);
                Uri uri = Uri.fromFile(new File(audioPath));
                recordRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Snackbar.make(v, "Voice note sent", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        //save the file_type,image_url,name and size to FileModel
                        AudioModel audioModel = new AudioModel("3gp", downloadUrl.toString(), time, "");
                        ChatModel chatModel = new ChatModel(userModel, Calendar.getInstance().getTime().getTime()+"", audioModel,"");
                        //Update the chat model with the file just uploaded
                        mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_CHAT + pref.getString("ClickedTopic", null) + "/" + ChatListNameClicked).push().setValue(chatModel);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(v, "Voice note sending failed", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
            }

        }
        else{
            StorageReference recordRef = audioStorageRef.child("Recordings" + Topic + "/" + Surname +" "+ Name + "/" + time);
            Uri uri = Uri.fromFile(new File(audioPath));
            recordRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Snackbar.make(v, "Voice note sent", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    //save the file_type,image_url,name and size to FileModel
                    AudioModel audioModel = new AudioModel("3gp", downloadUrl.toString(), time, "");
                    ChatModel chatModel = new ChatModel(userModel, Calendar.getInstance().getTime().getTime() + "", audioModel,"");
                    //Update the chat model with the file just uploaded
                    mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_CHAT + Topic + "/" + Surname+" " + Name).push().setValue(chatModel);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Snackbar.make(v, "Voice note sending failed", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }

    }

    //Start image Full screen activity when image is clicked on and put extra userName,UserPics and imageClicked
    @Override
    public void clickImageChat(View view, int position,String nameUser,String urlPhotoUser,String urlPhotoClick) {
        Intent intent = new Intent(this,FullScreenImageActivity.class);
        intent.putExtra("nameUser",nameUser);
        intent.putExtra("urlPhotoUser", urlPhotoUser);
        intent.putExtra("urlPhotoClick", urlPhotoClick);
        startActivity(intent);
    }

    //play audio when audio file is clicked
    @Override
    public void clickAudioChat(View view, int position,String urlAudioClick) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(urlAudioClick);
        }catch(Exception e){

        }
        mediaPlayer.prepareAsync();
        //You can show progress dialog here until it prepared to play
        progressDialog.setMessage("Preparing to play! Please Wait...");
        progressDialog.show();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //Now dismis progress dialog, Media palyer will start playing
                progressDialog.dismiss();
                mp.start();
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // dissmiss progress bar here. It will come here when MediaPlayer
                //  is not able to play file. You can show error message to user
                progressDialog.dismiss();
                return false;
            }
        });
    }

    //ALERT!!! I dont need this
    @Override
    public void clickImageMapChat(View view, int position,String latitude,String longitude) {
        String uri = String.format("geo:%s,%s?z=17&q=%s,%s", latitude,longitude,latitude,longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }

    /**
     *Upload image file to Firebase
     * NOTE: ENABLE DOCUMENT UPLOAD TOO
     */
    private void sendFileFirebase(StorageReference storageReference, final Uri file){
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("RYM_Mag_11", MODE_PRIVATE);
        if (storageReference != null){
            final String time = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
            if(pref.getBoolean("Counsellor",false)){
                Bundle bundle = getIntent().getExtras();
                //Open chat from notification
                openChat = bundle.getString("openChat");
                if (openChat != null) {
                    StorageReference imageGalleryRef = storageReference.child(Constants.STORAGE_PATH_CHAT +Topic+"/" + openChat + "/" + time);
                    UploadTask uploadTask = imageGalleryRef.putFile(file);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(v, "Image upload failed!", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            //save the file_type,image_url,name and size to FileModel
                            FileModel fileModel = new FileModel("img", downloadUrl.toString(), time, "");
                            ChatModel chatModel = new ChatModel(userModel, "", Calendar.getInstance().getTime().getTime() + "", fileModel,"");
                            //Update the chat model with the file just uploaded
                            mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_CHAT + Topic + "/" + openChat).push().setValue(chatModel);
                            Snackbar.make(v, "Image upload completed!", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    });
                } else{
                    StorageReference imageGalleryRef = storageReference.child(Constants.STORAGE_PATH_CHAT +pref.getString("ClickedTopic",null)+"/" + ChatListNameClicked + "/" + time);
                    UploadTask uploadTask = imageGalleryRef.putFile(file);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(v, "Image upload failed!", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            //save the file_type,image_url,name and size to FileModel
                            FileModel fileModel = new FileModel("img", downloadUrl.toString(), time, "");
                            ChatModel chatModel = new ChatModel(userModel, "", Calendar.getInstance().getTime().getTime() + "", fileModel,"");
                            //Update the chat model with the file just uploaded
                            mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_CHAT + pref.getString("ClickedTopic",null) + "/" + ChatListNameClicked).push().setValue(chatModel);
                            Snackbar.make(v, "Image upload completed!", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    });
                }


            }
            else{
                StorageReference imageGalleryRef = storageReference.child(Constants.STORAGE_PATH_CHAT +Topic+"/" + Surname+" " + Name + "/" + time);
                UploadTask uploadTask = imageGalleryRef.putFile(file);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(v, "Image upload failed!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        //save the file_type,image_url,name and size to FileModel
                        FileModel fileModel = new FileModel("img", downloadUrl.toString(), time, "");
                        ChatModel chatModel = new ChatModel(userModel, "", Calendar.getInstance().getTime().getTime() + "", fileModel,"");
                        //Update the chat model with the file just uploaded
                        mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_CHAT + Topic + "/" + Surname+" " + Name).push().setValue(chatModel);
                        Snackbar.make(v, "Image upload completed!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
            }
        } else {
            //IS NULL
        }
    }

    private void photoGalleryIntent(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture_title)), IMAGE_GALLERY_REQUEST);
    }

    /**
     * Send chat with no image file to Firebase
     */
    private void sendMessageFirebase(){
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("RYM_Mag_11", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();

        if(pref.getBoolean("Counsellor",false)){
            ChatModel model = new ChatModel(userModel,edMessage.getText().toString().trim(), Calendar.getInstance().getTime().getTime()+"",null,"");
            Bundle bundle = getIntent().getExtras();
            //Open chat from notification
            openChat = bundle.getString("openChat");
            if (openChat != null) {
                final DatabaseReference messageRef = mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_CHAT+Topic+"/"+openChat);
                messageRef.push().setValue(model);
                mFirebaseDatabaseReference.child("ChatTopicsUser/"+Topic+"/"+openChat).child("counsellor").setValue(Surname+" "+Name);
                edMessage.setText("");
                ChatListNameClicked = openChat;
                profPicsUrl = picsUrlOpenChat;
            } else{
                final DatabaseReference messageRef = mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_CHAT+pref.getString("ClickedTopic",null)+"/"+ChatListNameClicked);
                messageRef.push().setValue(model);
                mFirebaseDatabaseReference.child("ChatTopicsUser/"+pref.getString("ClickedTopic",null)+"/"+ChatListNameClicked).child("counsellor").setValue(Surname+" "+Name);
                edMessage.setText("");
            }
            //Get receiver's Token
            mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_PROFILE+"/"+ChatListNameClicked)
                    .child(Constants.ARG_FIREBASE_TOKEN).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    System.out.println(snapshot.getValue());
                    CounselleeToken = snapshot.getValue().toString();
                }
                @Override public void onCancelled(DatabaseError error) { }
            });
            // send push notification to the receiver
            sendPushNotificationToReceiver(model.getUserModel().getName(),
                    model.getMessage(),
                    ChatListNameClicked,
                    pref.getString("ClickedTopic",null),
                    profPicsUrl,
                    new SharedPrefUtil(getApplication()).getString(Constants.ARG_FIREBASE_TOKEN),
                    CounselleeToken);
        }
        else{
            final ChatModel model = new ChatModel(userModel,edMessage.getText().toString().trim(), Calendar.getInstance().getTime().getTime()+"",null,"");
            mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_CHAT+Topic+"/"+Surname+" " + Name).push().setValue(model);
            if(pref.getBoolean("firstMessage",true)){
                mFirebaseDatabaseReference.child("ChatTopicsUser/" + Topic+"/"+Surname+" " + Name).setValue(userModel);
                //mFirebaseDatabaseReference.child("ChatTopicsUser/" +Topic+"/"+Surname+" " + Name).child("counsellorStatus").setValue("online");
                mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_PROFILE +"/"+ Surname+" " + Name).child("status").setValue("online");
                autoMessageModel = new UserModel("RYM MAGAZINE", null, "", null);
                mFirebaseDatabaseReference.child("ChatTopicsUser/" +Topic+"/"+Surname+" " + Name).child("counsellor").setValue("No counsellor yet!");
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Send automatic messgae to first time user after 3seconds
                        final ChatModel autoMessage = new ChatModel(autoMessageModel,"Welcome... We received your message. A counsellor will reply you shortly but before then, you can tell us how we can be of help. Please be reminded that all discussions here are encrypted, which means they are secured and can only be seen by you and your counsellor. Hence, your privacy is assured.\n\nRYM MAGAZINE", Calendar.getInstance().getTime().getTime()+"",null,"");
                        mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_CHAT+Topic+"/"+Surname+" " + Name).push().setValue(autoMessage);
                    }
                }, 2000);

                editor.putBoolean("firstMessage",false);
                editor.apply();
                CheckTyping();
            }
            edMessage.setText("");
            //Get notification receiver
            mFirebaseDatabaseReference.child("ChatTopicsUser/" +Topic+"/"+Surname+" " + Name)
                    .child("counsellor").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    System.out.println(snapshot.getValue());
                    Counsellor = snapshot.getValue().toString();
                    //If no counsellor yet
                    if(Counsellor.equals("No counsellor yet!")){
                        //For now do nothing...
                    }
                    else{ //There is a counsellor
                    //Get receiver's Token
                    mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_PROFILE+"/"+Counsellor)
                            .child(Constants.ARG_FIREBASE_TOKEN).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            System.out.println(snapshot.getValue());
                            CounsellorToken = snapshot.getValue().toString();
                            // send push notification to the receiver
                            sendPushNotificationToReceiver(model.getUserModel().getName(),
                                    model.getMessage(),
                                    model.getUserModel().getName(),
                                    Topic,
                                    "",
                                    new SharedPrefUtil(getApplication()).getString(Constants.ARG_FIREBASE_TOKEN),
                                    CounsellorToken);
                        }
                        @Override public void onCancelled(DatabaseError error) { }
                    });
                }
                }
                @Override public void onCancelled(DatabaseError error) { }
            });
        }
    }

    private void sendPushNotificationToReceiver(String username,
                                                String message,
                                                String receiver,
                                                String topic,
                                                String userProfPics,
                                                String firebaseToken,
                                                String receiverFirebaseToken) {
        FcmNotificationBuilder.initialize()
                .title(username)
                .message(message)
                .username(username)
                .receiver(receiver)
                .topic(topic)
                .userProfPics(userProfPics)
                .firebaseToken(firebaseToken)
                .receiverFirebaseToken(receiverFirebaseToken)
                .send();
    }

    /**
     * Arrange chat and start from the last read message chat (scroll down automatically)
     */
    private void lerMessagensFirebase() {
        Bundle bundle = getIntent().getExtras();
        //Open chat from notification
        openChat = bundle.getString("openChat");
        if (openChat != null) {
            mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference mFirebaseDatabaseReference2 = mFirebaseDatabaseReference.child("chats");
            DatabaseReference mFirebaseDatabaseReference3 = mFirebaseDatabaseReference2.child(Topic);
            final ChatFirebaseAdapter firebaseAdapter = new ChatFirebaseAdapter(mFirebaseDatabaseReference3.child(openChat), userModel.getName(), this);
            //Use this to know the position of last chat read
            firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    int friendlyMessageCount = firebaseAdapter.getItemCount();
                    int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                    if (lastVisiblePosition == -1 ||
                            (positionStart >= (friendlyMessageCount - 1) &&
                                    lastVisiblePosition == (positionStart - 1))) {
                        rvListMessage.scrollToPosition(positionStart);
                    }
                }
            });
            rvListMessage.setLayoutManager(mLinearLayoutManager);
            rvListMessage.setAdapter(firebaseAdapter);
            progressDialog.dismiss();
        } else {
            final SharedPreferences pref = getApplicationContext().getSharedPreferences("RYM_Mag_11", MODE_PRIVATE);
            if (pref.getBoolean("Counsellor", false)) {
                String ClickedTopic = pref.getString("ClickedTopic", null);
                mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
                DatabaseReference mFirebaseDatabaseReference2 = mFirebaseDatabaseReference.child("chats");
                DatabaseReference mFirebaseDatabaseReference3 = mFirebaseDatabaseReference2.child(ClickedTopic);
                final ChatFirebaseAdapter firebaseAdapter = new ChatFirebaseAdapter(mFirebaseDatabaseReference3.child(ChatListNameClicked), userModel.getName(), this);
                //Use this to know the position of last chat read
                firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        super.onItemRangeInserted(positionStart, itemCount);
                        int friendlyMessageCount = firebaseAdapter.getItemCount();
                        int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                        if (lastVisiblePosition == -1 ||
                                (positionStart >= (friendlyMessageCount - 1) &&
                                        lastVisiblePosition == (positionStart - 1))) {
                            rvListMessage.scrollToPosition(positionStart);
                        }
                    }
                });
                rvListMessage.setLayoutManager(mLinearLayoutManager);
                rvListMessage.setAdapter(firebaseAdapter);
                progressDialog.dismiss();
            } else {
                mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
                DatabaseReference mFirebaseDatabaseReference2 = mFirebaseDatabaseReference.child("chats");
                DatabaseReference mFirebaseDatabaseReference3 = mFirebaseDatabaseReference2.child(Topic);
                final ChatFirebaseAdapter firebaseAdapter = new ChatFirebaseAdapter(mFirebaseDatabaseReference3.child(Surname + " " + Name), userModel.getName(), this);
                //Use this to know the position of last chat read
                firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        super.onItemRangeInserted(positionStart, itemCount);
                        int friendlyMessageCount = firebaseAdapter.getItemCount();
                        int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                        if (lastVisiblePosition == -1 ||
                                (positionStart >= (friendlyMessageCount - 1) &&
                                        lastVisiblePosition == (positionStart - 1))) {
                            rvListMessage.scrollToPosition(positionStart);
                        }
                    }
                });
                rvListMessage.setLayoutManager(mLinearLayoutManager);
                rvListMessage.setAdapter(firebaseAdapter);
                progressDialog.dismiss();
            }

        }
    }

    /**
     * Verify it user is logged in, else start Login Activity
     */
    private void verificaUsuarioLogado(){
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("RYM_Mag_11", MODE_PRIVATE);
        mFirebaseAuth = FirebaseAuth.getInstance();
        if (mFirebaseAuth.getCurrentUser()== null){
            startActivity(new Intent(this, CreateAccount.class));
            finish();
        }else{
            userModel = new UserModel(Surname+" "+Name, pref.getString("userPicsUrl", null), "", new SharedPrefUtil(getApplication()).getString(Constants.ARG_FIREBASE_TOKEN));
            lerMessagensFirebase();
        }
    }

    /**
     * Initialise views
     */
    private void bindViews(){
        contentRoot = findViewById(R.id.contentRoot);
        edMessage = (EmojiconEditText)findViewById(R.id.editTextMessage);
        btSendMessage = (ImageView)findViewById(R.id.buttonSend);
        btEmoji = (ImageView)findViewById(R.id.buttonEmoji);
        emojIcon = new EmojIconActions(this,contentRoot,edMessage,btEmoji,"#F44336","#ffe3e3","#ffffff");
        emojIcon.setIconsIds(R.drawable.ic_action_keyboard, R.drawable.smiley);
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e(TAG, "Keyboard opened!");
            }

            @Override
            public void onKeyboardClose() {
                Log.e(TAG, "Keyboard closed");
            }
        });
        emojIcon.ShowEmojIcon();
        rvListMessage = (RecyclerView)findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

    }

    private void CheckTyping(){
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("RYM_Mag_11", MODE_PRIVATE);
        final Handler handler = new Handler();
        final Runnable input_finish_checker = new Runnable() {
            public void run() {
                if (System.currentTimeMillis() > (last_text_edit + delay - 500)) {
                    if(pref.getBoolean("Counsellor",false)){
                        mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_PROFILE+"/" +Surname+" " + Name).child("status").setValue("typing...");
                    }
                    else{
                        if(pref.getBoolean("firstMessage",true)) {
                        }else{
                            mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_PROFILE+"/" + Surname+" " + Name).child("status").setValue("typing...");
                        }
                    }
                }
            }
        };
        edMessage.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged (CharSequence s,int start, int count, int after){
                }
                @Override
                public void onTextChanged ( final CharSequence s, int start, int before, int count){
                    //You need to remove this to run only once
                    handler.removeCallbacks(input_finish_checker);

                }
                @Override
                public void afterTextChanged ( final Editable s){
                    //avoid triggering event when text is empty
                    if (s.length() > 0) {
                        last_text_edit = System.currentTimeMillis();
                        handler.postDelayed(input_finish_checker, delay);
                    } else {
                        if(pref.getBoolean("Counsellor",false)){
                            mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_PROFILE+"/" +Surname+" " + Name).child("status").setValue("online");
                        }
                        else{
                            if(pref.getBoolean("firstMessage",true)) {
                            }else{
                                mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_PROFILE+"/" + Surname+" " + Name).child("status").setValue("online");
                            }
                        }
                    }
                }
            }
        );
    }

    private CharSequence converteTimestamp(String mileSegundos){
        return DateUtils.getRelativeTimeSpanString(Long.parseLong(mileSegundos),System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
    }
    private void ReadStatus(){
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("RYM_Mag_11", MODE_PRIVATE);
        if(pref.getBoolean("Counsellor",false)){
            mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_PROFILE+"/" + ChatListNameClicked)
                    .child("status").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    System.out.println(snapshot.getValue());
                    if(snapshot.getValue().equals("online")){
                        onlineStatus.setText(snapshot.getValue().toString());
                    }
                    else if(snapshot.getValue().equals("typing...")){
                        onlineStatus.setText(snapshot.getValue().toString());
                    }
                    else{
                        onlineStatus.setText(converteTimestamp(snapshot.getValue().toString()));
                    }
                }
                @Override public void onCancelled(DatabaseError error) { }
            });
        }
        else {
            if (pref.getBoolean("firstMessage", true)) {

            }
            else{
                mFirebaseDatabaseReference.child("ChatTopicsUser/" + Topic+"/"+Surname+" "+Name)
                        .child("counsellor").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        System.out.println(snapshot.getValue());
                        mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_PROFILE+"/" + snapshot.getValue().toString())
                                .child("status").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                System.out.println(snapshot.getValue());
                                if(snapshot.getValue().equals("online")){
                                    onlineStatus.setText(snapshot.getValue().toString());
                                }
                                else if(snapshot.getValue().equals("typing...")){
                                    onlineStatus.setText(snapshot.getValue().toString());
                                }
                                else{
                                    onlineStatus.setText(converteTimestamp(snapshot.getValue().toString()));
                                }
                            }
                            @Override public void onCancelled(DatabaseError error) { }
                        });
                    }
                    @Override public void onCancelled(DatabaseError error) { }
                });
            }
        }
    }

    private void ReadStatus2(){
        ChatListNameClicked = openChat;
        ReadStatus();
    }

    public void SendNotification(){
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("RYM_Mag_11", MODE_PRIVATE);
        notify = false;
        ids = new HashSet<>();
        if(pref.getBoolean("Counsellor",false)){
            mFirebaseDatabaseReference.child("ChatTopicsUser/"+pref.getString("ClickedTopic",null)+"/"+ChatListNameClicked).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (notify) {
                        ChatModel message = dataSnapshot.getValue(ChatModel.class);
                        if (!ids.contains(dataSnapshot.getKey()))
                            NotificationUtils.notifyMessage(Chat.this, message);
                    } else {
                        ids.add(dataSnapshot.getKey());
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }else{
            mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_CHAT+Topic+"/"+Surname+" " + Name).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (notify) {
                        ChatModel message = dataSnapshot.getValue(ChatModel.class);
                        if (!ids.contains(dataSnapshot.getKey()))
                            NotificationUtils.notifyMessage(Chat.this, message);
                    } else {
                        ids.add(dataSnapshot.getKey());
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_PROFILE+"/" +Surname+" "+Name).child("status").setValue("online");
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainApplication.setChatActivityOpen(true);
        notify = false;
        mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_PROFILE+"/" +Surname+" "+Name).child("status").setValue("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainApplication.setChatActivityOpen(false);
        notify = true;
        mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_PROFILE+"/" +Surname+" "+Name).child("status").setValue(Calendar.getInstance().getTime().getTime()+"");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
        return;
    }


}
