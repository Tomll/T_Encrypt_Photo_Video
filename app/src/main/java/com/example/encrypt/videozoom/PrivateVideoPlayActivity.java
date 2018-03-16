package com.example.encrypt.videozoom;

import android.content.Context;
import android.content.ContextWrapper;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.encrypt.R;
import com.example.encrypt.activity.BaseActivity;

/**
 * Created by ruipan.dong on 2017/9/26.
 * 私密视频播放界面
 */

public class PrivateVideoPlayActivity extends BaseActivity {
    private VideoView videoView;
    private MyMediaController mediaController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        addAppActivity(PrivateVideoPlayActivity.this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        String videoPath = getIntent().getStringExtra("videoPath");
        videoView = (VideoView) findViewById(R.id.video_view);
        mediaController = new MyMediaController(PrivateVideoPlayActivity.this);
        videoView.setMediaController(mediaController);
        playVideo(videoPath);//播放视频
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    //google 2015/5 为修复VideoView内存泄漏，而增加的方法
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new ContextWrapper(newBase) {
            @Override
            public Object getSystemService(String name) {
                if (Context.AUDIO_SERVICE.equals(name))
                    return getApplicationContext().getSystemService(name);
                return super.getSystemService(name);
            }
        });
    }

    /**
     * 播放视频
     */
    public void playVideo(String videoPath) {
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();// 播放
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                finish();
            }
        });
    }

    //自定义MediaController
    class MyMediaController extends MediaController {

        public MyMediaController(Context context) {
            super(context);
        }

        @Override
        public void show() {
            show(5 * 1000);
        }
    }


}
