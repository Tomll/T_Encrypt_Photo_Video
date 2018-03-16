package com.example.encrypt.photozoom;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import com.example.encrypt.videozoom.VideoItem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.crypto.CipherInputStream;

/**
 * 该类用于：存放自定义的ImageItem对象
 */
public class Bimp {
	public static int max = 0;
	/**
	 * Bimp类中的这个ArryList用于存放自定义的ImageItem对象,VideoItem对象
	 */
	public static ArrayList<ImageItem> tempSelectBitmap = new ArrayList<>();
	public static ArrayList<VideoItem> tempSelectVideo = new ArrayList<>();


	/**
	 * 这个方法 通过循环质量压缩， 将图片的质量压缩到 一个合理的（设置好的）大小
	 * @param bitmap ：待压缩的 bitmap
	 * @param size ：设定的大小（单位kb），压缩后的bitmap的大小不会超过 size (kb)
	 * @return
	 */
	private static Bitmap compressImage(Bitmap bitmap, int size) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		while (baos.toByteArray().length / 1024 > size) { // 循环判断如果压缩后图片是否大于 size(kb),大于继续压缩
			baos.reset();// 重置baos即清空baos
			options -= 10;// 每次都减少10
			bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩为options%，把压缩后的数据存放到baos中

		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap1 = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		return bitmap1;
	}
	
	/**
	 * 拍照图片的采样方法：根据图片存储path获取图片，对图片进行二次采样的方法，
	 * 固定缩放比例为：6 ，
	 * 且return bitmap之前 还会进行图片质量压缩
	 * @return Bitmap
	 */
	public static Bitmap handleBitmap(String path) {
		// 存储缩放比例
		int sampleSize = 6;
		// 创建图片处理类的对象
		BitmapFactory.Options options = new BitmapFactory.Options();
		// 只加载图片的边缘区域，
		options.inJustDecodeBounds = true;
		// 第一采样解码里面的内容bitmap值为null
		BitmapFactory.decodeFile(path, options);
		// 缩放为原图的1/sampleSize；
		options.inSampleSize = sampleSize;
		// 第二次采样解码,加载缩放之后的图片
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		return compressImage(bitmap, 64); //ruturn之前，进行一下图片质量压缩
	}
	
	/**
	 * 相册中选定图片的采样方法：由ContentResolver解析uri中的图片，进行二次采样处理的方法
	 * 固定缩放比例为：6 ，return bitmap之前，进行图片质量压缩
	 * @return Bitmap
	 */
	public static Bitmap handleBitmap(Uri uri, ContentResolver cr) {
		// 存储缩放比例
		int sampleSize = 6;
		// 创建图片处理类的对象
		BitmapFactory.Options options = new BitmapFactory.Options();
		// 只加载图片的边缘区域，
		options.inJustDecodeBounds = true;
		// 第一采样解码里面的内容bitmap值为null
		try {
			BitmapFactory.decodeStream(cr.openInputStream(uri), null, options);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// 缩放为原图的1/sampleSize；当可以
		options.inSampleSize = sampleSize;
		// 第二次采样解码,加载缩放之后的图片
		options.inJustDecodeBounds = false;
		try {
			return compressImage(BitmapFactory.decodeStream(cr.openInputStream(uri), null, options), 768);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * 图片文件输入流的采样方法：对读取的图片文件的InputStream，进行二次采样处理的方法
	 * 固定缩放比例为：6 ，return bitmap之前，进行图片质量压缩
	 * @return Bitmap
	 */
	public static Bitmap handleBitmap(CipherInputStream fis) {
		// 存储缩放比例
		int sampleSize = 6;
		// 创建图片处理类的对象
		BitmapFactory.Options options = new BitmapFactory.Options();
		// 只加载图片的边缘区域，
		options.inJustDecodeBounds = true;
		// 第一采样解码里面的内容bitmap值为null
		BitmapFactory.decodeStream(fis, null, options);
		// 缩放为原图的1/sampleSize；当可以
		options.inSampleSize = sampleSize;
		// 第二次采样解码,加载缩放之后的图片
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeStream(fis, null, options);
	}

	/**
	 * 此方法 用于将Bitmap转换为Base64
	 * @param bitmap
	 * @return Base64（String）
	 */
	public static String bitmapToBase64(Bitmap bitmap) {
		String result = null;
		ByteArrayOutputStream baos = null;
		try {
			if (bitmap != null) {
				baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				baos.flush();
				baos.close();
				byte[] bitmapBytes = baos.toByteArray();
				result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (baos != null) {
					baos.flush();
					baos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	
}
