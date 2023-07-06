package thinkstu.main;

import android.app.AlertDialog;
import android.content.Intent;

import thinkstu.R;
import thinkstu.edit.EditActivity;
import thinkstu.helper.Msg;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    HashMap<Integer, String> str = new HashMap<>();     // 要删除的项的标志HashMap数组
    private int          update        = 0;             // 设置是否在onStart中更新数据源的标志
    private List<Diary>  diaryList     = new ArrayList<>(); // 存储diary对象的数组
    private DiaryAdapter adapter;
    // 是否处于多选删除状态，设置这个变量是为了让区分正常点击和多选删除时的点击事件，以及长按状态时不再响应长按事件
    private boolean      isDeleteState = false;
    // 网格布局管理器，1列
    GridLayoutManager   gridLayoutManager   = new GridLayoutManager(this, 1);
    // 线性布局管理器
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    SwipeRefreshLayout  swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化按钮和RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        Button       build        = findViewById(R.id.build_button);
        Button       delete       = findViewById(R.id.delete_button);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        // 设置缓存大小，防止数据过多时卡顿
        recyclerView.setItemViewCacheSize(100);
        // 下拉刷新
        swipeRefreshLayout.setOnRefreshListener(() -> refreshDiaries());
        //从数据库中获取全部日记
        diaryList = DataSupport.order("id desc").find(Diary.class);
        // 如果日记数大于0，就使用网格布局管理器，否则使用线性布局管理器
        if (diaryList.size() > 0) {
            recyclerView.setLayoutManager(gridLayoutManager);
        } else {
            recyclerView.setLayoutManager(linearLayoutManager);
        }
        // 初始化适配器，传入日记数组和是否处于多选删除状态
        adapter = new DiaryAdapter(diaryList, false);
        recyclerView.setAdapter(adapter);

        // 点击新建按钮时，跳转到编辑界面，默认为空白信息
        build.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditActivity.class);
            intent.putExtra("diaryTitle", "");
            intent.putExtra("diaryContent", "");
            intent.putExtra("signal", 0);   // 0 表示新建
            startActivity(intent);
        });

        // 点击删除按钮时，弹出对话框，确认删除
        delete.setOnClickListener(v -> deleteSelections());

        // RecyclerView 的回调函数，添加点击事件响应和长按事件响应
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            //点击事件响应
            @Override
            public void onItemClick(View view, int position) {
                if (!isDeleteState && diaryList.size() > 0) {
                    // 普通点击事件
                    Diary  mDiary       = diaryList.get(position);
                    String diaryTitle   = mDiary.getTitle();
                    String diaryContent = mDiary.getContent();
                    String author       = mDiary.getAuthor();
                    Intent intent       = new Intent(MainActivity.this, EditActivity.class);
                    //传递标题、内容、作者
                    intent.putExtra("diaryTitle", diaryTitle);
                    intent.putExtra("diaryContent", diaryContent);
                    intent.putExtra("author", author);
                    //传入修改标志1：表示修改原有日记内容
                    intent.putExtra("signal", 1);
                    startActivity(intent);
                } else if (diaryList.size() > 0) {
                    //长按进入多选状态后的点击事件
                    CheckBox checkBox = view.findViewById(R.id.check_box);
                    if (checkBox.isChecked()) {
                        checkBox.setChecked(false);
                        str.remove(position);
                    } else {
                        str.put(position, DiaryAdapter.mDiaryList.get(position).getTitle());
                        str.put(position, DiaryAdapter.mDiaryList.get(position).getContent());
                        checkBox.setChecked(true);
                    }
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // 长按事件
                if (!isDeleteState && diaryList.size() > 0) {
                    isDeleteState = true;
                    str.clear();
                    //把当前选中的的复选框设置为选中状态
                    DiaryAdapter.isSelected.put(position, true);
                    //把所有的CheckBox显示出来
                    RecyclerView recyclerView = findViewById(R.id.recycler_view);
                    //第二个参数为true表示长按进入多选删除状态时的适配器初始化
                    adapter = new DiaryAdapter(diaryList, true);
                    recyclerView.setAdapter(adapter);
                    str.put(position, DiaryAdapter.mDiaryList.get(position).getTitle());
                    str.put(position, DiaryAdapter.mDiaryList.get(position).getContent());
                }
            }
        }));

        // 左滑删除日记卡片
        ItemTouchHelper.SimpleCallback itemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("提示");
                builder.setMessage("您确认要删除这条日记吗？");
                // 保存viewHolder，用于在对话框关闭时处理数据
                final RecyclerView.ViewHolder viewHolderSaved = viewHolder;
                builder.setPositiveButton("确认", (dialog, which) -> {
                    int position = viewHolderSaved.getAdapterPosition();
                    // 删除数据库中相应的日记项
                    Diary diary = diaryList.get(position);
                    DataSupport.deleteAll(Diary.class, "title = ? and content = ?", diary.getTitle(), diary.getContent());
                    // 删除数据源中相应的数据并更新RecyclerView
                    diaryList.remove(position);
                    adapter.notifyItemRemoved(position);
                    // 如果删除后数据源为空，需要更新LayoutManager
                    if (diaryList.size() == 0) {
                        recyclerView.setLayoutManager(linearLayoutManager);
                    }
                    Msg.shorts(MainActivity.this, "日记已删除~");
                });

                builder.setNegativeButton("取消", (dialog, which) -> {
                    // 用户取消删除操作，需要重置已经滑动的item
                    adapter.notifyItemChanged(viewHolderSaved.getAdapterPosition());
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        };
        // 创建 ItemTouchHelper 并将 Callback 传入
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        // 将 ItemTouchHelper 和 RecyclerView 关联起来
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    //用户返回该活动时
    @Override
    protected void onStart() {
        super.onStart();
        isDeleteState = false;
        if (update == 1) {
            diaryList.clear();
            RecyclerView recyclerView = findViewById(R.id.recycler_view);
            List<Diary>  data         = DataSupport.order("id desc").find(Diary.class);
            for (Diary diary : data) {
                diaryList.add(diary);
            }
            if (diaryList.size() > 0) {
                recyclerView.setLayoutManager(gridLayoutManager);
            } else {
                recyclerView.setLayoutManager(linearLayoutManager);
            }
            adapter = new DiaryAdapter(diaryList, false);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        update = 1;//更新数据源
    }

    //执行删除的函数
    private void deleteSelections() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (str.size() == 0) {
            Msg.shorts(this, "长按选择日记删除~");
        } else {
            builder.setTitle("提示");
            builder.setMessage("确认删除所选日记？");
            builder.setPositiveButton("确认", (dialog, which) -> {
                for (int i : str.keySet()) {
                    DataSupport.deleteAll(Diary.class, "content=?", str.get(i));
                }
                diaryList.clear();
                List<Diary> data = DataSupport.order("id desc").find(Diary.class);
                for (Diary diary : data) {
                    diaryList.add(diary);
                }
                adapter.notifyDataSetChanged();
                RecyclerView recyclerView = findViewById(R.id.recycler_view);
                if (diaryList.size() > 0) {
                    recyclerView.setLayoutManager(gridLayoutManager);
                } else {
                    recyclerView.setLayoutManager(linearLayoutManager);
                }
                adapter = new DiaryAdapter(diaryList, false);
                recyclerView.setAdapter(adapter);
                isDeleteState = false;
                str.clear();
                Toast.makeText(MainActivity.this, "删除成功！", Toast.LENGTH_SHORT).show();
            });
            builder.setNegativeButton("取消", (dialog, which) -> {
                RecyclerView recyclerView = findViewById(R.id.recycler_view);
                adapter = new DiaryAdapter(diaryList, false);
                recyclerView.setAdapter(adapter);
                isDeleteState = false;
                str.clear();
            });
            builder.create().show();
        }
    }

    public void refreshDiaries() {
        // 从数据库中重新获取日记
        List<Diary> newDiaryList = DataSupport.order("id desc").find(Diary.class);
        // 清空原有日记列表数据
        diaryList.clear();
        // 添加新的日记数据
        diaryList.addAll(newDiaryList);
        // 更新界面
        adapter.notifyDataSetChanged();
        // 判断使用的LayoutManager
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        if (diaryList.size() > 0) {
            recyclerView.setLayoutManager(gridLayoutManager);
        } else {
            recyclerView.setLayoutManager(linearLayoutManager);
        }
        // 刷新操作完成，隐藏刷新进度条
        swipeRefreshLayout.setRefreshing(false);
        Msg.shorts(this, "刷新成功~");
    }
}