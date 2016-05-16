package io.punchtime.punchtime.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

import io.punchtime.punchtime.R;
import io.punchtime.punchtime.ui.activities.MainActivity;

/**
 * Created by arnaud on 06/05/16.
 */
public class StatsFragment extends Fragment {
    private  MainActivity activity;
    private DecoView weekArcView;
    private DecoView dayArcView;

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

        // Store charts
        weekArcView = (DecoView) getView().findViewById(R.id.weekArcView);
        dayArcView = (DecoView) v.findViewById(R.id.dayArcView);

        // Setup charts
        setupWeekArc();
        setupDayArc();

        return v;
    }

    private void setupDayArc() {
        // Create background track
        dayArcView.addSeries(new SeriesItem.Builder(Color.argb(56,0,0,0))
                .setRange(0, 100, 100)
                .setLineWidth(32f)
                .build());

        //Create data series track
        SeriesItem seriesItem2 = new SeriesItem.Builder(getResources().getColor(R.color.colorAccent))
                .setRange(0, 100, 0)
                .setLineWidth(32f)
                .build();

        dayArcView.addSeries(seriesItem2);

        dayArcView.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
                .setDelay(0)
                .setDuration(500)
                .build());
    }

    private void setupWeekArc() {

        // Create background track
        weekArcView.addSeries(new SeriesItem.Builder(Color.argb(56,0,0,0))
                .setRange(0, 100, 100)
                .setLineWidth(32f)
                .build());

        //Create data series track
        SeriesItem seriesItem1 = new SeriesItem.Builder(getResources().getColor(R.color.colorAccent))
                .setRange(0, 100, 0)
                .setLineWidth(32f)
                .build();

        weekArcView.addSeries(seriesItem1);

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
}
