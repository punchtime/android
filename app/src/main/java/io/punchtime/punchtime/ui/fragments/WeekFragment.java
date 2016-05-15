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
public class WeekFragment extends CalendarFragment {
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

    // fixes bug where it goes to last week
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("H", Locale.getDefault());
        int hour = Integer.parseInt(format.format(cal.getTime()));
        mWeekView.goToHour(hour - 1);
        //dodgy fix to fix the start week, but I guess it works so yeah there's that
        int day = cal.get(Calendar.DAY_OF_WEEK);
        if (day > 2 && day < 8) {
            cal.add(Calendar.DATE, 7);
        }
        mWeekView.goToDate(cal);
    }

    private void setupDateTimeInterpreter(final boolean shortDate) {
        mWeekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                String weekday = weekdayNameFormat.format(date.getTime());
                SimpleDateFormat format = new SimpleDateFormat(" d/M", Locale.getDefault());

                // All android api level do not have a standard way of getting the first letter of
                // the week day name. Hence we get the first char programmatically.
                // Details: http://stackoverflow.com/questions/16959502/get-one-letter-abbreviation-of-week-day-of-a-date-in-java#answer-16959657
                if (shortDate)
                    weekday = String.valueOf(weekday.charAt(0));
                return weekday.toUpperCase() + format.format(date.getTime());
            }

            @Override
            public String interpretTime(int hour) {
                return hour < 10 ? "0" + hour + ":00" : hour + ":00";
            }
        });
    }

    @Override
    protected String getEventTitle(Pulse pulse) {
        return pulse.getAddressCityCountry();
    }
}
