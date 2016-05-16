package io.punchtime.punchtime.ui.fragments;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.RectF;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LongSparseArray;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.firebase.client.Firebase;
import android.content.Context;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.punchtime.punchtime.R;
import io.punchtime.punchtime.data.Pulse;
import io.punchtime.punchtime.logic.operations.PulseOperations;
import io.punchtime.punchtime.ui.activities.MainActivity;
import io.punchtime.punchtime.ui.activities.MapDetailActivity;

/**
 * Created by arnaud on 05/05/16.
 */
public class CalendarFragment extends Fragment implements WeekView.EventClickListener, MonthLoader.MonthChangeListener,
        WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener {
    private WeekView mWeekView;
    private Firebase mRef;
    private PulseOperations operations;
    private LongSparseArray<Pulse> pulseArray;
    private LongSparseArray<Pulse> keyArray;
    private static Context context;
    private MainActivity activity;

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
        for(int i = 0; i < pulseArray.size(); i++) {
            // get the key
            long key = pulseArray.keyAt(i);

            // get the object by the key.
            Pulse pulse = pulseArray.get(key);
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
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        //get pulse and assign note variable
        final Pulse pulse = pulseArray.get(event.getId());
        String pulseNote = pulse.getNote();

        // change message if no note is found
        String noteInputMessage = getString(R.string.edit_note);
        if ("".equals(pulseNote)) {
            pulseNote = getString(R.string.no_note_added);
            noteInputMessage = getString(R.string.add_a_note);
        }

        // set title
        alertDialogBuilder.setTitle(getString(R.string.info_pulse_title));

        // set message
        alertDialogBuilder.setMessage(pulseNote + "\n"
                + pulse.getAddressStreet() + "\n" +  pulse.getAddressCityCountry());

        alertDialogBuilder.setPositiveButton(noteInputMessage, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // show new dialog that lets you edit the note / add a new one
                // handled by the CustomListener
            }
        })
        .setNeutralButton(getString(R.string.show_on_map), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent((Activity) getActivity(), MapDetailActivity.class);
                final Location l = new Location("pulse");
                l.setLatitude(pulse.getLatitude());
                l.setLongitude(pulse.getLongitude());
                intent.putExtra("location", l);
                startActivity(intent);
            }
        })
        .setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
        Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new NoteListener(alertDialog, context, pulseNote, event.getId()));
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
        context = getContext();
        activity = (MainActivity) getActivity();
        mRef = activity.getFirebaseRef();
        operations = new PulseOperations(mRef);
        pulseArray = new LongSparseArray<>();
    }
    public void setupWeekView(WeekView weekView) {

        mWeekView = weekView;

        // get pulse data and notify mWeekview of change when all the data is pulled
        pulseArray = operations.getPulseData(mWeekView);

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

    class NoteListener implements View.OnClickListener {
        private final Dialog dialog;
        private final String note;
        private Long key;

        public NoteListener(Dialog dialog, Context context, String note, Long key) {
            this.dialog = dialog;
            this.note = note;
            this.key = key;
        }
        @Override
        public void onClick(View v) {
            final AlertDialog.Builder inputAlert = new AlertDialog.Builder(context);

            inputAlert.setMessage(getString(R.string.write_current_note_message));
            final EditText userInput = new EditText(context);
            inputAlert.setView(userInput);

            inputAlert.setTitle(note);

            inputAlert.setPositiveButton(getString(R.string.submit), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // update note for pulse in Firebase
                    operations.updatePulseNote(key, note);
                }
            });
            inputAlert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog alertDialog = inputAlert.create();
            alertDialog.show();
        }
    }

}
