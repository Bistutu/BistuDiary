package thinkstu.edit;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import thinkstu.R;
import thinkstu.main.KeyboardUtils;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import thinkstu.main.Diary;

import android.widget.EditText;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

public class EditActivity extends AppCompatActivity {

    //接收上个活动传入的日记内容
    private String diaryTitle;
    private String diaryContent;
    //接收上个活动传入的标志
    private int    signal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        //接收由 MainActivity 传来的日记信息
        Intent intent = getIntent();
        diaryTitle   = intent.getStringExtra("diaryTitle");
        diaryContent = intent.getStringExtra("diaryContent");
        signal       = intent.getIntExtra("signal", 0);
        Toolbar toolbar = findViewById(R.id.edit_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_keyboard_backspace_24);
        }
        final EditText editText = findViewById(R.id.edit_content);
        editText.setText(diaryContent);
        //光标放文本后面
        editText.setSelection(editText.getText().length());
        editText.setOnClickListener(v -> editText.setCursorVisible(true));
        final EditText editTitle = findViewById(R.id.edit_title);
        editTitle.setText(diaryTitle);
        //光标放文本后面
        editTitle.setSelection(editTitle.getText().length());
        editTitle.setOnClickListener(v -> editTitle.setCursorVisible(true));
    }

    //加载菜单
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_toolbar, menu);
        return true;
    }

    //菜单项的点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //点击保存
            case R.id.save_button: {
                EditText editText  = findViewById(R.id.edit_content);
                EditText editTitle = findViewById(R.id.edit_title);
                String   title     = editTitle.getText().toString();
                String   content   = editText.getText().toString();
                // 构造日记对象
                Diary mDiary = getDiary("默认用户", title, content);

                // 保存日记
                if (signal == 0) {
                    mDiary.save();
                    // 防止连续点击’存储‘按钮连续存储一样的内容
                    signal = 3;
                    //显示登录成功并跳转到主界面活动
                }
                // 修改日记
                else {
                    //防止连续点击’存储‘按钮连续存储一样的内容
                    signal = 3;
                    updateDiary(mDiary, "content=?", diaryContent);
                    //显示登录成功并跳转到主界面活动
                }
                Toast.makeText(EditActivity.this, "保存成功！", Toast.LENGTH_SHORT).show();
                finish();
                //隐藏光标并收起键盘
                editText.setCursorVisible(false);
                KeyboardUtils.hideKeyboard(this);
                break;
            }
            //点击返回
            case android.R.id.home: {
                //已经保存的直接返回
                if (signal == 3) {
                    finish();
                }
                //未保存的提示是否保存
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("");
                    builder.setMessage("保存此次修改吗？");
                    builder.setPositiveButton("确认", (dialog, which) -> {
                        String title   = ((EditText) findViewById(R.id.edit_title)).getText().toString();
                        String content = ((EditText) findViewById(R.id.edit_content)).getText().toString();
                        // 构造日记对象
                        Diary mDiary = getDiary("", title, content);
                        if (signal == 0) {
                            mDiary.save();
                        } else {
                            updateDiary(mDiary, "title=?", diaryTitle);
                        }
                        finish();
                    });
                    builder.setNegativeButton("取消", (dialog, which) -> finish());
                    builder.create().show();
                }
                break;
            }
            default:
        }
        return true;
    }

    private void updateDiary(Diary mDiary, String x, String diaryContent) {
        ContentValues values = new ContentValues();
        values.put("time", mDiary.getTime());
        DataSupport.updateAll(Diary.class, values, "content=?", diaryContent);
        values.put("title", mDiary.getTitle());
        DataSupport.updateAll(Diary.class, values, x, diaryContent);
        values.put("content", mDiary.getContent());
        DataSupport.updateAll(Diary.class, values, "content=?", diaryContent);
    }

    @NonNull
    private Diary getDiary(String account, String title, String content) {
        //获得用户名
        SharedPreferences pref   = getSharedPreferences("diary_preferences", Context.MODE_PRIVATE);
        String            author = pref.getString("account", account);
        String            time   = new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(new java.util.Date());
        Diary             mDiary = new Diary(title, content, time, author);
        return mDiary;
    }
}