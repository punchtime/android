package io.punchtime.punchtime.ui.fragments;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;
import java.util.Locale;
import java.util.TooManyListenersException;

import io.punchtime.punchtime.R;
import io.punchtime.punchtime.data.Pulse;
import io.punchtime.punchtime.ui.SnackbarFactory;
import io.punchtime.punchtime.ui.activities.MainActivity;

/**
 * Created by Arnaud on 3/23/2016.
 */
public class DashboardFragment extends Fragment  implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        CompoundButton.OnCheckedChangeListener {
    private static View view;
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
    private boolean trackingLocationMode;
    private boolean trackingLocation;
    private Firebase mRef;
    private FloatingActionButton fab;

    public DashboardFragment() {
        Bundle args = new Bundle();
        setArguments(args);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, final Bundle savedInstanceState) {
        context = getContext();

        // Defines the xml file for the fragment
        final View v = inflater.inflate(R.layout.fragment_dashboard, parent, false);

        activity = (MainActivity) getActivity();
        activity.setTitle(R.string.main_activity_title);
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
        mMapFragment =  (SupportMapFragment)getChildFragmentManager()
                .findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        fab = (FloatingActionButton) v.findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (trackingLocationMode) {
                        setTrackingLocation(!trackingLocation);
                    } else {
                        getLocation();
                        if (mLastLocation != null) {
                            SnackbarFactory.createSnackbar(getContext(), view, "Location is " + mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude()).show();
                            // gets current location, adds a marker, and changes the camera
                            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                            mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title("Marker"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                            Pulse pulse = new Pulse(latLng.latitude, latLng.longitude, "some note", System.currentTimeMillis(), "google:116529723379255029542", "-KBdSPf90dvJCeH3J8m7", true);
                            mRef.push().setValue(pulse);
                        } else {
                            SnackbarFactory.createSnackbar(getContext(), view, "Could not retrieve location").show();
                        }
                    }
                }
            });
        }
        setTrackingLocationMode(false);
        return v;
    }



    // triggered soon after onCreateView
    // Any view setup should occur here.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects
        mGoogleApiClient = new GoogleApiClient.Builder( getActivity() )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .addApi( LocationServices.API )
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
        if(buttonView.getId() == R.id.pin) {
            // handle switch
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.mMap = map;
        map.getUiSettings().setMapToolbarEnabled(false);
        map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));
    }

    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();

        mRef = activity.getFirebaseRef();

//        FirebaseRecyclerAdapter<Pulse, PulseViewHolder> mAdapter = new FirebaseRecyclerAdapter<Pulse, PulseViewHolder>(Pulse.class, android.R.layout.two_line_list_item, PulseViewHolder.class, mRef) {
//            @Override
//            protected void populateViewHolder(PulseViewHolder pulseViewHolder, Pulse pulse, int i) {
//                pulseViewHolder.nameText.setText("Checkin at Lat=" + pulse.getLatitude() + " Long=" + pulse.getLongitude());
//                pulseViewHolder.messageText.setText("By " + pulse.getEmployee() + "\nNote: " + pulse.getNote()
//                        + "\nStart: " + DateUtils.formatDateTime(context, pulse.getCheckin(), DateUtils.FORMAT_SHOW_TIME + DateUtils.FORMAT_SHOW_DATE + DateUtils.FORMAT_SHOW_YEAR)
//                        + "\nEnd: " + DateUtils.formatDateTime(context, pulse.getCheckout(), DateUtils.FORMAT_SHOW_TIME + DateUtils.FORMAT_SHOW_DATE + DateUtils.FORMAT_SHOW_YEAR));
//            }
//        };
//
//        pulsesList.setAdapter(mAdapter);
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        this.connectedGoogleApi = true;
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
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    getLocation();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void setTrackingLocationMode(boolean b) {
        this.trackingLocationMode = b;

        if (trackingLocationMode) {
            setTrackingLocation(false);
        } else {
            fab.setImageResource(R.drawable.ic_pin_drop_24dp);
            setTrackingLocation(false);
        }

    }

    public void setTrackingLocation(boolean trackingLocation) {
        this.trackingLocation = trackingLocation;
        if (trackingLocationMode) {
            if(trackingLocation) {
                fab.setImageResource(R.drawable.ic_stop_black);
            } else {
                fab.setImageResource(R.drawable.ic_play_arrow_black);
            }
        }
    }

    public static class PulseViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView nameText;

        public PulseViewHolder(View itemView) {
            super(itemView);
            nameText = (TextView)itemView.findViewById(android.R.id.text1);
            messageText = (TextView) itemView.findViewById(android.R.id.text2);
        }
    }
}
