package cn.mmvtc.mmvtc;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.mmvtc.mmvtc.utils.HttpUtils;


/**
 * A simple {@link Fragment} subclass.
 */
public class ScoreFragment extends Fragment {
    private ListView listView;
    private SimpleAdapter adapter;
    private List<Map<String, String>> list = new ArrayList<Map<String,String>>();
    private String scoreUrl = "";
    private String cookie = "";
    private String viewstate = "";

    public ScoreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_score, container, false);

        scoreUrl=MainActivity.getScoreUrl();
        cookie=MainActivity.getCookie();
        listView = (ListView) v.findViewById(R.id.listView);


        adapter = new SimpleAdapter(getActivity(), list, R.layout.score_item, new String[]{"nianfen","xueqi","className","score"}, new int[]{R.id.nianfen,R.id.xueqi,R.id.className,R.id.score});
        listView.setAdapter(adapter);

       // HttpUtils.loge("scoreUrl",scoreUrl);
        if (list.isEmpty()){
            new Thread(runnable).start();
        }

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
            getScore();
            handler.sendEmptyMessage(0);
        }
    };
    private void getViewstate() {//


        HttpGet httpGet = new HttpGet(scoreUrl);
        httpGet.setHeader("Cookie", cookie);//设置cookie
        httpGet.setHeader("Referer", scoreUrl);//设置上一个网页的网址
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

    private void getScore() {
        Connection conn = Jsoup.connect(scoreUrl)
                .method(Connection.Method.POST)
                .timeout(6000)
                .header("Cookie", cookie)
                .data("__EVENTTARGET","")
                .data("__VIEWSTATE",viewstate)
                .data("ddlXN", "")
                .data("ddlXQ","")
                .data("ddl_kcxz", "")
                .data("btn_zcj", "历年成绩").referrer(scoreUrl);
        Connection.Response response = null;
        try {
            response = conn.execute();
            String body = response.body();
            getScoreItem(body);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getScoreItem(String html) {//解析html获取数据
        Document content = Jsoup.parse(html);
        Element ScoreList = content.getElementById("Datagrid1");
        Elements tr = ScoreList.getElementsByTag("tr");
        for (int i = 1; i < tr.size(); i++) {
            Elements td = tr.get(i).getElementsByTag("td");
            String nianfen=td.get(0).text();
            String xueqi = td.get(1).text();
            String className = td.get(3).text();
            String score = td.get(8).text();
            Map<String, String> map = new HashMap<String, String>();
            map.put("nianfen",nianfen);
            map.put("xueqi", xueqi);
            map.put("className", className);
            map.put("score", score);
            list.add(map);
        }
        HttpUtils.loge("DATA",list.toString());
    }
}
