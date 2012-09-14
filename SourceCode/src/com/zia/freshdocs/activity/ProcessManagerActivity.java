
package com.zia.freshdocs.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zia.freshdocs.R;

public class ProcessManagerActivity extends ListActivity {
	private final int CONVERT = 1024;
	
	private PackageManager mPackageManager;
	private List<RunningAppProcessInfo> mDisplayProcess;
	private ActivityManager mActivityManager;
	private MyListAdapter mDelegate;
	private TextView mAvailMemLabel, mNumProcLabel;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.manage_layout);		
		mPackageManager = getPackageManager();
		
		mAvailMemLabel = (TextView)findViewById(R.id.available_memory_label);
		mNumProcLabel = (TextView)findViewById(R.id.num_processes_label);
		mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		
		mDisplayProcess = new ArrayList<RunningAppProcessInfo>();
		updateList();
		
		mDelegate = new MyListAdapter();
		setListAdapter(mDelegate);
	}
	
	@Override
	protected void onListItemClick(ListView parent, View view, int position, long id) {
		AlertDialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		CharSequence[] options = {"Details", "Launch"};
		final int index = position;
		
		builder.setTitle("Process options");
		
		try {
			builder.setIcon(mPackageManager.getApplicationIcon(mDisplayProcess.get(position).processName));
			
		} catch (NameNotFoundException e) {
			builder.setIcon(R.drawable.processinfo);
		}
		
		builder.setItems(options, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int choice) {
				
				switch (choice) {				
					case 0:
						Toast.makeText(ProcessManagerActivity.this, mDisplayProcess.get(index).processName,
								   Toast.LENGTH_SHORT).show();
						break;
						
					case 1:
						Intent i = mPackageManager.getLaunchIntentForPackage(mDisplayProcess.get(index).processName);
						
						if(i != null)
							startActivity(i);
						else
							Toast.makeText(ProcessManagerActivity.this, "Could not launch", Toast.LENGTH_SHORT).show();	
						
						break;
				}
			}
		});
		
		dialog = builder.create();
		dialog.show();
	}
	
	/**
	 * 
	 */
	private void updateLabels() {
		MemoryInfo memoryInfo;
		double memorySize;
		
		memoryInfo = new ActivityManager.MemoryInfo();
		mActivityManager.getMemoryInfo(memoryInfo);
		memorySize = (memoryInfo.availMem / (CONVERT * CONVERT));		
		
		mAvailMemLabel.setText(String.format("Available memory:\t %.2f Mb", memorySize));
		mNumProcLabel.setText("Number of processes:\t " + mDisplayProcess.size());
	}
	
	private void updateList() {
		List<RunningAppProcessInfo> totalProcess = mActivityManager.getRunningAppProcesses();
		int lengh;
		
		totalProcess = mActivityManager.getRunningAppProcesses();
		lengh = totalProcess.size();
		
		for (int i = 0; i < lengh; i++){
			if(totalProcess.get(i).importance != RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
			    totalProcess.get(i).importance != RunningAppProcessInfo.IMPORTANCE_SERVICE)
				mDisplayProcess.add(totalProcess.get(i));
		}
		
		updateLabels();
	}
	
	/* (non-JavaDoc)
	 * private inner class to bind the listview and its data source
	 * @author Joe Berria
	 */
	private class MyListAdapter extends ArrayAdapter<RunningAppProcessInfo> {
		
		public MyListAdapter() {
			super(ProcessManagerActivity.this, R.layout.table_row, mDisplayProcess);
		}
		
		private String parseName(String pkgName) {
			String[] items = pkgName.split("\\.");
			String name = "";
			int len = items.length;
			
			for (int i = 0; i < len; i++){
				if(!items[i].equalsIgnoreCase("com") && !items[i].equalsIgnoreCase("android") &&
				   !items[i].equalsIgnoreCase("google") && !items[i].equalsIgnoreCase("process") &&
				   !items[i].equalsIgnoreCase("htc") && !items[i].equalsIgnoreCase("coremobility"))
					name = items[i];
			}		
			return name;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			String pkg_name = mDisplayProcess.get(position).processName;
				
			if(view == null) {
				LayoutInflater inflater = getLayoutInflater();
				view = inflater.inflate(R.layout.table_row, parent, false);
			}
				
			TextView bottomLabel = (TextView)view.findViewById(R.id.bottom_view);
			TextView topLabel = (TextView)view.findViewById(R.id.top_view);
			ImageView icon = (ImageView)view.findViewById(R.id.row_image);
			icon.setAdjustViewBounds(true);
			icon.setMaxHeight(50);
			
			topLabel.setText(parseName(pkg_name));
			bottomLabel.setText(String.format("%s, pid: %d",
							mDisplayProcess.get(position).processName, mDisplayProcess.get(position).pid));
			
			try {
				icon.setImageDrawable(mPackageManager.getApplicationIcon(pkg_name));
				
			} catch (NameNotFoundException e) {
				icon.setImageResource(R.drawable.processinfo);
			}
			
			return view;			
		}
	}
}
