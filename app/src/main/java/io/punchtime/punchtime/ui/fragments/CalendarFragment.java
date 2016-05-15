package io.punchtime.punchtime.ui.fragments;


import android.content.DialogInterface;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LongSparseArray;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.punchtime.punchtime.R;
import io.punchtime.punchtime.data.Pulse;
import io.punchtime.punchtime.logic.operations.PulseOperations;
import io.punchtime.punchtime.ui.activities.MainActivity;

/**
 * Created by arnaud on 05/05/16.
 */
public class CalendarFragment extends Fragment implements WeekView.EventClickListener, MonthLoader.MonthChangeListener,
        WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener {
    private WeekView mWeekView;
    private Firebase mRef;
    private PulseOperations operations;
    private LongSparseArray<Pulse> pulseList;

    // triggered soon after onCreateView
    // Any view setup should occur here.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects
        Calendar cal = Calendar.getInstance();
        mWeekView.goToHour(cal.get(Calendar.HOUR_OF_DAY) - 1);
    }

    // gets the note for a given pulse
    protected String getEventTitle(Pulse pulse) {
        return pulse.getAddressStreet() + "\n" + pulse.getAddressCityCountry();
    }

    @Override
    public List<?extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        List<WeekViewEvent> events = new ArrayList<>();
        for(int i = 0; i < pulseList.size(); i++) {
            // get the key
            long key = pulseList.keyAt(i);
            // get the object by the key.
            Pulse pulse = pulseList.get(key);
            Calendar startTime = Calendar.getInstance();
            startTime.setTimeInMillis(pulse.getCheckin());
            int pulseMonth = startTime.get(Calendar.MONTH);
            int pulseYear = startTime.get(Calendar.YEAR);
            // checks if the pulse belongs to a new year/month
            // else we get triple entries
            if (newMonth == pulseMonth && newYear == pulseYear) {
                // create calendar instance of checkin
                startTime.setTimeInMillis(pulse.getCheckin());
                // create calendar instance of checkout
                Calendar endTime = Calendar.getInstance();
                Calendar now = Calendar.getInstance();
                if (pulse.getCheckout() == 0) {
                    endTime.setTimeInMillis(now.getTimeInMillis());
                } else {
                    endTime.setTimeInMillis(pulse.getCheckout());
                }
                // add event to the eventlist
                WeekViewEvent event = new WeekViewEvent(key, getEventTitle(pulse),startTime,endTime);
                event.setColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
                events.add(event);
            }
        }
        return events;
    }
    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getContext());

        Log.d("PUNCH", String.valueOf(event.getId()));
        Pulse pulse = pulseList.get(event.getId());
        String pulseNote = pulse.getNote();
        String noteInputMessage = getString(R.string.edit_note);
        if (pulseNote == "") {
            pulseNote = getString(R.string.no_note_added);
            noteInputMessage = getString(R.string.add_a_note);
        }
        alertDialogBuilder.setTitle(getString(R.string.info_pulse_title));
        alertDialogBuilder.setMessage(pulseNote + "\n"
                + pulse.getAddressStreet() + "\n" +  pulse.getAddressCityCountry());

        alertDialogBuilder.setPositiveButton(noteInputMessage, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // continue with delete
            }
        })
        .setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }
    @Override
    public void onEmptyViewLongPress(Calendar calendar) {

    }
    @Override
    public void onEventLongPress(WeekViewEvent event,RectF eventRect) {

    }
    public void setupVariables() {
        //set activity
        MainActivity activity = (MainActivity) getActivity();
        mRef = activity.getFirebaseRef();
        operations = new PulseOperations(mRef);
        pulseList = new LongSparseArray<>();
    }
    public void setupWeekView(WeekView weekView) {

        mWeekView = weekView;

        // get pulse data and notify mWeekview of change when all the data is pulled
        pulseList = operations.getPulseData(mWeekView);

        // event click listener
        mWeekView.setOnEventClickListener(this);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);

        // Set long press listener for events.
        mWeekView.setEventLongPressListener(this);

        // Set long press listener for empty view
        mWeekView.setEmptyViewLongPressListener(this);
    }
}

