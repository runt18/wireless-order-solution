package com.wireless.ui;

import java.io.File;
import java.util.ArrayList;

import com.wireless.common.Params;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class ContentFragment extends Fragment {
	View mCurrentView;
	
   @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState) {  
	   View view = inflater.inflate(R.layout.content_layout, container,false);
	   mCurrentView = view;
	   return view;
   }

	public void onUpdateContent(int position) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
        Gallery mGallery = (Gallery)this.getActivity().findViewById(R.id.gallery1);
        ImageAdapter adapter = new ImageAdapter(this.getActivity());
        mGallery.setAdapter(adapter);

	}
}

class ImageAdapter extends BaseAdapter {

	private Context context;
	// 图片的资源ID
	private ArrayList<String> imgPaths = new ArrayList<String>();

	// 构造函数
	public ImageAdapter(Context context) {
		this.context = context;
		getImages();
	}

	// 返回所有图片的个数
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return imgPaths.size();
	}

	// 返回图片路径
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return imgPaths.get(position);
	}

	// 返回图片在资源的位置
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	// 此方法是最主要的，他设置好的ImageView对象返回给Gallery
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ImageView imageView = new ImageView(context);
		
		String filePath = imgPaths.get(position);
		Bitmap bm = BitmapFactory.decodeFile(filePath);

		// 通过索引获得图片并设置给ImageView
		imageView.setImageBitmap(bm);
		// 设置ImageView的伸缩规格，用了自带的属性值
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

		return imageView;
	}
	
	private void getImages(){
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{
			String sdCardPath = Environment.getExternalStorageDirectory().getPath();
			String imgStoreString = sdCardPath+Params.IMG_STORE_STRING;
			File imgPath = new File(imgStoreString);
			if(imgPath.exists())
			{
				File[] allFiles = imgPath.listFiles();
				if(allFiles != null)
				{
					for(File f : allFiles)
					{
						String imgAbsPath = f.getAbsolutePath();
						if(imgAbsPath.endsWith("jpg")||imgAbsPath.endsWith("gif")||imgAbsPath.endsWith("png")||imgAbsPath.endsWith("bmp"))
						{
							imgPaths.add(imgAbsPath);
						}
					}
				}
			}
		}
	}
}
