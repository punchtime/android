package io.punchtime.punchtime.ui.activities;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.ui.auth.core.AuthProviderType;
import com.firebase.ui.auth.core.FirebaseLoginBaseActivity;
import com.firebase.ui.auth.core.FirebaseLoginError;

import io.punchtime.punchtime.DownloadImageTask;
import io.punchtime.punchtime.R;
import io.punchtime.punchtime.ui.fragments.DashboardFragment;
import io.punchtime.punchtime.ui.fragments.HistoryFragment;
import io.punchtime.punchtime.ui.fragments.SettingsFragment;

public class MainActivity extends FirebaseLoginBaseActivity {

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private AppBarLayout appBar;
    private Firebase mRef;
    private View headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find our drawer layout view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        appBar = (AppBarLayout) findViewById(R.id.appBar);

        drawerToggle = setupDrawerToggle();
        mDrawer.addDrawerListener(drawerToggle);

        // find our drawer view
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        headerView = navigationView.getHeaderView(0);
        setupDrawerContent(navigationView);

        Firebase.setAndroidContext(this);

        // connect to firebase
        mRef = new Firebase( getString(R.string.firebase_url) + "/pulses");

        // set default view as dashboard
        if (savedInstanceState == null) {
            Fragment fragment = null;
            try {
                fragment = DashboardFragment.class.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            setFragment(fragment);
        }

        View navHeader = headerView.findViewById(R.id.nav_header);
            navHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "header clicked", Toast.LENGTH_LONG).show();
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.logout_dialog_title))
                        .setMessage(getString(R.string.logout_dialog_message))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                    logout();
                                    mDrawer.closeDrawer(GravityCompat.START);
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // All providers are optional! Remove any you don't want.
        setEnabledAuthProvider(AuthProviderType.FACEBOOK);
        setEnabledAuthProvider(AuthProviderType.TWITTER);
        setEnabledAuthProvider(AuthProviderType.GOOGLE);
        setEnabledAuthProvider(AuthProviderType.PASSWORD);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void addViewToToolbar(View v) {
        toolbar.addView(v);
    }

    public void removeViewFromToolbar(View v) {
        toolbar.removeView(v);
    }

    public void addViewToAppBarLayout(View v) {
        appBar.addView(v);
    }

    public void removeViewFromAppBarLayout(View v) {
        appBar.removeView(v);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // open or close the drawer
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean selectDrawerItem(MenuItem item) {
        // Handle navigation view item clicks here.
        Fragment fragment = null;
        Bundle args = new Bundle();
        switch (item.getItemId()) {
            case R.id.nav_dashboard:
                fragment = new DashboardFragment();
                break;
            case R.id.nav_day:
                fragment = new HistoryFragment();
                break;
            case R.id.nav_3days:
                fragment = new HistoryFragment();
                args.putInt("fragment",R.id.nav_3days);
                fragment.setArguments(args);
                break;
            case R.id.nav_week:
                fragment = new HistoryFragment();
                args.putInt("fragment",R.id.nav_week);
                fragment.setArguments(args);
                break;
            case R.id.nav_month:
                fragment = new HistoryFragment();
                args.putInt("fragment",R.id.nav_month);
                fragment.setArguments(args);
                break;
            case R.id.nav_settings:
                fragment = new SettingsFragment();
                break;
            default:
                fragment = new DashboardFragment();
        }

        setFragment(fragment);

        // close drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
    }

    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE! Make sure to override the method with only a single `Bundle` argument
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onFirebaseLoggedIn(AuthData authData) {
        TextView mail = (TextView) headerView.findViewById(R.id.userMail);
        TextView name = (TextView) headerView.findViewById(R.id.userName);
        ImageView pic = (ImageView) headerView.findViewById(R.id.imageView);

        if (authData.getProvider().equals("password")) {
            name.setText(authData.getProviderData().get("email").toString());
            mail.setText("");
        }
        else if (authData.getProvider().equals("facebook")) {
            name.setText(authData.getProviderData().get("displayName").toString());
            mail.setText("Logged in with Facebook");
        }
        else if (authData.getProvider().equals("twitter")) {
            name.setText(authData.getProviderData().get("displayName").toString());
            mail.setText("Logged in with Twitter");
        }
        else {
            name.setText(authData.getProviderData().get("displayName").toString());
            mail.setText(authData.getProviderData().get("email").toString());
        }

        new DownloadImageTask(pic)
                .execute(authData.getProviderData().get("profileImageURL").toString());
    }

    @Override
    public void onFirebaseLoggedOut() {
        showFirebaseLoginPrompt();
    }
    @Override
    public Firebase getFirebaseRef() {
        return mRef;
    }

    @Override
    protected void onFirebaseLoginProviderError(FirebaseLoginError firebaseLoginError) {
        Log.d("blah", firebaseLoginError.message);
        Toast.makeText(MainActivity.this, firebaseLoginError.message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onFirebaseLoginUserError(FirebaseLoginError firebaseLoginError) {
        Log.d("blah", firebaseLoginError.message);
        Toast.makeText(MainActivity.this, firebaseLoginError.message, Toast.LENGTH_LONG).show();
    }
}