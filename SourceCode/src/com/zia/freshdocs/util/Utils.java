package com.zia.freshdocs.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zia.freshdocs.R;
import com.zia.freshdocs.model.ViewItem;

public class Utils {
	public static ViewItem createCustomView(Context context, String title, String value) {
		LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout view = (RelativeLayout) mInflater.inflate(R.layout.custom_table, null);
		TextView tvTitle = (TextView) view.findViewById(R.id.title);
		tvTitle.setText(title);

		TextView tvValue = (TextView) view.findViewById(R.id.value);
		tvValue.setText(value);

		ViewItem viewItem = new ViewItem(view);

		return viewItem;
	}
}
