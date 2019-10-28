package ru.xrystalll.goload;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import ru.xrystalll.goload.filepicker.MaterialFilePicker;
import ru.xrystalll.goload.filepicker.ui.FilePickerActivity;

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

import static android.app.Activity.RESULT_OK;

public class UploadFragment extends Fragment {

    private TextView fileName;
    private EditText userName;
    private String filePath;
    private View loader;
    private static final int FILE_REQUEST = 1;
    private static final String URL_DATA = "https://goload.ru/api/upload.php?from=ru.xrystalll.goload";
    private String jsonResponse;
    private SharedPreferences sharedPref;
    private String storageValue = "10";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        Button btnUpload = view.findViewById(R.id.upload);
        LinearLayout fileInput = view.findViewById(R.id.fileInput);
        fileName = view.findViewById(R.id.fileName);
        userName = view.findViewById(R.id.userName);
        loader = view.findViewById(R.id.uploadLoader);

        sharedPref = getActivity().getSharedPreferences("SharedSettings", Context.MODE_PRIVATE);
        String user = sharedPref.getString("UserName", "");
        if (!user.equalsIgnoreCase("")) {
            userName.setText(user);
        }

        fileInput.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                displayFileChooser();
            }
        });

        RadioGroup radioGroup = view.findViewById(R.id.storage_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.storage_1:
                        storageValue = "1";
                        break;
                    case R.id.storage_10:
                        storageValue = "10";
                        break;
                    case R.id.storage_180:
                        storageValue = "";
                        break;
                }
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (userName.length() == 0) {
                    Toast.makeText(getActivity(), R.string.enter_name_error, Toast.LENGTH_SHORT).show();
                } else if (filePath == null) {
                    Toast.makeText(getActivity(), R.string.choose_file_error, Toast.LENGTH_SHORT).show();
                } else {
                    if (hasNetwork()) {
                        new UploadFile().execute();
                    } else {
                        Toast.makeText(getActivity(), R.string.check_connection_error, Toast.LENGTH_SHORT).show();
                    }
                    setUsername(userName.getText().toString().trim());
                }
            }
        });

        return view;
    }

    private void setUsername(String name) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("UserName", name);
        editor.apply();
    }

    private void displayFileChooser() {
        new MaterialFilePicker()
            .withSupportFragment(this)
            .withRequestCode(FILE_REQUEST)
            .withHiddenFiles(true)
            .start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_REQUEST && resultCode == RESULT_OK) {
            filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            if (filePath != null) {
                String nameFile = new File(filePath).getName();
                fileName.setText(nameFile);
            }
        }
    }

    private void uploadFile() {
        String sourceFileUri = filePath;
        HttpURLConnection conn;
        DataOutputStream dos;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        String user = userName.getText().toString().trim();
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {
            Toast.makeText(getActivity(), "File not exist:" + sourceFileUri, Toast.LENGTH_SHORT).show();
        } else {
            try {
                FileInputStream fileInputStream = new FileInputStream(sourceFileUri);
                URL url = new URL(URL_DATA);

                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("filename", sourceFileUri);
                conn.setRequestProperty("username", user);
                conn.setRequestProperty("del", storageValue);

                dos = new DataOutputStream(conn.getOutputStream());


                // Storage
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"del\";" + lineEnd);
                dos.writeBytes(lineEnd);

                dos.write(storageValue.getBytes());
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


                // Filename
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"username\";" + lineEnd);
                dos.writeBytes(lineEnd);

                dos.write(user.getBytes());
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


                // File
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"filename\";" +
                        "filename=\"" + sourceFileUri + "\"" + lineEnd);
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

    }

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

    private void showLoader() {
        loader.setVisibility(View.VISIBLE);
    }

    private void hideLoader() {
        loader.setVisibility(View.GONE);
    }

    @SuppressLint("StaticFieldLeak")
    class UploadFile extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoader();
        }

        @Override
        protected Void doInBackground(Void... args) {
            uploadFile();
            return null;
        }

        @Override
        protected  void onPostExecute(Void result) {
            super.onPostExecute(result);
            hideLoader();
            if (!jsonResponse.equals("error")) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    JSONObject o = jsonObject.getJSONObject("data");

                    String id = o.getString("id");

                    Intent i = new Intent(getActivity(), FileActivity.class);
                    i.putExtra("id", id);
                    startActivity(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean hasNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();

    }

}