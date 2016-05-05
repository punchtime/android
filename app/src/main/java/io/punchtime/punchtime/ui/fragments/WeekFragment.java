package io.punchtime.punchtime.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.WeekView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.punchtime.punchtime.R;
import io.punchtime.punchtime.data.Pulse;

/**
 * Created by haroenv on 26/03/16.
 */
public class WeekFragment extends CalendarSetup {
    private WeekView mWeekView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_week, parent, false);

        super.setupVariables();

        // get view
        mWeekView = (WeekView) v.findViewById(R.id.weekView);

        super.setupWeekView(mWeekView);

        setupDateTimeInterpreter(true);

        //return view

        return v;
    }

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

    @Override
    protected String getEventTitle(Pulse pulse) {
        return pulse.getAddressCityCountry();
    }
}
