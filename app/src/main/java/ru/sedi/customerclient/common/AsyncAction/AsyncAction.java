package ru.sedi.customerclient.common.AsyncAction;


import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.sedi.customerclient.common.LogUtil;

public class AsyncAction {
    private static int actionId = 1;
    private static ExecutorService pool = Executors.newCachedThreadPool();
    private static Handler mHandler = new Handler(Looper.getMainLooper());

    public static void run(final IFunc<Object> action) {
        run(action, null);
    }

    public static <T> void run(final IFunc<T> action, final IActionFeedback<T> complete) {
        pool.submit(new Runnable() {
            public void run() {
                Thread.currentThread().setName("AsyncAction Id: ".concat(Integer.toString(actionId++)));
                Exception exception = null;
                T result = null;
                try {
                    result = action.Func();
                } catch (Exception ex) {
                    exception = ex;
                }

                if (complete != null)
                    mHandler.post(new CompliteAction<T>(complete, exception, result));
            }
        });
    }

    public static void runInMainThread(Runnable runnable) {
        try {
            mHandler.post(runnable);
        } catch (Exception e) {
            LogUtil.log(e);
        }
    }
}
