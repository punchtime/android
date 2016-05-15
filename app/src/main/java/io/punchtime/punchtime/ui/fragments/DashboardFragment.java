package io.punchtime.punchtime.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.punchtime.punchtime.R;
import io.punchtime.punchtime.data.Pulse;
import io.punchtime.punchtime.logic.operations.PulseOperations;
import io.punchtime.punchtime.logic.loaders.LocationLoader;
import io.punchtime.punchtime.logic.loaders.TimerLoader;
import io.punchtime.punchtime.ui.SnackbarFactory;
import io.punchtime.punchtime.ui.activities.MainActivity;
import io.punchtime.punchtime.ui.activities.MapDetailActivity;

/**
 * Created by Arnaud on 3/23/2016.
 * for project: Punchtime
 */
public class DashboardFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Location mLastLocation;
    private static Context context;
    private MainActivity activity;
    private MaterialAnimatedSwitch mSwitch;
    private Firebase mRef;
    private FloatingActionButton fab;
    private Button mapButton;
    private Button noteButton;
    private SharedPreferences preferences;
    private boolean isCheckedIn;
    private View v;
    private Geocoder geocoder;
    private TextView street;
    private TextView city;
    private TextView timeCheckin;
    private Pulse mLastPulse;
    PulseOperations operations;

    public DashboardFragment() {
        Bundle args = new Bundle();
        setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, final Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        v = inflater.inflate(R.layout.fragment_dashboard, parent, false);

        // Store context
        context = getContext();

        // Store calling activity (Always MainActivity)
        activity = (MainActivity) getActivity();

        getLoaderManager().initLoader(0, null, locationLoaderCallbacks);
        getLoaderManager().initLoader(1, null, timerLoaderCallbacks);

        // Setup toolbar
        activity.setTitle(R.string.menu_dashboard);
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        mSwitch = (MaterialAnimatedSwitch) LayoutInflater.from(activity).inflate(R.layout.toolbar_switch, toolbar, false);

        activity.addViewToToolbar(mSwitch);

        street = (TextView) v.findViewById(R.id.streetText);
        city = (TextView) v.findViewById(R.id.cityText);
        timeCheckin = (TextView) v.findViewById(R.id.timeHere);

        // Load the map async
        SupportMapFragment mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        // Get preferences from storage
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        // Setup geocoder
        geocoder = new Geocoder(getContext(), Locale.getDefault());

        // Setup switch in toolbar
        mSwitch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean b) {
                setTrackingLocationMode(b);
            }
        });

        // Setup FAB action
        fab = (FloatingActionButton) v.findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED && preferences.getBoolean("logged_in", false)) {
                        if (preferences.getBoolean("tracking_location_mode", false)) {
                            // Tracking location mode, toggle play/stop
                            setTrackingLocation(!preferences.getBoolean("tracking_location", false));
                        } else {
                            // Manual checkin mode
                            if (mLastLocation != null) {
                                // Location found
                                if(isCheckedIn) checkOut();
                                else checkIn();
                            } else {
                                // No location found yet
                                SnackbarFactory.createSnackbar(getContext(), view, activity.getString(R.string.no_location_error_message)).show();
                            }
                        }
                    }
                }
            });
        }

        // Setup "Add a note" button action
        noteButton = (Button) v.findViewById(R.id.noteButton);
        noteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder inputAlert = new AlertDialog.Builder(context);

                inputAlert.setMessage(getString(R.string.write_current_note_message));
                final EditText userInput = new EditText(context);
                inputAlert.setView(userInput);

                String title;
                if (mLastPulse.getNote() == null || mLastPulse.getNote().isEmpty()) {
                    title = getString(R.string.add_a_note);
                } else {
                    title = getString(R.string.edit_note);
                    userInput.setText(mLastPulse.getNote());
                }
                inputAlert.setTitle(title);

                inputAlert.setPositiveButton(getString(R.string.submit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("note", userInput.getText().toString());
                        mLastPulse.setNote(userInput.getText().toString());
                        new UpdateLastPulseTask().execute(map);
                        noteButton.setText(getText(R.string.edit_note));
                    }
                });
                inputAlert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = inputAlert.create();
                alertDialog.show();
            }
        });

        // Setup "Show on map" button action
        mapButton = (Button) v.findViewById(R.id.showMapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, MapDetailActivity.class);
                intent.putExtra("location", mLastLocation);
                startActivity(intent);
            }
        });

        setTrackingLocationMode(preferences.getBoolean("tracking_location_mode", false));

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        mRef = activity.getFirebaseRef();

        // create PulseOperations instance
        operations = new PulseOperations(mRef);

        if(!preferences.getBoolean("logged_in",false)) {
            // If the user is not logged in, show login rationale and prompt to login
            v.findViewById(R.id.cardView).setVisibility(View.INVISIBLE);
            Snackbar snackbar = Snackbar.make(v, R.string.login_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.settings, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            activity.setFragment(new SettingsFragment());
                        }
                    });
            View snackbarView = snackbar.getView();
            TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setMaxLines(4);
            snackbar.show();
        } else {
            new GetLastPulseTask().execute();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onDestroyView() {
        FragmentManager fm = getFragmentManager();

        Fragment xmlFragment = fm.findFragmentById(R.id.map);
        if (xmlFragment != null) {
            fm.beginTransaction().remove(xmlFragment).commit();
        }
        activity.removeViewFromToolbar(mSwitch);

        super.onDestroyView();
    }

    private void checkIn() {
        SnackbarFactory.createSnackbar(activity, v, getString(R.string.checkin_message)).show();

        // Create pulse without geocoding
        Pulse pulse = new Pulse(mLastLocation.getLatitude(), mLastLocation.getLongitude(), "", System.currentTimeMillis(), activity.getAuth().getUid(), "-KBdSPf90dvJCeH3J8m7", true);

        mLastPulse = pulse;

        setCheckedIn(true);

        // Add geocoding to pulse, will also update UI and push to Firebase
        new ReverseGeocodePulseTask().execute(pulse);
    }

    private void checkOut() {
        setCheckedIn(false);
        SnackbarFactory.createSnackbar(activity, v, getString(R.string.checkout_message)).show();

        // Update UI
        updateCheckinUI(null);

        // Update last pulse with checkout timestamp
        Map<String, Object> map = new HashMap<>();
        map.put("checkout", System.currentTimeMillis());
        new UpdateLastPulseTask().execute(map);
    }

    public void updateCheckinUI(Pulse pulse) {
        if (pulse != null && pulse.getCheckout() == 0) {
            // Checked in
            street.setText(pulse.getAddressStreet());
            city.setText(pulse.getAddressCityCountry());
            mapButton.setVisibility(View.VISIBLE);
            if(pulse.getNote() != null && !pulse.getNote().isEmpty()) noteButton.setText(getString(R.string.edit_note));
            noteButton.setVisibility(View.VISIBLE);
            timeCheckin.setText(DateUtils.getRelativeTimeSpanString(context, mLastPulse.getCheckin()) + " – now");
        } else {
            // Not checked in
            street.setText(R.string.placeholder_street);
            city.setText(R.string.placeholder_city);
            mapButton.setVisibility(View.INVISIBLE);
            noteButton.setVisibility(View.INVISIBLE);
            timeCheckin.setText("");
        }
    }

    public void setCheckedIn(boolean checkedIn) {
        fab.show();
        isCheckedIn = checkedIn;

        if(!preferences.getBoolean("tracking_location_mode", false)) {
            if(checkedIn) {
                fab.setImageResource(R.drawable.ic_location_off_black_24dp);
            } else {
                fab.setImageResource(R.drawable.ic_pin_drop_24dp);
            }
        }
    }

    public void setTrackingLocationMode(boolean trackingLocationMode) {
        preferences.edit().putBoolean("tracking_location_mode", trackingLocationMode).apply();
        setTrackingLocation(false);

        if (!trackingLocationMode) {
            mSwitch.setOnInitFinishedListener(null);
            if(isCheckedIn) {
                fab.setImageResource(R.drawable.ic_location_off_black_24dp);
            } else {
                fab.setImageResource(R.drawable.ic_pin_drop_24dp);
            }
        } else {
            mSwitch.post(new Runnable() {
                @Override
                public void run() {
                    if (!mSwitch.isChecked()) {
                        mSwitch.toggle();
                    }
                }
            });
        }
    }

    public void setTrackingLocation(boolean trackingLocation) {
        preferences.edit().putBoolean("tracking_location", trackingLocation).apply();

        if (preferences.getBoolean("tracking_location_mode", false)) {
            if(trackingLocation) {
                fab.setImageResource(R.drawable.ic_stop_black);
            } else {
                fab.setImageResource(R.drawable.ic_play_arrow_black);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        UiSettings settings = map.getUiSettings();
        settings.setMapToolbarEnabled(false);
        settings.setScrollGesturesEnabled(false);
        this.mMap = map;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Intent intent = new Intent(activity, MapDetailActivity.class);
                intent.putExtra("location", mLastLocation);
                startActivity(intent);
            }
        });
    }

    // Callbacks for LocationLoader
    private LoaderManager.LoaderCallbacks<Location> locationLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<Location>() {
                @Override
                public Loader<Location> onCreateLoader(
                        int id, Bundle args) {
                    return new LocationLoader(context);
                }
                @Override
                public void onLoadFinished(Loader<Location> loader, Location location) {
                    // Update mLastLocation
                    mLastLocation = location;
                    if(location != null && mMap != null) {
                        LatLng locationLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                        // Draw location and accuracy on map
                        mMap.clear();
                        mMap.addCircle(new CircleOptions()
                                .center(locationLatLng)
                                .radius(2.5)
                                .strokeColor(Color.WHITE)
                                .strokeWidth(2)
                                .fillColor(0xFF1DE9B6))
                                .setZIndex(1);

                        mMap.addCircle(new CircleOptions()
                                .center(locationLatLng)
                                .radius(location.getAccuracy())
                                .strokeColor(0xFF282F3F)
                                .strokeWidth(1)
                                .fillColor(0x663B4358));

                        // Move camera to new location
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 18));
                    }
                }
                @Override
                public void onLoaderReset(Loader<Location> loader) {}
            };

    // Callbacks for TimerLoader
    private LoaderManager.LoaderCallbacks<Void> timerLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<Void>() {
                @Override
                public Loader<Void> onCreateLoader(
                        int id, Bundle args) {
                    return new TimerLoader(context);
                }
                @Override
                public void onLoadFinished(Loader<Void> loader, Void mNull) {
                    // Fires every minute
                }

                @Override
                public void onLoaderReset(Loader<Void> loader) {}
            };


    private class ReverseGeocodePulseTask extends AsyncTask<Pulse, Pulse, Pulse> {
        @Override
        protected Pulse doInBackground(Pulse... params) {
            Pulse p = params[0];
            try {
                Address address = geocoder.getFromLocation(p.getLatitude(), p.getLongitude(), 1).get(0);
                p.setAddressStreet(address.getAddressLine(0));
                p.setAddressCityCountry(address.getAddressLine(1) + ", " + address.getAddressLine(2));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return p;
        }

        @Override
        protected void onPostExecute(Pulse pulse) {
            super.onPostExecute(pulse);

            mLastPulse = pulse;

            // Update UI
            updateCheckinUI(pulse);

            // Push to firebase
            new PushPulseToFirebaseTask().execute(pulse);
        }
    }

    private class PushPulseToFirebaseTask extends AsyncTask<Pulse, Void, Void> {
        @Override
        protected Void doInBackground(Pulse... params) {
            Pulse p = params[0];
            // Push the new pulse to firebase
            Firebase fb = mRef.child("users/" + mRef.getAuth().getUid()).child("pulses").push();
            fb.setValue(p);
            return null;
        }
    }

    private class GetLastPulseTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mRef.child("users").child(mRef.getAuth().getUid()).child("pulses").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Pulse latestPulse = dataSnapshot.getChildren().iterator().next().getValue(Pulse.class);
                        mLastPulse = latestPulse;
                        updateCheckinUI(latestPulse);
                        if(latestPulse.getCheckout() == 0) setCheckedIn(true);
                        else setCheckedIn(false);
                    } catch (Exception e) {
                        updateCheckinUI(null);
                        setCheckedIn(false);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
            return null;
        }
    }

    private class UpdateLastPulseTask extends AsyncTask<Map<String,Object>, Void, Void> {
        private Map<String, Object> map;

        @Override
        @SafeVarargs
        protected final Void doInBackground(Map<String,Object>... params) {
            map = params[0];
            Query query = mRef.child("users").child(mRef.getAuth().getUid()).child("pulses").limitToLast(1);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mRef.child("users").child(mRef.getAuth().getUid()).child("pulses").child(dataSnapshot.getChildren().iterator().next().getKey()).updateChildren(map);
                }
                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    // We allow cancelling and then weep
                    // TODO: maybe try calling it again?
                }
            });
            return null;
        }
    }
}
