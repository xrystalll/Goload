package ru.xrystalll.goload;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ru.xrystalll.goload.support.SettingsUtils;

public class LoginActivity extends AppCompatActivity {

    private final String BASE_API_URL = "https://goload.ru";
    private SharedPreferences sharedPref;

    private EditText loginInput, passwordInput;
    private View loader;
    private String login, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SettingsUtils settingsUtils = new SettingsUtils(getBaseContext());
        setTheme(settingsUtils.loadThemeState() ? R.style.LightTheme : R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sharedPref = getSharedPreferences("SharedSettings", Context.MODE_PRIVATE);
        loginInput = findViewById(R.id.login_input);
        passwordInput = findViewById(R.id.password_input);
        Button btn = findViewById(R.id.auth);
        TextView registration = findViewById(R.id.reg);
        loader = findViewById(R.id.authLoader);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loginInput.getText().toString().trim().length() < 1) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_login_error), Toast.LENGTH_SHORT).show();
                } else if (passwordInput.getText().toString().trim().length() < 1) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_password_error), Toast.LENGTH_SHORT).show();
                } else {
                    if (hasNetwork()) { 
                        showLoader();
                        login = loginInput.getText().toString().trim();
                        password = passwordInput.getText().toString().trim();
                        auth(login, password);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.check_connection_error), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                builder.setShowTitle(true);
                builder.setToolbarColor(getResources().getColor(R.color.colorAccent));
                customTabsIntent.intent.setPackage("com.android.chrome");
                customTabsIntent.launchUrl(LoginActivity.this, Uri.parse(BASE_API_URL + "/sign_up"));
            }
        });
    }

    private void auth(final String login, final String password) {
        String URL_DATA = BASE_API_URL + "/api/auth.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_DATA, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (!s.contains("error")) {
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        JSONObject o = jsonObject.getJSONObject("user");

                        setUser(o.getString("id"), o.getString("login"), o.getString("password"), o.getString("userPhoto"));
                        hideLoader();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();

                    } catch (JSONException e) {
                        hideLoader();
                        e.printStackTrace();
                    }
                } else {
                    hideLoader();
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                hideLoader();
                Toast.makeText(getApplicationContext(), volleyError.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("login", login);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.getCache().clear();
        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);
    }

    private void setUser(String id, String login, String password, String userPhoto) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("UserId", id);
        editor.putString("UserName", login);
        editor.putString("UserPassword", password);
        editor.putString("UserPhoto", userPhoto);
        editor.apply();
    }

    private void showLoader() {
        loader.setVisibility(View.VISIBLE);
    }

    private void hideLoader() {
        loader.setVisibility(View.GONE);
    }

    private boolean hasNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

}
