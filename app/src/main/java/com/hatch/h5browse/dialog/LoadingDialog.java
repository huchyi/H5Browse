package com.hatch.h5browse.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import android.view.Window;
import android.widget.TextView;

import com.hatch.h5browse.R;

import java.lang.ref.WeakReference;
import java.sql.Time;


@SuppressLint("ValidFragment")
public class LoadingDialog extends Dialog {

    private String textStr;
    private int time;

    /**
     * @param textStr 显示文本
     */
    public LoadingDialog(Context context, String textStr) {
        super(context);
        this.textStr = textStr;
    }

    /**
     * @param textStr 显示文本
     * @param time    加载时间，s
     */
    public LoadingDialog(Context context, String textStr, int time) {
        super(context);
        this.textStr = textStr;
        if (time > 0) {
            this.time = time;
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (LoadingDialog.this.isShowing()) {
                LoadingDialog.this.dismiss();
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉默认的title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去掉白色边角
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        setContentView(R.layout.dialog_loading);

        TextView content = findViewById(R.id.dialog_loading_text_content);
        if (TextUtils.isEmpty(textStr)) {
            content.setVisibility(View.GONE);
        } else {
            content.setText(textStr);
            content.setVisibility(View.VISIBLE);
        }

        if (time != 0) {
            new Thread(runnable).start();
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            try {
                Thread.sleep(time * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.sendEmptyMessage(0);

        }
    };

    @Override
    public void dismiss() {
        handler.removeCallbacks(runnable);
        super.dismiss();
    }
}
