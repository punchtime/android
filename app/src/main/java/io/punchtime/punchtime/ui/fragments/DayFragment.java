package io.punchtime.punchtime.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alamkanak.weekview.WeekView;

import io.punchtime.punchtime.R;

/**
 * Created by Arnaud on 3/23/2016.
 * for project: Punchtime
 */
public class DayFragment extends CalendarFragment {
    private WeekView mWeekView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_day, parent, false);

        super.setupVariables();

        // get view
        mWeekView = (WeekView) v.findViewById(R.id.weekView);

        super.setupWeekView(mWeekView);

        //return view

        return v;
    }
}
