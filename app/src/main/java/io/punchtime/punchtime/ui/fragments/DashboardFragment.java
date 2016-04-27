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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.punchtime.punchtime.R;
import io.punchtime.punchtime.data.Pulse;
import io.punchtime.punchtime.ui.SnackbarFactory;
import io.punchtime.punchtime.ui.activities.MainActivity;
import io.punchtime.punchtime.ui.activities.MapDetailActivity;

/**
 * Created by Arnaud on 3/23/2016.
 */
public class DashboardFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        CompoundButton.OnCheckedChangeListener,
        LocationListener {
    private GoogleApiClient mGoogleApiClient;
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private Location mLastLocation;
    private boolean connectedGoogleApi;
    private static Context context;
    private RecyclerView pulsesList;
    private MainActivity activity;
    private Toolbar toolbar;
    private MaterialAnimatedSwitch mSwitch;
    private Firebase mRef;
    private FloatingActionButton fab;
    private Button mapButton;
    private Button noteButton;
    private SharedPreferences preferences;
    private boolean isCheckedIn;
    private View v;
    private Geocoder geocoder;
    private Pulse lastPulse;

    public DashboardFragment() {
        Bundle args = new Bundle();
        setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, final Bundle savedInstanceState) {
        context = getContext();

        // Defines the xml file for the fragment
        v = inflater.inflate(R.layout.fragment_dashboard, parent, false);

        activity = (MainActivity) getActivity();
        activity.setTitle(R.string.menu_dashboard);
        toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        mSwitch = (MaterialAnimatedSwitch) LayoutInflater.from(activity).inflate(R.layout.toolbar_switch, toolbar, false);
        activity.addViewToToolbar(mSwitch);

        mSwitch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean b) {
                setTrackingLocationMode(b);
            }
        });

//        pulsesList =  (RecyclerView) v.findViewById(R.id.pulsesList);
//        pulsesList.setLayoutManager(new LinearLayoutManager(getContext()));

        // get map fragment
        mMapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        mMapFragment.getMapAsync(this);

        fab = (FloatingActionButton) v.findViewById(R.id.fab);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        geocoder = new Geocoder(getContext(), Locale.getDefault());

        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (preferences.getBoolean("tracking_location_mode", false)) {
                        setTrackingLocation(!preferences.getBoolean("tracking_location", false));
                    } else {
                        if (ContextCompat.checkSelfPermission(activity,
                                Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED && preferences.getBoolean("logged_in", false)) {
                            if (mLastLocation != null) {
                                if(isCheckedIn) checkOut();
                                else checkIn();
                            } else {
                                SnackbarFactory.createSnackbar(getContext(), view, "Could not retrieve location").show();
                            }
                        } else {
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
                        }
                    }
                }
            });
        }

        noteButton = (Button) v.findViewById(R.id.noteButton);
        noteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder inputAlert = new AlertDialog.Builder(context);
                inputAlert.setTitle("Add a note");
                inputAlert.setMessage("write a note for your current checkin");
                final EditText userInput = new EditText(context);
                inputAlert.setView(userInput);
                inputAlert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("note", userInput.getText().toString());
                        new UpdateLastPulseTask().execute(map);
                    }
                });
                inputAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = inputAlert.create();
                alertDialog.show();

            }
        });

        mapButton = (Button) v.findViewById(R.id.showMapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, MapDetailActivity.class);
                intent.putExtra("location", mLastLocation);
                startActivity(intent);
            }
        });

        return v;
    }

    private void checkIn() {
        setCheckedIn(true);
        SnackbarFactory.createSnackbar(activity, v, "Checked in at current location").show();

        Pulse pulse = new Pulse(mLastLocation.getLatitude(), mLastLocation.getLongitude(), "", System.currentTimeMillis(), activity.getAuth().getUid(), "-KBdSPf90dvJCeH3J8m7", true);
        setLastPulse(pulse);

        Firebase fb = mRef.child("pulses").push();
        fb.setValue(pulse);

        Map<String, Object> map = new HashMap<>();
        map.put(fb.getKey(),true);

        mRef.child("users").child(activity.getAuth().getUid()).child("pulses").updateChildren(map);
    }

    private void checkOut() {
        setCheckedIn(false);
        SnackbarFactory.createSnackbar(activity, v, "Checked out at current location").show();

        Map<String, Object> map = new HashMap<>();
        map.put("checkout", System.currentTimeMillis());
        new UpdateLastPulseTask().execute(map);
    }

    private void updateCard(Address address) {
        TextView street = (TextView) v.findViewById(R.id.streetText);
        TextView city = (TextView) v.findViewById(R.id.cityText);

        street.setText(address.getAddressLine(0));
        city.setText(address.getAddressLine(1) + ", " + address.getAddressLine(2));
    }


    // triggered soon after onCreateView
    // Any view setup should occur here.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        if(buttonView.getId() == R.id.pin) {
//            // handle switch
//        }
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

        getLocation();
        onLocationChanged(mLastLocation);
    }

    public void onStart() {
        mGoogleApiClient.connect();
        getLocation();
        super.onStart();

        mRef = activity.getFirebaseRef();

        lastPulse = new Pulse();

        if(!preferences.getBoolean("logged_in",false)) {
            ((CardView) getView().findViewById(R.id.cardView)).setVisibility(View.INVISIBLE);
        } else {
            Query query = mRef.child("pulses").orderByChild("employee").equalTo(mRef.getAuth().getUid());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    new LastPulseTask().execute(dataSnapshot);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {}
            });
        }

        boolean trackingLocationMode = preferences.getBoolean("tracking_location_mode", false);
        setTrackingLocationMode(trackingLocationMode);
    }

    public void onStop() {
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        this.connectedGoogleApi = true;
        getLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        this.connectedGoogleApi = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        this.connectedGoogleApi = false;
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
    }



    public void setTrackingLocationMode(boolean b) {
        preferences.edit().putBoolean("tracking_location_mode", b).apply();

        if (b) {
            setTrackingLocation(false);
        } else {
            if(isCheckedIn) {
                fab.setImageResource(R.drawable.ic_location_off_black_24dp);
            } else {
                fab.setImageResource(R.drawable.ic_pin_drop_24dp);
            }
            setTrackingLocation(false);
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

    public void setCheckedIn(boolean checkedIn) {
        isCheckedIn = checkedIn;
        if(isCheckedIn) {
            fab.setImageResource(R.drawable.ic_location_off_black_24dp);
            mapButton.setVisibility(View.VISIBLE);
            noteButton.setVisibility(View.VISIBLE);

            new ReverseGeocodingTask().execute(new LatLng(lastPulse.getLatitude(),lastPulse.getLongitude()));
        } else {
            fab.setImageResource(R.drawable.ic_pin_drop_24dp);
            mapButton.setVisibility(View.INVISIBLE);
            noteButton.setVisibility(View.INVISIBLE);
            TextView street = (TextView) v.findViewById(R.id.streetText);
            TextView city = (TextView) v.findViewById(R.id.cityText);

            street.setText(R.string.placeholder_street);
            city.setText(R.string.placeholder_city);
        }
    }

    protected void startLocationUpdates() {
        if(ContextCompat.checkSelfPermission(activity,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, LocationRequest.create().setInterval(1000).setFastestInterval(100), this);
        }
    }

    protected void stopLocationUpdates() {
        if(mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if(location != null) {
            LatLng locationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 18));

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
        }
    }

    public void setLastPulse(Pulse lastPulse) {
        this.lastPulse = lastPulse;
        if(isCheckedIn) new ReverseGeocodingTask().execute(new LatLng(lastPulse.getLatitude(),lastPulse.getLongitude()));
    }

    private class ReverseGeocodingTask extends AsyncTask<LatLng, Address, Address> {
        Address address;
        @Override
        protected Address doInBackground(LatLng... params) {

            LatLng loc = params[0];
            try {
                // Call the synchronous getFromLocation() method by passing in the lat/long values.
                address = geocoder.getFromLocation(loc.latitude, loc.longitude, 1).get(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return address;
        }

        @Override
        protected void onPostExecute(Address address) {
            super.onPostExecute(address);
            if(address != null) updateCard(address);
        }
    }

    private class LastPulseTask extends AsyncTask<DataSnapshot, Pulse, Pulse> {
        @Override
        protected Pulse doInBackground(DataSnapshot... params) {
            DataSnapshot snap = params[0];
            if(snap.getChildrenCount() == 0) return null;

            Pulse p;
            Pulse latestPulse = new Pulse();
            long latestTimestamp, currentTimestamp;
            latestTimestamp = 0;

            for (DataSnapshot child : snap.getChildren()) {
                p = child.getValue(Pulse.class);
                currentTimestamp = p.getCheckin();
                if(currentTimestamp > latestTimestamp) {
                    latestTimestamp = currentTimestamp;
                    latestPulse = p;
                }
            }

            return latestPulse;
        }

        @Override
        protected void onPostExecute(Pulse p) {
            super.onPostExecute(p);
            if(p != null) {
                setLastPulse(p);
                if(p.getCheckout() == 0) setCheckedIn(true);
                else setCheckedIn(false);
            }  else {
                setCheckedIn(false);
            }
        }
    }

    private class UpdateLastPulseTask extends AsyncTask<Map<String,Object>, Pulse, Pulse> {
        Map<String, Object> map;

        @Override
        protected Pulse doInBackground(Map<String,Object>... params) {
            map = params[0];
            Query query = mRef.child("pulses").orderByChild("employee").equalTo(mRef.getAuth().getUid());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getChildrenCount() != 0) {
                        Pulse p;
                        long latestTimestamp, currentTimestamp;
                        latestTimestamp = 0;
                        String lastPulseKey = "";

                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            p = child.getValue(Pulse.class);

                            currentTimestamp = p.getCheckin();
                            if (currentTimestamp > latestTimestamp) {
                                latestTimestamp = currentTimestamp;
                                lastPulseKey = child.getKey();
                            }
                        }

                        mRef.child("pulses").child(lastPulseKey).updateChildren(map);
                    }
                }
                @Override
                public void onCancelled(FirebaseError firebaseError) {}
            });
            return null;
        }
    }
}
