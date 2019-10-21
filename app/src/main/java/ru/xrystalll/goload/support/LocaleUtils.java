package ru.xrystalll.goload.support;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class LocaleUtils {

    private final SharedPreferences sharedPref;

    public LocaleUtils(@NonNull Context context) {
        sharedPref = context.getSharedPreferences("SharedSettings", Context.MODE_PRIVATE);
    }

    public void setLocaleString(String state) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("AppLocale", state);
        editor.apply();
    }

    public String getLocaleString() {
        return sharedPref.getString("AppLocale", "");
    }

}
