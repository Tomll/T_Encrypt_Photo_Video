package com.example.encrypt.videozoom;

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
import com.example.encrypt.photozoom.Bimp;

import java.util.ArrayList;

/**
 * Created by dongrp on 2017/7/13.
 */

public class PrivateVideoAlbumGridViewAdapter extends BaseAdapter {

    private ArrayList<VideoItem> listPrivFlies = new ArrayList<>();
    private Context mContext;

    public PrivateVideoAlbumGridViewAdapter(Context c, ArrayList<VideoItem> list) {
        mContext = c;
        listPrivFlies = list;
    }

    /**
     * 适配器 数据全选、取消全选 的方法
     *
     * @param isSelectedAll
     */
    public void selectAll(boolean isSelectedAll) {
        Bimp.tempSelectVideo.clear();
        if (isSelectedAll) {
            Bimp.tempSelectVideo.addAll(listPrivFlies);
        }
        notifyDataSetChanged();
    }

    /**
     * 解密完成后，刷新适配器的方法
     */
    public void refreshDataAfterDecrypt() {
        listPrivFlies.removeAll(Bimp.tempSelectVideo);
        notifyDataSetChanged();
        Bimp.tempSelectVideo.clear();
    }

    public int getCount() {
        if (listPrivFlies.size() == 0) {
            PrivateVideoAlbum.showNoPictureTip();
        }
        return listPrivFlies.size();
    }

    public VideoItem getItem(int position) {
        return listPrivFlies.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_video_album_gridview, parent, false);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_view);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            viewHolder.imagePlay = (ImageView) convertView.findViewById(R.id.image_play);
            viewHolder.imagePlay.setVisibility(View.VISIBLE);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //加载视频缩略图
        Glide.with(mContext).load(listPrivFlies.get(position).getPath()).thumbnail(0.5f).placeholder(R.color.greytext).into(viewHolder.imageView);
        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewHolder.checkBox.isChecked()) {
                    Bimp.tempSelectVideo.add(listPrivFlies.get(position));
                } else {
                    Bimp.tempSelectVideo.remove(listPrivFlies.get(position));
                }
            }
        });
        //防止滑动的时候由于控件复用而导致数据错乱，所以控件的适配必须有数据源中的内容决定
        if (Bimp.tempSelectVideo.contains(listPrivFlies.get(position))) {
            viewHolder.checkBox.setChecked(true);
        } else {
            viewHolder.checkBox.setChecked(false);
        }
        //点击item进入Gallery进行单张查看
        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳到VideoActivity播放视频
                mContext.startActivity(new Intent(mContext, PrivateVideoPlayActivity.class).putExtra("videoPath", listPrivFlies.get(position).getPath()));
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
        public ImageView imagePlay;
    }


}
