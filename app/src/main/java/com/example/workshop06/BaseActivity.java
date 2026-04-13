package com.example.workshop06;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;

    protected void onRefresh() {}

    protected long getRefreshIntervalMs() {
        return 30_000L;
    }

    protected void startAutoRefresh() {
        stopAutoRefresh();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                onRefresh();
                refreshHandler.postDelayed(this, getRefreshIntervalMs());
            }
        };
        refreshHandler.postDelayed(refreshRunnable, getRefreshIntervalMs());
    }

    protected void stopAutoRefresh() {
        if (refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
            refreshRunnable = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAutoRefresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoRefresh();
    }

    protected void showKeyboard(android.view.View view) {
        if (view == null) return;
        view.requestFocus();
        view.post(() -> {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }
}