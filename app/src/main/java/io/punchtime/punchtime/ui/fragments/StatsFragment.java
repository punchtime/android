package io.punchtime.punchtime.ui.fragments;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

import java.util.ArrayList;

import io.punchtime.punchtime.R;
import io.punchtime.punchtime.data.Pulse;
import io.punchtime.punchtime.ui.activities.MainActivity;

/**
 * Created by arnaud on 06/05/16.
 */
public class StatsFragment extends Fragment {
    private  MainActivity activity;
    private DecoView weekArcView;
    private DecoView dayArcView;
    private TextView dayHoursWorked;
    private TextView weekHoursWorked;

    private int seriesDayIndex;
    private int seriesWeekIndex;

    public StatsFragment() {
        Bundle args = new Bundle();
        setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, final Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        View v = inflater.inflate(R.layout.fragment_stats, parent, false);

        // Store calling activity (Always MainActivity)
        activity = (MainActivity) getActivity();

        // Setup toolbar
        activity.setTitle(R.string.menu_stats);

        // Store UI
        weekArcView = (DecoView) v.findViewById(R.id.weekArcView);
        dayArcView = (DecoView) v.findViewById(R.id.dayArcView);
        dayHoursWorked = (TextView) v.findViewById(R.id.dayHoursWorked);
        weekHoursWorked = (TextView) v.findViewById(R.id.weekHoursWorked);

        // Setup charts
        setupWeekArc();
        setupDayArc();

        // get Firebase data
        getStatistics();

        return v;
    }

    private void getStatistics() {
        if (activity.getAuth() != null) {
            Query query = activity.getFirebaseRef().child("users").child(activity.getAuth().getUid()).child("pulses").limitToLast(200);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<Pulse> pulses = new ArrayList<>();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        pulses.add(child.getValue(Pulse.class));
                    }
                    new CalculateHoursWorked().execute(pulses);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

    private void setupDayArc() {
        // Create background track
        dayArcView.addSeries(new SeriesItem.Builder(Color.argb(100,0,0,0))
                .setRange(0, 100, 100)
                .setLineWidth(32f)
                .build());

        //Create data series track
        SeriesItem seriesItem2 = new SeriesItem.Builder(getResources().getColor(R.color.colorAccent))
                .setRange(0, 100, 0)
                .setLineWidth(32f)
                .build();

        seriesDayIndex = dayArcView.addSeries(seriesItem2);

        dayArcView.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
                .setDelay(0)
                .setDuration(500)
                .build());
    }

    private void setupWeekArc() {
        // Create background track
        weekArcView.addSeries(new SeriesItem.Builder(Color.argb(100,0,0,0))
                .setRange(0, 100, 100)
                .setLineWidth(32f)
                .build());

        //Create data series track
        SeriesItem seriesItemWeek = new SeriesItem.Builder(getResources().getColor(R.color.colorAccent))
                .setRange(0, 100, 0)
                .setInitialVisibility(false)
                .setLineWidth(32f)
                .build();

        seriesWeekIndex = weekArcView.addSeries(seriesItemWeek);

        weekArcView.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
                .setDelay(0)
                .setDuration(500)
                .build());
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void updateChartUI(double hoursDay, double hoursWeek) {
        // TODO: 17/05/16 Don't hardcode values
        int quotaDay = 8;
        int quotaWeek = 38;
        dayArcView.addEvent(new DecoEvent.Builder((int)((hoursDay / quotaDay) * 100)).setIndex(seriesWeekIndex).setDuration(2000).build());
        weekArcView.addEvent(new DecoEvent.Builder((int)((hoursWeek / quotaWeek) * 100)).setIndex(seriesDayIndex).setDuration(2000).build());

        dayHoursWorked.setText((Math.round(hoursDay * 10.0) / 10.0) + " / " + quotaDay);
        weekHoursWorked.setText((Math.round(hoursWeek * 10.0) / 10.0) + " / " + quotaWeek);
    }

    private class CalculateHoursWorked extends AsyncTask<ArrayList<Pulse>, double[], double[]> {
        @Override
        protected double[] doInBackground(ArrayList<Pulse>... params) {
            ArrayList<Pulse> pulses = params[0];
            double hoursWorkedToday = 0, hoursWorkedWeek = 0, hoursWorkedPulse;
            for (Pulse pulse: pulses) {
                if(pulse.getCheckin() < (System.currentTimeMillis() - 6.048e+8)) continue;

                hoursWorkedPulse = pulse.getCheckout() != 0 ? (pulse.getCheckout() - pulse.getCheckin()) / 3.6e+6 : (System.currentTimeMillis() - pulse.getCheckin()) / 3.6e+6;

                if(pulse.getCheckin() > (System.currentTimeMillis() - 6.048e+8)) {
                    hoursWorkedWeek += hoursWorkedPulse;
                    if(pulse.getCheckin() > (System.currentTimeMillis() - 8.64e+7)) hoursWorkedToday += hoursWorkedPulse;
                }
            }

            return new double[] {hoursWorkedToday, hoursWorkedWeek};
        }

        @Override
        protected void onPostExecute(double[] hoursWorked) {
            super.onPostExecute(hoursWorked);
            updateChartUI(hoursWorked[0], hoursWorked[1]);
        }
    }
}
