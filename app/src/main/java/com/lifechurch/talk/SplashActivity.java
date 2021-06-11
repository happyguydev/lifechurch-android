package com.lifechurch.talk;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import org.chat21.android.ui.login.activities.ChatSplashActivity;

import static org.chat21.android.utils.DebugConstants.DEBUG_LOGIN;

/**
 * Created by stefanodp91 on 21/12/17.
 */

public class SplashActivity extends ChatSplashActivity {

    @Override
    protected Class<?> getTargetClass() {
        Log.d(DEBUG_LOGIN, "SplashActivity.getTargetClass");
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);

        }
        return TabActivity.class;
    }

//    @Override
//    protected Intent getLoginIntent() {
//        return new Intent(this, ChatLoginActivity.class);
//    }
}