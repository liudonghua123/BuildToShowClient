package com.buildingtoshow.client;

import android.app.Application;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.MapsInitializer;
//import com.baidu.mapapi.SDKInitializer;
/**
 * Created by liudonghua on 14-6-23.
 */
public class BuildingToShowApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //SDKInitializer.initialize(this);
//        try {
            MapsInitializer.initialize(this);
//        } catch (GooglePlayServicesNotAvailableException e) {
//            Toast.makeText(getApplicationContext(), "GooglePlayServicesNotAvailableException: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }

        switch (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) )
        {
            case ConnectionResult.SUCCESS:
                Toast.makeText(getApplicationContext(), "SUCCESS", Toast.LENGTH_SHORT).show();
                break;
            case ConnectionResult.SERVICE_MISSING:
                Toast.makeText(getApplicationContext(), "SERVICE MISSING", Toast.LENGTH_SHORT).show();
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                Toast.makeText(getApplicationContext(), "UPDATE REQUIRED", Toast.LENGTH_SHORT).show();
                break;
            default: Toast.makeText(getApplicationContext(), GooglePlayServicesUtil.isGooglePlayServicesAvailable(this), Toast.LENGTH_SHORT).show();
        }
    }
}
