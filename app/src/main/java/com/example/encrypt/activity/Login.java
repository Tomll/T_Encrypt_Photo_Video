package com.example.encrypt.activity;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.encrypt.R;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by dongrp on 2016/8/16. 登录界面
 */
@SuppressLint("NewApi")
public class Login extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, EditText.OnEditorActionListener {
    private static String TAG = "Login_1";
    private TextView textView1, textView2, textView3;
    private FrameLayout frameLayout2;
    private EditText editText1, editText2;
    private CheckBox checkBox1, checkBox2;
    private ContentResolver contentResolver;
    public static SharedPreferences sp;
    public static SharedPreferences.Editor editor;
    public static final String PRIVATE_SPACE_SP = "share_preference";
    public static final String PRIVATE_SPACE_PWD = "private_space_password";
    public static final String FirstRun = "first_run";
    public static final String ChangePrivateMarkFromAdvancedSetup = "change_private_mark1";
    public static final String ChangePrivateMarkFromSecurityQuestionActivity = "change_private_mark2";
    private boolean isFirstRun = true; // 本地全局标志：程序是否是第一次运行
    public static boolean isChangePrivateMark = false; // 本地全局标志：是否修改密码
    private boolean resetPrivateMarkFromAdvancedSetup = false; // AdvancedSetupActivity发送过来的是否修改密码的标志
    private boolean resetPrivateMarkFromSecurityQuestion = false; // SecurityQuestionActivity发送过来的是否修改密码的标志
    public static FingerprintManager mFingerprintManager;
    public static KeyguardManager mKeyManager;
    private ImageView btFingerprint;//“指纹”按钮：点击该按钮弹出指纹验证框
    private TextView tip_finger_print_login;//“点击进行指纹登录”
    private AlertDialog dialogFingerPrint;//指纹验证框
    private View fingerPrintView;
    private ImageView ivFingerPrint;//指纹验证框中的指纹图片
    private final static int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 1689;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        addAppActivity(Login.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1655);
        }
        initData();
        initView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1655) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        if (null != dialogFingerPrint && dialogFingerPrint.isShowing()) {//avoid android.view.WindowLeaked
            dialogFingerPrint.dismiss();
        }
        stopFingerprint();//注销指纹监听
        super.onPause();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mKeyManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        mFingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);
        contentResolver = getContentResolver();
        sp = getSharedPreferences(PRIVATE_SPACE_SP, MODE_PRIVATE);
        editor = sp.edit();
        isFirstRun = sp.getBoolean(FirstRun, true);
        resetPrivateMarkFromAdvancedSetup = getIntent().getBooleanExtra(ChangePrivateMarkFromAdvancedSetup, false);
        resetPrivateMarkFromSecurityQuestion = getIntent().getBooleanExtra(ChangePrivateMarkFromSecurityQuestionActivity, false);
        if (resetPrivateMarkFromSecurityQuestion) {
            isChangePrivateMark = true;
        }
    }

    /**
     * 初始化view
     */
    private void initView() {
        textView1 = (TextView) findViewById(R.id.textView1);// 标题
        textView2 = (TextView) findViewById(R.id.textView2);// 提示信息
        textView3 = (TextView) findViewById(R.id.textView3);// “忘记密码”
        btFingerprint = (ImageView) findViewById(R.id.bt_finger_print);//“指纹”按钮
        tip_finger_print_login = (TextView) findViewById(R.id.tip_finger_print_login);//“点击进行指纹登录”提示语
        fingerPrintView = LayoutInflater.from(this).inflate(R.layout.finger_print_view, null);
        ivFingerPrint = (ImageView) fingerPrintView.findViewById(R.id.iv_finger_print);//验证框中的指纹图片
        dialogFingerPrint = new AlertDialog.Builder(this).setCancelable(false).setView(fingerPrintView).create();
        frameLayout2 = (FrameLayout) findViewById(R.id.frame_layout2);
        editText1 = (EditText) findViewById(R.id.editText1);
        editText2 = (EditText) findViewById(R.id.editText2);
        editText1.setOnEditorActionListener(this);
        editText2.setOnEditorActionListener(this);
        checkBox1 = (CheckBox) findViewById(R.id.checkBox1);
        checkBox2 = (CheckBox) findViewById(R.id.checkBox2);
        checkBox1.setOnCheckedChangeListener(this);
        checkBox2.setOnCheckedChangeListener(this);
        // 根据isFirstRun和isChangePrivateMark的状态值，决定所展示的布局
        if (!isFirstRun && !isChangePrivateMark) { // 日常登录
            textView1.setText(getString(R.string.confirm_private_mark));
            //textView2.setText(getString(R.string.login_by_private_mark_or_private_fingerprint));
            textView2.setVisibility(View.GONE);
            textView3.setVisibility(View.VISIBLE);
            frameLayout2.setVisibility(View.GONE);
        }
        //注册指纹监听
        if (!isFirstRun && hasFingerMode() && sp.getBoolean("enterByPrivateFingerprint", false)
                && !resetPrivateMarkFromAdvancedSetup && !resetPrivateMarkFromSecurityQuestion) {
            btFingerprint.setVisibility(View.VISIBLE);
            tip_finger_print_login.setVisibility(View.VISIBLE);
        } else if (isFirstRun) {
            //app首次运行,默认打开指纹登录开关
            BseApplication.editor.putBoolean("enterByPrivateFingerprint", true).commit();
        }
    }

    // 点击监听回调
    @Override
    public void onClick(View v) {
        //startActivity(new Intent(Login.this, PrivateContactsActivity.class));
        switch (v.getId()) {
            case R.id.textView3: // “忘记密码”按钮
                startActivity(new Intent(Login.this, SecurityQuestion.class));
                if (resetPrivateMarkFromAdvancedSetup) { // 从设置界面跳来，修改密码：直接finish，
                    finish();
                }
                break;
            case R.id.button1: // “登录”按钮
                if (checkInput()) {
                    loginOrResetPwd(); // 执行主业务逻辑
                }
                break;
            case R.id.bt_finger_print://指纹登录按钮
                if (isFingerEnable(this)) {
                    dialogFingerPrint.show();
                    startFingerprint();
                }
                break;
            case R.id.cancel://指纹验证框中取消按钮
                stopFingerprint();
                dialogFingerPrint.dismiss();
                break;
        }
    }

    /**
     * 点击软键盘DONE按钮，同样执行提交逻辑
     */
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            if (checkInput()) {
                loginOrResetPwd(); // 执行主业务逻辑
            }
            return true;
        }
        return false;
    }

    /**
     * 点击“提交”按钮后的主要业务逻辑：处理日常登录业务 和 密码修改业务
     */
    public void loginOrResetPwd() {
        if (!isFirstRun && !isChangePrivateMark) { // 日常登录
            if (md5(md5(editText1.getText().toString().trim())).equals(BseApplication.sp.getString(PRIVATE_SPACE_PWD, null))) { // 验证成功
                if (resetPrivateMarkFromAdvancedSetup) { // 验证成功后：修改密码
                    isChangePrivateMark = true;
                    textView1.setText(getString(R.string.set_private_mark));
                    textView2.setText(getString(R.string.please_set_private_mark));
                    textView3.setVisibility(View.GONE);
                    frameLayout2.setVisibility(View.VISIBLE);
                    editText1.setText(null);
                } else { // 验证成功后：日常登录
                    startActivity(new Intent(Login.this, Main.class));
                    finish();
                }
            } else { // 验证失败
                Toast.makeText(this, getString(R.string.wrong_private_mark), Toast.LENGTH_LONG).show();
                editText1.setText(null);
            }
        } else if (!isFirstRun && isChangePrivateMark) { // 日常修改密码
            BseApplication.editor.putString(PRIVATE_SPACE_PWD, md5(md5(editText1.getText().toString().trim())));
            BseApplication.editor.commit();
            isChangePrivateMark = false;
            Toast.makeText(this, getString(R.string.successful_reset_private_mark), Toast.LENGTH_SHORT).show();
            finish();// finish()后露出第一个登录界面 或 高级设置界面
        } else if (isFirstRun) { // 第一次运行app
            BseApplication.editor.putString(PRIVATE_SPACE_PWD, md5(md5(editText1.getText().toString().trim())));
            BseApplication.editor.commit();
            Toast.makeText(this, getString(R.string.successful_set_private_mark), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Login.this, SecurityQuestion.class));
            finish();
        }
    }

    /**
     * 输入检查
     *
     * @return boolean
     */
    public boolean checkInput() {
        if (!isFirstRun && !isChangePrivateMark) { // 登录输入检验
            if (TextUtils.isEmpty(editText1.getText().toString().trim())) {
                Toast.makeText(this, getString(R.string.password_cant_be_empty), Toast.LENGTH_LONG).show();
                return false;
            }
            if (editText1.getText().toString().trim().length() < 6) {
                Toast.makeText(this, getString(R.string.the_length_of_password_must_be_6), Toast.LENGTH_LONG).show();
                return false;
            }
        } else { // 修改密码输入检验
            if (TextUtils.isEmpty(editText1.getText().toString().trim())
                    || TextUtils.isEmpty(editText2.getText().toString().trim())) {
                Toast.makeText(this, getString(R.string.password_cant_be_empty), Toast.LENGTH_LONG).show();
                return false;
            }
            if (editText1.getText().toString().trim().length() < 6
                    || editText2.getText().toString().trim().length() < 6) {
                Toast.makeText(this, getString(R.string.the_length_of_password_must_be_6), Toast.LENGTH_LONG).show();
                return false;
            }
            if (!TextUtils.equals(editText1.getText().toString().trim(), editText2.getText().toString().trim())) {
                Toast.makeText(this, getString(R.string.two_different_input), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    // checkBox的CheckedChanged监听回调,用于设置密码的显示与隐藏
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.checkBox1:
                if (isChecked) { // 显示密码
                    editText1.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else { // 隐藏密码
                    editText1.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                break;
            case R.id.checkBox2:
                if (isChecked) {
                    editText2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    editText2.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                break;
        }
    }

    /**
     * MD5 加密算法
     */
    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    // 返回键
    @Override
    public void onBackPressed() {
        if (!isFirstRun && isChangePrivateMark) { // 处于修改密码界面
            isChangePrivateMark = false;
        }
        finish();
    }

    /**
     * 以下内容为指纹解锁功能
     */
    private static final long LOCKOUT_DURATION = 30000;//需要等待30S
    private Boolean mInFingerprintLockout = false;//判断是否失败5次，指纹上锁
    private CancellationSignal mCancellationSignal;//取消指纹验证的信号类

    /**
     * 指纹错误动画
     */
    public void startFingerWrongAnimation() {
        ObjectAnimator animator0 = ObjectAnimator.ofFloat(ivFingerPrint, "translationX", 0, -40);
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(ivFingerPrint, "translationX", -40, 40);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(ivFingerPrint, "translationX", 40, -40);
        ObjectAnimator animator3 = ObjectAnimator.ofFloat(ivFingerPrint, "translationX", -40, 40);
        ObjectAnimator animator4 = ObjectAnimator.ofFloat(ivFingerPrint, "translationX", 40, 0);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(animator1).after(animator0);
        animSet.play(animator2).after(animator1);
        animSet.play(animator3).after(animator2);
        animSet.play(animator4).after(animator3);

        animSet.setDuration(100);
        animSet.start();
    }

    //检测是否有指纹模块
    public static boolean hasFingerMode() {
        if (!mFingerprintManager.isHardwareDetected()) {
            return false;
        }
        return true;
    }

    //检测指纹是否可用
    public static boolean isFingerEnable(Context context) {
        if (!mKeyManager.isKeyguardSecure()) {
            Toast.makeText(context, "请先设置锁屏", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
            context.startActivity(intent);
            return false;
        }
        if (!mFingerprintManager.hasEnrolledFingerprints()) {
            Toast.makeText(context, "请先录入指纹", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
            context.startActivity(intent);
            return false;
        }
        return true;
    }

    //指纹验证结果回调
    FingerprintManager.AuthenticationCallback mAuthCallback = new FingerprintManager.AuthenticationCallback() {
        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            //但多次指纹密码验证错误后，进入此方法；并且，不能短时间内调用指纹验证
            Toast.makeText(Login.this, errString, Toast.LENGTH_SHORT).show();
            //showAuthenticationScreen();
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            Toast.makeText(Login.this, helpString, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            Toast.makeText(Login.this, "指纹识别成功", Toast.LENGTH_SHORT).show();
            if (resetPrivateMarkFromAdvancedSetup) { // 验证成功后：修改密码
                isChangePrivateMark = true;
                textView1.setText(getString(R.string.set_private_mark));
                textView2.setText(getString(R.string.please_set_private_mark));
                textView3.setVisibility(View.GONE);
                frameLayout2.setVisibility(View.VISIBLE);
                editText1.setText(null);
            } else if (!Login.this.isFinishing()) { // 验证成功后：日常登录,且界面没有finish
                startActivity(new Intent(Login.this, Main.class));
                finish();
            }
        }

        @Override
        public void onAuthenticationFailed() {
            Toast.makeText(Login.this, "指纹识别失败", Toast.LENGTH_SHORT).show();
            startFingerWrongAnimation();
        }
    };

    /**
     * 开启指纹监听
     */
    private void startFingerprint() {
        mCancellationSignal = new CancellationSignal();
        mFingerprintManager.authenticate(null, mCancellationSignal, 0, mAuthCallback, null);
    }

    /**
     * 停止指纹监听
     */
    private void stopFingerprint() {
        try {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/*    private void showAuthenticationScreen() {
        Intent intent = mKeyManager.createConfirmDeviceCredentialIntent("finger", "测试指纹识别");
        if (intent != null) {
            startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            // Challenge completed, proceed with using cipher
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "识别成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "识别失败", Toast.LENGTH_SHORT).show();
            }
        }
    }*/


}
