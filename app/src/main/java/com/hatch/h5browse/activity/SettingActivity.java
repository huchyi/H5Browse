package com.hatch.h5browse.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Switch;

import com.hatch.h5browse.MyApplication;
import com.hatch.h5browse.R;
import com.just.agentweb.AgentWebConfig;

public class SettingActivity extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        findViewById(R.id.setting_clear_cache).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.setting_clear_cache:
                clearCache();
                break;
            default:
                break;

        }
    }

    public void clearCache() {
        MyApplication.showToast("已清理缓存");
        //清空所有 AgentWeb 硬盘缓存，包括 WebView 的缓存 , AgentWeb 下载的图片 ，视频 ，apk 等文件。
        AgentWebConfig.clearDiskCache(SettingActivity.this);
    }

}
