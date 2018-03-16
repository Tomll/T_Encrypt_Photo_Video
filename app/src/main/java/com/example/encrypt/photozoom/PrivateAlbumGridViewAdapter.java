package com.example.encrypt.photozoom;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.encrypt.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dongrp on 2017/7/13.
 */

public class PrivateAlbumGridViewAdapter extends BaseAdapter {

    private ArrayList<ImageItem> listPrivFlies = new ArrayList<ImageItem>();
    private ArrayList<ImageItem> selectedDataList = new ArrayList<ImageItem>();
    private Context mContext;
    //private BitmapCache cache;
    //private DisplayMetrics dm;

    public PrivateAlbumGridViewAdapter(Context c, ArrayList<ImageItem> list) {
        mContext = c;
        listPrivFlies = list;
        //cache = new BitmapCache();
        //dm = new DisplayMetrics();
        //((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
    }

    /**
     * 适配器 数据全选、取消全选 的方法
     * @param isSelectedAll
     */
    public void selectAll(boolean isSelectedAll){
        Bimp.tempSelectBitmap.clear();
        if (isSelectedAll){
            Bimp.tempSelectBitmap.addAll(listPrivFlies);
        }
        notifyDataSetChanged();
    }

    /**
     * 适配器 获取全部数据集 的方法
     * @return
     */
    public ArrayList<ImageItem> getDataList(){
        return listPrivFlies;
    }

    /**
     * 适配器 获取已选数据集 的方法
     * @return
     */
    public ArrayList<ImageItem> getSelectedData(){
        return Bimp.tempSelectBitmap;
    }

    /**
     * 解密完成后，刷新适配器的方法
     */
    public void refreshDataAfterDecrypt(){
        listPrivFlies.removeAll(Bimp.tempSelectBitmap);
        notifyDataSetChanged();
        Bimp.tempSelectBitmap.clear();
    }


    public int getCount() {
        if (listPrivFlies.size() == 0){
            PrivateAlbum.showNoPictureTip();
        }
        return listPrivFlies.size();
    }

    public ImageItem getItem(int position) {
        return listPrivFlies.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

//    BitmapCache.ImageCallback callback = new BitmapCache.ImageCallback() {
//        @Override
//        public void imageLoad(ImageView imageView, Bitmap bitmap, Object... params) {
//            if (imageView != null && bitmap != null) {
//                String url = (String) params[0];
//                if (url != null && url.equals((String) imageView.getTag())) {
//                    ((ImageView) imageView).setImageBitmap(bitmap);
//                } else {
//                    Log.e(TAG, "callback, bmp not match");
//                }
//            } else {
//                Log.e(TAG, "callback, bmp null");
//            }
//        }
//    };

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_album_gridview, parent, false);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_view);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

/*        String path;
        if (dataList != null && dataList.size() > position)
            path = dataList.get(position).imagePath;
        else
            path = "camera_default";
        if (path.contains("camera_default")) {
            viewHolder.imageView.setImageResource(R.drawable.plugin_camera_no_pictures);
        } else {
            final ImageItem item = dataList.get(position);
            viewHolder.imageView.setTag(item.imagePath);
            cache.displayBmp(viewHolder.imageView, item.thumbnailPath, item.imagePath,callback);
        }*/

        String privImagePath = listPrivFlies.get(position).getImagePath();
        String fileName = privImagePath.substring(privImagePath.lastIndexOf("/") + 1);
        String imagePath = "/data/data/" + mContext.getPackageName() + "/files/" + fileName;
        if (new File(imagePath).exists()){
            Glide.with(mContext).load(imagePath).thumbnail(0.5f).into(viewHolder.imageView);
        }else {
            viewHolder.imageView.setImageResource(R.color.greytext);
        }

        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewHolder.checkBox.isChecked()){
                    Bimp.tempSelectBitmap.add(listPrivFlies.get(position));
                }else {
                    Bimp.tempSelectBitmap.remove(listPrivFlies.get(position));
                }
            }
        });
        //防止滑动的时候由于控件复用而导致数据错乱，所以控件的适配必须有数据源中的内容决定
        if (Bimp.tempSelectBitmap.contains(listPrivFlies.get(position))) {
            viewHolder.checkBox.setChecked(true);
        } else {
            viewHolder.checkBox.setChecked(false);
        }
        //点击item进入Gallery进行单张查看
        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, Gallery.class).putExtra("position",position).putExtra("isFromPrivateAlbum",true));
            }
        });
        return convertView;
    }

    /**
     * 存放列表项控件句柄
     */
    public class ViewHolder {
        public ImageView imageView;
        public CheckBox checkBox;
    }


}
