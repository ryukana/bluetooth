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
        final String command = intent.getData().getPath().replace("¥¥", "");
        AppLog.d(command);
        if (!BluetoothManager.INSTANCE.isConnected()) {
            BluetoothManager.INSTANCE.connect(new BluetoothListener(command));
        } else {
            BluetoothManager.INSTANCE.write(command);
        }
    }
    private static class BluetoothListener implements BluetoothManager.IBluetoothListener {
        private final String command;
        public BluetoothListener(final String command) {
            this.command = command;
        }
        @Override
        public void onErrorOccurred(final String message) {
        }
        @Override
        public void onConnectStarted() {
        }
        @Override
        public void onConnectSuccess() {
            BluetoothReceiver.executeEachCommand(command);
        }
        @Override
        public void onProgressMessage(String message) {
            AppLog.d(message);
        }
        @Override
        public void onMessageFromDevice(String message) {
        }
        @Override
        public void onDisconnected() {
        }
    }
    private static void executeEachCommand(final String command){
        AppLog.d(command);
        BluetoothManager.INSTANCE.write(command);
    }
}
