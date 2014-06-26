package com.buildingtoshow.client.adapter;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.buildingtoshow.client.R;
import com.buildingtoshow.client.db.DBHelper;
import com.buildingtoshow.client.utils.Utils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liudonghua on 14-6-25.
 */
public class TraceHistoryCursorAdapter extends CursorAdapter {
    private List<Marker> mMarkers = new ArrayList<Marker>();
    private List<Polyline> mPolyLines = new ArrayList<Polyline>();

    public TraceHistoryCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // when the view will be created for first time,
        // we need to tell the adapters, how each item will look
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View retView = inflater.inflate(R.layout.trace_history_list_item, parent, false);

        return retView;
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        // here we are setting our data
        // that means, take the data from the cursor and put it in views

//        MapView mapView = (MapView) view.findViewById(R.id.trace_history_list_item_map);
        ImageView imageViewSnapshot = (ImageView) view.findViewById(R.id.trace_history_list_item_snapshot);
        TextView textViewDistance = (TextView) view.findViewById(R.id.trace_history_list_item_text_view_distance);
        TextView textViewTotalTime = (TextView) view.findViewById(R.id.trace_history_list_item_text_view_total_time);
        TextView textViewAverageSpeed = (TextView) view.findViewById(R.id.trace_history_list_item_text_view_average_speed);
        TextView textViewStartDatetime = (TextView) view.findViewById(R.id.trace_history_list_item_text_view_start_date_time);

        String snapshotPath = cursor.getString(cursor.getColumnIndex(DBHelper.TRACE_RECORD_SNAPSHOT_PATH));
        String distanceString = cursor.getString(cursor.getColumnIndex(DBHelper.TRACE_RECORD_DISTANCE));
        String totalTimeString = cursor.getString(cursor.getColumnIndex(DBHelper.TRACE_RECORD_TOTAL_TIME));

        imageViewSnapshot.setImageBitmap(Utils.getImageBitmapFromFilePath(snapshotPath));
        textViewDistance.setText(distanceString);
        textViewTotalTime.setText(totalTimeString);
        textViewAverageSpeed.setText(Utils.formatFloatNumber(Double.parseDouble(distanceString) / Double.parseDouble(totalTimeString)));
        textViewStartDatetime.setText(cursor.getString(cursor.getColumnIndex(DBHelper.TRACE_RECORD_START_DATETIME)));

//        cleanup();
//        // http://stackoverflow.com/questions/14909917/in-google-maps-v2-fragment-getmap-returns-null
//        // http://stackoverflow.com/questions/20689861/android-getmap-null-inside-dialogfragment
//        // The map is pretty complex and is initializing a lot of state on and off the render thread.
//        GoogleMap map = mapView.getMap();
//        if (map != null) {
//            // 绘制地图
//            String latLngString = cursor.getString(cursor.getColumnIndex(DBHelper.TRACE_RECORD_LOCATIONS));
//            List<Location> locations = Utils.stringToLocations(latLngString);
//            Marker startMarker = map.addMarker(new MarkerOptions()
//                    .position(Utils.locationToLatLng(locations.get(0)))
//                    .title("起点")
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//            Marker endMarker = map.addMarker(new MarkerOptions()
//                    .position(Utils.locationToLatLng(locations.get(locations.size() - 1)))
//                    .title("终点")
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
//            mMarkers.add(startMarker);
//            mMarkers.add(endMarker);
//            for (int i = 0; i < locations.size() - 1; i++) {
//                Polyline polyline = map.addPolyline(new PolylineOptions()
//                        .add(Utils.locationToLatLng(locations.get(i)))
//                        .add(Utils.locationToLatLng(locations.get(i + 1)))
//                        .width(5)
//                        .color(Color.BLUE)
//                        .geodesic(true));
//                mPolyLines.add(polyline);
//            }
//        }
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