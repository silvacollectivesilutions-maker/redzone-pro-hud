package com.htown.redzonepro;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;

public class SystemTuner {

    public static boolean hasSecurePermission(Context context) {
        return context.checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasWriteSettings(Context context) {
        return Settings.System.canWrite(context);
    }

    public static String activarModoCompetitivo(Context context) {
        if (!hasSecurePermission(context)) {
            return "FALTA PERMISO: Ejecuta el comando pm grant en Brevent/ADB.";
        }
        ContentResolver cr = context.getContentResolver();
        StringBuilder log = new StringBuilder();

        try {
            // 1. Bloquear 120Hz constantes (evita caídas a 60Hz en PvP)
            Settings.System.putFloat(cr, "min_refresh_rate", 120.0f);
            Settings.System.putFloat(cr, "peak_refresh_rate", 120.0f);
            log.append("✔ 120Hz Fijos: ACTIVO\n");

            // 2. Velocidad de puntero al máximo (-7 a 7)
            Settings.System.putInt(cr, "pointer_speed", 7);
            log.append("✔ Puntero: +7 (MAX)\n");

            // 3. Sensibilidad táctil mejorada de Samsung One UI
            Settings.System.putInt(cr, "auto_adjust_touch", 1);
            log.append("✔ Panel Táctil Samsung: BOOST\n");

            // 4. Retraso de toque sostenido al mínimo (200ms)
            Settings.Secure.putInt(cr, "long_press_timeout", 200);
            log.append("✔ Respuesta Táctil: 200ms\n");

            // 5. Eliminar animaciones del sistema
            Settings.Global.putFloat(cr, Settings.Global.WINDOW_ANIMATION_SCALE, 0.0f);
            Settings.Global.putFloat(cr, Settings.Global.TRANSITION_ANIMATION_SCALE, 0.0f);
            Settings.Global.putFloat(cr, Settings.Global.ANIMATOR_DURATION_SCALE, 0.0f);
            log.append("✔ Animaciones: 0.0x (Cero lag)");

        } catch (Exception e) {
            return "Error parcial: " + e.getMessage();
        }
        return log.toString();
    }

    public static String restaurarNormal(Context context) {
        if (!hasSecurePermission(context)) return "Sin permiso WRITE_SECURE_SETTINGS.";
        ContentResolver cr = context.getContentResolver();
        try {
            Settings.System.putFloat(cr, "min_refresh_rate", 24.0f);
            Settings.System.putFloat(cr, "peak_refresh_rate", 120.0f);
            Settings.System.putInt(cr, "pointer_speed", 0);
            Settings.Secure.putInt(cr, "long_press_timeout", 400);
            Settings.Global.putFloat(cr, Settings.Global.WINDOW_ANIMATION_SCALE, 1.0f);
            Settings.Global.putFloat(cr, Settings.Global.TRANSITION_ANIMATION_SCALE, 1.0f);
            Settings.Global.putFloat(cr, Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f);
            return "Modo Normal restaurado (batería optimizada).";
        } catch (Exception e) {
            return "Error al restaurar: " + e.getMessage();
        }
    }

    public static String leerEstadoReal(Context context) {
        ContentResolver cr = context.getContentResolver();
        float minHz = Settings.System.getFloat(cr, "min_refresh_rate", 60.0f);
        int ptr = Settings.System.getInt(cr, "pointer_speed", 0);
        int touchMs = Settings.Secure.getInt(cr, "long_press_timeout", 400);
        return "Estado Real S24 -> " + (int) minHz + "Hz | Puntero: " + ptr + " | Toque: " + touchMs + "ms";
    }
}
