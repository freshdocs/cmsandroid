package com.zia.freshdocs.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

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
//		Context context = getContext();
//		Resources res = context.getResources();
		ViewGroup container = (ViewGroup) super.getView(position, convertView, parent);
		CMISHost host = getItem(position);
		TextView child = (TextView) container.getChildAt(0);
		child.setText(host.getHostname());
		
//		if(position == getCount() - 1)
//		{
//			TextView child = (TextView) container.getChildAt(0);
//			child.setText(host.getHostname());
//			SpannableStringBuilder sb = new SpannableStringBuilder(child.getText());
//			sb.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, sb.length(), 
//					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//			child.setText(sb);
//			child.setTextAppearance(context, R.style.TextAppearance_Medium);
//			Drawable icon = res.getDrawable(R.drawable.ic_menu_add);
//			icon.setBounds(new Rect(0, 0, 25, 25));
//			child.setCompoundDrawables(icon, null, null, null);
//		}
		
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
