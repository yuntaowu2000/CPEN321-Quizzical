package com.cpen321.quizzical.ui.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.cpen321.quizzical.MainMenuFragments.ProfileFragment;
import com.cpen321.quizzical.MainMenuFragments.QuizFragment;
import com.cpen321.quizzical.MainMenuFragments.StatisticFragment;

public class MyHomePagerAdapter extends FragmentPagerAdapter {

    int numOfTabs;
    boolean is_Instructor;

    public MyHomePagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        this.numOfTabs = behavior;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new QuizFragment(is_Instructor);
            case 1:
                return new StatisticFragment(is_Instructor);
            default:
                return new ProfileFragment();
        }

    }

    @Override
    public int getCount() {
        return numOfTabs;
    }

    public void setIs_Instructor(boolean is_Instructor) {
        this.is_Instructor = is_Instructor;
    }
}
