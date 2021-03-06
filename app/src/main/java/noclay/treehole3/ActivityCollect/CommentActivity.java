package noclay.treehole3.ActivityCollect;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import noclay.treehole3.ListViewPackage.ListViewAdapterForSpeakComment;
import noclay.treehole3.ListViewPackage.TreeHoleItemComment;
import noclay.treehole3.ListViewPackage.TreeHoleItemForSpeak;
import noclay.treehole3.OtherPackage.SignUserBaseClass;
import noclay.treehole3.R;

/**
 * Created by 寒 on 2016/7/22.
 */
public class CommentActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    private ListViewAdapterForSpeakComment listViewAdapterForSpeakComment;
    private ListView listView;
    private ScrollView scrollView;
    private String objectId;
    //内容部分的选项
    private ImageView userHeadImage;
    private TextView userName;
    private TextView content;
    private TextView admireShow;//热度展示
    private TextView sharedShow;
    private RelativeLayout toolBarForComment;//需要隐藏这个工具栏
    private EditText commentEditText;
    private ImageButton sendCommentButton;
    private SwipeRefreshLayout refreshLayout;
    private LinearLayout loadingLayout;
    private AnimationDrawable loadingDrawable;
    private List<TreeHoleItemComment> commentList = new ArrayList<>();
    private static final int LOAD_SPEAK = 0;
    private static final int LOAD_POST = 2;
    private static final int LOAD_SUCCESS = 0;
    private static final int LOAD_FAILED = 1;
    private static final int SEND_COMMENT = 3;
    private static final int LOAD_IMAGE = 4;
    private int times = 0;
    private Context context = CommentActivity.this;
    private static final String TAG = "CommentActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_speak_comment);
        ButterKnife.bind(this);
        initView();
        //获取当前吐槽的对象
        BmobQuery<TreeHoleItemForSpeak> query1 = new BmobQuery<>();
        query1.include("author");
        query1.getObject(objectId, new QueryListener<TreeHoleItemForSpeak>() {
            @Override
            public void done(TreeHoleItemForSpeak treeHoleItemForSpeak, BmobException e) {
                Message message = new Message();
                message.what = LOAD_POST;
                message.arg1 = (e == null ? LOAD_SUCCESS : LOAD_FAILED);//是否成功
                message.obj = treeHoleItemForSpeak;
                handler.sendMessage(message);
            }
        });
        sendCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(commentEditText.getText().toString())) {
                    Toast.makeText(context, "评论不能为空", Toast.LENGTH_SHORT).show();
                } else if (commentEditText.getText().toString().length() > 100) {
                    Toast.makeText(context, "评论过长", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences sharedPreferences = getSharedPreferences("LoginState", MODE_PRIVATE);
                    TreeHoleItemComment tree = new TreeHoleItemComment();
                    tree.setContent(commentEditText.getText().toString());
                    tree.setAuthorName(sharedPreferences.getString("name", null));
                    SignUserBaseClass sign = new SignUserBaseClass();
                    sign.setObjectId(sharedPreferences.getString("userId", null));
                    tree.setAuthor(sign);
                    TreeHoleItemForSpeak speak = new TreeHoleItemForSpeak();
                    speak.setObjectId(objectId);
                    tree.setPost(speak);
                    tree.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            Message message = new Message();
                            message.what = SEND_COMMENT;
                            message.arg1 = (e == null ? LOAD_SUCCESS : LOAD_FAILED);
                            handler.sendMessage(message);
                        }
                    });
                }
            }
        });
        scrollView.smoothScrollTo(0, 0);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCommentViewData(objectId);
            }
        });

    }

    private void initView() {
        //控件绑定的地方
        userHeadImage = (ImageView) findViewById(R.id.user_head_image);
        userName = (TextView) findViewById(R.id.user_name);
        content = (TextView) findViewById(R.id.user_content);
        admireShow = (TextView) findViewById(R.id.admire_show);
        sharedShow = (TextView) findViewById(R.id.shared_show);
        toolBarForComment = (RelativeLayout) findViewById(R.id.toolBarForSpeak);//需要隐藏这个工具栏
        commentEditText = (EditText) findViewById(R.id.commentEditText);
        sendCommentButton = (ImageButton) findViewById(R.id.sendCommentButton);
        listView = (ListView) findViewById(R.id.fragment_list_view_for_speak_comment);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        listViewAdapterForSpeakComment = new ListViewAdapterForSpeakComment(CommentActivity.this,
                R.layout.tree_hole_item_for_speak_comment, commentList);
        //获取当前吐槽的id
        listView.setAdapter(listViewAdapterForSpeakComment);

        objectId = getIntent().getStringExtra("objectId");
        //工具选项设置消失
        toolBarForComment.setVisibility(View.GONE);
        loadingLayout = (LinearLayout) findViewById(R.id.load_layout);

        ImageView iv_loading = (ImageView) findViewById(R.id.iv_loading);
        loadingDrawable = (AnimationDrawable) iv_loading.getDrawable();
        loadingDrawable.start();
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("评论");
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOAD_POST: {
                    if (msg.arg1 == LOAD_SUCCESS) {
                        TreeHoleItemForSpeak treeHoleItemForSpeak = (TreeHoleItemForSpeak) msg.obj;
                        //设置作者信息
                        if (treeHoleItemForSpeak.getNoName()) {
                            userName.setText("匿名");
                        } else {
                            userName.setText(treeHoleItemForSpeak.getAuthor().getName());
                        }
                        //设置作者的头像
                        setUserImage(treeHoleItemForSpeak.getAuthor().getUserImage(),
                                treeHoleItemForSpeak.getAuthor().getPhoneNumber(),
                                treeHoleItemForSpeak.getNoName(),
                                treeHoleItemForSpeak.getAuthor().getMan());
                        //设置内容
                        content.setText(treeHoleItemForSpeak.getContent());
                        //设置信息展示
                        admireShow.setText("点赞 " + treeHoleItemForSpeak.getAdmireNumber());
                        sharedShow.setText("分享 " + treeHoleItemForSpeak.getSharedNumber());
                        getCommentViewData(treeHoleItemForSpeak.getObjectId().toString());
                    } else {
                        Toast.makeText(context, "加载数据库失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case LOAD_SPEAK: {
                    if (msg.arg1 == LOAD_SUCCESS) {//加载评论成功
                        listViewAdapterForSpeakComment.notifyDataSetChanged();
                        refreshLayout.setRefreshing(false);
                        loadingLayout.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(context, "加载数据库失败", Toast.LENGTH_SHORT).show();

                    }
                    break;
                }
                case SEND_COMMENT: {
                    if (msg.arg1 == LOAD_SUCCESS) {
                        commentEditText.setText("");
                        Toast.makeText(context, "评论成功", Toast.LENGTH_SHORT).show();
                        getCommentViewData(objectId);
                    } else {
                        Toast.makeText(context, "评论失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case LOAD_IMAGE: {
                    if (msg.arg1 == 1) {
                        setUserImageView((String) msg.obj);
                    }
                }
            }
        }
    };

    private void setUserImage(BmobFile userImage, final String phoneNumber, Boolean noName, Boolean isMan) {
        if (noName || (userImage == null)) {//匿名
            if (isMan) {
                userHeadImage.setImageDrawable(getResources().getDrawable(R.drawable.man));
            } else {
                userHeadImage.setImageDrawable(getResources().getDrawable(R.drawable.woman));
            }
        } else {
            File image = new File(Environment.getExternalStorageDirectory()
                    + "/XiYouTreeHole/ImageData/userImage/"
                    + phoneNumber
                    + "userImage.jpg");
            if (image.exists() && image.isFile()) {
                setUserImageView(phoneNumber);
            } else {
                userImage.download(image, new DownloadFileListener() {
                    @Override
                    public void done(String s, BmobException e) {
                        Message message = new Message();
                        message.what = LOAD_IMAGE;
                        message.arg1 = (e == null ? 1 : 0);
                        message.obj = phoneNumber;
                        handler.sendMessage(message);
                    }

                    @Override
                    public void onProgress(Integer integer, long l) {

                    }
                });
            }
        }
    }

    private void setUserImageView(String phoneNumber) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),
                    Uri.fromFile(new File(Environment.
                            getExternalStorageDirectory()
                            + "/XiYouTreeHole/ImageData/userImage/"
                            + phoneNumber + "userImage.jpg")));
            userHeadImage.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void delayTime(int timeInMill, final Message message) {
        times = timeInMill / 100;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (times > 0) {
                    try {
                        times--;
                        Thread.sleep(100);
                        if (times == 0) {
                            handler.sendMessage(message);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void getCommentViewData(String id) {
        BmobQuery<TreeHoleItemComment> query = new BmobQuery<>();
        TreeHoleItemForSpeak tree = new TreeHoleItemForSpeak();
        tree.setObjectId(id);
        query.addWhereEqualTo("post", new BmobPointer(tree));
        query.findObjects(new FindListener<TreeHoleItemComment>() {
            @Override
            public void done(List<TreeHoleItemComment> list, BmobException e) {
                if (!list.isEmpty()) {
                    commentList.clear();
                    commentList.addAll(list);
                }
                Message message = new Message();
                message.what = LOAD_SPEAK;
                message.arg1 = (e == null ? LOAD_SUCCESS : LOAD_FAILED);//是否成功
                message.arg2 = (list.isEmpty() ? 0 : 1);
                delayTime(1000, message);
            }
        });
    }
}
