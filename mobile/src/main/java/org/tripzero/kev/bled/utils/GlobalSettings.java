package org.tripzero.kev.bled.utils;


import android.app.Application;

import com.parse.Parse;

/**
 * Created by ammonrees on 6/21/14.
 */
public class GlobalSettings extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // do the ACRA init here

        Parse.enableLocalDatastore(this);
        Parse.initialize(getApplicationContext(), "MMR4HpqGKKM9pQ9YiPG9vmMJqQohhN7mI9MUQvOP", "eizOSFWDUXe0TJCnaxRV5dDfiyZ3qCoyokGLrFCg");
    }
}

