package com.hatch.h5browse.common;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.webkit.WebView;

import com.hatch.h5browse.MyApplication;
import com.hatch.h5browse.R;
import com.hatch.h5browse.activity.MainActivity;
import com.hatch.h5browse.database.CollectionDao;
import com.hatch.h5browse.database.DownloadDao;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class Utils {

    /**
     * 获取去网页截图
     */
    public static Bitmap loadBitmapFromViewBySystemHigh(WebView webView) {
        if (webView == null) {
            return null;
        }
        //允许当前窗口保存缓存信息
        webView.setDrawingCacheEnabled(true);
        webView.buildDrawingCache();

        Bitmap b = webView.getDrawingCache();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            b.setConfig(Bitmap.Config.ARGB_8888);
        }
        int width = b.getWidth();
        int height = b.getHeight();

        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(0.4f, 0.4f);

        Bitmap bitmap = Bitmap.createBitmap(b, 0, 0, width, height, matrix, true);

        //销毁缓存信息
        webView.destroyDrawingCache();
        webView.setDrawingCacheEnabled(false);

        return bitmap;
    }

    /**
     * 获取去网页截图，系统方法
     */
    public static Bitmap loadBitmapFromViewBySystem(WebView webView) {
        if (webView == null) {
            return null;
        }
        //允许当前窗口保存缓存信息
        webView.setDrawingCacheEnabled(true);
        webView.buildDrawingCache();

        Bitmap b = webView.getDrawingCache();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            b.setConfig(Bitmap.Config.ARGB_4444);
        }
        int width = b.getWidth();
        int height = b.getHeight();

        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(0.4f, 0.4f);

        Bitmap bitmap = Bitmap.createBitmap(b, 0, 0, width, height, matrix, true);

        //销毁缓存信息
        webView.destroyDrawingCache();
        webView.setDrawingCacheEnabled(false);

        return bitmap;
    }

    /**
     * 保存截图到sd卡
     */
    public static String saveCutPictureToSD(String name, Bitmap iconb) {
        String url = getSDPath();
        if (url == null) {
            return null;
        }
        String filesDir = url + "/H5Browse/screenshot/" + name + ".jpg";
        File file = new File(filesDir);
        byte[] bytes = Bitmap2Bytes(iconb);
        FileIOUtils.writeFileFromBytesByStream(file, bytes);
        return filesDir;
    }

    /**
     * 获取去网页截图到data/data/<pakage>/files目录
     */
    public static String saveUrlIcon(String name, Bitmap iconb) {
        String filesDir = MyApplication.getApplication().getFilesDir().toString() + "/web_icon/" + name + ".jpg";
        File file = new File(filesDir);
        byte[] bytes = Bitmap2Bytes(iconb);
        FileIOUtils.writeFileFromBytesByStream(file, bytes);
        return filesDir;
    }

    /**
     * bitmap转化为byte
     */
    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 得到sd卡路径
     */
    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        if (sdDir == null) {
            return null;
        }
        return sdDir.toString();
    }


    public static File getParentFile(@NonNull Context context) {
        String url = Utils.getSDPath();
        if (url == null) {
            url = context.getCacheDir().getPath();
        }
        return new File(url + "/H5Browse/");
    }

    public static String getParentFileString(@NonNull Context context) {
        String url = Utils.getSDPath();
        if (url == null) {
            url = context.getCacheDir().getPath();
        }
        return url + "/H5Browse/";
    }

    /**
     * 退出确认框
     */
    public static void backAndFinish(final Context context) {
        new AlertDialog.Builder(context)
                .setTitle("确认退出吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“确认”后的操作
                        ((MainActivity) context).finish();

                    }
                })
                .setNegativeButton("返回", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“返回”后的操作,这里不设置没有任何操作
                    }
                }).show();
    }

    /**
     * 获取完整的url的路径
     */
    public static String getUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("about:")) {
            return url;
        }
        if (Patterns.WEB_URL.matcher(url).matches()) {
            return "http://" + url;
        }
        String mSearchUrl = "https://m.baidu.com/s?word=";
        url = mSearchUrl + url;
        return url;
    }

    public static String dataChange(String data) {
        if (!TextUtils.isEmpty(data)) {
            long ll = Long.valueOf(data);
            if (ll < 1024) {
                if (ll < 0) {
                    ll = 0;
                }
                return ll + "B";
            } else if (ll < 1048576) {
                return (int) (ll / 1024) + "Kb";
            } else if (ll < 1073741824) {
                return (int) (ll / 1024 / 1024) + "Mb";
            } else {
                return (int) (ll / 1024 / 1024 / 1024) + "Gb";
            }
        }
        return data;
    }

    //关闭数据库
    public static void closeAllDB() {
        CollectionDao.getInstance().close();
        DownloadDao.getInstance().close();
    }


    public static boolean isAd(Context context, String url) {
        Log.i("hcy","isAd url:" + url);
        Resources res = context.getResources();
        String[] filterUrls = AdFiltersUtils.adIP;
        if (filterUrls == null || filterUrls.length <= 0) {
            filterUrls = res.getStringArray(R.array.adUrls);
        }
        for (String adUrl : filterUrls) {
            if (adUrl.contains("*")) {
                String[] split = adUrl.split("\\*");
                boolean isThis = true;
                for (String str : split) {
                    if (!url.contains(str)) {
                        isThis = false;
                        break;
                    }
                }
                if (isThis) {
                    Log.i("hcy",adUrl + ",isAd * url:" + url);
                    return true;
                }
            } else if (url.contains(adUrl)) {
                Log.i("hcy","isAd  url:" + url);
                return true;
            }
        }
        return false;
    }
}
