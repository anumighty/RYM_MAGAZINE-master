package com.rym.magazine.mag;

/**
 * Created by Anumightytm on 2/1/2017.
 */
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rym.magazine.Constants;
import com.rym.magazine.MainApplication;
import com.rym.magazine.R;
import com.rym.magazine.utils.PrefUtils;

public class ArticleService extends Service {
    public static final String ONE_MINUTES = "60000";
    private final OnSharedPreferenceChangeListener mListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (PrefUtils.REFRESH_INTERVAL.equals(key)) {
                restartTimer(false);
            }
        }
    };

    private AlarmManager mAlarmManager;
    private PendingIntent mTimerIntent;

    @Override
    public IBinder onBind(Intent intent) {
        onRebind(intent);
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true; // we want to use rebind
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PrefUtils.registerOnPrefChangeListener(mListener);
        restartTimer(true);
    }

    private void restartTimer(boolean created) {
        if (mTimerIntent == null) {
            mTimerIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, RefreshArticleAlarmReceiver.class), 0);
        } else {
            mAlarmManager.cancel(mTimerIntent);
        }

        int time = 3600000;
        try {
            time = Math.max(60000, Integer.parseInt(ONE_MINUTES));
        } catch (Exception ignored) {
        }

        long elapsedRealTime = SystemClock.elapsedRealtime();
        long initialRefreshTime = elapsedRealTime + 10000;

        if (created) {
            long lastRefresh = PrefUtils.getLong(PrefUtils.LAST_ARTICLE_SCHEDULED_REFRESH, 0);

            // If the system rebooted, we need to reset the last value
            if (elapsedRealTime < lastRefresh) {
                lastRefresh = 0;
                PrefUtils.putLong(PrefUtils.LAST_ARTICLE_SCHEDULED_REFRESH, 0);
            }

            if (lastRefresh > 0) {
                // this indicates a service restart by the system
                initialRefreshTime = Math.max(initialRefreshTime, lastRefresh + time);
            }
        }

        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, initialRefreshTime, time, mTimerIntent);
    }

    @Override
    public void onDestroy() {
        if (mTimerIntent != null) {
            mAlarmManager.cancel(mTimerIntent);
        }
        PrefUtils.unregisterOnPrefChangeListener(mListener);
        super.onDestroy();
    }



    public class RefreshArticleAlarmReceiver extends BroadcastReceiver {
        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mLike = mRootRef.child("Likes");
        DatabaseReference mFacebook = mRootRef.child("Facebook");
        DatabaseReference mWhatsapp = mRootRef.child("Whatsapp");
        DatabaseReference mComments = mRootRef.child("Comments Count");

        @Override
        public void onReceive(Context context, Intent intent) {
            LikechildListener();


        }

        public void LikechildListener() {
            final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            final SharedPreferences.Editor editor = pref.edit();
            DatabaseReference mLikeCount = mLike.child("SPEAK IN, SPEAK ON, SPEAK OUT");
            Query queryRef = mLikeCount.orderByValue();

            queryRef.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Object ans = (Object) snapshot.getValue();
                    Intent notificationIntent = new Intent(ArticleService.this, List_of_Articles.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(ArticleService.this, 0, notificationIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(ArticleService.this);

                    Notification notification = builder.setContentTitle("RYM Magazine")
                            .setContentText("An article has been liked...")
                            .setTicker("New Like!")
                            .setSmallIcon(R.drawable.noti_small)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher)) //
                            .setWhen(System.currentTimeMillis()) //
                            .setAutoCancel(true) //
                            .setContentIntent(pendingIntent).build();

                    NotificationManager notificationManager = (NotificationManager) ArticleService.this.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(0, notification);
                    builder.setVibrate(new long[]{0, 1000});
                    String ringtone = null;
                    builder.setSound(Uri.parse(ringtone));
                    builder.setLights(0xffffffff, 300, 1000);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                }
            });
        }
    }
}

