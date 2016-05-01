package io.punchtime.punchtime.ui.fragments;

import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.punchtime.punchtime.R;
import io.punchtime.punchtime.data.Pulse;
import io.punchtime.punchtime.ui.activities.MainActivity;

/**
 * Created by haroenv on 26/03/16.
 */
public class WeekFragment extends Fragment  implements WeekView.EventClickListener, MonthLoader.MonthChangeListener,
        WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener {

    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private static final int TYPE_WEEK_VIEW = 3;
    private int mWeekViewType = TYPE_WEEK_VIEW;
    private WeekView mWeekView;
    private Firebase mRef;
    private MainActivity activity;
    List<Pulse> pulseList;

    // TODO fix view to start on current day, currently you always start on last week for some obscure reason
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        final View v = inflater.inflate(R.layout.fragment_week, parent, false);

        //set activity
        activity = (MainActivity) getActivity();

        mRef = activity.getFirebaseRef();
        pulseList = new ArrayList<>();
        getPulseData();

        // get view
        mWeekView = (WeekView) v.findViewById(R.id.weekView);

        // event click listener
        mWeekView.setOnEventClickListener(this);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);

        // Set long press listener for events.
        mWeekView.setEventLongPressListener(this);

        // Set long press listener for empty view
        mWeekView.setEmptyViewLongPressListener(this);

        // Set up a date time interpreter to interpret how the date and time will be formatted in
        // the week view. This is optional.
        setupDateTimeInterpreter(true);

        //return view
        return v;
    }

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
        return pulse.getAddressCityCountry();
    }

    /**
     * Set up a date time interpreter which will show short date values when in week view and long
     * date values otherwise.
     * @param shortDate True if the date values should be short.
     */
    private void setupDateTimeInterpreter(final boolean shortDate) {
        mWeekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                String weekday = weekdayNameFormat.format(date.getTime());
                SimpleDateFormat format = new SimpleDateFormat(" M/d", Locale.getDefault());

                // All android api level do not have a standard way of getting the first letter of
                // the week day name. Hence we get the first char programmatically.
                // Details: http://stackoverflow.com/questions/16959502/get-one-letter-abbreviation-of-week-day-of-a-date-in-java#answer-16959657
                if (shortDate)
                    weekday = String.valueOf(weekday.charAt(0));
                return weekday.toUpperCase() + format.format(date.getTime());
            }

            @Override
            public String interpretTime(int hour) {
                return hour > 11 ? (hour - 12) + " PM" : (hour == 0 ? "12 AM" : hour + " AM");
            }
        });
    }

    // Gets pulses ASYNC from Firebase
    public void getPulseData() {
        Query query = mRef.child("pulses").orderByChild("employee").equalTo(mRef.getAuth().getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pulseList.clear();
                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    pulseList.add(child.getValue(Pulse.class));
                }

                // Here we know pulsesList is filled
                //trigger update of weekview;
                mWeekView.notifyDatasetChanged();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
    }

    @Override
    public List<?extends WeekViewEvent> onMonthChange(int newYear,int newMonth) {
        List<WeekViewEvent> events = new ArrayList<>();
        for (Pulse pulse : pulseList) {
            Calendar startTime = Calendar.getInstance();
            startTime.setTimeInMillis(pulse.getCheckin());
            int pulseMonth = startTime.get(Calendar.MONTH);
            int pulseYear = startTime.get(Calendar.YEAR);
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
                WeekViewEvent event=new WeekViewEvent(1,getEventTitle(pulse),startTime,endTime);
                event.setColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
                events.add(event);
            }
        }
        /*log starttime of all weekviewevents might be useful to debug pulses
        for (WeekViewEvent event : events
             ) {
            Log.d("event", event.getStartTime().toString() + "\n" + event.getEndTime().toString());
        }*/
        return events;
    }
    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        // TODO: 01/05/16 Haroen: go to a view where pulse can be seen and edited
    }
    @Override
    public void onEmptyViewLongPress(Calendar calendar) {

    }
    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {

    }

    public WeekView getWeekView() {
        return mWeekView;
    }
}
