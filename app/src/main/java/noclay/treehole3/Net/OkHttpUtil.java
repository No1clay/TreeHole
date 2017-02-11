package noclay.treehole3.Net;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by no_clay on 2017/2/10.
 */

public class OkHttpUtil {
    /**
     * 实例化静态okHttpClient对象
     */
    public static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .cookieJar(new CookiesManager()).build();
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


    public static OkHttpClient getOkHttpClient() {

        return okHttpClient;
    }

    public static String getUrlLogin() {
        return URL_LOGIN;
    }

    public static String getUrlCode() {
        return URL_CODE;
    }

    public static String getREFERER() {
        return REFERER;
    }

    /**
     * 获得一个Request对象
     *
     * @param url 请求的Url
     * @return 返回一个Request对象
     */
    public static Request getRequest(String url) {
        Request request = new Request.Builder().url(url).addHeader("Host", HOST).addHeader("Referer", REFERER).build();
        return request;
    }

    /**
     * 获得一个Request对象
     *
     * @param url         请求的Url
     * @param requestBody 请求的参数
     * @return 返回一个Request对象
     */
    public static Request getRequest(String url, RequestBody requestBody) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Host", HOST)
                .addHeader("Referer", REFERER)
                .post(requestBody)
                .build();
        return request;
    }

    /**
     * 获得一个Request对象
     *
     * @param url         请求的Url
     * @param Referer     请求头Referer
     * @param requestBody 请求的参数
     * @return 返回一个Request对象
     */
    public static Request getRequest(String url, String Referer, RequestBody requestBody) {
        Request request = new Request.Builder().url(url).addHeader("Host", HOST).addHeader("Referer", Referer).post(requestBody).build();
        return request;
    }

    /**
     * 获得一个Requese对象
     *
     * @param url     请求的Url
     * @param Host    请求头Host
     * @param Referer 请求头Referer
     * @return 返回一个Request对象
     */
    public static Request getRequest(String url, String Host, String Referer) {
        Request request = new Request.Builder().url(url).addHeader("Host", Host).addHeader("Referer", Referer).build();
        return request;
    }

    /**
     * 将Url中的中文进行编码
     *
     * @param url 要进行编码的Url
     * @return 编码后的url
     */
    public static String encodeUrl(String url) {
        String new_url = url;
        Matcher matcher = Pattern.compile("[\\u4e00-\\u9fa5]").matcher(new_url);
        while (matcher.find()) {
            try {
                new_url = new_url.replaceAll(matcher.group(), URLEncoder.encode(matcher.group(), "gb2312"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return new_url;
    }
}
