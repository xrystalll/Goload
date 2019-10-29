package ru.xrystalll.goload.support;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class SettingsUtils {

    private final SharedPreferences sharedPref;

    public SettingsUtils(@NonNull Context context) {
        sharedPref = context.getSharedPreferences("SharedSettings", Context.MODE_PRIVATE);
    }

    public void setThemeState(Boolean state) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("NightMode", state);
        editor.apply();
    }

    public Boolean loadThemeState() {
        return sharedPref.getBoolean("NightMode", false);
    }


    public void setLocaleString(String lang) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("AppLocale", lang);
        editor.apply();
    }

    public String getLocaleString() {
        return sharedPref.getString("AppLocale", "");
    }

}
