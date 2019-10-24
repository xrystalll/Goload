package ru.xrystalll.goload;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class WebViewActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        String url;
        String title;
        if (getIntent().hasExtra("url") && getIntent().hasExtra("title")) {
            url = getIntent().getStringExtra("url");
            title = getIntent().getStringExtra("title");
        } else {
            url = "https://goload.ru";
            title = "";
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(title);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        WebView webview = findViewById(R.id.webview);

        webview.setWebViewClient(new WebViewClient());

        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        webview.loadUrl(url);
    }
}
