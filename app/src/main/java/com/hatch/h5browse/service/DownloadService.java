package com.hatch.h5browse.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hatch.h5browse.download.QueueController;
import com.liulishuo.okdownload.DownloadContext;
import com.liulishuo.okdownload.DownloadContextListener;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.cause.EndCause;


import java.util.ArrayList;

public class DownloadService extends Service {

    private QueueController controller;


    private static final int ADD_ONE = 1;
    private static final int ADD_ALL = 2;
    private static final int START_ONE = 11;
    private static final int START_ALL = 12;
    private static final int STOP_ONE = 21;
    private static final int STOP_ALL = 22;
    private static final int REMOVE_ONE = 31;
    private static final int REMOVE_ALL = 32;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        Log.i("hcy", "DownloadService  onCreate");
        super.onCreate();
        init();
    }

    /**
     * 初始化下载控制
     */
    private void init() {
        controller = new QueueController(this, new DownloadContextListener() {
            @Override
            public void taskEnd(@NonNull DownloadContext context, @NonNull DownloadTask task,
                                @NonNull EndCause cause,
                                @android.support.annotation.Nullable Exception realCause,
                                int remainCount) {
            }

            @Override
            public void queueEnd(@NonNull DownloadContext context) {

            }
        });
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("hcy", "DownloadService  onStartCommand");
        if (intent != null) {
            int status = intent.getIntExtra("status", 0);
            statusController(status, intent);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void statusController(int status, Intent intent) {
        switch (status) {
            case ADD_ONE:
                String url = intent.getStringExtra("url");
                controller.addTask(url);
                break;
            case ADD_ALL:
                ArrayList<String> list = intent.getStringArrayListExtra("list_url");
                controller.addAllTask(list);
                break;
            case START_ONE:
                String urlStart = intent.getStringExtra("url");
                controller.startOne(urlStart);
                break;
            case START_ALL:
                controller.startAll();
                break;
            case STOP_ONE: //停止和移除效果是一样的
                String urlStop = intent.getStringExtra("url");
                controller.stopOne(urlStop);
                break;
            case STOP_ALL://停止和移除效果是一样的
                controller.stopAll();
                break;
            case REMOVE_ONE:
                String urlRemove = intent.getStringExtra("url");
                controller.removeTask(urlRemove);
                if (controller.size() <= 0) {
                    this.stopSelf();
                }
                break;
            case REMOVE_ALL:
                controller.removeAllTask();
                this.stopSelf();
                break;
        }

    }

    @Override
    public void onDestroy() {
        Log.i("hcy", "DownloadService  onDestroy");
        super.onDestroy();
        controller.stopAll();
    }
}
