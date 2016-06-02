package io.punchtime.punchtime.logic.operations;

//import android.os.AsyncTask;

import android.support.v4.util.LongSparseArray;
import android.support.v7.preference.PreferenceManager;

import com.alamkanak.weekview.WeekView;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import io.punchtime.punchtime.data.Pulse;
import io.punchtime.punchtime.ui.activities.MainActivity;

//import com.firebase.client.Query;

/**
 * Created by arnaud on 15/05/16.
 */
public class PulseOperations {
    private Firebase mRef;
    private MainActivity activity;
    private LongSparseArray<Pulse> pulseArray;
    private LongSparseArray<String> keyArray;

    public PulseOperations(Firebase mRef, MainActivity activity) {
        this.mRef = mRef;
        this.activity = activity;
        pulseArray = new LongSparseArray<>();
        keyArray = new LongSparseArray<>();
    }

    public LongSparseArray<String> getKeyArray() {
        return this.keyArray;
    }

    /*
     * Could be replaced with a more general method that updates any child of a pulse
     */
    public void updatePulseNote(Long longKey, String noteString) {
        //get key for pulse
        String key = keyArray.get(longKey);

        Firebase query = mRef.child("users").child(mRef.getAuth().getUid()).child("pulses").child(key);
        Map<String, Object> note = new HashMap<>();
        note.put("note", noteString);
        query.updateChildren(note);
    }

    /*
     *  Gets pulsedata ASYNC from Firebase
     */
    public LongSparseArray<Pulse> getPulseData(final WeekView weekView) {
        mRef.child("users").child(mRef.getAuth().getUid()).child("pulses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pulseArray.clear();
                long key = 0;
                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    String currentCompany = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).getString("pref_current_company","");
                    if(currentCompany.equals(child.child("employer").getValue(String.class))) {
                        pulseArray.put(key, child.getValue(Pulse.class));
                        keyArray.put(key, child.getKey());
                        key++;
                    }
                }

                // Here we know pulsesList is filled
                // trigger update of weekview;
                weekView.notifyDatasetChanged();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // Firebase isn't allowed to be cancelled
                // get a better connection already
            }
        });
        return pulseArray;
    }
}
