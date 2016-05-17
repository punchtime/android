package io.punchtime.punchtime.ui.fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.RectF;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LongSparseArray;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.firebase.client.Firebase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private PulseOperations operations;
    private LongSparseArray<Pulse> pulseArray;
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

        // setup the format
        DateFormat formatter = new SimpleDateFormat("HH:mm");

        // convert millisecs to date
        Date pulseCheckinDate = new Date(pulse.getCheckin());

        // set strings
        String pulseCheckin = formatter.format(pulseCheckinDate);
        String pulseCheckout = "now";

        // check if pulse is still going, change string if needed
        if (pulse.getCheckout() != 0) {
            Date pulseCheckoutDate = new Date(pulse.getCheckout());
            pulseCheckout = formatter.format(pulseCheckoutDate);
        }


        String pulseNote = pulse.getNote();
        String notePlaceholder = pulseNote;
        String noteInputMessage;

        // extra string for new dialog
        String addNoteTitle;

        // change message if no note is found
        if ("".equals(notePlaceholder)) {
            notePlaceholder = getString(R.string.no_note_added);
            noteInputMessage = getString(R.string.add_a_note);
            addNoteTitle = noteInputMessage;
        }
        else {
            noteInputMessage = getString(R.string.edit_note);
            addNoteTitle = noteInputMessage;
        }

        // set icon
        alertDialogBuilder.setIcon(R.drawable.ic_access_time_black_24dp);

        // set title
        /*int duration = (int) (pulse.getCheckout() - pulse.getCheckin() / 1000*60*60) % 24;
        if (duration > 12) {
            String durationString = Integer.toString(duration);
            alertDialogBuilder.setTitle(pulseCheckin + " - " + pulseCheckout + " (" + durationString + " hours)");
        }
        else {*/
            
        alertDialogBuilder.setTitle(pulseCheckin + " - " + pulseCheckout);


        // set message
        alertDialogBuilder.setMessage(notePlaceholder + "\n"
                + pulse.getAddressStreet() + "\n" +  pulse.getAddressCityCountry());

        alertDialogBuilder.setPositiveButton(noteInputMessage, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // show new dialog that lets you edit the note / add a new one
                // handled by the CustomListener
            }
        })
        .setNeutralButton(getString(R.string.show_on_map), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getActivity(), MapDetailActivity.class);
                final Location l = new Location("pulse");
                l.setLatitude(pulse.getLatitude());
                l.setLongitude(pulse.getLongitude());
                intent.putExtra("location", l);
                startActivity(intent);
            }
        })
        .setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // does nothing, just closes the dialog
            }
        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
        Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new NoteListener(alertDialog, addNoteTitle, pulseNote, event.getId()));
    }
    @Override
    public void onEmptyViewLongPress(Calendar calendar) {
        // no use for this listener
    }
    @Override
    public void onEventLongPress(WeekViewEvent event,RectF eventRect) {
        // no use for this listener (yet)
    }
    public void setupVariables() {
        //set context and activity
        context = getContext();
        activity = (MainActivity) getActivity();
        Firebase mRef = activity.getFirebaseRef();
        operations = new PulseOperations(mRef);
        pulseArray = new LongSparseArray<>();
    }
    public void setupWeekView(WeekView weekView) {

        mWeekView = weekView;

        // get pulse data and notify mWeekview of change when all the data is pulled
        if(activity.getAuth() != null) pulseArray = operations.getPulseData(mWeekView);

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
        private final Dialog upperDialog;
        private String note;
        private String noteTitle;
        private Long key;

        public NoteListener(Dialog dialog, String noteTitle, String note, Long key) {
            this.upperDialog = dialog;
            this.note = note;
            this.key = key;
            this.noteTitle = noteTitle;
        }
        // opens new alert dialog that lets you edit/add a note
        @Override
        public void onClick(View v) {
            final AlertDialog.Builder inputAlert = new AlertDialog.Builder(context);

            final EditText userInput = new EditText(context);
            inputAlert.setView(userInput);

            // set placeholder on text
            userInput.setText(note);

            inputAlert.setTitle(noteTitle);

            inputAlert.setPositiveButton(getString(R.string.submit), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String input = userInput.getText().toString();
                    // update note for pulse in Firebase
                    operations.updatePulseNote(key, input);

                    // update the pulse manually
                    // don't think Firebase automatically updates it
                    Pulse pulse = pulseArray.get(key);
                    pulse.setNote(input);
                    pulseArray.put(key, pulse);

                    // notifiy the weekview to refresh
                    mWeekView.notifyDatasetChanged();

                    // try and hide keyboard because it's still up on the upperDialog, doesn't really work rip
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(upperDialog.getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);

                    //TODO Don't dismiss upperDialog and update to include new message

                    // now we just hide the upperDialog
                    upperDialog.dismiss();
                }
            });
            inputAlert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // does nothing, just closes the dialog
                }
            });
            AlertDialog alertDialog = inputAlert.create();
            alertDialog.show();
        }
    }

}
