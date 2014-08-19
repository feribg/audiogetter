package com.github.feribg.audiogetter.ui.adapters;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.github.feribg.audiogetter.ui.fragments.SoundcloudSearchResultsFragment;
import com.github.feribg.audiogetter.ui.fragments.VimeoSearchResultsFragment;
import com.github.feribg.audiogetter.ui.fragments.YoutubeSearchResultsFragment;

public class SearchPagerAdapter extends FragmentPagerAdapter {

    public static final int PAGE_SOUNDCLOUD = 0;
    public static final int PAGE_YOUTUBE = 1;
    public static final int PAGE_VIMEO = 2;

    public static final String ARG_SEARCH_TERM = "search_term";
    private String searchTerm;
    private Fragment mCurrentFragment;


    public SearchPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        try {
            Fragment fragment;
            switch (i) {
                case PAGE_SOUNDCLOUD:
                    fragment = new SoundcloudSearchResultsFragment();
                    break;
                case PAGE_YOUTUBE:
                    fragment = new YoutubeSearchResultsFragment();
                    break;
                case PAGE_VIMEO:
                    fragment = new VimeoSearchResultsFragment();
                    break;
                default:
                    throw new Exception("Invalid page choice");
            }
            Bundle args = new Bundle();
            args.putString(ARG_SEARCH_TERM, searchTerm);
            fragment.setArguments(args);
            return fragment;
        } catch (Exception e) {
            return null;
        }

    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (mCurrentFragment != object) {
            mCurrentFragment = (Fragment) object;
        }
        super.setPrimaryItem(container, position, object);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title;
        switch (position) {
            case PAGE_SOUNDCLOUD:
                title = "SoundCloud";
                break;
            case PAGE_YOUTUBE:
                title = "YouTube";
                break;
            case PAGE_VIMEO:
                title = "Vimeo";
                break;
            default:
                title = "Unknown";
                break;
        }
        return title;
    }


    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }
}
