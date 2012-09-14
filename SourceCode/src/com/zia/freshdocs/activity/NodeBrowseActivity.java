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
import java.util.ArrayList;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
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
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zia.freshdocs.R;
import com.zia.freshdocs.app.CMISApplication;
import com.zia.freshdocs.cmis.CMIS;
import com.zia.freshdocs.model.Constants;
import com.zia.freshdocs.model.Constants.NetworkStatus;
import com.zia.freshdocs.model.NodeRef;
import com.zia.freshdocs.preference.CMISPreferencesManager;
import com.zia.freshdocs.util.SharedPreferencesAccess;
import com.zia.freshdocs.util.StringUtils;
import com.zia.freshdocs.util.Utils;
import com.zia.freshdocs.widget.ViVoteChart;
import com.zia.freshdocs.widget.adapter.CMISAdapter;
import com.zia.freshdocs.widget.adapter.CommentAdapter;
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
	private final int CREATE_NEW_FOLDER = 2;
	private final int RATING = 3;
	private final int RATE_COMMENT = 4;
	public static final int REQUEST_UPLOAD_CODE = 5;
	private String mFolderName, mFolderDescription;
	private String mFolderId; 
	private String mFavoriteTitle;
	private Drawable mFavoriteImage;
	private ArrayList<String> mArrayComment;
	private int mPosition;

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
			mHandler.sendEmptyMessage(CREATE_NEW_FOLDER);
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
			case CREATE_NEW_FOLDER:
				showCreateNewFolderDialog();
				break;
			case RATING:
				showRatingDialog();
				break;
			case RATE_COMMENT:
				showCommentDialog();
				break;
			}
		}
		};
	
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
		mPosition = position;
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
		
		if(requestCode == REQUEST_UPLOAD_CODE){
			if(resultCode == RESULT_OK){
				// If the file selection was successful
				try {
					// Get the URI of the selected file
					final Uri uri = data.getData();
					// Create a file instance from the URI
					final File file = new File(Utils.getFilePath(this, uri));
					
					// Upload
					mAdapter.getCmis().upload(file, mAdapter.getItem(mPosition).getName(), Constants.DOCUMENT_LIBRARY, "");
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}
		}else if (data != null && data.hasExtra(Constants.QUIT)) {
			onQuit();
		}
	}
	
	private void showRatingDialog(){
		try {
			//We need to get the instance of the LayoutInflater, use the context of this activity
	        LayoutInflater inflater = (LayoutInflater) NodeBrowseActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        //Inflate the view from a predefined XML layout
	        View layout = inflater.inflate(R.layout.rating_layout, null, false);
	        
	        // Avoid Popup's bug
	        Display display = getWindowManager().getDefaultDisplay(); 
	        int height = display.getHeight() - 100;
	        
	        // create a WRAP_CONTENT PopupWindow
	        mPopUp = new PopupWindow(layout, WindowManager.LayoutParams.FILL_PARENT, height, true);
	        mPopUp.setBackgroundDrawable(new BitmapDrawable());
	        mPopUp.setOutsideTouchable(true);
	        // display the popup in the center
	        mPopUp.showAtLocation(layout, Gravity.CENTER, 0, 0);
	        
	        String ratingsCount, ratingsTotal, averageRating;
	        
	        ratingsCount = SharedPreferencesAccess.getValueFromSharedPreferences(NodeBrowseActivity.this, "ratingsCount");
	        ratingsTotal = SharedPreferencesAccess.getValueFromSharedPreferences(NodeBrowseActivity.this, "ratingsTotal");
	        averageRating = SharedPreferencesAccess.getValueFromSharedPreferences(NodeBrowseActivity.this, "averageRating");
	        if(averageRating.equalsIgnoreCase("-1")) // No comment
	        	averageRating = "0";
	        
	        TextView txtChartVoteRankPoint = (TextView) layout.findViewById(R.id.txtChartVoteRankPoint);
	        txtChartVoteRankPoint.setText(averageRating);
	        
	        RatingBar rtbChartVoteRankPoint = (RatingBar) layout.findViewById(R.id.rtbChartVoteRankPoint);
	        rtbChartVoteRankPoint.setRating(Float.parseFloat(averageRating.trim()));
	        
	        TextView txtChartVoteTotalComment = (TextView) layout.findViewById(R.id.txtChartVoteTotalComment);
	        txtChartVoteTotalComment.setText("(" + ratingsTotal + ")");
	        
	        ViVoteChart viVoteChart = (ViVoteChart) layout.findViewById(R.id.loChartView);
	        int[] rateValue = {0,0,0,0,0};
	        viVoteChart.setAttribute(rateValue);
	        viVoteChart.createChart();
	        
	        ListView lvCcomment = (ListView) layout.findViewById(R.id.lvComment);
	        mArrayComment = new ArrayList<String>();
	        final CommentAdapter arrayAdapter = new CommentAdapter(this,R.layout.user_comment_item, mArrayComment);
	        lvCcomment.setAdapter(arrayAdapter);
	        lvCcomment.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id) {
					mPosition = position;
					// Remove old view
					mQuickAction.removeAll();
					mQuickAction.addItem(getResources().getDrawable(R.drawable.excel),
							getString(R.string.delete_server), new OnClickListener() {
								public void onClick(View v) {
									try {
										String dataStr = mArrayComment.get(position);
										JSONObject dataObj = new JSONObject(dataStr);
										String nodeRef = dataObj.isNull("nodeRef") ? "" : dataObj.getString("nodeRef");
										if(!StringUtils.isEmpty(nodeRef)){
											try {
												mAdapter.getCmis().deleleComment(nodeRef);
												mArrayComment.remove(position);
												arrayAdapter.notifyDataSetChanged();
											} catch (ClientProtocolException e) {
												e.printStackTrace();
											} catch (IOException e) {
												e.printStackTrace();
											}
											mQuickAction.dismiss();
											
										}
										
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							});
					
					// shows the quick action window on the screen
					mQuickAction.show();
					
					return false;
				}
	        	
			});
	        
	        TextView txtNoComment = (TextView) layout.findViewById(R.id.txtNoComment);
	        
			if (mFolderId != null) {
				try {
					mAdapter.getCmis().getComment(NodeBrowseActivity.this, mFolderId);
					
					JSONArray dataArr = SharedPreferencesAccess.loadJSONArrayToSharedPreferences(NodeBrowseActivity.this, "comment");
					
					for(int i = 0 ; i < dataArr.length(); i++){
					    JSONObject object = (JSONObject) dataArr.get(i); 
					    String content = object.isNull("content") ? "" : object.getString("content");
					    content = content.replace("<p>", ""); // Remove unused data
					    content = content.replace("</p>", "");
					    Log.d("content", content);
					    
					    String author = object.isNull("author") ? "" : object.getString("author");
					    
					    JSONObject authorObj = new JSONObject(author);
					    String username = authorObj.isNull("username") ? "" : authorObj.getString("username");
					    Log.d("username", username);
					    
					    String nodeRef = object.isNull("nodeRef") ? "" : object.getString("nodeRef");
					    if(!StringUtils.isEmpty(nodeRef)){
					    	nodeRef = nodeRef.substring(nodeRef.lastIndexOf("/") + 1, nodeRef.length());
					    }
					    Log.d("nodeRef", nodeRef);
					    
					    String createdOn =  object.isNull("createdOn") ? "" : object.getString("createdOn");
					    Log.d("createdOn", createdOn);
					    
					    mArrayComment.add("{\"content\": \"" +content + "\",\"createdOn\": \"" +createdOn + "\",\"nodeRef\": \"" +nodeRef + "\",\"username\": \"" + username + "\"}");
					    
					}
					if(mArrayComment.size() == 0){ // No comment
						lvCcomment.setVisibility(View.GONE);
						txtNoComment.setVisibility(View.VISIBLE);
					}
					
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
//				mHandler.sendEmptyMessage(REFRESH);
			}
	       
	        Button btnComment = (Button) layout.findViewById(R.id.btn_comment_post);
	        btnComment.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Close dialog
					mPopUp.dismiss();
					mHandler.sendEmptyMessage(RATE_COMMENT);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void showCommentDialog(){
		try {
			//We need to get the instance of the LayoutInflater, use the context of this activity
	        LayoutInflater inflater = (LayoutInflater) NodeBrowseActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        //Inflate the view from a predefined XML layout
	        View layout = inflater.inflate(R.layout.rating, null, false);
	        
	        // Avoid Popup's bug
	        Display display = getWindowManager().getDefaultDisplay(); 
	        int height = display.getHeight() - 100;
	        
	        // create a WRAP_CONTENT PopupWindow
	        mPopUp = new PopupWindow(layout, WindowManager.LayoutParams.FILL_PARENT, height, true);
	        mPopUp.setBackgroundDrawable(new BitmapDrawable());
	        mPopUp.setOutsideTouchable(true);
	        // display the popup in the center
	        mPopUp.showAtLocation(layout, Gravity.CENTER, 0, 0);
	        
	        final EditText txtComment = (EditText) layout.findViewById(R.id.txtComment);
	        final EditText txtTitle = (EditText) layout.findViewById(R.id.txtTitle);
	        final RatingBar rtbRate = (RatingBar) layout.findViewById(R.id.rtb_comment_input);
	        
	        Button btnComment = (Button) layout.findViewById(R.id.btnComment);
	        btnComment.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(StringUtils.isEmpty(txtComment.getText().toString())||StringUtils.isEmpty(txtTitle.getText().toString()) ){
//						txtComment.setError(NodeBrowseActivity.this.getString(R.string.str_mandatory_info));
						Toast.makeText(NodeBrowseActivity.this, getString(R.string.str_mandatory_info), Toast.LENGTH_SHORT).show();
					}else{
						try {
							mAdapter.getCmis().addComment(mFolderId, txtTitle.getText().toString() , txtComment.getText().toString());
							
							if(rtbRate.getRating() != 0)
								mAdapter.getCmis().addRating(mFolderId, String.valueOf(rtbRate.getRating()));
						} catch (ClientProtocolException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
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
		
		mPosition = position;
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

			mQuickAction.addItem(getResources().getDrawable(R.drawable.context_share),
					getString(R.string.send), new OnClickListener() {
						public void onClick(View v) {
							mQuickAction.dismiss();
							mAdapter.shareContent(position);
						}
					});
			mFavoriteTitle = getString(R.string.add_favorite);
			mFavoriteImage = getResources().getDrawable(R.drawable.context_favorite_add);
			if (favorites.contains(ref)) {
				mFavoriteTitle = getString(R.string.remove_favorite);
				mFavoriteImage = getResources().getDrawable(R.drawable.context_favorite_remove);
			}

			mQuickAction.addItem(mFavoriteImage,
					mFavoriteTitle, new OnClickListener() {
						public void onClick(View v) {
							mQuickAction.dismiss();
							
							// Add/Remove favorite in Client
							mAdapter.toggleFavorite(position);
							
							if(mFavoriteTitle.equalsIgnoreCase(getString(R.string.add_favorite))){
								// Add favorite in Server
								try {
									String documentId = mAdapter.getItem(position).getObjectId();
									String userId = mAdapter.getCmis().getPrefs().getUsername();
									if(documentId != null && userId != null){
										ArrayList<String> oldFavorites = mAdapter.getCmis().getFavorite(userId);
										if(oldFavorites != null){
											oldFavorites.add(documentId);
											String newFavorites = "";
											for(int i = 0; i < oldFavorites.size(); i++){
												if(i == oldFavorites.size() - 1)
													newFavorites = newFavorites + oldFavorites.get(i);
												else
													newFavorites = newFavorites + oldFavorites.get(i) + ",";
											}
											
											Log.e("addFavorite", newFavorites);
											
											mAdapter.getCmis().addFavorite(userId, newFavorites);
											mHandler.sendEmptyMessage(REFRESH);
										}
									}else{
										
									}
									
								} catch (ClientProtocolException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}else{
								// Remove favorite in Server
								try {
									String documentId = mAdapter.getItem(position).getObjectId();
									String userId = mAdapter.getCmis().getPrefs().getUsername();
									if(documentId != null && userId != null){
										ArrayList<String> oldFavorites = mAdapter.getCmis().getFavorite(userId);
										if(oldFavorites != null){
											int index = oldFavorites.indexOf(documentId);
											if(index != -1){ // If found
												oldFavorites.remove(index);  // Remove favorite
												String newFavorites = "";
												for(int i = 0; i < oldFavorites.size(); i++){
													if(i == oldFavorites.size() - 1)
														newFavorites = newFavorites + oldFavorites.get(i);
													else
														newFavorites = newFavorites + oldFavorites.get(i) + ",";
												}
												
												Log.e("removeFavorite", newFavorites);
												
												mAdapter.getCmis().addFavorite(userId, newFavorites);
												mHandler.sendEmptyMessage(REFRESH);
											}
										}
									}else{
										
									}
									
								} catch (ClientProtocolException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}

						}
					});
			
			mQuickAction.addItem(getResources().getDrawable(R.drawable.context_delete),
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

			mQuickAction.addItem(getResources().getDrawable(R.drawable.context_file_info),
					getString(R.string.str_file_information),
					new OnClickListener() {
						public void onClick(View v) {
							mAdapter.showFileInfo(NodeBrowseActivity.this, position, isFile);
							mQuickAction.dismiss();
						}
					});
		} else { // If folder
			isFile = false;
			// Show folder information
			mQuickAction.addItem(getResources().getDrawable(R.drawable.context_folder_info),
					getString(R.string.str_folder_information),
					new OnClickListener() {
						public void onClick(View v) {
							mAdapter.showFileInfo(NodeBrowseActivity.this, position, isFile);	
							mQuickAction.dismiss();
						}
					});

			mQuickAction.addItem(getResources().getDrawable(R.drawable.context_delete), getString(R.string.str_delete),
					new OnClickListener() {
						public void onClick(View v) {
							mRequestThread = new Thread(new Runnable() {
								public void run() {
									synchronized (this) {
										mFolderId = mAdapter.getItem(position).getObjectId();
										if (mFolderId != null) {
											mFolderId = mFolderId.substring(mFolderId.lastIndexOf("/") + 1,mFolderId.length());
											try {
												mAdapter.getCmis().deleleFolder(mFolderId);
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
			
			mQuickAction.addItem(getResources().getDrawable(R.drawable.context_rating), getString(R.string.str_rating),
					new OnClickListener() {
						public void onClick(View v) {
							mRequestThread = new Thread(new Runnable() {
								public void run() {
									synchronized (this) {
										mFolderId = mAdapter.getItem(position).getObjectId();
										if (mFolderId != null) {
											mFolderId = mFolderId.substring(mFolderId.lastIndexOf("/") + 1,mFolderId.length());
											try {
												if(mAdapter.getCmis().getRating(NodeBrowseActivity.this, mFolderId)){
													mHandler.sendEmptyMessage(RATING);
													mQuickAction.dismiss();
												}
											} catch (ClientProtocolException e) {
												e.printStackTrace();
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
										
									}
								}
							});
							mRequestThread.start();
						}
					});
			mQuickAction.addItem(getResources().getDrawable(R.drawable.context_upload),
					getString(R.string.str_upload),
					new OnClickListener() {
						public void onClick(View v) {
							mRequestThread = new Thread(new Runnable() {
								public void run() {
									synchronized (this) {
										mFolderId = mAdapter.getItem(position).getObjectId();
										if (mFolderId != null) {
											mFolderId = mFolderId.substring(mFolderId.lastIndexOf("/") + 1,mFolderId.length());
												Intent fileChooserIntent = new Intent(NodeBrowseActivity.this, FileExplorerActivity.class);
												fileChooserIntent.putExtra(Constants.UPLOAD, true);
												startActivityForResult(fileChooserIntent, REQUEST_UPLOAD_CODE);
												mQuickAction.dismiss();
//												mHandler.sendEmptyMessage(REFRESH);
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
