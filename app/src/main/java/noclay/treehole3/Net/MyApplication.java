package noclay.treehole3.Net;

import android.app.Application;
import android.content.Context;

/**
 * Created by no_clay on 2017/2/10.
 */

public class MyApplication extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    /**
     * 静态方法以供全局调用Application的context
     *
     * @return Application的Context对象
     */
    public static Context getContext() {
        return context;
    }
}
