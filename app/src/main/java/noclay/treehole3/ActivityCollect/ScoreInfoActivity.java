package noclay.treehole3.ActivityCollect;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import noclay.treehole3.ListViewPackage.AdapterForScore;
import noclay.treehole3.Net.OkHttpSet;
import noclay.treehole3.Net.RequestUrl;
import noclay.treehole3.Net.ScoreData;
import noclay.treehole3.R;

public class ScoreInfoActivity extends AppCompatActivity implements
        Spinner.OnItemSelectedListener {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.selectDate)
    AppCompatSpinner mSelectDate;
    @BindView(R.id.scoreList)
    RecyclerView mScoreList;
    @BindView(R.id.activity_score_info)
    LinearLayout mActivityScoreInfo;
    int type;
    RequestUrl requestUrl;
    String name;
    String number;
    String sessionId;
    //前面放的是学年，例如2015-2016， 最后一个为score的viewstate
    String[] schoolYear;
    String[] dataStrings;
    ArrayAdapter<String> spinnerAdapter;
    String scoreViewState;
    List<ScoreData> mDatas;
    List<ScoreData> temp;
    AdapterForScore adapterForScore;
    ProgressDialog loadDialog;
    Context context = this;
    int last;
    public static final int LOAD_SCORE = 0;
    public static final int GET_VIEW_STATE = 1;
    public static final int NOT_PASS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_info);
        ButterKnife.bind(this);
        initView();
        loadDialog = new ProgressDialog(context);
        loadDialog.setCancelable(false);
        loadDialog.setMessage("加载数据");
        if (loadDialog != null && !loadDialog.isShowing()) {
            loadDialog.show();
        }
        loadDialog.show();
        //获取scoreViewState
        getViewState();
        if (type == 1) {
            mSelectDate.setOnItemSelectedListener(this);
        }
    }

    private void getViewState() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                schoolYear = OkHttpSet.getSchoolYearCount(requestUrl, sessionId);
                if (schoolYear != null && schoolYear.length > 1) {
                    scoreViewState = schoolYear[schoolYear.length - 1];
                    dataStrings = new String[(schoolYear.length - 1) * 2];
                    int j = 0;
                    for (int i = 0; i < schoolYear.length - 1
                            && j < dataStrings.length; i++) {
                        dataStrings[j] = schoolYear[i] + "学年第一学期";
                        j++;
                        dataStrings[j] = schoolYear[i] + "学年第二学期";
                        j++;
                    }
                    Message message = Message.obtain();
                    message.what = GET_VIEW_STATE;
                    handler.sendMessage(message);
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (loadDialog != null) {
                                loadDialog.dismiss();
                            }
                            Toast.makeText(context, "加载失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void initView() {
        type = getIntent().getIntExtra("type", 0);
        if (type == 0) {
            //查询不及格
            mSelectDate.setVisibility(View.GONE);
        }
        name = getIntent().getStringExtra("name");
        number = getIntent().getStringExtra("number");
        sessionId = getIntent().getStringExtra("sessionId");
        requestUrl = new RequestUrl(name, number);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (type == 0) {
                actionBar.setTitle("未通过的学科");
            } else {
                actionBar.setTitle("历年成绩");
            }
        }
        mDatas = new ArrayList<>();
        adapterForScore = new AdapterForScore(mDatas, context);
        mScoreList.setAdapter(adapterForScore);
        mScoreList.setLayoutManager(new LinearLayoutManager(context));
        mScoreList.setHasFixedSize(true);
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
    public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
        if (type == NOT_PASS) {
            return;
        }
        if (position != last) {
            loadDialog = new ProgressDialog(context);
            loadDialog.setCancelable(false);
            loadDialog.setMessage("加载数据");
            loadDialog.show();
            last = position;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                temp = OkHttpSet.getScore(requestUrl,
                        schoolYear[position / 2],
                        (position % 2 + 1),
                        sessionId,
                        scoreViewState);
                Message message = Message.obtain();
                message.what = LOAD_SCORE;
                handler.sendMessage(message);
            }
        }).start();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOAD_SCORE: {
                    mDatas.clear();
                    if (loadDialog != null) {
                        loadDialog.dismiss();
                    }
                    if (temp == null) {
                        Toast.makeText(context, "加载失败", Toast.LENGTH_SHORT).show();
                    } else if (temp.size() == 0) {
                        if (type == NOT_PASS){
                            Toast.makeText(context, "太棒了，都过了哦", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(context, "还没有成绩哦", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mDatas.addAll(temp);
                    }
                    adapterForScore.notifyDataSetChanged();
                    break;
                }
                case GET_VIEW_STATE: {
                    if (type == NOT_PASS){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                temp = OkHttpSet.getNotPassScore(
                                        requestUrl,
                                        sessionId,
                                        scoreViewState);
                                Message message = Message.obtain();
                                message.what = LOAD_SCORE;
                                handler.sendMessage(message);
                            }
                        }).start();
                    }else{
                        spinnerAdapter = new ArrayAdapter<String>(context,
                                android.R.layout.simple_spinner_item, dataStrings);
                        spinnerAdapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item);
                        mSelectDate.setAdapter(spinnerAdapter);
                    }
                    break;
                }
            }
        }
    };
}
