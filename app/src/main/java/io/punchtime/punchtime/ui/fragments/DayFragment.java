package io.punchtime.punchtime.ui.fragments;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.punchtime.punchtime.R;
import io.punchtime.punchtime.ui.activities.MainActivity;

/**
 * Created by Arnaud on 3/23/2016.
 */
public class DayFragment extends Fragment implements WeekView.EventClickListener, MonthLoader.MonthChangeListener,
        WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener {

    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private static final int TYPE_WEEK_VIEW = 3;
    private int mWeekViewType = TYPE_DAY_VIEW;
    private WeekView mWeekView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_day, parent, false);
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
    }

    protected String getEventTitle(Calendar time) {
        return String.format("Event of %02d:%02d %s/%d", time.get(Calendar.HOUR_OF_DAY),
                time.get(Calendar.MINUTE), time.get(Calendar.MONTH)+1, time.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public List<?extends WeekViewEvent>onMonthChange(int newYear,int newMonth){

        // simple hard coded event in eventlist
        List<WeekViewEvent>events=new ArrayList<WeekViewEvent>();

        Calendar startTime=Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY,3);
        startTime.set(Calendar.MINUTE,0);
        startTime.set(Calendar.MONTH,newMonth-1);
        startTime.set(Calendar.YEAR,newYear);
        Calendar endTime=(Calendar)startTime.clone();
        endTime.add(Calendar.HOUR,1);
        endTime.set(Calendar.MONTH,newMonth-1);
        WeekViewEvent event=new WeekViewEvent(1,getEventTitle(startTime),startTime,endTime);
        event.setColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
        events.add(event);

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
