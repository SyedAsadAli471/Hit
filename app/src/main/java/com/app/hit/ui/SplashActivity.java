package com.app.hit.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.app.hit.R;
import com.app.hit.util.Prefs;

public class SplashActivity extends AppCompatActivity {

    ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Prefs.getInstance(getApplicationContext()).setString("IS_CONCUSSION", "");

        //Image animation
        final AppCompatImageView imageView = findViewById(R.id.logo);
        final float initialScale = 1.0f;
        final float targetScale = 1.5f;
        final long animationDuration = 2000L;

        ValueAnimator animator = ValueAnimator.ofFloat(initialScale, targetScale);
        animator.setDuration(animationDuration);

        animator.addUpdateListener(valueAnimator -> {
            float animatedValue = (float) valueAnimator.getAnimatedValue();
            imageView.setScaleX(animatedValue);
            imageView.setScaleY(animatedValue);
        });
        animator.start();
        //Image animation end


        final View lightView = findViewById(R.id.lightView);
        final int screenWidth = getResources().getDisplayMetrics().widthPixels;
        final long animationDuration1 = 300; // Duration of each animation cycle in milliseconds
        final int repeatCount = ValueAnimator.INFINITE; // Infinite repeat count
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(lightView, "alpha", 0f, 0.1f);
        alphaAnimator.setDuration(animationDuration1/2); // Half of the total duration

        ObjectAnimator translateXAnimator = ObjectAnimator.ofFloat(lightView, "translationX", 0f, screenWidth);
        translateXAnimator.setDuration(animationDuration1);
        translateXAnimator.setInterpolator(new LinearInterpolator()); // Linear interpolator for smooth back-and-forth motion
        translateXAnimator.setRepeatCount(repeatCount);
        translateXAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                float animatedValue = (float) animator.getAnimatedValue();
                lightView.setTranslationX(animatedValue);
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alphaAnimator, translateXAnimator);
        animatorSet.start();



        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, AuthActivity.class);
                startActivity(intent);
                finish();
            }
        },3000);
    }
}