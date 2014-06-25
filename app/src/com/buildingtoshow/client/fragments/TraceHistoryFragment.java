package com.buildingtoshow.client.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.buildingtoshow.client.R;
import com.buildingtoshow.client.adapter.TraceHistoryCursorAdapter;
import com.buildingtoshow.client.db.TraceRecordHelper;
import com.buildingtoshow.client.utils.Menus;;
import com.twotoasters.jazzylistview.JazzyHelper;
import com.twotoasters.jazzylistview.JazzyListView;

public class TraceHistoryFragment extends Fragment{

    private View mRootView;
	private JazzyListView mListViewTraceHistory;
    private TraceHistoryCursorAdapter mTraceHistoryCursorAdapter;
    private TraceRecordHelper mTraceRecordHelper;
	
    public static TraceHistoryFragment newInstance() {
        Bundle bundle = new Bundle();

        TraceHistoryFragment fragment = new TraceHistoryFragment();
        fragment.setArguments(bundle);

        return fragment;
    }	
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.trace_history_list, container, false);

        mTraceRecordHelper = TraceRecordHelper.getInstance(getActivity());
        mListViewTraceHistory = (JazzyListView) mRootView.findViewById(R.id.trace_history_list_view);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mTraceHistoryCursorAdapter = new TraceHistoryCursorAdapter(TraceHistoryFragment.this.getActivity(), mTraceRecordHelper.query());
                mListViewTraceHistory.setAdapter(mTraceHistoryCursorAdapter);
            }
        });

        mRootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT ));
		return mRootView;
	}
				
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
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
    }

    public void updateTraceHisotory() {
        // http://stackoverflow.com/questions/1985955/android-simplecursoradapter-doesnt-update-when-database-changes
        // use Loader instead
        mTraceHistoryCursorAdapter.getCursor().requery();
        mTraceHistoryCursorAdapter.notifyDataSetChanged();
    }
}


