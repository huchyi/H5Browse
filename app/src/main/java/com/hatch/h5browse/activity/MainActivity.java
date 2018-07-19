package com.hatch.h5browse.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hatch.h5browse.R;
import com.hatch.h5browse.adapter.RecyclerViewItemAdapter;
import com.hatch.h5browse.common.Utils;
import com.hatch.h5browse.fragment.BaseWebFragment;
import com.hatch.h5browse.listener.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private List<BaseWebFragment> fragmentList;
    private List<Bitmap> bitmapList;
    private FragmentManager mFragmentManager;
    private BaseWebFragment fragment;
    private int fragmentPosition;

    private RecyclerView mRecyclerView;
    private RecyclerViewItemAdapter itemAdapter;
    private RelativeLayout pagePreviewRl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pagePreviewRl = findViewById(R.id.page_preview);
        ImageView addPageIV = findViewById(R.id.page_preview_add_page);
        TextView backPageTV = findViewById(R.id.page_preview_back);
        addPageIV.setOnClickListener(this);
        backPageTV.setOnClickListener(this);
        init();
    }

    @Override
    protected void onDestroy() {
        Utils.closeAllDB();
        super.onDestroy();
    }

    private void init() {
        fragmentList = new ArrayList<>();
        bitmapList = new ArrayList<>();
        fragmentPosition = 0;
        fragmentList.add(new BaseWebFragment());
        mFragmentManager = getSupportFragmentManager();
        showView(fragmentPosition);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.page_preview_add_page:
                fragmentList.add(new BaseWebFragment());
                showView(fragmentList.size() - 1);
                break;
            case R.id.page_preview_back:
                if (pagePreviewRl.getVisibility() != View.GONE) {
                    pagePreviewRl.setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }

    }



    private void showChoiceItem() {
        if (mRecyclerView != null && itemAdapter != null) {
            if (pagePreviewRl.getVisibility() != View.VISIBLE) {
                pagePreviewRl.setVisibility(View.VISIBLE);
            }
            itemAdapter.notifyDataSetChanged();
            return;
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);

        itemAdapter = new RecyclerViewItemAdapter(bitmapList, this);//添加适配器，这里适配器刚刚装入了数据
        itemAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                showView(position);
            }

            @Override
            public void removeItem(View view, int position) {
                removeView(position);
            }
        });


        mRecyclerView = findViewById(R.id.recycler_view);//获取对象
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(itemAdapter);
        if (pagePreviewRl.getVisibility() != View.VISIBLE) {
            pagePreviewRl.setVisibility(View.VISIBLE);
        }
    }

    private void showView(int position) {
        if (fragmentList.size() == 0) {
            fragmentList.add(new BaseWebFragment());
            if (pagePreviewRl.getVisibility() != View.GONE) {
                pagePreviewRl.setVisibility(View.GONE);
            }
        }
        fragmentPosition = position;
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        fragment = fragmentList.get(position);
        if (fragment.isAdded()) {
            for (int i = 0; i < fragmentList.size(); i++) {
                if (position == i) {
                    transaction.show(fragment);
                } else {
                    transaction.hide(fragmentList.get(i));
                }
            }
            transaction.commit();
        } else {
            transaction.add(R.id.fragment_container, fragment).commit();
        }
        if (pagePreviewRl.getVisibility() != View.GONE) {
            pagePreviewRl.setVisibility(View.GONE);
        }
        dispatchNumMsg();
    }

    private void removeView(int position) {
        if (fragmentPosition == position) { //如果移除了正在显示的view，则指针指向0
            fragmentPosition = 0;
        } else if (position < fragmentPosition) {//如果移除的是小于当前的指针的数据，则指针减一，并且如果小于0，则重新指向0
            fragmentPosition = fragmentPosition - 1;
            if (fragmentPosition < 0) {
                fragmentPosition = 0;
            }
        }
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        BaseWebFragment fragment = fragmentList.get(position);
        transaction.remove(fragment).commit();
        fragmentList.remove(position);
        bitmapList.remove(position);
        itemAdapter.notifyDataSetChanged();
        if (fragmentList.size() == 0) {
            showView(fragmentPosition);
        }
    }

    public void pageShow(Bitmap bitmap) {
        addItem(fragmentPosition, bitmap);
        showChoiceItem();
    }

    public void addItem(int position, Bitmap bitmap) {
        if (position == bitmapList.size()) {
            bitmapList.add(position, bitmap);
        } else {
            bitmapList.set(position, bitmap);
        }
    }

    //分发设置当前的页面个数
    public void dispatchNumMsg() {
        if (fragmentList != null && fragmentList.size() > 0) {
            int size = fragmentList.size();
            for (BaseWebFragment fragment : fragmentList) {
                fragment.setPageNum(size);
            }
        }
    }

    /**
     * 退户
     * */
    public void exit(){
        Utils.backAndFinish(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (pagePreviewRl.getVisibility() == View.VISIBLE) {//展示页面预览
                pagePreviewRl.setVisibility(View.GONE);
            } else if (fragment.canGoBack()) { //页面fragment是否能返回
                fragment.onKeyBack();
            } else {
                exit();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
