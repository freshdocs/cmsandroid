
package com.zia.freshdocs.widget.fileexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zia.freshdocs.R;
import com.zia.freshdocs.activity.AndGuru;
import com.zia.freshdocs.activity.ApplicationBackupActivity;
import com.zia.freshdocs.activity.DirectoryInfoActivity;
import com.zia.freshdocs.activity.ProcessManagerActivity;
import com.zia.freshdocs.activity.WirelessManagerActivity;
import com.zia.freshdocs.widget.fileexplorer.CustomFastScrollView.SectionIndexer;
import com.zia.freshdocs.widget.fileexplorer.carousel.CarouselAdapter;
import com.zia.freshdocs.widget.fileexplorer.carousel.CarouselAdapter.OnItemClickListener;
import com.zia.freshdocs.widget.quickaction.ActionItem;
import com.zia.freshdocs.widget.quickaction.QuickAction;

/**
 * This class sits between the Main activity and the FileManager class. 
 * To keep the FileManager class modular, this class exists to handle 
 * UI events and communicate that information to the FileManger class
 * 
 * This class is responsible for the buttons onClick method. If one needs
 * to change the functionality of the buttons found from the Main activity
 * or add button logic, this is the class that will need to be edited.
 * 
 * This class is responsible for handling the information that is displayed
 * from the list view (the files and folder) with a a nested class TableRow.
 * The TableRow class is responsible for displaying which icon is shown for each
 * entry. For example a folder will display the folder icon, a Word doc will 
 * display a word icon and so on. If more icons are to be added, the TableRow 
 * class must be updated to display those changes. 
 * 
 */
public class EventHandler implements OnClickListener , OnItemClickListener {
	/*
	 * Unique types to control which file operation gets
	 * performed in the background
	 */
    private static final int SEARCH_TYPE =		0x00;
	private static final int COPY_TYPE =		0x01;
	private static final int UNZIP_TYPE =		0x02;
	private static final int UNZIPTO_TYPE =		0x03;
	private static final int ZIP_TYPE =			0x04;
	private static final int DELETE_TYPE = 		0x05;
//	private static final int MANAGE_DIALOG =	 0x06;
	
	private final Context mContext;
	private final FileManager mFileManager;
	private TableRow mDelegate;
	private boolean multi_select_flag = false;
	private boolean delete_after_copy = false;
	private boolean thumbnail_flag = true;
	private int mColor = Color.WHITE;
	//the list used to feed info into the array adapter and when multi-select is on
	private ArrayList<String> mDataSource, mMultiSelectData;
	private TextView mPathLabel;
	private TextView mInfoLabel;
	
	private static final int ATTACH_FILE =		0x00;
	private static final int DELETE_FILE =		0x01;
	private static final int COPY_FILE =		0x02;
	private static final int MOVE_FILE =		0x03;
	
	
	
	/**
	 * Creates an EventHandler object. This object is used to communicate
	 * most work from the Main activity to the FileManager class.
	 * 
	 * @param context	The context of the main activity e.g  Main
	 * @param manager	The FileManager object that was instantiated from Main
	 */
	public EventHandler(Context context, final FileManager manager) {
		mContext = context;
		mFileManager = manager;
		
		mDataSource = new ArrayList<String>(mFileManager.getHomeDir());
	}

	/**
	 * This method is called from the Main activity and this has the same
	 * reference to the same object so when changes are made here or there
	 * they will display in the same way.
	 * 
	 * @param adapter	The TableRow object
	 */
	public void setListAdapter(TableRow adapter) {
		mDelegate = adapter;
	}
	
	/**
	 * This method is called from the Main activity and is passed
	 * the TextView that should be updated as the directory changes
	 * so the user knows which folder they are in.
	 * 
	 * @param path	The label to update as the directory changes
	 * @param label	the label to update information
	 */
	public void setUpdateLabels(TextView path, TextView label) {
		mPathLabel = path;
		mInfoLabel = label;
	}
	
	/**
	 * 
	 * @param color
	 */
	public void setTextColor(int color) {
		mColor = color;
	}
	
	/**
	 * Set this true and thumbnails will be used as the icon for image files. False will
	 * show a default image. 
	 * 
	 * @param show
	 */
	public void setShowThumbnails(boolean show) {
		thumbnail_flag = show;
	}
	
	/**
	 * If you want to move a file (cut/paste) and not just copy/paste use this method to 
	 * tell the file manager to delete the old reference of the file.
	 * 
	 * @param delete true if you want to move a file, false to copy the file
	 */
	public void setDeleteAfterCopy(boolean delete) {
		delete_after_copy = delete;
	}
	
	/**
	 * Indicates whether the user wants to select 
	 * multiple files or folders at a time.
	 * <br><br>
	 * false by default
	 * 
	 * @return	true if the user has turned on multi selection
	 */
	public boolean isMultiSelected() {
		return multi_select_flag;
	}
	
	/**
	 * Use this method to determine if the user has selected multiple files/folders
	 * 
	 * @return	returns true if the user is holding multiple objects (multi-select)
	 */
	public boolean hasMultiSelectData() {
		return (mMultiSelectData != null && mMultiSelectData.size() > 0);
	}
	
	/**
	 * Will search for a file then display all files with the 
	 * search parameter in its name
	 * 
	 * @param name	the name to search for
	 */
	public void searchForFile(String name) {
		new BackgroundWork(SEARCH_TYPE).execute(name);
	}
	
	/**
	 * Will delete the file name that is passed on a background
	 * thread.
	 * 
	 * @param name
	 */
	public void deleteFile(String name) {
		new BackgroundWork(DELETE_TYPE).execute(name);
	}
	
	/**
	 * Will copy a file or folder to another location.
	 * 
	 * @param oldLocation	from location
	 * @param newLocation	to location
	 */
	public void copyFile(String oldLocation, String newLocation) {
		String[] data = {oldLocation, newLocation};
		new BackgroundWork(COPY_TYPE).execute(data);
	}
	
	/**
	 * 
	 * @param newLocation
	 */
	public void copyFileMultiSelect(String newLocation) {
		String[] data;
		int index = 1;
		
		if (mMultiSelectData.size() > 0) {
			data = new String[mMultiSelectData.size() + 1];
			data[0] = newLocation;
			
			for(String s : mMultiSelectData)
				data[index++] = s;
			
			new BackgroundWork(COPY_TYPE).execute(data);
		}
	}
	
	/**
	 * This will extract a zip file to the same directory.
	 * 
	 * @param file	the zip file name
	 * @param path	the path were the zip file will be extracted (the current directory)
	 */
	public void unZipFile(String file, String path) {
		new BackgroundWork(UNZIP_TYPE).execute(file, path);
	}
	
	/**
	 * This method will take a zip file and extract it to another
	 * location
	 *  
	 * @param name		the name of the of the new file (the dir name is used)
	 * @param newDir	the dir where to extract to
	 * @param oldDir	the dir where the zip file is
	 */
	public void unZipFileToDir(String name, String newDir, String oldDir) {
		new BackgroundWork(UNZIPTO_TYPE).execute(name, newDir, oldDir);
	}
	
	/**
	 * Creates a zip file
	 * 
	 * @param zipPath	the path to the directory you want to zip
	 */
	public void zipFile(String zipPath) {
		new BackgroundWork(ZIP_TYPE).execute(zipPath);
	}

	/**
	 *  This method, handles the button presses of the top buttons found
	 *  in the Main activity. 
	 */
	@Override
	public void onClick(View v) {
		hideOpitionMenu();
		switch(v.getId()) {
			case R.id.btnBack:			
				if (mFileManager.getCurrentDir() != "/") {
					if(multi_select_flag) {
						mDelegate.killMultiSelect(true);
						Toast.makeText(mContext, "Multi-select is now off", 
									   Toast.LENGTH_SHORT).show();
					}
					updateDirectory(mFileManager.getPreviousDir());
					if(mPathLabel != null)
						mPathLabel.setText(mFileManager.getCurrentDir());
				}
				break;
			
			case R.id.btnHome:		
				if(multi_select_flag) {
					mDelegate.killMultiSelect(true);
					Toast.makeText(mContext, "Multi-select is now off", Toast.LENGTH_SHORT).show();
				}
				updateDirectory(mFileManager.getHomeDir());
				if(mPathLabel != null)
					mPathLabel.setText(mFileManager.getCurrentDir());
				break;
				
			case R.id.btnInfo:
				Intent info = new Intent(mContext, DirectoryInfoActivity.class);
				info.putExtra("PATH_NAME", mFileManager.getCurrentDir());
				mContext.startActivity(info);
				break;
				
			case R.id.btnManage:
//				display_dialog(MANAGE_DIALOG);
				// Quick aciton for Tool
				final QuickAction qa = new QuickAction(v);
				
		        final ActionItem first = new ActionItem();
				first.setTitle("Process Infomation");
				first.setIcon(mContext.getResources().getDrawable(R.drawable.process));
				first.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(mContext, ProcessManagerActivity.class);
						mContext.startActivity(i);
						qa.dismiss();
					}
				});
				
				
				final ActionItem second = new ActionItem();
				second.setTitle("Wifi Infomation");
				second.setIcon(mContext.getResources().getDrawable(R.drawable.wifiinfo));
				second.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(mContext, WirelessManagerActivity.class);
						mContext.startActivity(i);
						qa.dismiss();
					}
				});
				
				final ActionItem third = new ActionItem();
				third.setTitle("Application backup");
				third.setIcon(mContext.getResources().getDrawable(R.drawable.appbackup));
				third.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(mContext, ApplicationBackupActivity.class);
						mContext.startActivity(i);
						qa.dismiss();
					}
				});
				
				qa.addActionItem(first);
				qa.addActionItem(second);
				qa.addActionItem(third);
				qa.setAnimStyle(QuickAction.ANIM_REFLECT);
				
				qa.show();
				break;
				
			case R.id.btnMultiselect:			
				if(multi_select_flag) {
					mDelegate.killMultiSelect(true);	
					Toast.makeText(mContext, "Multi-select is now off", Toast.LENGTH_SHORT).show();
				} else {
					multi_select_flag = true;
					Toast.makeText(mContext, "Multi-select is now on", Toast.LENGTH_SHORT).show();
					LinearLayout hidden_lay = (LinearLayout)((Activity) mContext).findViewById(R.id.hidden_buttons);
					
					hidden_lay.setVisibility(LinearLayout.VISIBLE);
				}
				break;
			
		}
	}
	
	/**
	 * will return the data in the ArrayList that holds the dir contents. 
	 * 
	 * @param position	the index of the arraylist holding the dir content
	 * @return the data in the arraylist at position (position)
	 */
	public String getData(int position) {
		
		if(position > mDataSource.size() - 1 || position < 0){
			return null;
		}
		
		return mDataSource.get(position);
	}

	/**
	 * called to update the file contents as the user navigates there
	 * phones file system. 
	 * 
	 * @param content	an ArrayList of the file/folders in the current directory.
	 */
	public void updateDirectory(ArrayList<String> content) {	
		if(!mDataSource.isEmpty() || mDataSource.size() != 0){
			mDataSource.clear();
		}
		
		for(String data : content){
			mDataSource.add(data);
		}
			
		mDelegate.notifyDataSetChanged();
	}

	private static class ViewHolder {
		TextView topView;
		TextView bottomView;
		ImageView icon;
		ImageView mSelect;	//multi-select check mark icon
	}

	
	/**
	 * A nested class to handle displaying a custom view in the ListView that
	 * is used in the Main activity. If any icons are to be added, they must
	 * be implemented in the getView method. This class is instantiated once in Main
	 * and has no reason to be instantiated again. 
	 * 
	 */
    public class TableRow extends ArrayAdapter<String> implements SectionIndexer {
    	private final int KB = 1024;
    	private final int MG = KB * KB;
    	private final int GB = MG * KB;
    	private String display_size;
    	private String dir_name = "/sdcard";
    	private ArrayList<Integer> positions;
    	private LinearLayout hidden_layout;
    	private ThumbnailCreator thumbnail;
    	private HashMap<String, Integer> alphaIndexer;
    	private String[] sections;
    	
    	public TableRow() {
    		super(mContext, R.layout.table_row, mDataSource);
    		thumbnail = new ThumbnailCreator(32, 32);
    		alphaIndexer = new HashMap<String, Integer>(); 	
    		int size = mDataSource.size();
            for (int i =0 ; i < size ; i++) {
            	String element = mDataSource.get(i);
            	String firstChar = element.substring(0, 1).toUpperCase();
                alphaIndexer.put(firstChar, i);
            }
            
            Set<String> keys = alphaIndexer.keySet();
            Iterator<String> it = keys.iterator();
            ArrayList<String> keyList = new ArrayList<String>();
            while (it.hasNext()) {
                keyList.add(it.next());
            }
            Collections.sort(keyList);
            sections = new String[keyList.size()];
            keyList.toArray(sections);
    	}
    	
    	@Override
    	public void notifyDataSetChanged() {
    		int size = mDataSource.size();
            for (int i =0 ; i < size ; i++) {
            	String element = mDataSource.get(i);
            	String firstChar = element.substring(0, 1).toUpperCase();
                alphaIndexer.put(firstChar, i);
            } 
            Set<String> keys = alphaIndexer.keySet();
            Iterator<String> it = keys.iterator();
            ArrayList<String> keyList = new ArrayList<String>();
            while (it.hasNext()) {
                String key = it.next();
                keyList.add(key);
            }
            Collections.sort(keyList);
            sections = new String[keyList.size()];
            keyList.toArray(sections);
    		super.notifyDataSetChanged();
    	}
    	
		@Override
		public int getPositionForSection(int section) {
			try {
				String letter = sections[section];
	            return alphaIndexer.get(letter);
			} catch (IndexOutOfBoundsException e) {
				return 0;
			}
			
		}

		@Override
		public int getSectionForPosition(int position) {
			int prevIndex = 0;
	        for(int i = 0; i < sections.length; i++)
	        {
	            if(getPositionForSection(i) > position && prevIndex <= position)
	            {
	                prevIndex = i;
	                break;
	            }
	            prevIndex = i;
	        }
	        return prevIndex;
		}

		@Override
		public Object[] getSections() {
			return sections;
		}
    	
    	public void addMultiPosition(int index, String path) {
    		if(positions == null){
    			positions = new ArrayList<Integer>();
    		}
    		
    		if(mMultiSelectData == null) {
    			positions.add(index);
    			add_multiSelect_file(path);
    			
    		} else if(mMultiSelectData.contains(path)) {
    			if(positions.contains(index))
    				positions.remove(new Integer(index));
    			
    			mMultiSelectData.remove(path);
    			
    		} else {
    			positions.add(index);
    			add_multiSelect_file(path);
    		}
    		
    		notifyDataSetChanged();
    	}
   	
    	/**
    	 * This will turn off multi-select and hide the multi-select buttons at the
    	 * bottom of the view. 
    	 * 
    	 * @param clearData if this is true any files/folders the user selected for multi-select
    	 * 					will be cleared. If false, the data will be kept for later use. Note:
    	 * 					multi-select copy and move will usually be the only one to pass false, 
    	 * 					so we can later paste it to another folder.
    	 */
    	public void killMultiSelect(boolean clearData) {
    		hidden_layout = (LinearLayout)((Activity)mContext).findViewById(R.id.hidden_buttons);
    		hidden_layout.setVisibility(LinearLayout.GONE);
    		multi_select_flag = false;
    		
    		if(positions != null && !positions.isEmpty())
    			positions.clear();
    		
    		if(clearData)
    			if(mMultiSelectData != null && !mMultiSelectData.isEmpty())
    				mMultiSelectData.clear();
    		
    		notifyDataSetChanged();
    	}
    	
    	public String getFilePermissions(File file) {
    		String per = "-";
    	    		
    		if(file.isDirectory())
    			per += "d";
    		if(file.canRead())
    			per += "r";
    		if(file.canWrite())
    			per += "w";
    		
    		return per;
    	}
    	
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		hideOpitionMenu();
    		ViewHolder holder;
    		int num_items = 0;
    		String temp = mFileManager.getCurrentDir();
    		String filePath = mFileManager.getCurrentDir() + "/" + mDataSource.get(position); 
    		File file = new File(filePath);
    		String[] list = file.list();
    		
    		if(list != null){
    			num_items = list.length;
    		}
    			
    		if(convertView == null) {
    			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    			convertView = inflater.inflate(R.layout.table_row, parent, false);
    			holder = new ViewHolder();
    			holder.topView = (TextView)convertView.findViewById(R.id.top_view);
    			holder.bottomView = (TextView)convertView.findViewById(R.id.bottom_view);
    			holder.icon = (ImageView)convertView.findViewById(R.id.row_image);
    			holder.mSelect = (ImageView)convertView.findViewById(R.id.multiselect_icon);
    			convertView.setTag(holder);
    			
    		} else {
    			holder = (ViewHolder)convertView.getTag();
    		}
    		
    		/* This will check if the thumbnail cache needs to be cleared by checking
    		 * if the user has changed directories. This way the cache wont show
    		 * a wrong thumbnail image for the new image file 
    		 */
    		if(!dir_name.equals(temp) && thumbnail_flag) {
    			thumbnail.clearBitmapCache();
    			dir_name = temp;
    		}    			
    		  		
    		if (positions != null && positions.contains(position)){
    			holder.mSelect.setVisibility(ImageView.VISIBLE);
    		}else{
    			holder.mSelect.setVisibility(ImageView.GONE);
    		}
    			

    		holder.topView.setTextColor(mColor);
    		holder.bottomView.setTextColor(mColor);
    		
    		if(file != null && file.isFile()) {
    			String ext = file.toString();
    			
    			/* This series of elifse if statements will determine which 
    			 * icon is displayed 
    			 */
    			
    			if(ext.endsWith("apk")){
    				PackageManager packageManager = getContext().getPackageManager();
					PackageInfo packageInfo = packageManager.getPackageArchiveInfo(ext, PackageManager.GET_ACTIVITIES);
					if(packageInfo != null){
						ApplicationInfo  appInfo = packageInfo.applicationInfo;
						appInfo.sourceDir = ext;
						appInfo.publicSourceDir = ext;
						if(appInfo != null){
		    				if(appInfo.icon != 0 ){
		    					try {
		    						Drawable icon = packageManager.getApplicationIcon(appInfo);
//		    						Drawable icon = packageManager.getResourcesForApplication(appInfo).getDrawable(appInfo.icon);
		    						if(icon != null){
		    							holder.icon.setImageDrawable(icon);
		    						}else{
		    							holder.icon.setImageResource(R.drawable.apk);
		    						}
		    					} catch (Exception e) {
		    						e.printStackTrace();
		    						holder.icon.setImageResource(R.drawable.apk);
		    					} 
		    				}
						}
					}
				}else{
    				holder.icon.setImageResource(IconStore.getIconByExtendsion(ext));
    			}
    		
    		} else if (file != null && file.isDirectory()) {
    			holder.icon.setImageResource(R.drawable.folder);
    		}
    		    		
    		String permission = getFilePermissions(file);
    		
    		if(file.isFile()) {   // Is file
    			double size = file.length();
        		if (size > GB){
        			display_size = String.format("%.2f Gb ", (double)size / GB);
        		}else if (size < GB && size > MG){
        			display_size = String.format("%.2f Mb ", (double)size / MG);
        		}else if (size < MG && size > KB){
        			display_size = String.format("%.2f Kb ", (double)size/ KB);
        		}else{
    				display_size = String.format("%.2f bytes ", (double)size);
    			}
    				
        		Date date  = new Date(file.lastModified());
        		String lastModified = String.valueOf(date.getYear() + 1900) 
										+ "-"  + String.valueOf(Convert(date.getMonth() + 1))
										+ "-"  + String.valueOf(Convert(date.getDate()))
										+ " "  + String.valueOf(Convert(date.getHours()))
										+ ":"  + String.valueOf(Convert(date.getMinutes()));
        		
        		if(file.isHidden()){
        			holder.bottomView.setText("(hidden) | " + lastModified +" | " + display_size +" | "+ permission);
        		}else{
        			holder.bottomView.setText(lastModified + " | " + display_size +" | "+ permission);
        		}
    		} else {    // is folder
   			
    			if(file.isHidden()){
    				holder.bottomView.setText("(hidden) | " + num_items +" items | " + permission);
    			}else{
    				holder.bottomView.setText(+ num_items +" items | " + permission);
    			}
    		}
    		holder.topView.setText(file.getName());
    		return convertView;
    	}
    	
    	private void add_multiSelect_file(String src) {
    		if(mMultiSelectData == null){
    			mMultiSelectData = new ArrayList<String>();
    		}
    		mMultiSelectData.add(src);
    	}
    }

    
    
    private String Convert(int number){
        String temp;
        if(number >= 10){
        	temp = "" + number;
        }else{
        	temp = "0" + number;
        } 
        return temp;
    }
    
    /**
     * A private inner class of EventHandler used to perform time extensive 
     * operations. So the user does not think the the application has hung, 
     * operations such as copy/past, search, unzip and zip will all be performed 
     * in the background. This class extends AsyncTask in order to give the user
     * a progress dialog to show that the app is working properly.
     * 
     * (note): this class will eventually be changed from using AsyncTask to using
     * Handlers and messages to perform background operations. 
     * 
     * @author Joe Berria
     */
    private class BackgroundWork extends AsyncTask<String, Void, ArrayList<String>> {
    	private String file_name;
    	private ProgressDialog pr_dialog;
    	private int type;
    	private int copy_rtn;
    	
    	private BackgroundWork(int type) {
    		this.type = type;
    	}
    	
    	/**
    	 * This is done on the EDT thread. this is called before 
    	 * doInBackground is called
    	 */
    	@Override
    	protected void onPreExecute() {
    		
    		switch(type) {
    			case SEARCH_TYPE:
    				pr_dialog = ProgressDialog.show(mContext, "Searching", 
    												"Searching current file system...",
    												true, true);
    				break;
    				
    			case COPY_TYPE:
    				pr_dialog = ProgressDialog.show(mContext, "Copying", 
    												"Copying file...", 
    												true, false);
    				break;
    				
    			case UNZIP_TYPE:
    				pr_dialog = ProgressDialog.show(mContext, "Unzipping", 
    												"Unpacking zip file please wait...",
    												true, false);
    				break;
    				
    			case UNZIPTO_TYPE:
    				pr_dialog = ProgressDialog.show(mContext, "Unzipping", 
    												"Unpacking zip file please wait...",
    												true, false);
    				break;
    			
    			case ZIP_TYPE:
    				pr_dialog = ProgressDialog.show(mContext, "Zipping", 
    												"Zipping folder...", 
    												true, false);
    				break;
    				
    			case DELETE_TYPE:
    				pr_dialog = ProgressDialog.show(mContext, "Deleting", 
    												"Deleting files...", 
    												true, false);
    				break;
    		}
    	}

    	/**
    	 * background thread here
    	 */
    	@Override
		protected ArrayList<String> doInBackground(String... params) {
			
			switch(type) {
				case SEARCH_TYPE:
					file_name = params[0];
					ArrayList<String> found = mFileManager.searchInDirectory(mFileManager.getCurrentDir(),file_name);
					return found;
					
				case COPY_TYPE:
					int len = params.length;
					
					if(mMultiSelectData != null && !mMultiSelectData.isEmpty()) {
						for(int i = 1; i < len; i++) {
							copy_rtn = mFileManager.copyToDirectory(params[i], params[0]);
							if(delete_after_copy){
								mFileManager.deleteTarget(params[i]);
							}
						}
					} else {
						copy_rtn = mFileManager.copyToDirectory(params[0], params[1]);
						if(delete_after_copy){
							mFileManager.deleteTarget(params[0]);
						}
					}
					delete_after_copy = false;
					return null;
					
				case UNZIP_TYPE:
					mFileManager.extractZipFiles(params[0], params[1]);
					return null;
					
				case UNZIPTO_TYPE:
					mFileManager.extractZipFilesFromDir(params[0], params[1], params[2]);
					return null;
					
				case ZIP_TYPE:
					mFileManager.createZipFile(params[0]);
					return null;
					
				case DELETE_TYPE:
					int size = params.length;
					for(int i = 0; i < size; i++){
						mFileManager.deleteTarget(params[i]);
					}
					return null;
			}
			return null;
		}
		
    	/**
    	 * This is called when the background thread is finished. Like onPreExecute, anything
    	 * here will be done on the EDT thread. 
    	 */
    	@Override
		protected void onPostExecute(final ArrayList<String> file) {		
    		
    		hideOpitionMenu();
			final CharSequence[] names;
			int len = file != null ? file.size() : 0;
			
			switch(type) {
				case SEARCH_TYPE:				
					if(len == 0) {
						Toast.makeText(mContext, "Couldn't find " + file_name, Toast.LENGTH_SHORT).show();
					} else {
						names = new CharSequence[len];
						for (int i = 0; i < len; i++) {
							String entry = file.get(i);
							names[i] = entry.substring(entry.lastIndexOf("/") + 1, entry.length());
						}
						
						AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
						builder.setTitle("Found " + len + " file(s)");
						builder.setItems(names, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int position) {
								String path = file.get(position);
								updateDirectory(mFileManager.getNextDir(path.substring(0, path.lastIndexOf("/")), true));
							}
						});
						
						AlertDialog dialog = builder.create();
						dialog.show();
					}
					
					pr_dialog.dismiss();
					break;
					
				case COPY_TYPE:
					if(mMultiSelectData != null && !mMultiSelectData.isEmpty()) {
						multi_select_flag = false;
						mMultiSelectData.clear();
					}
					
					if(copy_rtn == 0){
						Toast.makeText(mContext, "File successfully copied and pasted", 
								Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(mContext, "Copy pasted failed", Toast.LENGTH_SHORT).show();
					}
						
					pr_dialog.dismiss();
					mInfoLabel.setText("");
					break;
					
				case UNZIP_TYPE:
					updateDirectory(mFileManager.getNextDir(mFileManager.getCurrentDir(), true));
					pr_dialog.dismiss();
					break;
					
				case UNZIPTO_TYPE:
					updateDirectory(mFileManager.getNextDir(mFileManager.getCurrentDir(), true));
					pr_dialog.dismiss();
					break;
					
				case ZIP_TYPE:
					updateDirectory(mFileManager.getNextDir(mFileManager.getCurrentDir(), true));
					pr_dialog.dismiss();
					break;
					
				case DELETE_TYPE:
					if(mMultiSelectData != null && !mMultiSelectData.isEmpty()) {
						mMultiSelectData.clear();
						multi_select_flag = false;
					}
					
					updateDirectory(mFileManager.getNextDir(mFileManager.getCurrentDir(), true));
					pr_dialog.dismiss();
					mInfoLabel.setText("");
					break;
			}
		}
    }



	@Override
	public void onItemClick(CarouselAdapter<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		if(position == ATTACH_FILE){
			/* check if user selected objects before going further */
			if(mMultiSelectData == null || mMultiSelectData.isEmpty()) {
				mDelegate.killMultiSelect(true);
				Toast.makeText(mContext, "You must select at least one item before continuing", Toast.LENGTH_SHORT).show();
			}else{
				ArrayList<Uri> uris = new ArrayList<Uri>();
				int length = mMultiSelectData.size();
				Intent mailIntent = new Intent();
				
				mailIntent.setAction(android.content.Intent.ACTION_SEND_MULTIPLE);
				mailIntent.setType("application/mail");
				
				for(int i = 0; i < length; i++) {
					File file = new File(mMultiSelectData.get(i));
					uris.add(Uri.fromFile(file));
				}
				
				mailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
				mContext.startActivity(Intent.createChooser(mailIntent, "Email using..."));
				
				mDelegate.killMultiSelect(true);
			}
		}
		if(position == MOVE_FILE || position == COPY_FILE){
			if(mMultiSelectData == null || mMultiSelectData.isEmpty()) {
				mDelegate.killMultiSelect(true);
				Toast.makeText(mContext, "You must select at least one item before continuing", Toast.LENGTH_SHORT).show();
			}else{
				if(position == MOVE_FILE){
					delete_after_copy = true;
				}
				mInfoLabel.setVisibility(View.VISIBLE);	
				mInfoLabel.setText("Holding " + mMultiSelectData.size() + " file(s)");
				mDelegate.killMultiSelect(false);
			}

		}
		if(position == DELETE_FILE){
			if(mMultiSelectData == null || mMultiSelectData.isEmpty()) {
				mDelegate.killMultiSelect(true);
				Toast.makeText(mContext, "You must select at least one item before continuing", Toast.LENGTH_SHORT).show();
			}else{
				final String[] data = new String[mMultiSelectData.size()];
				int at = 0;
				
				for(String string : mMultiSelectData){
					data[at++] = string;
				}
				
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setMessage("Are you sure you want to delete " +
								    data.length + " files? This cannot be undone.");
				builder.setCancelable(false);
				builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new BackgroundWork(DELETE_TYPE).execute(data);
						mDelegate.killMultiSelect(true);
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mDelegate.killMultiSelect(true);
						dialog.cancel();
					}
				});
				
				builder.create().show();
			}
		}
	}
	private void hideOpitionMenu(){
		if (AndGuru.mMenu.isShowing()) {
			AndGuru.mMenu.hide();
		}
	}
}
