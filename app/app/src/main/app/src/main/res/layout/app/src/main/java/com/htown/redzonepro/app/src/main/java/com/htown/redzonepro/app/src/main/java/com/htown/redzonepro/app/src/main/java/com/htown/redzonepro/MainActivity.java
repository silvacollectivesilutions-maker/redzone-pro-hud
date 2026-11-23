package com.htown.redzonepro;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    private boolean crosshairActive = false;
    private TextView txtStatus, txtTelemetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtStatus = findViewById(R.id.txtStatus);
        txtTelemetry = findViewById(R.id.txtTelemetry);
        Button btnBoost = findViewById(R.id.btnBoost);
        Button btnRestore = findViewById(R.id.btnRestore);
        Button btnCrosshair = findViewById(R.id.btnCrosshair);
        DragLabView dragLab = findViewById(R.id.dragLab);

        actualizarEstado();

        btnBoost.setOnClickListener(v -> {
            if (!SystemTuner.hasWriteSettings(this)) {
                startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getPackageName())));
                return;
            }
            String res = SystemTuner.activarModoCompetitivo(this);
            txtStatus.setText(res + "\n\n" + SystemTuner.leerEstadoReal(this));
        });

        btnRestore.setOnClickListener(v -> {
            String res = SystemTuner.restaurarNormal(this);
            txtStatus.setText(res + "\n\n" + SystemTuner.leerEstadoReal(this));
        });

        btnCrosshair.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())));
                return;
            }
            Intent svc = new Intent(this, CrosshairService.class);
            if (!crosshairActive) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(svc);
                else startService(svc);
                crosshairActive = true;
            } else {
                stopService(svc);
                crosshairActive = false;
            }
        });

        dragLab.setOnTelemetryListener(report -> txtTelemetry.setText(report));
    }

    private void actualizarEstado() {
        String perm = SystemTuner.hasSecurePermission(this)
                ? "PERMISO SECURE: CONCEDIDO ✔"
                : "PERMISO SECURE: PENDIENTE (usar Brevent/ADB)";
        txtStatus.setText(perm + "\n" + SystemTuner.leerEstadoReal(this));
    }
}
