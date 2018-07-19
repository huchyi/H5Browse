package com.hatch.h5browse.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.hatch.h5browse.MyApplication;
import com.hatch.h5browse.R;
import com.hatch.h5browse.adapter.CollectionBaseAdapter;
import com.hatch.h5browse.bean.CollectionBean;
import com.hatch.h5browse.database.CollectionDao;

import java.util.ArrayList;
import java.util.List;

public class CollectionActivity extends Activity {
    private ListView listView;
    private CollectionBaseAdapter adapter;
    private List<CollectionBean> dataList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        findViewById(R.id.collection_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.collection_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new android.support.v7.app.AlertDialog.Builder(CollectionActivity.this)
                        .setTitle("确认清空收藏夹吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CollectionDao.getInstance().deleteAll();
                                dataList.clear();
                                adapter.notifyDataSetChanged();
                            }
                        }).setNegativeButton("返回", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();

            }
        });
        listView = findViewById(R.id.list_view);
        dataList = new ArrayList<>();
        try {
            List<CollectionBean> ll = CollectionDao.getInstance().findAll();
            dataList.addAll(ll);
        } catch (Exception e) {
            e.printStackTrace();
        }
        adapter = new CollectionBaseAdapter(this, dataList);
        listView.setAdapter(adapter);
        initData();
    }

    private void initData() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                CollectionBean collectionBean = (CollectionBean) parent.getAdapter().getItem(position);
                bundle.putString("url", collectionBean.url);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {

                String[] items = {"编辑", "删除", "取消"};
                new AlertDialog.Builder(CollectionActivity.this)
                        .setTitle("Hi~")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:

                                        LayoutInflater inflater = CollectionActivity.this.getLayoutInflater();
                                        final View view = inflater.inflate(R.layout.dialog_delete_collection_item, null);
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(CollectionActivity.this);
                                        builder.setView(view).create();
                                        final AlertDialog alertDialog = builder.show();


                                        final EditText titleET = view.findViewById(R.id.dialog_delete_item_title);
                                        final EditText urlET = view.findViewById(R.id.dialog_delete_item_url);
                                        TextView ok = view.findViewById(R.id.dialog_delete_item_ok);
                                        TextView cancel = view.findViewById(R.id.dialog_delete_item_cancel);
                                        final CollectionBean cb0 = (CollectionBean) parent.getAdapter().getItem(position);
                                        titleET.setText(cb0.title);
                                        urlET.setText(cb0.url);
                                        ok.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                String title = titleET.getText().toString();
                                                if (TextUtils.isEmpty(title)) {
                                                    MyApplication.showToast("标题不能为空");
                                                    return;
                                                }
                                                String url = urlET.getText().toString();
                                                if (TextUtils.isEmpty(url)) {
                                                    MyApplication.showToast("链接不能为空");
                                                    return;
                                                }
                                                if (title.equals(cb0.title) && url.equals(cb0.url)) {
                                                    alertDialog.dismiss();
                                                    return;
                                                }

                                                CollectionBean collectionBeanUpdate = new CollectionBean();
                                                collectionBeanUpdate.id = cb0.id;
                                                collectionBeanUpdate.title = title;
                                                collectionBeanUpdate.url = url;
                                                collectionBeanUpdate.iconPath = cb0.iconPath;
                                                try {
                                                    CollectionDao.getInstance().update(collectionBeanUpdate);
                                                    dataList.clear();
                                                    dataList.addAll(CollectionDao.getInstance().findAll());
                                                    adapter.notifyDataSetChanged();
                                                } catch (Exception e) {
                                                    MyApplication.showToast("更新失败");
                                                    e.printStackTrace();
                                                }
                                                alertDialog.dismiss();
                                            }
                                        });
                                        cancel.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                alertDialog.dismiss();
                                            }
                                        });


                                        break;
                                    case 1:
                                        CollectionBean collectionBean = (CollectionBean) parent.getAdapter().getItem(position);
                                        try {
                                            CollectionDao.getInstance().delete(collectionBean.id);
                                            dataList.clear();
                                            dataList.addAll(CollectionDao.getInstance().findAll());
                                            adapter.notifyDataSetChanged();
                                            MyApplication.showToast("删除成功");
                                        } catch (Exception e) {
                                            MyApplication.showToast("删除失败");
                                            e.printStackTrace();
                                        }
                                        break;
                                    case 3:
                                        dialog.dismiss();
                                        break;
                                }
                            }
                        })
                        .create().show();


                return true;
            }
        });
    }

}
