package noclay.treehole3;

import android.animation.ObjectAnimator;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.mob.mobapi.MobAPI;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import de.hdodenhof.circleimageview.CircleImageView;
import noclay.treehole3.ActivityCollect.AddLoveActivity;
import noclay.treehole3.ActivityCollect.AddSpeakActivity;
import noclay.treehole3.ActivityCollect.ChangePassWord;
import noclay.treehole3.ActivityCollect.LoginActivity;
import noclay.treehole3.ActivityCollect.ManagerLoveActivity;
import noclay.treehole3.ActivityCollect.ManagerSpeakActivity;
import noclay.treehole3.ActivityCollect.SelectScoreActivity;
import noclay.treehole3.FragmentCollect.LoveWallFragment;
import noclay.treehole3.FragmentCollect.SpeakFragment;
import noclay.treehole3.OtherPackage.SignUserBaseClass;
import noclay.treehole3.SelectPopupWindow.ChooseImageDialog;
import noclay.treehole3.SelectPopupWindow.SelectPopupWindow;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.nav_view)
    NavigationView mNavView;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    View headerView;
    //底部按钮
    private LinearLayout loveButton, speakButton;
    private ImageView loveButtonIcon, speakButtonIcon;
    private TextView loveButtonTitle, speakButtonTitle;
    private ImageView toggleAddMenuButton;

    private CircleImageView nowUserImage;
    private TextView nowUserName;

    private FragmentManager fragmentManager;
    private LoveWallFragment loveWallFragment;
    private SpeakFragment speakFragment;

    private SelectPopupWindow mMenuView;
    private ChooseImageDialog chooseUserImageDialog;
    private long mExitTime;//退出的时间
    private Uri userImageUri;
    private LocalBroadcastManager localBroadcastManager;
    private LocalReceiver localReceiver;
//    private TextView temperature;
//    private TextView weatherView;
    private boolean isAddMenuOpen = false;
    private Context context = MainActivity.this;
    private String userImagePath;
    private static final int REQUEST_CODE_PICK_IMAGE = 0;
    private static final int REQUEST_CODE_CAPTURE_CAMEIA = 1;
    private static final int REQUEST_RESIZE_REQUEST_CODE = 2;
    private static final int MESSAGE_FROM_USER_IMAGE = 0;
    private static final int MESSAGE_FROM_UP_USER_IAMGE = 2;
    private static final int REQUEST_LOGIN = 3;
    private static final int REQUEST_THEME = 4;
    private static final String TAG = "MainActivity";
    public static final String ROOT_PATH =
            Environment.getExternalStorageDirectory() + "/XiYouTreeHole/ImageData/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        ButterKnife.bind(this);
        Bmob.initialize(MainActivity.this, "e7a1bf15265fddb02517d7d9181fe6a6");
        //初始化天气的Api
        MobAPI.initSDK(MainActivity.this, "18a8f9ead5620");
        //如果有登录的状态，则不进行登录
        //检查是否登录
        initView();

        checkIsLogined();
        nowUserImage.setOnClickListener(this);
        toggleAddMenuButton.setOnClickListener(this);
//        toggleTheme.setOnClickListener(this);
        loveButton.setOnClickListener(this);
        speakButton.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            }
            case R.id.help:{
                Snackbar.make(mToolbar, "暂无帮助信息", Snackbar.LENGTH_SHORT)
                        .setAction("返回", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(MainActivity.this,
                                        "谢谢", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
                break;
            }
        }
        return true;
    }

//    private void getWeather(String city) {
//        final Weather weather = (Weather) MobAPI.getAPI(Weather.NAME);
//        weather.queryByCityName("西安", new APICallback() {
//            @Override
//            public void onSuccess(API api, int i, Map<String, Object> map) {
//                ArrayList<HashMap<String, Object>> results = (ArrayList<HashMap<String, Object>>) map.get("result");
//                HashMap<String, Object> local = results.get(0);
//                ArrayList<HashMap<String, Object>> future = (ArrayList<HashMap<String, Object>>) local.get("future");
////                        showWeather.setText("温度" + firstDay.get("temperature") + "\n");
//                HashMap<String, Object> firstDay = future.get(0);
//                temperature.setText(firstDay.get("temperature").toString());
//                String dayTime = (String) firstDay.get("dayTime");
//                String night = (String) firstDay.get("night");
//                Log.d(TAG, "onSuccess: firstDay" + firstDay.toString());
//                Log.d(TAG, "onSuccess: dayTime" + dayTime);
//                Log.d(TAG, "onSuccess: night" + night);
//                String data;
//                if (dayTime != null && night != null) {
//                    data = dayTime.equals(night) ? dayTime : dayTime + "~" + night;
//                } else if (dayTime != null && night == null) {
//                    data = dayTime;
//                } else if (dayTime == null && night != null) {
//                    data = night;
//                } else {
//                    data = "暂无数据";
//                }
//                weatherView.setText("西安 " + data);
//            }
//
//            @Override
//            public void onError(API api, int i, Throwable throwable) {
//                temperature.setText("网络错误");
//            }
//        });
//    }

    private void chooseUserImage() {//选择照片
        chooseUserImageDialog = new ChooseImageDialog(context, new
                View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        chooseUserImageDialog.dismiss();
                        switch (v.getId()) {
                            case R.id.takePhotoBtn: {
                                String state = Environment.getExternalStorageState();
                                if (state.equals(Environment.MEDIA_MOUNTED)) {
                                    Intent getImageByCamera = new
                                            Intent("android.media.action.IMAGE_CAPTURE");
                                    startActivityForResult(getImageByCamera,
                                            REQUEST_CODE_CAPTURE_CAMEIA);
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "请确认已经插入SD卡", Toast.LENGTH_LONG).show();
                                }
                                break;
                            }
                            case R.id.pickPhotoBtn:
//                                Intent intent = new Intent(Intent.ACTION_PICK);//从相册中选取图片
                                Intent intent = new Intent("android.intent.action.GET_CONTENT");//从相册/文件管理中选取图片
                                intent.setType("image/*");//相片类型
                                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
                                break;
                            case R.id.cancelBtn: {
                                break;
                            }
                        }
                    }
                });
        chooseUserImageDialog.showAtLocation(findViewById(R.id.drawer_layout),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }


    private boolean isOpenNetWork() {
        ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connect.getActiveNetworkInfo() != null) {
            return connect.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }

    private void checkIsLogined() {
        SharedPreferences shared = getSharedPreferences("LoginState", MODE_PRIVATE);
        boolean isLogined = shared.getBoolean("loginRememberState", false);
        if (!isLogined) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, REQUEST_LOGIN);
        } else {
            setNowUser();
        }
    }

    private void setNowUser() {//设置用户的状态
        SharedPreferences shared = getSharedPreferences("LoginState", MODE_PRIVATE);
        nowUserName.setText(shared.getString("name", "您还没有登录"));
        final String phoneNumber = shared.getString("userName", "");
        userImagePath = Environment.getExternalStorageDirectory() +
                "/XiYouTreeHole/ImageData/userImage/" + phoneNumber + "userImage.jpg";
//        Log.d(TAG, "initUserImage() called with: " + "头像初始化");
        final File userImage = new File(userImagePath);
        if (userImage.exists()) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.
                        getContentResolver(), Uri.fromFile(userImage));
                nowUserImage.setImageBitmap(bitmap);
//                Log.d(TAG, "initUserImage() called with: " + "头像初始化完成");
            } catch (IOException e) {
                e.printStackTrace();
//                Log.e(TAG, "initUserImage: ", e);
            }
        } else if (isOpenNetWork()) {
            BmobQuery<SignUserBaseClass> user = new BmobQuery<>();
            user.getObject(shared.getString("userId", ""), new QueryListener<SignUserBaseClass>() {
                @Override
                public void done(SignUserBaseClass signUserBaseClass, BmobException e) {
                    if (e == null) {
                        BmobFile bmobFile = signUserBaseClass.getUserImage();
                        if (bmobFile != null) {
                            bmobFile.download(userImage, new DownloadFileListener() {
                                @Override
                                public void done(String s, BmobException e) {
                                    Message message = new Message();
                                    message.what = MESSAGE_FROM_USER_IMAGE;
                                    message.arg1 = 1;
                                    message.obj = s;
                                    handler.sendMessage(message);
                                }

                                @Override
                                public void onProgress(Integer integer, long l) {

                                }
                            });
                        } else {
                            Message message = new Message();
                            message.what = MESSAGE_FROM_USER_IMAGE;
                            message.arg1 = 0;
                            handler.sendMessage(message);
                        }
                    } else {
                        Toast.makeText(context, "数据库异常", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        } else {
            Toast.makeText(context, "你的网络状况不佳", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleButtonMenu() {
        mMenuView = new SelectPopupWindow(context, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.add_love_button: {
                        Intent intent = new Intent(context, AddLoveActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.add_speak_button: {
                        Intent intent = new Intent(context, AddSpeakActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.close_select_menu_button: {
                        break;
                    }
                }
                mMenuView.dismiss();
            }
        });
        mMenuView.showAtLocation(findViewById(R.id.mainLayout),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void setFooterTheme(int curNumber) {
        clearFooterTheme();
        switch (curNumber) {
            case 1: {
                loveButtonIcon.setImageDrawable(this.getResources().getDrawable(R.drawable.love_up));
                loveButtonTitle.setTextColor(getResources().getColor(R.color.mainBackground));
                setSelectFragment(1);
                break;
            }
            case 3: {
                speakButtonIcon.setImageDrawable(getResources().getDrawable(R.drawable.speak1));
                speakButtonTitle.setTextColor(getResources().getColor(R.color.mainBackground));
                setSelectFragment(2);
                break;
            }
        }
    }

    private void setSelectFragment(int i) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (loveWallFragment != null) {
            transaction.hide(loveWallFragment);
        }
        if (speakFragment != null) {
            transaction.hide(speakFragment);
        }
        if (i == 1) {
            if (loveWallFragment == null) {
                loveWallFragment = new LoveWallFragment();
                transaction.add(R.id.main_view_pager, loveWallFragment);
            } else {
                transaction.show(loveWallFragment);
            }
        } else if (i == 2) {
            if (speakFragment == null) {
                speakFragment = new SpeakFragment();
                transaction.add(R.id.main_view_pager, speakFragment);
            } else {
                transaction.show(speakFragment);
            }
        }
        transaction.commit();
    }


    private void clearFooterTheme() {
        loveButtonIcon.setImageDrawable(getResources().getDrawable(R.drawable.love));
        speakButtonIcon.setImageDrawable(getResources().getDrawable(R.drawable.speak));
        loveButtonTitle.setTextColor(getResources().getColor(R.color.lightGray));
        speakButtonTitle.setTextColor(getResources().getColor(R.color.lightGray));
    }

    private void initView() {
//        coverLayout = findViewById(cover_layout);
//        coverLayout.setAlpha(0);
//        temperature = (TextView) findViewById(temperature);
//        weatherView = (TextView) findViewById(R.id.weather);
//        toggleTheme = (RelativeLayout) findViewById(R.id.toggle_theme);
        loveButton = (LinearLayout) findViewById(R.id.love_button_layout);
        loveButtonIcon = (ImageView) findViewById(R.id.love_button_icon);
        loveButtonTitle = (TextView) findViewById(R.id.love_button_title);
        speakButton = (LinearLayout) findViewById(R.id.speak_button);
        speakButtonIcon = (ImageView) findViewById(R.id.speak_button_icon);
        speakButtonTitle = (TextView) findViewById(R.id.speak_button_title);
        toggleAddMenuButton = (ImageView) findViewById(R.id.add_button);
        headerView = mNavView.inflateHeaderView(R.layout.nav_header);
        nowUserImage = (CircleImageView) headerView.findViewById(R.id.userImage);
        nowUserName = (TextView) headerView.findViewById(R.id.userName);
        fragmentManager = getFragmentManager();
        setFooterTheme(1);
        //存储头像所在的目录
        File fileDir = new File(Environment.getExternalStorageDirectory() + "/XiYouTreeHole/ImageData/userImage");
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        //初始化广播
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("LOGIN_SUCCESS");
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.menu_32);
        }
        mNavView.setCheckedItem(R.id.my_tree_hole);
        mNavView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.my_tree_hole: {
                        Intent intent1 = new Intent(context, ManagerSpeakActivity.class);
                        startActivity(intent1);
                        break;
                    }
                    case R.id.my_love_wall: {
                        Intent intent2 = new Intent(context, ManagerLoveActivity.class);
                        startActivity(intent2);
                        break;
                    }
                    case R.id.change_passWord: {
//                        Log.d(TAG, "onClick() called with: " + "view = [修改密码]");
                        Intent intent3 = new Intent(MainActivity.this, ChangePassWord.class);
                        intent3.putExtra("isLogin", false);
                        startActivity(intent3);
                        break;
                    }
                    case R.id.toggle_account: {
//                        Log.d(TAG, "onClick() called with: " + "view = [切换用户]");
                        Intent intent4 = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent4);
                        break;
                    }
                    case R.id.my_score:{
                        Intent intent = new Intent(MainActivity.this, SelectScoreActivity.class);
                        startActivity(intent);
                        break;
                    }
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
//        //初始化天气
//        getWeather("西安");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(localReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isAddMenuOpen) {//如果菜单在打开的状态
                isAddMenuOpen = false;
            } else if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(context, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_FROM_USER_IMAGE: {
                    if (msg.arg1 == 1) {
                        String imagePath = (String) msg.obj;
                        nowUserImage.setImageURI(Uri.parse(imagePath));
                    } else {
                        nowUserImage.setImageDrawable(getResources().getDrawable(R.drawable.man));
                    }
                    break;
                }
                case MESSAGE_FROM_UP_USER_IAMGE: {
                    String phoneNumber = getSharedPreferences("LoginState", MODE_PRIVATE).
                            getString("userName", null);
                    String path = Environment.getExternalStorageDirectory() +
                            "/XiYouTreeHole/ImageData/userImage/";
                    if (msg.arg1 == 1) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.
                                    getContentResolver(), Uri.fromFile(new File(path +
                                    phoneNumber + "userImage.jpg")));
                            nowUserImage.setImageBitmap(bitmap);
                            Toast.makeText(context, "头像上传成功", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
//                            Log.e(TAG, "onActivityResult: " , e);
                        }
                    } else {
                        //头像上传失败
                        boolean flag1, flag2;
                        File oldFile = new File(path + phoneNumber + "userImage_copy.jpg");
                        flag1 = oldFile.renameTo(new File(path + phoneNumber + "userImage.jpg"));
//                        Log.d(TAG, "设置副本:" + oldFile.getAbsolutePath() + flag1);
                        Toast.makeText(context, "头像上传失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {

            } else {
                Log.d(TAG, "onActivityResult: exit");
                finish();
                System.exit(0);
            }
        }
        Uri imageUri;
        if (resultCode == RESULT_CANCELED) {
        } else if (resultCode == RESULT_OK) {//选取成功后进行裁剪
            switch (requestCode) {
                case REQUEST_CODE_PICK_IMAGE: {
                    //从图库中选择图片作为头像
                    imageUri = data.getData();
                    reSizeImage(imageUri);
                    break;
                }
                case REQUEST_CODE_CAPTURE_CAMEIA: {
                    //使用相机获取头像
                    imageUri = data.getData();
//                    Log.d(TAG, "onActivityResult: " + imageUri);
                    if (imageUri == null) {
                        //use bundle to get data
                        Bundle bundle = data.getExtras();
                        if (bundle != null) {
                            Bitmap bitMap = (Bitmap) bundle.get("data"); //get bitmap
                            imageUri = Uri.parse(MediaStore.Images.Media.
                                    insertImage(getContentResolver(), bitMap, null, null));
//                            Log.d(TAG, "onActivityResult: bndle != null" + imageUri);
                            reSizeImage(imageUri);
                        } else {
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                }
                case REQUEST_RESIZE_REQUEST_CODE: {
                    //剪切图片返回
//                    Log.d(TAG, "剪切完毕：" + userImageUri);
                    if (userImageUri == null) {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                    } else {//截取图片完成
                        String phoneNumber = getSharedPreferences("LoginState", MODE_PRIVATE).
                                getString("userName", null);

                        //新旧文件的替换

                        String path = Environment.getExternalStorageDirectory() +
                                "/XiYouTreeHole/ImageData/userImage/";
                        File oldFile = new File(path + phoneNumber + "userImage.jpg");
                        oldFile.renameTo(new File(path + phoneNumber + "userImage_copy.jpg"));
                        File newFile = new File(path + "crop.jpg");
                        newFile.renameTo(new File(path + phoneNumber + "userImage.jpg"));

                        final BmobFile bmobFile = new BmobFile(new File(path + phoneNumber + "userImage.jpg"));
                        bmobFile.upload(new UploadFileListener() {//尝试上传头像
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    SignUserBaseClass user = new SignUserBaseClass();
                                    user.setObjectId(getSharedPreferences("LoginState", MODE_PRIVATE).
                                            getString("userId", null));
                                    user.setUserImage(bmobFile);
                                    user.update(new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            Message message = new Message();
                                            message.what = MESSAGE_FROM_UP_USER_IAMGE;
                                            message.arg1 = (e == null ? 1 : 0);
                                            handler.sendMessage(message);
                                        }
                                    });
                                } else {
                                    Message message = new Message();
                                    message.what = MESSAGE_FROM_UP_USER_IAMGE;
                                    message.arg1 = 0;
                                    handler.sendMessage(message);
                                }
                            }
                        });
                        //尝试上传更新头像

                    }
                    break;
                }
            }
        }
    }

    private void reSizeImage(Uri uri) {//重新剪裁图片的大小
//        Log.d(TAG, "尝试剪切的文件输出" + "uri = [" + uri + "]");
        File outputImage = new File(Environment.getExternalStorageDirectory() + "/XiYouTreeHole/ImageData/userImage/crop.jpg");
        try {
            if (outputImage.exists()) {
                outputImage.getAbsoluteFile().delete();
//                Log.d(TAG, "删除 " + "uri = [" + outputImage.getAbsolutePath() + "]");
            }
            outputImage.createNewFile();
//            Log.d(TAG, "创建 " + "uri = [" + outputImage.getAbsolutePath() + "]");

        } catch (Exception e) {
            e.printStackTrace();
//            Log.d(TAG, "reSizeImage() called with: " + "uri = [" + e.toString() + "]");
        }
        userImageUri = Uri.fromFile(outputImage);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", true);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);// 去黑边
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);//输出是X方向的比例
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高，切忌不要再改动下列数字，会卡死
        intent.putExtra("outputX", 500);//输出X方向的像素
        intent.putExtra("outputY", 500);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", false);//设置为不返回数据
        /**
         * 此方法返回的图片只能是小图片（测试为高宽160px的图片）
         * 故将图片保存在Uri中，调用时将Uri转换为Bitmap，此方法还可解决miui系统不能return data的问题
         */
//        intent.putExtra("return-data", true);
//        intent.putExtra("output", Uri.fromFile(new File("/mnt/sdcard/temp")));//保存路径
        intent.putExtra(MediaStore.EXTRA_OUTPUT, userImageUri);
//        Log.d(TAG, "reSizeImage() called with: " + "uri = [" + userImageUri + "]");
        startActivityForResult(intent, REQUEST_RESIZE_REQUEST_CODE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.toggle_theme:
////                Toast.makeText(context, "切换主题", Toast.LENGTH_SHORT).show();
////                Intent intent = new Intent(this, ThemeActivity.class);
////                startActivityForResult(intent, REQUEST_THEME);
//////                        setToggleTheme(true);
//                break;
            case R.id.love_button_layout:
                setFooterTheme(1);
                break;
            case R.id.add_button: {
                ObjectAnimator objectAnimator = new ObjectAnimator()
                        .ofFloat(toggleAddMenuButton, View.ROTATION, 0, 45);
                objectAnimator.setDuration(500);
                objectAnimator.start();
                toggleButtonMenu();
                mMenuView.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        ObjectAnimator objectAnimator = new ObjectAnimator()
                                .ofFloat(toggleAddMenuButton, View.ROTATION, 45, 0);
                        objectAnimator.setDuration(500);
                        objectAnimator.start();
                    }
                });
                break;
            }
            case R.id.speak_button:
                setFooterTheme(3);
                break;

            case R.id.userImage: {//添加头像
                chooseUserImage();
                break;
            }
        }
    }

    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            setNowUser();
        }
    }

}
