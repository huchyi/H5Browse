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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hatch.h5browse.MyApplication;
import com.hatch.h5browse.R;
import com.hatch.h5browse.bean.KeyHistoryBean;
import com.hatch.h5browse.common.KeyBordUtil;
import com.hatch.h5browse.common.Utils;
import com.hatch.h5browse.data.SettingSharedPreferencesUtils;
import com.hatch.h5browse.database.KeyHistoryDao;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;

public class FullScreenDialog extends Dialog {
    private TextView mClipDataTipsTV;
    private TextView mClipDataTV;
    private ImageView mQRCodeIV;
    private ImageView mClearIV;
    private ImageView mSearchIV;
    private EditText mTitleET;
    private ListView listView;
    private TextView mClearHistoryTV;

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
    private MyAdapter KeyHistoryAdapter;
    private boolean isHistoryRecord = false;

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
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                    |WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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
        listView = view.findViewById(R.id.full_dialog_list_view);
        mClearHistoryTV = view.findViewById(R.id.full_dialog_clear_history);


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
        mTitleET.setOnEditorActionListener(onEditorActionListener);
        mClipDataTipsTV.setOnClickListener(mOnClickListener);
        mClipDataTV.setOnClickListener(mOnClickListener);
        mClearHistoryTV.setOnClickListener(mOnClickListener);
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

        //搜索历史
        isHistoryRecord = (boolean) SettingSharedPreferencesUtils.getParam(getContext(), SettingSharedPreferencesUtils.KEY_HISTORY_RECORD, false);
        try {
            if (isHistoryRecord) {
                mClearHistoryTV.setVisibility(View.VISIBLE);
                ArrayList<KeyHistoryBean> beans = new ArrayList<>(KeyHistoryDao.getInstance().findAll());
                if (beans.size() > 0) {
                    KeyHistoryAdapter = new MyAdapter(getContext(), beans);
                    listView.setAdapter(KeyHistoryAdapter);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            if (i==IME_ACTION_SEARCH){
                String content= mTitleET.getText().toString();
                if (TextUtils.isEmpty(content)){
                    return true;
                }
                //进行搜索的逻辑处理
                gotoSearch();
                return true;
            }
            return false;
        }
    };

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
                    gotoSearch();
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
                case R.id.full_dialog_clear_history://清空搜索历史记录
                    try {
                        KeyHistoryDao.getInstance().deleteAll();
                        if (KeyHistoryAdapter != null) {
                            ArrayList<KeyHistoryBean> beans = new ArrayList<>(KeyHistoryDao.getInstance().findAll());
                            KeyHistoryAdapter.notifyData(beans);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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

    private void gotoSearch(){
        String key = mTitleET.getText().toString();
        if (TextUtils.isEmpty(key)) {
            MyApplication.showToast("内容不能为空");
            return;
        }
        String url = Utils.getUrl(key);
        KeyHistoryBean keyHistoryBean = new KeyHistoryBean();
        keyHistoryBean.id = System.currentTimeMillis() + "";
        keyHistoryBean.key = key;
        keyHistoryBean.url = url;
        try {
            if (isHistoryRecord) {
                KeyHistoryDao.getInstance().insert(keyHistoryBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadUrl(url);
    }

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


    public class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<KeyHistoryBean> mDatas;

        //MyAdapter需要一个Context，通过Context获得Layout.inflater，然后通过inflater加载item的布局
        public MyAdapter(Context context, List<KeyHistoryBean> datas) {

            mInflater = LayoutInflater.from(context);
            mDatas = datas;
        }

        public void notifyData(List<KeyHistoryBean> datas) {
            mDatas = datas;
            notifyDataSetChanged();
        }

        //返回数据集的长度
        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        //这个方法才是重点，我们要为它编写一个ViewHolder
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.dialog_full_key_list_item, parent, false); //加载布局
                holder = new ViewHolder();
                holder.titleTv = convertView.findViewById(R.id.dialog_full_key_list_item_content);
                convertView.setTag(holder);
            } else {   //else里面说明，convertView已经被复用了，说明convertView中已经设置过tag了，即holder
                holder = (ViewHolder) convertView.getTag();
            }

            final KeyHistoryBean bean = mDatas.get(position);
            holder.titleTv.setText(bean.key);
            holder.titleTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadUrl(bean.url);
                }
            });

            return convertView;
        }

        //这个ViewHolder只能服务于当前这个特定的adapter，因为ViewHolder里会指定item的控件，不同的ListView，item可能不同，所以ViewHolder写成一个私有的类
        private class ViewHolder {
            TextView titleTv;
        }

    }
}
