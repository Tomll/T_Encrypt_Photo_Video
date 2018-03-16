package com.example.encrypt.videozoom;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.encrypt.R;
import com.example.encrypt.activity.BaseActivity;
import com.example.encrypt.database.DatabaseAdapter;
import com.example.encrypt.database.PsDatabaseHelper;
import com.example.encrypt.photozoom.AlbumHelper;
import com.example.encrypt.photozoom.Bimp;
import com.example.encrypt.util.XorEncryptionUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * 系统所有视频 集界面
 *
 * @author Tom
 */
public class VideoAlbum extends BaseActivity implements OnClickListener {
    private GridView gridView;
    private VideoAlbumGridViewAdapter gridVideoAdapter;
    public static ArrayList<VideoItem> videoList;
    private EncryptionTask mTask = null;
    public static ExecutorService executorService;
    private static DatabaseAdapter databaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        addAppActivity(VideoAlbum.this);

        //视频相关界面统一用蓝色调，以下逻辑修改状态栏颜色
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);//需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.setStatusBarColor(getResources().getColor((R.color.blue)));//设置状态栏颜色
        findViewById(R.id.button_add).setBackgroundColor(getResources().getColor(R.color.blue));

        executorService = Executors.newFixedThreadPool(20);//创建一个缓存线程池
        databaseAdapter = new DatabaseAdapter(VideoAlbum.this);//数据库操作工具类
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    /**
     * view初始化
     */
    private void init() {
        TextView tvTitle = (TextView) findViewById(R.id.title);
        tvTitle.setText(R.string.select_video);
        //系统中所有视频数据
        videoList = AlbumHelper.getSystemVideoList(VideoAlbum.this);
        //创建gridView并绑定适配器
        gridView = (GridView) findViewById(R.id.album_GridView);
        gridVideoAdapter = new VideoAlbumGridViewAdapter(this, videoList);
        gridView.setAdapter(gridVideoAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Bimp.tempSelectVideo.clear();
        if (null != progressDialog && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
/*        if (null != mTask && !mTask.isCancelled()){
            mTask.cancel(true);
        }*/
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_back:
                finish();
                break;
            case R.id.checkbox_select_all:
                ((CheckBox) view).setText(((CheckBox) view).isChecked() ? getString(R.string.deselect_all) : getString(R.string.select_all));
                gridVideoAdapter.selectAll(((CheckBox) view).isChecked());
                break;
            case R.id.button_add:
                if (Bimp.tempSelectVideo.size() == 0) {
                    Toast.makeText(this, getString(R.string.choose_at_least_one_video), Toast.LENGTH_SHORT).show();
                    break;
                }
                if (mTask != null) {
                    mTask.cancel(true);
                }
                mTask = new EncryptionTask(Bimp.tempSelectVideo);
                mTask.execute();
                break;
            default:
                break;
        }

    }

    /**
     * 批量加密异步任务
     */
    public ProgressDialog progressDialog;

    public class EncryptionTask extends AsyncTask<Void, Void, Boolean> {
        private ArrayList<VideoItem> mVideoArrayList;
        int startSize;

        public EncryptionTask(ArrayList<VideoItem> videoArrayList) {
            mVideoArrayList = videoArrayList;
            progressDialog = new ProgressDialog(VideoAlbum.this);
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startSize = databaseAdapter.getPhoto().size();
            progressDialog.setMessage(getString(R.string.encrypting));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result;
            result = encryptVideoList(mVideoArrayList); //加密视频集合
            int totalTime = 0;
            while (result && databaseAdapter.getPhoto().size() != (startSize + mVideoArrayList.size()) && totalTime < mVideoArrayList.size()) {
                try {
                    Thread.sleep(2000);
                    totalTime += 2;
                    Log.d("EncryptionTask", "totalTime:" + totalTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            gridVideoAdapter.refreshDataAfterEncrypt();
            String showMessage = result ? getString(R.string.encrypt_success) : getString(R.string.partial_video_encryption_failed);
            Toast.makeText(VideoAlbum.this, showMessage, Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }

    }

    /**
     * 加密文件集合
     *
     * @param arrayList
     * @return
     */
    boolean result = true;//最终的加密结果(默认为true,若有一个文件加密失败，则最后与的结果就为false)

    public boolean encryptVideoList(ArrayList<VideoItem> arrayList) {
        ArrayList<Future<Boolean>> futures = new ArrayList<>();
        futures.clear();
        //long l2 = System.currentTimeMillis();
        for (final VideoItem item : arrayList) {
            final String videoPath = item.getPath();
            final String privVideoPath = videoPath.replaceFirst("/storage/emulated/0", "/data/data/" + getPackageName() + "/files/storage/emulated/0");
            Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    boolean b = XorEncryptionUtil.encrypt(videoPath, privVideoPath);
                    if (b) {//加密成功，移动视频文件到私密路径
                        deleteVideo(item, privVideoPath, getContentResolver());
                    }
                    return b;
                }
            });
            futures.add(future);
        }
        //long l1 = System.currentTimeMillis();
        for (Future<Boolean> future : futures) {
            try {
                result &= future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        //long l = System.currentTimeMillis();
        //Log.d("VideoAlbum", "遍历加密结果耗时:" + (l - l1)/1000 + " s");
        //Log.d("VideoAlbum", "批量加密循环总耗时:" + (l - l2)/1000 + " s");
        //Log.d("VideoAlbum", "批量加密结果:" + result);
        return result;
    }

    /**
     * 明文件删除、明文件数据库条目删除、私密数据库插入
     *
     * @param item
     */
    public static void deleteVideo(VideoItem item, String privVideoPath, ContentResolver contentResolver) {
        //删除原文件
        new File(item.getPath()).delete();
        //删除系统数据库中该条明文件记录
        contentResolver.delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "_id=?", new String[]{item.getId()});
        //将加密后的文件条目插入私密数据库
        Log.d("VideoAlbum", item.toString());
        ContentValues contentValues = new ContentValues();
        contentValues.put(PsDatabaseHelper.VideoClumns._ID, Integer.valueOf(item.getId()));
        contentValues.put(PsDatabaseHelper.VideoClumns.DATA, privVideoPath);
        contentValues.put(PsDatabaseHelper.VideoClumns.DISPLAY_NAME, item.getDisplayName());
        contentValues.put(PsDatabaseHelper.VideoClumns.SIZE, Integer.valueOf(item.getSize()));
        contentValues.put(PsDatabaseHelper.VideoClumns.MIME_TYPE, item.getMimeType());
        contentValues.put(PsDatabaseHelper.VideoClumns.DATE_ADDED, Long.valueOf(item.getDateAdded()));
        contentValues.put(PsDatabaseHelper.VideoClumns.TITLE, item.getDateAdded());
        contentValues.put(PsDatabaseHelper.VideoClumns.ALBUM, item.getAlbum());
        contentValues.put(PsDatabaseHelper.VideoClumns.BUCKET_ID, item.getBucketId());
        contentValues.put(PsDatabaseHelper.VideoClumns.BUCKET_DISPLAY_NAME, item.getBucketDisplayName());
        try {
            contentValues.put(PsDatabaseHelper.VideoClumns.WIDTH, Integer.valueOf(item.getWidth()));
            contentValues.put(PsDatabaseHelper.VideoClumns.HEIGHT, Integer.valueOf(item.getHeight()));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        databaseAdapter.insertVideo(contentValues);
    }


}
