package ru.xrystalll.goload;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import ru.xrystalll.goload.support.SettingsUtils;

public class ReceivingActivity extends AppCompatActivity {

    private static final String URL_DATA = "https://goload.ru/api/upload.php?from=ru.xrystalll.goload&action=shared";
    private String userName, jsonResponse, filePath;
    private String userId = "0";
    private ProgressBar sharedUploading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SettingsUtils settingsUtils = new SettingsUtils(getBaseContext());
        setTheme(settingsUtils.loadThemeState() ? R.style.LightTheme : R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiving);

        SharedPreferences sharedPref = getSharedPreferences("SharedSettings", Context.MODE_PRIVATE);
        String user = sharedPref.getString("UserName", "");
        if (!user.equalsIgnoreCase("")) {
            userName = user;
        } else {
            userName = "Anonim";
        }
        String sharedUserId = sharedPref.getString("UserId", "");
        if (!user.equalsIgnoreCase("")) {
            userId = sharedUserId;
        }

        sharedUploading = findViewById(R.id.sharedUploading);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null && hasNetwork()) {
            if (type.startsWith("image/")) {
                if (getPermission()) {
                    handleSendImage(intent);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }
        } else {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    private void handleSendImage(@NonNull Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            filePath = saveImgUri(this, imageUri);
            try {
                File sourceFile = new File(filePath);
                if (sourceFile.isFile()) {
                    new UploadFile().execute();
                } else {
                    sharedUploading.setVisibility(View.GONE);
                    Toast.makeText(this, "It's not a file", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } catch (Exception e) {
                sharedUploading.setVisibility(View.GONE);
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void uploadFile() {
        HttpURLConnection conn;
        DataOutputStream dos;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;

        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            URL url = new URL(URL_DATA);

            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("filename", filePath);
            conn.setRequestProperty("userid", userId);
            conn.setRequestProperty("username", userName);

            dos = new DataOutputStream(conn.getOutputStream());


            // UserId
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"userid\";" + lineEnd);
            dos.writeBytes(lineEnd);

            dos.write(userId.getBytes());
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


            // Username
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"username\";" + lineEnd);
            dos.writeBytes(lineEnd);

            dos.write(userName.getBytes());
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


            // File
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"filename\";" +
                    "filename=\"" + filePath + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            int serverResponseCode = conn.getResponseCode();

            if (serverResponseCode == 200) {
                InputStream response = conn.getInputStream();
                jsonResponse = convertStreamToString(response);
            }

            fileInputStream.close();
            dos.flush();
            dos.close();
        } catch (MalformedURLException ex) {
            ex.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class UploadFile extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... args) {
            uploadFile();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!jsonResponse.contains("error")) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    JSONObject o = jsonObject.getJSONObject("data");

                    String id = o.getString("id");

                    Intent i = new Intent(ReceivingActivity.this, FileActivity.class);
                    i.putExtra("id", id);
                    startActivity(i);
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @NonNull
    private String saveImgUri(@NonNull Context context, Uri imgUri) {
        final int chunkSize = 1024;
        byte[] buffer = new byte[4 * chunkSize];

        File imgFile = new File(context.getExternalFilesDir(null), "sharedImage.png");

        FileOutputStream fos = null;
        try {
            InputStream in = context.getContentResolver().openInputStream(imgUri);
            fos = new FileOutputStream(imgFile);
            int bytesRead;
            assert in != null;
            while ((bytesRead = in.read(buffer)) > 0) {
                fos.write(Arrays.copyOfRange(buffer, 0, Math.max(0, bytesRead)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return context.getExternalFilesDir(null) + "/sharedImage.png";
    }

    @NonNull
    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private boolean hasNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private boolean getPermission() {
        String permission = "android.permission.WRITE_EXTERNAL_STORAGE";
        return checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

}
