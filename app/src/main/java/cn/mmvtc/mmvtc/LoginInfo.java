package cn.mmvtc.mmvtc;

import java.io.Serializable;

/**
 * Created by W on 2019/6/22.
 *
 *
 */

public class LoginInfo implements Serializable{
    private String user;
    private String password;
    private String cookie;
    private String vertify;
    private String studentName;

    @Override
    public String toString() {
        return "LoginInfo{" +
                "user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", cookie='" + cookie + '\'' +
                ", vertify='" + vertify + '\'' +
                ", studentName='" + studentName + '\'' +
                ", __VIEWSTATE='" + __VIEWSTATE + '\'' +
                '}';
    }

    public  String __VIEWSTATE="dDw3OTkxMjIwNTU7Oz5qFv56B08dbR82AMSOW+P8WDKexA==";


    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getVertify() {
        return vertify;
    }

    public void setVertify(String vertify) {
        this.vertify = vertify;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
}
