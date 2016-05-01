package io.punchtime.punchtime.ui.fragments;

import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import io.punchtime.punchtime.R;
import io.punchtime.punchtime.data.Pulse;
import io.punchtime.punchtime.ui.activities.MainActivity;

/**
 * Created by haroenv on 26/03/16.
 * for project: Punchtime
 */
public class ThreeDayFragment extends Fragment implements WeekView.EventClickListener, MonthLoader.MonthChangeListener,
        WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener{
    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private static final int TYPE_WEEK_VIEW = 3;
    private int mWeekViewType = TYPE_THREE_DAY_VIEW;
    private WeekView mWeekView;
    private Firebase mRef;
    private MainActivity activity;
    List<Pulse> pulseList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_three_day, parent, false);

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
                endTime.setTimeInMillis(pulse.getCheckout());
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

    }
    @Override
    public void onEmptyViewLongPress(Calendar calendar) {

    }
    @Override
    public void onEventLongPress(WeekViewEvent event,RectF eventRect) {

    }
}
