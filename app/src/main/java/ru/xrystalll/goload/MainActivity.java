package ru.xrystalll.goload;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

import ru.xrystalll.goload.support.LocaleUtils;
import ru.xrystalll.goload.support.ThemePreference;

public class MainActivity extends AppCompatActivity {

    private Fragment fragment;
    private FragmentManager fragmentManager;

    private boolean getPermission(String permission) {
        String stringBuilder = "android.permission." + permission;
        return checkCallingOrSelfPermission(stringBuilder) != PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemePreference themePref = new ThemePreference(getBaseContext());
        setTheme(themePref.loadNightModeState() ? R.style.LightTheme : R.style.AppTheme);

        super.onCreate(savedInstanceState);

        LocaleUtils localeUtils = new LocaleUtils(getBaseContext());
        String localeState = localeUtils.getLocaleString();
        String lang = localeState != null ? localeState : "en";

        Locale locale = new Locale(lang);
        setLocale(locale);

        if (getPermission("WRITE_EXTERNAL_STORAGE")) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);

        fragmentManager = getSupportFragmentManager();
        fragment = new HomeFragment();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.main_container, fragment).commit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_home:
                            setTitle(R.string.app_name);
                            fragment = new HomeFragment();
                            break;
                        case R.id.action_upload:
                            setTitle(R.string.text_upload);
                            fragment = new UploadFragment();
                            break;
                        case R.id.action_settings:
                            setTitle(R.string.text_settings);
                            fragment = new SettingsFragment();
                            break;
                    }
                    final FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.main_container, fragment).commit();
                    return true;
                }
            });
    }

    private void setLocale(Locale locale) {
        Locale.setDefault(locale);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            Intent i = new Intent(this, SearchActivity.class);
            startActivity(i);
        }
        return true;
    }

}
