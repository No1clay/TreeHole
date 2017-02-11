package noclay.treehole3.ActivityCollect;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import noclay.treehole3.ListViewPackage.SelectScoreItemAdapter;
import noclay.treehole3.MainActivity;
import noclay.treehole3.Net.OkHttpUtil;
import noclay.treehole3.Net.RequestUrl;
import noclay.treehole3.R;
import noclay.treehole3.SelectPopupWindow.InputPopWindow;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SelectScoreActivity extends AppCompatActivity
        implements SelectScoreItemAdapter.OnItemClickListener,
        View.OnClickListener {

    @BindView(R.id.user_image)
    CircleImageView mUserImage;
    @BindView(R.id.user_name)
    TextView mUserName;
    @BindView(R.id.loginLayout)
    LinearLayout mLoginLayout;
    @BindView(R.id.menuList)
    RecyclerView mMenuList;
    ProgressDialog progressDialog;
    SelectScoreItemAdapter adapter;
    InputPopWindow inputWindow;
    //网络请求的变量
    String checkCodeUrl;
    String sessionId;
    String viewState = "dDwtNTE2MjI4MTQ7Oz61IGQDPAm6cyppI+uTzQcI8sEH6Q==";
    String name;
    String studentNumber;
    String privateMainASPX = "http://222.24.19.201/xs_main.aspx?xh=";
    RequestUrl mRequestUrl;
    private Context context = SelectScoreActivity.this;
    public static final int MSG_ERROR = -1;
    public static final int MSG_LOGIN = 0;
    public static final int MSG_DOWNLOAD_IMAGE = 1;
    public static final int MSG_DOWNLOAD_IMAGE_OK = 2;
    private static final String TAG = "SelectScoreActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_score);
        ButterKnife.bind(this);
        initView();
        initFiled();
    }

    private void initFiled() {
        checkCodeUrl = "http://222.24.19.201/CheckCode.aspx";
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("我的成绩");
        }
        Drawable drawable = mLoginLayout.getBackground();
        drawable.setAlpha(50);
        mLoginLayout.setBackgroundDrawable(drawable);
        adapter = new SelectScoreItemAdapter(SelectScoreActivity.this);
        mMenuList.setLayoutManager(new GridLayoutManager(SelectScoreActivity.this, 2));
        mMenuList.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
        mUserImage.setOnClickListener(this);
    }

    @Override
    public void onItemClick(View view, int position) {
        switch (position) {
            case 0: {
                Intent intent = new Intent(context, ScoreInfoActivity.class);
                startActivity(intent);
                break;
            }
            case 1: {
                Intent intent = new Intent(context, ScoreInfoActivity.class);
                startActivity(intent);
                break;
            }
            case 2: {
                break;
            }
            case 3: {
                mUserImage.setClickable(true);
                Toast.makeText(this, "已经退出，可以切换用户了哦", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_image: {
                inputWindow = new InputPopWindow(SelectScoreActivity.this, this);
                if (inputWindow != null && !inputWindow.isShowing()) {
                    SharedPreferences sp = getSharedPreferences("student", MODE_PRIVATE);
                    String number = sp.getString("number", "");
                    String password = sp.getString("password", "");
                    inputWindow.initData(number, password);
//                    getViewState();
                    getVerifyImage();
                    inputWindow.showAtLocation(findViewById(R.id.content_select_score),
                            Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
                }
                break;
            }
            case R.id.verify_image: {
//                getViewState();
                getVerifyImage();
                break;
            }
            case R.id.login: {
                if (inputWindow != null) {
                    inputWindow.dismiss();
                }
                login();
                break;
            }
        }
    }

    /**
     * 请求失败内容:
     * 验证码不正确！！
     * 用户名不能为空！！
     * 密码不能为空！！
     * 验证码不能为空，如看不清请刷新！！
     */
    public void login() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("正在登录");
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient()
                        .newBuilder()
                        .build();
                studentNumber = inputWindow.getStudentNumber();
                /**
                 * 测试部分
                 */
                RequestBody builder = new FormBody.Builder()
                        .add("__VIEWSTATE", "dDwtNTE2MjI4MTQ7Oz61IGQDPAm6cyppI+uTzQcI8sEH6Q==")
                        .add("__VIEWSTATEGENERATOR", "92719903")
                        .add("txtUserName", inputWindow.getStudentNumber().trim())
                        .add("TextBox2", inputWindow.getPassWord().trim())
                        .add("txtSecretCode", inputWindow.getVerifyNumber())
                        .add("RadioButtonList1", "学生")
                        .add("Button1", "")
                        .add("lbLanguage", "")
                        .add("hidPdrs", "")
                        .add("hidsc", "")
                        .build();
                Request request = new Request.Builder()
                        .url(OkHttpUtil.getUrlLogin())
                        .addHeader("Host", OkHttpUtil.HOST)
                        .addHeader("Referer", OkHttpUtil.REFERER)
                        .addHeader("Cookie", sessionId)
                        .post(builder)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    Document document = Jsoup.parse(response.body().string());
                    Elements ele = document.select("#form1 > script");
                    Log.d(TAG, "run: ele = " + ele.toString());
                    int start = ele.toString().indexOf("('") + 2;
                    int end = ele.toString().indexOf("')");
                    int len = ele.toString().length();
                    if ((start >= 0 && start < len)
                            && (end >= 0 && end < len)) {
                        final String toastContent = ele.toString().substring(
                                ele.toString().indexOf("('") + 2,
                                ele.toString().indexOf("')")
                        );
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, toastContent, Toast.LENGTH_SHORT).show();
                            }
                        });
                        Log.d(TAG, "run: content = " + toastContent);
                    } else {
                        //登陆成功
                        Elements element = document.select("#xhxm");
                        String temp = element.text();
                        if (temp != null) {
                            int endI = temp.indexOf("同学");
                            if (endI >= 0) {
                                name = temp.substring(0, endI);
                                mRequestUrl = new RequestUrl(name, studentNumber);
                                Log.d(TAG, "run: 名字: " + name);
                                Log.d(TAG, "run: 学号: " + studentNumber);
                                //开始查找个人主页的头像
                                rememberUser(studentNumber, inputWindow.getPassWord());
                                Message message = Message.obtain();
                                message.what = MSG_LOGIN;
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
        }).start();
    }

    private void rememberUser(String studentNumber, String passWord) {
        SharedPreferences sp = context.getSharedPreferences("student", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("number", studentNumber);
        editor.putString("password", passWord);
        editor.commit();
    }

    public boolean getMessage() {
        if (mRequestUrl == null) {
            return false;
        }
        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(mRequestUrl.getMessageUrl())
                .addHeader("Cookie", sessionId)
                .addHeader("Referer", privateMainASPX + studentNumber)
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


    public String getPicture(OkHttpClient client) {
        return null;
    }

    public void getViewState() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document = Jsoup.connect("http://222.24.62.120/default2.aspx").get();
                    Elements elements = document.select("#form1 > input[type=\"hidden\"]");
                    String temp = elements.attr("value");
                    if (temp != null) {
                        Log.d(TAG, "run: temp  = " + temp);
                        viewState = temp;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public void getVerifyImage() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(checkCodeUrl)
                        .get()
                        .build();
                try {
                    Response reponse = client.newCall(request).execute();
                    if (reponse.code() == 200) {
                        File path = new File(MainActivity.ROOT_PATH + "Cache/");
                        if (!path.exists()) {
                            path.mkdir();
                        }
                        final File verifyImage = new File(MainActivity.ROOT_PATH + "Cache/checkCode.png");
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                inputWindow.setVerifyImage(verifyImage.getAbsolutePath());
                            }
                        });
                        String value = reponse.header("Set-Cookie");
                        sessionId = value.substring(0, value.indexOf(";"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        checkCodeUrl += "?";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_LOGIN: {
                    //不可点击
                    mUserImage.setClickable(false);
                    mUserName.setText(name);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Boolean flag = getMessage();
                            if (!flag){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "未获取到头像信息", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }).start();
                    break;
                }
                case MSG_DOWNLOAD_IMAGE: {
                    final String url = (String) msg.obj;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getImage(name, studentNumber, url, sessionId);
                        }
                    }).start();
                    break;
                }
                case MSG_DOWNLOAD_IMAGE_OK: {
                    String path = (String) msg.obj;
                    Log.d(TAG, "handleMessage: path = " + path);
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.
                                getContentResolver(), Uri.fromFile(new File(path)));
                        mUserImage.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    progressDialog.dismiss();
                    break;
                }
                case MSG_ERROR:{
                    if (msg.arg1 == -1){
                        //登录失败
                        progressDialog.dismiss();
                        Snackbar.make(mUserImage, "未成功登录", Snackbar.LENGTH_SHORT)
                                .setAction("返回", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        finish();
                                    }
                                }).show();
                    }
                    break;
                }
            }
        }
    };

    private void getImage(String name, String number, String url, String sessionId) {
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
                File file = new File(MainActivity.ROOT_PATH + "Cache/");
                if (!file.exists()) {
                    file.mkdir();
                }
                File image = new File(MainActivity.ROOT_PATH + "Cache/" + number + ".png");
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
            e.printStackTrace();
        }

    }

}
