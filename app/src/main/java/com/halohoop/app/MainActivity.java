package com.halohoop.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.SeekBar;

import com.halohoop.app.views.CalibrateView;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private CalibrateView mCv;
    private float mAngle;
    private float mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCv = (CalibrateView) findViewById(R.id.cv);
        SeekBar mSbState = (SeekBar) findViewById(R.id.sb_state);
        SeekBar mSbAngle = (SeekBar) findViewById(R.id.sb_angle);
        mSbState.setOnSeekBarChangeListener(this);
        mSbAngle.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.sb_state) {
            mProgress = progress;
            Log.i("huanghaiqi", "huanghaiqi:mProgress:" + mProgress);
        } else if (seekBar.getId() == R.id.sb_angle) {
            mAngle = progress;
            Log.i("huanghaiqi", "huanghaiqi:mAngle:" + mAngle);
        }
        mCv.setReliableValue(mProgress, mAngle);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
