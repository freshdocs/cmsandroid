package com.zia.freshdocs.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
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
	
	/**
	 * converts a stream to a string
	 * 
	 * @param inputStream
	 *            stream from the http connection with the server
	 * @return json string from the server
	 */
	public static String convertStreamToString(final InputStream inputStream) {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		try {
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return stringBuilder.toString();
	}
	/**
	 * Get real file path
	 * @param context
	 * @param uri
	 * @return
	 * @throws URISyntaxException
	 */
	public static String getFilePath(Context context, Uri uri) throws URISyntaxException {

		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = { "_data" };
			Cursor cursor = null;

			try {
				cursor = context.getContentResolver().query(uri, projection, null, null, null);
				int column_index = cursor
				.getColumnIndexOrThrow("_data");
				if (cursor.moveToFirst()) {
					return cursor.getString(column_index);
				}
			} catch (Exception e) {
				// Eat it
			}
		}

		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}
	

}
