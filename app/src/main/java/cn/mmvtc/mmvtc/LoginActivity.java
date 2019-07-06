package cn.mmvtc.mmvtc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.style.TtsSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;

import cn.mmvtc.mmvtc.utils.HttpUtils;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText et_user, et_password, et_vertify;
    private CheckBox isSave;
    private ImageView iv_vertify;
    private Button btn_login;
    private Button tv_clear;
    private SharedPreferences sharedPreferences;
    private LoginInfo loginInfo = new LoginInfo();
    private String switchVertifyUrl="http://jwc.mmvtc.cn/CheckCode.aspx";
    private String LoginUrl="http://jwc.mmvtc.cn/default2.aspx";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();//初始化控件
        last_read();//上次是不是保存密码
        new Thread(vertifyRun).start();// 获取验证码并得到cookie
    }

    // handler更新
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    byte[] Picture = (byte[]) msg.obj;
                    //使用BitmapFactory工厂，把字节数组转化为bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(Picture, 0, Picture.length);
                    iv_vertify.setImageBitmap(bitmap);
                    break;
                case 2:// 验证码错误
                    loginFail("验证码错误");
                    break;
                case 3:// 密码错误
                    loginFail("密码错误");
                    break;
                case 4:// 用户不存
                    loginFail("用户名不存在或未按照要求参加教学活动");
                    break;
                case 5:

                    //登录成功
                    Toast.makeText(LoginActivity.this, "登陆成功", Toast.LENGTH_LONG)
                            .show();
                    Intent intent = new Intent(LoginActivity.this,
                            MainActivity.class);
                    intent.putExtra("info", loginInfo);
                    startActivity(intent);
                    finish();
                    break;


            }
        }
    };

    private void loginFail(String tip) {
        Toast.makeText(LoginActivity.this, tip, Toast.LENGTH_LONG).show();
        et_vertify.setText("");
        new Thread(vertifyRun).start();
    }




    //初始化控件
    private void initView() {
        et_user = (EditText) findViewById(R.id.et_user);
        et_password = (EditText) findViewById(R.id.et_password);
        et_vertify = (EditText) findViewById(R.id.et_vertify);
        isSave = (CheckBox) findViewById(R.id.cb_isSave);
        iv_vertify = (ImageView) findViewById(R.id.iv_vertify);
        btn_login = (Button) findViewById(R.id.btn_login);
        tv_clear= (Button) findViewById(R.id.clear_log);
        tv_clear.setOnClickListener(this);
        iv_vertify.setOnClickListener(this);
        btn_login.setOnClickListener(this);
    }

    /**
     * 上次登录是否有记住密码
     */
    private void last_read() {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String stuUser = sp.getString("user", null);
        String stuPassword = sp.getString("password", null);
        if (!(TextUtils.isEmpty(stuUser) && TextUtils.isEmpty(stuPassword))) {
            et_user.setText(stuUser);
            et_password.setText(stuPassword);
            isSave.setChecked(true);
        }
    }

    //保存密码
    private void saveUser(String user, String password) {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("user", user);
        editor.putString("password", password);
        editor.commit();
    }
    //清除记录
    private void clear_log() {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
        et_user.setText("");
        et_password.setText("");
        Toast.makeText(this, "清除成功", Toast.LENGTH_SHORT).show();
    }
    //按钮点击操作
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_vertify:
                new Thread(vertifyRun).start();
                break;
            case R.id.btn_login:
                this.exLogin();
                break;
            case R.id.clear_log:
                this.clear_log();
                break;
        }
    }



    /**
     * 登录
     */
    private void exLogin() {
        loginInfo.setUser(et_user.getText().toString().trim());
        loginInfo.setPassword(et_password.getText().toString().trim());
        loginInfo.setVertify(et_vertify.getText().toString().trim());
        if (TextUtils.isEmpty(loginInfo.getUser())) {
            Toast.makeText(this, "用户名不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(loginInfo.getPassword())) {
            Toast.makeText(this, "密码不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (loginInfo.getVertify().length() != 4) {
            Toast.makeText(this, "验证码为4位！", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(loginRun).start();
    }

    //线程 转换验证码
    Runnable vertifyRun = new Runnable() {
        @Override
        public void run() {
            getVertify();
        }
    };
    //线程 登录网络请求
    Runnable loginRun = new Runnable() {
        @Override
        public void run() {
            login();
        }
    };

    //登录请求
    private void login() {
        Connection conn = Jsoup.connect(LoginUrl)
                .method(Connection.Method.POST)
                .timeout(6000)
                .header("Cookie", loginInfo.getCookie())
                .data("__VIEWSTATE", loginInfo.__VIEWSTATE)
                .data("TextBox1", loginInfo.getUser())
                .data("TextBox2", loginInfo.getPassword())
                .data("TextBox3", loginInfo.getVertify())
                .data("RadioButtonList1", "学生")
                .data("Button1", "").referrer(LoginUrl);
        Connection.Response response = null;
        try {
            response = conn.execute();
            String body = response.body();
            checkLogin(body);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    // 登陆结果
    private void checkLogin(String content) {
        Message msg = new Message();
        if (content.indexOf("验证码不正确") != -1) {
            msg.what = 2;
        } else if (content.indexOf("密码错误") != -1) {
            msg.what = 3;
        } else if (content.indexOf("用户名不存在或未按照要求参加教学活动") != -1) {
            msg.what = 4;
        } else if (content.indexOf("欢迎你") == -1) {

            //得到姓名
            Document document = Jsoup.parse(content);
            HttpUtils.loge("Document", document.text());
            String studentName = document.getElementById("xhxm").text();
            loginInfo.setStudentName(studentName);

            msg.what = 5;
            if (isSave.isChecked()) {// 是否保存账号密码
                saveUser(loginInfo.getUser(), loginInfo.getPassword());
            }
        } else {
            Log.i("why:", content);
        }
        handler.sendMessage(msg);// handler更新
    }

    /**
     * 得到验证码和cookie
     */
    private void getVertify() {
        Connection conn = Jsoup.connect(switchVertifyUrl)
                .ignoreContentType(true)
                .userAgent("Mozilla")
                .timeout(3000)
                .method(Connection.Method.GET);
        Connection.Response response = null;
        try {
            response = conn.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //获得cookie
        Map<String, String> getCookies = response.cookies();
        String Cookie = getCookies.toString();
        Cookie = Cookie.substring(Cookie.indexOf("{") + 1, Cookie.lastIndexOf("}"));
        Cookie = Cookie.replaceAll(",", ";");
        loginInfo.setCookie(Cookie);
        HttpUtils.loge("cookie", Cookie);
        //图片改为字节
        byte[] byte_image = response.bodyAsBytes();
        Log.i("byte_image", byte_image.toString());
        Message message = handler.obtainMessage();
        message.obj = byte_image;
        message.what = 1;
        handler.sendMessage(message);

    }
}
