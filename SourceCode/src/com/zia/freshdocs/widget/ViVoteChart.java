package com.zia.freshdocs.widget;

import com.zia.freshdocs.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ViVoteChart extends LinearLayout {
	private DrawRect[] rect = new DrawRect[5];
	private TextView[] stringValue = new TextView[5];
	private int[] voteValue;

	public ViVoteChart(Context paramContext) {
		this(paramContext, null);
	}

	public ViVoteChart(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		View localView = ((LayoutInflater) paramContext
				.getSystemService("layout_inflater")).inflate(
				R.layout.votechart, null);
		addView(localView, new LinearLayout.LayoutParams(-1, -2));
		this.rect[0] = ((DrawRect) localView.findViewById(R.id.rectangChart1));
		this.rect[1] = ((DrawRect) localView.findViewById(R.id.rectangChart2));
		this.rect[2] = ((DrawRect) localView.findViewById(R.id.rectangChart3));
		this.rect[3] = ((DrawRect) localView.findViewById(R.id.rectangChart4));
		this.rect[4] = ((DrawRect) localView.findViewById(R.id.rectangChart5));
		this.stringValue[0] = ((TextView) localView.findViewById(R.id.voteText1));
		this.stringValue[1] = ((TextView) localView.findViewById(R.id.voteText2));
		this.stringValue[2] = ((TextView) localView.findViewById(R.id.voteText3));
		this.stringValue[3] = ((TextView) localView.findViewById(R.id.voteText4));
		this.stringValue[4] = ((TextView) localView.findViewById(R.id.voteText5));
		this.voteValue = new int[5];
	}

	public void setAttribute(int[] paramArrayOfInt) {
		this.voteValue = paramArrayOfInt;
	}
	
	public void createChart(){
		int[] arrayOfInt = this.voteValue;
		int totalValue = 0;
		
		for(int t = 0; t < arrayOfInt.length; t ++){
			totalValue += arrayOfInt[t];
			
		}
		
		for(int i = 0; i < arrayOfInt.length; i ++){
			int voteValue = arrayOfInt[i];
			this.rect[i].setValue(voteValue, totalValue);
			this.stringValue[i].setText(String.valueOf(voteValue));
		}
	}

}
