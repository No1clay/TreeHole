package noclay.treehole3.ActivityCollect;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import noclay.treehole3.Net.OkHttpSet;
import noclay.treehole3.R;

public class SelectCETActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.input_name)
    EditText mInputName;
    @BindView(R.id.input_number)
    EditText mInputNumber;
    @BindView(R.id.search)
    Button mSearch;
    @BindView(R.id.searchLayout)
    LinearLayout mSearchLayout;
    @BindView(R.id.result)
    WebView mResult;
    @BindView(R.id.activity_select_cet)
    LinearLayout mActivitySelectCet;
    ProgressDialog load;
    Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_cet);
        ButterKnife.bind(this);
        load = new ProgressDialog(context);
        load.setCancelable(true);
        load.setMessage("正在加载");
        if (load != null && !load.isShowing()){
            load.show();
        }
        initView();
        mSearch.setOnClickListener(this);
    }

    private void initView() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("查询四六级");
        }
        mResult.setVisibility(View.INVISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String title = OkHttpSet.getTitleOfCET();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (load != null && load.isShowing()){
                            load.dismiss();
                        }
                        if (title == null){
                            Toast.makeText(context, "加载失败", Toast.LENGTH_SHORT).show();
                        }else{
                            mTitle.setText(Html.fromHtml(title));
                        }
                    }
                });
            }
        }).start();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.search:{
                final String name = mInputName.getText().toString();
                final String number = mInputNumber.getText().toString();
                if (name.length() < 1){
                    Toast.makeText(context, "姓名不能为空", Toast.LENGTH_SHORT).show();
                }else if (number.length() != 15){
                    Toast.makeText(context, "准考证号格式不对", Toast.LENGTH_SHORT).show();
                }else{
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final String content = OkHttpSet.getCET(name, number);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (content == null){
                                        Toast.makeText(context, "查询出错", Toast.LENGTH_SHORT).show();
                                    }else{
                                        mSearchLayout.setVisibility(View.GONE);
                                        mResult.setVisibility(View.VISIBLE);
                                        mResult.loadDataWithBaseURL(null, content,
                                                "text/html", "utf-8", null);
                                    }
                                }
                            });
                        }
                    }).start();
                }
                break;
            }
        }
    }
}
