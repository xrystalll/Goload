package ru.xrystalll.goload;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import ru.xrystalll.goload.support.SettingsUtils;

public class WebViewActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SettingsUtils settingsUtils = new SettingsUtils(getBaseContext());
        setTheme(settingsUtils.loadThemeState() ? R.style.LightTheme : R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        String BASE_API_URL = "https://goload.ru";
        String url;
        String title;
        if (getIntent().hasExtra("url") && getIntent().hasExtra("title")) {
            url = getIntent().getStringExtra("url");
            title = getIntent().getStringExtra("title");
        } else {
            url = BASE_API_URL;
            title = getString(R.string.app_name);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(title);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        WebView webview = findViewById(R.id.webview);
        RelativeLayout connError = findViewById(R.id.connError);

        webview.setWebViewClient(new WebViewClient());

        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        if (hasNetwork()) {
            webview.loadUrl(url);
        } else {
            assert url != null;
            if (url.contains("https://") || url.contains("http://")) {
                webview.setVisibility(View.GONE);
                connError.setVisibility(View.VISIBLE);
            } else if (url.contains("file:///")) {
                webview.loadUrl(url);
            }
        }
    }

    private boolean hasNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

}
