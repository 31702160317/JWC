package cn.mmvtc.mmvtc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import cn.mmvtc.mmvtc.Adapter.MyFragmentPagerAdapter;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener,ViewPager.OnPageChangeListener{

    private ViewPager pager;
    private RadioGroup radioGroup;
    private RadioButton rb_me, rb_score,rb_course;
    private TextView tv_studentName;
    private MyFragmentPagerAdapter adapter;
    private ImageView img;
    private LoginInfo loginInfo;
    private SharedPreferences sharedPreferences;
    private static String infoUrl = "";//个人信息链接
    private static String scoreUrl = "";//成绩信息
    private static  String courseUrl="";//课表查询
    private  static  String cookie="";  //cookie
    private  static  String stduentName ="";
    private String imgUrl="http://jwc.mmvtc.cn/";
    private boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        Intent intent =getIntent();

//        将登录数据反序列化
        loginInfo = (cn.mmvtc.mmvtc.LoginInfo) intent.getSerializableExtra("info");
        cookie=loginInfo.getCookie();
        stduentName=loginInfo.getStudentName().replace("同学","");
        Log.i("name",loginInfo.getStudentName());
        tv_studentName.setText(loginInfo.getStudentName());

        infoUrl ="http://jwc.mmvtc.cn/xsgrxx.aspx?xh="+loginInfo.getUser()+"&xm="+ URLEncoder.encode(stduentName)+"&gnmkdm=N121501";
        scoreUrl ="http://jwc.mmvtc.cn/xscjcx.aspx?xh="+loginInfo.getUser()+"&xm="+ URLEncoder.encode(stduentName)+"&gnmkdm=N121065";
        //http://jwc.mmvtc.cn/xskbcx.aspx?xh=学号&xm=%CD%F5%BD%F0%B3%C7&gnmkdm=N121603
        courseUrl="http://jwc.mmvtc.cn/xskbcx.aspx?xh="+loginInfo.getUser()+"&xm="+ URLEncoder.encode(stduentName)+"&gnmkdm=N121603";

        Log.i("courseUrl",courseUrl);
        new Thread(getImg).start();//得到头像
    }
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==1){
                byte[] Picture = (byte[]) msg.obj;
                //使用BitmapFactory工厂，把字节数组转化为bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(Picture, 0, Picture.length);
               // Bitmap imgMap= BitmapToRound_Util.toRoundBitmap(bitmap);
                img.setImageBitmap(bitmap);
            }

        }
    };

    private void init() {
        radioGroup = (RadioGroup) findViewById(R.id.rg_tab_bar);
        rb_me = (RadioButton) findViewById(R.id.rb_me);
        rb_score = (RadioButton) findViewById(R.id.rb_score);
        rb_course= (RadioButton) findViewById(R.id.rb_course);
        tv_studentName = (TextView) findViewById(R.id.tv_studentName);
        img= (ImageView) findViewById(R.id.img);
        rb_course.setChecked(true);
        radioGroup.setOnCheckedChangeListener(this);
        List<Fragment> fragments = new ArrayList<Fragment>();//设置fragment
        fragments.add(new CourseFragment());
        fragments.add(new ScoreFragment());
        fragments.add(new InfoFragment());
        adapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), fragments);//初始化adapter
        pager = (ViewPager) findViewById(R.id.viewpager);//设置ViewPager
        pager.setOffscreenPageLimit(3);
        pager.setAdapter(adapter);
        pager.setCurrentItem(0);
        pager.setOnPageChangeListener(this);
    }
//得到登录者的头像
Runnable getImg= new Runnable() {
    @Override
    public void run() {
        getImgMes();
        getImgByte();
    }
};
    //得到头像图片URL连接
    private void getImgMes(){
        Connection conn = Jsoup.connect(infoUrl)
                .method(Connection.Method.GET)
                .header("Cookie", cookie)
                .referrer(infoUrl);
        Connection.Response response = null;
        try {
            response = conn.execute();
            String content = response.body();

            Document dom = Jsoup.parse(content);
            String img = dom.select("#xszp").attr("src");//得到
            imgUrl+=img;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //获得imgurl后下载
private void getImgByte(){
    Connection conn = Jsoup.connect(imgUrl)
            .ignoreContentType(true)
            .timeout(6000)
            .method(Connection.Method.GET)
            .header("Cookie", cookie)
            .referrer(imgUrl);
    Connection.Response response = null;
    try {
        response = conn.execute();
        String content = response.body();

        byte[] byte_image = response.bodyAsBytes();
        Log.i("byte_image", byte_image.toString());
        Message message = handler.obtainMessage();
        message.obj = byte_image;
        message.what = 1;
        handler.sendMessage(message);


    } catch (IOException e) {
        e.printStackTrace();
    }

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
            switch (pager.getCurrentItem()) {
                case 0:
                    rb_course.setChecked(true);
                    break;
                case 1:
                    rb_score.setChecked(true);
                    break;
                case 2:
                    rb_me.setChecked(true);
                    break;
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int arg1) {
        switch (arg1) {
            case R.id.rb_course:
                pager.setCurrentItem(0);
                break;
            case R.id.rb_score:
                pager.setCurrentItem(1);
                break;
            case R.id.rb_me:
                pager.setCurrentItem(2);
                break;
        }
    }




    public static String getInfoUrl() {
        return infoUrl;
    }

    public static String getScoreUrl() {
        return scoreUrl;
    }
    public static String getCourseUrl(){
        return  courseUrl;
    }

    public static String getCookie() {
        return cookie ;
    }
//双击退出软件
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_BACK){
            if(flag==false){
                flag=true;
                Toast.makeText(getApplicationContext(), "再按一次退出软件", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        flag=false;
                    }
                }, 2000);
            }else{
                //清除数据学期年份
                SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.clear();
                editor.commit();
                finish();
                System.exit(0);
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
    }
}
