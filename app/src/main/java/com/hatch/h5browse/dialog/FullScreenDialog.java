package com.hatch.h5browse.dialog;


import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hatch.h5browse.MyApplication;
import com.hatch.h5browse.R;
import com.hatch.h5browse.common.KeyBordUtil;
import com.hatch.h5browse.common.Utils;

import static android.content.Context.CLIPBOARD_SERVICE;

public class FullScreenDialog extends Dialog {
    private TextView mClipDataTipsTV;
    private TextView mClipDataTV;
    private ImageView mQRCodeIV;
    private ImageView mClearIV;
    private ImageView mSearchIV;
    private EditText mTitleET;

    private TextView mBottomWWW;
    private TextView mBottomM;
    private TextView mBottomHTTP;
    private TextView mBottomHTTPS;
    private TextView mBottomPoint;
    private TextView mBottomSlash;
    private TextView mBottomCN;
    private TextView mBottomCOM;

    private String mUrl = "";
    private OnFullScreenDialogListener OnFullScreenDialogListener;
    private Context mContext;

    public FullScreenDialog(Context context, String url) {
        super(context);
        mUrl = url;
        mContext = context;
    }

    public void setUrlListener(OnFullScreenDialogListener OnFullScreenDialogListener) {
        this.OnFullScreenDialogListener = OnFullScreenDialogListener;
    }

    public interface OnFullScreenDialogListener {
        void onUrlListener(String url);

        void openQRListener();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //<!--关键点1-->
        if (getWindow() != null) {
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_full_screen, null);
        //<!--关键点2-->
        setContentView(view);
        //<!--关键点3-->
        getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        //<!--关键点4-->
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        if (getOwnerActivity() != null) {
            KeyBordUtil.openKeybord(mTitleET, getOwnerActivity());
        }

        mTitleET = view.findViewById(R.id.full_dialog_edit_title);
        mQRCodeIV = view.findViewById(R.id.full_dialog_iv_qr);
        mClearIV = view.findViewById(R.id.full_dialog_iv_clear_text);
        mSearchIV = view.findViewById(R.id.full_dialog_iv_search);
        mClipDataTipsTV = view.findViewById(R.id.full_dialog_clip_data_tips);
        mClipDataTV = view.findViewById(R.id.full_dialog_clip_data);


        mBottomWWW = view.findViewById(R.id.full_dialog_bottom_ll_www);
        mBottomM = view.findViewById(R.id.full_dialog_bottom_ll_m);
        mBottomHTTP = view.findViewById(R.id.full_dialog_bottom_ll_http);
        mBottomHTTPS = view.findViewById(R.id.full_dialog_bottom_ll_https);
        mBottomPoint = view.findViewById(R.id.full_dialog_bottom_ll_point);
        mBottomSlash = view.findViewById(R.id.full_dialog_bottom_ll_slash);
        mBottomCN = view.findViewById(R.id.full_dialog_bottom_ll_cn);
        mBottomCOM = view.findViewById(R.id.full_dialog_bottom_ll_com);

        view.findViewById(R.id.full_dialog_main_ll).setOnClickListener(mOnClickListener);

        mQRCodeIV.setOnClickListener(mOnClickListener);
        mClearIV.setOnClickListener(mOnClickListener);
        mSearchIV.setOnClickListener(mOnClickListener);
        mTitleET.addTextChangedListener(mTextWatcher);
        mClipDataTipsTV.setOnClickListener(mOnClickListener);
        mClipDataTV.setOnClickListener(mOnClickListener);
        mBottomWWW.setOnClickListener(mOnClickListener);
        mBottomM.setOnClickListener(mOnClickListener);
        mBottomHTTP.setOnClickListener(mOnClickListener);
        mBottomHTTPS.setOnClickListener(mOnClickListener);
        mBottomPoint.setOnClickListener(mOnClickListener);
        mBottomSlash.setOnClickListener(mOnClickListener);
        mBottomCN.setOnClickListener(mOnClickListener);
        mBottomCOM.setOnClickListener(mOnClickListener);


        if (!TextUtils.isEmpty(mUrl) && !mUrl.equals("about:blank")) {
            mTitleET.setText(mUrl);
            mTitleET.setSelection(mUrl.length());
        }
        ClipboardManager mClipboardManager = (ClipboardManager) mContext.getSystemService(CLIPBOARD_SERVICE);
        if (mClipboardManager != null) {
            ClipData mClipData = mClipboardManager.getPrimaryClip();
            ClipData.Item item = mClipData.getItemAt(0);
            String clipData = item.getText().toString();
            if (!TextUtils.isEmpty(clipData)) {
                mClipDataTipsTV.setVisibility(View.VISIBLE);
                mClipDataTV.setText(clipData);
                mClipDataTV.setVisibility(View.VISIBLE);
            }
        }

    }


    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.full_dialog_iv_qr:
                    OnFullScreenDialogListener.openQRListener();
                    FullScreenDialog.this.dismiss();
                    break;
                case R.id.full_dialog_iv_clear_text:
                    mTitleET.setText("");
                    if (mClearIV.getVisibility() != View.GONE) {
                        mClearIV.setVisibility(View.GONE);
                    }
                    break;
                case R.id.full_dialog_iv_search:
                    String url = mTitleET.getText().toString();
                    if (TextUtils.isEmpty(url)) {
                        MyApplication.showToast("内容不能为空");
                        return;
                    }
                    loadUrl(Utils.getUrl(url));
                    break;
                case R.id.full_dialog_clip_data_tips:
                case R.id.full_dialog_clip_data:
                    String clipData = mClipDataTV.getText().toString();
                    if (!TextUtils.isEmpty(clipData)) {
                        mTitleET.setText(clipData);
                        mTitleET.setSelection(clipData.length());
                        mClipDataTipsTV.setVisibility(View.GONE);
                        mClipDataTV.setVisibility(View.GONE);
                    }
                    break;
                case R.id.full_dialog_main_ll:
                    FullScreenDialog.this.dismiss();
                    break;
                case R.id.full_dialog_bottom_ll_www:
                    mTitleET.setText(mTitleET.getText().toString() + mBottomWWW.getText().toString());
                    mTitleET.setSelection(mTitleET.getText().toString().length());
                    break;
                case R.id.full_dialog_bottom_ll_m:
                    mTitleET.setText(mTitleET.getText().toString() + mBottomM.getText().toString());
                    mTitleET.setSelection(mTitleET.getText().toString().length());
                    break;
                case R.id.full_dialog_bottom_ll_http:
                    mTitleET.setText(mTitleET.getText().toString() + mBottomHTTP.getText().toString());
                    mTitleET.setSelection(mTitleET.getText().toString().length());
                    break;
                case R.id.full_dialog_bottom_ll_https:
                    mTitleET.setText(mTitleET.getText().toString() + mBottomHTTPS.getText().toString());
                    mTitleET.setSelection(mTitleET.getText().toString().length());
                    break;
                case R.id.full_dialog_bottom_ll_point:
                    mTitleET.setText(mTitleET.getText().toString() + mBottomPoint.getText().toString());
                    mTitleET.setSelection(mTitleET.getText().toString().length());
                    break;
                case R.id.full_dialog_bottom_ll_slash:
                    mTitleET.setText(mTitleET.getText().toString() + mBottomSlash.getText().toString());
                    mTitleET.setSelection(mTitleET.getText().toString().length());
                    break;
                case R.id.full_dialog_bottom_ll_cn:
                    mTitleET.setText(mTitleET.getText().toString() + mBottomCN.getText().toString());
                    mTitleET.setSelection(mTitleET.getText().toString().length());
                    break;
                case R.id.full_dialog_bottom_ll_com:
                    mTitleET.setText(mTitleET.getText().toString() + mBottomCOM.getText().toString());
                    mTitleET.setSelection(mTitleET.getText().toString().length());
                    break;
                default:
                    break;

            }
        }

    };

    private void setBottomQuickText(boolean hasWorld) {

        mBottomWWW.setVisibility(hasWorld ? View.GONE : View.VISIBLE);
        mBottomM.setVisibility(hasWorld ? View.GONE : View.VISIBLE);
        mBottomHTTP.setVisibility(hasWorld ? View.GONE : View.VISIBLE);
        mBottomHTTPS.setVisibility(hasWorld ? View.GONE : View.VISIBLE);

        mBottomPoint.setVisibility(hasWorld ? View.VISIBLE : View.GONE);
        mBottomSlash.setVisibility(hasWorld ? View.VISIBLE : View.GONE);
        mBottomCN.setVisibility(hasWorld ? View.VISIBLE : View.GONE);
        mBottomCOM.setVisibility(hasWorld ? View.VISIBLE : View.GONE);

    }


    private void loadUrl(String url) {
        if (OnFullScreenDialogListener != null && !url.equals(mUrl)) {
            OnFullScreenDialogListener.onUrlListener(url);
        }
        if (getOwnerActivity() != null && KeyBordUtil.isSoftInputShow(getOwnerActivity())) {
            KeyBordUtil.closeKeybord(mTitleET, getOwnerActivity());
        }
        this.dismiss();
    }

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0) {
                if (mClearIV.getVisibility() != View.VISIBLE) {
                    mClearIV.setVisibility(View.VISIBLE);
                }
                setBottomQuickText(true);
            } else {
                setBottomQuickText(false);
                if (mClearIV.getVisibility() != View.GONE) {
                    mClearIV.setVisibility(View.GONE);
                }
            }
        }
    };


}
