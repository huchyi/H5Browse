package com.hatch.h5browse.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hatch.h5browse.R;
import com.hatch.h5browse.bean.CollectionBean;
import com.hatch.h5browse.common.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.List;

public class CollectionBaseAdapter extends BaseAdapter

{
    private List<CollectionBean> dataList;
    private Context context;
    private int resource;

    /**
     * 有参构造
     *
     * @param context  界面
     * @param dataList 数据
     */
    public CollectionBaseAdapter(Context context, List<CollectionBean> dataList) {
        this.context = context;
        this.dataList = dataList;

    }

    @Override
    public int getCount() {

        return dataList.size();
    }

    @Override
    public Object getItem(int index) {

        return dataList.get(index);
    }

    @Override
    public long getItemId(int index) {

        return index;
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            // 给xml布局文件创建java对象
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.activity_collection_list_item, null);
            // 指向布局文件内部组件
            viewHolder.urlTV = view.findViewById(R.id.item_url);
            viewHolder.dateTV = view.findViewById(R.id.item_date);
            viewHolder.titleTV = view.findViewById(R.id.item_title);
            viewHolder.iconIV = view.findViewById(R.id.item_img);
            // 增加额外变量
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        // 获取数据显示在各组件
        CollectionBean collectionBean = dataList.get(index);
        if (!TextUtils.isEmpty(collectionBean.iconPath)) {
            viewHolder.iconIV.setImageBitmap(BitmapFactory.decodeFile(collectionBean.iconPath));
        }
        if (!TextUtils.isEmpty(collectionBean.title)) {
            viewHolder.titleTV.setText(collectionBean.title);
        }
        if (!TextUtils.isEmpty(collectionBean.url)) {
            viewHolder.urlTV.setText(collectionBean.url);
        }
        if (!TextUtils.isEmpty(collectionBean.id)) {
            viewHolder.dateTV.setText(TimeUtils.date2String(TimeUtils.millis2Date(Long.valueOf(collectionBean.id))));
        }
        return view;
    }

    class ViewHolder {
        ImageView iconIV;
        TextView urlTV, dateTV, titleTV;
    }

}


