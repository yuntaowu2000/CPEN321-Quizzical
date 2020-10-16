package com.cpen321.quizzical;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.cpen321.quizzical.ui.main.MyHomePagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class HomeActivity extends AppCompatActivity {

    TabLayout tabLayout;
    boolean is_Instructor;
    SharedPreferences sp;


    /**
     * This is the class for the main home screen of the APP
     * We use a tab layout for quiz, class statistics/leader board, and profile
     * Instructor and students will have different layout and functionalities
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //check if the user is instructor from shared preferences
        sp = getSharedPreferences(getString(R.string.curr_login_user), MODE_PRIVATE);
        is_Instructor = sp.getBoolean(getString(R.string.IS_INSTRUCTOR), false);

        ViewPager viewPager = findViewById(R.id.view_pager);

        tabLayout = findViewById(R.id.tabs);

        setupAllTabs();

        //The following is used for tab behavior set up (from template)
        MyHomePagerAdapter adapter = new MyHomePagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

        adapter.setIs_Instructor(is_Instructor);

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void setupTab(String tabText) {
        TabLayout.Tab tab = tabLayout.newTab();
        tab.setText(tabText);
        tabLayout.addTab(tab);
    }

    private void setupAllTabs() {
        setupTab(getString(R.string.Quizzes));
        if (is_Instructor) {
            setupTab(getString(R.string.Class_Statistics));
        } else {
            setupTab(getString(R.string.LeaderBoard));
        }
        setupTab(getString(R.string.Profile));
    }

}