package io.punchtime.punchtime.ui.fragments;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import io.punchtime.punchtime.R;
import io.punchtime.punchtime.ui.activities.MainActivity;

/**
 * Created by haroenv on 26/03/16.
 * for project: Punchtime
 */
public class SettingsFragment extends PreferenceFragmentCompat {
    SharedPreferences preferences;

    private MainActivity activity;
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        activity = (MainActivity) getActivity();
        activity.setTitle(R.string.settings);
        preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());

        if (getArguments() != null) {
            String invite = getArguments().getString("invite");
            if (invite != null) {
                ((MainActivity) getActivity()).getNavigationView().setCheckedItem(R.id.nav_settings);
                joinCompany(invite);
            }
        }

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
        android.support.v7.preference.Preference prefLogin = findPreference("pref_key_account");

        if(preferences.getBoolean("logged_in", false)) {
            prefLogin.setSummary(R.string.logged_in_summary);
        } else {
            prefLogin.setSummary(R.string.not_logged_in_summary);
        }

        prefLogin.setOnPreferenceClickListener(new android.support.v7.preference.Preference.OnPreferenceClickListener() {
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
                activity.setLaunchedFromIntent(false);
            }
            return false;
            }
        });

        android.support.v7.preference.Preference prefAddCompany = findPreference("pref_key_add_company");

        prefAddCompany.setOnPreferenceClickListener(new android.support.v7.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final android.support.v7.preference.Preference pref) {
                final AlertDialog.Builder inviteAlert = new AlertDialog.Builder(activity);
                inviteAlert.setTitle("Enter your invitation code");
                final EditText userInput = new EditText(activity);
                inviteAlert.setView(userInput);
                inviteAlert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        joinCompany(userInput.getText().toString().trim());
                    }
                });
                inviteAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = inviteAlert.create();
                alertDialog.show();
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

    private void joinCompany(String inviteCode) {
        activity.getFirebaseRef().child("invites").child(inviteCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    if(dataSnapshot.child("claimed").getValue(boolean.class)) {
                        // Alert already joined
                        new AlertDialog.Builder(activity)
                                .setTitle("Invitation code error")
                                .setMessage("This invite code has already been claimed")
                                .create().show();
                        return;
                    }
                    // Claim invite
                    Map<String, Object> map = new HashMap<>();
                    map.put("user", activity.getAuth().getUid());
                    map.put("claimed", true);
                    dataSnapshot.getRef().updateChildren(map);

                    // Alert success
                    new AlertDialog.Builder(activity)
                            .setTitle("Success")
                            .setMessage("You have successfully joined " + dataSnapshot.child("company/name").getValue().toString())
                            .create().show();

                } else {
                    // Alert failure
                    new AlertDialog.Builder(activity)
                            .setTitle("Invitation code error")
                            .setMessage("Please verify that you entered the right code and try again.")
                            .create().show();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // don't disconnect from Firebase please
            }
        });
    }
}
