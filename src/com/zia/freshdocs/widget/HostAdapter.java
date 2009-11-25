package com.zia.freshdocs.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class HostAdapter extends ArrayAdapter<String>
{
	private ImageView _errorImage = null;
	private ProgressBar _hostProgressBar = null;
	
	public HostAdapter(Context context, int textViewResourceId, String[] objects)
	{
		super(context, textViewResourceId, objects);
	}

	public HostAdapter(Context context, int textViewResourceId)
	{
		super(context, textViewResourceId);
	}

	public HostAdapter(Context context, int resource, int textViewResourceId,
			String[] objects)
	{
		super(context, resource, textViewResourceId, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final ViewGroup container = (ViewGroup) super.getView(position, convertView, parent);
		return container; 
	}
	
	public void toggleProgress(View view, boolean active)
	{
		RelativeLayout container = (RelativeLayout) view;
		int childIndex = container.indexOfChild(_hostProgressBar);
		
		if(active && childIndex == -1)
		{
			_hostProgressBar = new ProgressBar(getContext());
			_hostProgressBar.setIndeterminate(true);
			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, 
					LayoutParams.WRAP_CONTENT);
			params.alignWithParent = true;
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			container.addView(_hostProgressBar, params);
		}
		else if(!active && childIndex > -1)
		{
			container.removeView(_hostProgressBar);
		}
	}
	
	public void toggleError(View view, boolean active)
	{
		RelativeLayout container = (RelativeLayout) view;
		int childIndex = container.indexOfChild(_errorImage);
		
		if(active && childIndex == -1)
		{
			_errorImage = new ImageView(getContext());
			_errorImage.setImageResource(android.R.drawable.ic_dialog_alert);
			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, 
					LayoutParams.WRAP_CONTENT);
			params.alignWithParent = true;
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			container.addView(_errorImage, params);
		}
		else if(!active && childIndex > -1)
		{
			container.removeView(_errorImage);
		}
	}
}
