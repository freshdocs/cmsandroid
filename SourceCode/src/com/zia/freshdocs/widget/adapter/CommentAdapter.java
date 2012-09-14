package com.zia.freshdocs.widget.adapter;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zia.freshdocs.R;

/**
 * Simple Adapter for JSON
 * 
 * Based on {@link ArrayAdapter}
 * 
 */

public class CommentAdapter extends BaseAdapter {
	Context context;
	int layoutResourceId;
	ArrayList<String> data = null;
	private LayoutInflater mInflater;

	public CommentAdapter(Context context, int layoutResourceId, ArrayList<String> data) {
//		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.data = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if (convertView == null) {
			view = mInflater.inflate(layoutResourceId, parent, false);
		} else {
			view = convertView;
		}
		
		TextView username = (TextView) view.findViewById(R.id.txtcomment_username);
		TextView comment = (TextView) view.findViewById(R.id.txtcomment_text);
		TextView date = (TextView) view.findViewById(R.id.txtcomment_date);
		
		String dataStr = data.get(position);
		
		try {
			JSONObject dataObj = new JSONObject(dataStr);
			username.setText(dataObj.isNull("content") ? "" : dataObj.getString("content"));
			comment.setText(dataObj.isNull("username") ? "" : dataObj.getString("username"));
			date.setText(dataObj.isNull("createdOn") ? "" : dataObj.getString("createdOn"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return view;

	}

	@Override
	public int getCount() {
		if (data != null) {
            return data.size();
        } else {
            return 0;
        }
	}

	@Override
	public Object getItem(int index) {
		if (data != null) {
            return data.get(index);
        } else {
            return null;
        }
	}

	@Override
    public long getItemId(int index) {
        return index;
    }

}
