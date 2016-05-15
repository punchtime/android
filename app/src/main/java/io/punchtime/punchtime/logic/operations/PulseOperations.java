package io.punchtime.punchtime.logic.operations;

import android.support.v4.util.LongSparseArray;

import com.alamkanak.weekview.WeekView;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import io.punchtime.punchtime.data.Pulse;

/**
 * Created by arnaud on 15/05/16.
 */
public class PulseOperations {
    private Firebase mRef;
    private LongSparseArray<Pulse> pulseList;

    public PulseOperations(Firebase mRef) {
        this.mRef = mRef;
        pulseList = new LongSparseArray<Pulse>();
    }

    /*
     *  Gets pulsedata ASYNC from Firebase
     */
    public LongSparseArray<Pulse> getPulseData(final WeekView weekView) {
        mRef.child("users").child(mRef.getAuth().getUid()).child("pulses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pulseList.clear();
                long key = 0;
                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    pulseList.put(key, child.getValue(Pulse.class));
                    key++;
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
        return pulseList;
    }
}
