package cn.mmvtc.mmvtc;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import cn.mmvtc.mmvtc.Adapter.CourseFragmentPagerAdapter;
import cn.mmvtc.mmvtc.CourseFragments.FirdayFragment;
import cn.mmvtc.mmvtc.CourseFragments.MondayFragment;
import cn.mmvtc.mmvtc.CourseFragments.SaturdayFragment;
import cn.mmvtc.mmvtc.CourseFragments.ThursdayFragment;
import cn.mmvtc.mmvtc.CourseFragments.TuesdayFragment;
import cn.mmvtc.mmvtc.CourseFragments.WednesdayFragment;
import cn.mmvtc.mmvtc.CourseFragments.sundayFragment;
import cn.mmvtc.mmvtc.utils.HttpUtils;
import cn.mmvtc.mmvtc.utils.ToastUtil;

/**
 * 课程主界面
 */
public class CourseFragment extends Fragment implements RadioGroup.OnCheckedChangeListener, ViewPager.OnPageChangeListener {
    private ViewPager pager_course;
    private String courseUrl;
    private String cookie;
    private CourseFragmentPagerAdapter adapter_course;
    private RadioGroup radioGroup;
    private Spinner sp_nf,sp_xq;
    private Button seach_ok;
    private SharedPreferences sharedPreferences;
    private ArrayAdapter<String> adapterNF,adapterNQ;
    private   List<String>  dataXN = new ArrayList<>();
    private  List<String> dataXQ = new ArrayList<>();
    private String defaultNF,defaultXQ;

    private int flagNF,flagNQ=0;//默认年份标记
    private List<Fragment> fragments;
    private int last_xuenian_flag,last_xueqi_flag=-1;//上次不正常销毁标记索引
    private RadioButton rb_monday, rb_tuesday, rb_wednesday, rb_thursday, rb_friday, rb_saturday, rb_sunday;
    private String mWay;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course, container, false);
        courseUrl = MainActivity.getCourseUrl();
        cookie = MainActivity.getCookie();

        //初始化
        sharedPreferences=getContext().getSharedPreferences("data", Context.MODE_PRIVATE);
        sp_nf= (Spinner) view.findViewById(R.id.sp_nf);
        sp_xq= (Spinner) view.findViewById(R.id.sp_xq);
        seach_ok= (Button) view.findViewById(R.id.seach_ok);

        radioGroup = (RadioGroup) view.findViewById(R.id.rg_tab_bar_course);
        rb_monday = (RadioButton) view.findViewById(R.id.rb_monday);
        rb_tuesday = (RadioButton) view.findViewById(R.id.rb_tuesday);
        rb_wednesday = (RadioButton) view.findViewById(R.id.rb_wednesday);
        rb_thursday = (RadioButton) view.findViewById(R.id.rb_thursday);
        rb_friday = (RadioButton) view.findViewById(R.id.rb_friday);
        rb_saturday = (RadioButton) view.findViewById(R.id.rb_saturday);
        rb_sunday = (RadioButton) view.findViewById(R.id.rb_sunday);

        //搜索

        adapterNF = new ArrayAdapter<String>(getActivity(), R.layout.myspinner_item, dataXN);
        adapterNF.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_nf.setAdapter(adapterNF);
        adapterNQ = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, dataXQ);
        adapterNQ.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_xq.setAdapter(adapterNQ);



         fragments = new ArrayList<Fragment>();//设置fragment
        fragments.add(new MondayFragment());
        fragments.add(new TuesdayFragment());
        fragments.add(new WednesdayFragment());
        fragments.add(new ThursdayFragment());
        fragments.add(new FirdayFragment());
        fragments.add(new SaturdayFragment());
        fragments.add(new sundayFragment());
        radioGroup.setOnCheckedChangeListener(this);
        //viewpage
        adapter_course = new CourseFragmentPagerAdapter(getChildFragmentManager(), fragments);

        pager_course = (ViewPager) view.findViewById(R.id.viewpager_course);
        pager_course.setOffscreenPageLimit(1);
        pager_course.setAdapter(adapter_course);

        pager_course.setOnPageChangeListener(this);

//判断现在星期几
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
        switch (mWay){
            case "2":
                rb_monday.setChecked(true);
                pager_course.setCurrentItem(0);
                break;
            case  "3":
                rb_tuesday.setChecked(true);
                pager_course.setCurrentItem(1);
                break;
            case  "4":
                rb_wednesday.setChecked(true);
                pager_course.setCurrentItem(2);
                break;
            case  "5":
                rb_thursday.setChecked(true);
                pager_course.setCurrentItem(3);
                break;
            case  "6":
                rb_friday.setChecked(true);
                pager_course.setCurrentItem(4);
                break;
            case  "7":
                rb_saturday.setChecked(true);
                pager_course.setCurrentItem(5);
                break;
            case  "1":
                rb_sunday.setChecked(true);
                pager_course.setCurrentItem(6);
                break;
        }
        //搜索并且刷新
        seach_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String valueNF= sp_nf.getSelectedItem().toString();
                String valueXQ= sp_xq.getSelectedItem().toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("xuenian", valueNF);
                editor.putString("xueqi", valueXQ);
                editor.commit();
              //判断星期假如当天是星期一又不能靠近星期一因为不刷新就跳到星期五，否则默认跳到星期一
                String mondayCheckRadioId="2131427450";
                String tuesdayCheckRadioId="2131427451";
                String CurrentRadioId=radioGroup.getCheckedRadioButtonId()+"";

                //当天是星期一，或者按钮在星期一 因为星期二靠近星期一不会刷新，则跳到星期五
                if (((mWay=="2")||(mondayCheckRadioId.equals(CurrentRadioId))) || tuesdayCheckRadioId.equals(CurrentRadioId)){
                    rb_friday.setChecked(true);
                    pager_course.setCurrentItem(4);
                }else{
                    rb_monday.setChecked(true);
                    pager_course.setCurrentItem(0);
                }

                //登录成功
               // ToastUtil.showToast(getContext(),"防止错误点击其他星期更精确");

            }
        });

//        加载年份
        new Thread(contentRun).start();
        return view;
    }
    //更新
    Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            adapterNF.notifyDataSetChanged();
            adapterNQ.notifyDataSetChanged();
            if(last_xuenian_flag==-1||last_xueqi_flag==-1){
                sp_nf.setSelection(flagNF,true);
                sp_xq.setSelection(flagNQ,true);
            }else{
                sp_nf.setSelection(last_xuenian_flag,true);
                sp_xq.setSelection(last_xueqi_flag,true);
            }

        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == 2) {
            switch (pager_course.getCurrentItem()) {
                case 0:
                    rb_monday.setChecked(true);
                    break;
                case 1:
                    rb_tuesday.setChecked(true);
                    break;
                case 2:
                    rb_wednesday.setChecked(true);
                    break;
                case 3:
                    rb_thursday.setChecked(true);
                    break;
                case 4:
                    rb_friday.setChecked(true);
                    break;
                case 5:
                    rb_saturday.setChecked(true);
                    break;
                case 6:
                    rb_sunday.setChecked(true);
                    break;
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int arg1) {
        switch (arg1) {
            case R.id.rb_monday:
                pager_course.setCurrentItem(0);
                break;
            case R.id.rb_tuesday:
                pager_course.setCurrentItem(1);
                break;
            case R.id.rb_wednesday:
                pager_course.setCurrentItem(2);
                break;
            case R.id.rb_thursday:
                pager_course.setCurrentItem(3);
                break;
            case R.id.rb_friday:
                pager_course.setCurrentItem(4);
                break;
            case R.id.rb_saturday:
                pager_course.setCurrentItem(5);
                break;
            case R.id.rb_sunday:
                pager_course.setCurrentItem(6);
                break;
        }
    }
    Runnable contentRun = new Runnable() {

        @Override
        public void run() {
            Connection conn = Jsoup.connect(courseUrl)
                    .method(Connection.Method.GET)
                    .header("Cookie", cookie)
                    .referrer(courseUrl);
            Connection.Response response = null;
            try {
                response = conn.execute();
                String content = response.body();

                Document dom = Jsoup.parse(content);
                //得到学年学期
                Elements valueXN=dom.select("#xnd option");
                Elements valueXQ=dom.select("#xqd option");
                //得到默认选中
                defaultNF=dom.select("#xnd option[selected=selected]").text();
                defaultXQ=dom.select("#xqd option[selected=selected]").text();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("defaultNF", defaultNF);
                editor.putString("defaultXQ", defaultXQ);
                editor.commit();
                HttpUtils.loge("defaultNFMAIN",sharedPreferences.getString("defaultNF",""));
                HttpUtils.loge("defaultXQMAIN",sharedPreferences.getString("defaultNQ","")+"");
                //避免重复添加
                if(!dataXN.isEmpty()||!dataXQ.isEmpty()){
                    dataXN.clear();
                    dataXQ.clear();
                }
                //第一次加载或者上次正常退出没有存储学期学年的索引
                for (int i=0;i<valueXN.size();i++){
                    if(valueXN.get(i).hasAttr("selected"))flagNF =i;//判断默认选中的索引
                    if(!valueXN.get(i).text().isEmpty())dataXN.add(valueXN.get(i).text());
                }
                for (int i=0;i<valueXQ.size();i++){
                    if(valueXQ.get(i).hasAttr("selected"))flagNQ =i;//判断默认选中的索引
                    //取集合中的不为空的text 并且没有3学期
                    if(!valueXQ.get(i).text().isEmpty()&&!valueXQ.get(i).text().equals("3"))dataXQ.add(valueXQ.get(i).text());
                }

                //假如上次是不正常销毁，选中默认年份学期
                String xueqi = sharedPreferences.getString("xueqi", null);
                String xuenian = sharedPreferences.getString("xuenian", null);
                if(!TextUtils.isEmpty(xueqi)&&!TextUtils.isEmpty(xuenian)) {
                    for (int i = 0; i < dataXN.size(); i++) {
                        if (dataXN.get(i).equals(xuenian)) last_xuenian_flag = i;
                    }
                    for (int i = 0; i < dataXQ.size(); i++) {

                        if (dataXQ.get(i).equals(xueqi)) last_xueqi_flag = i;

                    }
                }
                handler.sendEmptyMessage(0);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    };

    @Override
    public void onStart() {
        super.onStart();
    }




}
