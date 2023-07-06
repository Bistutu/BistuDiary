package thinkstu.helper;

import android.content.Context;
import android.widget.Toast;

public class Msg {
    public static void shorts(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    public static void longs(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
