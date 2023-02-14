package com.sys.bluetoothtether;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

public class OpenBluetoothTether {

    public static final String TAG = "OpenBluetoothTether";

    private AtomicReference<Object> mBluetoothPan = new AtomicReference<>();
    private volatile boolean connect = false;
    private BluetoothProfile.ServiceListener mProfileServiceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
            connect = true;
            Log.i(TAG, "onServiceConnected");
            mBluetoothPan.set(bluetoothProfile);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (connect) {
                        openBluetoothShare();
                        try {
                            Log.d(TAG, "OpenBluetoothTether: Thread.sleep(60 * 1000)");
                            Thread.sleep(60 * 1000);
                        } catch (Throwable e) {

                        }
                    }
                }
            }).start();
        }

        @Override
        public void onServiceDisconnected(int i) {
            connect = false;
            Log.i(TAG, "onServiceDisconnected");
        }
    };

    public void init(Context mContext) {
        Log.e(TAG, "init");
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        defaultAdapter.getProfileProxy(mContext, mProfileServiceListener, 5);
    }

    public void openBluetoothShare() {
        try {
            Object bluetoothPan = mBluetoothPan.get();
            Class bluetoothPanClass = Class.forName("android.bluetooth.BluetoothPan");
            if (bluetoothPan != null) {
                Method methodIsTethering = bluetoothPanClass.getMethod("isTetheringOn");
                boolean open = (boolean) methodIsTethering.invoke(bluetoothPan);
                Log.i(TAG, "isTetheringOn = " + open);
                if (!open) {
                    Method methodSetBluetoothTethering = bluetoothPanClass.getMethod("setBluetoothTethering", boolean.class);
                    methodSetBluetoothTethering.invoke(bluetoothPan, true);
                    Log.i(TAG, "isTetheringOn = " + methodIsTethering.invoke(bluetoothPan));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "openBluetoothShare: ", e);
        }
    }

}
