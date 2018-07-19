/*
 * Copyright (c) 2017 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hatch.h5browse.download;

import android.app.Service;
import android.content.Context;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.hatch.h5browse.bean.DownloadBean;
import com.hatch.h5browse.common.FileUtils;
import com.hatch.h5browse.common.TimeUtils;
import com.hatch.h5browse.common.Utils;
import com.hatch.h5browse.database.DownloadDao;
import com.liulishuo.okdownload.DownloadContext;
import com.liulishuo.okdownload.DownloadContextListener;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;

public class QueueController {
    private List<DownloadTask> taskList = new ArrayList<>();
    private DownloadContext context;
    private final QueueListener listener = new QueueListener();
    private DownloadContext.Builder builder;
    private File queueDir;


    public QueueController(@NonNull Context context, @NonNull DownloadContextListener listener) {
        initTasks(context, listener);
    }

    private void initTasks(@NonNull Context context, @NonNull DownloadContextListener listener) {
        final DownloadContext.QueueSet set = new DownloadContext.QueueSet();
        this.queueDir = new File(getParentFile(context), "download");

        set.setParentPathFile(queueDir);
        set.setMinIntervalMillisCallbackProcess(200);

        builder = set.commit();
        builder.setListener(listener);
        initListener(context);
    }

    private void initListener(Context context) {
        listener.initService((Service) context, this);
    }

    public void addTask(String url) {
        DownloadTask boundTask = builder.bind(url);
        TagUtil.saveTaskName(boundTask, url);
        boundTask.enqueue(listener);
        OkDownload.with().downloadDispatcher().enqueue(boundTask);
        insertDB(boundTask);
        this.context = builder.build();
        this.taskList = Arrays.asList(this.context.getTasks());

    }

    public void addAllTask(ArrayList<String> list) {
        DownloadTask boundTask;
        for (String url : list) {
            boundTask = builder.bind(url);
            boundTask.enqueue(listener);
            TagUtil.saveTaskName(boundTask, url);
            OkDownload.with().downloadDispatcher().enqueue(boundTask);
            insertDB(boundTask);
        }

        this.context = builder.build();
        this.taskList = Arrays.asList(this.context.getTasks());
    }

    public void startOne(String url) {
        DownloadTask downloadTask = getTask(url);
        if (downloadTask != null) {
            OkDownload.with().downloadDispatcher().enqueue(downloadTask);
            updateDBStatus(downloadTask.getUrl(), 1);
        } else {
            addTask(url);
        }
    }

    public void startAll() {
        if (context != null) {
            this.context.startOnParallel(listener);
            for (DownloadTask downloadTask : taskList) {
                updateDBStatus(downloadTask.getUrl(), 1);
            }
        }
    }

    public void stopOne(String url) {
        DownloadTask downloadTask = getTask(url);
        if (downloadTask != null) {
            OkDownload.with().downloadDispatcher().cancel(downloadTask);
        }
    }

    public void stopAll() {
        if (context != null && context.isStarted()) {
            context.stop();
        }
    }

    public void removeTask(String url) {
        DownloadTask downloadTask = getTask(url);
        removeTask(downloadTask);
    }

    public void removeTask(DownloadTask downloadTask) {
        if (downloadTask != null) {
            downloadTask.cancel();
            builder.unbind(downloadTask);

            this.context = builder.build();
            this.taskList = Arrays.asList(this.context.getTasks());
            deleteFile(downloadTask);
        }
    }

    public void removeAllTask() {
        if (taskList != null && taskList.size() > 0) {
            for (DownloadTask downloadTask : taskList) {
                builder.unbind(downloadTask);
            }

            this.context = builder.build();
            this.taskList = new ArrayList<>();
        }
        deleteFiles();
    }

    private static File getParentFile(@NonNull Context context) {
        String url = Utils.getSDPath();
        if (url == null) {
            return context.getCacheDir();
        }
        return new File(url + "/H5Browse/");
    }


    private void insertDB(DownloadTask downloadTask) {
        DownloadBean downloadBean = new DownloadBean();
        downloadBean.id = String.valueOf(System.currentTimeMillis());
        downloadBean.url = downloadTask.getUrl();
        downloadBean.filePath = queueDir.getPath();
        downloadBean.fileName = downloadTask.getFilename();
        downloadBean.status = 0;
        downloadBean.progress = 0;
        try {
            DownloadBean db = DownloadDao.getInstance().find(downloadTask.getUrl());
            if (db != null && !TextUtils.isEmpty(db.url) && db.url.equals(downloadTask.getUrl())) {
                return;
            }
            DownloadDao.getInstance().insert(downloadBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateDBStatus(final String url, final int status) {
        //更新数据库
        try {
            DownloadDao.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    DownloadBean dd = realm.where(DownloadBean.class).equalTo("url", url).findFirst();
                    if (dd != null) {
                        dd.status = status;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFileName(String url) {
        String filename = FileUtils.getFileName(url);
        if (TextUtils.isEmpty(filename)) {
            filename = TimeUtils.date2String(TimeUtils.string2Date("" + System.currentTimeMillis()));
        }
        return filename;
    }

    private void deleteFiles() {
        if (queueDir != null) {
            String[] children = queueDir.list();
            if (children != null) {
                for (String child : children) {
                    if (!new File(queueDir, child).delete()) {
                        Log.w("hcy", "delete " + child + " failed!");
                    }
                }
            }

            if (!queueDir.delete()) {
                Log.w("hcy", "delete " + queueDir + " failed!");
            }
        }

        for (DownloadTask task : taskList) {
            TagUtil.clearProceedTask(task);
        }
    }

    private void deleteFile(DownloadTask task) {
        if (queueDir != null) {
            String[] children = queueDir.list();
            if (children != null) {
                for (String child : children) {
                    if (!TextUtils.isEmpty(task.getFilename()) && task.getFilename().equals(child) && !new File(queueDir, child).delete()) {
                        break;
                    }
                }
            }
        }
        TagUtil.clearProceedTask(task);
    }


    public List<DownloadTask> getAllTask() {
        return taskList;
    }

    private DownloadTask getTask(String url) {
        for (DownloadTask downloadTask : taskList) {
            String uu = TagUtil.getTaskName(downloadTask);
            if (!TextUtils.isEmpty(uu) && !TextUtils.isEmpty(url) && uu.equals(url)) {
                return downloadTask;
            }
        }
        return null;
    }


    public int size() {
        if (taskList == null) {
            return 0;
        }
        return taskList.size();
    }
}