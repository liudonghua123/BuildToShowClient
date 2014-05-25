package com.buildingtoshow.client.navigationviewpagerliveo;

import android.content.Context;
import com.buildingtoshow.client.adapter.NavigationAdapter;
import com.buildingtoshow.client.adapter.NavigationItemAdapter;
import com.buildingtoshow.client.utils.Utils;
import com.buildingtoshow.client.R;

public class NavigationList {
	
	public static NavigationAdapter getNavigationAdapter(Context context){
		
		NavigationAdapter navigationAdapter = new NavigationAdapter(context);		
		String[] menuItems = context.getResources().getStringArray(R.array.nav_menu_items);
		
		for (int i = 0; i < menuItems.length; i++) {
			
			String title = menuItems[i];				
			NavigationItemAdapter itemNavigation;				
			itemNavigation = new NavigationItemAdapter(title, Utils.iconNavigation[i]);									
			navigationAdapter.addItem(itemNavigation);
		}		
		return navigationAdapter;			
	}	
}
