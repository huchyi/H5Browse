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

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hatch.h5browse.MyApplication;
import com.hatch.h5browse.R;
import com.hatch.h5browse.activity.CollectionActivity;
import com.hatch.h5browse.bean.CollectionBean;
import com.hatch.h5browse.bean.DownloadBean;
import com.hatch.h5browse.common.FileUtils;
import com.hatch.h5browse.common.Utils;
import com.hatch.h5browse.database.CollectionDao;
import com.hatch.h5browse.database.DownloadDao;
import com.hatch.h5browse.listener.OnItemClickListener;
import com.hatch.h5browse.service.DownloadService;

import java.io.File;
import java.util.ArrayList;

import io.realm.Realm;


public class QueueRecyclerAdapter
        extends RecyclerView.Adapter<QueueRecyclerAdapter.QueueViewHolder> {
    private ArrayList<DownloadBean> downloadBeans;
    private Context context;

    public QueueRecyclerAdapter(Context context, ArrayList<DownloadBean> downloadBeans) {
        this.context = context;
        this.downloadBeans = downloadBeans;
    }

    @Override
    public QueueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new QueueViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_download_item_queue, parent, false));
    }

    @Override
    public void onBindViewHolder(final QueueViewHolder holder, int position) {

        position = holder.getAdapterPosition();
        //item的点击事件
        final int finalPosition = position;
        holder.itemRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] items = {"文件位置", "删除", "取消"};
                new AlertDialog.Builder(context)
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                        intent.setDataAndType(Uri.parse(downloadBeans.get(finalPosition).filePath), "*/*");
//                                        intent.setType("*/*");
                                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                                        try {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                                context.startActivity(intent);
                                            }
                                            MyApplication.showToast("文件路径：" + downloadBeans.get(finalPosition).filePath);

                                        } catch (ActivityNotFoundException ex) {
                                            MyApplication.showToast("亲，木有文件管理器啊-_-!!");
                                        }

                                        break;
                                    case 1:
                                        String filePatch = downloadBeans.get(finalPosition).filePath + "/" + downloadBeans.get(finalPosition).fileName;
                                        FileUtils.deleteFile(filePatch);
                                        try {
                                            DownloadDao.getInstance().delete(downloadBeans.get(finalPosition).url);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        downloadBeans.remove(finalPosition);
                                        notifyDataSetChanged();
                                    case 2:
                                        dialog.dismiss();
                                        break;
                                }
                            }
                        })
                        .create().show();

            }
        });


        //名字
        String name = downloadBeans.get(position).fileName;
        if (!TextUtils.isEmpty(name)) {
            holder.nameTv.setText(name);
        }


        //状态
        final int status = downloadBeans.get(position).status;
        if (status == 2) { //下载成功
            holder.statusIV.setImageResource(R.mipmap.download_success_icon);

            //progress
            holder.progressBar.setProgress(100);

            //下载的长度和总长度
            String numStr = Utils.dataChange(downloadBeans.get(position).totalLength)
                    + "/"
                    + Utils.dataChange(downloadBeans.get(position).totalLength);
            holder.loadNumTv.setText(numStr);

            //速度
            holder.speedTv.setText("下载完成");


        } else if (status == 1) {//正在下载
            holder.statusIV.setImageResource(R.mipmap.download_stop_icon);


            //progress
            holder.progressBar.setProgress(downloadBeans.get(position).progress);

            //下载的长度和总长度
            String numStr = Utils.dataChange(downloadBeans.get(position).currentLength)
                    + "/"
                    + Utils.dataChange(downloadBeans.get(position).totalLength);
            holder.loadNumTv.setText(numStr);

            //速度
            holder.speedTv.setText(String.valueOf(downloadBeans.get(position).speed));
            holder.statusIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DownloadService.class);
                    intent.putExtra("url", downloadBeans.get(finalPosition).url);
                    intent.putExtra("status", 21);
                    context.startService(intent);
                    holder.statusIV.setImageResource(R.mipmap.download__managericon);
                    updateDBStatus(downloadBeans.get(finalPosition).url, 0, finalPosition);
                    notifyDataSetChanged();
                }
            });

        } else {  //暂停下载，可点击下载
            holder.statusIV.setImageResource(R.mipmap.download__managericon);

            //progress
            holder.progressBar.setProgress(downloadBeans.get(position).progress);

            //下载的长度和总长度
            String numStr = Utils.dataChange(downloadBeans.get(position).currentLength)
                    + "/"
                    + Utils.dataChange(downloadBeans.get(position).totalLength);
            holder.loadNumTv.setText(numStr);

            //速度
            holder.speedTv.setText("暂停下载");
            holder.statusIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DownloadService.class);
                    intent.putExtra("url", downloadBeans.get(finalPosition).url);
                    intent.putExtra("status", 11);
                    context.startService(intent);

                    holder.statusIV.setImageResource(R.mipmap.download_stop_icon);
                    updateDBStatus(downloadBeans.get(finalPosition).url, 1, finalPosition);
                    notifyDataSetChanged();
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return downloadBeans.size();
    }


    private void updateDBStatus(final String url, final int status, final int position) {
        //更新数据库
        try {
            DownloadDao.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    DownloadBean dd = realm.where(DownloadBean.class).equalTo("url", url).findFirst();
                    if (dd != null) {
                        dd.status = status;
                        downloadBeans.set(position, dd);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class QueueViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout itemRL;
        ImageView statusIV;
        TextView nameTv;
        ProgressBar progressBar;
        TextView loadNumTv;
        TextView speedTv;

        QueueViewHolder(View itemView) {
            super(itemView);
            itemRL = itemView.findViewById(R.id.download_item_rl);
            statusIV = itemView.findViewById(R.id.download_item_status);
            nameTv = itemView.findViewById(R.id.download_item_name);
            progressBar = itemView.findViewById(R.id.download_item_progressBar);
            loadNumTv = itemView.findViewById(R.id.download_item_num);
            speedTv = itemView.findViewById(R.id.download_item_speed);
        }
    }
}