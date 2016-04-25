package io.punchtime.punchtime.ui.fragments;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.View;

import io.punchtime.punchtime.R;
import io.punchtime.punchtime.ui.activities.MainActivity;

/**
 * Created by haroenv on 26/03/16.
 */
public class SettingsFragment extends PreferenceFragmentCompat {
    SharedPreferences preferences;

    private MainActivity activity;
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        activity = (MainActivity) getActivity();
        activity.setTitle(R.string.settings);
        preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
        android.support.v7.preference.Preference pref = findPreference("pref_key_account");

        if(preferences.getBoolean("logged_in", false)) {
            pref.setSummary(R.string.logged_in_summary);
        } else {
            pref.setSummary(R.string.not_logged_in_summary);
        }

        pref.setOnPreferenceClickListener(new android.support.v7.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final android.support.v7.preference.Preference pref) {
                if(preferences.getBoolean("logged_in", false)) {
                    new AlertDialog.Builder(activity)
                            .setTitle(getString(R.string.logout_dialog_title))
                            .setMessage(getString(R.string.logout_dialog_message))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    activity.logout();
                                    pref.setSummary(R.string.not_logged_in_summary);
                                }})
                            .setNegativeButton(android.R.string.no, null).show();
                } else {
                    activity.showFirebaseLoginPrompt();
                }
                return false;
            }
        });
    }

    // triggered soon after onCreateView
    // Any view setup should occur here.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects
    }
}
