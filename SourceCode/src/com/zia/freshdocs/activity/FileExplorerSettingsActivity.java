
package com.zia.freshdocs.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import com.zia.freshdocs.R;

/**
 * user-specific settings for FileExplorer
 *
 */
public class FileExplorerSettingsActivity extends Activity {
	private boolean mHiddenChanged = false;
	private boolean mColorChanged = false;
	private boolean mThumbnailChanged = false;
	private boolean mSortChanged = false;
	private boolean mSpaceChanged = false;
	
	private boolean hidden_state;
	private boolean thumbnail_state;
	private int color_state, sort_state, mSpaceState;
	private Intent is = new Intent();
	
	/** 
	 * Called when the activity is first created. 
	 * */	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		
		Intent i = getIntent();
		hidden_state = i.getExtras().getBoolean("HIDDEN");
		thumbnail_state = i.getExtras().getBoolean("THUMBNAIL");
		color_state = i.getExtras().getInt("COLOR");
		sort_state = i.getExtras().getInt("SORT");
		mSpaceState = i.getExtras().getInt("SPACE");
				
		final CheckBox chbHidden = (CheckBox)findViewById(R.id.setting_hidden_box);
		final CheckBox chbThumbnail = (CheckBox)findViewById(R.id.setting_thumbnail_box);
		final CheckBox chbSpace = (CheckBox)findViewById(R.id.setting_storage_box);
		final ImageButton imgBtnColor = (ImageButton)findViewById(R.id.setting_text_color_button);
		final ImageButton imgBtnSort = (ImageButton)findViewById(R.id.settings_sort_button);
		
		chbHidden.setChecked(hidden_state);
		chbThumbnail.setChecked(thumbnail_state);
		chbSpace.setChecked(mSpaceState == View.VISIBLE);
	
		imgBtnColor.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				AlertDialog.Builder builder = new AlertDialog.Builder(FileExplorerSettingsActivity.this);
				CharSequence[] options = {getString(R.string.str_color_black), getString(R.string.str_color_magenta),
										getString(R.string.str_color_yellow), getString(R.string.str_color_red), getString(R.string.str_color_cyan),
										getString(R.string.str_color_blue), getString(R.string.str_color_green), getString(R.string.str_color_white )};
				
				int index = ((color_state & 0x00000000) << 2) % options.length;
				
				builder.setTitle(getString(R.string.str_change_text_color_setting));
				builder.setIcon(R.drawable.color);
				builder.setSingleChoiceItems(options, index, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int index) {
						switch(index) {
							case 0:
								color_state = Color.BLACK;
								is.putExtra("COLOR", color_state);
								mColorChanged = true;
								
								break;
							case 1:
								color_state = Color.MAGENTA;
								is.putExtra("COLOR", color_state);
								mColorChanged = true;
								
								break;
							case 2:
								color_state = Color.YELLOW;
								is.putExtra("COLOR", color_state);
								mColorChanged = true;
								
								break;
							case 3:
								color_state = Color.RED;
								is.putExtra("COLOR", color_state);
								mColorChanged = true;
								
								break;
							case 4:
								color_state = Color.CYAN;
								is.putExtra("COLOR", color_state);
								mColorChanged = true;
								
								break;
							case 5:
								color_state = Color.BLUE;
								is.putExtra("COLOR", color_state);
								mColorChanged = true;
								
								break;
							case 6:
								color_state = Color.GREEN;
								is.putExtra("COLOR", color_state);
								mColorChanged = true;
							case 7:
								color_state = Color.WHITE;
								is.putExtra("COLOR", color_state);
								mColorChanged = true;
									
								break;
						}
					}
				});
				
				builder.create().show();
			}
		});
		
		chbHidden.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				hidden_state = isChecked;
				
				is.putExtra("HIDDEN", hidden_state);
				mHiddenChanged = true;
			}
		});
		
		chbThumbnail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				thumbnail_state = isChecked;
				
				is.putExtra("THUMBNAIL", thumbnail_state);
				mThumbnailChanged = true;
			}
		});
		
		chbSpace.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					mSpaceState = View.VISIBLE;
				}else {
					mSpaceState = View.GONE;
				}
				
				mSpaceChanged = true;
				is.putExtra("SPACE", mSpaceState);				
			}
		});
		
		imgBtnSort.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				AlertDialog.Builder builder = new AlertDialog.Builder(FileExplorerSettingsActivity.this);
    			CharSequence[] options = {"None", "Alphabetical", "Type"};
    			
    			builder.setTitle("Sort by...");
    			builder.setIcon(R.drawable.filter);
    			builder.setSingleChoiceItems(options, sort_state, new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int index) {
						switch(index) {
						case 0:
							sort_state = 0;
							mSortChanged = true;
							is.putExtra("SORT", sort_state);
							break;
							
						case 1:
							sort_state = 1;
							mSortChanged = true;
							is.putExtra("SORT", sort_state);
							break;
							
						case 2:
							sort_state = 2;
							mSortChanged = true;
							is.putExtra("SORT", sort_state);
							break;
						}
					}
				});
    			
    			builder.create().show();
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(!mSpaceChanged)
			is.putExtra("SPACE", mSpaceState);
		
		if(!mHiddenChanged)
			is.putExtra("HIDDEN", hidden_state);
		
		if(!mColorChanged)
			is.putExtra("COLOR", color_state);
		
		if(!mThumbnailChanged)
			is.putExtra("THUMBNAIL", thumbnail_state);
		
		if(!mSortChanged)
			is.putExtra("SORT", sort_state);
			
		setResult(RESULT_CANCELED, is);
	}
}
