package cn.mmvtc.mmvtc.CourseFragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.mmvtc.mmvtc.CourseFragment;
import cn.mmvtc.mmvtc.utils.HttpUtils;
import cn.mmvtc.mmvtc.MainActivity;
import cn.mmvtc.mmvtc.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class SaturdayFragment extends Fragment {
    private ListView listView;
    private SimpleAdapter adapter;
    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    private String courseUrl = "";
    private String cookie = "";
    private int column=6;  //表格当前列数
    private SharedPreferences sp;
    private String viewstate = "";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_week, container, false);
        sp = getContext().getSharedPreferences("data", getContext().MODE_PRIVATE);
        courseUrl= MainActivity.getCourseUrl();
        cookie=MainActivity.getCookie();
        HttpUtils.loge("courseUrl",cookie);
        new Thread(runnable).start();
        listView = (ListView) v.findViewById(R.id.list_monday);
        adapter = new SimpleAdapter(getActivity(), list, R.layout.week_item, new String[]{"course"}, new int[]{R.id.tv_course});
        listView.setAdapter(adapter);
        return v;
    }



    Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            adapter.notifyDataSetChanged();
        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            getViewstate();
            getTableData();
            handler.sendEmptyMessage(0);
        }
    };


    public void getTableData() {

        //判断本地是否有存储过学期学年
        String xueqi = sp.getString("xueqi", null);
        String xuenian = sp.getString("xuenian", null);
        String defaultNF=sp.getString("defaultNF",null);
        String defaultXQ=sp.getString("defaultXQ",null);

        Connection conn = Jsoup.connect(courseUrl);
        if ((TextUtils.isEmpty(xueqi) && TextUtils.isEmpty(xuenian))||(xueqi.equals(defaultXQ)&&xuenian.equals(defaultNF))) {
            conn.method(Connection.Method.GET);
            conn.timeout(6000);
            conn.header("Cookie", cookie);
            conn.referrer(courseUrl);
        } else {
            conn.method(Connection.Method.POST);
            conn.timeout(6000);
            conn.header("Cookie", cookie);
            conn.data("__EVENTTARGET","xnd");
            conn.data("__EVENTARGUMENT","");
            conn.data("__VIEWSTATE",viewstate);
            conn.data("xnd",xuenian);
            conn.data("xqd",xueqi);
            conn.referrer(courseUrl);
        }
        Connection.Response response = null;
        try {
            response = conn.execute();
            String content = response.body();
            if(!list.isEmpty()){
                list.clear();
            }

            Document html = Jsoup.parse(content);
            Elements courseTable = html.select("#Table1");

            String []courses=new String[13];
            Elements tr = courseTable.select("tr");
            HttpUtils.loge("tr元素", tr.size() + "");
            //得到第三列数据
            String allData= "";
            for (int i = 2; i < tr.size(); i++) {
                Elements td = tr.get(i).select("td[align=Center]");
                for (int j = 0; j < td.size(); j++) {
                    // 数据值而且值不能为空
                    if ((column-1 == j)&&(!td.get(column-1).text().isEmpty())) allData +=td.get(column-1).text().trim()+"=";
                }
            }
            //将字符串转成数组
            courses=allData.split("=");
            // HttpUtils.loge("allData", courses.length+"");
            if(courses.length<=1){
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("course", "今天放假啦！！");
                list.add(map);
            }else{
                for (int i = 0; i < courses.length; i++) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("course", courses[i]);
                    list.add(map);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void getViewstate() {//
        HttpGet httpGet = new HttpGet(courseUrl);
        httpGet.setHeader("Cookie", cookie);//设置cookie
        httpGet.setHeader("Referer", courseUrl);//设置上一个网页的网址
        HttpClient client = new DefaultHttpClient();
        try {
            HttpResponse httpResponse = client.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String content = EntityUtils.toString(httpResponse.getEntity());
                Document html = Jsoup.parse(content);
                Elements e = html.select("input[name=__VIEWSTATE]");//这里的到密钥
                viewstate = e.get(0).attr("value");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
