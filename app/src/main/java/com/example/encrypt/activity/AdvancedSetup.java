package com.example.encrypt.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.example.encrypt.R;

import static com.example.encrypt.activity.Login.ChangePrivateMarkFromAdvancedSetup;

/**
 * Created by dongrp on 2016/8/15. 高级设置界面
 */
public class AdvancedSetup extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    RelativeLayout rv1;
    Switch mSwitch1, /*mSwitch2, mSwitch3,*/
            mSwitch4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advance_setup);
        addAppActivity(AdvancedSetup.this);
        initView();
    }


    /**
     * 初始化view
     */
    private void initView() {
        rv1 = (RelativeLayout) findViewById(R.id.rv1);
        if (!Login.hasFingerMode()) {
            rv1.setVisibility(View.GONE);
        }
        mSwitch1 = (Switch) findViewById(R.id.switch1);
//        mSwitch2 = (Switch) findViewById(R.id.switch2);
//        mSwitch3 = (Switch) findViewById(R.id.switch3);
        mSwitch4 = (Switch) findViewById(R.id.switch4);
        // 根据xml中保存的开关状态 设置switch状态
        mSwitch1.setChecked(Login.sp.getBoolean("enterByPrivateFingerprint", false));
//        mSwitch2.setChecked(Login.sp.getBoolean("enterByDoubleDecline", false));
//        mSwitch3.setChecked(ImportExportUtils.isVcf(this));
        mSwitch4.setChecked(Login.sp.getBoolean("fastExit", false));
        //checkChange监听
        mSwitch1.setOnCheckedChangeListener(this);
//        mSwitch2.setOnCheckedChangeListener(this);
//        mSwitch3.setOnCheckedChangeListener(this);
        mSwitch4.setOnCheckedChangeListener(this);
    }

    // switch的CheckedChanged监听
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switch1: // 私密指纹 开关
                Login.editor.putBoolean("enterByPrivateFingerprint", isChecked);
                break;
/*            case R.id.switch2: // 双指下滑 开关
                Login.editor.putBoolean("enterByDoubleDecline", isChecked);
                break;
            case R.id.switch3: // 是否保存cvf 开关
                Login.editor.putBoolean(ImportExportUtils.KEY_SHARE_SAVEVCF, isChecked);
                break;*/
            case R.id.switch4: // 快速退出 开关
                Login.editor.putBoolean("fastExit", isChecked);
                break;
        }
        Login.editor.commit();

    }

    // 点击监听回调
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rv1: // 私密指纹开关
                mSwitch1.toggle();
                break;
/*            case R.id.rv2: // 双指下滑开关
                mSwitch2.toggle();
                break;
            case R.id.rv3: // vcf开关
                mSwitch3.toggle();
                break;*/
            case R.id.rv4: // 快速退出开关
                mSwitch4.toggle();
                break;
            case R.id.rv5: // 更改密码
                startActivity(new Intent(AdvancedSetup.this, Login.class).putExtra(ChangePrivateMarkFromAdvancedSetup, true));
                break;
            case R.id.rv6: // 密保问题
                startActivity(new Intent(AdvancedSetup.this, SecurityQuestion.class));
                break;
            case R.id.rv7: // 使用帮助
                startActivity(new Intent(AdvancedSetup.this, UseHelp.class));
                break;
        }
    }

}
