package com.ly.homekobo.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ly.homekobo.R;
import com.ly.homekobo.adapter.TabPagerAdapter;
import com.ly.homekobo.base.BaseActivity;
import com.ly.homekobo.base.BaseFragment;
import com.ly.homekobo.bean.DeviceBean;
import com.ly.homekobo.blue.BluetoothLeService;
import com.ly.homekobo.fragment.AnmoFragment;
import com.ly.homekobo.fragment.DengguangFragment;
import com.ly.homekobo.fragment.KuaijieK1Fragment;
import com.ly.homekobo.fragment.KuaijieK2Fragment;
import com.ly.homekobo.fragment.WeitiaoW1Fragment;
import com.ly.homekobo.fragment.WeitiaoW2Fragment;
import com.ly.homekobo.fragment.WeitiaoW4Fragment;
import com.ly.homekobo.util.LogUtils;
import com.ly.homekobo.util.Prefer;
import com.ly.homekobo.util.ToastUtils;
import com.ly.homekobo.view.NoScrollViewPager;
import com.ly.homekobo.view.TranslucentActionBar;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends BaseActivity implements View.OnClickListener, TranslucentActionBar.ActionBarClickListener {

    public static final String TAG = "HomeActivity";

    // 设置tab个数
    private final static int tabCount = 4;

    @BindView(R.id.actionbar)
    TranslucentActionBar actionBar;

    @BindView(R.id.ll_content)
    RelativeLayout relativeLayout;

    @BindView(R.id.vp_home)
    NoScrollViewPager viewPager;

    @BindView(R.id.tab1)
    LinearLayout tab1;
    @BindView(R.id.tab1_text)
    TextView tab1TextView;

    @BindView(R.id.tab2)
    LinearLayout tab2;
    @BindView(R.id.tab2_text)
    TextView tab2TextView;


    @BindView(R.id.tab3)
    LinearLayout tab3;
    @BindView(R.id.tab3_text)
    TextView tab3TextView;


    @BindView(R.id.tab4)
    LinearLayout tab4;
    @BindView(R.id.tab4_text)
    TextView tab4TextView;

    List<TextView> tabTextViews;

    List<BaseFragment> fragments;
    TabPagerAdapter tabPagerAdapter;

    String blueName;

    @Override
    public void onLeftClick() {
        finish();
    }

    @Override
    public void onRightClick() {
        Intent intent = new Intent(HomeActivity.this, SettingActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        DeviceBean deviceBean = Prefer.getInstance().getConnectedDevice();
        if (deviceBean != null) {
            blueName = deviceBean.getTitle();
        }
        actionBar.setData(blueName, R.mipmap.ic_back, null, R.mipmap.ic_set, null, this);
        actionBar.setStatusBarHeight(getStatusBarHeight());
        LogUtils.e(TAG, "当前连接的蓝牙名称为：" + blueName);
        initView();
        setCurrentTab(1);

        // 启动蓝牙service
        Intent blueServiceIntent = new Intent(HomeActivity.this, BluetoothLeService.class);
        startService(blueServiceIntent);
        bindService(blueServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void initView() {
        tab1.setOnClickListener(this);
        tab2.setOnClickListener(this);
        tab3.setOnClickListener(this);
        tab4.setOnClickListener(this);

        tabTextViews = new ArrayList<>();
        tabTextViews.add(tab1TextView);
        tabTextViews.add(tab2TextView);
        tabTextViews.add(tab3TextView);
        tabTextViews.add(tab4TextView);

        fragments = new ArrayList<>();
        setFragments();
        tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(tabPagerAdapter);
        viewPager.setScroll(true);
        viewPager.setOffscreenPageLimit(3);
    }


    private void setFragments() {
        if (TextUtils.isEmpty(blueName)) {
            fragments.add(new KuaijieK1Fragment());
            fragments.add(new WeitiaoW1Fragment());
        } else if (blueName.contains("QMS-IQ") || blueName.contains("QMS-I06")
                || blueName.contains("QMS-LQ") || blueName.contains("QMS-L04")) {
            fragments.add(new KuaijieK1Fragment());
            fragments.add(new WeitiaoW1Fragment());
        } else if (blueName.contains("QMS-JQ-D") || blueName.contains("QMS4")) {
            fragments.add(new KuaijieK2Fragment());
            fragments.add(new WeitiaoW2Fragment());
        } else if (blueName.contains("QMS-MQ") || blueName.contains("QMS2")) {
            fragments.add(new KuaijieK2Fragment());
            fragments.add(new WeitiaoW4Fragment());
        } else {
            fragments.add(new KuaijieK1Fragment());
            fragments.add(new WeitiaoW1Fragment());
        }

        fragments.add(new AnmoFragment());
        fragments.add(new DengguangFragment());
    }

    private void setCurrentTab(int tabIndex) {
        if (tabCount == 4 && tabIndex == 4) {
            tabIndex = 4;
        }
        int position = tabIndex - 1;
        for (int i = 0; i < tabCount; i++) {
            if (position == i) {
                tabTextViews.get(i).setSelected(true);
            } else {
                tabTextViews.get(i).setSelected(false);
            }
        }
        viewPager.setCurrentItem(position, false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tab1:
                setCurrentTab(1);
                break;
            case R.id.tab2:
                setCurrentTab(2);
                break;
            case R.id.tab3:
                setCurrentTab(3);
                break;
            case R.id.tab4:
                setCurrentTab(4);
                break;
            default:
                break;
        }
    }

    /* 意图过滤器 */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }


    /**
     * 广播接收器，负责接收BluetoothLeService类发送的数据
     */
    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            LogUtils.i(TAG, "监听到蓝牙状态 ：" + action);
            if (action == BluetoothLeService.ACTION_GATT_DISCONNECTED) {
                // 监听到蓝牙已断开
                LogUtils.e(TAG, "监听到蓝牙状态 ：已断开");
                Prefer.getInstance().setBleStatus("未连接", null);
                ToastUtils.showToast(HomeActivity.this, R.string.device_disconnect);
            }
        }
    };


    /* BluetoothLeService绑定的回调函数 */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            LogUtils.d(TAG, "BluetoothLeService 已绑定 HomeActivity");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtils.i(TAG, "BluetoothLeService 已断开");
        }
    };

}
