package com.janrain.android.engage.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import com.janrain.android.engage.JREngage;

import java.lang.reflect.Field;

/**
 * Created by IntelliJ IDEA.
 * User: nathan
 * Date: 6/2/11
 * Time: 4:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidUtils {
    public static final String TAG = AndroidUtils.class.getSimpleName();
    private AndroidUtils() {}

    public static boolean isSmallOrNormalScreen() {
        int screenConfig = JREngage.getContext().getResources().getConfiguration().screenLayout;
        screenConfig &= Configuration.SCREENLAYOUT_SIZE_MASK;

        // Galaxy Tab 7" (the first one) reports SCREENLAYOUT_SIZE_NORMAL
        // Motorola Xoom reports SCREENLAYOUT_SIZE_XLARGE
        // Nexus S reports SCREENLAYOUT_SIZE_NORMAL

        return screenConfig == Configuration.SCREENLAYOUT_SIZE_NORMAL ||
                screenConfig == Configuration.SCREENLAYOUT_SIZE_SMALL;
    }

    public static boolean isCupcake() {
        return Build.VERSION.RELEASE.startsWith("1.5");
    }

    public static int getAndroidSdkInt() {
        Field SDK_INT = null;
        try {
            SDK_INT = Build.VERSION.class.getField("SDK_INT");

            return (Integer) SDK_INT.getInt(null);
        } catch (NoSuchFieldException e) {
            // Must be Cupcake
            return 3;
        } catch (IllegalAccessException e) {
            // Not expected
            throw new RuntimeException(e);
        }
    }

    public static ApplicationInfo getApplicationInfo() {
        String packageName = JREngage.getContext().getPackageName();
        try {
            return JREngage.getContext().getPackageManager().getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static int scaleDipPixels(int dip) {
        Context c = JREngage.getContext();
        final float scale = c.getResources().getDisplayMetrics().density;
        return (int) (((float) dip) * scale);
    }
}
