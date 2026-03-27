package com.ecotrack.app.view.home;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.saturn.R;

/**
 * Custom View that draws an animated circular progress ring for the Eco-Score.
 *
 * - Background track: 10% white ring
 * - Foreground arc: accent_green, animated sweep angle
 * - Center text: score number (Syne Bold, 44sp) + "/ 100" (Jakarta, 12sp, muted)
 */
public class EcoScoreRingView extends View {

    // Paints
    private Paint trackPaint;
    private Paint arcPaint;
    private Paint scorePaint;
    private Paint labelPaint;

    // Drawing area
    private RectF arcRect;

    // State
    private int targetScore = 0;
    private float currentSweep = 0f; // animated sweep angle (0–360)

    // Dimensions
    private float strokeWidth;

    public EcoScoreRingView(Context context) {
        super(context);
        init();
    }

    public EcoScoreRingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EcoScoreRingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        strokeWidth = getResources().getDimension(R.dimen.eco_score_ring_stroke);

        // Background track — 10% white
        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidth);
        trackPaint.setColor(0x1AFFFFFF); // 10% white
        trackPaint.setStrokeCap(Paint.Cap.ROUND);

        // Foreground arc — accent_green
        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(strokeWidth);
        arcPaint.setColor(ContextCompat.getColor(getContext(), R.color.accent_green));
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        // Score number — Syne Bold, 44sp equivalent
        scorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scorePaint.setTextAlign(Paint.Align.CENTER);
        scorePaint.setColor(ContextCompat.getColor(getContext(), R.color.accent_green));
        scorePaint.setTextSize(spToPx(44));
        try {
            scorePaint.setTypeface(ResourcesCompat.getFont(getContext(), R.font.syne_bold));
        } catch (Exception ignored) { }

        // "/ 100" label — Jakarta Regular, 12sp, muted
        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_muted));
        labelPaint.setTextSize(spToPx(12));
        try {
            labelPaint.setTypeface(ResourcesCompat.getFont(getContext(), R.font.plus_jakarta_sans_regular));
        } catch (Exception ignored) { }

        arcRect = new RectF();
    }

    /**
     * Set the score and animate the ring from current position to the target.
     */
    public void setScore(int score) {
        this.targetScore = Math.max(0, Math.min(score, 100));
        float targetSweep = (targetScore / 100f) * 360f;

        ValueAnimator animator = ValueAnimator.ofFloat(currentSweep, targetSweep);
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            currentSweep = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float halfStroke = strokeWidth / 2f;
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(cx, cy) - halfStroke;

        // Update arc rect
        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius);

        // Draw background track (full circle)
        canvas.drawArc(arcRect, 0, 360, false, trackPaint);

        // Draw foreground arc (starting from 12 o'clock = -90°)
        if (currentSweep > 0) {
            canvas.drawArc(arcRect, -90, currentSweep, false, arcPaint);
        }

        // Draw score number (slightly above center)
        int displayScore = Math.round((currentSweep / 360f) * 100f);
        String scoreText = String.valueOf(displayScore);
        float scoreY = cy + scorePaint.getTextSize() / 3f - spToPx(6);
        canvas.drawText(scoreText, cx, scoreY, scorePaint);

        // Draw "/ 100" below the score
        float labelY = scoreY + spToPx(18);
        canvas.drawText("/ 100", cx, labelY, labelPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = (int) getResources().getDimension(R.dimen.eco_score_ring_size);
        int measuredWidth = resolveSize(size, widthMeasureSpec);
        int measuredHeight = resolveSize(size, heightMeasureSpec);
        int finalSize = Math.min(measuredWidth, measuredHeight);
        setMeasuredDimension(finalSize, finalSize);
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}
