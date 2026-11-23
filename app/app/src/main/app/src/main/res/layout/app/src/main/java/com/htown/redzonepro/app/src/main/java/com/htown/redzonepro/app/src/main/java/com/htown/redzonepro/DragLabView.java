package com.htown.redzonepro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

public class DragLabView extends View {
    public interface OnTelemetryListener {
        void onResult(String report);
    }

    private final Paint pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint guidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path dragPath = new Path();
    private VelocityTracker velocityTracker;
    private float startX, startY, peakVy;
    private OnTelemetryListener listener;

    public DragLabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        pathPaint.setColor(Color.parseColor("#FF5500"));
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(10f);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);

        guidePaint.setColor(Color.parseColor("#2A3F5F"));
        guidePaint.setStyle(Paint.Style.STROKE);
        guidePaint.setStrokeWidth(3f);
    }

    public void setOnTelemetryListener(OnTelemetryListener l) {
        this.listener = l;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        canvas.drawLine(cx, 0, cx, getHeight(), guidePaint);
        canvas.drawCircle(cx, getHeight() * 0.78f, 65f, guidePaint);
        canvas.drawPath(dragPath, pathPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dragPath.reset();
                startX = event.getX();
                startY = event.getY();
                peakVy = 0;
                dragPath.moveTo(startX, startY);
                if (velocityTracker == null) velocityTracker = VelocityTracker.obtain();
                else velocityTracker.clear();
                velocityTracker.addMovement(event);
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                dragPath.lineTo(event.getX(), event.getY());
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000);
                float vy = -velocityTracker.getYVelocity();
                if (vy > peakVy) peakVy = vy;
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                float dx = event.getX() - startX;
                float dy = startY - event.getY();
                double angle = Math.toDegrees(Math.atan2(dx, Math.max(1, dy)));
                analizarArrastre(peakVy, angle, dy);
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void analizarArrastre(float vy, double angle, float distY) {
        if (listener == null) return;
        String diagnostico;
        if (distY < 80) {
            diagnostico = "Arrastre muy corto -> Sube General a 196-200.";
        } else if (vy < 1800) {
            diagnostico = "Subida lenta (se queda en pecho) -> Sube General +6 puntos (192-198).";
        } else if (vy > 4800) {
            diagnostico = "Subida explosiva (pasa la cabeza) -> Baja General a 182-188.";
        } else {
            diagnostico = "ZONA TODO ROJO PERFECTA -> Mantén General entre 188-194.";
        }

        String desviacion = Math.abs(angle) > 14
                ? String.format("Desvío lateral: %.1f° (endereza el pulgar)", angle)
                : String.format("Alineación limpia: %.1f°", angle);

        listener.onResult(String.format("Velocidad: %.0f px/s\n%s\n%s", vy, desviacion, diagnostico));
    }
}
