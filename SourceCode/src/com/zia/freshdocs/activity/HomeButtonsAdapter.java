package com.zia.freshdocs.activity;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.zia.freshdocs.R;

/**
 * This class is used with a GridView object. It provides a set of ImageCell objects 
 * that support dragging and dropping.
 * 
 * @deprecated - Did not use this class, but tried it with activity_home_with_grid.xml.
 */

public class HomeButtonsAdapter extends BaseAdapter 
{

// Constants
public static final int DEFAULT_NUM_IMAGES = 6;

/**
 */
// Variables

public ViewGroup mParentView = null;
private Context mContext;

// references to our images
private Integer[] mPictureIds = {
        R.drawable.home_button1,
        R.drawable.home_button2,
        R.drawable.home_button3,
        R.drawable.home_button4,
        R.drawable.home_button5,
        R.drawable.home_button6 
        } ;

// references to our labels
private Integer[] mLabelIds = {
        R.string.title_feature1,
        R.string.title_feature2,
        R.string.title_feature3,
        R.string.title_feature4,
        R.string.title_feature5,
        R.string.title_feature6
        } ;

/**
 */
// Constructor

public HomeButtonsAdapter(Context c) 
{
    mContext = c;
}

/**
 */
// Methods

/**
 * getCount
 */

public int getCount() 
{
	return DEFAULT_NUM_IMAGES;
	/*
    Resources res = mContext.getResources();
    int numImages = res.getInteger (R.integer.num_images);    
    return numImages;
    */
}

public Object getItem(int position) 
{
    return null;
}

public long getItemId(int position) {
    return position;
}

/**
 * getView
 * Return a view object that can be used on the home activity screen.
 * 
 * @return ImageCell
 */
public View getView (int position, View convertView, ViewGroup parent) 
{
    mParentView = parent;

    View v = null;
    if (convertView == null) {
        // If it's not recycled, create a new view.
        LayoutInflater li = ((Activity) mContext).getLayoutInflater();
        v = li.inflate (R.layout.activity_home_button, null);

    } else {
        v = (View) convertView;
    }

    // Find the image part and update the image source.
    
    // Find the label part and update the text.

    /*
    v.mCellNumber = position;
    v.mGrid = (GridView) mParentView;
    v.mEmpty = true;
//    v.setBackgroundResource (R.color.drop_target_enabled);
    v.setBackgroundResource (R.color.cell_empty);
    */
    return v;
}


} // end class
