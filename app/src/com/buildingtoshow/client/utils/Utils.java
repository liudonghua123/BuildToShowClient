package com.buildingtoshow.client.utils;

import android.content.Context;
import android.location.Location;

import com.buildingtoshow.client.R;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Utils {

    private static DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private static DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String LATLNG_INNER_DELIMITER = ",";
    private static final String LATLNG_OUTTER_DELIMITER = ";";

	//Set all the navigation icons and always to set "zero 0" for the item is a category
	public static int[] iconNavigation = new int[] { 
		0, 0, 0, 0, 0, R.drawable.ic_action_settings, R.drawable.ic_action_about};	
	
	//get title of the item navigation
	public static String getTitleItem(Context context, int posicao){		
		String[] titulos = context.getResources().getStringArray(R.array.nav_menu_items);  
		return titulos[posicao];
	} 
	
	public static int[] colors = new int[] { 
		R.color.blue_dark, R.color.blue_dark, R.color.red_dark, R.color.red_light,
		R.color.green_dark, R.color.green_light, R.color.orange_dark, R.color.orange_light,
		R.color.purple_dark, R.color.purple_light};

    public static Location createLocation(double latitude, double longitude) {
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    public static String formatFloatNumber(double number) {
        return decimalFormat.format(number);
    }

    public static LatLng locationToLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public static Location latLngToLocation(LatLng latLng) {
        Location location =  new Location("");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        return location;
    }

    /**
     * px值向dip值转换
     * @see #dipToPx(android.content.Context, float)
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int pxToDip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * dip值向px值转换
     * @see #pxToDip(android.content.Context, float)
     *
     * @param context
     * @param dipValue
     * @return
     */
    public static int dipToPx(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 返回ISO8601格式的日期字符串("YYYY-MM-DD HH:MM:SS.SSS").
     *
     * @param date
     * @return
     */
    public static String formatDateTime(Date date) {
        return simpleDateFormat.format(date);
    }

    /**
     * 地址列表向字符串转换，以 Latitude,Longitude;Latitude,Longitude;... 为格式
     * @see #stringToLocations(String)
     *
     * @param locations
     * @return
     */
    public static String locationsToString(List<Location> locations) {
        double latitude, longitude;
        List<String> latLngPairs = new ArrayList<String>();
        for(Location location : locations) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            latLngPairs.add(String.format("%f%s%f", latitude, LATLNG_INNER_DELIMITER, longitude));
        }
//        // JDK 8 support this
//        String.join(":", latLngPairs);
        return join(latLngPairs, LATLNG_OUTTER_DELIMITER);
    }

    /**
     * 字符串转换向地址列表
     * @see #locationsToString(java.util.List)
     *
     * @param locationString
     * @return
     */
    public static List<Location> stringToLocations(String locationString) {
        Location location;
        List<Location> locations = new ArrayList<Location>();
        String[] locationStringArrays = locationString.split(LATLNG_OUTTER_DELIMITER);
        if(locationStringArrays == null || locationStringArrays.length == 0) {
            return locations;
        }
        for(String latLngString : locationStringArrays) {
            String[] latLngStringSplitted = latLngString.split(LATLNG_INNER_DELIMITER);
            location = new Location("");
            location.setLatitude(Double.parseDouble(latLngStringSplitted[0]));
            location.setLongitude(Double.parseDouble(latLngStringSplitted[1]));
            locations.add(location);
        }
        return locations;
    }

    public static String join(Collection<?> col, String delim) {
        StringBuilder sb = new StringBuilder();
        Iterator<?> iter = col.iterator();
        if (iter.hasNext())
            sb.append(iter.next().toString());
        while (iter.hasNext()) {
            sb.append(delim);
            sb.append(iter.next().toString());
        }
        return sb.toString();
    }
}
