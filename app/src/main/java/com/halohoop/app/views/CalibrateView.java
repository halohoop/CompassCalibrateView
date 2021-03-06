package com.halohoop.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by halohoop on 16-9-24.
 */
public class CalibrateView extends View {

    private int mMeasuredWidth;
    private int mMeasuredHeight;
    private PointF mMiddlePointF;
    private Paint mPaint;
    private float mPlateBottomRadius;
    private float mPlateBottomOffsetFromMiddle;
    private float mPlateMiddleRadius;
    private float mPlateMiddleOffsetFromMiddle;
    private float mPlateTopRadius;
    private float mPlateTopOffsetFromMiddle;
    private float mDefaultFULLReliableValue;
    private float mDefaultHalfReliableValue;
    private List<AngleState> mAngleStates = new ArrayList<>();
    private float mAngle;
    private int mBallRadius;
    private int ballOffsetFromMiddlePoint;
    private boolean mIsCalibrateStart;
    private ExecutorService mExecutorService;

    public float getDefaultFULLReliableValue() {
        return mDefaultFULLReliableValue;
    }

    public float getDefaultHalfReliableValue() {
        return mDefaultHalfReliableValue;
    }

    public CalibrateView(Context context) {
        this(context, null);
    }

    public CalibrateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalibrateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(12, 5);
    }

    private void init(int plateStepCount, int stepsInPlateStepCount) {
        int everyStepCount = stepsInPlateStepCount * plateStepCount;
        float everyStepAngle = 360f / everyStepCount;
        //init every angle state to be uncalibrated
        for (int i = 0; i < everyStepCount; i++) {
            if (i == 0) {
                mAngleStates.add(new AngleState(State.NONE, 0));
                continue;
            }
            mAngleStates.add(new AngleState(State.NONE,
                    mAngleStates.get(mAngleStates.size() - 1).mAngle + everyStepAngle));
        }
        mAngle = 0;
        mBallRadius = 15;
        ballOffsetFromMiddlePoint = 270;
        mDefaultFULLReliableValue = 80;
        mDefaultHalfReliableValue = 85;
        mPlateBottomRadius = 300;
        mPlateBottomOffsetFromMiddle = 280;
        mPlateMiddleRadius = 285;
        mPlateMiddleOffsetFromMiddle = 280;
        mPlateTopRadius = 300;
        mPlateTopOffsetFromMiddle = 280;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(Color.WHITE);

        mExecutorService = Executors.newFixedThreadPool(1);
    }

    private Runnable mTask = new Runnable() {
        @Override
        public void run() {
            int mFullCount = 0;
            for (int i = 0; i < mAngleStates.size(); i++) {
                AngleState angleState = mAngleStates.get(i);
                if (angleState.mState == State.FULL) {
                    mFullCount++;
                }
            }
            if (mFullCount == mAngleStates.size()) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        if (mOnFinishCallback != null) {
                            mOnFinishCallback.onFinish();
                        }
                    }
                });
                mExecutorService.shutdown();
            }
        }
    };


    public void setOnFinishCallback(OnFinishCallback onFinishCallback) {
        this.mOnFinishCallback = onFinishCallback;
    }

    private OnFinishCallback mOnFinishCallback;

    public static interface OnFinishCallback {
        void onFinish();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMeasuredWidth = getMeasuredWidth();
        mMeasuredHeight = getMeasuredHeight();
        if (mMiddlePointF == null) {
            mMiddlePointF = new PointF(mMeasuredWidth / 2, mMeasuredHeight / 2);
        } else {
            mMiddlePointF.x = mMeasuredWidth / 2;
            mMiddlePointF.y = mMeasuredHeight / 2;
        }
    }

    public void setReliableValue(float reliableValue, float angle) {
        this.mAngle = angle;
        int index = Math.round(angle);
        int tmpIndex = index;
        while (tmpIndex % 6 != 0) {
            tmpIndex++;
            if (tmpIndex >= mAngleStates.size()) break;
        }
        index = tmpIndex / 6;
        if (index >= mAngleStates.size()) {
            return;
        }
        AngleState angleState = mAngleStates.get(index);
        if (angleState.mState != State.FULL) {
            if (reliableValue <= mDefaultHalfReliableValue
                    && reliableValue > mDefaultFULLReliableValue) {
                angleState.mState = State.HALF;
            } else if (reliableValue <= mDefaultFULLReliableValue) {
                angleState.mState = State.FULL;
            } else {
                angleState.mState = State.NONE;
            }
        } else {
            //eat it, because already in full state
        }
        invalidate();
        try {
            mExecutorService.execute(mTask);
        } catch (RejectedExecutionException ree) {
        }

    }

    public void setIsCalibrateStart(boolean isCalibrateStart) {
        this.mIsCalibrateStart = isCalibrateStart;
    }

    public boolean isCalibrateStart() {
        return mIsCalibrateStart;
    }

    private class AngleState {
        State mState = State.NONE;
        float mAngle = -1;

        public AngleState(State state, float angle) {
            this.mState = state;
            this.mAngle = angle;
        }
    }

    /**
     * 当传感器精度是
     * SENSOR_STATUS_NO_CONTACT或者SENSOR_STATUS_UNRELIABLE
     * 是NONE
     */
    public enum State {
        NONE, HALF, FULL
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mAngleStates.size(); i++) {
            canvas.save();
            mPaint.setStrokeWidth(1);
            mPaint.setColor(Color.WHITE);
            AngleState angleState = mAngleStates.get(i);
            canvas.rotate(angleState.mAngle, mMiddlePointF.x, mMiddlePointF.y);
            canvas.drawLine(mMiddlePointF.x, mMiddlePointF.y - mPlateBottomOffsetFromMiddle,
                    mMiddlePointF.x, mMiddlePointF.y - mPlateBottomRadius, mPaint);
            canvas.restore();
        }
        for (int i = 0; i < mAngleStates.size(); i++) {
            canvas.save();
            AngleState angleState = mAngleStates.get(i);
            canvas.rotate(angleState.mAngle, mMiddlePointF.x, mMiddlePointF.y);
//            if (angleState.mState == State.NONE) {
//                mPaint.setColor(Color.WHITE);
//                canvas.drawLine(mMiddlePointF.x, mMiddlePointF.y - mPlateBottomOffsetFromMiddle,
//                        mMiddlePointF.x, mMiddlePointF.y - mPlateBottomRadius, mPaint);
//            } else
            mPaint.setStrokeWidth(5);
            if (angleState.mState == State.HALF) {
                mPaint.setColor(Color.RED);
                canvas.drawLine(mMiddlePointF.x, mMiddlePointF.y - mPlateMiddleOffsetFromMiddle,
                        mMiddlePointF.x, mMiddlePointF.y - mPlateMiddleRadius, mPaint);
            } else if (angleState.mState == State.FULL) {
                mPaint.setColor(Color.GREEN);
                canvas.drawLine(mMiddlePointF.x, mMiddlePointF.y - mPlateTopOffsetFromMiddle,
                        mMiddlePointF.x, mMiddlePointF.y - mPlateTopRadius, mPaint);
            }
            canvas.restore();
        }
        //画小球
        canvas.save();
        mPaint.setColor(Color.RED);
        canvas.rotate(mAngle, mMiddlePointF.x, mMiddlePointF.y);
        canvas.drawCircle(mMiddlePointF.x, mMiddlePointF.y - ballOffsetFromMiddlePoint, mBallRadius, mPaint);
        canvas.restore();
    }

}
