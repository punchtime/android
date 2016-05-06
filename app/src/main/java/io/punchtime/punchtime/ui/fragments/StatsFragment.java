package io.punchtime.punchtime.ui.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.punchtime.punchtime.R;
import io.punchtime.punchtime.ui.activities.MainActivity;

/**
 * Created by arnaud on 06/05/16.
 */
public class StatsFragment extends Fragment {
    private View v;

    public StatsFragment() {
        Bundle args = new Bundle();
        setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, final Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        v = inflater.inflate(R.layout.fragment_stats, parent, false);

        // Store calling activity (Always MainActivity)
        MainActivity activity = (MainActivity) getActivity();

        // Setup toolbar
        activity.setTitle(R.string.menu_stats);

        return v;
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
