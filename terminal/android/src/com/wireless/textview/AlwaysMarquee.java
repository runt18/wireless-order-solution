package com.wireless.textview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class AlwaysMarquee extends TextView {

	public AlwaysMarquee(Context context) {
	super(context);
	}

	public AlwaysMarquee(Context context, AttributeSet attrs) {
	super(context, attrs);
	}

	public AlwaysMarquee(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
	}

	@Override
	public boolean isFocused() {
	return true;
	}

}
