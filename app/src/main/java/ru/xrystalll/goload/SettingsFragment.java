package ru.xrystalll.goload;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.util.Objects;

import ru.xrystalll.goload.support.ThemePreference;
import ru.xrystalll.goload.support.LocaleUtils;

public class SettingsFragment extends PreferenceFragmentCompat {

    private ThemePreference themePref;
    private LocaleUtils localeUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Preference clearCache = findPreference("clear_cache");
        clearCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                deleteCache(getActivity());
                Toast.makeText(getActivity(), "Cache is cleared", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        Preference openRules = findPreference("open_rules_webview");
        openRules.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(getActivity(), WebViewActivity.class);
                i.putExtra("url", "https://goload.ru/rules");
                i.putExtra("title", getString(R.string.rules_title));
                startActivity(i);
                return true;
            }
        });

        Preference openLicences = findPreference("open_licences_webview");
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
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getActivity().setTitle(R.string.text_settings);

        themePref = new ThemePreference(Objects.requireNonNull(getContext()));
        localeUtils = new LocaleUtils(Objects.requireNonNull(getContext()));

        setPreferencesFromResource(R.xml.settings_preference, rootKey);

        PreferenceManager.getDefaultSharedPreferences(
                getActivity()).registerOnSharedPreferenceChangeListener(
        new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("switch_theme")) {
                    boolean theme = sharedPreferences.getBoolean(key, false);
                    if (theme) {
                        themePref.setThemeModeState(false);
                        restart();
                    } else {
                        themePref.setThemeModeState(true);
                        restart();
                    }
                }

                if (key.equals("lang")) {
                    String value = sharedPreferences.getString(key, "lang");
                    if (value.equals("ru")) {
                        localeUtils.setLocaleString("ru");
                        restart();
                    } else {
                        localeUtils.setLocaleString("en");
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

}