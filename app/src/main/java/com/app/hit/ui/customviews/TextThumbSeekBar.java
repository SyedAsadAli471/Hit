package com.app.hit.ui.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.content.res.ResourcesCompat;

import com.app.hit.R;


public class TextThumbSeekBar extends AppCompatSeekBar {
    private int mThumbSize;
    private TextPaint mTextPaintDollar;
    private TextPaint mTextPaintNumber;

    public TextThumbSeekBar(Context context) {
        this(context, null);
    }

    public TextThumbSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.seekBarStyle);
    }

    public TextThumbSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mThumbSize = getResources().getDimensionPixelSize(R.dimen._76udp);

        mTextPaintDollar = new TextPaint();
        mTextPaintDollar.setColor(Color.WHITE);
        mTextPaintDollar.setTextSize(getResources().getDimensionPixelSize(R.dimen._14usp));
//        mTextPaintDollar.setTypeface(ResourcesCompat.getFont(context, R.font.proxima_nova_regular));
        mTextPaintDollar.setTextAlign(Paint.Align.CENTER);

        mTextPaintNumber = new TextPaint();
        mTextPaintNumber.setColor(getResources().getColor(R.color.primary));
        mTextPaintNumber.setTextSize(getResources().getDimensionPixelSize(R.dimen._14usp));
        mTextPaintNumber.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaintNumber.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        String dollarText = "";
        Rect boundsDollar = new Rect();
        mTextPaintDollar.getTextBounds(dollarText, 0, dollarText.length(), boundsDollar);

        String progressText = String.valueOf(getProgress());
        Rect boundsNumber = new Rect();
        mTextPaintNumber.getTextBounds(progressText, 0, progressText.length(), boundsNumber);

        int leftPadding = getPaddingLeft() - getThumbOffset();
        int rightPadding = getPaddingRight() - getThumbOffset();
        int width = getWidth() - leftPadding - rightPadding;
        float progressRatio = (float) getProgress() / getMax();
        float thumbOffset = mThumbSize * (.5f - progressRatio);
        float thumbX = progressRatio * width + leftPadding + thumbOffset;
        float thumbXDollar = thumbX - boundsNumber.width() / 2f;
        float thumbXNumber = thumbX + boundsDollar.width() / 2f;
        float thumbY = getHeight() / 2f + boundsNumber.height() / 2f;

        canvas.drawText(dollarText, thumbXDollar, thumbY, mTextPaintDollar);
        canvas.drawText(progressText, thumbXNumber, thumbY, mTextPaintNumber);
    }

}