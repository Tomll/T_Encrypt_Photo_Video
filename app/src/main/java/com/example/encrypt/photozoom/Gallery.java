package com.example.encrypt.photozoom;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.encrypt.R;
import com.example.encrypt.activity.BaseActivity;
import com.example.encrypt.util.XorEncryptionUtil;


/**
 * 这个类是用于：对选定图片  进行预览
 *
 * @author Tom
 */
public class Gallery extends BaseActivity implements OnClickListener, OnPageChangeListener {
    private Intent intent;
    public static boolean isFromPrivateAlbum;
    private int location;//当前的位置
    private static Button buttonAdd, buttonMin;
    private ProgressDialog progressDialog;
    private GalleryViewPagerAdapter adapter;
    private ViewPagerFixed pager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();// 隐藏掉ActionBar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_gallery);
        addAppActivity(Gallery.this);
    }

    protected void onResume() {
        super.onResume();
        initViewAndCtrl(); //初始化view 和 ctrl
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

/*    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }*/

    /**
     * 初始化view和适配器ctrl
     */
    private void initViewAndCtrl() {
        intent = getIntent();
        isFromPrivateAlbum = intent.getBooleanExtra("isFromPrivateAlbum", false);
        //加密、解密按钮
        progressDialog = new ProgressDialog(Gallery.this);
        buttonAdd = (Button) findViewById(R.id.buttonAdd);
        buttonMin = (Button) findViewById(R.id.buttonMin);
        buttonAdd.setOnClickListener(this);
        buttonMin.setOnClickListener(this);
        if (isFromPrivateAlbum) {//私密相册
            buttonAdd.setVisibility(View.GONE);
            buttonMin.setVisibility(View.VISIBLE);
        } else {
            buttonAdd.setVisibility(View.VISIBLE);
            buttonMin.setVisibility(View.GONE);
        }

        //ViewPagerFixed
        pager = (ViewPagerFixed) findViewById(R.id.gallery01);
        pager.setOnPageChangeListener(this); //注册滑动监听
        //GalleryViewPagerAdapter
        adapter = new GalleryViewPagerAdapter(Gallery.this);
        pager.setAdapter(adapter); //绑定适配器
        //调用者希望从第几张开始显示,就setCurrentItem到目标位置
        int id = intent.getIntExtra("position", 0);
        pager.setCurrentItem(id);
    }

    //监听ViewPager滑动的三个方法
    @Override
    public void onPageSelected(int arg0) {
        location = arg0;
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    //点击监听
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonAdd://加密
                new SingleEncryptionOrDecryptionTask().execute();
                break;
            case R.id.buttonMin://解密
                new SingleEncryptionOrDecryptionTask().execute();
                break;
            default:
                break;
        }
    }

    /**
     * 单个文件 加密 或 解密 的异步任务，通过isFromPrivateAlbum区分加密、解密
     */
    public class SingleEncryptionOrDecryptionTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setCancelable(false);
            progressDialog.setMessage(isFromPrivateAlbum ? getString(R.string.decrypting) : getString(R.string.encrypting));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;
            if (isFromPrivateAlbum) {//从私密相册来，肯定是要解密了
                result = decryptSinglePhoto();//解密单张图片
            } else {//从正常相册来，肯定是要加密了
                result = encryptSinglePhoto();//加密单张图片
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (adapter.getCount() == 0) {
                finish();
            } else {
                adapter.notifyDataSetChanged();
            }
            String showMessage = result ? getString(R.string.success) : getString(R.string.fail);
            Toast.makeText(Gallery.this, showMessage, Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }

    }

    /**
     * 加密单张图片的方法
     */
    public boolean encryptSinglePhoto() {
        ImageItem item = Album.dataList.get(location);
        String imagePath = item.getImagePath();
        String privImagePath = imagePath.replaceFirst("/storage/emulated/0", "/data/data/" + getPackageName() + "/files/storage/emulated/0");
//        boolean b = AESEncryptionUtil.encryptFile(imagePath, privImagePath);
        boolean b = XorEncryptionUtil.encrypt(imagePath, privImagePath);
        if (b) {//成功
            if (Bimp.tempSelectBitmap.contains(Album.dataList.get(location))) {
                Bimp.tempSelectBitmap.remove(Album.dataList.get(location));
            }
            Album.dataList.remove(location);
            //adapter.notifyDataSetChanged(); 在异步任务的子线程中不能刷新UI线程，所以注释了
            Album.delete(item, privImagePath, getContentResolver());
            return true;
        } else {//失败
            return false;
        }
    }

    /**
     * 解密单张图片
     */
    public boolean decryptSinglePhoto() {
        ImageItem item = PrivateAlbum.dateList.get(location);
        String privImagePath = item.getImagePath(); //这个私密文件的绝对路径
        String imagePath = privImagePath.replaceFirst("/data/data/" + getPackageName() + "/files/storage/emulated/0", "/storage/emulated/0");
//        boolean b = AESEncryptionUtil.decryptFile(privImagePath, imagePath);
        boolean b = XorEncryptionUtil.encrypt(privImagePath, imagePath);
        if (b) {
            if (Bimp.tempSelectBitmap.contains(PrivateAlbum.dateList.get(location))) {
                Bimp.tempSelectBitmap.remove(PrivateAlbum.dateList.get(location));
            }
            PrivateAlbum.dateList.remove(location);
            PrivateAlbum.delete(item, imagePath, getContentResolver());
            return true;
        } else {
            return false;
        }
    }

    /**
     * 切换 “加密” “解密” 按钮的显示与隐藏
     */
    static boolean isHide = false;

    public static void switchButtonVisibility() {
        if (!isHide && !isFromPrivateAlbum) {
            ObjectAnimator.ofFloat(buttonAdd, "translationY", 0, 300).setDuration(200).start();
            isHide = true;
        } else if (isHide && !isFromPrivateAlbum) {
            ObjectAnimator.ofFloat(buttonAdd, "translationY", 300, 0).setDuration(200).start();
            isHide = false;
        } else if (!isHide && isFromPrivateAlbum) {
            ObjectAnimator.ofFloat(buttonMin, "translationY", 0, 300).setDuration(200).start();
            isHide = true;
        } else if (isHide && isFromPrivateAlbum) {
            ObjectAnimator.ofFloat(buttonMin, "translationY", 300, 0).setDuration(200).start();
            isHide = false;
        }
    }


}
