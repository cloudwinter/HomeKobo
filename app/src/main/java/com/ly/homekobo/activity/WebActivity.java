package com.ly.homekobo.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


import com.ly.homekobo.R;
import com.ly.homekobo.base.BaseActivity;
import com.ly.homekobo.util.Prefer;
import com.ly.homekobo.view.TranslucentActionBar;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class WebActivity extends BaseActivity implements TranslucentActionBar.ActionBarClickListener {

    @BindView(R.id.actionbar)
    TranslucentActionBar actionBar;
    @BindView(R.id.web)
    WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        ButterKnife.bind(this);
        actionBar.setData(getString(R.string.privacy_policy),R.mipmap.ic_back,null, 0, null, this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            actionBar.setStatusBarHeight(getStatusBarHeight());
        }
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        WebSettings webSettings = webView.getSettings();
        //webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//把html中的内容放大webview等宽的一列中
        webSettings.setJavaScriptEnabled(true);//支持js
        webSettings.setSupportZoom(true); // 可以缩放
        webSettings.setBuiltInZoomControls(true); // 显示放大缩小

        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);


    }

    @Override
    protected void onResume() {
        super.onResume();
        actionBar.setTitle(getString(R.string.privacy_policy));
        String url = "";
        String language = Prefer.getInstance().getSelectedLanguage();
        if ("en".equals(language)) {
            url = "file:///android_res/mipmap/protocol_en.jpeg";
        } else {
            url = "file:///android_res/mipmap/protocol_fr.jpegg";
        }
        webView.loadUrl(url);
    }

    @Override
    public void onLeftClick() {
        finish();
    }

    @Override
    public void onRightClick() {

    }

}
