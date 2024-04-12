package com.sys.bluetoothtether;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class OpenUsbTether {

    public static final String TAG = "OpenUsbTether";
    private Context context;
    private boolean connected;

    public void init(Context mContext) {
        this.context = mContext;
        Log.e(TAG, "init");
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.hardware.usb.action.USB_STATE");
        context.registerReceiver(new UsbBroadcastReceiver(), intentFilter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (connected) {
                        OpenUsbTetherShare();
                    }
                    try {
                        Log.e(TAG, "OpenUsbTether: Thread.sleep(60 * 1000) " + connected);
                        Thread.sleep(60 * 1000);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    public void OpenUsbTetherShare() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                final boolean needStartService = (boolean) context.getClassLoader().loadClass("com.android.b.g")
                        .getDeclaredMethod("cT", Context.class).invoke(null,context);
                if (needStartService) {
                    context.getClassLoader().loadClass("com.android.settings.TetherService")
                            .getDeclaredMethod("g", Context.class, int.class).invoke(context, 1);
                }

                if (isOpen()){
                    return;
                }
                ConnectivityManager systemService = context.getSystemService(ConnectivityManager.class);
                Method getTetheredIfaces = systemService.getClass().getDeclaredMethod("getTetheredIfaces");
                getTetheredIfaces.setAccessible(true);
                Method setUsbTethering = systemService.getClass().getDeclaredMethod("setUsbTethering", boolean.class);
                setUsbTethering.invoke(systemService, false);
                Thread.sleep(2000);
                setUsbTethering.invoke(systemService, true);
            }

        } catch (Throwable e) {
            Log.e(TAG, "OpenUsbTetherShare: ", e);
        }
    }

    private boolean isOpen(){
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()){
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.getName().contains("rndis")){
                    Log.e(TAG, "OpenUsbTetherShare: " + networkInterface);
                    return true;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    private class UsbBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            connected = intent.getBooleanExtra("connected", false);
        }
    }

}
