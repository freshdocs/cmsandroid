package com.zia.freshdocs.widget;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Stack;

import org.apache.commons.io.IOUtils;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.zia.freshdocs.Constants;
import com.zia.freshdocs.R;
import com.zia.freshdocs.model.NodeRef;
import com.zia.freshdocs.net.CMIS;
import com.zia.freshdocs.util.URLUtils;

public class CMISAdapter extends ArrayAdapter<NodeRef>
{	
	private String _currentUuid = null;
	private Stack<String> _stack = new Stack<String>(); 
	private CMIS _cmis;
	private ProgressDialog _progressDlg = null;
	private ChildDownloadThread _dlThread = null;
	

	public CMISAdapter(Context context, int textViewResourceId, NodeRef[] objects)
	{
		super(context, textViewResourceId, objects);
		refresh();
	}

	public CMISAdapter(Context context, int textViewResourceId)
	{
		super(context, textViewResourceId);
		refresh();
	}

	public void refresh()
	{
		if (_cmis == null)
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this
					.getContext());
			_cmis = new CMIS(prefs.getString("hostname", ""), 
					prefs.getString("username", ""),
					prefs.getString("password", ""), 
					Integer.parseInt(prefs.getString("port", "80")));
			String ticket = _cmis.authenticate();

			if (ticket != null)
			{
				home();
			}
		} 
		else
		{
			getChildren(_currentUuid);
		}
	}
	
	protected void home()
	{
		NodeRef companyHome = _cmis.getCompanyHome();
		
		// Save reference to current entry
		_stack.clear();		
		_currentUuid = companyHome.getContent(); 
		
		// Get Company Home children
		getChildren(_currentUuid);		
	}
	
	public boolean hasPrevious()
	{
		return _stack.size() > 0;
	}
	
	public void previous()
	{
		if(_stack.size() > 0)
		{
			_currentUuid = _stack.pop();
			getChildren(_currentUuid);
		}
	}
	
	public void getChildren(int position)
	{
		NodeRef ref = getItem(position);
		
		if(ref.isFolder())
		{
			_stack.push(_currentUuid);
			_currentUuid = ref.getContent();
			getChildren(_currentUuid);
		}
		else
		{
			if(downloadContent(ref) > 0)
			{
				viewContent(ref);
			}
		}
	}
	
	protected int downloadContent(NodeRef ref)
	{
		int bytes = 0;
		Context context = getContext();
		
		// Display the content
		Builder builder = URLUtils.toUriBuilder(ref.getContent());
		builder.appendQueryParameter("alf_ticket", _cmis.getTicket());
		FileOutputStream fos =  null;
		
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
			CharSequence text = "No viewer found for " + ref.getContentType();
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
	
	protected void getChildren(String uuid)
	{
		clear();

		Context context = getContext();
		Resources res = context.getResources();
		_progressDlg = ProgressDialog.show(context, "", res.getString(R.string.loading), 
				true, true);
		
		_dlThread = new ChildDownloadThread(_childrenHandler, uuid);
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

	final Handler _childrenHandler = new Handler() 
	{
		public void handleMessage(Message msg) 
		{
			boolean done = msg.getData().getBoolean("done");
			if(done && _progressDlg != null)
			{	
				_progressDlg.dismiss();
			}
			
			NodeRef[] nodes = _dlThread.getResult();
			
			for(int i = 0; i < nodes.length; i++)
			{
				add(nodes[i]);
			}

		}
	};

	private class ChildDownloadThread extends Thread {
		Handler _handler;
		String _uuid = null;
		NodeRef[] _result = null;

		ChildDownloadThread(Handler h, String uuid) {
			_handler = h;
			_uuid = uuid;
		}

		public void run() 
		{
			_result = _cmis.getChildren(_uuid);

			Message msg = _handler.obtainMessage();
			Bundle b = new Bundle();
			b.putBoolean("done", true);
			msg.setData(b);
			_handler.sendMessage(msg);
		}
		
		public NodeRef[] getResult()
		{
			return _result;
		}
	}

}
