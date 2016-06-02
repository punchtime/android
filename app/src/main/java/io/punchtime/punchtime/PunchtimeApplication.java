package io.punchtime.punchtime;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by elias on 6/05/16.
 * for project: Punchtime
 */
public class PunchtimeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        Firebase.setAndroidContext(this);
    }
}
