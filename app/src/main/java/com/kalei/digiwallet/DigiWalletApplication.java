package com.kalei.digiwallet;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import android.app.Application;

import io.fabric.sdk.android.Fabric;

/**
 * Created by risaki on 4/24/16.
 */
public class DigiWalletApplication extends Application {

    public static String FLURRY_KEY = "JGBTZJXTZFXBS5VY6T56";
    //    public static PhotoLocationApplication mInstance;
    //TODO: turn off debug at the end.
    public static boolean debug = false;

//    public PhotoLocationApplication() {
//        mInstance = this;
//    }
//
//    public static PhotoLocationApplication getInstance() {
//        return mInstance;
//    }

//    protected void attachBaseContext(Context base) {
//        super.attachBaseContext(base);
////        MultiDex.install(this);
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(debug).build())
                .build();
    }

//    public static String getVersionName(Context context) {
//        PackageInfo pInfo;
//        try {
//            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
//            return pInfo.versionName;
//        } catch (NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        return "error";
//    }
//
//    public static int getVersionCode(Context context) {
//        PackageInfo pInfo;
//
//        try {
//            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
//            return pInfo.versionCode;
//        } catch (NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        return -1;
//    }
}
