package com.example.encrypt.photozoom;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.encrypt.util.XorEncryptionUtil;

import java.io.File;

/**
 * 自定义的PagerAdapter：用于GalleryActivity界面的ViewPagerFixed的适配
 * 功能：已选图片在画廊进行预览
 *
 * @author Tom
 */
public class GalleryViewPagerAdapter extends PagerAdapter {

    private Context mContext;

    //构造
    public GalleryViewPagerAdapter(Context context) {
        super();
        mContext = context;
    }

    public int getCount() {
        if (Gallery.isFromPrivateAlbum) {
            return PrivateAlbum.dateList.size();
        } else {
            return Album.dataList.size();
        }
    }

    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        PhotoView photoView = new PhotoView(mContext);
        photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                Gallery.switchButtonVisibility();
            }
        });
        if (Gallery.isFromPrivateAlbum) {
            ImageItem item = PrivateAlbum.dateList.get(position);
            String privImagePath = item.getImagePath();
            String fileName = privImagePath.substring(privImagePath.lastIndexOf("/") + 1);
            String imagePath = "/data/data/" + mContext.getPackageName() + "/files/" + fileName;
            //缓存文件夹中如果有该文件，就不需要再次解密了
            if (new File(imagePath).exists()) {
                if (imagePath.endsWith(".gif")){
                    Glide.with(mContext).load(imagePath).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(photoView);
                }else {
                    Glide.with(mContext).load(imagePath).into(photoView);
                }
            } else {
//				File file = AESEncryptionUtil.decryptFile2(privImagePath, imagePath);
                File file = XorEncryptionUtil.encryptToFile(privImagePath, imagePath);
                if (imagePath.endsWith(".gif")){
                    Glide.with(mContext).load(file).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(photoView);
                }else {
                    Glide.with(mContext).load(file).into(photoView);
                }
            }
        } else {
            if (Album.dataList.get(position).getImagePath().endsWith(".gif")) {
                Glide.with(mContext).load(((ImageItem) Album.dataList.get(position)).getImagePath()).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(photoView);
            } else {
                Glide.with(mContext).load(((ImageItem) Album.dataList.get(position)).getImagePath()).into(photoView);
            }
        }
        photoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        container.addView(photoView);
        return photoView;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        //super.destroyItem(container, position, object);
    }

/*	int mChildCount;
    @Override
	public void notifyDataSetChanged() {
		mChildCount = getCount();
		super.notifyDataSetChanged();
	}

	@Override
	public int getItemPosition(Object object)   {
		if ( mChildCount > 0) {
			mChildCount --;
			return POSITION_NONE;
		}
		return super.getItemPosition(object);
	}*/

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }


}
