package net.crevion.uploadfile;

import android.app.Application;

/**
 * Created by yusuf on 6/9/17.
 */

public class UploadApps extends Application {

    private static UploadApps INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }

    public static UploadApps getInstance() {
        return INSTANCE;
    }
}
