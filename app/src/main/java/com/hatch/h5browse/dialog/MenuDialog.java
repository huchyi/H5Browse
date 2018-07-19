package com.hatch.h5browse.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.hatch.h5browse.R;
import com.hatch.h5browse.data.OtherSharedPreferencesUtils;
import com.hatch.h5browse.database.CollectionDao;
import com.hatch.h5browse.fragment.BaseWebFragment;

import java.util.ArrayList;
import java.util.HashMap;

public class MenuDialog extends Dialog implements AdapterView.OnItemClickListener {

    private final static int ITEM_ADD_COLLECTION = 0;// 加入书签
    private final static int ITEM_COLLECTION_BOOK = 1;// 收藏夹
    private final static int ITEM_DOWNLOAD_MANAGE = 2;// 下载管理
    private final static int ITEM_COPY_LINK = 3;// 复制链接
    private final static int ITEM_NO_PIC = 4;// 无图模式
    private final static int ITEM_CUT_PIC = 5;// 网页截图
    private final static int ITEM_MORE = 6;// 更多设置
    private final static int ITEM_EXIT = 7;// 退出

    private BaseWebFragment fragment;
    private SimpleAdapter simperAdapter;
    private ArrayList<HashMap<String, Object>> data;


    private int[] menu_image_array = {R.mipmap.collection_icon, R.mipmap.collection_book_icon, R.mipmap.download__managericon,
            R.mipmap.copy_link_icon, R.mipmap.no_picture_icon, R.mipmap.cut_picture_icon, R.mipmap.more_icon, R.mipmap.exit_icon};

    private String[] menu_name_array = {
            "加入书签", "收藏夹", "下载管理", "复制链接",
            "加载图片", "网页截图", "更多设置", "退出"};


    public MenuDialog(Context context, BaseWebFragment fragment) {
        // 在构造方法里, 传入主题
        super(context, R.style.MenuDialogBackgroundNull);
        this.fragment = fragment;
        // 拿到Dialog的Window, 修改Window的属性
        Window window = getWindow();
        if (window != null) {
            window.getDecorView().setPadding(30, 0, 30, 30);
            // 获取Window的LayoutParams
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
            attributes.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            // 一定要重新设置, 才能生效
            window.setAttributes(attributes);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.menu_gridview, null);
        setContentView(view);
        initView();
    }

    @Override
    public void show() {
        super.show();
        boolean picMode = (boolean) OtherSharedPreferencesUtils.getParam(getContext(), OtherSharedPreferencesUtils.PIC_MODE, true);
        if ((picMode && !menu_name_array[4].equals("加载图片"))
                || (!picMode && !menu_name_array[4].equals("无图模式"))) {
            menu_name_array[4] = picMode ? "加载图片" : "无图模式";
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("itemImage", menu_image_array[4]);
            map.put("itemText", menu_name_array[4]);
            data.set(4, map);
            simperAdapter.notifyDataSetChanged();
        }

    }

    private void initView() {
        data = new ArrayList<>();
        for (int i = 0; i < menu_name_array.length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("itemImage", menu_image_array[i]);
            map.put("itemText", menu_name_array[i]);
            data.add(map);
        }
        simperAdapter = new SimpleAdapter(getContext(), data,
                R.layout.menu_item, new String[]{"itemImage", "itemText"},
                new int[]{R.id.item_image, R.id.item_text});
        GridView menuGrid = findViewById(R.id.gridview);
        menuGrid.setAdapter(simperAdapter);
        menuGrid.setOnItemClickListener(this);

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case ITEM_ADD_COLLECTION:// 加入书签
                fragment.insertCollection();
                break;
            case ITEM_COLLECTION_BOOK:// 收藏夹
                fragment.showCollection();
                break;
            case ITEM_DOWNLOAD_MANAGE:// 下载管理
                fragment.showDownload();
                break;
            case ITEM_COPY_LINK:// 复制链接
                fragment.toCopyLink(getContext());
                break;
            case ITEM_NO_PIC:// 无图模式
                boolean picMode = (boolean) OtherSharedPreferencesUtils.getParam(getContext(), OtherSharedPreferencesUtils.PIC_MODE, true);
                fragment.setPicMode(!picMode, true);
                OtherSharedPreferencesUtils.setParam(getContext(), OtherSharedPreferencesUtils.PIC_MODE, !picMode);
                break;
            case ITEM_CUT_PIC:// 网页截图
                fragment.getCutPic();
                break;
            case ITEM_MORE:// 更多设置
                fragment.toMoreSetting();
                break;
            case ITEM_EXIT:// 退出
                fragment.exit();
                break;
        }
        this.dismiss();
    }
}
