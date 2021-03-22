package com.audioplayer.sparklesv10.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.audioplayer.sparklesv10.R;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        viewPager = (ViewPager) rootView.findViewById(R.id.viewPager);

        setUpViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        return rootView;
    }

    private void setUpViewPager (ViewPager viewPager) {
        FragmentAdapter adapter = new FragmentAdapter(getActivity().getSupportFragmentManager());
        adapter.AddFragments(new SongFragment(), "Songs");
        adapter.AddFragments(new AlbumFragment(), "Album");
        adapter.AddFragments(new ArtistFragment(), "Artist");
        viewPager.setAdapter(adapter);
    }

    public static class FragmentAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragmentList = new ArrayList<>();
        private List<String> titleList = new ArrayList<>();

        public FragmentAdapter (@NonNull FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void AddFragments(Fragment fragment, String title) {
            fragmentList.add(fragment);
            titleList.add(title);
        }

        @NonNull
        @Override
        public CharSequence getPageTitle(int position){
            return titleList.get(position);
        }
    }
}