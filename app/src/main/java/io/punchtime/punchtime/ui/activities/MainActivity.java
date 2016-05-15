package io.punchtime.punchtime.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.preference.PreferenceManager;
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

import java.util.HashMap;
import java.util.Map;

import io.punchtime.punchtime.R;
import io.punchtime.punchtime.logic.tasks.DownloadImageTask;
import io.punchtime.punchtime.ui.fragments.DashboardFragment;
import io.punchtime.punchtime.ui.fragments.HistoryFragment;
import io.punchtime.punchtime.ui.fragments.SettingsFragment;
import io.punchtime.punchtime.ui.fragments.StatsFragment;

public class MainActivity extends FirebaseLoginBaseActivity {

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private AppBarLayout appBar;
    private Firebase mRef;
    private View headerView;
    private SharedPreferences preferences;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Handle punchime intents
        Intent intent = getIntent();

        if (intent != null) {
            String action = intent.getAction();
            if (action != null && action.equals("android.intent.action.VIEW")) {
                Uri data = intent.getData();
                if (data.getHost().equals("invite")) {
                    // open settings
                    setFragment(new SettingsFragment());
                    // open the invitation field
                    // fill in invitation id
                    Log.d("data",data.getLastPathSegment().toString());
                }
            }
        }



        // find our drawer layout view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        appBar = (AppBarLayout) findViewById(R.id.appBar);

        drawerToggle = setupDrawerToggle();
        mDrawer.addDrawerListener(drawerToggle);

        // find our drawer view
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        headerView = (navigationView == null) ? null : navigationView.getHeaderView(0);
        setupDrawerContent(navigationView);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // connect to firebase
        mRef = new Firebase( getString(R.string.firebase_url));

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
                setFragment(new SettingsFragment());
                navigationView.setCheckedItem(R.id.nav_settings);
                mDrawer.closeDrawer(GravityCompat.START);
            }
        });

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Intent permissionIntent = new Intent(this, PermissionErrorActivity.class);
            startActivity(permissionIntent);
        }
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
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
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
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    public boolean selectDrawerItem(MenuItem item) {
        // Handle navigation view item clicks here.
        Fragment fragment;
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
            case R.id.nav_stats:
                fragment = new StatsFragment();
                break;
            case R.id.nav_settings:
                fragment = new SettingsFragment();
                break;
            default:
                fragment = new DashboardFragment();
                break;
        }

        setFragment(fragment);

        // close drawer
        mDrawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void setFragment(Fragment fragment) {
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
    public void onFirebaseLoggedIn(final AuthData authData) {
        setFragment(new DashboardFragment());
        navigationView.setCheckedItem(R.id.nav_dashboard);

        // Store user data in firebase
        Map<String, Object> map = new HashMap<>();
        map.put("provider", authData.getProvider());
        if(authData.getProviderData().containsKey("displayName")) {
            map.put("name", authData.getProviderData().get("displayName").toString());
        }

        TextView mail = (TextView) headerView.findViewById(R.id.userMail);
        TextView name = (TextView) headerView.findViewById(R.id.userName);
        ImageView pic = (ImageView) headerView.findViewById(R.id.imageView);

        switch (authData.getProvider()) {
            case "facebook":
                map.put("image","https://graph.facebook.com/"+authData.getProviderData().get("id")+"/picture?height=300");
                name.setText(authData.getProviderData().get("displayName").toString());
                mail.setText(R.string.logged_in_facebook);
                break;
            case "twitter":
                map.put("image","https://twitter.com/"+authData.getProviderData().get("username")+"/profile_image?size=original");
                name.setText(authData.getProviderData().get("displayName").toString());
                mail.setText(R.string.logged_in_twitter);
                break;
            case "google":
                map.put("image", authData.getProviderData().get("profileImageURL").toString());
                name.setText(authData.getProviderData().get("displayName").toString());
                mail.setText(authData.getProviderData().get("email").toString());
                break;
            case "password":
                map.put("image", authData.getProviderData().get("profileImageURL").toString());
                name.setText(authData.getProviderData().get("email").toString());
                mail.setText("");
                break;
            default:
                map.put("image","https://www.drupal.org/files/profile_default.png"); // TODO: 28/04/16 give a real url
                break;
        }

        mRef.child("users").child(authData.getUid()).updateChildren(map);

        preferences.edit().putBoolean("logged_in", true).apply();

        new DownloadImageTask(pic).execute(map.get("image").toString());
    }

    @Override
    public void onFirebaseLoggedOut() {
        ((TextView) headerView.findViewById(R.id.userName)).setText(R.string.placeholder_user);
        ((TextView) headerView.findViewById(R.id.userMail)).setText(R.string.placeholder_email);
        ((ImageView) headerView.findViewById(R.id.imageView)).setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.sym_def_app_icon));

        preferences.edit().putBoolean("logged_in", false).apply();
    }
    @Override
    public Firebase getFirebaseRef() {
        return mRef;
    }

    @Override
    protected void onFirebaseLoginProviderError(FirebaseLoginError firebaseLoginError) {
        Toast.makeText(MainActivity.this, firebaseLoginError.message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onFirebaseLoginUserError(FirebaseLoginError firebaseLoginError) {
        Toast.makeText(MainActivity.this, firebaseLoginError.message, Toast.LENGTH_LONG).show();
    }

    public NavigationView getNavigationView() {
        return navigationView;
    }
}
