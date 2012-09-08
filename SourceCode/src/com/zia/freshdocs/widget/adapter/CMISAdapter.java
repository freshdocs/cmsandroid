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
package com.zia.freshdocs.widget.adapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.io.IOUtils;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.zia.freshdocs.R;
import com.zia.freshdocs.app.CMISApplication;
import com.zia.freshdocs.cmis.CMIS;
import com.zia.freshdocs.model.Constants;
import com.zia.freshdocs.model.NodeRef;
import com.zia.freshdocs.model.ViewItem;
import com.zia.freshdocs.net.Downloadable;
import com.zia.freshdocs.preference.CMISPreferencesManager;
import com.zia.freshdocs.util.Pair;
import com.zia.freshdocs.util.Utils;
import com.zia.freshdocs.widget.UITableView;

/**
 * Handles navigation of the repo via the CMIS api.
 * 
 * @author jsimpson
 * 
 */
@SuppressLint("HandlerLeak")
public class CMISAdapter extends ArrayAdapter<NodeRef> {
	private static final int BUF_SIZE = 16384;

	private Pair<String, NodeRef[]> mCurrentState = new Pair<String, NodeRef[]>(
			null, null);
	private Stack<Pair<String, NodeRef[]>> mStack = new Stack<Pair<String, NodeRef[]>>();
	private CMIS mCmis;
	private ProgressDialog mProgressDlg = null;
	private ChildDownloadThread mDlThread = null;
	private boolean mFavoritesView = false;

	public CMISAdapter(Context context, int textViewResourceId,
			NodeRef[] objects) {
		super(context, textViewResourceId, objects);
	}

	public CMISAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	public CMISAdapter(Context context, int resource, int textViewResourceId) {
		super(context, resource, textViewResourceId);
	}

	public CMIS getCmis() {
		return mCmis;
	}

	public void setCmis(CMIS cmis) {
		this.mCmis = cmis;
	}

	public boolean isFavoritesView() {
		return mFavoritesView;
	}

	public void setFavoritesView(boolean favoritesView) {
		this.mFavoritesView = favoritesView;
	}

	public void refresh() {
		getChildren(mCurrentState.getFirst());
	}

	/**
	 * Handles connecting to the host, get host info such as version and then
	 * displays the Company Home contents.
	 */
	public void home() {
		startProgressDlg(true);

		mDlThread = new ChildDownloadThread(new Handler() {
			public void handleMessage(Message msg) {
				// Save reference to current entry
				mStack.clear();

				NodeRef[] companyHome = (NodeRef[]) mDlThread.getResult();

				if (companyHome != null) {
					dismissProgressDlg();

					// Get Company Home children
					mCurrentState = new Pair<String, NodeRef[]>("", companyHome);
					populateList(companyHome);
				} else {
					CMISApplication app = (CMISApplication) getContext()
							.getApplicationContext();
					app.handleNetworkStatus();
				}
			}
		}, new Downloadable() {
			public Object execute() {
				return mCmis.getCompanyHome();
			}
		});
		mDlThread.start();
	}

	public boolean isFolder(int position) {
		return getItem(position).isFolder();
	}

	public boolean hasPrevious() {
		return mStack.size() > 0;
	}

	public void previous() {
		if (mStack.size() > 0) {
			mCurrentState = mStack.pop();
			populateList(mCurrentState.getSecond());
		}
	}

	/**
	 * Retrieve the children from the specified node.
	 * 
	 * @param position
	 *            n-th child position
	 */
	public void getChildren(int position) {
		final NodeRef ref = getItem(position);

		// For folders display the contents for the specified URI
		if (ref.isFolder()) {
			mStack.push(mCurrentState);
			String uuid = ref.getContent();
			mCurrentState = new Pair<String, NodeRef[]>(uuid, null);
			getChildren(uuid);
		}
		// For non-folders try a download (if there is an external storage card)
		else {
			String storageState = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(storageState)) {
				downloadContent(ref, new Handler() {
					public void handleMessage(Message msg) {
						boolean done = msg.getData().getBoolean("done");
						if (done) {
							dismissProgressDlg();

							File file = (File) mDlThread.getResult();
							if (file != null) {
								viewContent(file, ref);
							}
						} else {
							int value = msg.getData().getInt("progress");
							if (value > 0) {
								mProgressDlg.setProgress(value);
							}
						}
					}
				});
			}
		}
	}

	/**
	 * Send the content using a built-in Android activity which can handle the
	 * content type.
	 * 
	 * @param position
	 */
	public void shareContent(int position) {
		final NodeRef ref = getItem(position);
		downloadContent(ref, new Handler() {
			public void handleMessage(Message msg) {
				Context context = getContext();
				boolean done = msg.getData().getBoolean("done");

				if (done) {
					dismissProgressDlg();

					File file = (File) mDlThread.getResult();

					if (file != null) {
						Resources res = context.getResources();
						Uri uri = Uri.fromFile(file);
						Intent emailIntent = new Intent(Intent.ACTION_SEND);
						emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
						emailIntent.putExtra(Intent.EXTRA_SUBJECT,
								ref.getName());
						emailIntent.putExtra(Intent.EXTRA_TEXT,
								res.getString(R.string.email_text));
						emailIntent.setType(ref.getContentType());

						try {
							context.startActivity(Intent.createChooser(
									emailIntent,
									res.getString(R.string.email_title)));
						} catch (ActivityNotFoundException e) {
							String text = "No suitable applications registered to send "
									+ ref.getContentType();
							int duration = Toast.LENGTH_SHORT;
							Toast toast = Toast.makeText(context, text,
									duration);
							toast.show();
						}
					}
				} else {
					int value = msg.getData().getInt("progress");
					if (value > 0) {
						mProgressDlg.setProgress(value);
					}
				}
			}
		});
	}

	/**
	 * Display a favorite (stored on disk)
	 * 
	 * @param position
	 */
	public void viewFavorite(int position) {
		Context context = getContext();
		CMISPreferencesManager prefsMgr = CMISPreferencesManager.getInstance();
		NodeRef ref = getItem(position);
		Set<NodeRef> favorites = prefsMgr.getFavorites(context);

		if (favorites.contains(ref)) {
			CMISApplication app = (CMISApplication) getContext()
					.getApplicationContext();
			File f = app.getFile(ref.getName(), -1);

			if (f != null) {
				viewContent(f, ref);
			}
		}
	}

	/**
	 * Saves the content node to the favorites area of the sdcard
	 * 
	 * @param position
	 */
	public void toggleFavorite(int position) {
		final Context context = getContext();
		final CMISPreferencesManager prefsMgr = CMISPreferencesManager
				.getInstance();
		final NodeRef ref = getItem(position);
		final Set<NodeRef> favorites = prefsMgr.getFavorites(context);

		if (favorites.contains(ref)) {
			favorites.remove(ref);
			prefsMgr.storeFavorites(context, favorites);

			if (mFavoritesView) {
				remove(ref);
				notifyDataSetChanged();
			}
		} else {
			downloadContent(ref, new Handler() {
				public void handleMessage(Message msg) {
					boolean done = msg.getData().getBoolean("done");
					if (done) {
						dismissProgressDlg();

						File file = (File) mDlThread.getResult();

						if (file != null) {
							favorites.add(ref);
							prefsMgr.storeFavorites(context, favorites);
						}
					} else {
						int value = msg.getData().getInt("progress");
						if (value > 0) {
							mProgressDlg.setProgress(value);
						}
					}
				}
			});
		}
	}
	
	/**
	 * Display file information
	 * 
	 * @param context
	 */
	
	public void showFileInfo(Context context, int position, boolean isFile){
		
		final NodeRef ref = getItem(position);
		try {
			//We need to get the instance of the LayoutInflater, use the context of this activity
	        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        //Inflate the view from a predefined XML layout
	        View layout = inflater.inflate(R.layout.file_info_dialog, null, false);
	        // create a WRAP_CONTENT PopupWindow
	        PopupWindow mPopUp = new PopupWindow(layout, WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
	        mPopUp.setBackgroundDrawable(new BitmapDrawable());
	        mPopUp.setOutsideTouchable(true);
	        // display the popup in the center
	        mPopUp.showAtLocation(layout, Gravity.CENTER, 0, 0);
	        
	        TextView title = (TextView) layout.findViewById(R.id.dialog_title);
	        if(isFile)
	        	title.setText(context.getString(R.string.str_file_information));
	        else
	        	title.setText(context.getString(R.string.str_folder_information));
	        
	        UITableView tableView = (UITableView) layout.findViewById(R.id.tableView);
	        
	        ViewItem viewItem;
	        
	        if(ref.getName() != null){
	        	viewItem = Utils.createCustomView(context, context.getString(R.string.str_name), ref.getName());
	    		tableView.addViewItem(viewItem);
	        }
	        if(ref.getCreateBy() != null){
	        	viewItem = Utils.createCustomView(context, context.getString(R.string.str_created_by), ref.getCreateBy());
	    		tableView.addViewItem(viewItem);
	        }
	        
	        if(ref.getLastModificationDate() != null){
	        	viewItem = Utils.createCustomView(context, context.getString(R.string.str_last_modification_date), ref.getLastModificationDate());
	     		tableView.addViewItem(viewItem);
	        }
    		
	        if(ref.getLastModifiedBy() != null){
	        	viewItem = Utils.createCustomView(context, context.getString(R.string.str_last_modified_by), ref.getLastModifiedBy());
	     		tableView.addViewItem(viewItem);
	        }
	        
	        if(ref.getContent() != null){
	        	viewItem = Utils.createCustomView(context, context.getString(R.string.str_content), ref.getContent());
	     		tableView.addViewItem(viewItem);
	        }
	        
	        if(ref.getContentType() != null){
	        	viewItem = Utils.createCustomView(context, context.getString(R.string.str_content_type), ref.getContentType());
	     		tableView.addViewItem(viewItem);
	        }
	        
	        if(ref.getObjectId() != null){
	        	viewItem = Utils.createCustomView(context, context.getString(R.string.str_object_id), ref.getObjectId());
	     		tableView.addViewItem(viewItem);
	        }
	        
	        if(ref.getParentId() != null){
	        	viewItem = Utils.createCustomView(context, context.getString(R.string.str_parent_id), ref.getParentId());
	     		tableView.addViewItem(viewItem);
	        }
	        
	        if(ref.getVersion() != null){
	        	viewItem = Utils.createCustomView(context, context.getString(R.string.str_version), ref.getVersion());
	     		tableView.addViewItem(viewItem);
	        }
	        
	        tableView.commit();
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Download the content for the given NodeRef
	 * 
	 * @param ref
	 * @param handler
	 */
	protected void downloadContent(final NodeRef ref, final Handler handler) {
		startProgressDlg(false);
		mProgressDlg.setMax(Long.valueOf(ref.getContentLength()).intValue());

		mDlThread = new ChildDownloadThread(handler, new Downloadable() {
			public Object execute() {
				File f = null;

				try {
					CMISApplication app = (CMISApplication) getContext()
							.getApplicationContext();
					URL url = new URL(ref.getContent());
					String name = ref.getName();
					long fileSize = ref.getContentLength();
					f = app.getFile(name, fileSize);

					if (f != null && f.length() != fileSize) {
						Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

						FileOutputStream fos = new FileOutputStream(f);
						InputStream is = mCmis.get(url.getPath());

						byte[] buffer = new byte[BUF_SIZE];
						int len = is.read(buffer);
						int total = len;
						Message msg = null;
						Bundle b = null;

						while (len != -1) {
							msg = handler.obtainMessage();
							b = new Bundle();
							b.putInt("progress", total);
							msg.setData(b);
							handler.sendMessage(msg);

							fos.write(buffer, 0, len);
							len = is.read(buffer);
							total += len;

							if (Thread.interrupted()) {
								fos.close();
								f = null;
								throw new InterruptedException();
							}
						}

						fos.flush();
						fos.close();
					}
				} catch (Exception e) {
					Log.e(CMISAdapter.class.getSimpleName(), "", e);
				}

				return f;
			}
		});
		mDlThread.start();
	}

	public void interrupt() {
		if (mDlThread != null && mDlThread.isAlive()) {
			mDlThread.interrupt();
		}
	}

	/**
	 * Send an Intent requesting for an activity to display the content.
	 * 
	 * @param file
	 * @param ref
	 */
	protected void viewContent(File file, NodeRef ref) {
		Context context = getContext();

		// Ask for viewer
		Uri uri = Uri.fromFile(file);
		Intent viewIntent = new Intent(Intent.ACTION_VIEW);
		viewIntent.setDataAndType(uri, ref.getContentType());
		try {
			context.startActivity(viewIntent);
		} catch (ActivityNotFoundException e) {
			deleteContent(file);
			String text = "No viewer found for " + ref.getContentType();
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}

	protected void deleteContent(File file) {
		if (file.exists()) {
			file.delete();
		}
	}

	protected void getChildren(final String uuid) {
		startProgressDlg(true);

		mDlThread = new ChildDownloadThread(mResultHandler, new Downloadable() {
			public Object execute() {
				return mCmis.getChildren(uuid);
			}
		});
		mDlThread.start();
	}

	protected void startProgressDlg(boolean indeterminate) {
		Context context = getContext();
		Resources res = context.getResources();

		if (mProgressDlg == null || !mProgressDlg.isShowing()) {
			mProgressDlg = new ProgressDialog(context);
			mProgressDlg
					.setProgressStyle(indeterminate ? ProgressDialog.STYLE_SPINNER
							: ProgressDialog.STYLE_HORIZONTAL);
			mProgressDlg.setMessage(res.getString(R.string.loading));
			mProgressDlg.setTitle("");
			mProgressDlg.setCancelable(true);
			mProgressDlg.setIndeterminate(indeterminate);
			mProgressDlg.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					interrupt();
				}
			});
			mProgressDlg.show();
		}
	}

	protected void dismissProgressDlg() {
		if (mProgressDlg != null && mProgressDlg.isShowing()) {
			mProgressDlg.dismiss();
		}
	}

	/**
	 * Query the server using the query appropriate for the server version
	 * 
	 * @param term
	 */
	public void query(final String term) {
		startProgressDlg(true);

		mDlThread = new ChildDownloadThread(mResultHandler, new Downloadable() {
			public Object execute() {
				Context context = getContext();
				Resources res = context.getResources();
				InputStream is = res.openRawResource(mCmis.getVersion()
						.contains("0.6") ? R.raw.query_0_6 : R.raw.query_1_0);
				String xml = null;

				try {
					xml = String.format(IOUtils.toString(is), term, term);
					return mCmis.query(xml);
				} catch (IOException e) {
					Log.e(CMISAdapter.class.getSimpleName(), "", e);
				}

				return null;
			}
		});
		mDlThread.start();

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);

		NodeRef nodeRef = getItem(position);

		TextView textView = (TextView) view.findViewById(R.id.node_ref_label);
		textView.setText(nodeRef.getName());

		TextView textModifiedView = (TextView) view
				.findViewById(R.id.node_ref_modified);
		String lastModified = nodeRef.getLastModifiedBy();
		textModifiedView.setText(lastModified);

		String dateStr = nodeRef.getLastModificationDate();
		SimpleDateFormat parseFormat = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		Date date = null;

		try {
			date = parseFormat.parse(dateStr);
			SimpleDateFormat outFormat = new SimpleDateFormat(
					"HH:mm:ss MM/dd/yyyy");
			dateStr = outFormat.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (lastModified == null) {
			textModifiedView.setText(dateStr);
		} else {
			TextView textModDateView = (TextView) view
					.findViewById(R.id.node_ref_modified_date);
			textModDateView.setText(dateStr);
		}
		

		ImageView imgView = (ImageView) view.findViewById(R.id.node_ref_img);
		String contentType = nodeRef.getContentType();
		Drawable icon = getDrawableForType(contentType == null ? "cmis/folder"
				: contentType);
		imgView.setImageDrawable(icon);
		
		return view;
	}

	protected Drawable getDrawableForType(String contentType) {
		Context context = getContext();
		Resources resources = context.getResources();
		int resId = Constants.mimeMap.get(Constants.mimeMap
				.containsKey(contentType) ? contentType : null);
		Drawable icon = resources.getDrawable(resId);
		icon.setBounds(new Rect(0, 0, 44, 44));
		return icon;
	}

	protected void populateList(NodeRef[] nodes) {
		clear();

		if (nodes != null) {
			Arrays.sort(nodes, new Comparator<NodeRef>() {
				public int compare(NodeRef left, NodeRef right) {
					return left.getName().compareTo(right.getName());
				}
			});

			int n = nodes.length;
			for (int i = 0; nodes != null && i < n; i++) {
				NodeRef ref = nodes[i];
				if (!ref.getName().startsWith(".")) {
					add(nodes[i]);
				}
			}
		}
	}

	final Handler mResultHandler = new Handler() {
		public void handleMessage(Message msg) {
			dismissProgressDlg();

			boolean done = msg.getData().getBoolean("done");
			if (done) {
				NodeRef[] nodes = (NodeRef[]) mDlThread.getResult();
				mCurrentState = new Pair<String, NodeRef[]>(
						mCurrentState.getFirst(), nodes);
				populateList(nodes);
			}
		}
	};

	private class ChildDownloadThread extends Thread {
		Handler _handler;
		Downloadable _delegate;
		Object _result = null;

		ChildDownloadThread(Handler h, Downloadable delegate) {
			_handler = h;
			_delegate = delegate;
		}

		public void run() {
			_result = _delegate.execute();

			Message msg = _handler.obtainMessage();
			Bundle b = new Bundle();
			b.putBoolean("done", true);
			msg.setData(b);

			_handler.sendMessage(msg);
		}

		public Object getResult() {
			return _result;
		}
	}
}
