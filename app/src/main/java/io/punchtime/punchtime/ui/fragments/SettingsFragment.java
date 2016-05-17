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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;

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

    private final ContactItem[] items = new ContactItem[3];

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
        Preference prefLogin = findPreference("pref_key_account");

        if(preferences.getBoolean("logged_in", false)) {
            prefLogin.setSummary(R.string.logged_in_summary);
        } else {
            prefLogin.setSummary(R.string.not_logged_in_summary);
        }

        prefLogin.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference pref) {
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

        Preference prefAddCompany = findPreference("pref_key_add_company");

        prefAddCompany.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference pref) {
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
        if(activity.getAuth() != null) setCompaniesData(currentCompany);
        currentCompany.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                setCompaniesData(currentCompany);
                return false;
            }
        });

        // TODO: 18/05/16 figure out when to call setContact() so it's updated when the company changes
        /*currentCompany.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                setContact();
                return false;
            }
        });*/

        final ListAdapter adapter = new ArrayAdapter<ContactItem>(
                activity,
                android.R.layout.select_dialog_item,
                android.R.id.text1,
                items){
            public View getView(int position, View convertView, ViewGroup parent) {
                // Use super class to create the View
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView)v.findViewById(android.R.id.text1);

                // Put the image on the TextView
                tv.setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0, 0);

                // Add margin between image and text (support various screen densities)
                int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 5f);
                tv.setCompoundDrawablePadding(dp5);
                tv.setTextSize(16);

                return v;
            }
        };

        if(activity.getAuth() != null) setContact();
        final Preference contact = findPreference("pref_contact");
        contact.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.contact_title);

                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        switch (which) {
                            // the phone number
                            case 0:
                                startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", items[which].text, null)));
                                break;
                            // the email address
                            case 1:
                                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                                emailIntent.setData(Uri.parse("mailto:"+ items[which].text));
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

    protected void setContact() {
        activity.getFirebaseRef().child("companies").child(preferences.getString("pref_current_company","")).child("contact").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                items[0] = new ContactItem(dataSnapshot.child("phone").getValue(String.class), R.drawable.ic_call_black_24dp);
                items[1] = new ContactItem(dataSnapshot.child("email").getValue(String.class), R.drawable.ic_email_black_24dp);
                items[2] = new ContactItem(dataSnapshot.child("note").getValue(String.class), 0);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // we only need to get the data once.
            }
        });
    }

    protected void setCompaniesData(final ListPreference lp) {
        activity.getFirebaseRef().child("users").child(activity.getAuth().getUid()).child("employee").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<CharSequence> entries = new ArrayList<>();
                final List<CharSequence> entryValues = new ArrayList<>();
                List<CharSequence> employerKeys = new ArrayList<>();

                for (DataSnapshot employer : dataSnapshot.getChildren()) {
                    employerKeys.add(employer.getKey());
                    entryValues.add(employer.getKey());
                    activity.getFirebaseRef().child("companies").child(employer.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            entries.add(dataSnapshot.child("name").getValue(String.class));
                            lp.setEntries(entries.toArray(new CharSequence[entries.size()]));
                            lp.setEntryValues(entryValues.toArray(new CharSequence[entryValues.size()]));
                            if(lp.getValue() == null) lp.setValue((String)entryValues.get(0));
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            // no. Firebase doesn't do errors
                        }
                    });
                }
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

    public static class ContactItem {
        public final String text;
        public final int icon;
        public ContactItem(String text, Integer icon) {
            this.text = text;
            this.icon = icon;
        }
        @Override
        public String toString() {
            return text;
        }
    }
}
