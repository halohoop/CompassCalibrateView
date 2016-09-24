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

/**
 * Created by halohoop on 16-9-24.
 */
public class CalibrateView extends View {

    private float mEveryStepAngle;
    private int mMeasuredWidth;
    private int mMeasuredHeight;
    private int mEveryStepCount;
    private PointF mMiddlePointF;
    private Paint mPaint;
    private float mPlateBottomRadius;
    private float mPlateBottomOffsetFromMiddle;
    private float mPlateMiddleRadius;
    private float mPlateMiddleOffsetFromMiddle;
    private float mPlateTopRadius;
    private float mPlateTopOffsetFromMiddle;

    public CalibrateView(Context context) {
        this(context, null);
    }

    public CalibrateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalibrateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mEveryStepCount = 15 * 12;
        mEveryStepAngle = 360 / mEveryStepCount;
        //init every angle state to be uncalibrated
        for (int i = 0; i < mEveryStepCount; i++) {
            if (i == 0) {
                mAngleStates.add(new AngleState(State.NONE, 0));
                continue;
            }
            mAngleStates.add(new AngleState(State.NONE,
                    mAngleStates.get(mAngleStates.size() - 1).mAngle + mEveryStepAngle));
        }
        mPlateBottomRadius = 150;
        mPlateBottomOffsetFromMiddle = 100;
        mPlateMiddleRadius = 200;
        mPlateMiddleOffsetFromMiddle = 150;
        mPlateTopRadius = 250;
        mPlateTopOffsetFromMiddle = 200;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
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

    private List<AngleState> mAngleStates = new ArrayList<>();

    private class AngleState {
        State mState = State.NONE;
        float mAngle = -1;

        public AngleState(State state, float angle) {
            this.mState = state;
            this.mAngle = angle;
        }
    }

    public enum State {
        NONE, HALF, DONE
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //draw the bottom plate
        canvas.save();
        mPaint.setColor(Color.WHITE);
        for (int i = 0; i < mEveryStepCount; i++) {
            canvas.rotate(mEveryStepAngle, mMiddlePointF.x, mMiddlePointF.y);
            canvas.drawLine(mMiddlePointF.x, mMiddlePointF.y - mPlateBottomOffsetFromMiddle,
                    mMiddlePointF.x, mMiddlePointF.y - mPlateBottomRadius, mPaint);
        }
        canvas.restore();
        //draw the middle plate
        canvas.save();
        mPaint.setColor(Color.RED);
        for (int i = 0; i < mEveryStepCount; i++) {
            canvas.rotate(mEveryStepAngle, mMiddlePointF.x, mMiddlePointF.y);
            canvas.drawLine(mMiddlePointF.x, mMiddlePointF.y - mPlateMiddleOffsetFromMiddle,
                    mMiddlePointF.x, mMiddlePointF.y - mPlateMiddleRadius, mPaint);
        }
        canvas.restore();
        //draw the top plate
        canvas.save();
        mPaint.setColor(Color.GREEN);
        for (int i = 0; i < mEveryStepCount; i++) {
            canvas.rotate(mEveryStepAngle, mMiddlePointF.x, mMiddlePointF.y);
            canvas.drawLine(mMiddlePointF.x, mMiddlePointF.y - mPlateTopOffsetFromMiddle,
                    mMiddlePointF.x, mMiddlePointF.y - mPlateTopRadius, mPaint);
        }
        canvas.restore();

    }


}
