package com.ly.homekobo.fragment;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.ly.homekobo.MyApplication;
import com.ly.homekobo.R;
import com.ly.homekobo.RunningContext;
import com.ly.homekobo.base.BaseFragment;
import com.ly.homekobo.bean.DeviceBean;
import com.ly.homekobo.blue.BluetoothLeService;
import com.ly.homekobo.util.BlueUtils;
import com.ly.homekobo.util.LogUtils;
import com.ly.homekobo.util.Prefer;
import com.ly.homekobo.util.ToastUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class KuaijieBaseFragment extends BaseFragment {

    public static final String TAG = "KuaijieBaseFragment";

    /**
     * 记忆询问码code
     */
    protected final static long MGS_ASK_STATUS_CODE = 7;

    /**
     * 默认间隔
     */
    protected final static long DEFAULT_INTERVAL = 2000;

    // 蓝牙设备名称
    protected String blueDeviceName;


    // 特征值
    protected BluetoothGattCharacteristic characteristic;

    /**
     * 蓝牙回传数据
     *
     * @param data
     */
    abstract void handleReceiveData(String data);

    /**
     * 发起记忆询问码
     */
    abstract void askStatus();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().registerReceiver(mKuaijieReceiver, makeGattUpdateIntentFilter());
        characteristic = MyApplication.getInstance().gattCharacteristic;
        DeviceBean deviceBean = Prefer.getInstance().getConnectedDevice();
        if (deviceBean != null) {
            blueDeviceName = deviceBean.getTitle();
            LogUtils.e(TAG, "blueDeviceName名称：" + blueDeviceName);
        }
        RunningContext.threadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(300L);
                    askStatus();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mKuaijieReceiver);
        super.onDestroy();
    }


    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MGS_ASK_STATUS_CODE) {
                String cmd = (String) msg.obj;
                sendBlueCmd(cmd);
            }
        }
    };


    /**
     * 发送记忆询问码的命令
     *
     * @param cmd
     */
    protected void sendAskBlueCmd(final String cmd) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                sendBlueCmd(cmd);
            }
        });
    }

    /**
     * 发送蓝牙命令
     *
     * @param cmd
     */
    protected void sendBlueCmd(String cmd) {
        cmd = cmd.replace(" ", "");
        Log.i(TAG, "sendBlueCmd: " + cmd);
        // 判断蓝牙是否连接
        if (!BlueUtils.isConnected()) {
            ToastUtils.showToast(getContext(), getString(R.string.device_no_connected));
            LogUtils.i(TAG, "sendBlueCmd -> 蓝牙未连接");
            return;
        }
        if (characteristic == null) {
            characteristic = MyApplication.getInstance().gattCharacteristic;
        }
        if (characteristic == null) {
            LogUtils.i(TAG, "sendBlueCmd -> 特征值未获取到");
            return;
        }
        characteristic.setValue(BlueUtils.StringToBytes(cmd));
        MyApplication.getInstance().mBluetoothLeService.writeCharacteristic(characteristic);
    }


    /**
     * 广播接收器，负责接收BluetoothLeService类发送的数据
     */
    private final BroadcastReceiver mKuaijieReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) { //发现GATT服务器
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //处理发送过来的数据  (//有效数据)
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    String data = bundle.getString(BluetoothLeService.EXTRA_DATA);
                    if (data != null) {
                        LogUtils.e("==快捷  接收设备返回的数据==", data);
                        handleReceiveData(data);
                    }
                }
            }
        }
    };


    /* 意图过滤器 */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

}
