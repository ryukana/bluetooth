package com.kana.ryu.bluetooth;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
public enum BluetoothManager {
    INSTANCE;
    private static final String DIALOG_TAG = "DIALOG_TAG";
    private static final String DEVICE_NAME = "RNBT-";
    // なんだこれ？よくわからんけどBluetoothのプロトコルごとに決まっているらしい。多くのサイトでこういう記載だったので、真似しておく。
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int VIEW_STATUS = 0;
    private static final int VIEW_INPUT = 1;
    private static final int VIEW_CONNECTING = 2;
    private static final int VIEW_CONNECTED = 3;
    private static final int VIEW_DISCONNECTED = 4;
    private static final int VIEW_ERROR = 99;
    private boolean isStarted;
    private BluetoothTask mBluetoothTask;
    public void connect(IBluetoothListener listener) {
        if (isStarted) {
            AppLog.d("connect is already finished");
            listener.onConnectSuccess();
            return;
        }
        listener.onConnectStarted();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        listener.onProgressMessage("device searching");
        if (adapter == null) {
            listener.onErrorOccurred("this device not support bluetooth");
            return;
        }
        Set<BluetoothDevice> devices = adapter.getBondedDevices();
        BluetoothDevice targetBluetoothDevice = null;
        for (BluetoothDevice tmpDevice : devices) {
            AppLog.d("found device is :" + tmpDevice.getName());
            if (tmpDevice.getName().contains(DEVICE_NAME)) {
                AppLog.d("this is target device");
                listener.onProgressMessage("target device found: " + tmpDevice.getName());
                targetBluetoothDevice = tmpDevice;
            }
        }
        if (targetBluetoothDevice == null) {
            listener.onErrorOccurred("target device is not found");
            return;
        }
        try {
            mBluetoothTask = new BluetoothTask(new CallBackHandler(listener), targetBluetoothDevice);
            new Thread(mBluetoothTask).start();
        } catch (IOException e) {
            AppLog.e(e);
            listener.onErrorOccurred("unknown error is occurred");
        }
    }
    public void connect(FragmentActivity activity, IBluetoothListener listener) {
        if (isStarted) {
            AppLog.d("connect is already finished");
            listener.onConnectSuccess();
            return;
        }
        listener.onConnectStarted();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        listener.onProgressMessage("device searching");
        if (adapter == null) {
            listener.onErrorOccurred("this device not support bluetooth");
            return;
        }
        Set<BluetoothDevice> devices = adapter.getBondedDevices();
        if (devices.size() == 0) {
            listener.onErrorOccurred("target device is not found");
            return;
        }
        AppLog.d("devices.size():" + devices.size());
        FragmentManager manager = activity.getSupportFragmentManager();
        ChooseBluetoothDeviceDialogFragment chooseBluetoothDeviceDialog =
                new ChooseBluetoothDeviceDialogFragment(devices, listener);
        chooseBluetoothDeviceDialog.show(manager, DIALOG_TAG);
    }
    private class ChooseBluetoothDeviceDialogFragment extends DialogFragment {
        final Set<BluetoothDevice> devices;
        final IBluetoothListener listener;
        public ChooseBluetoothDeviceDialogFragment(Set<BluetoothDevice> devices,
                                                   IBluetoothListener listener) {
            this.devices = devices;
            this.listener = listener;
        }
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final ArrayList<BluetoothDevice> devices = new ArrayList<>();
            Iterator<BluetoothDevice> iterator = this.devices.iterator();
            while (iterator.hasNext()) {
                BluetoothDevice tmpDevice = (BluetoothDevice) iterator.next();
                devices.add(tmpDevice);
            }
            final ArrayList<String> bluetoothDeviceName = new ArrayList<>();
            for (BluetoothDevice bluetoothDevice : devices.toArray(new BluetoothDevice[devices.size()])) {
                bluetoothDeviceName.add(bluetoothDevice.getName());
            }
            builder.setTitle("choose device").setItems(bluetoothDeviceName.toArray(
                            new String[bluetoothDeviceName.size()]),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                mBluetoothTask = new BluetoothTask(new CallBackHandler(listener), devices.get(which));
                                new Thread(mBluetoothTask).start();
                            } catch (IOException e) {
                                AppLog.e(e);
                                listener.onErrorOccurred("unknown error is occurred");
                            }
                        }
                    });
            return builder.create();
        }
    }
    public boolean isConnected() {
        return isStarted;
    }
    public void write(String message) {
        if (!isStarted) {
            AppLog.e("please call this method after connect");
            return;
        }
        mBluetoothTask.write(message);
    }
    public void disconnect() {
        if (!isStarted) {
            AppLog.e("please call this method after connect");
            return;
        }
        mBluetoothTask.disconnect();
        mBluetoothTask = null;
    }
    private class BluetoothTask implements Runnable {
        private final Handler mHandler;
        private final BluetoothDevice mBluetoothDevice;
        private InputStream mInputStream;
        private OutputStream mOutputStream;
        private BluetoothSocket mBluetoothSocket;
        private boolean isCalledDisconnect;
        public BluetoothTask(Handler handler, BluetoothDevice bluetoothDevice) throws IOException {
            Message valueMsg = new Message();
            valueMsg.what = VIEW_CONNECTING;
            handler.sendMessage(valueMsg);
            mHandler = handler;
            mBluetoothDevice = bluetoothDevice;
        }
        private void write(String message) {
            final Message valueMsg = new Message();
            try {
                mOutputStream.write(message.getBytes());
                valueMsg.what = VIEW_STATUS;
                valueMsg.obj = "Write:" + message;
            } catch (IOException e) {
                AppLog.e(e);
                valueMsg.what = VIEW_ERROR;
                valueMsg.obj = "Error:" + e;
            }
            mHandler.sendMessage(valueMsg);
        }
        private void disconnect() {
            final Message valueMsg = new Message();
            isCalledDisconnect = true;
            try {
                mBluetoothSocket.close();
                valueMsg.what = VIEW_DISCONNECTED;
            } catch (IOException e) {
                AppLog.e(e);
                valueMsg.what = VIEW_ERROR;
                valueMsg.obj = "Error:" + e;
            }
            isStarted = false;
            mHandler.sendMessage(valueMsg);
        }
        @Override
        public void run() {
            try {
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
                mBluetoothSocket.connect();
                mInputStream = mBluetoothSocket.getInputStream();
                mOutputStream = mBluetoothSocket.getOutputStream();
                // InputStreamのバッファを格納
                byte[] buffer = new byte[1024];
                // 取得したバッファのサイズを格納
                int bytes;
                Message valueMsg = new Message();
                valueMsg.what = VIEW_CONNECTED;
                mHandler.sendMessage(valueMsg);
                isStarted = true;
                while (true) {
                    // InputStreamの読み込み
                    bytes = mInputStream.read(buffer);
                    AppLog.i("bytes=" + bytes);
                    // String型に変換
                    String readMsg = new String(buffer, 0, bytes);
                    // null以外なら表示
                    if (readMsg.trim() != null && !readMsg.trim().equals("")) {
                        AppLog.i("value=" + readMsg.trim());
                        valueMsg = new Message();
                        valueMsg.what = VIEW_INPUT;
                        valueMsg.obj = readMsg;
                        mHandler.sendMessage(valueMsg);
                    }
                }
            } catch (Exception e) {
                isStarted = false;
                Message valueMsg = new Message();
                if (isCalledDisconnect) {
                    valueMsg.what = VIEW_STATUS;
                    valueMsg.obj = "disconnected";
                } else {
                    AppLog.e(e);
                    valueMsg.what = VIEW_ERROR;
                    valueMsg.obj = e.getMessage();
                    try {
                        mBluetoothSocket.close();
                    } catch (Exception ee) {
                        AppLog.e(ee);
                    }
                }
                mHandler.sendMessage(valueMsg);
            }
        }
    }
    private class CallBackHandler extends Handler {
        private final IBluetoothListener mListener;
        public CallBackHandler(IBluetoothListener listener) {
            mListener = listener;
        }
        @Override
        public void handleMessage(Message msg) {
            int action = msg.what;
            String msgStr = (String) msg.obj;
            switch (action) {
                case VIEW_CONNECTING:
                    mListener.onProgressMessage("connecting");
                    break;
                case VIEW_CONNECTED:
                    mListener.onConnectSuccess();
                    mListener.onProgressMessage("connected");
                    break;
                case VIEW_STATUS:
                    mListener.onProgressMessage(msgStr);
                    break;
                case VIEW_INPUT:
                    mListener.onMessageFromDevice(msgStr);
                    break;
                case VIEW_DISCONNECTED:
                    mListener.onDisconnected();
                    mListener.onProgressMessage("disconnected");
                    break;
                case VIEW_ERROR:
                    mListener.onErrorOccurred(msgStr);
                    mListener.onProgressMessage(msgStr);
                    break;
                default:
                    AppLog.e("unexpected message action");
            }
        }
    }
    public interface IBluetoothListener {
        void onErrorOccurred(String message);
        void onConnectStarted();
        void onConnectSuccess();
        void onProgressMessage(String message);
        void onMessageFromDevice(String message);
        void onDisconnected();
    }
}
