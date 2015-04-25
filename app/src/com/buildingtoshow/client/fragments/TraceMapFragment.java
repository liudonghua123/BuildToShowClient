package com.buildingtoshow.client.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.buildingtoshow.client.R;
import com.buildingtoshow.client.db.TraceRecordHelper;
import com.buildingtoshow.client.utils.Menus;
import com.buildingtoshow.client.utils.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//import com.baidu.mapapi.map.BaiduMap;
//import com.baidu.mapapi.map.MapView;
//import com.buildingtoshow.client.R;
//import com.buildingtoshow.client.utils.Menus;

public class TraceMapFragment extends Fragment implements LocationListener, OnClickListener {

    private static final String LOG_TAG = "TraceMapFragment";

    // 初始化为空的列表
    private List<Location> mLocations = new ArrayList<Location>();
    private List<Marker> mMarkers = new ArrayList<Marker>();
    private List<Polyline> mPolyLines = new ArrayList<Polyline>();
    private LocationManager mLocationManager;
    private String mProvider;
    private TraceRecordHelper mTraceRecordHelper;

    private boolean mIsStartTrace;
    private Date mStartDate;
    private Location mLastLocation;
    private long mLastElapsedTimeInSeconds = 0;
    private long mCurrentElapsedTimeInSeconds = 0;
    private float mLastDistance = 0;
    private float mCurrentDistance = 0;
    private float mCurrentSpeed = 0;

    // 用作计时器 http://www.52rd.com/Blog/Detail_RD.Blog_jimbo_lee_49769.html
    private Handler mHandler = new Handler();
    private Runnable timerTaskRunable = new Runnable(){
        @Override
        public void run() {
            mCurrentElapsedTimeInSeconds++;
            // 更新消逝时间UI
            mTextViewElapsedTime.setText(Long.toString(mCurrentElapsedTimeInSeconds));
            mHandler.postDelayed(this, ONE_SECOND_IN_MILIS);
        }
    };

    private View mRootView;
    private MapView mMapView;
    private GoogleMap mMap;
    //private BaiduMap mMap;
    private TextView mTextViewDistance;
    private TextView mTextViewElapsedTime;
    private TextView mTextViewSpeed;
    private ViewSwitcher mBottomViewSwitcher;
    private Button mButtonStart;
    private Button mButtonEnd;

    private static final int ONE_SECOND_IN_MILIS = 1000;

    public static TraceMapFragment newInstance() {
        TraceMapFragment fragment = new TraceMapFragment();
        return fragment;
    }	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.trace_map, container, false);

        mMapView = (MapView) mRootView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        setUpMapIfNeeded();

        setUpNoneMapViews();

        mRootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		return mRootView;
	}

    private void setUpNoneMapViews() {
        mTextViewDistance = (TextView) mRootView.findViewById(R.id.trace_map_text_view_distance);
        mTextViewElapsedTime = (TextView) mRootView.findViewById(R.id.trace_map_text_view_elapsed_time);
        mTextViewSpeed = (TextView) mRootView.findViewById(R.id.trace_map_text_view_speed);

        mBottomViewSwitcher = (ViewSwitcher) mRootView.findViewById(R.id.trace_map_view_switcher_bottom);
        mButtonStart = (Button) mRootView.findViewById(R.id.trace_map_button_start);
        mButtonEnd = (Button) mRootView.findViewById(R.id.trace_map_button_end);

        // Set Animation
        // Can also set in XML using android:inAnimation and android:outAnimation
        // load the two animations
        Animation animIn = AnimationUtils.loadAnimation(this.getActivity(), R.anim.slide_in_top);
        Animation animOut = AnimationUtils.loadAnimation(this.getActivity(), R.anim.slide_out_bottom);
        // set them on the ViewSwitcher
        mBottomViewSwitcher.setInAnimation(animIn);
        mBottomViewSwitcher.setOutAnimation(animOut);

        mButtonStart.setOnClickListener(this);
        mButtonEnd.setOnClickListener(this);
    }

    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);		
		inflater.inflate(R.menu.menu, menu);
					    	   	    
		menu.findItem(Menus.ADD).setVisible(false);
		menu.findItem(Menus.UPDATE).setVisible(false);		
		menu.findItem(Menus.SEARCH).setVisible(true);	
		
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(Menus.SEARCH));
	    searchView.setQueryHint(this.getString(R.string.search));
	    
	    ((EditText)searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text))
        .setHintTextColor(getResources().getColor(R.color.white));		
	}

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            // http://stackoverflow.com/questions/19541915/google-maps-cameraupdatefactory-not-initalized
            // do this in Application
            //MapsInitializer.initialize(getActivity());

            mMap = ((MapView) mRootView.findViewById(R.id.map)).getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
//        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.setPadding(0, 0, 0, 150);
        mMap.setMyLocationEnabled(true);

        // set LocationListener
        // Getting LocationManager object from System Service LOCATION_SERVICE
        mLocationManager = (LocationManager) this.getActivity().getSystemService(this.getActivity().LOCATION_SERVICE);
        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

//		  // 查询精度：高
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);
//        // 是否查询海拨：否
//        criteria.setAltitudeRequired(false);
//        // 是否查询方位角 : 否
//        criteria.setBearingRequired(false);
//        // 是否允许付费：是
//        criteria.setCostAllowed(true);
//        // 电量要求：低
//        criteria.setPowerRequirement(Criteria.POWER_LOW);

        // // 返回最合适的符合条件的 provider ，第 2 个参数为 true 说明 , 如果只有一个 provider 是有效的 , 则返回当前的
        // 可能的返回值有LocationManager.NETWORK_PROVIDER or LocationManager.GPS_PROVIDER
        // http://www.cnblogs.com/transmuse/archive/2010/12/31/1923358.html
        mProvider = mLocationManager.getBestProvider(criteria, true);
        mLocationManager.requestLocationUpdates(mProvider, 0, 0, this);

    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(!mIsStartTrace) {
            return;
        }
        // Getting latitude of the current location
        double latitude = location.getLatitude();
        // Getting longitude of the current location
        double longitude = location.getLongitude();
        // Creating a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);
        // Showing the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        // 首次获取到起点位置
        if(mLastLocation == null) {
            mLastLocation = location;
            mLocations.add(location);
            // 绘制起点
            // https://developers.google.com/maps/documentation/android/marker
            Marker startMarker = mMap.addMarker(new MarkerOptions()
                    .position(Utils.locationToLatLng(location))
                    .title("起点")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            mMarkers.add(startMarker);
        }
        else {
            float distanceElapsed = location.distanceTo(mLastLocation);
            if(distanceElapsed > 10) {
                mLocations.add(location);
                // 计算当前速度和距离
                mCurrentDistance += distanceElapsed;
                mCurrentSpeed = distanceElapsed / (mCurrentElapsedTimeInSeconds - mLastElapsedTimeInSeconds);

                // 绘制线条
                // https://developers.google.com/maps/documentation/android/shapes
                Polyline polyline = mMap.addPolyline(new PolylineOptions()
                        .add(Utils.locationToLatLng(mLastLocation))
                        .add(Utils.locationToLatLng(location))
                        .width(5)
                        .color(Color.BLUE)
                        .geodesic(true));
                mPolyLines.add(polyline);

                mLastDistance = mCurrentDistance;
                mLastElapsedTimeInSeconds = mCurrentElapsedTimeInSeconds;
                mLastLocation = location;

                // 更新UI
                mTextViewDistance.setText(Integer.toString((int) mCurrentDistance));
                mTextViewSpeed.setText(Utils.formatFloatNumber(mCurrentSpeed));
            }

        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.trace_map_button_start:
                mIsStartTrace = true;
//                Time currentTime = new Time(Time.getCurrentTimezone());
//                currentTime.setToNow();
                mStartDate = Calendar.getInstance(Locale.getDefault()).getTime();
                // 重置
                reset();
                // 清理
                cleanup();
                // 显示停止按钮
                mBottomViewSwitcher.showNext();
                // 启动计时，记录位置
                mHandler.post(timerTaskRunable);
//                // 绘制起点
//                mLastLocation = mLocationManager.getLastKnownLocation(mProvider);
//                mLocations.add(mLastLocation);
//                // https://developers.google.com/maps/documentation/android/marker
//                Marker startMarker = mMap.addMarker(new MarkerOptions()
//                        .position(Utils.locationToLatLng(mLastLocation))
//                        .title("起点")
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//                mMarkers.add(startMarker);
                break;
            case R.id.trace_map_button_end:
                mIsStartTrace = false;
                // 显示开始按钮
                mBottomViewSwitcher.showPrevious();
                // 停止计时，记录位置
                mHandler.removeCallbacks(timerTaskRunable);
                // 绘制终点
                Marker endMarker = mMap.addMarker(new MarkerOptions()
                        .position(Utils.locationToLatLng(mLastLocation))
                        .title("终点")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
                mMarkers.add(endMarker);

                // 保存记录
                showProbablySaveDialog();
                break;
        }
    }

    private void showProbablySaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TraceMapFragment.this.getActivity());
        builder.setMessage(R.string.trace_map_need_save);
        builder.setTitle(R.string.trace_map_need_save);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 保存地图快照至图片
                final String snapshotFilePath = Utils.generateMapSnapshotFilePath(TraceMapFragment.this.getActivity());
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    public void onMapLoaded() {
                        mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                            public void onSnapshotReady(Bitmap bitmap) {
                                // Write image to disk
                                FileOutputStream out = null;
                                try {
                                    out = new FileOutputStream(snapshotFilePath);
                                } catch (FileNotFoundException e) {
                                    Log.e(LOG_TAG, e.getMessage());
                                }
                                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                            }
                        });
                    }
                });
                mTraceRecordHelper = TraceRecordHelper.getInstance(TraceMapFragment.this.getActivity());
                mTraceRecordHelper.insert(mStartDate, (int)mCurrentDistance, (int)mCurrentElapsedTimeInSeconds, mLocations, snapshotFilePath);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void reset() {
        mCurrentDistance = 0;
        mLastDistance = 0;
        mCurrentElapsedTimeInSeconds = 0;
        mLastElapsedTimeInSeconds = 0;
        mLocations.clear();
        mLastLocation = null;
    }

    private void cleanup() {
        for(Marker marker : mMarkers) {
            marker.remove();
        }
        for(Polyline polyline : mPolyLines) {
            polyline.remove();
        }
    }
}


