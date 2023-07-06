package thinkstu.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;

import thinkstu.R;
import thinkstu.helper.Msg;
import thinkstu.main.MainActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;


public class LoginActivity extends AppCompatActivity {

    private SharedPreferences pref;//定义一个SharedPreferences对象
    private SharedPreferences.Editor editor;//调用SharedPreferences对象的edit()方法来获取一个SharedPreferences.Editor对象，用以添加要保存的数据
    private Button login;//登录按钮
    private EditText adminEdit;//用户名输入框
    private EditText passwordEdit;//密码输入框

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //布局充满整个屏幕，状态栏为透明
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_login);
        //获取各组件或对象的实例
        pref = getSharedPreferences(
                "diary_preferences",
                Context.MODE_PRIVATE
        );

        login = findViewById(R.id.login_button);
        adminEdit = findViewById(R.id.admin);
        passwordEdit = findViewById(R.id.password);
        //用户点击登录时的处理事件
        login.setOnClickListener(v -> {
            //读出用户名和密码并判断是否正确
            String account = adminEdit.getText().toString();
            String password = passwordEdit.getText().toString();
            //用户名和密码正确
            if (account.equals("admin") && password.equals("123456")) {
                editor = pref.edit();
                editor.putString("account", account);
                //提交完成数据存储
                editor.apply();
                //显示登录成功并跳转到主界面活动
                Msg.shorts(this, "登录成功");
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();   //结束当前活动
            } else {
                Msg.shorts(this, "账号或密码错误！");
            }
        });
    }
}
