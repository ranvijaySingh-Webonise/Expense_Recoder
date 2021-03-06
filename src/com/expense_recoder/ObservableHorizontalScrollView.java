package com.expense_recoder;

import com.expense_recoder.interfaces.HorizontalScrollViewListener;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

public class ObservableHorizontalScrollView extends HorizontalScrollView {
private HorizontalScrollViewListener scrollViewListener = null;
	
	public ObservableHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context,attrs,defStyle);
	}
	
	public ObservableHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setHorizontalScrollViewListener(HorizontalScrollViewListener scrollViewListener) {
		this.scrollViewListener = scrollViewListener;
	}
	
	@Override
	protected void onScrollChanged(int x , int y , int oldX,  int oldY) {
		super.onScrollChanged(x, y, oldX, oldY);
		if(scrollViewListener != null) {
			scrollViewListener.onScrollChanged(this, x, y, oldX, oldY);
		}
	}
}
