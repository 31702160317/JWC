package cn.mmvtc.mmvtc.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by W on 2019/6/23.
 *
 * httpok
 *
 */

public class HttpUtils {

    public static final String LoginUrl = "http://jwc.mmvtc.cn/default2.aspx";
    public static final String switchVertifyUrl = "http://jwc.mmvtc.cn/CheckCode.aspx";
    public static void GetVertify(okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();//第一步：okhttp对象创建
        Request request = new Request.Builder()
                .url(switchVertifyUrl)                       //第二步：Requset对象创建
                .build();
        client.newCall(request).enqueue(callback);  // client.newCall(request)call对象
    }


    //登录操作
    public static void PostLogin(Map map, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body=new FormBody.Builder()
                .add("__VIEWSTATE",map.get("__VIEWSTATE").toString())
                .add("TextBox2",map.get("TextBox2").toString())
                .add("TextBox1",map.get("TextBox1").toString())
                .add("TextBox3",map.get("TextBox3").toString())
                .add("RadioButtonList1",map.get("RadioButtonList1").toString())
                .add("Button1",map.get("Button1").toString())
                .build();

        Request request = new Request.Builder()
                .url(LoginUrl)

                .post(body)
                .addHeader("Cookie",map.get("Cookie").toString())
                .build();
        client.newCall(request).enqueue(callback);
    }
    //个人信息
    public static void getInfo( String url, String Cookie,okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient().newBuilder().followRedirects(true).followSslRedirects(true).build();//第一步：okhttp对象创建
        Request request = new Request.Builder()
                .url(url)                       //第二步：Requset对象创建
                .addHeader("Cookie",Cookie)
                .addHeader("Referer",url)
                .build();
        client.newCall(request).enqueue(callback);  // client.newCall(request)call对象
    }
    public static void getScoreVIEWSTATE( String url, String Cookie,okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient().newBuilder().followRedirects(true).followSslRedirects(true).build();//第一步：okhttp对象创建
        Request request = new Request.Builder()
                .url(url)                       //第二步：Requset对象创建
                .addHeader("Cookie",Cookie)
                .addHeader("Referer",url)
                .build();
        client.newCall(request).enqueue(callback);  // client.newCall(request)call对象
    }

    public static void postScore( String url, String Cookie,String __VIEWSTATE,okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient()
                .newBuilder().followRedirects(true)
                .followSslRedirects(true).build();//第一步：okhttp对象创建
        RequestBody body=new FormBody.Builder()
                .add("__EVENTARGUMENT","")
                .add("__EVENTTARGET","")
                .add("__VIEWSTATE",__VIEWSTATE)
                .add("ddlXN","")
                .add("ddlXQ","")
                .add("ddl_kcxz","")
                .add("btn_zcj","历年成绩")
                .build();
        Request request = new Request.Builder()//第二步：Requset对象创建
                .url(url)
                .post(body)
                .addHeader("Cookie",Cookie)
                .addHeader("Referer",url)
                .addHeader("User-Agent","Mozilla/5.0 (Windows NT 5.1; rv:47.0) Gecko/20100101 Firefox/47.0")

                .build();
        client.newCall(request).enqueue(callback);  // client.newCall(request)call对象
    }

    /**
     *打印所有
     * @param tag
     * @param msg
     */
    public static   void loge  (String tag, String msg) {
        if (tag == null || tag.length() == 0
                || msg == null || msg.length() == 0)
            return;
        int segmentSize = 3 * 1024;
        long length = msg.length();
        if (length <= segmentSize ) {// 长度小于等于限制直接打印
            Log.e(tag, msg);
        }else {
            while (msg.length() > segmentSize ) {// 循环分段打印日志
                String logContent = msg.substring(0, segmentSize );
                msg = msg.replace(logContent, "");
                Log.e(tag,"-------------------"+ logContent);
            }
            Log.e(tag,"-------------------"+ msg);// 打印剩余日志
        }
    }

    /**
     * //升序https://blog.csdn.net/dly1580854879/article/details/77326954
     * @param oriMap
     * @return
     */
    public  static Map<String, String> sortMapBykeyAsc(Map<String, String> oriMap) {
        Map<String, String> sortedMap = new LinkedHashMap<String, String>();
        try {
            if (oriMap != null && !oriMap.isEmpty()) {
                List<Map.Entry<String, String>> entryList = new ArrayList<Map.Entry<String, String>>(oriMap.entrySet());
                Collections.sort(entryList,
                        new Comparator<Map.Entry<String, String>>() {
                            @Override
                            public int compare(Map.Entry<String, String> entry2,
                                               Map.Entry<String, String> entry1) {
                                int value2 = 0, value1 = 0;
                                try {
                                    value2 = Integer.parseInt(entry1.getKey());
                                    value1 = Integer.parseInt(entry2.getKey());
                                } catch (NumberFormatException e) {
                                    value2 = 0;
                                    value1 = 0;
                                }
                                return value1 - value2;
                            }
                        });
                Iterator<Map.Entry<String, String>> iter = entryList.iterator();
                Map.Entry<String, String> tmpEntry = null;
                while (iter.hasNext()) {
                    tmpEntry = iter.next();
                    sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
                }
            }
        } catch (Exception e) {
        }
        return sortedMap;
    }

}
