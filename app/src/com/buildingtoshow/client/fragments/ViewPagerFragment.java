package com.buildingtoshow.client.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.buildingtoshow.client.R;
import com.buildingtoshow.client.adapter.ViewPagerAdapter;
import com.buildingtoshow.client.sliding.SamplePagerItem;
import com.buildingtoshow.client.sliding.SlidingTabLayout;
import com.buildingtoshow.client.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerFragment extends Fragment{
	private List<SamplePagerItem> mTabs = new ArrayList<SamplePagerItem>();
    private ViewPagerAdapter mViewPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTabs.add(new SamplePagerItem(0, getString(R.string.tab_one), getResources().getColor(Utils.colors[0]),  Color.GRAY));
        mTabs.add(new SamplePagerItem(1, getString(R.string.tab_two), getResources().getColor(Utils.colors[2]), Color.GRAY));
        mTabs.add(new SamplePagerItem(2, getString(R.string.tab_three), getResources().getColor(Utils.colors[4]), Color.GRAY));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.viewpager_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ViewPager mViewPager = (CustomViewPager) view.findViewById(R.id.mPager);
    	
    	mViewPager.setOffscreenPageLimit(3);
        mViewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), mTabs);
        mViewPager.setAdapter(mViewPagerAdapter);

        SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.mTabs);
        mSlidingTabLayout.setBackgroundResource(R.color.white);
        mSlidingTabLayout.setViewPager(mViewPager);

        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

            @Override
            public int getIndicatorColor(int position) {
                return mTabs.get(position).getIndicatorColor();
            }

            @Override
            public int getDividerColor(int position) {
                return mTabs.get(position).getDividerColor();
            }
        });

        mSlidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(position == 1) {
                    ((TraceHistoryFragment)(mViewPagerAdapter.getItem(position))).updateTraceHisotory();
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}