package com.kana.ryu.bluetooth;
import android.util.Log;
public class AppLog {
    public static final String TAG = "kanabluetooth";
    private static final boolean isDebug = true;
    public static void v() {
        if (isDebug) {
            Log.v(TAG, getMetaInfo());
        }
    }
    public static void v(String message) {
        if (isDebug) {
            Log.v(TAG, getMetaInfo() + null2str(message));
        }
    }
    public static void d() {
        if (isDebug) {
            Log.d(TAG, getMetaInfo());
        }
    }
    public static void d(String message) {
        if (isDebug) {
            Log.d(TAG, getMetaInfo() + null2str(message));
        }
    }
    public static void i() {
        if (isDebug) {
            Log.i(TAG, getMetaInfo());
        }
    }
    public static void i(String message) {
        if (isDebug) {
            Log.i(TAG, getMetaInfo() + null2str(message));
        }
    }
    public static void w(String message) {
        if (isDebug) {
            Log.w(TAG, getMetaInfo() + null2str(message));
        }
    }
    public static void w(String message, Throwable e) {
        if (isDebug) {
            Log.w(TAG, getMetaInfo() + null2str(message), e);
            printThrowable(e);
            if (e.getCause() != null) {
                printThrowable(e.getCause());
            }
        }
    }
    public static void e(String message) {
        if (isDebug) {
            Log.e(TAG, getMetaInfo() + null2str(message));
        }
    }
    public static void e(String message, Throwable e) {
        if (isDebug) {
            Log.e(TAG, getMetaInfo() + null2str(message), e);
            printThrowable(e);
            if (e.getCause() != null) {
                printThrowable(e.getCause());
            }
        }
    }
    public static void e(Throwable e) {
        if (isDebug) {
            printThrowable(e);
            if (e.getCause() != null) {
                printThrowable(e.getCause());
            }
        }
    }
    private static String null2str(String string) {
        if (string == null) {
            return "(null)";
        }
        return string;
    }
    /**
     * 例外のスタックトレースをログに出力する
     *
     * @param e
     */
    private static void printThrowable(Throwable e) {
        Log.e(TAG, e.getClass().getName() + ": " + e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            Log.e(TAG, "  at " + getMetaInfo(element));
        }
    }
    /**
     * ログ呼び出し元のメタ情報を取得する
     *
     * @return [className#methodName:line]
     */
    private static String getMetaInfo() {
        // スタックトレースから情報を取得 // 0: VM, 1: Thread, 2: LogUtil#getMetaInfo, 3: LogUtil#d など, 4: 呼び出し元
        final StackTraceElement element = Thread.currentThread().getStackTrace()[4];
        return getMetaInfo(element);
    }
    /**
     * スタックトレースからクラス名、メソッド名、行数を取得する
     *
     * @return [className#methodName:line]
     */
    public static String getMetaInfo(StackTraceElement element) {
        // クラス名、メソッド名、行数を取得
        final String fullClassName = element.getClassName();
        final String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        final String methodName = element.getMethodName();
        final int lineNumber = element.getLineNumber();
        // メタ情報
        final String metaInfo = "[" + simpleClassName + "#" + methodName + ":" + lineNumber + "]";
        return metaInfo;
    }
}
