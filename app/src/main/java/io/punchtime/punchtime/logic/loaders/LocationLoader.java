package io.punchtime.punchtime.logic.loaders;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.Loader;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by elias on 28/04/16.
 * for project: Punchtime
 */
public class LocationLoader extends Loader<Location>
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private ConnectionResult mConnectionResult;

    public LocationLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (mLastLocation != null) {
            deliverResult(mLastLocation);
        }
        if (mGoogleApiClient == null) {
            mGoogleApiClient =
                    new GoogleApiClient.Builder(getContext(), this, this)
                            .addApi(LocationServices.API)
                            .build();
            mGoogleApiClient.connect();
        } else if (mGoogleApiClient.isConnected()) {
            // Request updates
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, new LocationRequest(), this);
        }
    }
    @Override
    protected void onStopLoading() {
        // Reduce battery usage when the activity is stopped
        // This helps us handle if the home button is pressed
        // And the loader is stopped but not yet destroyed
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient,
                    new LocationRequest()
                            .setPriority(LocationRequest.PRIORITY_NO_POWER),
                    this);
        }
    }
    @Override
    protected void onForceLoad() {
        // Resend the last known location if we have one
        if (mLastLocation != null) {
            deliverResult(mLastLocation);
        }
        // Try to reconnect if we aren’t connected
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        mConnectionResult = null;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Try to immediately return a result
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            deliverResult(mLastLocation);
        }
        // Request updates
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, LocationRequest.create().setInterval(1000).setFastestInterval(100), this);
    }
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        // Deliver the location changes
        deliverResult(location);
    }
    @Override
    public void onConnectionSuspended(int cause) {
        // Cry softly, hope it comes back on its own
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mConnectionResult = connectionResult;
        // Signal that something has gone wrong.
        deliverResult(null);
    }
    /**
     * Retrieve the ConnectionResult associated with a null
     * Location to aid inrecovering from connection failures.
     * Call startResolutionForResult() and then restart the
     * loader when the result is returned.
     * @return The last ConnectionResult
     */
    public ConnectionResult getConnectionResult() {
        return mConnectionResult;
    }
    @Override
    protected void onReset() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }
}
