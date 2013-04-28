package com.wireless.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.excep.ProtocolException;
import com.wireless.ordermenu.R;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.util.imgFetcher.ImageFetcher;

/**
 * the dialog which contains an image and some operations to control the food
 * @author ggdsn1
 *
 */
public class ImageDialog extends Dialog {
	private ImageFetcher mImageFetcher;
	private Food mFood;
	
	public ImageDialog(Context context, int theme, Food f) {
		super(context, theme);
		mFood = f;
	}

	public ImageDialog(Context context, Food f) {
		super(context);
		mFood =f;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.recommend_food_dialog);
		
		int width = 900;
		int height = 500;
		Window dialogWindow = getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.width = width;
		lp.height = height;
		dialogWindow.setAttributes(lp);
		
		mImageFetcher = new ImageFetcher(this.getContext(), width, height);
		ImageView imageView = (ImageView) findViewById(R.id.imageView_recDialog);
		imageView.setScaleType(ScaleType.CENTER_CROP);
		mImageFetcher.loadImage(mFood.image, imageView);
		
		final EditText countEditText = (EditText) findViewById(R.id.editText_count_rec_dialog);
		//设置数量加加
		((ImageButton)  findViewById(R.id.imageButton_plus_rec_dialog)).setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				if(!countEditText.getText().toString().equals(""))
				{
					float curNum = Float.parseFloat(countEditText.getText().toString());
					countEditText.setText(NumericUtil.float2String2(++curNum));
				}
			}
		});
		
		//设置数量减
		((ImageButton) findViewById(R.id.imageButton_minus_recommendDialog)).setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				if(!countEditText.getText().toString().equals(""))
				{
					float curNum = Float.parseFloat(countEditText.getText().toString());
					if(--curNum >= 1)
					{
						countEditText.setText(NumericUtil.float2String2(curNum));
					}
				}
			}
		});
		//点菜按钮
		((ImageButton) findViewById(R.id.imageButton_addFood_rec_dialog)).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				OrderFood mOrderFood = new OrderFood(mFood);
				float oriCnt = mOrderFood.getCount();
				try{
					mOrderFood.setCount(Float.parseFloat(((EditText)  findViewById(R.id.editText_count_rec_dialog)).getText().toString()));
					ShoppingCart.instance().addFood(mOrderFood);
//					Toast.makeText(getContext(), mOrderFood.name + "已添加", Toast.LENGTH_SHORT).show();
					dismiss();
				}catch(ProtocolException e){
					mOrderFood.setCount(oriCnt);
					Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
		//关闭按钮
		((ImageButton) findViewById(R.id.imageButton_close_rec_dialog)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				 dismiss();
			}
		});
		//价格和名称的显示
		((TextView)  findViewById(R.id.textView_price_rec_dialog)).setText(NumericUtil.float2String2(mFood.getPrice()));
		((TextView)  findViewById(R.id.textView_food_name_recommend_dialog)).setText(mFood.getName());
	}

}
