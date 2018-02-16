package com.th.forge.sunset;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class SunsetFragment extends Fragment {

    private View mSceneView;
    private View mSunView;
    private View mSkyView;
    private View mSunReflectionView;
    private int mBlueSkyColor;
    private int mSunsetSkyColor;
    private int mNightSkyColor;

    ViewGroup.LayoutParams sunView;
    ViewGroup.LayoutParams skyView;

    private boolean mSunset = true;

    private final long DURATION = 3000L;
    private float mSunYCurrent = Float.NaN;

    private int mCurrentSkyColor;
    private int mCurrentNightSkyColor;

    private AnimatorSet mSunriseAnimator;
    private AnimatorSet mSunsetAnimator;
    private static final String TAG = "SunsetFragment";

    public static SunsetFragment newInstance() {
        return new SunsetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sunset, container, false);
        mSceneView = view;
        mSunView = view.findViewById(R.id.sun);
        mSunReflectionView = view.findViewById(R.id.sun_reflection);
        mSkyView = view.findViewById(R.id.sky);
        Resources resources = getResources();
        mBlueSkyColor = resources.getColor(R.color.blue_sky);
        mSunsetSkyColor = resources.getColor(R.color.sunset_sky);
        mNightSkyColor = resources.getColor(R.color.night_sky);
        sunView = mSunView.getLayoutParams();
        skyView = mSkyView.getLayoutParams();
        Log.d(TAG, "skyView " + skyView.width);
        Log.d(TAG, "SunReflection top: " + mSunReflectionView.getTop());
        Log.d(TAG, "SunView bottom " + mSunView.getBottom());
        Log.d(TAG, "SkyView height " + mSkyView.getHeight());
        Log.d(TAG, "SkySun subtraction " + (mSkyView.getHeight() - mSunView.getBottom()));
        mSunReflectionView.setTop(mSkyView.getHeight() + mSkyView.getHeight() - mSunView.getBottom());
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d(TAG, "onglobal... " + mSkyView.getHeight());
            }
        });
        mSceneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSunset) {
                    startSunset(true);
                    startSunrise(false);
                } else {
                    startSunrise(true);
                    startSunset(false);
                }
                mSunset = !mSunset;
            }
        });
        return view;
    }

    private void startSunrise(boolean isOn) {
        if (isOn) {
            ObjectAnimator heightAnimator;
            ObjectAnimator sunsetSkyAnimator;
            ObjectAnimator nightSkyAnimator;
            Log.d(TAG, "SunView sunrise getTop " + mSunView.getTop());
            float sunYStart = Float.isNaN(mSunYCurrent) ? mSkyView.getHeight() : mSunYCurrent;
            float sunYEnd = mSunView.getTop();

            long duration = (Float.valueOf(mSunYCurrent).isNaN() ?
                    DURATION : (long) (DURATION / (mSunView.getTop() - mSkyView.getHeight())
                    * (mSunView.getTop() - mSunYCurrent)));

            int sunriseSkyColorStart = Float.isNaN(mSunYCurrent) ? mSunsetSkyColor : mCurrentSkyColor;
            int nightSkyColorStart = mCurrentNightSkyColor == 0 ? mNightSkyColor : mCurrentNightSkyColor;

            heightAnimator = ObjectAnimator
                    .ofFloat(mSunView, "y", sunYStart, sunYEnd)
                    .setDuration(duration);
            heightAnimator.setInterpolator(new DecelerateInterpolator());
            heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mSunYCurrent = (float) animation.getAnimatedValue();
                }
            });

//            sunsetSkyAnimator = ObjectAnimator
//                    .ofInt(mSkyView, "backgroundColor", sunriseSkyColorStart, mBlueSkyColor)
//                    .setDuration(duration);
            sunsetSkyAnimator = ObjectAnimator
                    .ofObject(mSkyView, "backgroundColor", new ArgbEvaluator(), sunriseSkyColorStart, mBlueSkyColor)
                    .setDuration(duration);
//            sunsetSkyAnimator.setEvaluator(new ArgbEvaluator());
            sunsetSkyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mCurrentSkyColor = (int) animation.getAnimatedValue();
                }
            });

            /*nightSkyAnimator = ObjectAnimator
                    .ofObject(mSkyView, "backgroundColor", new ArgbEvaluator(), nightSkyColorStart, mSunsetSkyColor)
                    .setDuration(mSunYCurrent == mSkyView.getHeight() ? DURATION / 2 : 0);*/
            nightSkyAnimator = ObjectAnimator
                    .ofInt(mSkyView, "backgroundColor", nightSkyColorStart, mSunsetSkyColor)
                    .setDuration(mSunYCurrent == mSkyView.getHeight() ? DURATION / 2 : 0);
            nightSkyAnimator.setEvaluator(new ArgbEvaluator());
            nightSkyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mCurrentNightSkyColor = (int) animation.getAnimatedValue();
                }
            });

            mSunriseAnimator = new AnimatorSet();
            mSunriseAnimator.play(heightAnimator)
                    .with(sunsetSkyAnimator)
                    .after(nightSkyAnimator);
            mSunriseAnimator.start();
        } else {
            if (mSunriseAnimator != null) {
                mSunriseAnimator.end();
            }
            mSunriseAnimator = null;
        }
    }

    private void startSunset(boolean isOn) {
        if (isOn) {
            ObjectAnimator heightAnimator;
            ObjectAnimator sunsetSkyAnimator;
            ObjectAnimator nightSkyAnimator;

            Log.d(TAG, "sv " + sunView.height);

            float sunYStart = Float.isNaN(mSunYCurrent) ? mSunView.getTop() : mSunYCurrent;
            float sunYEnd = mSkyView.getHeight();

            long duration = Float.isNaN(mSunYCurrent) ? DURATION :
                    (long) (DURATION / (mSkyView.getHeight() - mSunView.getTop()) * (mSkyView.getHeight() - mSunYCurrent));

            int sunsetSkyColorStart = mCurrentSkyColor == 0 ? mBlueSkyColor : mCurrentSkyColor;
            int nightSkyColorStart = mCurrentNightSkyColor == 0 ? mSunsetSkyColor : mCurrentNightSkyColor;

            heightAnimator = ObjectAnimator
                    .ofFloat(mSunView, "y", sunYStart, sunYEnd)
                    .setDuration(duration);
            heightAnimator.setInterpolator(new AccelerateInterpolator());
            heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mSunYCurrent = (float) animation.getAnimatedValue();
                }
            });

            sunsetSkyAnimator = ObjectAnimator
                    .ofInt(mSkyView, "backgroundColor", sunsetSkyColorStart, mSunsetSkyColor)
                    .setDuration(duration);
            sunsetSkyAnimator.setEvaluator(new ArgbEvaluator());
            sunsetSkyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mCurrentSkyColor = (int) animation.getAnimatedValue();
                }
            });

            nightSkyAnimator = ObjectAnimator
                    .ofInt(mSkyView, "backgroundColor", nightSkyColorStart, mNightSkyColor)
                    .setDuration(DURATION / 2);
            nightSkyAnimator.setEvaluator(new ArgbEvaluator());
            nightSkyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mCurrentNightSkyColor = (int) animation.getAnimatedValue();
                }
            });

            mSunsetAnimator = new AnimatorSet();
            mSunsetAnimator.play(heightAnimator)
                    .with(sunsetSkyAnimator)
                    .before(nightSkyAnimator);
            mSunsetAnimator.start();
        } else {
            if (mSunsetAnimator != null) {
                mSunsetAnimator.end();
            }
            mSunsetAnimator = null;
        }
    }


}
