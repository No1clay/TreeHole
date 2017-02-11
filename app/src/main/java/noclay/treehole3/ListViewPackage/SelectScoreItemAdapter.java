package noclay.treehole3.ListViewPackage;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import noclay.treehole3.R;

/**
 * Created by no_clay on 2017/2/9.
 */

public class SelectScoreItemAdapter extends
        RecyclerView.Adapter<SelectScoreItemAdapter.ViewHolder> {
    private Context mContext;
    private List<MenuItem> menus;
    OnItemClickListener onItemClickListener;


    String [] titles = {"成绩", "未通过", "四六级", "退出"};
    int[] icons = {
            R.drawable.score_white_200,
            R.drawable.warning_white_200,
            R.drawable.cet_white_200,
            R.drawable.quit_white_200
    };
    int[] colors = {
        R.color.lightBlue,
            R.color.cyan,
            R.color.blue,
            R.color.red
    };

    public SelectScoreItemAdapter(Context context) {
        this.mContext = context;
        menus = new ArrayList<>();
        for (int i = 0; i < titles.length && i < icons.length; i++) {
            MenuItem menu = new MenuItem(titles[i], icons[i]);
            menu.setColor(colors[i]);
            menus.add(menu);
        }
    }

    public interface OnItemClickListener{
        public void onItemClick(View view, int position);
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public SelectScoreItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.select_score_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        MenuItem menuItem = menus.get(position);
        holder.mIcon.setImageDrawable(
                mContext.getResources()
                        .getDrawable(menuItem.getIconId()));
        holder.mTitle.setText(menuItem.getTitle());
        holder.background.setCardBackgroundColor(
                mContext.getResources().getColor(menuItem.getColor()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return menus.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.icon)
        ImageView mIcon;
        @BindView(R.id.title)
        TextView mTitle;
        @BindView(R.id.background)
        CardView background;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    static class MenuItem{
        String title;
        int iconId;
        int color;

        public MenuItem(String title, int iconId) {

            this.title = title;
            this.iconId = iconId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getIconId() {
            return iconId;
        }

        public void setIconId(int iconId) {
            this.iconId = iconId;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }
    }
}
