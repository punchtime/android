package io.punchtime.punchtime.ui.fragments;

/**
 * Created by elias on 19/04/16.
 */

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.punchtime.punchtime.R;
import io.punchtime.punchtime.ui.activities.MainActivity;

public class HistoryFragment extends Fragment {
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private MainActivity activity;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history, parent, false);
        // Defines the xml file for the fragment
        activity = (MainActivity) getActivity();
        activity.setTitle(R.string.menu_history);

        viewPager = (ViewPager) v.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        AppBarLayout appBar = (AppBarLayout) activity.findViewById(R.id.appBar);
        tabLayout = (TabLayout) LayoutInflater.from(activity).inflate(R.layout.tabs, appBar, false);
        activity.addViewToAppBarLayout(tabLayout);

        tabLayout.setupWithViewPager(viewPager);
        Bundle args = getArguments();
        if (args != null) {
            switch (args.getInt("fragment", 0)) {
                case R.id.nav_3days:
                    viewPager.setCurrentItem(1);
                    break;
                case R.id.nav_week:
                    viewPager.setCurrentItem(2);
                    break;
                case R.id.nav_month:
                    viewPager.setCurrentItem(3);
                    break;
            }
        }

        return v;
    }

    // triggered soon after onCreateView
    // Any view setup should occur here.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects
    }

    @Override
    public void onDestroyView() {
        activity.removeViewFromAppBarLayout(tabLayout);
        super.onDestroyView();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new DayFragment(), getString(R.string.menu_day));
        adapter.addFragment(new ThreeDayFragment(), getString(R.string.menu_3_day));
        adapter.addFragment(new WeekFragment(), getString(R.string.menu_week));
        adapter.addFragment(new MonthFragment(), getString(R.string.menu_month));
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}

