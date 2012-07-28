package com.zia.freshdocs.widget.fileexplorer.carousel;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class CarouselFrame extends FrameLayout implements Comparable<CarouselFrame> {
 
	private int index;
	private float currentAngle;
	private float x;
	private float y;
	private float z;
	private boolean drawn;
	
	private ImageView image;
	private TextView txt;
	private WindowManager mWinMgr;
	private int displayWidth;
	
	public CarouselFrame(Context context) {
		this(context, null, 0);
	}	
 
	public CarouselFrame(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public CarouselFrame(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mWinMgr = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		displayWidth = mWinMgr.getDefaultDisplay().getWidth();
		image = new ImageView(context);
		LayoutParams imgParams = new LayoutParams(displayWidth, 150);
		imgParams.gravity = Gravity.CENTER;
		this.addView(image, imgParams);
//		this.addView(image, new LayoutParams(displayWidth, 150));
		txt = new TextView(context);
//		this.addView(txt, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT));
	}
	
	public void setImageBitmap(Bitmap img){
		image.setImageBitmap(img);
	}
	
	public void setText(String text){
		txt.setText(text);
//		txt.setTextColor(com.andguru.R.color.white);
		LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_VERTICAL;
		params.setMargins(displayWidth/2 - 25, 28, 0, 0);
		this.addView(txt,params);		
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
 
	public int getIndex() {
		return index;
	}
 

	public void setCurrentAngle(float currentAngle) {
		this.currentAngle = currentAngle;
	}
 
	public float getCurrentAngle() {
		return currentAngle;
	}
 
	public int compareTo(CarouselFrame another) {
		return (int)(another.z - this.z);
	}
 
	public void setX(float x) {
		this.x = x;
	}
 
	public float getX() {
		return x;
	}
 
	public void setY(float y) {
		this.y = y;
	}
 
	public float getY() {
		return y;
	}
 
	public void setZ(float z) {
		this.z = z;
	}
 
	public float getZ() {
		return z;
	}
 
	public void setDrawn(boolean drawn) {
		this.drawn = drawn;
	}
 
	public boolean isDrawn() {
		return drawn;
	}
	
	
}