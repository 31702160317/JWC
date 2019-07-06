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

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends Fragment {
    private SimpleAdapter adapter;
    private String infoUrl;
    private String cookie;

    private ListView listView;

    private List<Map<String, String>> list = new ArrayList<Map<String,String>>();

    private String[] listKey = {  "学号", "姓名", "性别",   "出生日期", "身份证号", "民族","来源地区",
            "政治面貌", "学院",   "专业名称", "专业方向", "培养方向",  "行政班",  "学制",
            "学籍状态",   "学历层次",  "入学日期","当前所在级","考生号",  "准考证号", "毕业中学"};

    private String[] listValue = { "xh",  "xm",  "lbl_xb", "lbl_csrq", "lbl_sfzh", "lbl_mz",  "lbl_lydq",
            "lbl_zzmm", "lbl_xy", "lbl_zymc","lbl_zyfx","lbl_pyfx", "lbl_xzb", "lbl_xz",
            "lbl_xjzt", "lbl_CC", "lbl_rxrq","lbl_dqszj","lbl_ksh", "lbl_zkzh", "lbl_byzx"};

    private String[] listKey1 = {"籍贯","出生地",  "宿舍号", "电子邮箱", "联系电话", "邮政编码","家庭所在地"};
    private String[] listValue1 = {"txtjg","csd", "ssh",   "dzyxdz",  "lxdh",    "yzbm","jtszd"};
    public InfoFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        infoUrl = MainActivity.getInfoUrl();
        cookie = MainActivity.getCookie();

        listView = (ListView) view.findViewById(R.id.info_listView);
        adapter = new SimpleAdapter(getActivity(), list, R.layout.me_item, new String[]{"key","value"}, new int[]{R.id.tv_key,R.id.tv_value});
        listView.setAdapter(adapter);
        if (list.isEmpty()){
            new Thread(contentRun).start();
        }

        return view;
    }
    Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            adapter.notifyDataSetChanged();
        };
    };

    Runnable contentRun = new Runnable() {

        @Override
        public void run() {
            if(!list.isEmpty()){
                list.clear();
            }
            Connection conn = Jsoup.connect(infoUrl)
                    .timeout(6000)
                    .method(Connection.Method.GET)
                    .header("Cookie", cookie)
                    .referrer(infoUrl);
            Connection.Response response = null;
            try {
                response = conn.execute();
                String content = response.body();
              // HttpUtils.loge("信息html",content);
                getData(content);
                handler.sendEmptyMessage(0);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private void getData(String html) {//解析html获取数据
            Document dom = Jsoup.parse(html);
            for (int i = 0; i < listKey.length; i++) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("key", listKey[i]);
                map.put("value", dom.getElementById(listValue[i]).text());
                list.add(map);
            }
            for (int i = 0; i < listKey1.length; i++) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("key", listKey1[i]);
                map.put("value", dom.getElementById(listValue1[i]).attr("value"));
                list.add(map);
            }
        }
    };

}
