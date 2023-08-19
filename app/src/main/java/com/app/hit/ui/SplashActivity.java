package com.app.hit.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

        logo = findViewById(R.id.logo);
        Animation bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce);
        logo.startAnimation(bounceAnimation);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        },2000);
    }
}