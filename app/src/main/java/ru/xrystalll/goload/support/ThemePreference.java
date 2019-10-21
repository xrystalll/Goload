package ru.xrystalll.goload.support;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class ThemePreference {

    private final SharedPreferences sharedPref;

    public ThemePreference(@NonNull Context context) {
        sharedPref = context.getSharedPreferences("SharedSettings", Context.MODE_PRIVATE);
    }

    public void setThemeModeState(Boolean state) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("NightMode", state);
        editor.apply();
    }

    public Boolean loadNightModeState() {
        return sharedPref.getBoolean("NightMode", false);
    }

}
