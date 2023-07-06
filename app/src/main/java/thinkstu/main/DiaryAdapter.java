package thinkstu.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import thinkstu.R;

public class DiaryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private       Boolean                   isDuplicate;    //是否多选的标志
    private       Context                   mContext;       //上下文
    public static List<Diary>               mDiaryList = new ArrayList<>();//数据项列
    // 键为 RecyclerView 中各子项的 position ，值为该位置复选框的选中状态
    public static HashMap<Integer, Boolean> isSelected = new HashMap<>();

    //这是有日记展示的Holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView diaryTitle;
        TextView diaryContent;
        TextView diaryTime;
        TextView author;
        CheckBox checkBox;

        // 初始化ViewHolder
        public ViewHolder(View view) {
            super(view);
            cardView     = (CardView) view;
            diaryTitle   = view.findViewById(R.id.diary_title);
            diaryContent = view.findViewById(R.id.diary_content);
            diaryTime    = view.findViewById(R.id.diary_time);
            author       = view.findViewById(R.id.diary_author);
            checkBox     = view.findViewById(R.id.check_box);
        }
    }

    // 空白日记的Holder
    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        View     empty_view;
        TextView textView;

        public EmptyViewHolder(View view) {
            super(view);
            empty_view = view;
            textView   = view.findViewById(R.id.empty_text);
        }
    }


    // 适配器的构造函数
    public DiaryAdapter(List<Diary> diaryList, boolean isDuplicate) {
        mDiaryList       = diaryList;
        this.isDuplicate = isDuplicate;
    }


    //数据源是否为空，为空返回 -1
    @Override
    public int getItemViewType(int position) {
        if (mDiaryList.size() == 0) {
            return -1;
        }
        return super.getItemViewType(position);
    }

    //创建ViewHolder并返回
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        //数据源为空时返回空的子项布局
        if (viewType == -1) {
            return new EmptyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.diary_empty_item, parent, false));
        }
        //数据源不为空时返回卡片布局
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.diary_item, parent, false));
    }

    // 具体的处理逻辑
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        //holder为卡片布局的holder
        if (holder instanceof ViewHolder) {
            if (isDuplicate) {
                ((ViewHolder) holder).checkBox.setVisibility(View.VISIBLE);
                //当isSelected中有该位置CheckBox的显示状态时就加载，没有就设为false
                if (isSelected.containsKey(position)) {
                    ((ViewHolder) holder).checkBox.setChecked(isSelected.get(position));
                } else {
                    isSelected.put(position, false);
                }
            } else {
                //单选状态时清空isSelected，并设置所有复选框不可见
                isSelected.clear();
                ((ViewHolder) holder).checkBox.setVisibility(View.GONE);
            }
            Diary mDiary = mDiaryList.get(position);
            ((ViewHolder) holder).diaryTitle.setText(mDiary.getTitle());
            String diaryContent = mDiary.getContent();
            // 长文本省略显示
            if (diaryContent.length() > 120) {
                diaryContent = diaryContent.substring(0, 120) + "......";
            }
            ((ViewHolder) holder).diaryContent.setText(diaryContent);
            ((ViewHolder) holder).diaryTime.setText(mDiary.getTime());
            ((ViewHolder) holder).author.setText("作者：" + mDiary.getAuthor());
        }
    }

    //告诉适配器有多少个项，以便留出足够的空间
    @Override
    public int getItemCount() {
        return mDiaryList.size() > 0 ? mDiaryList.size() : 1;
    }
}