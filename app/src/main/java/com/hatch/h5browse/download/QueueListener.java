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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;


import com.hatch.h5browse.bean.DownloadBean;
import com.hatch.h5browse.database.DownloadDao;
import com.hatch.h5browse.event.UpdateDownloadEvent;
import com.hatch.h5browse.service.DownloadService;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.SpeedCalculator;
import com.liulishuo.okdownload.StatusUtil;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.listener.DownloadListener1;
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed;
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist;
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Map;

import io.realm.Realm;

//public class QueueListener extends DownloadListener1 {
public class QueueListener extends DownloadListener4WithSpeed {
    private long totalLength;
    //    private String readableTotalLength;
    private Service service;
    private QueueController queueController;

    private long time;

    public void initService(Service service, QueueController queueController) {
        this.service = service;
        this.queueController = queueController;
    }

    @Override
    public void taskStart(@NonNull final DownloadTask task) {
    }

    @Override
    public void connectStart(@NonNull DownloadTask task, int blockIndex, @NonNull Map<String, List<String>> requestHeaderFields) {

    }

    @Override
    public void connectEnd(@NonNull DownloadTask task, int blockIndex, int responseCode, @NonNull Map<String, List<String>> responseHeaderFields) {

    }

    @Override
    public void infoReady(@NonNull final DownloadTask task, @NonNull BreakpointInfo info, boolean fromBreakpoint, @NonNull Listener4SpeedAssistExtend.Listener4SpeedModel model) {
        totalLength = info.getTotalLength();
//        readableTotalLength = Util.humanReadableBytes(totalLength, true);
        try {
            Log.i("hcy", "taskStart getFilename：" + task.getFilename());
            DownloadDao.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    DownloadBean dd = realm.where(DownloadBean.class).equalTo("url", task.getUrl()).findFirst();
                    if (dd != null) {
                        dd.fileName = task.getFilename();
                        dd.totalLength = totalLength + "";
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        UpdateDownloadEvent updateDownloadEvent = new UpdateDownloadEvent();
        updateDownloadEvent.status = updateDownloadEvent.START;
        updateDownloadEvent.task = task;
        updateDownloadEvent.totalLength = totalLength;
        EventBus.getDefault().post(updateDownloadEvent);

    }

    @Override
    public void progressBlock(@NonNull DownloadTask task, int blockIndex, long currentBlockOffset, @NonNull SpeedCalculator blockSpeed) {

    }

    @Override
    public void progress(@NonNull DownloadTask task, long currentOffset, @NonNull SpeedCalculator taskSpeed) {
        if (System.currentTimeMillis() - time > 1000) {
            time = System.currentTimeMillis();
            UpdateDownloadEvent updateDownloadEvent = new UpdateDownloadEvent();
            updateDownloadEvent.status = updateDownloadEvent.PROGRESS;
            updateDownloadEvent.task = task;
            updateDownloadEvent.currentLength = currentOffset;
            updateDownloadEvent.totalLength = totalLength;
            updateDownloadEvent.speed = taskSpeed.speed();
            updateDownloadEvent.processP = (int) (100 * (currentOffset / (double) totalLength));
            EventBus.getDefault().post(updateDownloadEvent);
            Log.i("hcy", currentOffset + "/" + totalLength + "(" + taskSpeed.speed() + ")" + ",processP:" + (currentOffset / (double) totalLength));
        }
    }

    @Override
    public void blockEnd(@NonNull DownloadTask task, int blockIndex, BlockInfo info, @NonNull SpeedCalculator blockSpeed) {

    }

    @Override
    public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Exception realCause, @NonNull SpeedCalculator taskSpeed) {
        Log.i("hcy", "taskEnd");
        if (cause == EndCause.COMPLETED) {

            UpdateDownloadEvent updateDownloadEvent = new UpdateDownloadEvent();
            updateDownloadEvent.status = updateDownloadEvent.END;
            updateDownloadEvent.task = task;
            EventBus.getDefault().post(updateDownloadEvent);

            queueController.updateDBStatus(task.getUrl(), 2);
            //控制器移除
            queueController.removeTask(task);
        } else {
            UpdateDownloadEvent updateDownloadEvent = new UpdateDownloadEvent();
            updateDownloadEvent.status = updateDownloadEvent.STOP;
            updateDownloadEvent.task = task;
            EventBus.getDefault().post(updateDownloadEvent);
        }

        if (queueController.getAllTask().size() <= 0) {
            service.stopSelf();
        }
    }


//    @Override
//    public void taskStart(@NonNull DownloadTask task, @NonNull Listener1Assist.Listener1Model model) {
//        Log.i("hcy", "taskStart");
//
//    }
//
//    @Override
//    public void retry(@NonNull DownloadTask task, @NonNull ResumeFailedCause cause) {
//        Log.i("hcy", "retry");
//    }
//
//    @Override
//    public void connected(@NonNull DownloadTask task, int blockCount, long currentOffset, long totalLength) {
//        Log.i("hcy", "connected");
//    }
//
//    @Override
//    public void progress(@NonNull DownloadTask task, long currentOffset, long totalLength) {
//        Log.i("hcy", totalLength + "-------currentOffset:" + currentOffset);
//
//    }
//
//    @Override
//    public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Exception realCause, @NonNull Listener1Assist.Listener1Model model) {
//        Log.i("hcy", "taskEnd");
//        if (cause == EndCause.COMPLETED) {
//            queueController.removeTask(task);
//        }
//        if (queueController.getAllTask().size() <= 0) {
//            service.stopSelf();
//        }
//    }


//    private static final String TAG = "QueueListener";
//
//    private SparseArray<QueueRecyclerAdapter.QueueViewHolder> holderMap = new SparseArray<>();
//
//    void bind(DownloadTask task, QueueRecyclerAdapter.QueueViewHolder holder) {
//        Log.i(TAG, "bind " + task.getId() + " with " + holder);
//        // replace.
//        final int size = holderMap.size();
//        for (int i = 0; i < size; i++) {
//            if (holderMap.valueAt(i) == holder) {
//                holderMap.removeAt(i);
//                break;
//            }
//        }
//        holderMap.put(task.getId(), holder);
//    }
//
//    void resetInfo(DownloadTask task, QueueRecyclerAdapter.QueueViewHolder holder) {
//        // task name
//        final String taskName = TagUtil.getTaskName(task);
//        holder.nameTv.setText(taskName);
//
//        // process references
//        final String status = TagUtil.getStatus(task);
//        if (status != null) {
//            //   started
//            holder.speedTv.setText(status);
//            if (status.equals(EndCause.COMPLETED.toString())) {
//                holder.progressBar.setProgress(holder.progressBar.getMax());
//            } else {
//                final long total = TagUtil.getTotal(task);
//                if (total == 0) {
//                    holder.progressBar.setProgress(0);
//                } else {
//                    ProgressUtil.calcProgressToViewAndMark(holder.progressBar,
//                            TagUtil.getOffset(task), total, false);
//                }
//            }
//        } else {
//            // non-started
//            final StatusUtil.Status statusOnStore = StatusUtil.getStatus(task);
//            TagUtil.saveStatus(task, statusOnStore.toString());
//            if (statusOnStore == StatusUtil.Status.COMPLETED) {
//                holder.speedTv.setText(EndCause.COMPLETED.toString());
//                holder.progressBar.setProgress(holder.progressBar.getMax());
//            } else {
//                switch (statusOnStore) {
//                    case IDLE:
//                        holder.speedTv.setText("state_idle");
//                        break;
//                    case PENDING:
//                        holder.speedTv.setText("state_pending");
//                        break;
//                    case RUNNING:
//                        holder.speedTv.setText("state_running");
//                        break;
//                    default:
//                        holder.speedTv.setText("state_unknown");
//                }
//
//                if (statusOnStore == StatusUtil.Status.UNKNOWN) {
//                    holder.progressBar.setProgress(0);
//                } else {
//                    final BreakpointInfo info = StatusUtil.getCurrentInfo(task);
//                    if (info != null) {
//                        TagUtil.saveTotal(task, info.getTotalLength());
//                        TagUtil.saveOffset(task, info.getTotalOffset());
//                        ProgressUtil.calcProgressToViewAndMark(holder.progressBar,
//                                info.getTotalOffset(), info.getTotalLength(), false);
//                    } else {
//                        holder.progressBar.setProgress(0);
//                    }
//                }
//
//            }
//        }
//    }
//
//    public void clearBoundHolder() {
//        holderMap.clear();
//    }
//
//    @Override
//    public void taskStart(@NonNull DownloadTask task,
//                          @NonNull Listener1Assist.Listener1Model model) {
//        final String status = "taskStart";
//        TagUtil.saveStatus(task, status);
//
//        final QueueRecyclerAdapter.QueueViewHolder holder = holderMap.get(task.getId());
//
//        if (holder == null) return;
//
//        holder.speedTv.setText(status);
//    }
//
//    @Override
//    public void retry(@NonNull DownloadTask task, @NonNull ResumeFailedCause cause) {
//        final String status = "retry";
//        TagUtil.saveStatus(task, status);
//
//        final QueueRecyclerAdapter.QueueViewHolder holder = holderMap.get(task.getId());
//        if (holder == null) return;
//
//        holder.speedTv.setText(status);
//    }
//
//    @Override
//    public void connected(@NonNull DownloadTask task, int blockCount, long currentOffset,
//                          long totalLength) {
//        final String status = "connected";
//        TagUtil.saveStatus(task, status);
//        TagUtil.saveOffset(task, currentOffset);
//        TagUtil.saveTotal(task, totalLength);
//
//        final QueueRecyclerAdapter.QueueViewHolder holder = holderMap.get(task.getId());
//        if (holder == null) return;
//
//        holder.speedTv.setText(status);
//
//        ProgressUtil.calcProgressToViewAndMark(holder.progressBar, currentOffset, totalLength,
//                false);
//    }
//
//    @Override
//    public void progress(@NonNull DownloadTask task, long currentOffset, long totalLength) {
//        final String status = "progress";
//        TagUtil.saveStatus(task, status);
//        TagUtil.saveOffset(task, currentOffset);
//
//        final QueueRecyclerAdapter.QueueViewHolder holder = holderMap.get(task.getId());
//        if (holder == null) return;
//
//        holder.speedTv.setText(status);
//
//        Log.i(TAG, "progress " + task.getId() + " with " + holder);
//        ProgressUtil.updateProgressToViewWithMark(holder.progressBar, currentOffset, false);
//    }
//
//    @Override
//    public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause,
//                        @Nullable Exception realCause,
//                        @NonNull Listener1Assist.Listener1Model model) {
//        final String status = cause.toString();
//        TagUtil.saveStatus(task, status);
//
//        final QueueRecyclerAdapter.QueueViewHolder holder = holderMap.get(task.getId());
//        if (holder == null) return;
//
//        holder.speedTv.setText(status);
//        if (cause == EndCause.COMPLETED) {
//            holder.progressBar.setProgress(holder.progressBar.getMax());
//        }
//    }
}