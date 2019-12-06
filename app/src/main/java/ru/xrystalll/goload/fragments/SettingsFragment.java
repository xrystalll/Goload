package ru.xrystalll.goload.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.net.Uri;
import android.widget.Toast;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.xrystalll.goload.MainActivity;
import ru.xrystalll.goload.LoginActivity;
import ru.xrystalll.goload.R;
import ru.xrystalll.goload.WebViewActivity;
import ru.xrystalll.goload.support.SettingsUtils;

public class SettingsFragment extends PreferenceFragmentCompat {

    private final String BASE_API_URL = "https://goload.ru";
    private SettingsUtils settingsUtils;
    private SharedPreferences sharedPref;
    private String sharedUserId, user, userPhoto;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = getActivity().getSharedPreferences("SharedSettings", Context.MODE_PRIVATE);

        final Preference clearCache = findPreference("clear_cache");
        assert clearCache != null;
        clearCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                deleteCache(getActivity());
                clearCache.setEnabled(false);
                Toast.makeText(getActivity(), getString(R.string.alert_cache), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        Preference openRules = findPreference("open_rules_webview");
        assert openRules != null;
        openRules.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openTab(BASE_API_URL + "/rules");
                return true;
            }
        });

        Preference openLicences = findPreference("open_licences_webview");
        assert openLicences != null;
        openLicences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(getActivity(), WebViewActivity.class);
                i.putExtra("url", "file:///android_asset/licenses.html");
                i.putExtra("title", getString(R.string.licenses_title));
                startActivity(i);
                return true;
            }
        });

        Preference openLogin = findPreference("log_in");
        assert openLogin != null;
        openLogin.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                return true;
            }
        });

        sharedUserId = sharedPref.getString("UserId", "");
        if (!sharedUserId.equalsIgnoreCase("")) {
            user = sharedPref.getString("UserName", "");
            userPhoto = sharedPref.getString("UserPhoto", "");
            openLogin.setEnabled(false);
            openLogin.setTitle(user);
            openLogin.setSummary("Online");
            openLogin.setIconSpaceReserved(true);
            openLogin.setIcon(getResources().getDrawable(R.drawable.ic_user_grey_24dp));
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        settingsUtils = new SettingsUtils(getActivity());

        setPreferencesFromResource(R.xml.settings_preference, rootKey);

        PreferenceManager.getDefaultSharedPreferences(
                getActivity()).registerOnSharedPreferenceChangeListener(
        new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("switch_theme")) {
                    boolean theme = sharedPreferences.getBoolean(key, false);
                    if (theme) {
                        settingsUtils.setThemeState(false);
                        restart();
                    } else {
                        settingsUtils.setThemeState(true);
                        restart();
                    }
                }

                if (key.equals("lang")) {
                    String value = sharedPreferences.getString(key, "lang");
                    if (value.equals("ru")) {
                        settingsUtils.setLocaleString("ru");
                        restart();
                    } else {
                        settingsUtils.setLocaleString("en");
                        restart();
                    }
                }
            }
        });
    }

    private void restart() {
        try {
            Intent i = new Intent(getActivity(), MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            assert children != null;
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    private void openTab(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        builder.addDefaultShareMenuItem();
        builder.setShowTitle(true);
        builder.setToolbarColor(getResources().getColor(R.color.colorAccent));
        customTabsIntent.intent.setPackage("com.android.chrome");
        customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
    }

    @Override
    public void onResume() {
        super.onResume();

        Preference openLogin = findPreference("log_in");
        sharedUserId = sharedPref.getString("UserId", "");
        if (!sharedUserId.equalsIgnoreCase("")) {
            user = sharedPref.getString("UserName", "");
            userPhoto = sharedPref.getString("UserPhoto", "");
            openLogin.setEnabled(false);
            openLogin.setTitle(user);
            openLogin.setSummary("Online");
            openLogin.setIconSpaceReserved(true);
            openLogin.setIcon(getResources().getDrawable(R.drawable.ic_user_grey_24dp));
        }
    }

}
