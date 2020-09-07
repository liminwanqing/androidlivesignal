package athena.backtrace;

import android.content.Context;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class backtrace {
    private static String TAG = "backtrace";
    private static String libName = "athenaStack";
    private static Context mContext;
    private static String mAppID;

    public static boolean init(final Context context, final String appId) {
        mContext = context;
        mAppID = appId;

        try {
            System.loadLibrary(libName);
            Log.i(TAG, String.format("load library: %s success", libName));
            return true;
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, String.format("System.loadLibrary %s failed", libName), e);
        }

        return false;
    }

    public static String getStackTrace() {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        Throwable print = new Throwable("crashreport");
        print.printStackTrace(printWriter);
        return result.toString();
    }

    public static String getNativeStack(int num) {
        return getNativeFromJNI(num);
    }

    /**
     * A native method that is implemented by the 'athenaTrace' native library,
     * which is packaged with this application.
     */
    public static native String getNativeFromJNI(int num);
}
