package com.buildingtoshow.client.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.buildingtoshow.client.R;
import com.buildingtoshow.client.db.TraceRecordHelper;
import com.buildingtoshow.client.utils.Menus;
import com.buildingtoshow.client.utils.Utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TraceMapFragment extends Fragment implements BDLocationListener, OnClickListener, MKOfflineMapListener, BaiduMap.OnMapStatusChangeListener {

    private static final String LOG_TAG = "TraceMapFragment";
    public static final int DEFAULT_ZOOM_LEVEL = 18;

    // 初始化为空的列表
    private List<BDLocation> mLocations = new ArrayList<BDLocation>();
    private List<Marker> mMarkers = new ArrayList<Marker>();
    private List<Polyline> mPolyLines = new ArrayList<Polyline>();
    private LocationManager mLocationManager;
    private String mProvider;
    private TraceRecordHelper mTraceRecordHelper;

    private boolean mIsStartTrace;
    private Date mStartDate;
    private BDLocation mLastLocation;
    private long mLastElapsedTimeInSeconds = 0;
    private long mCurrentElapsedTimeInSeconds = 0;
    private double mLastDistance = 0;
    private double mCurrentDistance = 0;
    private double mCurrentSpeed = 0;
    private boolean isFirstLoc = true;
    private boolean isRequestCurrentLocation = false;
    private float mCurrentMapZoom = DEFAULT_ZOOM_LEVEL;

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
    private BaiduMap mMap;
    private TextView mTextViewDistance;
    private TextView mTextViewElapsedTime;
    private TextView mTextViewSpeed;
    private ViewSwitcher mBottomViewSwitcher;
    private Button mButtonStart;
    private Button mButtonEnd;
    private ImageButton mImageButtonLocation;

    private LocationClient mLocationClient;

    private static final int ONE_SECOND_IN_MILIS = 1000;
    private MKOfflineMap mOffline;

    public static TraceMapFragment newInstance() {
        TraceMapFragment fragment = new TraceMapFragment();
        return fragment;
    }	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.trace_map, container, false);

        mMapView = (MapView) mRootView.findViewById(R.id.map);

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
        mImageButtonLocation = (ImageButton) mRootView.findViewById(R.id.trace_map_view_location);

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
        mImageButtonLocation.setOnClickListener(this);
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

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            MapView mapView = (MapView) mRootView.findViewById(R.id.map);
//            BaiduMapOptions mapOptions = new BaiduMapOptions();
//            // 隐藏比例尺控件
//            mapOptions.scaleControlEnabled(false);
//            // 隐藏缩放按钮
//            mapOptions.zoomControlsEnabled(false);
//            // Terrible MapView design, could not set BaiduMapOptions, unless create a new MapView
            hideZoomCtler();
            // use offline map
            mOffline = new MKOfflineMap();
            mOffline.init(this);
            mMap = mapView.getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    //隐藏 缩放控件和  百度logo
    // see http://blog.csdn.net/weizongwei5/article/details/39178243
    private void hideZoomCtler() {
        if(mMapView==null) {
            return;
        }
        int count = mMapView.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = mMapView.getChildAt(i);
            // 隐藏百度logo ZoomControl
            if (child instanceof ImageView || child instanceof ZoomControls) {
                child.setVisibility(View.GONE);
            }
        }
    }

    private void setUpMap() {
        // 普通地图
        //mMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        // 开启定位图层
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapStatusChangeListener(this);
        mMap.getUiSettings().setCompassEnabled(true);

        mLocationClient = new LocationClient(this.getActivity().getApplicationContext());
        mLocationClient.registerLocationListener(this);

        LocationClientOption locationOption = new LocationClientOption();
        // 打开gps
        locationOption.setOpenGps(true);
        // 设置坐标类型
        locationOption.setCoorType("bd09ll");
        locationOption.setScanSpan(1000);
        mLocationClient.setLocOption(locationOption);
        // 开启定位
        mLocationClient.start();
        mLocationClient.requestLocation();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Attempt to invoke virtual method 'java.util.ArrayList com.baidu.platform.comapi.map.q.e()' on a null object reference
                int count = mOffline.importOfflineData();
                ArrayList<MKOLUpdateElement> localMapList = mOffline.getAllUpdateInfo();
                Log.d(LOG_TAG, String.format("localMapList.size() returns %d", localMapList != null ? localMapList.size() : 0));
                boolean isKunmingMapAvailable = false;
                String kunmingCityName = "昆明";
                if(localMapList != null) {
                    for(MKOLUpdateElement localMap : localMapList) {
                        localMap.cityName.startsWith(kunmingCityName);
                        isKunmingMapAvailable = true;
                    }
                }
                if(!isKunmingMapAvailable) {
                    ArrayList<MKOLSearchRecord> records = mOffline.searchCity(kunmingCityName);
                    if (records != null && records.size() > 0) {
                        mOffline.start(records.get(0).cityID);
                    }
                }
                Log.d(LOG_TAG, String.format("mOffline.importOfflineData() returns %d", count));
            }
        }, 1000);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        // 退出时销毁定位
        mLocationClient.stop();
        // 关闭定位图层
        mMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        mOffline.destroy();
        super.onDestroy();
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        if (location == null || mMapView == null) {
            return;
        }
        // 构造定位数据
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                .direction(100)
                .latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
        // 设置定位数据
        mMap.setMyLocationData(locData);
        if (isFirstLoc || isRequestCurrentLocation) {
            isFirstLoc = false;
            isRequestCurrentLocation = false;
            LatLng latLng = new LatLng(location.getLatitude(),
                    location.getLongitude());
            MapStatusUpdate statusUpdate = MapStatusUpdateFactory.newLatLngZoom(latLng, mCurrentMapZoom);
            mMap.animateMapStatus(statusUpdate);
        }

        if(!mIsStartTrace) {
            return;
        }

        // 首次获取到起点位置
        if(mLastLocation == null) {
            mLastLocation = location;
            mLocations.add(location);
            // 绘制起点
            Marker startMarker = (Marker) mMap.addOverlay(new MarkerOptions()
                    .position(Utils.locationToLatLng(location))
                    .title("起点")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_st)));
            mMarkers.add(startMarker);
        }
        else {
            double distanceElapsed = DistanceUtil. getDistance(new LatLng(location.getLatitude(), location.getLongitude()),
                    new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            if(distanceElapsed > 10) {
                mLocations.add(location);
                // 计算当前速度和距离
                mCurrentDistance += distanceElapsed;
                mCurrentSpeed = distanceElapsed / (mCurrentElapsedTimeInSeconds - mLastElapsedTimeInSeconds);

                // 绘制线条
                List<LatLng> points = new ArrayList<LatLng>();
                points.add(Utils.locationToLatLng(mLastLocation));
                points.add(Utils.locationToLatLng(location));
                Polyline polyline = (Polyline) mMap.addOverlay(new PolylineOptions().points(points)
                        .width(5)
                        .color(Color.BLUE));
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.trace_map_button_start:
                mIsStartTrace = true;
                mStartDate = Calendar.getInstance(Locale.getDefault()).getTime();
                // 重置
                reset();
                // 清理
                cleanup();
                // 显示停止按钮
                mBottomViewSwitcher.showNext();
                // 启动计时，记录位置
                mHandler.post(timerTaskRunable);
                break;
            case R.id.trace_map_button_end:
                mIsStartTrace = false;
                // 显示开始按钮
                mBottomViewSwitcher.showPrevious();
                // 停止计时，记录位置
                mHandler.removeCallbacks(timerTaskRunable);
                // 绘制终点
                Marker endMarker = (Marker) mMap.addOverlay(new MarkerOptions()
                        .position(Utils.locationToLatLng(mLastLocation))
                        .title("终点")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_en)));
                mMarkers.add(endMarker);

                // 保存记录
                showProbablySaveDialog();
                break;
            case R.id.trace_map_view_location:
                if(mLocationClient.isStarted()) {
                    mLocationClient.requestLocation();
                    isRequestCurrentLocation = true;
                }
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
                mMap.snapshot(new BaiduMap.SnapshotReadyCallback() {

                    @Override
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
                mTraceRecordHelper = TraceRecordHelper.getInstance(TraceMapFragment.this.getActivity());
                mTraceRecordHelper.insert(mStartDate, (int) mCurrentDistance, (int) mCurrentElapsedTimeInSeconds, mLocations, snapshotFilePath);
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

    @Override
    public void onGetOfflineMapState(int type, int state) {
        switch (type) {
            case MKOfflineMap.TYPE_DOWNLOAD_UPDATE:
                MKOLUpdateElement update = mOffline.getUpdateInfo(state);
                if(update != null) {
                    Log.d(LOG_TAG, String.format("%s : %d%%", update.cityName,
                            update.ratio));
                }
                break;
            case MKOfflineMap.TYPE_NEW_OFFLINE:
                Log.d(LOG_TAG, String.format("add offlinemap num:%d", state));
                break;
            case MKOfflineMap.TYPE_VER_UPDATE:
                Log.d(LOG_TAG, String.format("new offlinemap ver"));
                break;
        }

    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChange(MapStatus mapStatus) {
        mCurrentMapZoom = mapStatus.zoom;
    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {

    }
}


