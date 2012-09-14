package com.zia.freshdocs.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class DrawRect extends TextView {

	private Paint rect = new Paint(1);
	private Paint color;
	private int voteValue;
	private int totalVote;

	public DrawRect(Context paramContext) {
		this(paramContext, null);
	}

	public DrawRect(Context paramContext, AttributeSet paramAttributeSet) {
		this(paramContext, paramAttributeSet, 0);
	}

	public DrawRect(Context paramContext, AttributeSet paramAttributeSet,
			int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
		this.rect.setStyle(Paint.Style.FILL);
		this.rect.setColor(Color.argb(40, 42, 170, 255));
		this.color = new Paint(1);
		this.color.setStyle(Paint.Style.FILL);
		this.color.setColor(Color.argb(170, 42, 170, 255));
	}

	public final void setValue(int voteValue, int totalVote) {
		this.totalVote = totalVote;
		this.voteValue = voteValue;
		invalidate();
	}

	public void draw(Canvas paramCanvas) {
		super.draw(paramCanvas);
		int width = getWidth();
		float percent = 0;
		if(this.voteValue == 0){
			paramCanvas.drawRect(0.0F, 3.0F, getWidth(), 28.0F, this.rect);
		}else{
			percent = width * this.voteValue / this.totalVote;
			paramCanvas.drawRect(0.0F, 3.0F, getWidth(), 28.0F, this.rect);
			paramCanvas.drawRect(0.0F, 3.0F, percent, 28.0F, this.color);
		}
	}

}
