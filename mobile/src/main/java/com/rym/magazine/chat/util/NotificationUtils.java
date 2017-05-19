package com.rym.magazine.chat.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.rym.magazine.R;
import com.rym.magazine.chat.Chat;
import com.rym.magazine.chat.model.ChatModel;

public final class NotificationUtils {

    public static final int NOTIFICATION_ID = 1;

    public static void notifyMessage(@NonNull Context context, @NonNull ChatModel model) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Drawable drawable= ContextCompat.getDrawable(context,R.mipmap.anu);

        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setLargeIcon(bitmap)
                        .setSmallIcon(R.drawable.noti_small)
                        .setContentTitle(model.getUserModel().getName())
                        .setContentText(model.getMessage())
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri);


        Intent resultIntent = new Intent(context, Chat.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(Chat.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private NotificationUtils() {
        throw new AssertionError("No instances.");
    }
}
