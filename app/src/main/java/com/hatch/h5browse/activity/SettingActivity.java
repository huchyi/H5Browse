package com.hatch.h5browse.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.hatch.h5browse.MyApplication;
import com.hatch.h5browse.R;
import com.hatch.h5browse.data.SettingSharedPreferencesUtils;
import com.hatch.h5browse.dialog.LoadingDialog;
import com.just.agentweb.AgentWebConfig;

public class SettingActivity extends Activity implements View.OnClickListener {

    private ImageView adBlockIV;
    private TextView searchUrlTV;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
        defaultSetting();
    }

    private void initView() {
        findViewById(R.id.setting_title_back).setOnClickListener(this);
        findViewById(R.id.setting_clear_cache).setOnClickListener(this);
        findViewById(R.id.setting_default_search).setOnClickListener(this);

        adBlockIV = findViewById(R.id.setting_ad_block_switch);
        adBlockIV.setOnClickListener(this);
        searchUrlTV = findViewById(R.id.setting_default_search_text);
    }

    private void defaultSetting() {
        //广告拦截
        boolean isOpen = (boolean) SettingSharedPreferencesUtils.getParam(this, SettingSharedPreferencesUtils.AD_BLOCK_MODE, false);
        adBlockIV.setImageResource(isOpen ? R.mipmap.swicth_open_icon : R.mipmap.swicth_close_icon);

        //搜索方式
        String searchUrl = (String) SettingSharedPreferencesUtils.getParam(SettingActivity.this,
                SettingSharedPreferencesUtils.SEARCH_URL,
                "https://m.baidu.com/s?word=");
        if (!TextUtils.isEmpty(searchUrl) && searchUrl.contains("google")) {
            searchUrlTV.setText("Baidu");
        } else {
            searchUrlTV.setText("Google");
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.setting_clear_cache:
                clearCache();
                break;

            case R.id.setting_ad_block_switch:
                adBlockSwitch();
                break;
            case R.id.setting_default_search:
                switchSearchModel();
                break;
            case R.id.setting_title_back:
                finish();
                break;
            default:
                break;

        }
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        LoadingDialog loadingDialog = new LoadingDialog(this, "清理缓存中。。。", 3);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                MyApplication.showToast("清理缓存成功");
            }
        });
        loadingDialog.show();
        //清空所有 AgentWeb 硬盘缓存，包括 WebView 的缓存 , AgentWeb 下载的图片 ，视频 ，apk 等文件。
        AgentWebConfig.clearDiskCache(SettingActivity.this);
    }

    /**
     * 广告拦截开关，true为拦截
     */
    public void adBlockSwitch() {
        boolean isOpen = (boolean) SettingSharedPreferencesUtils.getParam(this, SettingSharedPreferencesUtils.AD_BLOCK_MODE, false);
        adBlockIV.setImageResource(isOpen ? R.mipmap.swicth_close_icon : R.mipmap.swicth_open_icon);
        SettingSharedPreferencesUtils.setParam(this, SettingSharedPreferencesUtils.AD_BLOCK_MODE, !isOpen);
    }

    public void switchSearchModel() {

        String[] items = {"Baidu", "Google", "取消"};
        new AlertDialog.Builder(SettingActivity.this)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            //"https://m.baidu.com/s?word=";//"https://m.google.com/search?q=";
                            case 0:
                                SettingSharedPreferencesUtils.setParam(SettingActivity.this,
                                        SettingSharedPreferencesUtils.SEARCH_URL,
                                        "https://m.baidu.com/s?word=");
                                searchUrlTV.setText("Baidu");
                                break;
                            case 1:
                                SettingSharedPreferencesUtils.setParam(SettingActivity.this,
                                        SettingSharedPreferencesUtils.SEARCH_URL,
                                        "https://www.google.com/search?q=");
                                searchUrlTV.setText("Google");
                                break;
                            case 3:
                                dialog.dismiss();
                                break;
                        }
                    }
                })
                .create().show();

    }

}
