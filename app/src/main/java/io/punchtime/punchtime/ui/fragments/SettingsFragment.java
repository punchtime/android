package io.punchtime.punchtime.ui.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.punchtime.punchtime.R;
import io.punchtime.punchtime.ui.activities.MainActivity;

/**
 * Created by haroenv on 26/03/16.
 * for project: Punchtime
 */
public class SettingsFragment extends PreferenceFragmentCompat {
    private SharedPreferences preferences;

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

        prefAddCompany.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final android.support.v7.preference.Preference pref) {
                final AlertDialog.Builder inviteAlert = new AlertDialog.Builder(activity);
                inviteAlert.setTitle(R.string.invitation_prompt);
                final EditText userInput = new EditText(activity);
                inviteAlert.setView(userInput);
                inviteAlert.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        joinCompany(userInput.getText().toString().trim());
                    }
                });
                inviteAlert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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

        final ListPreference currentCompany = (ListPreference) findPreference("pref_current_company");
        // TODO: 16/05/16 Make this wait on firebase
        if(activity.getAuth() != null) setCompaniesData(currentCompany);
        currentCompany.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                setCompaniesData(currentCompany);
                return false;
            }
        });
        // TODO: 16/05/16 show contact of company
        final Preference contact = findPreference("pref_contact");
        contact.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.contact_title);
                // TODO: 17/05/16 get contact info from firebase and current employer
                final CharSequence[] contactArray = new CharSequence[3];
                contactArray[0] = "0032497466234";
                contactArray[1] = "hello@haroen.me";
                contactArray[2] = "Don't call me unless you're in great danger!";
                builder.setItems(contactArray, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which) {
                            // the phone number
                            case 0:
                                startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", contactArray[which].toString(), null)));
                                break;
                            // the email address
                            case 1:
                                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                                emailIntent.setData(Uri.parse("mailto:"+contactArray[which].toString()));
                                startActivity(Intent.createChooser(emailIntent, activity.getString(R.string.email_title)));
                                break;
                            default:
                                break;
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return false;
            }
        });
    }

    protected void setCompaniesData(final ListPreference lp) {
        activity.getFirebaseRef().child("users").child(activity.getAuth().getUid()).child("employer").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<CharSequence> entries = new ArrayList<>();
                List<CharSequence> entryValues = new ArrayList<>();
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot i : children) {
                    entries.add(i.toString());
                    entryValues.add(i.getKey());
                }
                lp.setEntries(entries.toArray(new CharSequence[entries.size()]));
                lp.setEntryValues(entryValues.toArray(new CharSequence[entryValues.size()]));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // we only need to get the data once.
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
                                .setTitle(R.string.invitation_claimed_title)
                                .setMessage(R.string.invitation_claimed_text)
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
                            .setTitle(R.string.invitation_success_title)
                            .setMessage(R.string.invitation_success_text + dataSnapshot.child("company/name").getValue().toString())
                            .create().show();

                } else {
                    // Alert failure
                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.invitation_error_title)
                            .setMessage(R.string.invitation_error_text)
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
