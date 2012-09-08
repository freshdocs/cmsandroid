/*******************************************************************************
 * The MIT License
 * 
 * Copyright (c) 2010 Zia Consulting, Inc
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package com.zia.freshdocs.activity;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zia.freshdocs.R;
import com.zia.freshdocs.app.CMISApplication;
import com.zia.freshdocs.cmis.CMIS;
import com.zia.freshdocs.model.Constants;
import com.zia.freshdocs.model.NodeRef;
import com.zia.freshdocs.model.Constants.NetworkStatus;
import com.zia.freshdocs.preference.CMISPreferencesManager;
import com.zia.freshdocs.widget.adapter.CMISAdapter;
import com.zia.freshdocs.widget.quickaction.QuickActionWindow;

@SuppressLint("HandlerLeak")
public class NodeBrowseActivity extends DashboardActivity implements OnItemLongClickListener
{
	private static final String HOST_ID_KEY = "id";
	
	protected CMISAdapter mAdapter;
	protected boolean mAdapterInitialized = false;
	private QuickActionWindow mQuickAction;
	private NodeRef mTempParent;
	private Thread mRequestThread;
	private PopupWindow mPopUp;
	private final int REFRESH = 0;
	private final int CLOSE_DIALOG = 1;
	private final int SHOW_DIALOG = 2;
	private String mFolderName, mFolderDescription;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setTheme(R.style.Theme_HoloEverywhereLight);
		super.onCreate(savedInstanceState);

		restoreCMIS(savedInstanceState);

		initializeListView();
		
		getListView().setOnItemLongClickListener(this);

		if (!mAdapterInitialized && mAdapter != null && mAdapter.getCmis() != null) {
			mAdapterInitialized = true;
			mAdapter.home();
		}
	}
	
	protected void restoreCMIS(Bundle savedInstanceState){
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(HOST_ID_KEY)) {
			String id = savedInstanceState.getString(HOST_ID_KEY);
			CMISApplication app = (CMISApplication) getApplication();
			app.initCMIS(id);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		CMISApplication app = (CMISApplication) getApplication();
		CMIS cmis = app.getCMIS();

		if (cmis != null) {
			outState.putString(HOST_ID_KEY, cmis.getPrefs().getId());
		}
	}

	/**
	 * Handles rotation by doing nothing (instead of onCreate being called)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.browser, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Remove About
		menu.removeItem(3);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_refresh:
			mHandler.sendEmptyMessage(REFRESH);
			return true;
		case R.id.menu_item_create_folder:
			mHandler.sendEmptyMessage(SHOW_DIALOG);
			return true;
		case R.id.menu_item_favorites:
			Intent favoritesIntent = new Intent(this, FavoritesActivity.class);
			startActivityForResult(favoritesIntent, 0);
			return true;
		case R.id.menu_item_quit:
			onQuit();
			return true;
		default:
			return false;
		}
	}
	
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH:
				mAdapter.refresh();
				break;
			case CLOSE_DIALOG:
				mPopUp.dismiss();
				break;
			case SHOW_DIALOG:
				showCreateNewFolderDialog();
				break;
			}
		}};
	
	protected void onQuit() {
		Intent quitIntent = new Intent();
		quitIntent.putExtra(Constants.QUIT, true);
		setResult(RESULT_OK, quitIntent);
		finish();
	}
	
	protected void initializeListView() {
		CMISApplication app = (CMISApplication) getApplication();
		CMIS cmis = app.getCMIS();

		if (cmis != null && cmis.getNetworkStatus() == NetworkStatus.OK) {
			setContentView(R.layout.nodes);
		} else {
			setContentView(R.layout.nodes_offline);
		}

		mAdapter = new CMISAdapter(this, R.layout.node_ref_item, R.id.node_ref_label);
		mAdapter.setCmis(cmis);
		setListAdapter(mAdapter);
	}	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && mAdapter != null
				&& mAdapter.hasPrevious()) {
			mAdapter.previous();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		mAdapter.getChildren(position);
		NodeRef ref = mAdapter.getItem(position);
		// Get parent folder
		if(ref.isFolder())
			mTempParent = mAdapter.getItem(position);
	}

	protected void onSearch() {
		onSearchRequested();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (data != null && data.hasExtra(Constants.QUIT)) {
			onQuit();
		}
	}
	
	private void showCreateNewFolderDialog() {
		try {
			//We need to get the instance of the LayoutInflater, use the context of this activity
	        LayoutInflater inflater = (LayoutInflater) NodeBrowseActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        //Inflate the view from a predefined XML layout
	        View layout = inflater.inflate(R.layout.create_folder_alfresco_dialog, null, false);
	        // create a WRAP_CONTENT PopupWindow
	        mPopUp = new PopupWindow(layout, WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
	        // display the popup in the center
	        mPopUp.showAtLocation(layout, Gravity.CENTER, 0, 0);
	        
	        TextView title = (TextView) layout.findViewById(R.id.dialog_title);
	        title.setText(getString(R.string.str_create_folder));
	        
	        final EditText edtFolderName = (EditText) layout.findViewById(R.id.folder_name);
	        final EditText edtFolderDescription = (EditText) layout.findViewById(R.id.folder_description);
	        
	        Button ok = (Button) layout.findViewById(R.id.btn_ok);
	        ok.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mFolderName = edtFolderName.getText().toString();
					mFolderDescription = edtFolderDescription.getText().toString();
					mHandler.sendEmptyMessage(CLOSE_DIALOG);
					
					mRequestThread = new Thread(new Runnable() {
						public void run() {
							synchronized (this) {
								// Get folderId from its child
								String folderID = mTempParent.getObjectId();
								if(folderID != null){
									folderID = folderID.substring(folderID.lastIndexOf("/") + 1, folderID.length());
									try {
										mAdapter.getCmis().createFolder(folderID, mFolderName, mFolderDescription);
									} catch (ClientProtocolException e) {
										e.printStackTrace();
									} catch (IOException e) {
										e.printStackTrace();
									}
									// Refresh content
									mHandler.sendEmptyMessage(REFRESH);
								}
							}
						}});
					mRequestThread.start();
				}
			});
	        
	        Button cancel = (Button) layout.findViewById(R.id.btn_cancel);
	        cancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mHandler.sendEmptyMessage(CLOSE_DIALOG);
				}
			});
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View view,
			final int position, long value) {
		final boolean isFile;
		
		// array to hold the coordinates of the clicked view
		int[] xy = new int[2];
		// fills the array with the computed coordinates
		view.getLocationInWindow(xy);
		// rectangle holding the clicked view area
		Rect rect = new Rect(xy[0], xy[1], xy[0] + view.getWidth(), xy[1] + view.getHeight());
		
		// a new QuickActionWindow object
		mQuickAction = new QuickActionWindow(
				NodeBrowseActivity.this, view, rect);
		// If file
		if (!mAdapter.isFolder(position)) {
			isFile = true;
			NodeRef ref = mAdapter.getItem(position);
			CMISPreferencesManager prefsMgr = CMISPreferencesManager
					.getInstance();
			Set<NodeRef> favorites = prefsMgr.getFavorites(this);

			mQuickAction.addItem(getResources().getDrawable(R.drawable.excel),
					getString(R.string.send), new OnClickListener() {
						public void onClick(View v) {
							mQuickAction.dismiss();
							mAdapter.shareContent(position);
						}
					});

			String favoriteTitle = getString(R.string.add_favorite);
			if (favorites.contains(ref)) {
				favoriteTitle = getString(R.string.remove_favorite);
			}

			mQuickAction.addItem(getResources().getDrawable(R.drawable.excel),
					favoriteTitle, new OnClickListener() {
						public void onClick(View v) {
							mQuickAction.dismiss();
							mAdapter.toggleFavorite(position);
						}
					});
			
			mQuickAction.addItem(getResources().getDrawable(R.drawable.excel),
					getString(R.string.str_delete),
					new OnClickListener() {
						public void onClick(View v) {
							mRequestThread = new Thread(new Runnable() {
								public void run() {
									synchronized (this) {
										String fileId = mAdapter.getItem(position).getObjectId();
										if (fileId != null) {
											fileId = fileId.substring(fileId.lastIndexOf("/") + 1,fileId.length());
											try {
												mAdapter.getCmis().deleleFile(fileId);
											} catch (ClientProtocolException e) {
												e.printStackTrace();
											} catch (IOException e) {
												e.printStackTrace();
											}
											mQuickAction.dismiss();
											mHandler.sendEmptyMessage(REFRESH);
										}
									}
								}
							});
							mRequestThread.start();
						}
					});

			// Show file information

			mQuickAction.addItem(getResources().getDrawable(R.drawable.excel),
					getString(R.string.str_file_information),
					new OnClickListener() {
						public void onClick(View v) {
							mAdapter.showFileInfo(NodeBrowseActivity.this, position, isFile);
							mQuickAction.dismiss();
						}
					});
			mQuickAction.addItem(getResources().getDrawable(R.drawable.excel),
					getString(R.string.str_delete),
					new OnClickListener() {
						public void onClick(View v) {
							mRequestThread = new Thread(new Runnable() {
								public void run() {
									synchronized (this) {
											try {
												mAdapter.getCmis().getPerson("demo");
											} catch (ClientProtocolException e) {
												e.printStackTrace();
											} catch (IOException e) {
												e.printStackTrace();
											}
											mQuickAction.dismiss();
											mHandler.sendEmptyMessage(REFRESH);
									}
								}
							});
							mRequestThread.start();
						}
					});
		} else { // If folder
			isFile = false;
			// Show folder information
			mQuickAction.addItem(getResources().getDrawable(R.drawable.excel),
					getString(R.string.str_folder_information),
					new OnClickListener() {
						public void onClick(View v) {
							mAdapter.showFileInfo(NodeBrowseActivity.this, position, isFile);	
							mQuickAction.dismiss();
						}
					});
			mQuickAction.addItem(getResources().getDrawable(R.drawable.excel),
					getString(R.string.str_upload),
					new OnClickListener() {
						public void onClick(View v) {
							mRequestThread = new Thread(new Runnable() {
								public void run() {
									synchronized (this) {
										String folderId = mAdapter.getItem(
												position).getObjectId();
										if (folderId != null) {
											folderId = folderId.substring(folderId.lastIndexOf("/") + 1,folderId.length());
											try {
//												mAdapter.getCmis().uploadDocument(folderId);
//												mAdapter.getCmis().upLoadFile("/sdcard/a.pdf", folderId);
												File upload = new File(Environment.getExternalStorageDirectory().getPath() + "/a.pdf");
												mAdapter.getCmis().upload(upload, "longnd", "documentLibrary", "");
											} catch (IOException e) {
												e.printStackTrace();
											}
											mQuickAction.dismiss();
											mHandler.sendEmptyMessage(REFRESH);
										}
									}
								}
							});
							mRequestThread.start();
						}
					});
			mQuickAction.addItem(getResources().getDrawable(R.drawable.excel),
					getString(R.string.str_delete),
					new OnClickListener() {
						public void onClick(View v) {
							mRequestThread = new Thread(new Runnable() {
								public void run() {
									synchronized (this) {
										String folderId = mAdapter.getItem(
												position).getObjectId();
										if (folderId != null) {
											folderId = folderId.substring(folderId.lastIndexOf("/") + 1,folderId.length());
											try {
												mAdapter.getCmis().deleleFolder(folderId);
											} catch (ClientProtocolException e) {
												e.printStackTrace();
											} catch (IOException e) {
												e.printStackTrace();
											}
											mQuickAction.dismiss();
											mHandler.sendEmptyMessage(REFRESH);
										}
									}
								}
							});
							mRequestThread.start();
						}
					});
			mQuickAction.addItem(getResources().getDrawable(R.drawable.excel), "Add comment",
					new OnClickListener() {
						public void onClick(View v) {
							mRequestThread = new Thread(new Runnable() {
								public void run() {
									synchronized (this) {
										String folderId = mAdapter.getItem(
												position).getObjectId();
										if (folderId != null) {
											folderId = folderId.substring(folderId.lastIndexOf("/") + 1,folderId.length());
											try {
												mAdapter.getCmis().addComment(folderId, "Good", "Up vote");
											} catch (ClientProtocolException e) {
												e.printStackTrace();
											} catch (IOException e) {
												e.printStackTrace();
											}
											mQuickAction.dismiss();
											mHandler.sendEmptyMessage(REFRESH);
										}
									}
								}
							});
							mRequestThread.start();
						}
					});
			mQuickAction.addItem(getResources().getDrawable(R.drawable.excel), "Get rating",
					new OnClickListener() {
						public void onClick(View v) {
							mRequestThread = new Thread(new Runnable() {
								public void run() {
									synchronized (this) {
										String folderId = mAdapter.getItem(
												position).getObjectId();
										if (folderId != null) {
											folderId = folderId.substring(folderId.lastIndexOf("/") + 1,folderId.length());
											try {
												mAdapter.getCmis().getRating(folderId);
											} catch (ClientProtocolException e) {
												e.printStackTrace();
											} catch (IOException e) {
												e.printStackTrace();
											}
											mQuickAction.dismiss();
											mHandler.sendEmptyMessage(REFRESH);
										}
									}
								}
							});
							mRequestThread.start();
						}
					});
			
			mQuickAction.addItem(getResources().getDrawable(R.drawable.excel), "Add rating",
					new OnClickListener() {
						public void onClick(View v) {
							mRequestThread = new Thread(new Runnable() {
								public void run() {
									synchronized (this) {
										String folderId = mAdapter.getItem(
												position).getObjectId();
										if (folderId != null) {
											folderId = folderId.substring(folderId.lastIndexOf("/") + 1,folderId.length());
											try {
												mAdapter.getCmis().addRating(folderId, "5", "Rate via Android");
											} catch (ClientProtocolException e) {
												e.printStackTrace();
											} catch (IOException e) {
												e.printStackTrace();
											}
											mQuickAction.dismiss();
											mHandler.sendEmptyMessage(REFRESH);
										}
									}
								}
							});
							mRequestThread.start();
						}
					});
			mQuickAction.addItem(getResources().getDrawable(R.drawable.excel), "Get comments",
					new OnClickListener() {
						public void onClick(View v) {
							mRequestThread = new Thread(new Runnable() {
								public void run() {
									synchronized (this) {
										String folderId = mAdapter.getItem(
												position).getObjectId();
										if (folderId != null) {
											folderId = folderId.substring(folderId.lastIndexOf("/") + 1,folderId.length());
											try {
												mAdapter.getCmis().getComment(folderId);
											} catch (ClientProtocolException e) {
												e.printStackTrace();
											} catch (IOException e) {
												e.printStackTrace();
											}
											mQuickAction.dismiss();
											mHandler.sendEmptyMessage(REFRESH);
										}
									}
								}
							});
							mRequestThread.start();
						}
					});
			mQuickAction.addItem(getResources().getDrawable(R.drawable.excel), "Delete comments",
					new OnClickListener() {
						public void onClick(View v) {
							mRequestThread = new Thread(new Runnable() {
								public void run() {
									synchronized (this) {
										String folderId = mAdapter.getItem(
												position).getObjectId();
										if (folderId != null) {
											folderId = folderId.substring(folderId.lastIndexOf("/") + 1,folderId.length());
											try {
												mAdapter.getCmis().deleleComment(folderId);
											} catch (ClientProtocolException e) {
												e.printStackTrace();
											} catch (IOException e) {
												e.printStackTrace();
											}
											mQuickAction.dismiss();
											mHandler.sendEmptyMessage(REFRESH);
										}
									}
								}
							});
							mRequestThread.start();
						}
					});
		}
		// shows the quick action window on the screen
		mQuickAction.show();

		return false;
	}
}
