package com.hatch.h5browse.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;

import com.hatch.h5browse.MyApplication;
import com.hatch.h5browse.R;
import com.hatch.h5browse.data.SettingSharedPreferencesUtils;
import com.just.agentweb.AgentWebConfig;

public class SettingActivity extends Activity implements View.OnClickListener {

    private ImageView adBlockIV;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        findViewById(R.id.setting_clear_cache).setOnClickListener(this);
        initView();
        defaultSetting();
    }

    private void initView() {
        adBlockIV = findViewById(R.id.setting_ad_block_switch);
        adBlockIV.setOnClickListener(this);
    }

    private void defaultSetting() {
        //广告拦截
        boolean isOpen = (boolean) SettingSharedPreferencesUtils.getParam(this, SettingSharedPreferencesUtils.AD_BLOCK_MODE, false);
        adBlockIV.setImageResource(isOpen ? R.mipmap.swicth_open_icon : R.mipmap.swicth_close_icon);
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
            default:
                break;

        }
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        MyApplication.showToast("已清理缓存");
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

}
