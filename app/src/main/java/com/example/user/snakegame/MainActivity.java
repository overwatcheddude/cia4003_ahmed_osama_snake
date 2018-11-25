package com.example.user.snakegame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Animated the snake logo
        Animation leftoright = AnimationUtils.loadAnimation(this, R.anim.lefttoright);
        ImageView imgSnake = findViewById(R.id.imgSnake);
        imgSnake.startAnimation(leftoright);
    }

    public void OnActivityClick(View v)
    {
        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);

        General gen = new General(getApplicationContext());
        findViewById(v.getId()).startAnimation(rotate);
        switch (v.getId())
        {
            case R.id.btnStartGame:
                gen.GoToActivity(GameActivity.class); break;
            case R.id.btnHighScores:
                gen.GoToActivity(HighScores.class); break;
            case R.id.btnProfile:
                gen.GoToActivity(profile.class); break;
            default: gen.DisplayMessage("Unknown button clicked.");
        }
    }
}
