package com.android.videoplayer.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import com.android.videoplayer.R;

/**
 * created by hty
 * 2022/7/10
 * Desc:字体加粗
 */
public class BoldTextView extends AppCompatTextView {

    private boolean mTextMarquee;
    private float mStrokeWidth=0.6f;

    public BoldTextView(Context context) {
        this(context,null);
    }

    public BoldTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BoldTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if(null!=attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BoldTextView);
            mStrokeWidth = typedArray.getFloat(R.styleable.BoldTextView_boldStorkeWidth,mStrokeWidth);
            mTextMarquee = typedArray.getBoolean(R.styleable.BoldTextView_boldMarquee,false);
            typedArray.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //获取当前控件的画笔
        TextPaint paint = getPaint();
        //设置画笔的描边宽度值
        paint.setStrokeWidth(mStrokeWidth);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        super.onDraw(canvas);
    }

    /**
     * 设置描边宽度
     * @param strokeWidth 从0.0起
     */
    public void setStrokeWidth(float strokeWidth){
        this.mStrokeWidth=strokeWidth;
        invalidate();
    }

    @Override
    public boolean isFocused() {
        return mTextMarquee;
    }
}