package com.threepagess.newsclient.utils;

import com.threepagess.newsclient.R;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * 自定义三级缓存图片加载工具
 * 
 * @author Kevin
 * @date 2015-10-24
 */
public class MyBitmapUtils {

	private NetCacheUtils mNetCacheUtils;
	private LocalCacheUtils mLocalCacheUtils;
	private MemoryCacheUtils mMemoryCacheUtils;

	public MyBitmapUtils() {
		mMemoryCacheUtils = new MemoryCacheUtils();
		mLocalCacheUtils = new LocalCacheUtils();
		mNetCacheUtils = new NetCacheUtils(mLocalCacheUtils, mMemoryCacheUtils);
	}

	public void display(ImageView imageView, String url) {
		// 设置默认图片
		imageView.setImageResource(R.drawable.pic_item_list_default);

		// 优先从内存中加载图片, 速度最快, 不浪费流量
		Bitmap bitmap = mMemoryCacheUtils.getMemoryCache(url);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
			System.out.println("从内存加载图片啦");
			return;
		}

		// 其次从本地(sdcard)加载图片, 速度快, 不浪费流量
		bitmap = mLocalCacheUtils.getLocalCache(url);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
			System.out.println("从本地加载图片啦");

			// 写内存缓存
			mMemoryCacheUtils.setMemoryCache(url, bitmap);
			return;
		}

		// 最后从网络下载图片, 速度慢, 浪费流量
		mNetCacheUtils.getBitmapFromNet(imageView, url);
	}

}
