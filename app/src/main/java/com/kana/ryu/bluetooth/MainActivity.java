package com.kana.ryu.bluetooth;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
public class MainActivity extends AppCompatActivity {
    @BindView(R.id.status_text)
    TextView mStatusTextView;
    @BindView(R.id.connect_fab)
    FloatingActionButton mConnectFAB;
    @BindView(R.id.disable_filter)
    FrameLayout mDisableFilter;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }
    @OnClick(R.id.connect_fab)
    public void onClickConnectFAB(View view) {
        BluetoothManager.INSTANCE.connect(new BluetoothListenerImpl(this));
    }
    @OnCheckedChanged(R.id.power_toggle_button)
    public void onPowerButton(boolean checked) {
        if (checked) {
            BluetoothManager.INSTANCE.write("9");
        } else {
            BluetoothManager.INSTANCE.write("0");
        }
    }
    @OnCheckedChanged(R.id.flash_toggle_button)
    public void onFlashButton(boolean checked) {
        if (checked) {
            BluetoothManager.INSTANCE.write("5");
        } else {
            BluetoothManager.INSTANCE.write("9");
        }
    }
    private void setConnectedViewState() {
        mDisableFilter.setVisibility(View.GONE);
        mConnectFAB.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.disable_color));
        mConnectFAB.setEnabled(false);
    }
    private void setDisconnectedViewState() {
        mDisableFilter.setVisibility(View.VISIBLE);
        mConnectFAB.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.enable_color));
        mConnectFAB.setEnabled(true);
    }
    private static class BluetoothListenerImpl implements BluetoothManager.IBluetoothListener {
        final WeakReference<MainActivity> mActivity;
        public BluetoothListenerImpl(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }
        @Override
        public void onConnectStarted() {
            MainActivity activity = mActivity.get();
            if (activity == null || activity.isFinishing()) {
                AppLog.e("activity == null || activity.isFinishing()");
            }
            activity.mConnectFAB.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.disable_color));
            activity.mConnectFAB.setEnabled(false);
            activity.progressDialog = new ProgressDialog(activity);
            activity.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            activity.progressDialog.setMessage("接続しています");
            activity.progressDialog.setCancelable(false);
            activity.progressDialog.show();
        }
        @Override
        public void onErrorOccurred(String message) {
            AppLog.d(message);
            MainActivity activity = mActivity.get();
            if (activity == null || activity.isFinishing()) {
                AppLog.e("activity == null || activity.isFinishing()");
            }
            ProgressDialog progressDialog = activity.progressDialog;
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            new AlertDialog.Builder(activity)
                    .setMessage("エラーが発生しました。")
                    .setPositiveButton("OK", null)
                    .create()
                    .show();
            activity.setDisconnectedViewState();
        }
        @Override
        public void onConnectSuccess() {
            MainActivity activity = mActivity.get();
            if (activity == null || activity.isFinishing()) {
                AppLog.e("activity == null || activity.isFinishing()");
            }
            ProgressDialog progressDialog = activity.progressDialog;
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            activity.setConnectedViewState();
        }
        @Override
        public void onProgressMessage(String message) {
            AppLog.d(message);
            MainActivity activity = mActivity.get();
            if (activity == null || activity.isFinishing()) {
                AppLog.e("activity == null || activity.isFinishing()");
            }
            activity.mStatusTextView.setText(message);
        }
        @Override
        public void onMessageFromDevice(String message) {
            MainActivity activity = mActivity.get();
            if (activity == null || activity.isFinishing()) {
                AppLog.e("activity == null || activity.isFinishing()");
            }
            AppLog.d(message);
        }
        @Override
        public void onDisconnected() {
            MainActivity activity = mActivity.get();
            if (activity == null || activity.isFinishing()) {
                AppLog.e("activity == null || activity.isFinishing()");
            }
            new AlertDialog.Builder(activity)
                    .setMessage("切断しました")
                    .setPositiveButton("OK", null)
                    .create()
                    .show();
            activity.setDisconnectedViewState();
        }
    }
}
