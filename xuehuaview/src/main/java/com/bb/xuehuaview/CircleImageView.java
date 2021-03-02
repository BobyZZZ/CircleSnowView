package com.bb.xuehuaview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class CircleImageView extends androidx.appcompat.widget.AppCompatImageView {
    String TAG = "CircleImageView";

    private int mWidth, mHeight;
    private float mRadius;
    private final Paint mPaint;
    private final PorterDuffXfermode mPorterDuffXfermode;
    private Path mCirclePath;
    private Path mSrcPath;
    private float mCenterX;
    private float mCenterY;

    public CircleImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setXfermode(mPorterDuffXfermode);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        mCenterX = mWidth / 2f;
        mCenterY = mHeight / 2f;
        mRadius = mCenterX;
        //圆路径
        mCirclePath = new Path();
        mCirclePath.addCircle(mRadius, mRadius, mRadius, Path.Direction.CCW);
        //
        mSrcPath = new Path();
        mSrcPath.addRect(mCenterX - mRadius, mCenterY - mRadius, mCenterX + mRadius,mCenterY + mRadius, Path.Direction.CCW);
        mSrcPath.op(mCirclePath, Path.Op.DIFFERENCE);
    }

    /**
     * 方式1
     * 通过裁剪canvas实现：有锯齿，且只有设置src生效，background不生效
     * @param canvas
     */
/*    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        Path path = new Path();
        path.addCircle(mRadius,mRadius,mRadius, Path.Direction.CCW);
        canvas.clipPath(path);
        super.onDraw(canvas);
        canvas.restore();
    }*/

    /**
     * 方式2
     * 通过合成模式
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        int saveLayer = canvas.saveLayer(null, null);

        super.onDraw(canvas);
        canvas.drawPath(mSrcPath, mPaint);

        canvas.restoreToCount(saveLayer);
    }
}
