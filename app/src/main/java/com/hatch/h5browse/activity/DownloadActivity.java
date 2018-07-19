package com.hatch.h5browse.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.hatch.h5browse.R;
import com.hatch.h5browse.bean.DownloadBean;
import com.hatch.h5browse.database.DownloadDao;
import com.hatch.h5browse.download.QueueRecyclerAdapter;
import com.hatch.h5browse.event.UpdateDownloadEvent;
import com.hatch.h5browse.listener.OnItemClickListener;
import com.hatch.h5browse.service.DownloadService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class DownloadActivity extends Activity {

    private ArrayList<DownloadBean> downloadBeans;

    private QueueRecyclerAdapter queueRecyclerAdapter;
    private RecyclerView mRecyclerView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        EventBus.getDefault().register(this);
        findViewById(R.id.download_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.download_bottom_all_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DownloadActivity.this, DownloadService.class);
                intent.putExtra("status", 12);
                startService(intent);
            }
        });

        findViewById(R.id.download_bottom_all_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DownloadActivity.this, DownloadService.class);
                intent.putExtra("status", 22);
                startService(intent);
            }
        });

        findViewById(R.id.download_bottom_all_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //更新数据库
                try {
                    DownloadDao.getInstance().deleteAll();
                    downloadBeans.clear();
                    queueRecyclerAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        downloadBeans = new ArrayList<>();
        try {
            List<DownloadBean> list = DownloadDao.getInstance().findAll();
            if (list != null) {
                downloadBeans.addAll(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        queueRecyclerAdapter = new QueueRecyclerAdapter(this, downloadBeans);//添加适配器，这里适配器刚刚装入了数据

        mRecyclerView = findViewById(R.id.download_recycler_view);//获取对象
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(queueRecyclerAdapter);
    }

    @Subscribe()
    public void updateDownloadEvent(UpdateDownloadEvent updateDownloadEvent) {
        if (downloadBeans == null) {
            downloadBeans = new ArrayList<>();
        }
        if (updateDownloadEvent.status == updateDownloadEvent.START) {
            for (int i = 0; i < downloadBeans.size(); i++) {
                DownloadBean downloadBean = downloadBeans.get(i);
                if (downloadBean.url.equals(updateDownloadEvent.task.getUrl())) {
                    DownloadDao.getInstance().getRealm().beginTransaction();
                    downloadBean.fileName = updateDownloadEvent.task.getFilename();
                    downloadBeans.set(i, downloadBean);
                    DownloadDao.getInstance().getRealm().commitTransaction();
                    queueRecyclerAdapter.notifyDataSetChanged();
                    return;
                }
            }

            try {
                DownloadBean downloadBean = DownloadDao.getInstance().find(updateDownloadEvent.task.getUrl());
                downloadBeans.add(downloadBean);
                queueRecyclerAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else if (updateDownloadEvent.status == updateDownloadEvent.END) { //结束
            for (int i = 0; i < downloadBeans.size(); i++) {
                DownloadBean downloadBean = downloadBeans.get(i);
                if (downloadBean.url.equals(updateDownloadEvent.task.getUrl())) {
                    DownloadDao.getInstance().getRealm().beginTransaction();
                    downloadBean.status = 2;
                    downloadBean.currentLength = downloadBean.totalLength;
                    downloadBeans.set(i, downloadBean);
                    DownloadDao.getInstance().getRealm().commitTransaction();
                    queueRecyclerAdapter.notifyDataSetChanged();
                    return;
                }

            }
        } else if (updateDownloadEvent.status == updateDownloadEvent.PROGRESS) { //progress

            for (int i = 0; i < downloadBeans.size(); i++) {
                DownloadBean downloadBean = downloadBeans.get(i);
                if (downloadBean.url.equals(updateDownloadEvent.task.getUrl())) {
                    DownloadDao.getInstance().getRealm().beginTransaction();
                    downloadBean.currentLength = updateDownloadEvent.currentLength + "";
                    downloadBean.totalLength = updateDownloadEvent.totalLength + "";
                    downloadBean.speed = updateDownloadEvent.speed;
                    downloadBean.progress = updateDownloadEvent.processP;
                    downloadBean.status = 1;
                    downloadBeans.set(i, downloadBean);
                    queueRecyclerAdapter.notifyDataSetChanged();
                    DownloadDao.getInstance().getRealm().commitTransaction();
                    return;
                }
            }

        } else if (updateDownloadEvent.status == updateDownloadEvent.STOP) { //暂停

            for (int i = 0; i < downloadBeans.size(); i++) {
                DownloadBean downloadBean = downloadBeans.get(i);
                if (downloadBean.url.equals(updateDownloadEvent.task.getUrl())) {
                    DownloadDao.getInstance().getRealm().beginTransaction();
                    downloadBean.status = 0;
                    downloadBeans.set(i, downloadBean);
                    DownloadDao.getInstance().getRealm().commitTransaction();
                    queueRecyclerAdapter.notifyDataSetChanged();
                    return;
                }

            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

}
