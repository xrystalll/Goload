package ru.xrystalll.goload;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ReceivingActivity extends AppCompatActivity {

    private static final String URL_DATA = "https://goload.ru/api/upload.php?from=ru.xrystalll.goload&action=shared";
    private String userName, jsonResponse, filePath;
    private ImageView sharedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiving);

        SharedPreferences sharedPref = getSharedPreferences("SharedSettings", Context.MODE_PRIVATE);
        String user = sharedPref.getString("UserName", "");
        if (!user.equalsIgnoreCase("")) {
            userName = user;
        } else {
            userName = "Anonim";
        }

        sharedImage = findViewById(R.id.sharedImage);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null && hasNetwork()) {
            if (type.startsWith("image/")) {
                handleSendImage(intent);
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
            sharedImage.setImageURI(imageUri);
            filePath = getRealPathFromURI(this, imageUri);
            File sourceFile = new File(filePath);
            if (sourceFile.isFile()) {
                new UploadFile().execute();
            } else {
                Toast.makeText(this, "It's not a file: " + filePath, Toast.LENGTH_LONG).show();
            }
        }
    }

    private String getRealPathFromURI(@NonNull Context context, Uri contentUri) {
        String result = null;
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri,  projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
            cursor.close();
        }
        return result;
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
            conn.setRequestProperty("username", userName);

            dos = new DataOutputStream(conn.getOutputStream());


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

    @SuppressLint("StaticFieldLeak")
    class UploadFile extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Uploading…", Toast.LENGTH_LONG).show();
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

    private boolean hasNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

}
