package com.threepagess.newsclient.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

/**
 * 网络缓存
 * 
 * @author Kevin
 * @date 2015-10-24
 */
public class NetCacheUtils {

	private LocalCacheUtils mLocalCacheUtils;
	private MemoryCacheUtils mMemoryCacheUtils;

	public NetCacheUtils(LocalCacheUtils localCacheUtils,
			MemoryCacheUtils memoryCacheUtils) {
		mLocalCacheUtils = localCacheUtils;
		mMemoryCacheUtils = memoryCacheUtils;
	}

	public void getBitmapFromNet(ImageView imageView, String url) {
		// AsyncTask 异步封装的工具, 可以实现异步请求及主界面更新(对线程池+handler的封装)
		new BitmapTask().execute(imageView, url);// 启动AsyncTask
	}

	/**
	 * 三个泛型意义: 第一个泛型:doInBackground里的参数类型 第二个泛型: onProgressUpdate里的参数类型 第三个泛型:
	 * onPostExecute里的参数类型及doInBackground的返回类型
	 * 
	 * @author Kevin
	 * @date 2015-10-24
	 */
	class BitmapTask extends AsyncTask<Object, Integer, Bitmap> {

		private ImageView imageView;
		private String url;

		// 1.预加载, 运行在主线程
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// System.out.println("onPreExecute");
		}

		// 2.正在加载, 运行在子线程(核心方法), 可以直接异步请求
		@Override
		protected Bitmap doInBackground(Object... params) {
			// System.out.println("doInBackground");
			imageView = (ImageView) params[0];
			url = (String) params[1];

			imageView.setTag(url);// 打标记, 将当前imageview和url绑定在了一起

			// 开始下载图片
			Bitmap bitmap = download(url);
			// publishProgress(values) 调用此方法实现进度更新(会回调onProgressUpdate)

			return bitmap;
		}

		// 3.更新进度的方法, 运行在主线程
		@Override
		protected void onProgressUpdate(Integer... values) {
			// 更新进度条
			super.onProgressUpdate(values);
		}

		// 4.加载结束, 运行在主线程(核心方法), 可以直接更新UI
		@Override
		protected void onPostExecute(Bitmap result) {
			// System.out.println("onPostExecute");

			if (result != null) {
				// 给imageView设置图片
				// 由于listview的重用机制导致imageview对象可能被多个item共用,
				// 从而可能将错误的图片设置给了imageView对象
				// 所以需要在此处校验, 判断是否是正确的图片
				String url = (String) imageView.getTag();

				if (url.equals(this.url)) {// 判断图片绑定的url是否就是当前bitmap的url,
											// 如果是,说明图片正确
					imageView.setImageBitmap(result);
					System.out.println("从网络加载图片啦!!!");

					// 写本地缓存
					mLocalCacheUtils.setLocalCache(url, result);
					// 写内存缓存
					mMemoryCacheUtils.setMemoryCache(url, result);
				}
			}

			super.onPostExecute(result);
		}

	}

	// 下载图片
	public Bitmap download(String url) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();

			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);// 连接超时
			conn.setReadTimeout(5000);// 读取超时

			conn.connect();

			int responseCode = conn.getResponseCode();

			if (responseCode == 200) {
				InputStream inputStream = conn.getInputStream();

				// 根据输入流生成bitmap对象
				Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

				return bitmap;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		return null;
	}

}
