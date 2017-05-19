package com.rym.magazine.mag;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rym.magazine.R;
import com.rym.magazine.activity.AboutActivity;
import com.rym.magazine.activity.HomeActivity;
import com.rym.magazine.chat.Login;
import com.rym.magazine.mag.Fragments.CoverPage;
import com.rym.magazine.mag.Fragments.About;
import com.rym.magazine.mag.curl.CurlActivity;
import com.rym.magazine.provider.FeedDataContentProvider;
import com.rym.magazine.service.FetcherService;
import com.rym.magazine.service.RefreshService;
import com.rym.magazine.utils.PrefUtils;
import com.rym.magazine.utils.UiUtils;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    ActionBar actionBar;

    private static final int[] TOPIC_NAME = new int[]{R.string.ng_news_top_stories};

    private static final String[] TOPIC_CODES = new String[]{"x"};

    private boolean mCanQuit = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.setPreferenceTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null) {
            setupNavigationDrawerContent(navigationView);
        }

        setupNavigationDrawerContent(navigationView);

        //REFRESH NEWS FEED OPTION (True/false)
        if (PrefUtils.getBoolean(PrefUtils.REFRESH_ENABLED, true)) {
            // starts the service independent to this activity
            startService(new Intent(this, RefreshService.class));
        } else {
            stopService(new Intent(this, RefreshService.class));
        }

        //Refresh on app open (True/false)
        if (PrefUtils.getBoolean(PrefUtils.REFRESH_ON_OPEN_ENABLED, false)) {
            if (!PrefUtils.getBoolean(PrefUtils.IS_REFRESHING, false)) {
                startService(new Intent(MainActivity.this, FetcherService.class).setAction(FetcherService.ACTION_REFRESH_FEEDS));
            }
        }

        //First fragment
        setFragment(0);

        //Welcome screen for 1st time user
        welcomeDialog();

        //Put in the stored value of Unread feeds in nav drawer textview and run runnable so that it keeps track of the changes
        new Runnable() {
            int updateInterval = 2000; //=two second
            @Override
            public void run() {
                NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
                final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String unreadFeeds = pref.getString("UnreadFeedsCount", "0");
                View header = navigationView.getHeaderView(0);
                TextView UnreadFeed = (TextView)header.findViewById(R.id.unread_feed_count);
                UnreadFeed.setText(unreadFeeds);

                UnreadFeed.postDelayed(this, updateInterval);
            }
        }.run();

        //Open News Feed Activity when Unread Feeds Count is clicked
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);

        View header = navigationView.getHeaderView(0);
        final RelativeLayout UnreadFeedBtn = (RelativeLayout)header.findViewById(R.id.unreadBtn);
        UnreadFeedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SharedPreferences.Editor editor = pref.edit();
                Animation animFadein = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
                UnreadFeedBtn.startAnimation(animFadein);
                Intent intent = null;
                intent = new Intent(getBaseContext(), HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_right);
                editor.putBoolean("Deeplink", false);
                editor.apply();
            }
        });


        //Auto open drawer if there are unread feeds
        String unreadFds = pref.getString("UnreadFeedsCount", "0");
        int UnreadFds = Integer.parseInt(unreadFds);
            if (UnreadFds >=1){
                if (drawerLayout != null) {
                    drawerLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drawerLayout.openDrawer(GravityCompat.START);
                        }

                    }, 1500);
                }

            }

    }




    public void unread_feeds(View view) {
        startActivity(new Intent(MainActivity.this, HomeActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_edit:
                Intent intent5 = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent5);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_right);                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupNavigationDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                    final SharedPreferences.Editor editor = pref.edit();
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.Home:
                                menuItem.setChecked(true);
                                setFragment(0);
                                drawerLayout.closeDrawer(GravityCompat.START);
                                return true;
                            case R.id.Article_list:
                                menuItem.setChecked(true);
                                drawerLayout.closeDrawer(GravityCompat.START);
                                Intent intent = new Intent(MainActivity.this, List_of_Articles.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_right);
                                return true;
                            case R.id.Rymites_gallery:
                                menuItem.setChecked(true);
                                drawerLayout.closeDrawer(GravityCompat.START);
                                Intent intent4 = new Intent(MainActivity.this, CurlActivity.class);
                                startActivity(intent4);
                                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_right);
                                return true;
                            case R.id.About:
                                drawerLayout.closeDrawer(GravityCompat.START);
                                Intent intent5 = new Intent(MainActivity.this, AboutActivity.class);
                                startActivity(intent5);
                                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_right);
                                return true;
                            case R.id.News_feed:
                                menuItem.setChecked(true);
                                drawerLayout.closeDrawer(GravityCompat.START);
                                Intent intent2 = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent2);
                                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_right);
                                editor.putBoolean("Deeplink", false);
                                editor.apply();
                                return true;
                            case R.id.Website:
                                menuItem.setChecked(true);
                                drawerLayout.closeDrawer(GravityCompat.START);
                                Intent intent3 = new Intent(MainActivity.this, Website.class);
                                startActivity(intent3);
                                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_right);
                                Toast.makeText(MainActivity.this, "Launching " + menuItem.getTitle().toString(), Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.Online_chat:
                                menuItem.setChecked(true);
                                drawerLayout.closeDrawer(GravityCompat.START);
                                Intent intent6 = new Intent(MainActivity.this, Login.class);
                                startActivity(intent6);
                                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_right);
                                return true;
                            case R.id.Send_report:
                                menuItem.setChecked(true);
                                setFragment(1);
                                Toast.makeText(MainActivity.this, menuItem.getTitle().toString(), Toast.LENGTH_SHORT).show();
                                drawerLayout.closeDrawer(GravityCompat.START);
                                return true;
                        }
                        return true;
                    }
                });
    }

    public void setFragment(int position) {
        FragmentManager fragmentManager;
        FragmentTransaction fragmentTransaction;
        switch (position) {
            case 0:
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                CoverPage coverPage = new CoverPage();
                fragmentTransaction.replace(R.id.fragment, coverPage);
                fragmentTransaction.commit();
                break;
            case 1:
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                About about = new About();
                fragmentTransaction.replace(R.id.fragment, about);
                fragmentTransaction.commit();
                break;
        }
    }


    // First open => we open the drawer for you
    private void welcomeDialog() {
        final SharedPreferences pref2 = getApplicationContext().getSharedPreferences("firstUser", MODE_PRIVATE);
        final SharedPreferences.Editor editor2 = pref2.edit();
        boolean firstUser = pref2.getBoolean("newUser",true);
        if (firstUser) {
            editor2.putBoolean("newUser",false);
            editor2.apply();
            //Automatically Add 1 News Feed for first user so they can start getting updates
            for (int topic = 0; topic < TOPIC_NAME.length; topic++) {
                String url;
                if (TOPIC_CODES[topic].equals("x")) {
                    url = "http://punchng.com/feed/";
                    FeedDataContentProvider.addFeed(this, url, getString(TOPIC_NAME[topic]), true);
                }
            }
            //Open drawer automatically and welcome message
            if (drawerLayout != null) {
                drawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawerLayout.openDrawer(GravityCompat.START);

                        final Dialog w = new Dialog(MainActivity.this, R.style.customDialog);
                        w.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        w.setCancelable(false);
                        w.setContentView(R.layout.welcome_dialog);

                        final TextView exit = (TextView) w.findViewById(R.id.exit);
                        exit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Animation zoom_out = AnimationUtils.loadAnimation(MainActivity.this, R.anim.zoom_out);
                                exit.startAnimation(zoom_out);
                                w.dismiss();
                            }
                        });
                        //SHOW
                        w.show();
                    }

                }, 1500);
            }
        }
    }

    public void onBackPressed() {
        // Before exiting from app the navigation drawer is opened
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        if (mCanQuit) {
            super.finish();
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
            return;
        }

        Toast.makeText(this, R.string.back_again_to_quit, Toast.LENGTH_SHORT).show();
        mCanQuit = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mCanQuit = false;
            }
        }, 3000);
    }
}

