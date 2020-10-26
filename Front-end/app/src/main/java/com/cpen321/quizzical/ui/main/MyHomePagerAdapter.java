package com.cpen321.quizzical.ui.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.cpen321.quizzical.mmf.ProfileFragment;
import com.cpen321.quizzical.mmf.QuizFragment;
import com.cpen321.quizzical.mmf.StatisticFragment;

public class MyHomePagerAdapter extends FragmentPagerAdapter {

    private int numOfTabs;

    public MyHomePagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        this.numOfTabs = behavior;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new QuizFragment();
            case 1:
                return new StatisticFragment();
            default:
                return new ProfileFragment();
        }

    }

    @Override
    public int getCount() {
        return numOfTabs;
    }

}
