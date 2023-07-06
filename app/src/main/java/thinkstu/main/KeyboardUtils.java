package thinkstu.main;

import android.app.Activity;

import android.content.Context;

import android.view.inputmethod.InputMethodManager;

public class KeyboardUtils {
    // 隐藏键盘的方法
    public static void hideKeyboard(Activity context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        // 隐藏软键盘
        imm.hideSoftInputFromWindow(context.getWindow().getDecorView().getWindowToken(), 0);
    }
}

