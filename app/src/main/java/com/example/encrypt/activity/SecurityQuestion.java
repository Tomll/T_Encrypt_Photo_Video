package com.example.encrypt.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.encrypt.R;

import java.util.ArrayList;

/**
 * Created by dongrp on 2016/8/22. 密保问题界面
 */
public class SecurityQuestion extends BaseActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener,EditText.OnEditorActionListener {
    private Spinner spinner;
    private EditText editText;
    private Button button;
    private ArrayList<String> list = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    public static String SelectedPosition = "selected_position"; // 记录选择的密保问题的位置

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_question);
        addAppActivity(SecurityQuestion.this);

        // 初始化spinner所需数据
        list.add(getString(R.string.favorite_sport));
        list.add(getString(R.string.name_of_class_teacher));
        list.add(getString(R.string.favorite_dish));
        list.add(getString(R.string.name_of_pet));
        String user_question = Login.sp.getString("user_question", "");
        if (Login.sp.getBoolean(Login.FirstRun, true)) {
            list.add(getString(R.string.user_question));
        } else if (!TextUtils.isEmpty(user_question)) {
            list.add(user_question);
        }

        // 各组件
        spinner = (Spinner) findViewById(R.id.spinner1);
        editText = (EditText) findViewById(R.id.editText1);
        editText.setOnEditorActionListener(this);
        button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(this);
        // 数组适配器
        adapter = new ArrayAdapter<String>(SecurityQuestion.this, R.layout.item_spinner, list);
        // spinner绑定数组适配器
        spinner.setAdapter(adapter);
        // spinner注册item的选中监听
        spinner.setOnItemSelectedListener(this);
        // 非第一次运行app:展示的密保为上次用户选择的密保问题
        if (!Login.sp.getBoolean(Login.FirstRun, true)) {
            spinner.setSelection(Login.sp.getInt(SelectedPosition, 0));// 取出上次记录的密保问题的位置
        }
    }

    // spinner的item被选中后，回调的两个方法
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (Login.sp.getBoolean(Login.FirstRun, true) && position == list.size() - 1) { // 选择最后一项“自定义问题”
            final EditText editText = new EditText(this);
            editText.setHint(getString(R.string.please_input_user_question));
            final AlertDialog alertDialog = new AlertDialog.Builder(SecurityQuestion.this).setCancelable(false)
                    .setTitle(getString(R.string.user_question)).setPositiveButton(R.string.ok, null)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            spinner.setSelection(0);
                        }
                    }).setView(editText).create();
                  //}).setView(editText, 40, 40, 40, 40).create();
            alertDialog.show();
            // 要实现点击“确定”或“取消”按钮alertDialog不自动消失，需要手动getButton 再setOnclick
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(editText.getText().toString().trim())) {
                        Login.editor.putString("user_question", editText.getText().toString().trim());
                        Login.editor.commit();
                        if (list.size() == 5) {
                            list.remove(list.size() - 1);
                            list.add(editText.getText().toString().trim());
                            list.add(getString(R.string.user_question));
                        } else if (list.size() == 6) {
                            list.remove(list.size() - 1);
                            list.remove(list.size() - 1);
                            list.add(editText.getText().toString().trim());
                            list.add(getString(R.string.user_question));
                        }
                        adapter.notifyDataSetChanged();
                        spinner.setSelection(list.size() - 2);
                        alertDialog.dismiss();
                    } else {
                        Toast.makeText(SecurityQuestion.this, R.string.question_cant_be_empty,Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    // 点击监听回调
    @Override
    public void onClick(View v) {
        //因为界面只有一个button所以不做switch判断
        handleTheAnswer();
    }

    //使用软键盘在EditText中的输入事件回调
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            handleTheAnswer();
            return true;
        }
        return false;
    }

    //处理输入的密保问题答案
    public void handleTheAnswer(){
        if (TextUtils.isEmpty(editText.getText().toString().trim())) {
            Toast.makeText(this, getString(R.string.answer_cant_be_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        // 非第一次运行app
        if (!Login.sp.getBoolean(Login.FirstRun, true)) {
            // 保存的密保答案
            String saveAnswer = Login.sp.getString(spinner.getSelectedItem().toString(), "");
            // 你输入的密保答案
            String yourAnswer = editText.getText().toString().trim();
            if (saveAnswer.equals(yourAnswer)) { // 密保匹配通过
                // 跳转到修改密码的界面
                startActivity(new Intent(SecurityQuestion.this, Login.class).putExtra(
                        Login.ChangePrivateMarkFromSecurityQuestionActivity, true));
                finish();
            } else { // 密保匹配失败
                Toast.makeText(this, getString(R.string.answer_is_wrong), Toast.LENGTH_LONG).show();
            }
        }
        // 第一次运行app
        else if (Login.sp.getBoolean(Login.FirstRun, true)) {
            Login.editor.putString(spinner.getSelectedItem().toString(), editText.getText().toString().trim());// 记录密保问题
            Login.editor.putInt(SelectedPosition, spinner.getSelectedItemPosition());// 记录选择的密保问题的位置
            Login.editor.putBoolean(Login.FirstRun, false);
            Login.editor.commit();
            Toast.makeText(this, getString(R.string.successful_set_private_question), Toast.LENGTH_LONG).show();
            startActivity(new Intent(SecurityQuestion.this, Main.class));
            finish();
        }
    }


}
