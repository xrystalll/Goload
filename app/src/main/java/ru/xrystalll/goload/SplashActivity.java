package ru.xrystalll.goload;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import android.content.Intent;
import android.os.Bundle;
import android.net.Uri;

import java.util.List;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Uri data = intent.getData();
        String BASE_API_URL = "https://goload.ru";

        if (data != null && data.toString().contains("search")) {
            startActivity(new Intent(this, SearchActivity.class));
            finish();
        } else if (data != null && data.toString().contains("files/goload")) {
            openTab(data.toString());
            finish();
        } else if (data != null && data.toString().contains("file")) {
            List<String> params = data.getPathSegments();
            String id = params.get(params.size() - 1).replace("file", "");
            String fileId = !id.isEmpty() ? id : null;
            if (fileId != null) {
                Intent i = new Intent(this, FileActivity.class);
                i.putExtra("id", fileId);
                startActivity(i);
                finish();
            } else {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        } else if (data != null && data.toString().contains("rules")) {
            openTab(data.toString());
            finish();
        } else if (data != null && data.toString().contains("api")) {
            openTab(BASE_API_URL + "/api");
            finish();
        } else if (data != null && data.toString().contains("report")) {
            List<String> params = data.getPathSegments();
            String id = params.get(params.size() - 1).replace("report", "");
            String fileId = !id.isEmpty() ? id : null;
            if (fileId != null) {
                openTab(BASE_API_URL + "/report" + fileId);
                finish();
            } else {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void openTab(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        builder.addDefaultShareMenuItem();
        builder.setShowTitle(true);
        builder.setToolbarColor(getResources().getColor(R.color.colorAccent));
        customTabsIntent.intent.setPackage("com.android.chrome");
        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

}
