package com.reactnct;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        Bundle payload = intent.getExtras();
        if (payload.containsKey("pt_id") && (payload.getString("pt_id").equals("pt_rating") || payload
                .getString("pt_id").equals("pt_product_display"))) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(payload.getInt("notificationId"));
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
    }


}

