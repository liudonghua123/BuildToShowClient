package com.buildingtoshow.client;

import android.app.Application;
import com.baidu.mapapi.SDKInitializer;
/**
 * Created by liudonghua on 14-6-23.
 */
public class BuildingToShowApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
    }
}
