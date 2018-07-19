package com.hatch.h5browse.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hatch.h5browse.R;
import com.hatch.h5browse.listener.OnItemClickListener;

import java.util.List;

public class RecyclerViewItemAdapter extends RecyclerView.Adapter<RecyclerViewItemAdapter.MyViewHolder> {

    private List<Bitmap> bitmapList;//存放数据
    private Context context;
    private OnItemClickListener mClickListener;


    public RecyclerViewItemAdapter(List<Bitmap> bitmapList, Context context) {
        this.bitmapList = bitmapList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.activity_recyclerview_item, parent, false), mClickListener);
    }

    //在这里可以获得每个子项里面的控件的实例，比如这里的TextView,子项本身的实例是itemView，
    // 在这里对获取对象进行操作
    //holder.itemView是子项视图的实例，holder.textView是子项内控件的实例
    //position是点击位置
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        //设置textView显示内容为list里的对应项
        holder.view.setImageBitmap(bitmapList.get(position));
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mClickListener = listener;
    }


    //要显示的子项数量
    @Override
    public int getItemCount() {
        return bitmapList.size();
    }

    //这里定义的是子项的类，不要在这里直接对获取对象进行操作
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private OnItemClickListener mListener;// 声明自定义的接口
        ImageView view;
        ImageView close;

        public MyViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            mListener = listener;
            // 为ItemView添加点击事件
            view = itemView.findViewById(R.id.activity_recyclerview_item_content);
            close = itemView.findViewById(R.id.activity_recyclerview_item_close);
            close.setOnClickListener(this);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // getpostion()为Viewholder自带的一个方法，用来获取RecyclerView当前的位置，将此作为参数，传出去
            switch (v.getId()) {
                case R.id.activity_recyclerview_item_content:
                    if (mListener != null) {
                        mListener.onItemClick(v, getLayoutPosition());
                    }
                    break;
                case R.id.activity_recyclerview_item_close:
                    if (mListener != null) {
                        mListener.removeItem(v, getLayoutPosition());
                    }
                    break;
                default:
                    break;
            }


        }
    }

}