package noclay.treehole3.Net;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import noclay.treehole3.MainActivity;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static noclay.treehole3.ActivityCollect.SelectScoreActivity.MSG_DOWNLOAD_IMAGE;
import static noclay.treehole3.ActivityCollect.SelectScoreActivity.MSG_DOWNLOAD_IMAGE_OK;
import static noclay.treehole3.ActivityCollect.SelectScoreActivity.MSG_ERROR;
import static noclay.treehole3.ActivityCollect.SelectScoreActivity.MSG_LOGIN;

/**
 * Created by no_clay on 2017/2/11.
 */

public class OkHttpSet {

    public static final String CHECK_CODE_PATH =
            MainActivity.ROOT_PATH + "Cache/checkCode.png";
    public static final String CACHE_PATH =
            MainActivity.ROOT_PATH + "Cache/";
    /**
     * Host
     */
    public static final String HOST = "222.24.19.201";
    /**
     * Referer
     */
    public static final String REFERER = "http://222.24.19.201/";
    /**
     * 验证码请求地址
     */
    public static final String URL_CODE = REFERER + "CheckCode.aspx";
    /**
     * 登录地址
     */
    public static final String URL_LOGIN = REFERER + "default2.aspx";

    /**
     * 传入当前的Url，用于请求验证码
     * 返回值为sessionId，即Cookie，如果为null，则请求失败
     * 验证码存储在MainActivity.ROOT_PATH + "Cache/checkCode.png"
     *
     * @param checkCodeUrl
     * @return
     */
    public static String getVerifyImage(String checkCodeUrl) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(checkCodeUrl)
                .get()
                .build();
        String sessionId = null;
        try {
            Response reponse = client.newCall(request).execute();
            if (reponse.code() == 200) {
                File path = new File(CACHE_PATH);
                if (!path.exists()) {
                    path.mkdir();
                }
                final File verifyImage = new File(CHECK_CODE_PATH);
                if (verifyImage.exists()) {
                    verifyImage.delete();
                }
                InputStream is = reponse.body().byteStream();
                OutputStream os = new FileOutputStream(verifyImage);
                byte[] b = new byte[1024];
                int c;
                while ((c = is.read(b)) > 0) {
                    os.write(b, 0, c);
                }
                is.close();
                os.close();
                String value = reponse.header("Set-Cookie");
                sessionId = value.substring(0, value.indexOf(";"));
                Log.d("sessionId", "getVerifyImage: " + sessionId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sessionId;
    }


    /**
     * 请求失败内容:
     * 验证码不正确！！
     * 用户名不能为空！！
     * 密码不能为空！！
     * 验证码不能为空，如看不清请刷新！！
     * @param number
     * @param password
     * @param verifyCode
     * @param cookie
     * @param handler
     */
    public static void login(String number,
                             String password,
                             String verifyCode,
                             String cookie,
                             Handler handler) {
        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .build();
        RequestBody builder = new FormBody.Builder()
                .add("__VIEWSTATE", "dDwtNTE2MjI4MTQ7Oz61IGQDPAm6cyppI+uTzQcI8sEH6Q==")
                .add("__VIEWSTATEGENERATOR", "92719903")
                .add("txtUserName", number)
                .add("TextBox2", password)
                .add("txtSecretCode", verifyCode)
                .add("RadioButtonList1", "学生")
                .add("Button1", "")
                .add("lbLanguage", "")
                .add("hidPdrs", "")
                .add("hidsc", "")
                .build();
        Request request = new Request.Builder()
                .url(OkHttpUtil.getUrlLogin())
                .addHeader("Host", HOST)
                .addHeader("Referer", REFERER)
                .addHeader("Cookie", cookie)
                .post(builder)
                .build();
        try {
            Response response = client.newCall(request).execute();
            Document document = Jsoup.parse(response.body().string());
            Elements ele = document.select("#form1 > script");
            int start = ele.toString().indexOf("('") + 2;
            int end = ele.toString().indexOf("')");
            int len = ele.toString().length();
            if ((start >= 0 && start < len)
                    && (end >= 0 && end < len)) {
                final String toastContent = ele.toString().substring(
                        ele.toString().indexOf("('") + 2,
                        ele.toString().indexOf("')")
                );
                Message message = Message.obtain();
                message.what = MSG_LOGIN;
                message.obj = toastContent;
                message.arg1 = 1;
                handler.sendMessage(message);
                return;
            } else {
                //登陆成功
                Elements element = document.select("#xhxm");
                String temp = element.text();
                if (temp != null) {
                    int endI = temp.indexOf("同学");
                    if (endI >= 0) {
                        String name = temp.substring(0, endI);
                        //开始查找个人主页的头像
                        Message message = Message.obtain();
                        message.what = MSG_LOGIN;
                        message.obj = name;
                        message.arg1 = 0;
                        handler.sendMessage(message);
                        return;
                    }
                }
            }
        } catch (IOException e) {
            Message message = Message.obtain();
            message.what = MSG_ERROR;
            message.arg1 = -1;
            message.obj = "未成功登录";
            handler.sendMessage(message);
            e.printStackTrace();
        }
    }

    /**
     * 获取个人头像：找到url
     * @param mRequestUrl
     * @param sessionId
     * @param handler
     * @return
     */
    public static boolean getMessage(RequestUrl mRequestUrl, String sessionId, Handler handler) {
        if (mRequestUrl == null) {
            return false;
        }
        Log.d("session", "getMessage: " + mRequestUrl.getMessageUrl());
        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(mRequestUrl.getMessageUrl())
                .addHeader("Cookie", sessionId)
                .addHeader("Referer", mRequestUrl.getHomeUrl())
                .get()
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() == 200) {
                Document document = Jsoup.parse(response.body().string());
                Elements ele = document.select("#xszp");
                Message message = Message.obtain();
                message.what = MSG_DOWNLOAD_IMAGE;
                message.obj = ele.attr("src");
                handler.sendMessage(message);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 获取头像：下载
     * @param mRequestUrl
     * @param name
     * @param number
     * @param url
     * @param sessionId
     * @param handler
     */
    public static void getImage(RequestUrl mRequestUrl,
                          String name,
                          String number,
                          String url,
                          String sessionId,
                          Handler handler) {
        if (mRequestUrl == null) {
            return;
        }
        OkHttpClient client = new OkHttpClient.Builder()
                .followSslRedirects(false)
                .followRedirects(false)
                .build();
        Request request = new Request.Builder()
                .url(mRequestUrl.getIP() + url)
                .addHeader("Cookie", sessionId)
                .addHeader("Referer", mRequestUrl.getIP() + url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() == 200) {
                File file = new File(CACHE_PATH);
                if (!file.exists()) {
                    file.mkdir();
                }
                File image = new File(CACHE_PATH + number + ".png");
                if (!image.exists()) {
                    InputStream is = response.body().byteStream();
                    OutputStream os = new FileOutputStream(image);
                    byte[] b = new byte[1024 * 10];
                    int c;
                    while ((c = is.read(b)) > 0) {
                        os.write(b, 0, c);
                    }
                    os.close();
                    is.close();
                }
                Message message = Message.obtain();
                message.what = MSG_DOWNLOAD_IMAGE_OK;
                message.obj = image.getAbsolutePath();
                handler.sendMessage(message);
            }
        } catch (IOException e) {
            Message message = Message.obtain();
            message.what = MSG_ERROR;
            message.arg1 = -1;
            message.obj = "未成功登录";
            handler.sendMessage(message);
            e.printStackTrace();
        }

    }

    /**
     * 获取ViewState
     * @param requestUrl
     * @param sessionId
     * @return
     */
    public static String[] getSchoolYearCount(RequestUrl requestUrl, String sessionId){
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
        Request request = new Request.Builder()
                .url(requestUrl.getScoreUrl())
                .addHeader("Cookie", sessionId)
                .addHeader("Host", HOST)
                .addHeader("Referer", requestUrl.getHomeUrl())
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.code() == 200){
                Document document = Jsoup.parse(response.body().string());
                Elements ele = document.select("#ddlXN > option");
                if (ele.size() != 1){
                    String[] result = new String[ele.size()];
                    for (int i = 1; i < ele.size(); i++) {
                        result[i - 1] = ele.get(i).text();
                    }
                    Elements view = document.select("#Form1 > input");
                    result[ele.size() - 1] = view.get(2).attr("value");
                    return result;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取历年成绩
     * @param requestUrl
     * @param schoolYear
     * @param term
     * @param sessionId
     * @param scoreViewState
     * @return
     */
    public static List<ScoreData> getScore(RequestUrl requestUrl,
                                           String schoolYear,
                                           int term,
                                           String sessionId,
                                           String scoreViewState){
        OkHttpClient client = new OkHttpClient.Builder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
        FormBody formBody = new FormBody.Builder()
                .add("__EVENTTARGET", "")
                .add("__EVENTARGUMENT", "")
                .add("__VIEWSTATE", scoreViewState)
                .add("hidLanguage", "")
                .add("ddlXN", schoolYear)
                .add("ddlXQ", term + "")
                .add("ddl_kcxz", "")
                .add("btn_xq", "%D1%A7%C6%DA%B3%C9%BC%A8")
                .build();
        Request request = new Request.Builder()
                .url(requestUrl.getScoreUrl())
                .addHeader("Cookie", sessionId)
                .addHeader("Referer", requestUrl.getHomeUrl())
                .addHeader("Host", HOST)
                .post(formBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            Log.d("score", "getScore: code = " + response.code());
            if (response.code() == 200){
                Document document = Jsoup.parse(response.body().string());
                Elements ele = document.select("#Datagrid1 > tbody > tr");
                List<ScoreData> result = new ArrayList<>();
                for (int i = 1; i < ele.size(); i++) {
                    Elements scoreItem = ele.get(i).children();
                    ScoreData score = new ScoreData();
                    score.setDate(scoreItem.get(0).text() + "-" + scoreItem.get(1).text());
                    score.setCode(scoreItem.get(2).text());
                    score.setName(scoreItem.get(3).text());
                    if ("必修课".equals(scoreItem.get(4).text())){
                        score.setSelect(false);
                    }else{
                        score.setSelect(true);
                    }
                    score.setCredit(scoreItem.get(6).text());
                    score.setGrade(scoreItem.get(7).text());
                    score.setScore(scoreItem.get(8).text());
                    score.setResit(scoreItem.get(10).text());
                    score.setRebuild(scoreItem.get(11).text());
                    score.setAcademy(scoreItem.get(12).text());
                    result.add(score);
                }
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * 获取挂科
     * @param requestUrl
     * @param sessionId
     * @param scoreViewState
     * @return
     */
    public static List<ScoreData> getNotPassScore(RequestUrl requestUrl,
                                           String sessionId,
                                           String scoreViewState){
        OkHttpClient client = new OkHttpClient.Builder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
        FormBody formBody = new FormBody.Builder()
                .add("__EVENTTARGET", "")
                .add("__EVENTARGUMENT", "")
                .add("__VIEWSTATE", scoreViewState)
                .add("hidLanguage", "")
                .add("ddlXN", "")
                .add("ddlXQ", "")
                .add("ddl_kcxz", "")
                .add("Button2", "%CE%B4%CD%A8%B9%FD%B3%C9%BC%A8")
                .build();
        Request request = new Request.Builder()
                .url(requestUrl.getScoreUrl())
                .addHeader("Cookie", sessionId)
                .addHeader("Referer", requestUrl.getHomeUrl())
                .addHeader("Host", HOST)
                .post(formBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            Log.d("score", "getScore: code = " + response.code());
            if (response.code() == 200){
                Document document = Jsoup.parse(response.body().string());
                Elements ele = document.select("#Datagrid3 > tbody > tr");
                List<ScoreData> result = new ArrayList<>();
                for (int i = 1; i < ele.size(); i++) {
                    Elements scoreItem = ele.get(i).children();
                    ScoreData score = new ScoreData();
                    score.setDate("");
                    score.setCode(scoreItem.get(0).text());
                    score.setName(scoreItem.get(1).text());
                    if ("必修课".equals(scoreItem.get(2).text())){
                        score.setSelect(false);
                    }else{
                        score.setSelect(true);
                    }
                    score.setCredit(scoreItem.get(3).text());
                    score.setGrade("");
                    score.setScore(scoreItem.get(4).text());
                    score.setResit("");
                    score.setRebuild("");
                    score.setAcademy("");
                    result.add(score);
                }
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
