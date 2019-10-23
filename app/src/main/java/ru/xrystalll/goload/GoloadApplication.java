package ru.xrystalll.goload;

import android.app.Activity;
import android.app.Application;

public class GoloadApplication extends Application {

    private Activity currentActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public  Activity getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(Activity currentActivity) {
        this.currentActivity = currentActivity;
    }
}
