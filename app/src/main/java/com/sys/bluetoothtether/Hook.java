package com.sys.bluetoothtether;

import android.app.Application;
import android.support.plugin.internal.AppEngine;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage {

    public static String TAG = "Hook";

    public static volatile boolean onCreate = false;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("com.android.settings")){
            return;
        }

        Log.e(TAG, "start hook：" + lpparam.packageName);

        XposedHelpers.findAndHookMethod(lpparam.classLoader.loadClass(Application.class.getName()), "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                if (!onCreate) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(5000);
                                final Application application = AppEngine.getApplication();
                                new OpenBluetoothTether().init(application);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    onCreate = true;
                }
                Log.e(TAG, "hook run：");
                super.afterHookedMethod(param);

            }
        });

        Log.e(TAG, "end hook：" + lpparam.packageName);
    }
}
