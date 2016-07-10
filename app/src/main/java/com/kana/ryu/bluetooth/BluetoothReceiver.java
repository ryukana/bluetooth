package com.kana.ryu.bluetooth;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public class BluetoothReceiver extends BroadcastReceiver {
    public BluetoothReceiver() {
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        AppLog.d(intent.getData().getScheme());
        AppLog.d(intent.getData().getHost());
        AppLog.d(intent.getData().getPath());
    }
}
