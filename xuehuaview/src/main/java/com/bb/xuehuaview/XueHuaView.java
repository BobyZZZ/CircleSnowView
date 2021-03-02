package com.bb.xuehuaview;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;

public class XueHuaView extends View {
    String TAG = "XueHuaView";

    private float mCenterX;
    private float mCenterY;
    private float mRadius;//圆半径 = width/2 * mRadiusFraction;
    private float mRadiusFraction = 0;
    private float mMaxOffset = 0;//移动最大距离
    private int mCount = 0;
    private Paint mPaint;
    private ArrayList<XueHua> mXueHuas;
    private Path mPath;
    private Random mRandom = new Random();
    private ValueAnimator mValueAnimator;
    private int mColor;

    public XueHuaView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        initPaint();

/*        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateXueHuaState();
            }
        });*/
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.XueHuaView);
        int indexCount = typedArray.getIndexCount();
        mColor = typedArray.getColor(R.styleable.XueHuaView_color, Color.WHITE);
        mCount = typedArray.getInt(R.styleable.XueHuaView_count, 310);
        mRadiusFraction = typedArray.getFloat(R.styleable.XueHuaView_radiusFraction, 0.6f);
        mMaxOffset = typedArray.getDimensionPixelSize(R.styleable.XueHuaView_maxOffset, 180);
        Log.d(TAG, "initAttrs() called with: mColor = [" + mColor + "]"
                + ",mCount = [" + mCount + "]"
                + ",mRadiusFraction = [" + mRadiusFraction + "]"
                + ",mMaxOffset = [" + mMaxOffset + "]");
        typedArray.recycle();
    }

    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2f;
        mCenterY = h / 2f;
        mRadius = mCenterX * mRadiusFraction;

        //根据圆产生点
        mPath = new Path();
        mPath.addCircle(mCenterX, mCenterY, mRadius, Path.Direction.CCW);
        //路径计算类
        PathMeasure pathMeasure = new PathMeasure(mPath, false);

        float[] pos = new float[2];//坐标
        float[] tan = new float[2];

        mXueHuas = new ArrayList<>();
        for (int i = 0; i < mCount; i++) {
            pathMeasure.getPosTan(pathMeasure.getLength() * i / mCount, pos, tan);
            double atan = Math.acos((pos[0] - mCenterX) / mRadius);
//            atan = Math.atan((pos[1] - mCenterY) / (pos[0] - mCenterX));
            XueHua xueHua = new XueHua(atan, 4, randomSpeed(), randomOffset());
            xueHua.x = pos[0];
            xueHua.y = pos[1];
//            Log.d(TAG, "x: " + pos[0] + ",y: " + pos[1]);
            mXueHuas.add(xueHua);
        }

        startPlay();
    }

    private void startPlay() {
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
        }
        mValueAnimator = ObjectAnimator.ofFloat(0, 1f);
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mValueAnimator.setDuration(500);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                updateXueHuaState();
            }
        });
        mValueAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long startTime = System.currentTimeMillis();
        super.onDraw(canvas);

/*        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(mPath, mPaint);

        mPaint.setStyle(Paint.Style.FILL);*/
        for (XueHua xueHua : mXueHuas) {
            mPaint.setAlpha(xueHua.alpha);
            canvas.drawCircle(xueHua.x, xueHua.y, xueHua.radius, mPaint);
        }
        long endTime = System.currentTimeMillis();
//        Log.d(TAG, "onDraw() startTime: " + startTime + ",endTime: " + endTime + ",castTime: " + (endTime - startTime) + "ms");
    }

    private float randomSpeed() {
        return mRandom.nextInt(2) + 4f;
//        return 2;
    }

    private float randomAlpha() {
        float minAlpha = 40;
        return minAlpha + mRandom.nextInt((int) (255 - minAlpha));
    }

    private float randomOffset() {
        return mRandom.nextFloat() * mMaxOffset;
    }

    private void updateXueHuaState() {
        int countL = 0;
        int countR = 0;
        for (XueHua xueHua : mXueHuas) {
            //更新偏移量
            if (xueHua.offset > mMaxOffset) {
                xueHua.offset = 0;
                xueHua.speed = randomSpeed();
            }
            xueHua.offset += xueHua.speed;

            //根据偏移量计算新的坐标位置
            if (xueHua.x > mCenterX) {
                countR++;
                xueHua.x = (float) ((mRadius + xueHua.offset) * Math.sin(xueHua.angle)) + mCenterX;
            } else {
                countL++;
                xueHua.x = mCenterX - (float) ((mRadius + xueHua.offset) * Math.sin(xueHua.angle));
            }

            if (xueHua.y > mCenterY) {
                xueHua.y = (float) ((mRadius + xueHua.offset) * Math.cos(xueHua.angle)) + mCenterY;
            } else {
                xueHua.y = mCenterY - (float) ((mRadius + xueHua.offset) * Math.cos(xueHua.angle));
            }

            //透明度
            xueHua.alpha = (int) ((1 - Math.min(xueHua.offset / mMaxOffset, 1)) * 255);
        }
//        Log.d(TAG, "updateXueHuaState countL: " + countL + ",countR: " + countR);
        invalidate();
    }

    class XueHua {
        float x;//x = 中心点x坐标 ± （圆半径 + 当前偏移量） * sin(angle)
        float y;//y = 中心点y坐标 ± （圆半径 + 当前偏移量） * cos(angle)
        int alpha;
        float radius;//粒子半径
        float offset;//当前偏移量
        float speed;
        double angle;

        XueHua(double angle, float radius, float speed, float offset) {
            this.angle = angle;
            this.radius = radius;
            this.speed = speed;
            this.offset = offset;
            this.alpha = (int) (this.offset / mMaxOffset * 255);
        }
    }
}
