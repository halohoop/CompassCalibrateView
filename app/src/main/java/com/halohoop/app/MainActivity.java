package com.halohoop.app;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.halohoop.app.views.CalibrateView;

public class MainActivity extends AppCompatActivity
        implements SensorEventListener {

    private CalibrateView mCv;
    private float mAngle;
    private float mProgress;
    private SensorManager mSensorManager;
    private Sensor gravitySensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCv = (CalibrateView) findViewById(R.id.cv);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, gravitySensor);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        handleSensorData(event.values);
    }

    private float mFullDistance = 98 * 2;//gravity 9.8 * 10 * 2

    private void handleSensorData(float[] values) {
        int x = Math.round(values[0] * 10);
        int y = Math.round(values[1] * 10);
        int z = Math.round(values[2] * 10);

        Log.i("huanghaiqi", "x:" + x + " y:" + y + " z:" + z);
        if (z <= mCv.getDefaultHalfReliableValue() && z >= -60) {//手机倾斜
            if (y <= 0) {
                //第1 2象限y <= 0,包括x轴上
                float deltaX = Math.abs(-98 - x);
                float precent = deltaX / mFullDistance;
                Log.i("huanghaiqi", "deltaX/mFullDistance=precent:" + deltaX + "/" + mFullDistance + "=" + precent);
                float angleDeltaFromXRight = 180 * precent;
                //从x轴右边，逆时针需要旋转的角度
                float finalAngleDeltaFromXRight = 90 - angleDeltaFromXRight;
                if (finalAngleDeltaFromXRight < 0) {
                    finalAngleDeltaFromXRight = 360 + finalAngleDeltaFromXRight;
                }
                Log.i("huanghaiqi", "finalAngleDeltaFromXRight:" + finalAngleDeltaFromXRight);
                if (mCv.isCalibrateStart()) {
                    finalAngleDeltaFromXRight = lowPass2(finalAngleDeltaFromXRight, mLastFinalAngleDeltaFromXRight);
                }
                mLastFinalAngleDeltaFromXRight = finalAngleDeltaFromXRight;
                mCv.setReliableValue(z, finalAngleDeltaFromXRight);
            } else {
                //第3 4象限y > 0
                float deltaX = Math.abs(-98 - x);
                float precent = deltaX / mFullDistance;
                Log.i("huanghaiqi", "deltaX/mFullDistance=precent:" + deltaX + "/" + mFullDistance + "=" + precent);
                float angleDeltaFromXRight = 180 * precent;
                //从x轴右边，逆时针需要旋转的角度
                float finalAngleDeltaFromXRight = 90 + angleDeltaFromXRight;
                Log.i("huanghaiqi", "finalAngleDeltaFromXRight:" + finalAngleDeltaFromXRight);
                if (mCv.isCalibrateStart()) {
                    finalAngleDeltaFromXRight = lowPass2(finalAngleDeltaFromXRight, mLastFinalAngleDeltaFromXRight);
                }
                mLastFinalAngleDeltaFromXRight = finalAngleDeltaFromXRight;

                mCv.setReliableValue(z, finalAngleDeltaFromXRight);
            }
            mCv.setIsCalibrateStart(true);
        }
    }

    private float mLastFinalAngleDeltaFromXRight = 0;

    /**
     * 平滑过度权重
     */
    private static final float ALPHA = 0.06f;

    public static float lowPass(float current, float last) {
        return last * (1.0f - ALPHA) + current * ALPHA;
    }

    public static float lowPass2(float current, float last) {
        float distance = (Math.min(current, last) - 0) + (360 - Math.max(current, last));
        if (distance < 180) {
            float left = 100;
            float right = left;
            if (current > last) {//last-->0    current-->360
                float distanceFrom360 = 360 - current;
                float distanceFrom0 = last - 0;
                right = left + distanceFrom360 + distanceFrom0;
                float tmpLowPass = lowPass(right, left);
                float lowPassDistanceFromLeft = tmpLowPass - left;
                if (distanceFrom0 >= lowPassDistanceFromLeft) {
                    return last - lowPassDistanceFromLeft;
                } else {
                    return 360 - (lowPassDistanceFromLeft - distanceFrom0);
                }
            } else if (current < last) {//last-->360    current-->0
                float distanceFrom360 = 360 - last;
                float distanceFrom0 = current - 0;
                right = left + distanceFrom360 + distanceFrom0;
                float tmpLowPass = lowPass(left, right);
                float lowPassDistanceFromLeft = tmpLowPass - left;
                if (distanceFrom0 >= lowPassDistanceFromLeft) {
                    return current - lowPassDistanceFromLeft;
                } else {
                    return 360 - (lowPassDistanceFromLeft - distanceFrom0);
                }
            }
        }
        return last * (1.0f - ALPHA) + current * ALPHA;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                Log.i("huanghaiqi", "accuracy:ACCURACY_HIGH");
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                Log.i("huanghaiqi", "accuracy:ACCURACY_MEDIUM");
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                Log.i("huanghaiqi", "accuracy:ACCURACY_LOW");
                break;
            case SensorManager.SENSOR_STATUS_NO_CONTACT:
                Log.i("huanghaiqi", "accuracy:NO_CONTACT");
                break;
            case SensorManager.SENSOR_STATUS_UNRELIABLE:
                Log.i("huanghaiqi", "accuracy:UNRELIABLE");
                break;
        }
    }
}
