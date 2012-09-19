package com.zia.freshdocs.activity;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zia.freshdocs.R;
import com.zia.freshdocs.model.Constants;
import com.zia.freshdocs.widget.fileexplorer.CustomFastScrollView;
import com.zia.freshdocs.widget.fileexplorer.EventHandler;
import com.zia.freshdocs.widget.fileexplorer.FileManager;
import com.zia.freshdocs.widget.fileexplorer.carousel.Carousel;
import com.zia.freshdocs.widget.fileexplorer.custommenu.CustomMenu;
import com.zia.freshdocs.widget.fileexplorer.custommenu.CustomMenu.OnMenuItemSelectedListener;
import com.zia.freshdocs.widget.fileexplorer.custommenu.CustomMenuItem;
import com.zia.freshdocs.widget.quickaction.QuickActionWindow;

public final class FileExplorerActivity extends ListActivity implements OnMenuItemSelectedListener, OnItemLongClickListener {
	private static final String PREFS_NAME = "ManagerPrefsFile"; // user
																	// preference
																	// file name
	private static final String PREFS_HIDDEN = "hidden";
	private static final String PREFS_COLOR = "color";
	private static final String PREFS_THUMBNAIL = "thumbnail";
	private static final String PREFS_SORT = "sort";
	private static final String PREFS_STORAGE = "sdcard space";

	private static final int MENU_MKDIR = 0x00;
	private static final int MENU_SETTING = 0x01;
	private static final int MENU_SEARCH = 0x02;
	private static final int MENU_QUIT = 0x03;
	private static final int SEARCH_BUTTON = 0x09;

	private static final int FOLDER_MENU_RENAME = 0x06;
	private static final int FILE_MENU_RENAME = 0x0b;
	private static final int SETTING_REQ = 0x10; // request code for intent
	private FileManager mFileManager;
	private EventHandler mHandler;
	private EventHandler.TableRow mTable;

	private SharedPreferences mSettings;
	private boolean mReturnIntent = false;
	private boolean mHoldingFile = false;
	private boolean mHoldingZip = false;
	private boolean mUseBackKey = true;
	private String mCopiedTarget;
	private String mZippedTarget;
	private String mSelectedListItem; // item from context menu
	private TextView mPathLabel, mDetailLabel, mStorageLabel;
	public static CustomMenu mMenu;
	private boolean isFolderMove, isFileMove;
	private CustomFastScrollView mFastScrollView;
	private boolean isIntentGetContent;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setTheme(R.style.Theme_HoloEverywhereLight);
		super.onCreate(savedInstanceState);
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) { // Check Sdcard
			if (android.os.Build.VERSION.SDK_INT != 11) {
				requestWindowFeature(Window.FEATURE_NO_TITLE);
			}

			setContentView(R.layout.file_explore);
			
			Intent intent = getIntent();
			isIntentGetContent = intent.getBooleanExtra(Constants.UPLOAD, false);
			
			// initialize the custom menu
			mMenu = new CustomMenu(this, this, getLayoutInflater());
			mMenu.setHideOnSelect(true);
			mMenu.setItemsPerLineInPortraitOrientation(4);
			mMenu.setItemsPerLineInLandscapeOrientation(8);
			// load the menu items
			loadMenuItems();

			/* read settings */
			mSettings = getSharedPreferences(PREFS_NAME, 0);
			boolean hide = mSettings.getBoolean(PREFS_HIDDEN, false);
			boolean thumb = mSettings.getBoolean(PREFS_THUMBNAIL, true);
			int space = mSettings.getInt(PREFS_STORAGE, View.VISIBLE);
			int color = mSettings.getInt(PREFS_COLOR, Color.BLACK);
			int sort = mSettings.getInt(PREFS_SORT, 1);

			mFileManager = new FileManager();
			mFileManager.setShowHiddenFiles(hide);
			mFileManager.setSortType(sort);

			mHandler = new EventHandler(FileExplorerActivity.this, mFileManager);
			mHandler.setTextColor(color);
			mHandler.setShowThumbnails(thumb);
			mTable = mHandler.new TableRow();
			/*
			 * sets the ListAdapter for our ListActivity and gives our
			 * EventHandler class the same adapter
			 */
			mHandler.setListAdapter(mTable);
			setListAdapter(mTable);
			/* register context menu for our list view */
//			getListView().setFastScrollEnabled(true);
			getListView().setOnItemLongClickListener(this);

			mStorageLabel = (TextView) findViewById(R.id.storage_label);
			mDetailLabel = (TextView) findViewById(R.id.detail_label);
			mPathLabel = (TextView) findViewById(R.id.path_label);
			mPathLabel.setText("Path: "	+ Environment.getExternalStorageDirectory().getPath());

			updateStorageLabel();
			mStorageLabel.setVisibility(space);

			mHandler.setUpdateLabels(mPathLabel, mDetailLabel);

			/* setup buttons */
			int[] imgBtnId = { R.id.btnBack, R.id.btnHome, R.id.btnInfo, R.id.btnManage, R.id.btnMultiselect };

			ImageButton[] imgBtn = new ImageButton[imgBtnId.length];

			for (int i = 0; i < imgBtnId.length; i++) {
				imgBtn[i] = (ImageButton) findViewById(imgBtnId[i]);
				imgBtn[i].setOnClickListener(mHandler);
			}
			
			String tempString = getIntent().getAction();

			if (tempString != null && tempString.equals(Intent.ACTION_GET_CONTENT)) {
				imgBtn[4].setVisibility(View.GONE);
				mReturnIntent = true;
			}

			/* setup Carousel */
			Carousel carousel = (Carousel) findViewById(R.id.carousel);
			carousel.setOnItemClickListener(mHandler);
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.str_no_sdcard))
					.setCancelable(false)
					.setPositiveButton(getString(R.string.str_exit),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,	int id) {
									finish();
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
		}

	}

	/**
	 * (non Java-Doc) Returns the file that was selected to the intent that
	 * called this activity. usually from the caller is another application.
	 */
	private void returnIntentResults(File data) {
		mReturnIntent = false;
		Intent returnIntent = new Intent();
		returnIntent.setData(Uri.fromFile(data));
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	private void updateStorageLabel() {
		long total, aval;
		int kb = 1024;

		StatFs fs = new StatFs(Environment.getExternalStorageDirectory().getPath());

		total = fs.getBlockCount() * (fs.getBlockSize() / kb);
		aval = fs.getAvailableBlocks() * (fs.getBlockSize() / kb);

		mStorageLabel.setText(String.format("Sdcard: Total %.2f GB "
				+ "\t\tAvailable %.2f GB", (double) total / (kb * kb),
				(double) aval / (kb * kb)));
	}

	/**
	 * To add more functionality and let the user interact with more file types,
	 * this is the function to add the ability.
	 * 
	 * (note): this method can be done more efficiently
	 */
	
	@Override
	public void onListItemClick(ListView parent, View view, int position,long id) {
//		parent.setBackgroundResource(R.drawable.hide);
		final String item = mHandler.getData(position);
		boolean multiSelect = mHandler.isMultiSelected();
		File file = new File(mFileManager.getCurrentDir() + "/" + item);

		// File type
		String itemExt = null;

		try {
			itemExt = item.substring(item.lastIndexOf("."), item.length());
		} catch (IndexOutOfBoundsException e) {
			itemExt = "";
		}

		/*
		 * If the user has multi-select on, we just need to record the file not
		 * make an intent for it.
		 */
		if (multiSelect) {
			mTable.addMultiPosition(position, file.getPath());
		} else {
			if (file.isDirectory()) {
				if (file.canRead()) {
//					getListView().setFastScrollEnabled(false);mFastScrollView
					if(mFastScrollView!=null){
						mFastScrollView.stop();
						mFastScrollView = null;
					}
					mHandler.updateDirectory(mFileManager.getNextDir(item,false));
					mTable.notifyDataSetChanged();
//					getListView().setFastScrollEnabled(true);
					if(mFastScrollView==null){
						mFastScrollView = new CustomFastScrollView(this);
					}
					boolean FLAG_THUMB_PLUS = false;
				    ListView view1 = getListView();
				    if (view1.getWidth() <= 0){
				    	 return;
				    }
				    int newWidth = FLAG_THUMB_PLUS ? view1.getWidth() - 1 : view1.getWidth() + 1;
				    ViewGroup.LayoutParams params = view1.getLayoutParams();
				    params.width = newWidth;
				    view1.setLayoutParams( params );
				    FLAG_THUMB_PLUS = !FLAG_THUMB_PLUS;

					mPathLabel.setText("Path: " + mFileManager.getCurrentDir());

					/*
					 * set back button switch to true (this will be better
					 * implemented later)
					 */

					if (!mUseBackKey) {
						mUseBackKey = true;
					}

				} else {
					Toast.makeText(this, R.string.str_cant_read_folder,	Toast.LENGTH_SHORT).show();
				}
			}else if(isIntentGetContent){
				// Otherwise, return the URI of the selected file
				final Intent data = new Intent();
				data.setData(Uri.fromFile(file));
				setResult(RESULT_OK, data);
				finish();
			}

			/* music file selected--add more audio formats */
			else if (itemExt.equalsIgnoreCase(".mp3")
					|| itemExt.equalsIgnoreCase(".m4a")) {

				if (mReturnIntent) {
					returnIntentResults(file);
				} else {
					Intent music_int = new Intent(this,AudioPlayblackActivity.class);
					music_int.putExtra("MUSIC PATH",mFileManager.getCurrentDir() + "/" + item);
					startActivity(music_int);
				}
			}

			/* photo file selected */
			else if (itemExt.equalsIgnoreCase(".jpeg")
					|| itemExt.equalsIgnoreCase(".jpg")
					|| itemExt.equalsIgnoreCase(".png")
					|| itemExt.equalsIgnoreCase(".gif")
					|| itemExt.equalsIgnoreCase(".bmp")
					|| itemExt.equalsIgnoreCase(".tiff")) {

				if (file.exists()) {
					if (mReturnIntent) {
						returnIntentResults(file);

					} else {
						Intent pictureIntent = new Intent();
						pictureIntent.setAction(android.content.Intent.ACTION_VIEW);
						pictureIntent.setDataAndType(Uri.fromFile(file),"image/*");
						startActivity(pictureIntent);
					}
				}
			}

			/* video file selected--add more video formats */
			else if (itemExt.equalsIgnoreCase(".m4v")
					|| itemExt.equalsIgnoreCase(".3gp")
					|| itemExt.equalsIgnoreCase(".wmv")
					|| itemExt.equalsIgnoreCase(".mp4")
					|| itemExt.equalsIgnoreCase(".ogg")
					|| itemExt.equalsIgnoreCase(".wav")) {

				if (file.exists()) {
					if (mReturnIntent) {
						returnIntentResults(file);
					} else {
						Intent movieIntent = new Intent();
						movieIntent.setAction(android.content.Intent.ACTION_VIEW);
						movieIntent.setDataAndType(Uri.fromFile(file),"video/*");
						startActivity(movieIntent);
					}
				}
			}

			/* zip file */
			else if (itemExt.equalsIgnoreCase(".zip")) {

				if (mReturnIntent) {
					returnIntentResults(file);

				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					AlertDialog alert;
					mZippedTarget = mFileManager.getCurrentDir() + "/" + item;
					CharSequence[] option = { "Extract here", "Extract to..." };

					builder.setTitle("Extract");
					builder.setItems(option,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									switch (which) {
									case 0:
										String dir = mFileManager.getCurrentDir();
										mHandler.unZipFile(item, dir + "/");
										break;

									case 1:
										mDetailLabel.setText("Holding " + item	+ " to extract");
										mHoldingZip = true;
										break;
									}
								}
							});

					alert = builder.create();
					alert.show();
				}
			}

			/* gzip files, this will be implemented later */
			else if (itemExt.equalsIgnoreCase(".gzip")
					|| itemExt.equalsIgnoreCase(".gz")) {

				if (mReturnIntent) {
					returnIntentResults(file);

				} else {
					// TODO:
				}
			}

			/* pdf file selected */
			else if (itemExt.equalsIgnoreCase(".pdf")) {

				if (file.exists()) {
					if (mReturnIntent) {
						returnIntentResults(file);

					} else {
						Intent pdfIntent = new Intent();
						pdfIntent.setAction(android.content.Intent.ACTION_VIEW);
						pdfIntent.setDataAndType(Uri.fromFile(file),"application/pdf");

						try {
							startActivity(pdfIntent);
						} catch (ActivityNotFoundException e) {
							Toast.makeText(this, R.string.str_coundnt_find_fdf_viewer,Toast.LENGTH_SHORT).show();
						}
					}
				}
			}

			/* Android application file */
			else if (itemExt.equalsIgnoreCase(".apk")) {

				if (file.exists()) {
					if (mReturnIntent) {
						returnIntentResults(file);

					} else {
						Intent apkIntent = new Intent();
						apkIntent.setAction(android.content.Intent.ACTION_VIEW);
						apkIntent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
						startActivity(apkIntent);
					}
				}
			}

			/* HTML file */
			else if (itemExt.equalsIgnoreCase(".html")) {

				if (file.exists()) {
					if (mReturnIntent) {
						returnIntentResults(file);

					} else {
						Intent htmlIntent = new Intent();
						htmlIntent.setAction(android.content.Intent.ACTION_VIEW);
						htmlIntent.setDataAndType(Uri.fromFile(file),"text/html");

						try {
							startActivity(htmlIntent);
						} catch (ActivityNotFoundException e) {
							Toast.makeText(this,R.string.str_coundnt_find_html_viewer,Toast.LENGTH_SHORT).show();
						}
					}
				}
			}

			/* text file */
			else if (itemExt.equalsIgnoreCase(".txt")) {

				if (file.exists()) {
					if (mReturnIntent) {
						returnIntentResults(file);

					} else {
						Intent txtIntent = new Intent();
						txtIntent.setAction(android.content.Intent.ACTION_VIEW);
						txtIntent.setDataAndType(Uri.fromFile(file),"text/plain");

						try {
							startActivity(txtIntent);
						} catch (ActivityNotFoundException e) {
							txtIntent.setType("text/*");
							startActivity(txtIntent);
						}
					}
				}
			}

			/* generic intent */
			else {
				if (file.exists()) {
					if (mReturnIntent) {
						returnIntentResults(file);

					} else {
						Intent generic = new Intent();
						generic.setAction(android.content.Intent.ACTION_VIEW);
						generic.setDataAndType(Uri.fromFile(file), "text/plain");

						try {
							startActivity(generic);
						} catch (ActivityNotFoundException e) {
							Toast.makeText(this,R.string.str_coundnt_find_to_open + file.getName(), Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		SharedPreferences.Editor editor = mSettings.edit();
		boolean check;
		boolean thumbnail;
		int color, sort, space;

		/*
		 * resultCode must equal RESULT_CANCELED because the only way out of
		 * that activity is pressing the back button on the phone this publishes
		 * a canceled result code not an ok result code
		 */
		if (requestCode == SETTING_REQ && resultCode == RESULT_CANCELED) {
			// save the information we get from settings activity
			check = data.getBooleanExtra("HIDDEN", false);
			thumbnail = data.getBooleanExtra("THUMBNAIL", true);
			color = data.getIntExtra("COLOR", Color.BLACK);
			sort = data.getIntExtra("SORT", 0);
			space = data.getIntExtra("SPACE", View.VISIBLE);

			editor.putBoolean(PREFS_HIDDEN, check);
			editor.putBoolean(PREFS_THUMBNAIL, thumbnail);
			editor.putInt(PREFS_COLOR, color);
			editor.putInt(PREFS_SORT, sort);
			editor.putInt(PREFS_STORAGE, space);
			editor.commit();

			mFileManager.setShowHiddenFiles(check);
			mFileManager.setSortType(sort);
			mHandler.setTextColor(color);
			mHandler.setShowThumbnails(thumbnail);
			mStorageLabel.setVisibility(space);
			mHandler.updateDirectory(mFileManager.getNextDir(mFileManager.getCurrentDir(), true));
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		final Dialog dialog = new Dialog(FileExplorerActivity.this);

		switch (id) {
		case MENU_MKDIR:
			dialog.setContentView(R.layout.input_layout);
			dialog.setTitle(R.string.str_create_folder);
			dialog.setCancelable(false);

			ImageView icon = (ImageView) dialog.findViewById(R.id.input_icon);
			icon.setImageResource(R.drawable.newfolder);

			TextView label = (TextView) dialog.findViewById(R.id.input_label);
			label.setText(mFileManager.getCurrentDir());
			final EditText input = (EditText) dialog
					.findViewById(R.id.input_inputText);

			Button cancel = (Button) dialog.findViewById(R.id.input_cancel_b);
			Button create = (Button) dialog.findViewById(R.id.input_create_b);

			create.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (input.getText().length() > 1) {
						if (mFileManager.createDir(mFileManager.getCurrentDir()
								+ "/", input.getText().toString()) == 0)
							Toast.makeText(
									FileExplorerActivity.this,
									"Folder " + input.getText().toString()
											+ " created", Toast.LENGTH_LONG)
									.show();
						else
							Toast.makeText(FileExplorerActivity.this,R.string.str_cant_create_folder,Toast.LENGTH_SHORT).show();
					}

					dialog.dismiss();
					String temp = mFileManager.getCurrentDir();
					mHandler.updateDirectory(mFileManager.getNextDir(temp, true));
				}
			});
			cancel.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			break;
		case FOLDER_MENU_RENAME:
		case FILE_MENU_RENAME:
			dialog.setContentView(R.layout.input_layout);
			dialog.setTitle("Rename " + mSelectedListItem);
			dialog.setCancelable(false);

			ImageView rename_icon = (ImageView) dialog.findViewById(R.id.input_icon);
			rename_icon.setImageResource(R.drawable.rename);

			TextView rename_label = (TextView) dialog.findViewById(R.id.input_label);
			rename_label.setText(mFileManager.getCurrentDir());
			final EditText rename_input = (EditText) dialog.findViewById(R.id.input_inputText);

			Button rename_cancel = (Button) dialog.findViewById(R.id.input_cancel_b);
			Button rename_create = (Button) dialog.findViewById(R.id.input_create_b);
			rename_create.setText("Rename");

			rename_create.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (rename_input.getText().length() < 1)
						dialog.dismiss();

					if (mFileManager.renameTarget(mFileManager.getCurrentDir()	+ "/" + mSelectedListItem, rename_input.getText().toString()) == 0) {
						Toast.makeText(	FileExplorerActivity.this,mSelectedListItem + " was renamed to "	+ rename_input.getText().toString(),
								Toast.LENGTH_LONG).show();
					} else
						Toast.makeText(FileExplorerActivity.this,mSelectedListItem + " was not renamed",	Toast.LENGTH_LONG).show();

					dialog.dismiss();
					String temp = mFileManager.getCurrentDir();
					mHandler.updateDirectory(mFileManager
							.getNextDir(temp, true));
				}
			});
			rename_cancel.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			break;

		case SEARCH_BUTTON:
		case MENU_SEARCH:
			dialog.setContentView(R.layout.input_layout);
			dialog.setTitle("Search");
			dialog.setCancelable(false);

			ImageView searchIcon = (ImageView) dialog.findViewById(R.id.input_icon);
			searchIcon.setImageResource(R.drawable.search);

			TextView search_label = (TextView) dialog.findViewById(R.id.input_label);
			search_label.setText("Search for a file");
			final EditText search_input = (EditText) dialog.findViewById(R.id.input_inputText);

			Button search_button = (Button) dialog.findViewById(R.id.input_create_b);
			Button cancel_button = (Button) dialog.findViewById(R.id.input_cancel_b);
			search_button.setText("Search");

			search_button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					String temp = search_input.getText().toString();
					if (temp.length() > 0){
						mHandler.searchForFile(temp);
					}
					dialog.dismiss();
				}
			});

			cancel_button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			break;
		}
		return dialog;
	}

	/**
	 * (non-Javadoc) This will check if the user is at root directory. If so, if
	 * they press back again, it will close the application.
	 * 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keycode, KeyEvent event) {

		String current = mFileManager.getCurrentDir();
		if (keycode != KeyEvent.KEYCODE_MENU) {
			// Hide menu
			if (mMenu.isShowing()) {
				mMenu.hide();
			}
		}
		if (keycode == KeyEvent.KEYCODE_SEARCH) {
			showDialog(SEARCH_BUTTON);
			return true;
		} else if (keycode == KeyEvent.KEYCODE_BACK && mUseBackKey&& !current.equals("/")) {
			if (mHandler.isMultiSelected()) {
				mTable.killMultiSelect(true);
				Toast.makeText(FileExplorerActivity.this, "Multi-select is now off",Toast.LENGTH_SHORT).show();
			}

			mHandler.updateDirectory(mFileManager.getPreviousDir());
			mPathLabel.setText("Path: " + mFileManager.getCurrentDir());
			return true;

		} else if (keycode == KeyEvent.KEYCODE_BACK && mUseBackKey
				&& current.equals("/")) {
			// Toast.makeText(FileExplorerActivity.this, "Press back again to quit.",Toast.LENGTH_SHORT).show();
			finish();
			mUseBackKey = false;
			mPathLabel.setText("Path: " + mFileManager.getCurrentDir());
			return false;

		} else if (keycode == KeyEvent.KEYCODE_BACK && !mUseBackKey&& current.equals("/")) {
			finish();
			return false;
		} else if (keycode == KeyEvent.KEYCODE_MENU) {
			doMenu();
			return false;
		}
		return false;
	}

	/**
	 * Load up our menu.
	 */
	private void loadMenuItems() {
		// This is kind of a tedious way to load up the menu items.
		ArrayList<CustomMenuItem> menuItems = new ArrayList<CustomMenuItem>();
		CustomMenuItem cmi = new CustomMenuItem();
		cmi.setCaption("New Folder");
		cmi.setImageResourceId(R.drawable.newfolder);
		cmi.setId(MENU_MKDIR);
		menuItems.add(cmi);
		cmi = new CustomMenuItem();
		cmi.setCaption("Search");
		cmi.setImageResourceId(R.drawable.search);
		cmi.setId(MENU_SEARCH);
		menuItems.add(cmi);
		cmi = new CustomMenuItem();
		cmi.setCaption("Settings");
		cmi.setImageResourceId(R.drawable.setting);
		cmi.setId(MENU_SETTING);
		menuItems.add(cmi);
		cmi = new CustomMenuItem();
		cmi.setCaption("Quit");
		cmi.setImageResourceId(R.drawable.logout);
		cmi.setId(MENU_QUIT);
		// menuItems.add(cmi);
		if (!mMenu.isShowing()) {
			try {
				mMenu.setMenuItems(menuItems);
			} catch (Exception e) {
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setTitle("Egads!");
				alert.setMessage(e.getMessage());
				alert.show();
			}
		}
	}

	/**
	 * Toggle our menu on user pressing the menu key.
	 */
	private void doMenu() {
		if (mMenu.isShowing()) {
			mMenu.hide();
		} else {
			// Note it doesn't matter what widget you send the menu as long as
			// it gets view.
			mMenu.show(findViewById(R.id.storage_label));
		}
	}

	public void MenuItemSelectedEvent(CustomMenuItem selection) {
		switch (selection.getId()) {
		case MENU_MKDIR:
			showDialog(MENU_MKDIR);
			break;
		case MENU_SEARCH:
			showDialog(MENU_SEARCH);
			break;
		case MENU_SETTING:
			Intent settings_int = new Intent(this, FileExplorerSettingsActivity.class);
			settings_int.putExtra("HIDDEN",
					mSettings.getBoolean(PREFS_HIDDEN, false));
			settings_int.putExtra("THUMBNAIL",
					mSettings.getBoolean(PREFS_THUMBNAIL, true));
			settings_int.putExtra("COLOR", mSettings.getInt(PREFS_COLOR, Color.BLACK));
			settings_int.putExtra("SORT", mSettings.getInt(PREFS_SORT, 0));
			settings_int.putExtra("SPACE",mSettings.getInt(PREFS_STORAGE, View.VISIBLE));
			startActivityForResult(settings_int, SETTING_REQ);
			break;
		case MENU_QUIT:
			finish();
			break;
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View view,
			int position, long id) {
		// Hide menu
		if (mMenu.isShowing()) {
			mMenu.hide();
		}
		boolean multi_data = mHandler.hasMultiSelectData();
		mSelectedListItem = mHandler.getData(position);

		// array to hold the coordinates of the clicked view
		int[] xy = new int[2];
		// fills the array with the computed coordinates
		view.getLocationInWindow(xy);
		// rectangle holding the clicked view area
		Rect rect = new Rect(xy[0], xy[1], xy[0] + view.getWidth(), xy[1]+ view.getHeight());

		// a new QuickActionWindow object
		final QuickActionWindow qa = new QuickActionWindow(FileExplorerActivity.this, view,rect);

		/* is it a directory and is multi-select turned off */
		if (mFileManager.isDirectory(mSelectedListItem)
				&& !mHandler.isMultiSelected()) { // isFolder
			// adds an item to the badge and defines the quick action to be
			// triggered
			// when the item is clicked on
			qa.addItem(getResources().getDrawable(R.drawable.delete),
					"Delete Folder", new OnClickListener() {
						public void onClick(View v) {
							delete();
							qa.dismiss();
						}
					});

			qa.addItem(getResources().getDrawable(R.drawable.rename),
					"Rename Folder", new OnClickListener() {
						public void onClick(View v) {
							showDialog(FOLDER_MENU_RENAME);
							qa.dismiss();
						}
					});

			qa.addItem(getResources().getDrawable(R.drawable.copy),
					"Copy Folder", new OnClickListener() {
						public void onClick(View v) {
							moveOrCopy();
							qa.dismiss();
						}
					});

			qa.addItem(getResources().getDrawable(R.drawable.more),
					"Move|Cut Folder", new OnClickListener() {
						public void onClick(View v) {
							isFolderMove = true;
							moveOrCopy();
							qa.dismiss();
						}
					});

			qa.addItem(	getResources().getDrawable(	android.R.drawable.ic_menu_compass), "Zip Folder",
					new OnClickListener() {
						public void onClick(View v) {
							String dir = mFileManager.getCurrentDir();
							mHandler.zipFile(dir + "/" + mSelectedListItem);
							qa.dismiss();
						}
					});

			if (mHoldingFile || multi_data) {
				qa.addItem(getResources().getDrawable(android.R.drawable.ic_menu_help),
						"Paste into folder", new OnClickListener() {
							public void onClick(View v) {
								boolean multi_select = mHandler
										.hasMultiSelectData();

								if (multi_select) {
									mHandler.copyFileMultiSelect(mFileManager
											.getCurrentDir()
											+ "/"
											+ mSelectedListItem);
								} else if (mHoldingFile
										&& mCopiedTarget.length() > 1) {
									mHandler.copyFile(mCopiedTarget,
											mFileManager.getCurrentDir() + "/"
													+ mSelectedListItem);
									mDetailLabel.setText("");
								}

								mHoldingFile = false;
								qa.dismiss();
							}
						});
			}
			if (mHoldingZip) {
				qa.addItem(getResources().getDrawable(android.R.drawable.ic_menu_crop),
						"Extract here", new OnClickListener() {
							public void onClick(View v) {
								if (mHoldingZip && mZippedTarget.length() > 1) {
									String current_dir = mFileManager
											.getCurrentDir()
											+ "/"
											+ mSelectedListItem + "/";
									String old_dir = mZippedTarget.substring(0,
											mZippedTarget.lastIndexOf("/"));
									String name = mZippedTarget.substring(
											mZippedTarget.lastIndexOf("/") + 1,
											mZippedTarget.length());

									if (new File(mZippedTarget).canRead()
											&& new File(current_dir).canWrite()) {
										mHandler.unZipFileToDir(name,
												current_dir, old_dir);
										mPathLabel.setText("Path: "
												+ current_dir);
									} else {
										Toast.makeText(
												FileExplorerActivity.this,
												"You do not have permission to unzip "
														+ name,
												Toast.LENGTH_SHORT).show();
									}
								}

								mHoldingZip = false;
								mDetailLabel.setText("");
								mZippedTarget = "";
								qa.dismiss();
							}
						});
			}

			// shows the quick action window on the screen
			qa.show();

		} else if (!mFileManager.isDirectory(mSelectedListItem)
				&& !mHandler.isMultiSelected()) { // isFile
			qa.addItem(getResources().getDrawable(R.drawable.delete),
					"Delete File", new OnClickListener() {
						public void onClick(View v) {
							delete();
							qa.dismiss();
						}
					});

			qa.addItem(getResources().getDrawable(R.drawable.delete),
					"Rename File", new OnClickListener() {
						public void onClick(View v) {
							showDialog(FILE_MENU_RENAME);
							qa.dismiss();
						}
					});

			qa.addItem(getResources().getDrawable(R.drawable.delete),
					"Copy File", new OnClickListener() {
						public void onClick(View v) {
							moveOrCopy();
							qa.dismiss();
						}
					});

			qa.addItem(getResources().getDrawable(R.drawable.delete),
					"Move|Cut File", new OnClickListener() {
						public void onClick(View v) {
							isFileMove = true;
							moveOrCopy();
							qa.dismiss();
						}
					});

			qa.addItem(getResources().getDrawable(R.drawable.delete), "Share",
					new OnClickListener() {
						public void onClick(View v) {
							File file = new File(mFileManager.getCurrentDir()
									+ "/" + mSelectedListItem);
							Intent share = new Intent();
							share.setAction(android.content.Intent.ACTION_SEND);
							share.setType("text/plain");
							share.putExtra(Intent.EXTRA_STREAM,
									Uri.fromFile(file));
							startActivity(Intent.createChooser(share,
									"Share via"));
							qa.dismiss();
						}
					});

			qa.show();
		}
		return false;
	}

	/**
	 * Delete file/folder
	 * */
	private void delete() {
		AlertDialog.Builder builder = new AlertDialog.Builder(FileExplorerActivity.this);
		builder.setTitle("Warning ");
		builder.setIcon(R.drawable.warning);
		builder.setMessage("Deleting " + mSelectedListItem
				+ " cannot be undone. Are you sure you want to delete?");
		builder.setCancelable(false);

		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		builder.setPositiveButton("Delete",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mHandler.deleteFile(mFileManager.getCurrentDir() + "/"
								+ mSelectedListItem);
					}
				});

		AlertDialog alert_d = builder.create();
		alert_d.show();
	}
	/**
	 * Move or Copy file/folder
	 * */
	private void moveOrCopy() {
		if (isFileMove || isFolderMove) {
			mHandler.setDeleteAfterCopy(true);
		}
		mHoldingFile = true;
		mCopiedTarget = mFileManager.getCurrentDir() + "/" + mSelectedListItem;
		mDetailLabel.setText("Holding " + mSelectedListItem);
	}

}
