package com.example.encrypt.photozoom;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.Toast;

import com.example.encrypt.R;
import com.example.encrypt.activity.BaseActivity;
import com.example.encrypt.database.DatabaseAdapter;
import com.example.encrypt.database.PsDatabaseHelper;
import com.example.encrypt.util.XorEncryptionUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 这个类用于：将ImageFolders中选定的Folder(含图片的文件夹)中所有图片 以GridView的形式 展示出来
 *
 * @author Tom
 */
public class Album extends BaseActivity implements OnClickListener {
    private GridView gridView;
    private AlbumGridViewAdapter gridImageAdapter;
    //这个静态 成员变量 在FolderGirdViewAdapter的item点击事件中 就已经赋值了
    public static ArrayList<ImageItem> dataList = new ArrayList<ImageItem>();
    private EncryptionTask mTask = null;
    public static ExecutorService executorService; //线程池
    private static DatabaseAdapter databaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        addAppActivity(Album.this);
        findViewById(R.id.button_add).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        executorService = Executors.newFixedThreadPool(20);//创建一个缓存线程池
        databaseAdapter = new DatabaseAdapter(Album.this);//数据库操作工具类
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
        //创建gridView并绑定适配器
        gridView = (GridView) findViewById(R.id.album_GridView);
        gridImageAdapter = new AlbumGridViewAdapter(this, dataList);
        gridView.setAdapter(gridImageAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Bimp.tempSelectBitmap.clear();//退出清空 Bimp.tempSelectBitmap
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
                gridImageAdapter.selectAll(((CheckBox) view).isChecked());
                break;
            case R.id.button_add:
                if (Bimp.tempSelectBitmap.size() == 0) {
                    Toast.makeText(this, getString(R.string.choose_at_least_one_picture), Toast.LENGTH_SHORT).show();
                    break;
                }
                if (mTask != null) {
                    mTask.cancel(true);
                }
                mTask = new EncryptionTask(Bimp.tempSelectBitmap);
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
        private ArrayList<ImageItem> mImageArrayList;
        int startSize;

        public EncryptionTask(ArrayList<ImageItem> imageArrayList) {
            this.mImageArrayList = imageArrayList;
            progressDialog = new ProgressDialog(Album.this);
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
            boolean result = false;
            result = encryptFileList(mImageArrayList); //加密文件集合
/*            int totalTime = 0;
            while (result && databaseAdapter.getPhoto().size() != (startSize + mImageArrayList.size()) && totalTime < mImageArrayList.size()) {
                try {
                    Thread.sleep(2000);
                    totalTime += 2;
                    //Log.d("EncryptionTask", "totalTime:" + totalTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }*/
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            gridImageAdapter.refreshDataAfterEncrypt();
            String showMessage = result ? getString(R.string.encrypt_success) : getString(R.string.partial_picture_encryption_failed);
            Toast.makeText(Album.this, showMessage, Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }

    }

    /**
     * 加密文件集合
     *
     * @param arrayList
     * @return
     */
    boolean result = true;//最后返回的加密结果

/*    public boolean encryptFileList(ArrayList<ImageItem> arrayList) {
        //long l2 = System.currentTimeMillis();
        for (final ImageItem item : arrayList) {
            final String imagePath = item.getImagePath();
            final String privImagePath = imagePath.replaceFirst("/storage/emulated/0", "/data/data/" + getPackageName() + "/files/storage/emulated/0");
            executorService.submit(new Runnable() {
                @Override
                public void run() {
//                    boolean b = AESEncryptionUtil.encryptFile(imagePath, privImagePath);
                    boolean b = XorEncryptionUtil.encrypt(imagePath, privImagePath);
                    if (b) {//加密成功，删除源文件
                        delete(item,privImagePath,getContentResolver());
                    } else { //加密失败，设置结果为false
                        result = b;
                    }
                }
            });
        }
        //long l = System.currentTimeMillis();
        //Log.d("dongrp", "加密for循环耗时:" + (l - l2) + " ms");
        return result;
    }*/

    //同步加密
    public boolean encryptFileList(ArrayList<ImageItem> arrayList) {
        for (final ImageItem item : arrayList) {
            final String imagePath = item.getImagePath();
            final String privImagePath = imagePath.replaceFirst("/storage/emulated/0", "/data/data/" + getPackageName() + "/files/storage/emulated/0");
            boolean b = XorEncryptionUtil.encrypt(imagePath, privImagePath);
            Log.d("Album", "b:" + b);
            if (b) {//加密成功，删除源文件
                delete(item, privImagePath, getContentResolver());
            } else { //加密失败，设置结果为false
                result = b;
            }
        }
        return result;
    }


    /**
     * 明文件删除、明文件数据库条目删除、私密数据库插入
     *
     * @param item
     */
    public static void delete(ImageItem item, String privImagePath, ContentResolver contentResolver) {
        //删除明文件
        File file = new File(item.getImagePath());
        file.delete();
        //删除系统数据库中该条明文件记录
        Uri baseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        contentResolver.delete(baseUri, "_id=?", new String[]{item.getImageId()});
        //将加密后的文件条目插入私密数据库
        Log.d("VideoAlbum", item.toString());
        ContentValues contentValues = new ContentValues();
        contentValues.put(PsDatabaseHelper.FilesClumns._ID, Integer.valueOf(item.getImageId()));
        contentValues.put(PsDatabaseHelper.FilesClumns._DATA, privImagePath);
        contentValues.put(PsDatabaseHelper.FilesClumns._SOURCE_DATA, item.getImagePath());
        contentValues.put(PsDatabaseHelper.FilesClumns._SIZE, Integer.valueOf(item.getSize()));
        contentValues.put(PsDatabaseHelper.FilesClumns._DISPLAY_NAME, item.getDisplayName());
        contentValues.put(PsDatabaseHelper.FilesClumns.TITLE, item.getTitle());
        contentValues.put(PsDatabaseHelper.FilesClumns.DATE_ADDED, Long.valueOf(item.getDateAdded()));
        contentValues.put(PsDatabaseHelper.FilesClumns.MIME_TYPE, item.getMimeType());
        contentValues.put(PsDatabaseHelper.FilesClumns.BUCKET_ID, item.getBucketId());
        contentValues.put(PsDatabaseHelper.FilesClumns.BUCKET_DISPLAY_NAME, item.getBucket_display_name());
        try {
            contentValues.put(PsDatabaseHelper.FilesClumns.WIDTH, Integer.valueOf(item.getWidth()));
            contentValues.put(PsDatabaseHelper.FilesClumns.HEIGHT, Integer.valueOf(item.getHeight()));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        databaseAdapter.insertPhoto(contentValues);
    }

}
