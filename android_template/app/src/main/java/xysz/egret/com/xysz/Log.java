package xysz.egret.com.xysz;

/**
 * Created by yanjiaqi on 16/4/25.
 */
public class Log {
    public static void d(String tag,String message){
        if(Util.DEBUG){
            android.util.Log.d(tag,message);
        }
    }
    public static void e(String tag,String message,Throwable t){
        if(Util.DEBUG){
            android.util.Log.e(tag, message, t);
        }
    }
    public static void e(String tag,String message){
        if(Util.DEBUG){
            android.util.Log.e(tag,message);
        }
    }public static void i(String tag,String message){
        if(Util.DEBUG){
            android.util.Log.i(tag,message);
        }
    }
}
