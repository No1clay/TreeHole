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

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import noclay.treehole3.ListViewPackage.SelectScoreItemAdapter;
import noclay.treehole3.Net.OkHttpSet;
import noclay.treehole3.Net.RequestUrl;
import noclay.treehole3.R;
import noclay.treehole3.SelectPopupWindow.InputPopWindow;

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
    String name;
    String studentNumber;
    RequestUrl mRequestUrl;
    private boolean isLogined = false;
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
        checkCodeUrl = RequestUrl.identifyCode;
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
                if (!isLogined){
                    Toast.makeText(this, "还没有登陆", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(context, ScoreInfoActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("number", studentNumber);
                intent.putExtra("sessionId", sessionId);
                intent.putExtra("type", 1);
                startActivity(intent);
                break;
            }
            case 1: {
                if (!isLogined){
                    Toast.makeText(this, "还没有登陆", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(context, ScoreInfoActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("number", studentNumber);
                intent.putExtra("sessionId", sessionId);
                intent.putExtra("type", 0);
                startActivity(intent);
                break;
            }
            case 2: {
                Intent intent = new Intent(context, SelectCETActivity.class);
                startActivity(intent);
                break;
            }
            case 3: {
                if (!isLogined){
                    Toast.makeText(this, "还没有登陆", Toast.LENGTH_SHORT).show();
                    return;
                }
                isLogined = false;
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
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sessionId = OkHttpSet.getVerifyImage(checkCodeUrl);
                            checkCodeUrl += "?";
                            if (sessionId != null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        inputWindow.setVerifyImage(OkHttpSet.CHECK_CODE_PATH);
                                    }
                                });
                            }
                        }
                    }).start();
                    inputWindow.showAtLocation(findViewById(R.id.content_select_score),
                            Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
                }
                break;
            }
            case R.id.verify_image: {
//                getViewState();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sessionId = OkHttpSet.getVerifyImage(checkCodeUrl);
                        if (sessionId != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    inputWindow.setVerifyImage(OkHttpSet.CHECK_CODE_PATH);
                                }
                            });
                        }
                    }
                }).start();
                break;
            }
            case R.id.login: {
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("正在登录");
                progressDialog.setCancelable(false);
                progressDialog.show();
                studentNumber = inputWindow.getStudentNumber();
                final String password = inputWindow.getPassWord();
                final String verifyCode = inputWindow.getVerifyNumber();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OkHttpSet.login(
                                studentNumber,
                                password,
                                verifyCode,
                                sessionId,
                                handler);
                    }
                }).start();
                break;
            }
        }
    }


    private void rememberUser(String studentNumber, String passWord) {
        SharedPreferences sp = context.getSharedPreferences("student", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("number", studentNumber);
        editor.putString("password", passWord);
        editor.commit();
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
                    if (msg.arg1 == 1) {
                        //没有登陆成功
                        progressDialog.dismiss();
                        Toast.makeText(context, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    } else {
                        isLogined = true;
                        rememberUser(studentNumber, inputWindow.getPassWord());
                        inputWindow.dismiss();
                        name = (String) msg.obj;
                        mUserImage.setClickable(false);
                        mUserName.setText(name);
                        mRequestUrl = new RequestUrl(name, studentNumber);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Boolean flag = OkHttpSet.getMessage(
                                        mRequestUrl,
                                        sessionId,
                                        handler);
                                if (!flag) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();
                                            Toast.makeText(context,
                                                    "未获取到头像信息",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }).start();
                    }

                    break;
                }
                case MSG_DOWNLOAD_IMAGE: {
                    final String url = (String) msg.obj;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            OkHttpSet.getImage(mRequestUrl, name, studentNumber, url, sessionId, handler);
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
                case MSG_ERROR: {
                    if (msg.arg1 == -1) {
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


}
