package com.rym.magazine.chat.adapter;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import com.rym.magazine.R;
import com.rym.magazine.chat.model.ChatModel;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

/**
 * Created by Alessandro Barreto on 23/06/2016.
 */
public class ChatFirebaseAdapter extends FirebaseRecyclerAdapter<ChatModel,ChatFirebaseAdapter.MyChatViewHolder> {
    private Context context;
    private static final int RIGHT_MSG = 0;
    private static final int LEFT_MSG = 1;
    private static final int RIGHT_MSG_IMG = 2;
    private static final int LEFT_MSG_IMG = 3;
    private static final int RIGHT_MSG_AUD = 4;
    private static final int LEFT_MSG_AUD = 5;

    private ClickListenerChatFirebase mClickListenerChatFirebase;

    private String nameUser;



    public ChatFirebaseAdapter(DatabaseReference ref, String nameUser, ClickListenerChatFirebase mClickListenerChatFirebase) {
        super(ChatModel.class, R.layout.item_message_left, ChatFirebaseAdapter.MyChatViewHolder.class, ref);
        this.nameUser = nameUser;
        this.mClickListenerChatFirebase = mClickListenerChatFirebase;
    }

    @Override
    public MyChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == RIGHT_MSG){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right,parent,false);
            return new MyChatViewHolder(view);
        }else if (viewType == LEFT_MSG){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left,parent,false);
            return new MyChatViewHolder(view);
        }else if (viewType == RIGHT_MSG_AUD) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record_right, parent, false);
            return new MyChatViewHolder(view);
        }else if (viewType == LEFT_MSG_AUD) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record_left, parent, false);
            return new MyChatViewHolder(view);
        }else if (viewType == RIGHT_MSG_IMG){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right_img,parent,false);
            return new MyChatViewHolder(view);
        }else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left_img,parent,false);
            return new MyChatViewHolder(view);
        }
    }

    //Determine which view to use for each message (ImageView, TextView or Audioview)
    @Override
    public int getItemViewType(int position) {
        ChatModel model = getItem(position);
        if (model.getAudioModel() != null){
            if (model.getUserModel().getName().equals(nameUser)){
                return RIGHT_MSG_AUD;
            }else{
                //playBeep();
                return LEFT_MSG_AUD;
            }
        }else if (model.getFile() != null){
            if (model.getFile().getType().equals("img") && model.getUserModel().getName().equals(nameUser)){
                return RIGHT_MSG_IMG;
            }else{
                //playBeep();
                return LEFT_MSG_IMG;
            }
        }else if (model.getUserModel().getName().equals(nameUser)){
            return RIGHT_MSG;
        }else{
            //playBeep();
            return LEFT_MSG;
        }
    }

    @Override
    protected void populateViewHolder(MyChatViewHolder viewHolder, ChatModel model, int position) {
        viewHolder.setIvUser(model.getUserModel().getPhoto_profile());
        viewHolder.setTxtMessage(model.getMessage());
        viewHolder.setTvTimestamp(model.getTimeStamp());
        viewHolder.setTvDelivery(model.getId());
        viewHolder.tvIsLocation(View.GONE);
        if (model.getFile() != null){
            viewHolder.tvIsLocation(View.GONE);
            viewHolder.setIvChatPhoto(model.getFile().getUrl_file());
        }else if(model.getAudioModel() != null){
            viewHolder.setIvChatAudio();
        }
    }

    public class MyChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvTimestamp,tvLocation, tvDelivery;
        EmojiconTextView txtMessage;
        ImageView ivUser,ivChatPhoto, ivChatAudio;

        public MyChatViewHolder(View itemView) {
            super(itemView);
            tvTimestamp = (TextView)itemView.findViewById(R.id.timestamp);
            tvDelivery = (TextView)itemView.findViewById(R.id.delivery);
            txtMessage = (EmojiconTextView)itemView.findViewById(R.id.txtMessage);
            tvLocation = (TextView)itemView.findViewById(R.id.tvLocation);
            ivChatPhoto = (ImageView)itemView.findViewById(R.id.img_chat);
            ivChatAudio = (ImageView)itemView.findViewById(R.id.audio_chat);
            ivUser = (ImageView)itemView.findViewById(R.id.ivUserChat);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            ChatModel model = getItem(position);
            if (model.getAudioModel() != null) {
                mClickListenerChatFirebase.clickAudioChat(view, position, model.getAudioModel().getUrl_file());
            }
            else{
                mClickListenerChatFirebase.clickImageChat(view,position,model.getUserModel().getName(),model.getUserModel().getPhoto_profile(),model.getFile().getUrl_file());
            }
        }

        public void setTxtMessage(String message){
            if (txtMessage == null)return;
            txtMessage.setText(message);
        }

        public void setIvUser(String urlPhotoUser){
            if (ivUser == null)return;
            Glide.with(ivUser.getContext()).load(urlPhotoUser).centerCrop().transform(new CircleTransform(ivUser.getContext())).override(40,40).into(ivUser);
        }

        public void setTvTimestamp(String timestamp){
            if (tvTimestamp == null)return;
            tvTimestamp.setText(converteTimestamp(timestamp));
        }
        public void setTvDelivery(String delivery){
            if (tvDelivery == null)return;
            tvDelivery.setText(delivery);
        }

        public void setIvChatPhoto(String url){
            if (ivChatPhoto == null)return;
            Glide.with(ivChatPhoto.getContext()).load(url)
                    .override(100, 100)
                    .fitCenter()
                    .into(ivChatPhoto);
            ivChatPhoto.setOnClickListener(this);
        }

        public void setIvChatAudio(){
            ivChatAudio.setOnClickListener(this);
        }


        public void tvIsLocation(int visible){
            if (tvLocation == null)return;
            tvLocation.setVisibility(visible);
        }

    }



    private CharSequence converteTimestamp(String mileSegundos){
        return DateUtils.getRelativeTimeSpanString(Long.parseLong(mileSegundos),System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
    }

    /*private Boolean appON = false;
    private void playBeep(){
        if(appON){
            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 20);
            toneGen1.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP,100);
        } else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    appON = true;
                }
            }, 5 * 1000);

        }
    }*/

}
