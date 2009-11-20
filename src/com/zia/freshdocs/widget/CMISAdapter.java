package com.zia.freshdocs.widget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Stack;

import org.apache.commons.io.IOUtils;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.zia.freshdocs.Constants;
import com.zia.freshdocs.Pair;
import com.zia.freshdocs.R;
import com.zia.freshdocs.cmis.CMIS;
import com.zia.freshdocs.model.NodeRef;
import com.zia.freshdocs.util.URLUtils;

public class CMISAdapter extends ArrayAdapter<NodeRef>
{	
	private Pair<String, NodeRef[]> _currentState = new Pair<String, NodeRef[]>(null, null);
	private Stack<Pair<String, NodeRef[]>> _stack = new Stack<Pair<String,NodeRef[]>>();
	private CMIS _cmis;
	private ProgressDialog _progressDlg = null;
	private ChildDownloadThread _dlThread = null;

	public CMISAdapter(Context context, int textViewResourceId, NodeRef[] objects)
	{
		super(context, textViewResourceId, objects);
	}

	public CMISAdapter(Context context, int textViewResourceId)
	{
		super(context, textViewResourceId);
	}
	
	public CMIS getCmis()
	{
		return _cmis;
	}

	public void setCmis(CMIS cmis)
	{
		this._cmis = cmis;
	}

	public void refresh()
	{
		getChildren(_currentState.getFirst());
	}
	
	public void home()
	{
		NodeRef companyHome = _cmis.getCompanyHome();
		
		// Save reference to current entry
		_stack.clear();		
		
		if(companyHome != null)
		{
			// Get Company Home children
			String uuid = companyHome.getContent();
			_currentState = new Pair<String, NodeRef[]>(uuid, null);
			getChildren(uuid);
		}
		else
		{
			handleNetworkStatus();
		}
	}

	protected void handleNetworkStatus()
	{
		Context context = getContext();
		Resources res = context.getResources();
		String text = res.getString(R.string.error_server_error);
		
		switch(_cmis.getNetworkStatus())
		{
		case CONNECTION_ERROR:
			text = res.getString(R.string.error_connection_failed);
			break;
		case CREDENTIALS_ERROR:
			text = res.getString(R.string.error_invalid_credentials);
			break;
		}

		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	
	public boolean isFolder(int position)
	{
		return getItem(position).isFolder();
	}
	
	public boolean hasPrevious()
	{
		return _stack.size() > 0;
	}
	
	public void previous()
	{
		if(_stack.size() > 0)
		{
			_currentState = _stack.pop();
			populateList(_currentState.getSecond());
		}
	}
	
	public void getChildren(int position)
	{
		final NodeRef ref = getItem(position);
		
		if(ref.isFolder())
		{
			_stack.push(_currentState);
			String uuid = ref.getContent();
			_currentState = new Pair<String, NodeRef[]>(uuid, null);
			getChildren(uuid);
		}
		else
		{
			downloadContent(ref,  new Handler() 
			{
				public void handleMessage(Message msg) 
				{
					boolean done = msg.getData().getBoolean("done");
					if(done && _progressDlg != null)
					{	
						_progressDlg.dismiss();
						int bytes = (Integer) _dlThread.getResult();
						
						if(bytes > 0)
						{
							viewContent(ref);
						}
					}			
				}
			});
		}
	}
	
	public void emailContent(int position)
	{
		final NodeRef ref = getItem(position);
		downloadContent(ref, new Handler() 
		{
			public void handleMessage(Message msg) 
			{
				Context context = getContext();
				boolean done = msg.getData().getBoolean("done");
				
				if(done && _progressDlg != null)
				{	
					_progressDlg.dismiss();
					int bytes = (Integer) _dlThread.getResult();
					
					if(bytes > 0)
					{
						Resources res = context.getResources();
						File file = context.getFileStreamPath(ref.getName());
						Uri uri = Uri.fromFile(file);
						Intent emailIntent = new Intent(Intent.ACTION_SEND);
						emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
						emailIntent.putExtra(Intent.EXTRA_SUBJECT, ref.getName());
						emailIntent.putExtra(Intent.EXTRA_TEXT, res.getString(R.string.email_text));
//						emailIntent.setType("message/rfc822");
						try
						{
							context.startActivity(Intent.createChooser(emailIntent, 
									res.getString(R.string.email_title)));
						}
						catch(ActivityNotFoundException e)
						{
							String text = "No suitable applications registered to send " + 
								ref.getContentType();
							int duration = Toast.LENGTH_SHORT;
							Toast toast = Toast.makeText(context, text, duration);
							toast.show();
						}
					}
				}			
			}
		});
	}
	
	protected void downloadContent(final NodeRef ref, Handler handler)
	{
		startProgressDlg();

		_dlThread = new ChildDownloadThread(handler, new Downloadable()
		{
			public Object execute()
			{
				Context context = getContext();
				FileOutputStream fos =  null;
				int bytes = 0;
				
				Builder builder = URLUtils.toUriBuilder(ref.getContent());
				builder.appendQueryParameter("alf_ticket", _cmis.getTicket());

				try
				{
					String name = ref.getName();
					fos = context.openFileOutput(name, Context.MODE_WORLD_READABLE);
					URL url = new URL(builder.build().toString());
					URLConnection conn = url.openConnection();
					bytes = IOUtils.copy(conn.getInputStream(), fos);
					fos.flush();
					fos.close();	
				} 
				catch(Exception e)
				{
					Log.e(CMISAdapter.class.getSimpleName(), "", e);
				}		
				
				return bytes;
			}
		});
		_dlThread.start();		
	}

	protected void viewContent(NodeRef ref)
	{
		Context context = getContext();
		
		// Ask for viewer
		File file = context.getFileStreamPath(ref.getName());
		Uri uri = Uri.fromFile(file);
		Intent viewIntent = new Intent(Intent.ACTION_VIEW);
		viewIntent.setDataAndType(uri, ref.getContentType());
		try
		{
			context.startActivity(viewIntent);
		}
		catch(ActivityNotFoundException e)
		{
			deleteContent(ref);
			String text = "No viewer found for " + ref.getContentType();
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}
	
	protected void deleteContent(NodeRef ref)
	{
		Context context = getContext();
		context.deleteFile(ref.getName());
	}
	
	protected void getChildren(final String uuid)
	{
		startProgressDlg();
		
		_dlThread = new ChildDownloadThread(_resultHandler, new Downloadable()
		{
			public Object execute()
			{
				return _cmis.getChildren(uuid);
			}
		});
		_dlThread.start();
	}

	protected void startProgressDlg()
	{
		Context context = getContext();
		Resources res = context.getResources();
		_progressDlg = ProgressDialog.show(context, "", res.getString(R.string.loading), 
				true, true);		
	}
	
	public void query(final String term)
	{
		startProgressDlg();
		
		_dlThread = new ChildDownloadThread(_resultHandler, new Downloadable()
		{
			public Object execute()
			{
				Context context = getContext();
				Resources res = context.getResources();
				InputStream is = res.openRawResource(
						_cmis.getVersion().contains("3.1") ? R.raw.query : R.raw.query_32);
				String xml = null;
				
				try
				{
					xml = String.format(IOUtils.toString(is), term, term);
					return _cmis.query(xml);
				}
				catch (IOException e)
				{
					Log.e(CMISAdapter.class.getSimpleName(), "", e);
				}
				
				return null;
			}
		});
		_dlThread.start();

	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		TextView textView = (TextView) super.getView(position, convertView, parent);
		NodeRef nodeRef = getItem(position);
		String contentType = nodeRef.getContentType();
		Drawable icon = getDrawableForType(contentType == null ? "cmis/folder" : contentType);
		textView.setCompoundDrawablePadding(5);
		textView.setCompoundDrawables(icon,	null, null, null);
		return textView;
	}
	
	protected Drawable getDrawableForType(String contentType)
	{
		Context context = getContext();
		Resources resources = context.getResources();
		int resId = Constants.mimeMap.get(Constants.mimeMap.containsKey(contentType) ? contentType : null);
		Drawable icon = resources.getDrawable(resId);
		icon.setBounds(new Rect(0, 0, 44, 44));
		return icon;
	}

	protected void populateList(NodeRef[] nodes)
	{
		clear();
		
		if(nodes != null)
		{
			Arrays.sort(nodes, new Comparator<NodeRef>()
					{
				public int compare(NodeRef left, NodeRef right)
				{
					return left.getName().compareTo(right.getName());
				}
					});

			int n = nodes.length;
			for(int i = 0; nodes != null && i < n; i++)
			{
				add(nodes[i]);
			}
		}
	}

	final Handler _resultHandler = new Handler() 
	{
		public void handleMessage(Message msg) 
		{
			boolean done = msg.getData().getBoolean("done");
			if(done && _progressDlg != null)
			{	
				_progressDlg.cancel();
				NodeRef[] nodes = (NodeRef[]) _dlThread.getResult();
				_currentState = new Pair<String, NodeRef[]>(_currentState.getFirst(), nodes);
				populateList(nodes);
			}			
		}
	};

	private interface Downloadable
	{
		public Object execute();
	}
	
	private class ChildDownloadThread extends Thread 
	{
		Handler _handler;
		Downloadable _delegate;
		Object _result = null;

		ChildDownloadThread(Handler h, Downloadable delegate) 
		{
			_handler = h;
			_delegate = delegate;
		}

		public void run() 
		{
			_result = _delegate.execute();

			Message msg = _handler.obtainMessage();
			Bundle b = new Bundle();
			b.putBoolean("done", true);
			msg.setData(b);
			_handler.sendMessage(msg);
		}
		
		public Object getResult()
		{
			return _result;
		}
	}
}
