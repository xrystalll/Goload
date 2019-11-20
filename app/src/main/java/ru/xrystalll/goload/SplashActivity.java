package ru.xrystalll.goload;

import androidx.appcompat.app.AppCompatActivity;

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
            Intent i = new Intent(this, WebViewActivity.class);
            i.putExtra("url", data.toString());
            i.putExtra("title", getString(R.string.file_title));
            startActivity(i);
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
            Intent i = new Intent(this, WebViewActivity.class);
            i.putExtra("url", data.toString());
            i.putExtra("title", getString(R.string.rules_title));
            startActivity(i);
            finish();
        } else if (data != null && data.toString().contains("api")) {
            Intent i = new Intent(this, WebViewActivity.class);
            i.putExtra("url", BASE_API_URL + "/api");
            i.putExtra("title", "API");
            startActivity(i);
            finish();
        } else if (data != null && data.toString().contains("report")) {
            List<String> params = data.getPathSegments();
            String id = params.get(params.size() - 1).replace("report", "");
            String fileId = !id.isEmpty() ? id : null;
            if (fileId != null) {
                Intent i = new Intent(this, WebViewActivity.class);
                i.putExtra("url", BASE_API_URL + "/report" + fileId);
                i.putExtra("title", "Report");
                startActivity(i);
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

}
