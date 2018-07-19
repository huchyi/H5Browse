package com.hatch.h5browse.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hatch.h5browse.activity.CollectionActivity;
import com.hatch.h5browse.activity.DownloadActivity;
import com.hatch.h5browse.activity.MainActivity;
import com.hatch.h5browse.MyApplication;
import com.hatch.h5browse.R;
import com.hatch.h5browse.activity.SettingActivity;
import com.hatch.h5browse.bean.CollectionBean;
import com.hatch.h5browse.client.MiddlewareChromeClient;
import com.hatch.h5browse.client.MiddlewareWebViewClient;
import com.hatch.h5browse.common.FragmentKeyDown;
import com.hatch.h5browse.common.TimeUtils;
import com.hatch.h5browse.common.UIController;
import com.hatch.h5browse.common.Utils;
import com.hatch.h5browse.data.OtherSharedPreferencesUtils;
import com.hatch.h5browse.data.SettingSharedPreferencesUtils;
import com.hatch.h5browse.database.CollectionDao;
import com.hatch.h5browse.dialog.FullScreenDialog;
import com.hatch.h5browse.dialog.MenuDialog;
import com.hatch.h5browse.event.CacheClearEvent;
import com.hatch.h5browse.service.DownloadService;
import com.just.agentweb.AbsAgentWebSettings;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.AgentWebConfig;
import com.just.agentweb.DefaultWebClient;
import com.just.agentweb.IAgentWebSettings;
import com.just.agentweb.LogUtils;
import com.just.agentweb.MiddlewareWebChromeBase;
import com.just.agentweb.MiddlewareWebClientBase;
import com.just.agentweb.NestedScrollAgentWebView;
import com.just.agentweb.PermissionInterceptor;
import com.just.agentweb.WebListenerManager;
import com.just.agentweb.download.AgentWebDownloader;
import com.just.agentweb.download.DefaultDownloadImpl;
import com.just.agentweb.download.DownloadListenerAdapter;
import com.just.agentweb.download.DownloadingService;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class BaseWebFragment extends Fragment implements FragmentKeyDown {

    private RelativeLayout mainRl;
    private CoordinatorLayout coordinatorLayout;
    private ImageView mQRCodeIV;
    private ImageView mReloadIV;
    private TextView mTitleIV;
    private TextView mLoadingNumIV;
    private LinearLayout mCenterSearch;

    private ImageView mBackIV;
    private ImageView mFinishIV;
    private ImageView mSetIV;
    private ImageView mPageNumIV;
    private TextView mPageNumTV;
    private ImageView mHomeIV;

    protected AgentWeb mAgentWeb;
    private PopupMenu mPopupMenu;

    private String mUrl = "http://shouji.baidu.com/";//"http://m.baidu.com";//"about:blank";//"http://www.hatcher.top/MyBlog/";
    private String mTitleUrl = mUrl;
    private boolean isLoading = false;//是否正在加载中，用于判断底部的按钮显示的图片和转台
    private boolean needClearHistory = false;//回到主页时需要用到
    private MainActivity mainActivity;
    private int pageNum = 1;

    private MenuDialog menuDialog;// menu菜单Dialog

    private FragmentManager mFragmentManager;
    private CustomCaptureFragment captureFragment;

    private List<CollectionBean> collectionBeanList; //收藏夹list

    private boolean isAdBlock;


    /**
     * 用于方便打印测试
     */
    private Gson mGson = new Gson();
    public static final String TAG = "hcy";
    private MiddlewareWebClientBase mMiddleWareWebClient;
    private MiddlewareWebChromeBase mMiddleWareWebChrome;
    private DownloadingService mDownloadingService;
    private AgentWebDownloader.ExtraService mExtraService;

    public static BaseWebFragment getInstance(Bundle bundle) {

        BaseWebFragment baseWebFragment = new BaseWebFragment();
        if (bundle != null) {
            baseWebFragment.setArguments(bundle);
        }
        return baseWebFragment;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCreate();
        EventBus.getDefault().register(this);
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private View mRootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_agentweb, container, false);
            init(mRootView);
        }
        return mRootView;
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initCreate() {
        collectionBeanList = new ArrayList<>();
    }

    private void init(View view) {
        initView(view);
        initData();
        loadUrl(mUrl);

        // AgentWeb 没有把WebView的功能全面覆盖 ，所以某些设置 AgentWeb 没有提供 ， 请从WebView方面入手设置。
        mAgentWeb.getWebCreator().getWebView().setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        AgentWebConfig.debug();//chrome的debug调试
        initSetting();
    }


    protected void initView(View view) {
        mainRl = view.findViewById(R.id.main_layout);
        coordinatorLayout = view.findViewById(R.id.coordinatorLayout);

        //顶部title
        mTitleIV = view.findViewById(R.id.toolbar_edit_title);
        mLoadingNumIV = view.findViewById(R.id.toolbar_title_loading_num);
        mQRCodeIV = view.findViewById(R.id.iv_qr);
        mReloadIV = view.findViewById(R.id.iv_reload);
        mCenterSearch = view.findViewById(R.id.fragment_center_search);

        mQRCodeIV.setOnClickListener(mOnClickListener);
        mReloadIV.setOnClickListener(mOnClickListener);
        mTitleIV.setOnClickListener(mOnClickListener);
        mCenterSearch.setOnClickListener(mOnClickListener);

        //底部菜单
        mBackIV = view.findViewById(R.id.iv_back);
        mFinishIV = view.findViewById(R.id.iv_finish);
        mSetIV = view.findViewById(R.id.iv_setting);
        mPageNumIV = view.findViewById(R.id.iv_page_num);
        mPageNumTV = view.findViewById(R.id.iv_page_num_tv);
        mHomeIV = view.findViewById(R.id.iv_home);

        mBackIV.setOnClickListener(mOnClickListener);
        mFinishIV.setOnClickListener(mOnClickListener);
        mSetIV.setOnClickListener(mOnClickListener);
        mPageNumIV.setOnClickListener(mOnClickListener);
        mHomeIV.setOnClickListener(mOnClickListener);
    }

    private void initData() {
        mPageNumTV.setText(String.valueOf(pageNum));
    }

    private void initSetting() {
        setPicMode((boolean) OtherSharedPreferencesUtils.getParam(getContext(), OtherSharedPreferencesUtils.PIC_MODE, true), false);

        isAdBlock = (boolean) SettingSharedPreferencesUtils.getParam(getContext(), SettingSharedPreferencesUtils.AD_BLOCK_MODE, false);
    }

    private void loadUrl(String url) {
        //页面搜索按钮是否隐藏
        if (TextUtils.isEmpty(url) || !"about:blank".equals(url)) {
            if (mCenterSearch.getVisibility() != View.GONE) {
                mCenterSearch.setVisibility(View.GONE);
            }
        } else {
            if (mCenterSearch.getVisibility() != View.VISIBLE) {
                mCenterSearch.setVisibility(View.VISIBLE);
            }
        }

        //加载url
        if (mAgentWeb != null) {
            mAgentWeb.getUrlLoader().loadUrl(url);
            return;
        }
        NestedScrollAgentWebView webView = new NestedScrollAgentWebView(getActivity());
        CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setBehavior(new AppBarLayout.ScrollingViewBehavior());

        mAgentWeb = AgentWeb.with(this)//
//                .setAgentWebParent((LinearLayout) view, -1, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))//传入AgentWeb的父控件。
                .setAgentWebParent(coordinatorLayout, 1, layoutParams)//lp记得设置behavior属性
                .useDefaultIndicator(-1, 3)//设置进度条颜色与高度，-1为默认值，高度为2，单位为dp。
                .setWebView(webView)
                .setAgentWebWebSettings(getSettings())//设置 IAgentWebSettings。
                .setWebViewClient(mWebViewClient)//WebViewClient ， 与 WebView 使用一致 ，但是请勿获取WebView调用setWebViewClient(xx)方法了,会覆盖AgentWeb DefaultWebClient,同时相应的中间件也会失效。
                .setWebChromeClient(mWebChromeClient) //WebChromeClient
                .setPermissionInterceptor(mPermissionInterceptor) //权限拦截 2.0.0 加入。
                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK) //严格模式 Android 4.2.2 以下会放弃注入对象 ，使用AgentWebView没影响。
                .setAgentWebUIController(new UIController(getActivity())) //自定义UI  AgentWeb3.0.0 加入。
                .setMainFrameErrorView(R.layout.agentweb_error_page, -1) //参数1是错误显示的布局，参数2点击刷新控件ID -1表示点击整个布局都刷新， AgentWeb 3.0.0 加入。
                .useMiddlewareWebChrome(getMiddlewareWebChrome()) //设置WebChromeClient中间件，支持多个WebChromeClient，AgentWeb 3.0.0 加入。
                .useMiddlewareWebClient(getMiddlewareWebClient()) //设置WebViewClient中间件，支持多个WebViewClient， AgentWeb 3.0.0 加入。
//                .setDownloadListener(mDownloadListener) 4.0.0 删除该API//下载回调
//                .openParallelDownload()// 4.0.0删除该API 打开并行下载 , 默认串行下载。 请通过AgentWebDownloader#Extra实现并行下载
//                .setNotifyIcon(R.drawable.ic_file_download_black_24dp) 4.0.0删除该api //下载通知图标。4.0.0后的版本请通过AgentWebDownloader#Extra修改icon
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.DISALLOW)//打开其他页面时，弹窗质询用户前往其他应用 AgentWeb 3.0.0 加入。
                .interceptUnkownUrl() //拦截找不到相关页面的Url AgentWeb 3.0.0 加入。
                .createAgentWeb()//创建AgentWeb。
                .ready()//设置 WebSettings。
                .go(url); //WebView载入该url地址的页面并显示。

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //处理图片二维码结果
        if (requestCode == 2 && resultCode == RESULT_OK) {//收藏夹返回加载url
            if (data != null && data.getExtras() != null) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    String url = bundle.getString("url");
                    if (!TextUtils.isEmpty(url)) {
                        loadUrl(url);
                    }
                }
            }
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.iv_back:
                    // true表示AgentWeb处理了该事件
                    if (!mAgentWeb.back()) {
                        MyApplication.showToast("已经是第一页了");
                    }
                    break;
                case R.id.iv_finish:
                    if (isLoading) {
                        mAgentWeb.getUrlLoader().stopLoading();
                    } else {
                        if (mAgentWeb.getWebCreator().getWebView().canGoForward()) {
                            mAgentWeb.getWebCreator().getWebView().goForward();
                        }
                    }
                    break;
                case R.id.iv_setting:
                    showMenu();
                    break;
                case R.id.iv_page_num:
                    Bitmap bitmap = Utils.loadBitmapFromViewBySystem(mAgentWeb.getWebCreator().getWebView());
                    mainActivity.pageShow(bitmap);
                    break;
                case R.id.iv_home:
                    if (!TextUtils.isEmpty(mUrl) && !TextUtils.isEmpty(mTitleUrl) && !mUrl.equals(mTitleUrl)) {
                        MyApplication.showToast("已经是首页了");
                        return;
                    }
                    needClearHistory = true;
                    loadUrl(mUrl);
                    break;
                case R.id.iv_qr:
                    openQR();
                    break;
                case R.id.iv_reload:
                    mAgentWeb.getUrlLoader().reload();
                    break;
                case R.id.toolbar_edit_title:
                case R.id.fragment_center_search:
                    FullScreenDialog fullScreenDialog = new FullScreenDialog(getActivity(), mTitleUrl);
                    fullScreenDialog.setUrlListener(new FullScreenDialog.OnFullScreenDialogListener() {
                        @Override
                        public void onUrlListener(String url) {
                            loadUrl(url);
                        }

                        @Override
                        public void openQRListener() {
                            openQR();
                        }
                    });
                    fullScreenDialog.show();
                    break;
                default:
                    break;

            }
        }

    };


    protected WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            mLoadingNumIV.setText(String.valueOf(newProgress));
            if (newProgress < 100) {
                if (mLoadingNumIV.getVisibility() != View.VISIBLE) {
                    mLoadingNumIV.setVisibility(View.VISIBLE);
                }
            } else {
                if (mLoadingNumIV.getVisibility() != View.GONE) {
                    mLoadingNumIV.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (mTitleIV != null && !TextUtils.isEmpty(title)) {
                if (title.length() > 10) {
                    title = title.substring(0, 10).concat("...");
                }
            }
            mTitleIV.setText(title);
        }
    };

    protected WebViewClient mWebViewClient = new WebViewClient() {


        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return shouldOverrideUrlLoading(view, request.getUrl() + "");
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            //判断是否是广告相关的资源链接
            if (isAdBlock && getContext() != null && Utils.isAd(getContext(), view.getUrl())) {
                return new WebResourceResponse(null, null, null);
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            super.doUpdateVisitedHistory(view, url, isReload);

            if (needClearHistory) {
                mAgentWeb.clearWebCache();
                needClearHistory = false;
            }
        }

        //
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {

//            Log.i(TAG, "view:" + new Gson().toJson(view.getHitTestResult()));
//            Log.i(TAG, "mWebViewClient shouldOverrideUrlLoading:" + url);
            //intent:// scheme的处理 如果返回false ， 则交给 DefaultWebClient 处理 ， 默认会打开该Activity  ， 如果Activity不存在则跳到应用市场上去.  true 表示拦截
            //例如优酷视频播放 ，intent://play?...package=com.youku.phone;end;
            //优酷想唤起自己应用播放该视频 ， 下面拦截地址返回 true  则会在应用内 H5 播放 ，禁止优酷唤起播放该视频， 如果返回 false ， DefaultWebClient  会根据intent 协议处理 该地址 ， 首先匹配该应用存不存在 ，如果存在 ， 唤起该应用播放 ， 如果不存在 ， 则跳到应用市场下载该应用 .
//            if (url.startsWith("intent://") && url.contains("com.youku.phone")) {
//                return true;
//            }
			/*else if (isAlipay(view, mUrl))   //1.2.5开始不用调用该方法了 ，只要引入支付宝sdk即可 ， DefaultWebClient 默认会处理相应url调起支付宝
			    return true;*/


            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            isLoading = true;
            setBottomImageBtnStatus();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mTitleUrl = url;
            isLoading = false;
            setBottomImageBtnStatus();
            if (mLoadingNumIV.getVisibility() != View.GONE) {
                mLoadingNumIV.setVisibility(View.GONE);
            }
        }
        /*错误页回调该方法 ， 如果重写了该方法， 上面传入了布局将不会显示 ， 交由开发者实现，注意参数对齐。*/
	   /* public void onMainFrameError(AbsAgentWebUIController agentWebUIController, WebView view, int errorCode, String description, String failingUrl) {

            Log.i(TAG, "AgentWebFragment onMainFrameError");
            agentWebUIController.onMainFrameError(view,errorCode,description,failingUrl);

        }*/

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
            isLoading = false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            isLoading = false;
        }
    };


    protected PermissionInterceptor mPermissionInterceptor = new PermissionInterceptor() {

        /**
         * PermissionInterceptor 能达到 url1 允许授权， url2 拒绝授权的效果。
         * @param url
         * @param permissions
         * @param action
         * @return true 该Url对应页面请求权限进行拦截 ，false 表示不拦截。
         */
        @Override
        public boolean intercept(String url, String[] permissions, String action) {
//            Log.i(TAG, "mUrl:" + url + "  permission:" + mGson.toJson(permissions) + " action:" + action);
            return false;
        }
    };


    /**
     * 更新于 AgentWeb  4.0.0
     */
    protected DownloadListenerAdapter mDownloadListenerAdapter = new DownloadListenerAdapter() {

        /**
         *
         * @param url                下载链接
         * @param userAgent          UserAgent
         * @param contentDisposition ContentDisposition
         * @param mimetype           资源的媒体类型
         * @param contentLength      文件长度
         * @param extra              下载配置 ， 用户可以通过 Extra 修改下载icon ， 关闭进度条 ， 是否强制下载。
         * @return true 表示用户处理了该下载事件 ， false 交给 AgentWeb 下载
         */
        @Override
        public boolean onStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength, AgentWebDownloader.Extra extra) {

            //**********************************自己处理下载*********************************

            if (getActivity() != null) {
                Log.i("hcy", "DownloadListenerAdapter  url:" + url);
                Intent intent = new Intent(getActivity(), DownloadService.class);
                intent.putExtra("url", url);
                intent.putExtra("status", 1);
                getActivity().startService(intent);
            }

            return true;
        }

    };

    /**
     * @return IAgentWebSettings
     */
    public IAgentWebSettings getSettings() {
        return new AbsAgentWebSettings() {
            private AgentWeb mAgentWeb;

            @Override
            protected void bindAgentWebSupport(AgentWeb agentWeb) {
                this.mAgentWeb = agentWeb;
            }

            /**
             * AgentWeb 4.0.0 内部删除了 DownloadListener 监听 ，以及相关API ，将 Download 部分完全抽离出来独立一个库，
             * 如果你需要使用 AgentWeb Download 部分 ， 请依赖上 compile 'com.just.agentweb:download:4.0.0 ，
             * 如果你需要监听下载结果，请自定义 AgentWebSetting ， New 出 DefaultDownloadImpl，传入DownloadListenerAdapter
             * 实现进度或者结果监听，例如下面这个例子，如果你不需要监听进度，或者下载结果，下面 setDownloader 的例子可以忽略。
             * @param webView
             * @param downloadListener
             * @return WebListenerManager
             */
            @Override
            public WebListenerManager setDownloader(WebView webView, android.webkit.DownloadListener downloadListener) {
                return super.setDownloader(webView,
                        DefaultDownloadImpl
                                .create((Activity) webView.getContext(),
                                        webView,
                                        mDownloadListenerAdapter,
                                        mDownloadListenerAdapter,
                                        this.mAgentWeb.getPermissionInterceptor()));
            }
        };
    }


    /**
     * 打开浏览器
     *
     * @param targetUrl 外部浏览器打开的地址
     */
    private void openBrowser(String targetUrl) {
        if (TextUtils.isEmpty(targetUrl) || targetUrl.startsWith("file://")) {
            MyApplication.showToast(targetUrl + " 该链接无法使用浏览器打开");
            return;
        }
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri mUri = Uri.parse(targetUrl);
        intent.setData(mUri);
        startActivity(intent);

    }


    private void openQR() {
        if (captureFragment != null) {
            if (!captureFragment.isAdded()) {
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                //把碎片添加到碎片中
                transaction.replace(R.id.qr_frame_layout, captureFragment);
                transaction.commit();
            }
            return;
        }

        //执行扫面Fragment的初始化操作
        captureFragment = new CustomCaptureFragment();
        // 为二维码扫描界面设置定制化界面
        CodeUtils.setFragmentArgs(captureFragment, R.layout.fragment_qr_framelayout);
//        captureFragment.setAnalyzeCallback(analyzeCallback);
        captureFragment.setAnalyzeListener(new CustomCaptureFragment.OnQRAnalyzeCallback() {
            @Override
            public void onSuccess(String result) {
                loadUrl(Utils.getUrl(result));
                closeQR();
            }

            @Override
            public void onFailed() {
                MyApplication.showToast("解析二维码失败");
            }

            @Override
            public void onBack() {
                closeQR();
            }
        });


        //注意这里是调用getChildFragmentManager()方法
        mFragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        //把碎片添加到碎片中
        transaction.replace(R.id.qr_frame_layout, captureFragment);
        transaction.commit();
    }

    private void closeQR() {
        if (captureFragment != null && captureFragment.isAdded()) {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            //把碎片添加到碎片中
            transaction.remove(captureFragment);
            transaction.commit();
        }
    }

    private boolean isQROpen() {
        return captureFragment != null && captureFragment.isAdded() && !captureFragment.isHidden();
    }


    private void setBottomImageBtnStatus() {
        if (isLoading) {
            mFinishIV.setImageResource(R.mipmap.close);
        } else {
            if (mAgentWeb.getWebCreator().getWebView().canGoForward()) {
                mFinishIV.setImageResource(R.mipmap.forward_icon);
            } else {
                mFinishIV.setImageResource(R.mipmap.forward_press_icon);
            }
        }

        if (mAgentWeb.getWebCreator().getWebView().canGoBack()) {
            mBackIV.setImageResource(R.mipmap.back_icon);
        } else {
            mBackIV.setImageResource(R.mipmap.back_press_icon);
        }
    }

    public boolean canGoBack() {
        return isQROpen() || mAgentWeb.getWebCreator().getWebView().canGoBack();
    }

    public void onKeyBack() {
        if (isQROpen()) {
            closeQR();
        } else if (mAgentWeb.getWebCreator().getWebView().canGoBack()) {
            if (!mAgentWeb.back()) {
                MyApplication.showToast("已经是第一页了");
            }
        }

    }

    /**
     * 设置当前的页面个数
     */
    public void setPageNum(int num) {
        if (mPageNumTV != null) {
            mPageNumTV.setText(String.valueOf(num));
        }
        pageNum = num;
    }


    private void showMenu() {
        if (menuDialog != null) {
            if (!menuDialog.isShowing()) {
                menuDialog.show();
            }
            return;
        }
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        menuDialog = new MenuDialog(activity, this);
        menuDialog.show();

    }


    @Subscribe
    public void cacheClear(CacheClearEvent cacheClearEvent) {
        toCleanWebCache();
    }

    /**
     * 清除 WebView 缓存
     */
    private void toCleanWebCache() {

        if (this.mAgentWeb != null) {

            //清理所有跟WebView相关的缓存 ，数据库， 历史记录 等。
            this.mAgentWeb.clearWebCache();
        }

    }

    /**
     * 收藏
     */
    public void insertCollection() {
        String url = mTitleUrl;
        String title = mTitleIV.getText().toString();

        if (!TextUtils.isEmpty(url) || !TextUtils.isEmpty(title)) {

            try {
                CollectionBean bean = CollectionDao.getInstance().find(url);
                if (bean != null && !TextUtils.isEmpty(url) && bean.url.equals(url)) {
                    MyApplication.showToast("收藏夹中已经存在");
                    return;
                }
                CollectionBean collectionBean = new CollectionBean();
                collectionBean.id = System.currentTimeMillis() + "";
                collectionBean.title = title;
                collectionBean.url = url;
                collectionBean.iconPath = Utils.saveUrlIcon(collectionBean.id, mAgentWeb.getWebCreator().getWebView().getFavicon());
                CollectionDao.getInstance().insert(collectionBean);
                MyApplication.showToast("已添加到收藏夹");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 展示收藏夹
     */
    public void showCollection() {
        this.startActivityForResult(new Intent(getActivity(), CollectionActivity.class), 2);
    }

    /**
     * 展示下载页面
     */
    public void showDownload() {
        this.startActivity(new Intent(getActivity(), DownloadActivity.class));
    }

    /**
     * 复制链接到粘贴板
     */
    public void toCopyLink(Context context) {
        if (mAgentWeb != null) {
            String url = mAgentWeb.getWebCreator().getWebView().getUrl();
            ClipboardManager mClipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (mClipboardManager != null) {
                mClipboardManager.setPrimaryClip(ClipData.newPlainText(null, url));
                MyApplication.showToast("已复制链接到粘贴板");
            }
        }
    }

    /**
     * 设置是否显示图片
     */
    public void setPicMode(boolean isLoad, boolean isShowToast) {
        if (isShowToast) {
            MyApplication.showToast(!isLoad ? "无图模式" : "加载图片");
        }
        mAgentWeb.getAgentWebSettings().getWebSettings().setBlockNetworkImage(!isLoad);
    }

    /**
     * 网页截图
     */
    public void getCutPic() {
        Bitmap bitmap = Utils.loadBitmapFromViewBySystemHigh(mAgentWeb.getWebCreator().getWebView());
        if (bitmap != null) {
            String url = Utils.saveCutPictureToSD(TimeUtils.date2String(TimeUtils.millis2Date(System.currentTimeMillis())), bitmap);
            if (url == null) {
                MyApplication.showToast("截图失败");
            } else {
                MyApplication.showToast("截图保存在：" + url);
            }
        }
    }

    /**
     * 更多设置
     */
    public void toMoreSetting() {
        startActivity(new Intent(getActivity(), SettingActivity.class));
    }

    /**
     * 退出
     */
    public void exit() {
        mainActivity.exit();
    }


    @Override
    public void onResume() {
        mAgentWeb.getWebLifeCycle().onResume();//恢复
        super.onResume();
    }

    @Override
    public void onPause() {
        mAgentWeb.getWebLifeCycle().onPause(); //暂停应用内所有WebView ， 调用mWebView.resumeTimers();/mAgentWeb.getWebLifeCycle().onResume(); 恢复。
        super.onPause();
    }

    @Override
    public boolean onFragmentKeyDown(int keyCode, KeyEvent event) {
        return mAgentWeb.handleKeyEvent(keyCode, event);
    }

    @Override
    public void onDestroyView() {
        mAgentWeb.getWebLifeCycle().onDestroy();
        super.onDestroyView();
    }

    /**
     * MiddlewareWebClientBase 是 AgentWeb 3.0.0 提供一个强大的功能，
     * 如果用户需要使用 AgentWeb 提供的功能， 不想重写 WebClientView方
     * 法覆盖AgentWeb提供的功能，那么 MiddlewareWebClientBase 是一个
     * 不错的选择 。
     *
     * @return
     */
    protected MiddlewareWebClientBase getMiddlewareWebClient() {
        return this.mMiddleWareWebClient = new MiddlewareWebViewClient() {
            /**
             *
             * @param view
             * @param url
             * @return
             */
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.startsWith("agentweb")) { // 拦截 url，不执行 DefaultWebClient#shouldOverrideUrlLoading
                    Log.i(TAG, "agentweb scheme ~");
                    return true;
                }

                if (super.shouldOverrideUrlLoading(view, url)) { // 执行 DefaultWebClient#shouldOverrideUrlLoading
                    return true;
                }
                // do you work
                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }
        };
    }

    protected MiddlewareWebChromeBase getMiddlewareWebChrome() {
        return this.mMiddleWareWebChrome = new MiddlewareChromeClient() {
        };
    }
}

