package com.buildingtoshow.client.fragments;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.baidu.mapapi.map.MapView;

/**
 * Created by Liu.D.H on 4/26/2015.
 * @see http://stackoverflow.com/questions/12117791/gesture-issue-with-mapview-in-viewpager-page
 */
public class CustomViewPager  extends ViewPager {

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if(v instanceof MapView){
            return true;
        }
        return super.canScroll(v, checkV, dx, x, y);
    }

}