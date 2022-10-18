package com.reactnativebatteryoptimizationcheck;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.ActivityEventListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.content.Intent;
import android.app.Activity;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

@ReactModule(name = BatteryOptimizationCheckModule.NAME)
public class BatteryOptimizationCheckModule extends ReactContextBaseJavaModule {
    public static final String NAME = "BatteryOptimizationCheck";

    private static final int BATTERY_OPT_MODAL_REQUEST = 1;
    private static final String OPT_ENABLED = "enabled";
    private static final String OPT_DISABLED = "disabled";

    private final ReactApplicationContext reactContext;
    private Promise mPromise;

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() { 
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
            if(requestCode == BATTERY_OPT_MODAL_REQUEST) {
                mPromise.resolve(resultCode == 0 ? OPT_ENABLED : OPT_DISABLED); 
                mPromise = null;
            }
        }
    };

    public BatteryOptimizationCheckModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(mActivityEventListener);
        this.reactContext = reactContext;
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void isBatteryOptEnabled(Promise promise) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = reactContext.getPackageName();
            PowerManager pm = (PowerManager) reactContext.getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                promise.resolve(true);
                return;
            }
        }
        promise.resolve(false);
    }

    @SuppressLint("BatteryLife")
    @ReactMethod
    public void openRequestDisableOptimization(Promise promise) {
        mPromise = promise;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = reactContext.getPackageName();
            PowerManager pm = (PowerManager) reactContext.getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                Activity currentActivity = getCurrentActivity();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                currentActivity.startActivityForResult(intent,BATTERY_OPT_MODAL_REQUEST);
            } else {
                mPromise.resolve(OPT_DISABLED);
            }
        } else {
            mPromise.resolve(OPT_DISABLED);
        }
    }

    @ReactMethod
    public void openOptimizationSettings() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            reactContext.startActivity(intent);
        }
    }

}
