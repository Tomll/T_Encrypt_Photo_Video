package com.example.encrypt.videozoom;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.encrypt.R;
import com.example.encrypt.photozoom.Bimp;
import com.example.encrypt.photozoom.BitmapCache;

import java.util.ArrayList;


/**
 * 适配器：将系统视频，以GridView的形式进行展示
 *
 * @author Tom
 */
public class VideoAlbumGridViewAdapter extends BaseAdapter {

    private final String TAG = getClass().getSimpleName();
    private ArrayList<VideoItem> dataList;
    private Context context;
    private BitmapCache cache;

    public VideoAlbumGridViewAdapter(Context context, ArrayList<VideoItem> dataList) {
        this.context = context;
        this.dataList = dataList;
        cache = new BitmapCache();
    }

    /**
     * 数据全选、取消全选 的方法
     */
    public void selectAll(boolean selectAll) {
        Bimp.tempSelectVideo.clear();
        if (selectAll) {
            Bimp.tempSelectVideo.addAll(dataList);
        }
        notifyDataSetChanged();
    }

    /**
     * 解密完成后，刷新适配器的方法
     */
    public void refreshDataAfterEncrypt() {
        dataList.removeAll(Bimp.tempSelectVideo);
        notifyDataSetChanged();
        Bimp.tempSelectVideo.clear();
    }

    public int getCount() {
        return dataList.size();
    }

    public Object getItem(int position) {
        return dataList.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    /**
     * 存放列表项控件句柄
     */
    private class ViewHolder {
        public ImageView imageView;
        public CheckBox checkBox;
        public ImageView imagePlay;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_video_album_gridview, parent, false);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_view);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            viewHolder.imagePlay = (ImageView) convertView.findViewById(R.id.image_play);
            viewHolder.imagePlay.setVisibility(View.VISIBLE);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //加载视频缩略图
        Glide.with(context).load(dataList.get(position).getPath()).thumbnail(0.5f).into(viewHolder.imageView);
        viewHolder.checkBox.setTag(position);
        viewHolder.checkBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewHolder.checkBox.isChecked()) {
                    Bimp.tempSelectVideo.add(dataList.get(position));
                } else {
                    Bimp.tempSelectVideo.remove(dataList.get(position));
                }
            }
        });
        //防止滑动的时候由于控件复用而导致数据错乱，所以控件的适配必须有数据源中的内容决定
        if (Bimp.tempSelectVideo.contains(dataList.get(position))) {
            viewHolder.checkBox.setChecked(true);
        } else {
            viewHolder.checkBox.setChecked(false);
        }
        //点击viewHolder.imageView 开始播放视频
        viewHolder.imageView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Uri uri = Uri.parse(dataList.get(position).getPath());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "video/*");
                context.startActivity(intent);
            }
        });
        return convertView;
    }

}
