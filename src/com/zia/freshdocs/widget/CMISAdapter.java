package com.zia.freshdocs.widget;

import java.util.Stack;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;

import com.zia.freshdocs.data.NodeRef;
import com.zia.freshdocs.net.CMIS;

public class CMISAdapter extends ArrayAdapter<NodeRef>
{
	private String _currentUuid = null;
	private Stack<String> _stack = new Stack<String>(); 
	private CMIS _cmis;

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
			home();
		}
	}
	
	protected void home()
	{
		NodeRef companyHome = _cmis.getCompanyHome();
		
		// Save reference to current entry
		_stack.clear();		
		_currentUuid = companyHome.getUuid(); 
		
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
		_stack.push(_currentUuid);
		_currentUuid = ref.getUuid();
		getChildren(_currentUuid);
	}
	
	protected void getChildren(String uuid)
	{
		clear();
		
		NodeRef[] nodes = _cmis.getChildren(uuid);
		
		for(int i = 0; i < nodes.length; i++)
		{
			add(nodes[i]);
		}
	}
}
