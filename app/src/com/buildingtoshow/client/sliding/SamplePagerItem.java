package com.buildingtoshow.client.sliding;

import android.support.v4.app.Fragment;

import com.buildingtoshow.client.fragments.TraceFriendFragment;
import com.buildingtoshow.client.fragments.TraceMapFragment;
import com.buildingtoshow.client.fragments.TraceHistoryFragment;

public class SamplePagerItem {
	
	private final CharSequence mTitle;
    private final int mIndicatorColor;
    private final int mDividerColor;
    private final int position;
        
    private Fragment[] listFragments;
    public SamplePagerItem(int position, CharSequence title, int indicatorColor, int dividerColor) {
        this.mTitle = title;
        this.position = position;
        this.mIndicatorColor = indicatorColor;
        this.mDividerColor = dividerColor;
        
        listFragments = new Fragment[] {TraceMapFragment.newInstance(), TraceHistoryFragment.newInstance(), TraceFriendFragment.newInstance()};
    }

    public Fragment createFragment() {
		return listFragments[position];
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public int getIndicatorColor() {
        return mIndicatorColor;
    }

    public int getDividerColor() {
        return mDividerColor;
    }
}
