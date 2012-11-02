package com.wireless.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ShadowImageView extends ImageView {

	private Paint mPaint;

	public ShadowImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        mPaint = new Paint();  
	}

	public ShadowImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
        mPaint = new Paint();  

	}

	public ShadowImageView(Context context) {
		super(context);
        mPaint = new Paint();  
	}

    @Override  
    protected void onDraw(Canvas canvas) {  
        super.onDraw(canvas);  
        // 画边框  
        Rect rect1 = getRect(canvas);  
        
        mPaint.setColor(Color.BLACK);  
        mPaint.setStyle(Paint.Style.STROKE); 
        
        // 画边框  
//        canvas.drawRect(rect1, mPaint);  
          
//        mPaint.setColor(Color.LTGRAY);  
  
        // 画一条竖线,模拟右边的阴影  
        canvas.drawLine(rect1.right + 1, rect1.top + 2, rect1.right + 1,  
                rect1.bottom + 2, mPaint);  
        // 画一条横线,模拟下边的阴影  
        canvas.drawLine(rect1.left + 2, rect1.bottom + 1, rect1.right + 2,  
                rect1.bottom + 1, mPaint);  
          
        // 画一条竖线,模拟右边的阴影  
        canvas.drawLine(rect1.right + 2, rect1.top + 3, rect1.right + 2,  
                rect1.bottom + 3, mPaint);  
        // 画一条横线,模拟下边的阴影  
        canvas.drawLine(rect1.left + 3, rect1.bottom + 2, rect1.right + 3,  
                rect1.bottom + 2, mPaint);  
    }  
  
    Rect getRect(Canvas canvas) {  
        Rect rect = canvas.getClipBounds();  
        rect.bottom -= getPaddingBottom();  
        rect.right -= getPaddingRight();  
        rect.left += getPaddingLeft();  
        rect.top += getPaddingTop();  
        return rect;  
    }  
}
