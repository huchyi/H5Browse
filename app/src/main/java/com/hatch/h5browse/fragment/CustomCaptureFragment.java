package com.hatch.h5browse.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hatch.h5browse.R;
import com.uuzuche.lib_zxing.activity.CaptureFragment;
import com.uuzuche.lib_zxing.activity.CodeUtils;

public class CustomCaptureFragment extends CaptureFragment {
    private RelativeLayout relativeLayout;
    private TextView mResultTV;
    private int REQUEST_IMAGE = 2;

    private OnQRAnalyzeCallback onQRAnalyzeCallback;

    private boolean isLighting = false;

    public interface OnQRAnalyzeCallback {
        void onSuccess(String result);

        void onFailed();

        void onBack();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.qr_photo_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });
        final ImageView lightIV = view.findViewById(R.id.qr_light_iv);
        lightIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CodeUtils.isLightEnable(isLighting = !isLighting);
                if(isLighting){
                    lightIV.setImageResource(R.mipmap.qr_light_open_icon);
                }else{
                    lightIV.setImageResource(R.mipmap.qr_light_close_icon);
                }

            }
        });
        view.findViewById(R.id.qr_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onQRAnalyzeCallback != null) {
                    onQRAnalyzeCallback.onBack();
                }
            }
        });


        //中间的框
        relativeLayout = view.findViewById(R.id.qr_result_rl);
        mResultTV = view.findViewById(R.id.qr_result_content);
        view.findViewById(R.id.qr_result_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relativeLayout.setVisibility(View.GONE);
                mResultTV.setText("");
                //重置扫描
                Message obtain = Message.obtain();
                obtain.what=R.id.restart_preview;
                getHandler().sendMessageDelayed(obtain, 500);
            }
        });
        view.findViewById(R.id.qr_result_load).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onQRAnalyzeCallback != null) {
                    onQRAnalyzeCallback.onSuccess(mResultTV.getText().toString());
                }
            }
        });

        setAnalyzeCallback(analyzeCallback);
    }

    /**
     * 二维码解析回调函数
     */
    CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            mResultTV.setText(result);
            if(relativeLayout.getVisibility() != View.VISIBLE){
                relativeLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAnalyzeFailed() {
            if (onQRAnalyzeCallback != null) {
                onQRAnalyzeCallback.onFailed();
            }
        }
    };

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){
            if(relativeLayout.getVisibility() != View.GONE){
                relativeLayout.setVisibility(View.GONE);
            }
            mResultTV.setText("");
        }
    }

    public void setAnalyzeListener(OnQRAnalyzeCallback onQRAnalyzeCallback) {
        this.onQRAnalyzeCallback = onQRAnalyzeCallback;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    CodeUtils.analyzeBitmap(uri.getPath(), new CodeUtils.AnalyzeCallback() {
                        @Override
                        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                            mResultTV.setText(result);
                            if(relativeLayout.getVisibility() != View.VISIBLE){
                                relativeLayout.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onAnalyzeFailed() {
                            if (onQRAnalyzeCallback != null) {
                                onQRAnalyzeCallback.onFailed();
                            }
                        }
                    });
                }
            }
        }
    }
}
