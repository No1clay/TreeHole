package noclay.treehole3.ListViewPackage;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import noclay.treehole3.Net.ScoreData;
import noclay.treehole3.R;

/**
 * Created by no_clay on 2017/2/12.
 */

public class AdapterForScore extends RecyclerView.Adapter<AdapterForScore.ViewHolder> {
    private List<ScoreData> mDatas;
    private Context mContext;
    private int resource;

    public AdapterForScore(List<ScoreData> datas, Context context) {
        mDatas = datas;
        mContext = context;
        resource = R.layout.score_show_item;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(resource, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ScoreData scoreData = mDatas.get(position);
        holder.mCourseAcademy.setText("开课学院：" + scoreData.getAcademy());
        holder.mCourseCode.setText("代码：" + scoreData.getCode());
        holder.mCourseCredit.setText("学分：" + scoreData.getCredit());
        holder.mCourseDate.setText("学期：" + scoreData.getDate());
        holder.mCourseGrade.setText("绩点：" + scoreData.getGrade());
        holder.mCourseName.setText(scoreData.getName());
        holder.mCourseResit.setText("补考：" + scoreData.getResit());
        holder.mCourseRebuild.setText("重修：" + scoreData.getRebuild());
        holder.mCourseScore.setText("成绩：" + scoreData.getScore());
        if (scoreData.isSelect()){
            holder.mCourseType.setImageDrawable(
                    mContext.getResources().getDrawable(R.drawable.xuanxiu_200));
        }else{
            holder.mCourseType.setImageDrawable(
                    mContext.getResources().getDrawable(R.drawable.bixiu_200));
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.courseType)
        ImageView mCourseType;
        @BindView(R.id.courseName)
        TextView mCourseName;
        @BindView(R.id.courseCode)
        TextView mCourseCode;
        @BindView(R.id.courseDate)
        TextView mCourseDate;
        @BindView(R.id.courseAcademy)
        TextView mCourseAcademy;
        @BindView(R.id.courseCredit)
        TextView mCourseCredit;
        @BindView(R.id.courseGrade)
        TextView mCourseGrade;
        @BindView(R.id.courseScore)
        TextView mCourseScore;
        @BindView(R.id.courseResit)
        TextView mCourseResit;
        @BindView(R.id.courseRebuild)
        TextView mCourseRebuild;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
