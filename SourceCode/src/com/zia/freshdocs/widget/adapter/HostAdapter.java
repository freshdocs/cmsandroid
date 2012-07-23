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

import android.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import com.zia.freshdocs.Constants;
import com.zia.freshdocs.preference.CMISHost;

public class HostAdapter extends ArrayAdapter<CMISHost>
{
	private ImageView _errorImage = null;
	private ProgressBar _hostProgressBar = null;
	
	public HostAdapter(Context context, int resource, int textViewResourceId,
			CMISHost[] objects)
	{
		super(context, resource, textViewResourceId, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Context context = getContext();
		Resources res = context.getResources();
		ViewGroup container = (ViewGroup) super.getView(position, convertView, parent);
		TextView child = (TextView) container.getChildAt(0);
		CMISHost host = getItem(position);
		child.setText(host.getHostname());
		
		if(host.getId().equals(Constants.NEW_HOST_ID))
		{
			SpannableStringBuilder sb = new SpannableStringBuilder(
					res.getString(com.zia.freshdocs.R.string.add_server));
			sb.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, sb.length(), 
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			child.setText(sb);
			child.setTextAppearance(context, R.style.TextAppearance_Medium);
			Drawable icon = res.getDrawable(R.drawable.ic_menu_add);
			icon.setBounds(new Rect(0, 0, 25, 25));
			child.setCompoundDrawables(icon, null, null, null);
		}
		else
		{
			child.setTextAppearance(context, R.style.TextAppearance_Large);
			child.setCompoundDrawables(null, null, null, null);
		}
		
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
