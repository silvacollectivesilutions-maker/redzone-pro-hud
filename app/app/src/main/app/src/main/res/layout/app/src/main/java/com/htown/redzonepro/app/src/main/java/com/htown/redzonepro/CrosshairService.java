package com.htown.redzonepro;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public class CrosshairService extends Service {
    private WindowManager windowManager;
    private View crosshairView;
    private static final String CHANNEL_ID = "redzone_hud_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundNotification();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        crosshairView = new View(this) {
            final Paint paintCore = new Paint(Paint.ANTI_ALIAS_FLAG);
            final Paint paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);

            {
                paintCore.setColor(Color.parseColor("#FF4500")); // Naranja neón
                paintCore.setStyle(Paint.Style.FILL);

                paintBorder.setColor(Color.parseColor("#56B4FF")); // Azul cielo
                paintBorder.setStyle(Paint.Style.STROKE);
                paintBorder.setStrokeWidth(2.5f);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                float cx = getWidth() / 2f;
                float cy = getHeight() / 2f;
                canvas.drawCircle(cx, cy, 4.5f, paintCore);
                canvas.drawCircle(cx, cy, 11f, paintBorder);
            }
        };

        int sizePx = (int) (32 * getResources().getDisplayMetrics().density);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                sizePx,
                sizePx,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER;
        windowManager.addView(crosshairView, params);
    }

    private void startForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "RedZone HUD", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
            Notification notif = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("RedZone Pro HUD Activo")
                    .setContentText("Mira táctica ejecutándose en 120Hz")
                    .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                    .build();
            startForeground(101, notif);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (crosshairView != null && windowManager != null) {
            windowManager.removeView(crosshairView);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
