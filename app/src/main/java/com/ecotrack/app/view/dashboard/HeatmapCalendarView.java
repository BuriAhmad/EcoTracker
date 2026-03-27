package com.ecotrack.app.view.dashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.saturn.R;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom View rendering a 7-column × 5-row grid of colored rounded squares
 * showing daily activity intensity (GitHub-style heatmap).
 *
 * Color mapping:
 * - 0 activities → bg_card
 * - 1–2 → 30% accent_green
 * - 3–5 → 60% accent_green
 * - 6+  → 100% accent_green
 */
public class HeatmapCalendarView extends View {

    private static final int COLS = 7;   // Mon–Sun
    private static final int ROWS = 5;   // 5 weeks

    private Paint[] levelPaints;
    private Paint labelPaint;

    private float cellSize;
    private float cellGap;
    private float labelHeight;

    // Data: date → activity count
    private Map<LocalDate, Integer> data = new HashMap<>();

    // Day labels
    private static final String[] DAY_LABELS = {"M", "T", "W", "T", "F", "S", "S"};

    public HeatmapCalendarView(Context context) {
        super(context);
        init();
    }

    public HeatmapCalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HeatmapCalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        cellSize = getResources().getDimension(R.dimen.heatmap_cell_size);
        cellGap = getResources().getDimension(R.dimen.heatmap_cell_gap);

        // 4 paint levels: none, low, medium, high
        levelPaints = new Paint[4];

        levelPaints[0] = makePaint(ContextCompat.getColor(getContext(), R.color.bg_card));
        levelPaints[1] = makePaint(0x4DA3F77B); // ~30% green
        levelPaints[2] = makePaint(0x99A3F77B); // ~60% green
        levelPaints[3] = makePaint(ContextCompat.getColor(getContext(), R.color.accent_green)); // 100%

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_muted));
        labelPaint.setTextSize(spToPx(10));
        try {
            labelPaint.setTypeface(ResourcesCompat.getFont(getContext(), R.font.plus_jakarta_sans_regular));
        } catch (Exception ignored) { }

        labelHeight = spToPx(14);
    }

    private Paint makePaint(int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        return p;
    }

    /**
     * Set heatmap data and redraw.
     */
    public void setData(Map<LocalDate, Integer> dailyCounts) {
        this.data = dailyCounts != null ? dailyCounts : new HashMap<>();
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float totalCellWidth = cellSize + cellGap;
        float gridWidth = COLS * totalCellWidth - cellGap;
        float startX = (getWidth() - gridWidth) / 2f;
        float startY = labelHeight;

        float cornerRadius = dpToPx(4);

        // Draw day labels
        for (int col = 0; col < COLS; col++) {
            float cx = startX + col * totalCellWidth + cellSize / 2f;
            canvas.drawText(DAY_LABELS[col], cx, labelHeight - dpToPx(4), labelPaint);
        }

        // Compute the starting Monday: go back (ROWS - 1) weeks from the current week's Monday
        LocalDate today = LocalDate.now();
        LocalDate thisMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate startMonday = thisMonday.minusWeeks(ROWS - 1);

        // Draw cells
        RectF rect = new RectF();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                LocalDate date = startMonday.plusDays((long) row * 7 + col);

                // Don't draw future dates
                if (date.isAfter(today)) continue;

                int count = data.getOrDefault(date, 0);
                int level = countToLevel(count);

                float left = startX + col * totalCellWidth;
                float top = startY + row * totalCellWidth;
                rect.set(left, top, left + cellSize, top + cellSize);
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, levelPaints[level]);
            }
        }
    }

    private int countToLevel(int count) {
        if (count <= 0) return 0;
        if (count <= 2) return 1;
        if (count <= 5) return 2;
        return 3;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float totalCellWidth = cellSize + cellGap;
        int width = (int) (COLS * totalCellWidth - cellGap + getPaddingLeft() + getPaddingRight());
        int height = (int) (labelHeight + ROWS * totalCellWidth - cellGap
                + getPaddingTop() + getPaddingBottom());

        setMeasuredDimension(
                resolveSize(width, widthMeasureSpec),
                resolveSize(height, heightMeasureSpec));
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}
